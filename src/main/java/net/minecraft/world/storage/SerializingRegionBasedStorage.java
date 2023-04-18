package net.minecraft.world.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SerializingRegionBasedStorage implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String SECTIONS_KEY = "Sections";
   private final StorageIoWorker worker;
   private final Long2ObjectMap loadedElements = new Long2ObjectOpenHashMap();
   private final LongLinkedOpenHashSet unsavedElements = new LongLinkedOpenHashSet();
   private final Function codecFactory;
   private final Function factory;
   private final DataFixer dataFixer;
   private final DataFixTypes dataFixTypes;
   private final DynamicRegistryManager dynamicRegistryManager;
   protected final HeightLimitView world;

   public SerializingRegionBasedStorage(Path path, Function codecFactory, Function factory, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean dsync, DynamicRegistryManager dynamicRegistryManager, HeightLimitView world) {
      this.codecFactory = codecFactory;
      this.factory = factory;
      this.dataFixer = dataFixer;
      this.dataFixTypes = dataFixTypes;
      this.dynamicRegistryManager = dynamicRegistryManager;
      this.world = world;
      this.worker = new StorageIoWorker(path, dsync, path.getFileName().toString());
   }

   protected void tick(BooleanSupplier shouldKeepTicking) {
      while(this.hasUnsavedElements() && shouldKeepTicking.getAsBoolean()) {
         ChunkPos lv = ChunkSectionPos.from(this.unsavedElements.firstLong()).toChunkPos();
         this.save(lv);
      }

   }

   public boolean hasUnsavedElements() {
      return !this.unsavedElements.isEmpty();
   }

   @Nullable
   protected Optional getIfLoaded(long pos) {
      return (Optional)this.loadedElements.get(pos);
   }

   protected Optional get(long pos) {
      if (this.isPosInvalid(pos)) {
         return Optional.empty();
      } else {
         Optional optional = this.getIfLoaded(pos);
         if (optional != null) {
            return optional;
         } else {
            this.loadDataAt(ChunkSectionPos.from(pos).toChunkPos());
            optional = this.getIfLoaded(pos);
            if (optional == null) {
               throw (IllegalStateException)Util.throwOrPause(new IllegalStateException());
            } else {
               return optional;
            }
         }
      }
   }

   protected boolean isPosInvalid(long pos) {
      int i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(pos));
      return this.world.isOutOfHeightLimit(i);
   }

   protected Object getOrCreate(long pos) {
      if (this.isPosInvalid(pos)) {
         throw (IllegalArgumentException)Util.throwOrPause(new IllegalArgumentException("sectionPos out of bounds"));
      } else {
         Optional optional = this.get(pos);
         if (optional.isPresent()) {
            return optional.get();
         } else {
            Object object = this.factory.apply(() -> {
               this.onUpdate(pos);
            });
            this.loadedElements.put(pos, Optional.of(object));
            return object;
         }
      }
   }

   private void loadDataAt(ChunkPos pos) {
      Optional optional = (Optional)this.loadNbt(pos).join();
      RegistryOps lv = RegistryOps.of(NbtOps.INSTANCE, (RegistryWrapper.WrapperLookup)this.dynamicRegistryManager);
      this.update(pos, lv, (NbtElement)optional.orElse((Object)null));
   }

   private CompletableFuture loadNbt(ChunkPos pos) {
      return this.worker.readChunkData(pos).exceptionally((throwable) -> {
         if (throwable instanceof IOException iOException) {
            LOGGER.error("Error reading chunk {} data from disk", pos, iOException);
            return Optional.empty();
         } else {
            throw new CompletionException(throwable);
         }
      });
   }

   private void update(ChunkPos pos, DynamicOps ops, @Nullable Object data) {
      if (data == null) {
         for(int i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
            this.loadedElements.put(chunkSectionPosAsLong(pos, i), Optional.empty());
         }
      } else {
         Dynamic dynamic = new Dynamic(ops, data);
         int j = getDataVersion(dynamic);
         int k = SharedConstants.getGameVersion().getSaveVersion().getId();
         boolean bl = j != k;
         Dynamic dynamic2 = this.dataFixTypes.update(this.dataFixer, dynamic, j, k);
         OptionalDynamic optionalDynamic = dynamic2.get("Sections");

         for(int l = this.world.getBottomSectionCoord(); l < this.world.getTopSectionCoord(); ++l) {
            long m = chunkSectionPosAsLong(pos, l);
            Optional optional = optionalDynamic.get(Integer.toString(l)).result().flatMap((dynamicx) -> {
               DataResult var10000 = ((Codec)this.codecFactory.apply(() -> {
                  this.onUpdate(m);
               })).parse(dynamicx);
               Logger var10001 = LOGGER;
               Objects.requireNonNull(var10001);
               return var10000.resultOrPartial(var10001::error);
            });
            this.loadedElements.put(m, optional);
            optional.ifPresent((sections) -> {
               this.onLoad(m);
               if (bl) {
                  this.onUpdate(m);
               }

            });
         }
      }

   }

   private void save(ChunkPos pos) {
      RegistryOps lv = RegistryOps.of(NbtOps.INSTANCE, (RegistryWrapper.WrapperLookup)this.dynamicRegistryManager);
      Dynamic dynamic = this.serialize(pos, lv);
      NbtElement lv2 = (NbtElement)dynamic.getValue();
      if (lv2 instanceof NbtCompound) {
         this.worker.setResult(pos, (NbtCompound)lv2);
      } else {
         LOGGER.error("Expected compound tag, got {}", lv2);
      }

   }

   private Dynamic serialize(ChunkPos chunkPos, DynamicOps ops) {
      Map map = Maps.newHashMap();

      for(int i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
         long l = chunkSectionPosAsLong(chunkPos, i);
         this.unsavedElements.remove(l);
         Optional optional = (Optional)this.loadedElements.get(l);
         if (optional != null && optional.isPresent()) {
            DataResult dataResult = ((Codec)this.codecFactory.apply(() -> {
               this.onUpdate(l);
            })).encodeStart(ops, optional.get());
            String string = Integer.toString(i);
            Logger var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            dataResult.resultOrPartial(var10001::error).ifPresent((object) -> {
               map.put(ops.createString(string), object);
            });
         }
      }

      return new Dynamic(ops, ops.createMap(ImmutableMap.of(ops.createString("Sections"), ops.createMap(map), ops.createString("DataVersion"), ops.createInt(SharedConstants.getGameVersion().getSaveVersion().getId()))));
   }

   private static long chunkSectionPosAsLong(ChunkPos chunkPos, int y) {
      return ChunkSectionPos.asLong(chunkPos.x, y, chunkPos.z);
   }

   protected void onLoad(long pos) {
   }

   protected void onUpdate(long pos) {
      Optional optional = (Optional)this.loadedElements.get(pos);
      if (optional != null && optional.isPresent()) {
         this.unsavedElements.add(pos);
      } else {
         LOGGER.warn("No data for position: {}", ChunkSectionPos.from(pos));
      }
   }

   private static int getDataVersion(Dynamic dynamic) {
      return dynamic.get("DataVersion").asInt(1945);
   }

   public void saveChunk(ChunkPos pos) {
      if (this.hasUnsavedElements()) {
         for(int i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
            long l = chunkSectionPosAsLong(pos, i);
            if (this.unsavedElements.contains(l)) {
               this.save(pos);
               return;
            }
         }
      }

   }

   public void close() throws IOException {
      this.worker.close();
   }
}
