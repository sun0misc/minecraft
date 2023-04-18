package net.minecraft.world;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtShort;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.SimpleTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkSerializer {
   private static final Codec CODEC;
   private static final Logger LOGGER;
   private static final String UPGRADE_DATA_KEY = "UpgradeData";
   private static final String BLOCK_TICKS = "block_ticks";
   private static final String FLUID_TICKS = "fluid_ticks";
   public static final String X_POS_KEY = "xPos";
   public static final String Z_POS_KEY = "zPos";
   public static final String HEIGHTMAPS_KEY = "Heightmaps";
   public static final String IS_LIGHT_ON_KEY = "isLightOn";
   public static final String SECTIONS_KEY = "sections";
   public static final String BLOCK_LIGHT_KEY = "BlockLight";
   public static final String SKY_LIGHT_KEY = "SkyLight";

   public static ProtoChunk deserialize(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos, NbtCompound nbt) {
      ChunkPos lv = new ChunkPos(nbt.getInt("xPos"), nbt.getInt("zPos"));
      if (!Objects.equals(chunkPos, lv)) {
         LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[]{chunkPos, chunkPos, lv});
      }

      UpgradeData lv2 = nbt.contains("UpgradeData", NbtElement.COMPOUND_TYPE) ? new UpgradeData(nbt.getCompound("UpgradeData"), world) : UpgradeData.NO_UPGRADE_DATA;
      boolean bl = nbt.getBoolean("isLightOn");
      NbtList lv3 = nbt.getList("sections", NbtElement.COMPOUND_TYPE);
      int i = world.countVerticalSections();
      ChunkSection[] lvs = new ChunkSection[i];
      boolean bl2 = world.getDimension().hasSkyLight();
      ChunkManager lv4 = world.getChunkManager();
      LightingProvider lv5 = lv4.getLightingProvider();
      Registry lv6 = world.getRegistryManager().get(RegistryKeys.BIOME);
      Codec codec = createCodec(lv6);
      boolean bl3 = false;

      DataResult var10000;
      for(int j = 0; j < lv3.size(); ++j) {
         NbtCompound lv7 = lv3.getCompound(j);
         int k = lv7.getByte("Y");
         int l = world.sectionCoordToIndex(k);
         if (l >= 0 && l < lvs.length) {
            Logger var10002;
            PalettedContainer lv8;
            if (lv7.contains("block_states", NbtElement.COMPOUND_TYPE)) {
               var10000 = CODEC.parse(NbtOps.INSTANCE, lv7.getCompound("block_states")).promotePartial((errorMessage) -> {
                  logRecoverableError(chunkPos, k, errorMessage);
               });
               var10002 = LOGGER;
               Objects.requireNonNull(var10002);
               lv8 = (PalettedContainer)var10000.getOrThrow(false, var10002::error);
            } else {
               lv8 = new PalettedContainer(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
            }

            Object lv9;
            if (lv7.contains("biomes", NbtElement.COMPOUND_TYPE)) {
               var10000 = codec.parse(NbtOps.INSTANCE, lv7.getCompound("biomes")).promotePartial((errorMessage) -> {
                  logRecoverableError(chunkPos, k, errorMessage);
               });
               var10002 = LOGGER;
               Objects.requireNonNull(var10002);
               lv9 = (ReadableContainer)var10000.getOrThrow(false, var10002::error);
            } else {
               lv9 = new PalettedContainer(lv6.getIndexedEntries(), lv6.entryOf(BiomeKeys.PLAINS), PalettedContainer.PaletteProvider.BIOME);
            }

            ChunkSection lv10 = new ChunkSection(k, lv8, (ReadableContainer)lv9);
            lvs[l] = lv10;
            poiStorage.initForPalette(chunkPos, lv10);
         }

         boolean bl4 = lv7.contains("BlockLight", NbtElement.BYTE_ARRAY_TYPE);
         boolean bl5 = bl2 && lv7.contains("SkyLight", NbtElement.BYTE_ARRAY_TYPE);
         if (bl4 || bl5) {
            if (!bl3) {
               lv5.setRetainData(chunkPos, true);
               bl3 = true;
            }

            if (bl4) {
               lv5.enqueueSectionData(LightType.BLOCK, ChunkSectionPos.from(chunkPos, k), new ChunkNibbleArray(lv7.getByteArray("BlockLight")), true);
            }

            if (bl5) {
               lv5.enqueueSectionData(LightType.SKY, ChunkSectionPos.from(chunkPos, k), new ChunkNibbleArray(lv7.getByteArray("SkyLight")), true);
            }
         }
      }

      long m = nbt.getLong("InhabitedTime");
      ChunkStatus.ChunkType lv11 = getChunkType(nbt);
      Logger var10001;
      BlendingData lv12;
      if (nbt.contains("blending_data", NbtElement.COMPOUND_TYPE)) {
         var10000 = BlendingData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, nbt.getCompound("blending_data")));
         var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         lv12 = (BlendingData)var10000.resultOrPartial(var10001::error).orElse((Object)null);
      } else {
         lv12 = null;
      }

      Object lv15;
      if (lv11 == ChunkStatus.ChunkType.LEVELCHUNK) {
         ChunkTickScheduler lv13 = ChunkTickScheduler.create(nbt.getList("block_ticks", NbtElement.COMPOUND_TYPE), (id) -> {
            return Registries.BLOCK.getOrEmpty(Identifier.tryParse(id));
         }, chunkPos);
         ChunkTickScheduler lv14 = ChunkTickScheduler.create(nbt.getList("fluid_ticks", NbtElement.COMPOUND_TYPE), (id) -> {
            return Registries.FLUID.getOrEmpty(Identifier.tryParse(id));
         }, chunkPos);
         lv15 = new WorldChunk(world.toServerWorld(), chunkPos, lv2, lv13, lv14, m, lvs, getEntityLoadingCallback(world, nbt), lv12);
      } else {
         SimpleTickScheduler lv16 = SimpleTickScheduler.tick(nbt.getList("block_ticks", NbtElement.COMPOUND_TYPE), (id) -> {
            return Registries.BLOCK.getOrEmpty(Identifier.tryParse(id));
         }, chunkPos);
         SimpleTickScheduler lv17 = SimpleTickScheduler.tick(nbt.getList("fluid_ticks", NbtElement.COMPOUND_TYPE), (id) -> {
            return Registries.FLUID.getOrEmpty(Identifier.tryParse(id));
         }, chunkPos);
         ProtoChunk lv18 = new ProtoChunk(chunkPos, lv2, lvs, lv16, lv17, world, lv6, lv12);
         lv15 = lv18;
         lv18.setInhabitedTime(m);
         if (nbt.contains("below_zero_retrogen", NbtElement.COMPOUND_TYPE)) {
            var10000 = BelowZeroRetrogen.CODEC.parse(new Dynamic(NbtOps.INSTANCE, nbt.getCompound("below_zero_retrogen")));
            var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            Optional var34 = var10000.resultOrPartial(var10001::error);
            Objects.requireNonNull(lv18);
            var34.ifPresent(lv18::setBelowZeroRetrogen);
         }

         ChunkStatus lv19 = ChunkStatus.byId(nbt.getString("Status"));
         lv18.setStatus(lv19);
         if (lv19.isAtLeast(ChunkStatus.FEATURES)) {
            lv18.setLightingProvider(lv5);
         }

         BelowZeroRetrogen lv20 = lv18.getBelowZeroRetrogen();
         boolean bl6 = lv19.isAtLeast(ChunkStatus.LIGHT) || lv20 != null && lv20.getTargetStatus().isAtLeast(ChunkStatus.LIGHT);
         if (!bl && bl6) {
            Iterator var27 = BlockPos.iterate(chunkPos.getStartX(), world.getBottomY(), chunkPos.getStartZ(), chunkPos.getEndX(), world.getTopY() - 1, chunkPos.getEndZ()).iterator();

            while(var27.hasNext()) {
               BlockPos lv21 = (BlockPos)var27.next();
               if (((Chunk)lv15).getBlockState(lv21).getLuminance() != 0) {
                  lv18.addLightSource(lv21);
               }
            }
         }
      }

      ((Chunk)lv15).setLightOn(bl);
      NbtCompound lv22 = nbt.getCompound("Heightmaps");
      EnumSet enumSet = EnumSet.noneOf(Heightmap.Type.class);
      Iterator var46 = ((Chunk)lv15).getStatus().getHeightmapTypes().iterator();

      while(var46.hasNext()) {
         Heightmap.Type lv23 = (Heightmap.Type)var46.next();
         String string = lv23.getName();
         if (lv22.contains(string, NbtElement.LONG_ARRAY_TYPE)) {
            ((Chunk)lv15).setHeightmap(lv23, lv22.getLongArray(string));
         } else {
            enumSet.add(lv23);
         }
      }

      Heightmap.populateHeightmaps((Chunk)lv15, enumSet);
      NbtCompound lv24 = nbt.getCompound("structures");
      ((Chunk)lv15).setStructureStarts(readStructureStarts(StructureContext.from(world), lv24, world.getSeed()));
      ((Chunk)lv15).setStructureReferences(readStructureReferences(world.getRegistryManager(), chunkPos, lv24));
      if (nbt.getBoolean("shouldSave")) {
         ((Chunk)lv15).setNeedsSaving(true);
      }

      NbtList lv25 = nbt.getList("PostProcessing", NbtElement.LIST_TYPE);

      NbtList lv26;
      int o;
      for(int n = 0; n < lv25.size(); ++n) {
         lv26 = lv25.getList(n);

         for(o = 0; o < lv26.size(); ++o) {
            ((Chunk)lv15).markBlockForPostProcessing(lv26.getShort(o), n);
         }
      }

      if (lv11 == ChunkStatus.ChunkType.LEVELCHUNK) {
         return new ReadOnlyChunk((WorldChunk)lv15, false);
      } else {
         ProtoChunk lv27 = (ProtoChunk)lv15;
         lv26 = nbt.getList("entities", NbtElement.COMPOUND_TYPE);

         for(o = 0; o < lv26.size(); ++o) {
            lv27.addEntity(lv26.getCompound(o));
         }

         NbtList lv28 = nbt.getList("block_entities", NbtElement.COMPOUND_TYPE);

         NbtCompound lv29;
         for(int p = 0; p < lv28.size(); ++p) {
            lv29 = lv28.getCompound(p);
            ((Chunk)lv15).addPendingBlockEntityNbt(lv29);
         }

         NbtList lv30 = nbt.getList("Lights", NbtElement.LIST_TYPE);

         for(int q = 0; q < lv30.size(); ++q) {
            ChunkSection lv31 = lvs[q];
            if (lv31 != null && !lv31.isEmpty()) {
               NbtList lv32 = lv30.getList(q);

               for(int r = 0; r < lv32.size(); ++r) {
                  lv27.addLightSource(lv32.getShort(r), q);
               }
            }
         }

         lv29 = nbt.getCompound("CarvingMasks");
         Iterator var59 = lv29.getKeys().iterator();

         while(var59.hasNext()) {
            String string2 = (String)var59.next();
            GenerationStep.Carver lv33 = GenerationStep.Carver.valueOf(string2);
            lv27.setCarvingMask(lv33, new CarvingMask(lv29.getLongArray(string2), ((Chunk)lv15).getBottomY()));
         }

         return lv27;
      }
   }

   private static void logRecoverableError(ChunkPos chunkPos, int y, String message) {
      LOGGER.error("Recoverable errors when loading section [" + chunkPos.x + ", " + y + ", " + chunkPos.z + "]: " + message);
   }

   private static Codec createCodec(Registry biomeRegistry) {
      return PalettedContainer.createReadableContainerCodec(biomeRegistry.getIndexedEntries(), biomeRegistry.createEntryCodec(), PalettedContainer.PaletteProvider.BIOME, biomeRegistry.entryOf(BiomeKeys.PLAINS));
   }

   public static NbtCompound serialize(ServerWorld world, Chunk chunk) {
      ChunkPos lv = chunk.getPos();
      NbtCompound lv2 = NbtHelper.putDataVersion(new NbtCompound());
      lv2.putInt("xPos", lv.x);
      lv2.putInt("yPos", chunk.getBottomSectionCoord());
      lv2.putInt("zPos", lv.z);
      lv2.putLong("LastUpdate", world.getTime());
      lv2.putLong("InhabitedTime", chunk.getInhabitedTime());
      lv2.putString("Status", chunk.getStatus().getId());
      BlendingData lv3 = chunk.getBlendingData();
      DataResult var10000;
      Logger var10001;
      if (lv3 != null) {
         var10000 = BlendingData.CODEC.encodeStart(NbtOps.INSTANCE, lv3);
         var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
            lv2.put("blending_data", arg2);
         });
      }

      BelowZeroRetrogen lv4 = chunk.getBelowZeroRetrogen();
      if (lv4 != null) {
         var10000 = BelowZeroRetrogen.CODEC.encodeStart(NbtOps.INSTANCE, lv4);
         var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
            lv2.put("below_zero_retrogen", arg2);
         });
      }

      UpgradeData lv5 = chunk.getUpgradeData();
      if (!lv5.isDone()) {
         lv2.put("UpgradeData", lv5.toNbt());
      }

      ChunkSection[] lvs = chunk.getSectionArray();
      NbtList lv6 = new NbtList();
      LightingProvider lv7 = world.getChunkManager().getLightingProvider();
      Registry lv8 = world.getRegistryManager().get(RegistryKeys.BIOME);
      Codec codec = createCodec(lv8);
      boolean bl = chunk.isLightOn();

      for(int i = lv7.getBottomY(); i < lv7.getTopY(); ++i) {
         int j = chunk.sectionCoordToIndex(i);
         boolean bl2 = j >= 0 && j < lvs.length;
         ChunkNibbleArray lv9 = lv7.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(lv, i));
         ChunkNibbleArray lv10 = lv7.get(LightType.SKY).getLightSection(ChunkSectionPos.from(lv, i));
         if (bl2 || lv9 != null || lv10 != null) {
            NbtCompound lv11 = new NbtCompound();
            if (bl2) {
               ChunkSection lv12 = lvs[j];
               DataResult var10002 = CODEC.encodeStart(NbtOps.INSTANCE, lv12.getBlockStateContainer());
               Logger var10004 = LOGGER;
               Objects.requireNonNull(var10004);
               lv11.put("block_states", (NbtElement)var10002.getOrThrow(false, var10004::error));
               var10002 = codec.encodeStart(NbtOps.INSTANCE, lv12.getBiomeContainer());
               var10004 = LOGGER;
               Objects.requireNonNull(var10004);
               lv11.put("biomes", (NbtElement)var10002.getOrThrow(false, var10004::error));
            }

            if (lv9 != null && !lv9.isUninitialized()) {
               lv11.putByteArray("BlockLight", lv9.asByteArray());
            }

            if (lv10 != null && !lv10.isUninitialized()) {
               lv11.putByteArray("SkyLight", lv10.asByteArray());
            }

            if (!lv11.isEmpty()) {
               lv11.putByte("Y", (byte)i);
               lv6.add(lv11);
            }
         }
      }

      lv2.put("sections", lv6);
      if (bl) {
         lv2.putBoolean("isLightOn", true);
      }

      NbtList lv13 = new NbtList();
      Iterator var23 = chunk.getBlockEntityPositions().iterator();

      NbtCompound lv15;
      while(var23.hasNext()) {
         BlockPos lv14 = (BlockPos)var23.next();
         lv15 = chunk.getPackedBlockEntityNbt(lv14);
         if (lv15 != null) {
            lv13.add(lv15);
         }
      }

      lv2.put("block_entities", lv13);
      if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
         ProtoChunk lv16 = (ProtoChunk)chunk;
         NbtList lv17 = new NbtList();
         lv17.addAll(lv16.getEntities());
         lv2.put("entities", lv17);
         lv2.put("Lights", toNbt(lv16.getLightSourcesBySection()));
         lv15 = new NbtCompound();
         GenerationStep.Carver[] var31 = GenerationStep.Carver.values();
         int var32 = var31.length;

         for(int var33 = 0; var33 < var32; ++var33) {
            GenerationStep.Carver lv18 = var31[var33];
            CarvingMask lv19 = lv16.getCarvingMask(lv18);
            if (lv19 != null) {
               lv15.putLongArray(lv18.toString(), lv19.getMask());
            }
         }

         lv2.put("CarvingMasks", lv15);
      }

      serializeTicks(world, lv2, chunk.getTickSchedulers());
      lv2.put("PostProcessing", toNbt(chunk.getPostProcessingLists()));
      NbtCompound lv20 = new NbtCompound();
      Iterator var28 = chunk.getHeightmaps().iterator();

      while(var28.hasNext()) {
         Map.Entry entry = (Map.Entry)var28.next();
         if (chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) {
            lv20.put(((Heightmap.Type)entry.getKey()).getName(), new NbtLongArray(((Heightmap)entry.getValue()).asLongArray()));
         }
      }

      lv2.put("Heightmaps", lv20);
      lv2.put("structures", writeStructures(StructureContext.from(world), lv, chunk.getStructureStarts(), chunk.getStructureReferences()));
      return lv2;
   }

   private static void serializeTicks(ServerWorld world, NbtCompound nbt, Chunk.TickSchedulers tickSchedulers) {
      long l = world.getLevelProperties().getTime();
      nbt.put("block_ticks", tickSchedulers.blocks().toNbt(l, (block) -> {
         return Registries.BLOCK.getId(block).toString();
      }));
      nbt.put("fluid_ticks", tickSchedulers.fluids().toNbt(l, (fluid) -> {
         return Registries.FLUID.getId(fluid).toString();
      }));
   }

   public static ChunkStatus.ChunkType getChunkType(@Nullable NbtCompound nbt) {
      return nbt != null ? ChunkStatus.byId(nbt.getString("Status")).getChunkType() : ChunkStatus.ChunkType.PROTOCHUNK;
   }

   @Nullable
   private static WorldChunk.EntityLoader getEntityLoadingCallback(ServerWorld world, NbtCompound nbt) {
      NbtList lv = getList(nbt, "entities");
      NbtList lv2 = getList(nbt, "block_entities");
      return lv == null && lv2 == null ? null : (chunk) -> {
         if (lv != null) {
            world.loadEntities(EntityType.streamFromNbt(lv, world));
         }

         if (lv2 != null) {
            for(int i = 0; i < lv2.size(); ++i) {
               NbtCompound lvx = lv2.getCompound(i);
               boolean bl = lvx.getBoolean("keepPacked");
               if (bl) {
                  chunk.addPendingBlockEntityNbt(lvx);
               } else {
                  BlockPos lv2x = BlockEntity.posFromNbt(lvx);
                  BlockEntity lv3 = BlockEntity.createFromNbt(lv2x, chunk.getBlockState(lv2x), lvx);
                  if (lv3 != null) {
                     chunk.setBlockEntity(lv3);
                  }
               }
            }
         }

      };
   }

   @Nullable
   private static NbtList getList(NbtCompound nbt, String key) {
      NbtList lv = nbt.getList(key, NbtElement.COMPOUND_TYPE);
      return lv.isEmpty() ? null : lv;
   }

   private static NbtCompound writeStructures(StructureContext context, ChunkPos pos, Map starts, Map references) {
      NbtCompound lv = new NbtCompound();
      NbtCompound lv2 = new NbtCompound();
      Registry lv3 = context.registryManager().get(RegistryKeys.STRUCTURE);
      Iterator var7 = starts.entrySet().iterator();

      while(var7.hasNext()) {
         Map.Entry entry = (Map.Entry)var7.next();
         Identifier lv4 = lv3.getId((Structure)entry.getKey());
         lv2.put(lv4.toString(), ((StructureStart)entry.getValue()).toNbt(context, pos));
      }

      lv.put("starts", lv2);
      NbtCompound lv5 = new NbtCompound();
      Iterator var12 = references.entrySet().iterator();

      while(var12.hasNext()) {
         Map.Entry entry2 = (Map.Entry)var12.next();
         if (!((LongSet)entry2.getValue()).isEmpty()) {
            Identifier lv6 = lv3.getId((Structure)entry2.getKey());
            lv5.put(lv6.toString(), new NbtLongArray((LongSet)entry2.getValue()));
         }
      }

      lv.put("References", lv5);
      return lv;
   }

   private static Map readStructureStarts(StructureContext context, NbtCompound nbt, long worldSeed) {
      Map map = Maps.newHashMap();
      Registry lv = context.registryManager().get(RegistryKeys.STRUCTURE);
      NbtCompound lv2 = nbt.getCompound("starts");
      Iterator var7 = lv2.getKeys().iterator();

      while(var7.hasNext()) {
         String string = (String)var7.next();
         Identifier lv3 = Identifier.tryParse(string);
         Structure lv4 = (Structure)lv.get(lv3);
         if (lv4 == null) {
            LOGGER.error("Unknown structure start: {}", lv3);
         } else {
            StructureStart lv5 = StructureStart.fromNbt(context, lv2.getCompound(string), worldSeed);
            if (lv5 != null) {
               map.put(lv4, lv5);
            }
         }
      }

      return map;
   }

   private static Map readStructureReferences(DynamicRegistryManager registryManager, ChunkPos pos, NbtCompound nbt) {
      Map map = Maps.newHashMap();
      Registry lv = registryManager.get(RegistryKeys.STRUCTURE);
      NbtCompound lv2 = nbt.getCompound("References");
      Iterator var6 = lv2.getKeys().iterator();

      while(var6.hasNext()) {
         String string = (String)var6.next();
         Identifier lv3 = Identifier.tryParse(string);
         Structure lv4 = (Structure)lv.get(lv3);
         if (lv4 == null) {
            LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", lv3, pos);
         } else {
            long[] ls = lv2.getLongArray(string);
            if (ls.length != 0) {
               map.put(lv4, new LongOpenHashSet(Arrays.stream(ls).filter((packedPos) -> {
                  ChunkPos lv = new ChunkPos(packedPos);
                  if (lv.getChebyshevDistance(pos) > 8) {
                     LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[]{lv3, lv, pos});
                     return false;
                  } else {
                     return true;
                  }
               }).toArray()));
            }
         }
      }

      return map;
   }

   public static NbtList toNbt(ShortList[] lists) {
      NbtList lv = new NbtList();
      ShortList[] var2 = lists;
      int var3 = lists.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ShortList shortList = var2[var4];
         NbtList lv2 = new NbtList();
         if (shortList != null) {
            ShortListIterator var7 = shortList.iterator();

            while(var7.hasNext()) {
               Short short_ = (Short)var7.next();
               lv2.add(NbtShort.of(short_));
            }
         }

         lv.add(lv2);
      }

      return lv;
   }

   static {
      CODEC = PalettedContainer.createPalettedContainerCodec(Block.STATE_IDS, BlockState.CODEC, PalettedContainer.PaletteProvider.BLOCK_STATE, Blocks.AIR.getDefaultState());
      LOGGER = LogUtils.getLogger();
   }
}
