package net.minecraft.world.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureHolder;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.SerializableTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Chunk implements BlockView, BiomeAccess.Storage, StructureHolder {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final LongSet EMPTY_STRUCTURE_REFERENCES = new LongOpenHashSet();
   protected final ShortList[] postProcessingLists;
   protected volatile boolean needsSaving;
   private volatile boolean lightOn;
   protected final ChunkPos pos;
   private long inhabitedTime;
   /** @deprecated */
   @Nullable
   @Deprecated
   private GenerationSettings generationSettings;
   @Nullable
   protected ChunkNoiseSampler chunkNoiseSampler;
   protected final UpgradeData upgradeData;
   @Nullable
   protected BlendingData blendingData;
   protected final Map heightmaps = Maps.newEnumMap(Heightmap.Type.class);
   private final Map structureStarts = Maps.newHashMap();
   private final Map structureReferences = Maps.newHashMap();
   protected final Map blockEntityNbts = Maps.newHashMap();
   protected final Map blockEntities = Maps.newHashMap();
   protected final HeightLimitView heightLimitView;
   protected final ChunkSection[] sectionArray;

   public Chunk(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry biome, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
      this.pos = pos;
      this.upgradeData = upgradeData;
      this.heightLimitView = heightLimitView;
      this.sectionArray = new ChunkSection[heightLimitView.countVerticalSections()];
      this.inhabitedTime = inhabitedTime;
      this.postProcessingLists = new ShortList[heightLimitView.countVerticalSections()];
      this.blendingData = blendingData;
      if (sectionArrayInitializer != null) {
         if (this.sectionArray.length == sectionArrayInitializer.length) {
            System.arraycopy(sectionArrayInitializer, 0, this.sectionArray, 0, this.sectionArray.length);
         } else {
            LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", sectionArrayInitializer.length, this.sectionArray.length);
         }
      }

      fillSectionArray(heightLimitView, biome, this.sectionArray);
   }

   private static void fillSectionArray(HeightLimitView world, Registry biome, ChunkSection[] sectionArray) {
      for(int i = 0; i < sectionArray.length; ++i) {
         if (sectionArray[i] == null) {
            sectionArray[i] = new ChunkSection(world.sectionIndexToCoord(i), biome);
         }
      }

   }

   public GameEventDispatcher getGameEventDispatcher(int ySectionCoord) {
      return GameEventDispatcher.EMPTY;
   }

   @Nullable
   public abstract BlockState setBlockState(BlockPos pos, BlockState state, boolean moved);

   public abstract void setBlockEntity(BlockEntity blockEntity);

   public abstract void addEntity(Entity entity);

   @Nullable
   public ChunkSection getHighestNonEmptySection() {
      ChunkSection[] lvs = this.getSectionArray();

      for(int i = lvs.length - 1; i >= 0; --i) {
         ChunkSection lv = lvs[i];
         if (!lv.isEmpty()) {
            return lv;
         }
      }

      return null;
   }

   public int getHighestNonEmptySectionYOffset() {
      ChunkSection lv = this.getHighestNonEmptySection();
      return lv == null ? this.getBottomY() : lv.getYOffset();
   }

   public Set getBlockEntityPositions() {
      Set set = Sets.newHashSet(this.blockEntityNbts.keySet());
      set.addAll(this.blockEntities.keySet());
      return set;
   }

   public ChunkSection[] getSectionArray() {
      return this.sectionArray;
   }

   public ChunkSection getSection(int yIndex) {
      return this.getSectionArray()[yIndex];
   }

   public Collection getHeightmaps() {
      return Collections.unmodifiableSet(this.heightmaps.entrySet());
   }

   public void setHeightmap(Heightmap.Type type, long[] heightmap) {
      this.getHeightmap(type).setTo(this, type, heightmap);
   }

   public Heightmap getHeightmap(Heightmap.Type type) {
      return (Heightmap)this.heightmaps.computeIfAbsent(type, (type2) -> {
         return new Heightmap(this, type2);
      });
   }

   public boolean hasHeightmap(Heightmap.Type type) {
      return this.heightmaps.get(type) != null;
   }

   public int sampleHeightmap(Heightmap.Type type, int x, int z) {
      Heightmap lv = (Heightmap)this.heightmaps.get(type);
      if (lv == null) {
         if (SharedConstants.isDevelopment && this instanceof WorldChunk) {
            LOGGER.error("Unprimed heightmap: " + type + " " + x + " " + z);
         }

         Heightmap.populateHeightmaps(this, EnumSet.of(type));
         lv = (Heightmap)this.heightmaps.get(type);
      }

      return lv.get(x & 15, z & 15) - 1;
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   @Nullable
   public StructureStart getStructureStart(Structure structure) {
      return (StructureStart)this.structureStarts.get(structure);
   }

   public void setStructureStart(Structure structure, StructureStart start) {
      this.structureStarts.put(structure, start);
      this.needsSaving = true;
   }

   public Map getStructureStarts() {
      return Collections.unmodifiableMap(this.structureStarts);
   }

   public void setStructureStarts(Map structureStarts) {
      this.structureStarts.clear();
      this.structureStarts.putAll(structureStarts);
      this.needsSaving = true;
   }

   public LongSet getStructureReferences(Structure structure) {
      return (LongSet)this.structureReferences.getOrDefault(structure, EMPTY_STRUCTURE_REFERENCES);
   }

   public void addStructureReference(Structure structure, long reference) {
      ((LongSet)this.structureReferences.computeIfAbsent(structure, (type2) -> {
         return new LongOpenHashSet();
      })).add(reference);
      this.needsSaving = true;
   }

   public Map getStructureReferences() {
      return Collections.unmodifiableMap(this.structureReferences);
   }

   public void setStructureReferences(Map structureReferences) {
      this.structureReferences.clear();
      this.structureReferences.putAll(structureReferences);
      this.needsSaving = true;
   }

   public boolean areSectionsEmptyBetween(int lowerHeight, int upperHeight) {
      if (lowerHeight < this.getBottomY()) {
         lowerHeight = this.getBottomY();
      }

      if (upperHeight >= this.getTopY()) {
         upperHeight = this.getTopY() - 1;
      }

      for(int k = lowerHeight; k <= upperHeight; k += 16) {
         if (!this.getSection(this.getSectionIndex(k)).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public void setNeedsSaving(boolean needsSaving) {
      this.needsSaving = needsSaving;
   }

   public boolean needsSaving() {
      return this.needsSaving;
   }

   public abstract ChunkStatus getStatus();

   public abstract void removeBlockEntity(BlockPos pos);

   public void markBlockForPostProcessing(BlockPos pos) {
      LOGGER.warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", pos);
   }

   public ShortList[] getPostProcessingLists() {
      return this.postProcessingLists;
   }

   public void markBlockForPostProcessing(short packedPos, int index) {
      getList(this.getPostProcessingLists(), index).add(packedPos);
   }

   public void addPendingBlockEntityNbt(NbtCompound nbt) {
      this.blockEntityNbts.put(BlockEntity.posFromNbt(nbt), nbt);
   }

   @Nullable
   public NbtCompound getBlockEntityNbt(BlockPos pos) {
      return (NbtCompound)this.blockEntityNbts.get(pos);
   }

   @Nullable
   public abstract NbtCompound getPackedBlockEntityNbt(BlockPos pos);

   public abstract Stream getLightSourcesStream();

   public abstract BasicTickScheduler getBlockTickScheduler();

   public abstract BasicTickScheduler getFluidTickScheduler();

   public abstract TickSchedulers getTickSchedulers();

   public UpgradeData getUpgradeData() {
      return this.upgradeData;
   }

   public boolean usesOldNoise() {
      return this.blendingData != null;
   }

   @Nullable
   public BlendingData getBlendingData() {
      return this.blendingData;
   }

   public void setBlendingData(BlendingData blendingData) {
      this.blendingData = blendingData;
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void increaseInhabitedTime(long delta) {
      this.inhabitedTime += delta;
   }

   public void setInhabitedTime(long inhabitedTime) {
      this.inhabitedTime = inhabitedTime;
   }

   public static ShortList getList(ShortList[] lists, int index) {
      if (lists[index] == null) {
         lists[index] = new ShortArrayList();
      }

      return lists[index];
   }

   public boolean isLightOn() {
      return this.lightOn;
   }

   public void setLightOn(boolean lightOn) {
      this.lightOn = lightOn;
      this.setNeedsSaving(true);
   }

   public int getBottomY() {
      return this.heightLimitView.getBottomY();
   }

   public int getHeight() {
      return this.heightLimitView.getHeight();
   }

   public ChunkNoiseSampler getOrCreateChunkNoiseSampler(Function chunkNoiseSamplerCreator) {
      if (this.chunkNoiseSampler == null) {
         this.chunkNoiseSampler = (ChunkNoiseSampler)chunkNoiseSamplerCreator.apply(this);
      }

      return this.chunkNoiseSampler;
   }

   /** @deprecated */
   @Deprecated
   public GenerationSettings getOrCreateGenerationSettings(Supplier generationSettingsCreator) {
      if (this.generationSettings == null) {
         this.generationSettings = (GenerationSettings)generationSettingsCreator.get();
      }

      return this.generationSettings;
   }

   public RegistryEntry getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
      try {
         int l = BiomeCoords.fromBlock(this.getBottomY());
         int m = l + BiomeCoords.fromBlock(this.getHeight()) - 1;
         int n = MathHelper.clamp(biomeY, l, m);
         int o = this.getSectionIndex(BiomeCoords.toBlock(n));
         return this.sectionArray[o].getBiome(biomeX & 3, n & 3, biomeZ & 3);
      } catch (Throwable var8) {
         CrashReport lv = CrashReport.create(var8, "Getting biome");
         CrashReportSection lv2 = lv.addElement("Biome being got");
         lv2.add("Location", () -> {
            return CrashReportSection.createPositionString(this, biomeX, biomeY, biomeZ);
         });
         throw new CrashException(lv);
      }
   }

   public void populateBiomes(BiomeSupplier biomeSupplier, MultiNoiseUtil.MultiNoiseSampler sampler) {
      ChunkPos lv = this.getPos();
      int i = BiomeCoords.fromBlock(lv.getStartX());
      int j = BiomeCoords.fromBlock(lv.getStartZ());
      HeightLimitView lv2 = this.getHeightLimitView();

      for(int k = lv2.getBottomSectionCoord(); k < lv2.getTopSectionCoord(); ++k) {
         ChunkSection lv3 = this.getSection(this.sectionCoordToIndex(k));
         lv3.populateBiomes(biomeSupplier, sampler, i, j);
      }

   }

   public boolean hasStructureReferences() {
      return !this.getStructureReferences().isEmpty();
   }

   @Nullable
   public BelowZeroRetrogen getBelowZeroRetrogen() {
      return null;
   }

   public boolean hasBelowZeroRetrogen() {
      return this.getBelowZeroRetrogen() != null;
   }

   public HeightLimitView getHeightLimitView() {
      return this;
   }

   public static record TickSchedulers(SerializableTickScheduler blocks, SerializableTickScheduler fluids) {
      public TickSchedulers(SerializableTickScheduler arg, SerializableTickScheduler arg2) {
         this.blocks = arg;
         this.fluids = arg2;
      }

      public SerializableTickScheduler blocks() {
         return this.blocks;
      }

      public SerializableTickScheduler fluids() {
         return this.fluids;
      }
   }
}
