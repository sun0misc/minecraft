package net.minecraft.world;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.tick.MultiTickScheduler;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkRegion implements StructureWorldAccess {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final List chunks;
   private final Chunk centerPos;
   private final int width;
   private final ServerWorld world;
   private final long seed;
   private final WorldProperties levelProperties;
   private final Random random;
   private final DimensionType dimension;
   private final MultiTickScheduler blockTickScheduler = new MultiTickScheduler((pos) -> {
      return this.getChunk(pos).getBlockTickScheduler();
   });
   private final MultiTickScheduler fluidTickScheduler = new MultiTickScheduler((pos) -> {
      return this.getChunk(pos).getFluidTickScheduler();
   });
   private final BiomeAccess biomeAccess;
   private final ChunkPos lowerCorner;
   private final ChunkPos upperCorner;
   private final StructureAccessor structureAccessor;
   private final ChunkStatus status;
   private final int placementRadius;
   @Nullable
   private Supplier currentlyGeneratingStructureName;
   private final AtomicLong tickOrder = new AtomicLong();
   private static final Identifier WORLDGEN_REGION_RANDOM_ID = new Identifier("worldgen_region_random");

   public ChunkRegion(ServerWorld world, List chunks, ChunkStatus status, int placementRadius) {
      this.status = status;
      this.placementRadius = placementRadius;
      int j = MathHelper.floor(Math.sqrt((double)chunks.size()));
      if (j * j != chunks.size()) {
         throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("Cache size is not a square."));
      } else {
         this.chunks = chunks;
         this.centerPos = (Chunk)chunks.get(chunks.size() / 2);
         this.width = j;
         this.world = world;
         this.seed = world.getSeed();
         this.levelProperties = world.getLevelProperties();
         this.random = world.getChunkManager().getNoiseConfig().getOrCreateRandomDeriver(WORLDGEN_REGION_RANDOM_ID).split(this.centerPos.getPos().getStartPos());
         this.dimension = world.getDimension();
         this.biomeAccess = new BiomeAccess(this, BiomeAccess.hashSeed(this.seed));
         this.lowerCorner = ((Chunk)chunks.get(0)).getPos();
         this.upperCorner = ((Chunk)chunks.get(chunks.size() - 1)).getPos();
         this.structureAccessor = world.getStructureAccessor().forRegion(this);
      }
   }

   public boolean needsBlending(ChunkPos chunkPos, int checkRadius) {
      return this.world.getChunkManager().threadedAnvilChunkStorage.needsBlending(chunkPos, checkRadius);
   }

   public ChunkPos getCenterPos() {
      return this.centerPos.getPos();
   }

   public void setCurrentlyGeneratingStructureName(@Nullable Supplier structureName) {
      this.currentlyGeneratingStructureName = structureName;
   }

   public Chunk getChunk(int chunkX, int chunkZ) {
      return this.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY);
   }

   @Nullable
   public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
      Chunk lv;
      if (this.isChunkLoaded(chunkX, chunkZ)) {
         int k = chunkX - this.lowerCorner.x;
         int l = chunkZ - this.lowerCorner.z;
         lv = (Chunk)this.chunks.get(k + l * this.width);
         if (lv.getStatus().isAtLeast(leastStatus)) {
            return lv;
         }
      } else {
         lv = null;
      }

      if (!create) {
         return null;
      } else {
         LOGGER.error("Requested chunk : {} {}", chunkX, chunkZ);
         LOGGER.error("Region bounds : {} {} | {} {}", new Object[]{this.lowerCorner.x, this.lowerCorner.z, this.upperCorner.x, this.upperCorner.z});
         if (lv != null) {
            throw (RuntimeException)Util.throwOrPause(new RuntimeException(String.format(Locale.ROOT, "Chunk is not of correct status. Expecting %s, got %s | %s %s", leastStatus, lv.getStatus(), chunkX, chunkZ)));
         } else {
            throw (RuntimeException)Util.throwOrPause(new RuntimeException(String.format(Locale.ROOT, "We are asking a region for a chunk out of bound | %s %s", chunkX, chunkZ)));
         }
      }
   }

   public boolean isChunkLoaded(int chunkX, int chunkZ) {
      return chunkX >= this.lowerCorner.x && chunkX <= this.upperCorner.x && chunkZ >= this.lowerCorner.z && chunkZ <= this.upperCorner.z;
   }

   public BlockState getBlockState(BlockPos pos) {
      return this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ())).getBlockState(pos);
   }

   public FluidState getFluidState(BlockPos pos) {
      return this.getChunk(pos).getFluidState(pos);
   }

   @Nullable
   public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, Predicate targetPredicate) {
      return null;
   }

   public int getAmbientDarkness() {
      return 0;
   }

   public BiomeAccess getBiomeAccess() {
      return this.biomeAccess;
   }

   public RegistryEntry getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
      return this.world.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
   }

   public float getBrightness(Direction direction, boolean shaded) {
      return 1.0F;
   }

   public LightingProvider getLightingProvider() {
      return this.world.getLightingProvider();
   }

   public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
      BlockState lv = this.getBlockState(pos);
      if (lv.isAir()) {
         return false;
      } else {
         if (drop) {
            BlockEntity lv2 = lv.hasBlockEntity() ? this.getBlockEntity(pos) : null;
            Block.dropStacks(lv, this.world, pos, lv2, breakingEntity, ItemStack.EMPTY);
         }

         return this.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL, maxUpdateDepth);
      }
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      Chunk lv = this.getChunk(pos);
      BlockEntity lv2 = lv.getBlockEntity(pos);
      if (lv2 != null) {
         return lv2;
      } else {
         NbtCompound lv3 = lv.getBlockEntityNbt(pos);
         BlockState lv4 = lv.getBlockState(pos);
         if (lv3 != null) {
            if ("DUMMY".equals(lv3.getString("id"))) {
               if (!lv4.hasBlockEntity()) {
                  return null;
               }

               lv2 = ((BlockEntityProvider)lv4.getBlock()).createBlockEntity(pos, lv4);
            } else {
               lv2 = BlockEntity.createFromNbt(pos, lv4, lv3);
            }

            if (lv2 != null) {
               lv.setBlockEntity(lv2);
               return lv2;
            }
         }

         if (lv4.hasBlockEntity()) {
            LOGGER.warn("Tried to access a block entity before it was created. {}", pos);
         }

         return null;
      }
   }

   public boolean isValidForSetBlock(BlockPos pos) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX());
      int j = ChunkSectionPos.getSectionCoord(pos.getZ());
      ChunkPos lv = this.getCenterPos();
      int k = Math.abs(lv.x - i);
      int l = Math.abs(lv.z - j);
      if (k <= this.placementRadius && l <= this.placementRadius) {
         if (this.centerPos.hasBelowZeroRetrogen()) {
            HeightLimitView lv2 = this.centerPos.getHeightLimitView();
            if (pos.getY() < lv2.getBottomY() || pos.getY() >= lv2.getTopY()) {
               return false;
            }
         }

         return true;
      } else {
         Util.error("Detected setBlock in a far chunk [" + i + ", " + j + "], pos: " + pos + ", status: " + this.status + (this.currentlyGeneratingStructureName == null ? "" : ", currently generating: " + (String)this.currentlyGeneratingStructureName.get()));
         return false;
      }
   }

   public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
      if (!this.isValidForSetBlock(pos)) {
         return false;
      } else {
         Chunk lv = this.getChunk(pos);
         BlockState lv2 = lv.setBlockState(pos, state, false);
         if (lv2 != null) {
            this.world.onBlockChanged(pos, lv2, state);
         }

         if (state.hasBlockEntity()) {
            if (lv.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
               BlockEntity lv3 = ((BlockEntityProvider)state.getBlock()).createBlockEntity(pos, state);
               if (lv3 != null) {
                  lv.setBlockEntity(lv3);
               } else {
                  lv.removeBlockEntity(pos);
               }
            } else {
               NbtCompound lv4 = new NbtCompound();
               lv4.putInt("x", pos.getX());
               lv4.putInt("y", pos.getY());
               lv4.putInt("z", pos.getZ());
               lv4.putString("id", "DUMMY");
               lv.addPendingBlockEntityNbt(lv4);
            }
         } else if (lv2 != null && lv2.hasBlockEntity()) {
            lv.removeBlockEntity(pos);
         }

         if (state.shouldPostProcess(this, pos)) {
            this.markBlockForPostProcessing(pos);
         }

         return true;
      }
   }

   private void markBlockForPostProcessing(BlockPos pos) {
      this.getChunk(pos).markBlockForPostProcessing(pos);
   }

   public boolean spawnEntity(Entity entity) {
      int i = ChunkSectionPos.getSectionCoord(entity.getBlockX());
      int j = ChunkSectionPos.getSectionCoord(entity.getBlockZ());
      this.getChunk(i, j).addEntity(entity);
      return true;
   }

   public boolean removeBlock(BlockPos pos, boolean move) {
      return this.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
   }

   public WorldBorder getWorldBorder() {
      return this.world.getWorldBorder();
   }

   public boolean isClient() {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public ServerWorld toServerWorld() {
      return this.world;
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.world.getRegistryManager();
   }

   public FeatureSet getEnabledFeatures() {
      return this.world.getEnabledFeatures();
   }

   public WorldProperties getLevelProperties() {
      return this.levelProperties;
   }

   public LocalDifficulty getLocalDifficulty(BlockPos pos) {
      if (!this.isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()))) {
         throw new RuntimeException("We are asking a region for a chunk out of bound");
      } else {
         return new LocalDifficulty(this.world.getDifficulty(), this.world.getTimeOfDay(), 0L, this.world.getMoonSize());
      }
   }

   @Nullable
   public MinecraftServer getServer() {
      return this.world.getServer();
   }

   public ChunkManager getChunkManager() {
      return this.world.getChunkManager();
   }

   public long getSeed() {
      return this.seed;
   }

   public QueryableTickScheduler getBlockTickScheduler() {
      return this.blockTickScheduler;
   }

   public QueryableTickScheduler getFluidTickScheduler() {
      return this.fluidTickScheduler;
   }

   public int getSeaLevel() {
      return this.world.getSeaLevel();
   }

   public Random getRandom() {
      return this.random;
   }

   public int getTopY(Heightmap.Type heightmap, int x, int z) {
      return this.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z)).sampleHeightmap(heightmap, x & 15, z & 15) + 1;
   }

   public void playSound(@Nullable PlayerEntity except, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
   }

   public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
   }

   public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
   }

   public void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {
   }

   public DimensionType getDimension() {
      return this.dimension;
   }

   public boolean testBlockState(BlockPos pos, Predicate state) {
      return state.test(this.getBlockState(pos));
   }

   public boolean testFluidState(BlockPos pos, Predicate state) {
      return state.test(this.getFluidState(pos));
   }

   public List getEntitiesByType(TypeFilter filter, Box box, Predicate predicate) {
      return Collections.emptyList();
   }

   public List getOtherEntities(@Nullable Entity except, Box box, @Nullable Predicate predicate) {
      return Collections.emptyList();
   }

   public List getPlayers() {
      return Collections.emptyList();
   }

   public int getBottomY() {
      return this.world.getBottomY();
   }

   public int getHeight() {
      return this.world.getHeight();
   }

   public long getTickOrder() {
      return this.tickOrder.getAndIncrement();
   }
}
