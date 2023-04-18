package net.minecraft.world.updater;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.slf4j.Logger;

public class WorldUpdater {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ThreadFactory UPDATE_THREAD_FACTORY = (new ThreadFactoryBuilder()).setDaemon(true).build();
   private final Registry dimensionOptionsRegistry;
   private final Set worldKeys;
   private final boolean eraseCache;
   private final LevelStorage.Session session;
   private final Thread updateThread;
   private final DataFixer dataFixer;
   private volatile boolean keepUpgradingChunks = true;
   private volatile boolean done;
   private volatile float progress;
   private volatile int totalChunkCount;
   private volatile int upgradedChunkCount;
   private volatile int skippedChunkCount;
   private final Object2FloatMap dimensionProgress = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap(Util.identityHashStrategy()));
   private volatile Text status = Text.translatable("optimizeWorld.stage.counting");
   private static final Pattern REGION_FILE_PATTERN = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
   private final PersistentStateManager persistentStateManager;

   public WorldUpdater(LevelStorage.Session session, DataFixer dataFixer, Registry dimensionOptionsRegistry, boolean eraseCache) {
      this.dimensionOptionsRegistry = dimensionOptionsRegistry;
      this.worldKeys = (Set)dimensionOptionsRegistry.getKeys().stream().map(RegistryKeys::toWorldKey).collect(Collectors.toUnmodifiableSet());
      this.eraseCache = eraseCache;
      this.dataFixer = dataFixer;
      this.session = session;
      this.persistentStateManager = new PersistentStateManager(this.session.getWorldDirectory(World.OVERWORLD).resolve("data").toFile(), dataFixer);
      this.updateThread = UPDATE_THREAD_FACTORY.newThread(this::updateWorld);
      this.updateThread.setUncaughtExceptionHandler((thread, throwable) -> {
         LOGGER.error("Error upgrading world", throwable);
         this.status = Text.translatable("optimizeWorld.stage.failed");
         this.done = true;
      });
      this.updateThread.start();
   }

   public void cancel() {
      this.keepUpgradingChunks = false;

      try {
         this.updateThread.join();
      } catch (InterruptedException var2) {
      }

   }

   private void updateWorld() {
      this.totalChunkCount = 0;
      ImmutableMap.Builder builder = ImmutableMap.builder();

      List list;
      for(Iterator var2 = this.worldKeys.iterator(); var2.hasNext(); this.totalChunkCount += list.size()) {
         RegistryKey lv = (RegistryKey)var2.next();
         list = this.getChunkPositions(lv);
         builder.put(lv, list.listIterator());
      }

      if (this.totalChunkCount == 0) {
         this.done = true;
      } else {
         float f = (float)this.totalChunkCount;
         ImmutableMap immutableMap = builder.build();
         ImmutableMap.Builder builder2 = ImmutableMap.builder();
         Iterator var5 = this.worldKeys.iterator();

         while(var5.hasNext()) {
            RegistryKey lv2 = (RegistryKey)var5.next();
            Path path = this.session.getWorldDirectory(lv2);
            builder2.put(lv2, new VersionedChunkStorage(path.resolve("region"), this.dataFixer, true));
         }

         ImmutableMap immutableMap2 = builder2.build();
         long l = Util.getMeasuringTimeMs();
         this.status = Text.translatable("optimizeWorld.stage.upgrading");

         while(this.keepUpgradingChunks) {
            boolean bl = false;
            float g = 0.0F;

            float h;
            for(Iterator var10 = this.worldKeys.iterator(); var10.hasNext(); g += h) {
               RegistryKey lv3 = (RegistryKey)var10.next();
               ListIterator listIterator = (ListIterator)immutableMap.get(lv3);
               VersionedChunkStorage lv4 = (VersionedChunkStorage)immutableMap2.get(lv3);
               if (listIterator.hasNext()) {
                  ChunkPos lv5 = (ChunkPos)listIterator.next();
                  boolean bl2 = false;

                  try {
                     NbtCompound lv6 = (NbtCompound)((Optional)lv4.getNbt(lv5).join()).orElse((Object)null);
                     if (lv6 != null) {
                        int i = VersionedChunkStorage.getDataVersion(lv6);
                        ChunkGenerator lv7 = ((DimensionOptions)this.dimensionOptionsRegistry.getOrThrow(RegistryKeys.toDimensionKey(lv3))).chunkGenerator();
                        NbtCompound lv8 = lv4.updateChunkNbt(lv3, () -> {
                           return this.persistentStateManager;
                        }, lv6, lv7.getCodecKey());
                        ChunkPos lv9 = new ChunkPos(lv8.getInt("xPos"), lv8.getInt("zPos"));
                        if (!lv9.equals(lv5)) {
                           LOGGER.warn("Chunk {} has invalid position {}", lv5, lv9);
                        }

                        boolean bl3 = i < SharedConstants.getGameVersion().getSaveVersion().getId();
                        if (this.eraseCache) {
                           bl3 = bl3 || lv8.contains("Heightmaps");
                           lv8.remove("Heightmaps");
                           bl3 = bl3 || lv8.contains("isLightOn");
                           lv8.remove("isLightOn");
                           NbtList lv10 = lv8.getList("sections", NbtElement.COMPOUND_TYPE);

                           for(int j = 0; j < lv10.size(); ++j) {
                              NbtCompound lv11 = lv10.getCompound(j);
                              bl3 = bl3 || lv11.contains("BlockLight");
                              lv11.remove("BlockLight");
                              bl3 = bl3 || lv11.contains("SkyLight");
                              lv11.remove("SkyLight");
                           }
                        }

                        if (bl3) {
                           lv4.setNbt(lv5, lv8);
                           bl2 = true;
                        }
                     }
                  } catch (CompletionException | CrashException var26) {
                     Throwable throwable = var26.getCause();
                     if (!(throwable instanceof IOException)) {
                        throw var26;
                     }

                     LOGGER.error("Error upgrading chunk {}", lv5, throwable);
                  }

                  if (bl2) {
                     ++this.upgradedChunkCount;
                  } else {
                     ++this.skippedChunkCount;
                  }

                  bl = true;
               }

               h = (float)listIterator.nextIndex() / f;
               this.dimensionProgress.put(lv3, h);
            }

            this.progress = g;
            if (!bl) {
               this.keepUpgradingChunks = false;
            }
         }

         this.status = Text.translatable("optimizeWorld.stage.finished");
         UnmodifiableIterator var32 = immutableMap2.values().iterator();

         while(var32.hasNext()) {
            VersionedChunkStorage lv12 = (VersionedChunkStorage)var32.next();

            try {
               lv12.close();
            } catch (IOException var25) {
               LOGGER.error("Error upgrading chunk", var25);
            }
         }

         this.persistentStateManager.save();
         l = Util.getMeasuringTimeMs() - l;
         LOGGER.info("World optimizaton finished after {} ms", l);
         this.done = true;
      }
   }

   private List getChunkPositions(RegistryKey world) {
      File file = this.session.getWorldDirectory(world).toFile();
      File file2 = new File(file, "region");
      File[] files = file2.listFiles((directory, name) -> {
         return name.endsWith(".mca");
      });
      if (files == null) {
         return ImmutableList.of();
      } else {
         List list = Lists.newArrayList();
         File[] var6 = files;
         int var7 = files.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            File file3 = var6[var8];
            Matcher matcher = REGION_FILE_PATTERN.matcher(file3.getName());
            if (matcher.matches()) {
               int i = Integer.parseInt(matcher.group(1)) << 5;
               int j = Integer.parseInt(matcher.group(2)) << 5;

               try {
                  RegionFile lv = new RegionFile(file3.toPath(), file2.toPath(), true);

                  try {
                     for(int k = 0; k < 32; ++k) {
                        for(int l = 0; l < 32; ++l) {
                           ChunkPos lv2 = new ChunkPos(k + i, l + j);
                           if (lv.isChunkValid(lv2)) {
                              list.add(lv2);
                           }
                        }
                     }
                  } catch (Throwable var18) {
                     try {
                        lv.close();
                     } catch (Throwable var17) {
                        var18.addSuppressed(var17);
                     }

                     throw var18;
                  }

                  lv.close();
               } catch (Throwable var19) {
               }
            }
         }

         return list;
      }
   }

   public boolean isDone() {
      return this.done;
   }

   public Set getWorlds() {
      return this.worldKeys;
   }

   public float getProgress(RegistryKey world) {
      return this.dimensionProgress.getFloat(world);
   }

   public float getProgress() {
      return this.progress;
   }

   public int getTotalChunkCount() {
      return this.totalChunkCount;
   }

   public int getUpgradedChunkCount() {
      return this.upgradedChunkCount;
   }

   public int getSkippedChunkCount() {
      return this.skippedChunkCount;
   }

   public Text getStatus() {
      return this.status;
   }
}
