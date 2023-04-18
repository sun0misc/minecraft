package net.minecraft.world;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public abstract class World implements WorldAccess, AutoCloseable {
   public static final Codec CODEC;
   public static final RegistryKey OVERWORLD;
   public static final RegistryKey NETHER;
   public static final RegistryKey END;
   public static final int HORIZONTAL_LIMIT = 30000000;
   public static final int MAX_UPDATE_DEPTH = 512;
   public static final int field_30967 = 32;
   public static final int field_30968 = 15;
   public static final int field_30969 = 24000;
   public static final int MAX_Y = 20000000;
   public static final int MIN_Y = -20000000;
   protected final List blockEntityTickers = Lists.newArrayList();
   protected final NeighborUpdater neighborUpdater;
   private final List pendingBlockEntityTickers = Lists.newArrayList();
   private boolean iteratingTickingBlockEntities;
   private final Thread thread;
   private final boolean debugWorld;
   private int ambientDarkness;
   protected int lcgBlockSeed = Random.create().nextInt();
   protected final int lcgBlockSeedIncrement = 1013904223;
   protected float rainGradientPrev;
   protected float rainGradient;
   protected float thunderGradientPrev;
   protected float thunderGradient;
   public final Random random = Random.create();
   /** @deprecated */
   @Deprecated
   private final Random threadSafeRandom = Random.createThreadSafe();
   private final RegistryKey dimension;
   private final RegistryEntry dimensionEntry;
   protected final MutableWorldProperties properties;
   private final Supplier profiler;
   public final boolean isClient;
   private final WorldBorder border;
   private final BiomeAccess biomeAccess;
   private final RegistryKey registryKey;
   private final DynamicRegistryManager registryManager;
   private final DamageSources damageSources;
   private long tickOrder;

   protected World(MutableWorldProperties properties, RegistryKey registryRef, DynamicRegistryManager registryManager, RegistryEntry dimensionEntry, Supplier profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
      this.profiler = profiler;
      this.properties = properties;
      this.dimensionEntry = dimensionEntry;
      this.dimension = (RegistryKey)dimensionEntry.getKey().orElseThrow(() -> {
         return new IllegalArgumentException("Dimension must be registered, got " + dimensionEntry);
      });
      final DimensionType lv = (DimensionType)dimensionEntry.value();
      this.registryKey = registryRef;
      this.isClient = isClient;
      if (lv.coordinateScale() != 1.0) {
         this.border = new WorldBorder() {
            public double getCenterX() {
               return super.getCenterX() / lv.coordinateScale();
            }

            public double getCenterZ() {
               return super.getCenterZ() / lv.coordinateScale();
            }
         };
      } else {
         this.border = new WorldBorder();
      }

      this.thread = Thread.currentThread();
      this.biomeAccess = new BiomeAccess(this, biomeAccess);
      this.debugWorld = debugWorld;
      this.neighborUpdater = new ChainRestrictedNeighborUpdater(this, maxChainedNeighborUpdates);
      this.registryManager = registryManager;
      this.damageSources = new DamageSources(registryManager);
   }

   public boolean isClient() {
      return this.isClient;
   }

   @Nullable
   public MinecraftServer getServer() {
      return null;
   }

   public boolean isInBuildLimit(BlockPos pos) {
      return !this.isOutOfHeightLimit(pos) && isValidHorizontally(pos);
   }

   public static boolean isValid(BlockPos pos) {
      return !isInvalidVertically(pos.getY()) && isValidHorizontally(pos);
   }

   private static boolean isValidHorizontally(BlockPos pos) {
      return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000;
   }

   private static boolean isInvalidVertically(int y) {
      return y < -20000000 || y >= 20000000;
   }

   public WorldChunk getWorldChunk(BlockPos pos) {
      return this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
   }

   public WorldChunk getChunk(int i, int j) {
      return (WorldChunk)this.getChunk(i, j, ChunkStatus.FULL);
   }

   @Nullable
   public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
      Chunk lv = this.getChunkManager().getChunk(chunkX, chunkZ, leastStatus, create);
      if (lv == null && create) {
         throw new IllegalStateException("Should always be able to create a chunk!");
      } else {
         return lv;
      }
   }

   public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
      return this.setBlockState(pos, state, flags, 512);
   }

   public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
      if (this.isOutOfHeightLimit(pos)) {
         return false;
      } else if (!this.isClient && this.isDebugWorld()) {
         return false;
      } else {
         WorldChunk lv = this.getWorldChunk(pos);
         Block lv2 = state.getBlock();
         BlockState lv3 = lv.setBlockState(pos, state, (flags & Block.MOVED) != 0);
         if (lv3 == null) {
            return false;
         } else {
            BlockState lv4 = this.getBlockState(pos);
            if ((flags & Block.SKIP_LIGHTING_UPDATES) == 0 && lv4 != lv3 && (lv4.getOpacity(this, pos) != lv3.getOpacity(this, pos) || lv4.getLuminance() != lv3.getLuminance() || lv4.hasSidedTransparency() || lv3.hasSidedTransparency())) {
               this.getProfiler().push("queueCheckLight");
               this.getChunkManager().getLightingProvider().checkBlock(pos);
               this.getProfiler().pop();
            }

            if (lv4 == state) {
               if (lv3 != lv4) {
                  this.scheduleBlockRerenderIfNeeded(pos, lv3, lv4);
               }

               if ((flags & Block.NOTIFY_LISTENERS) != 0 && (!this.isClient || (flags & Block.NO_REDRAW) == 0) && (this.isClient || lv.getLevelType() != null && lv.getLevelType().isAfter(ChunkHolder.LevelType.TICKING))) {
                  this.updateListeners(pos, lv3, state, flags);
               }

               if ((flags & Block.NOTIFY_NEIGHBORS) != 0) {
                  this.updateNeighbors(pos, lv3.getBlock());
                  if (!this.isClient && state.hasComparatorOutput()) {
                     this.updateComparators(pos, lv2);
                  }
               }

               if ((flags & Block.FORCE_STATE) == 0 && maxUpdateDepth > 0) {
                  int k = flags & ~(Block.NOTIFY_NEIGHBORS | Block.SKIP_DROPS);
                  lv3.prepare(this, pos, k, maxUpdateDepth - 1);
                  state.updateNeighbors(this, pos, k, maxUpdateDepth - 1);
                  state.prepare(this, pos, k, maxUpdateDepth - 1);
               }

               this.onBlockChanged(pos, lv3, lv4);
            }

            return true;
         }
      }
   }

   public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
   }

   public boolean removeBlock(BlockPos pos, boolean move) {
      FluidState lv = this.getFluidState(pos);
      return this.setBlockState(pos, lv.getBlockState(), Block.NOTIFY_ALL | (move ? Block.MOVED : 0));
   }

   public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
      BlockState lv = this.getBlockState(pos);
      if (lv.isAir()) {
         return false;
      } else {
         FluidState lv2 = this.getFluidState(pos);
         if (!(lv.getBlock() instanceof AbstractFireBlock)) {
            this.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(lv));
         }

         if (drop) {
            BlockEntity lv3 = lv.hasBlockEntity() ? this.getBlockEntity(pos) : null;
            Block.dropStacks(lv, this, pos, lv3, breakingEntity, ItemStack.EMPTY);
         }

         boolean bl2 = this.setBlockState(pos, lv2.getBlockState(), Block.NOTIFY_ALL, maxUpdateDepth);
         if (bl2) {
            this.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(breakingEntity, lv));
         }

         return bl2;
      }
   }

   public void addBlockBreakParticles(BlockPos pos, BlockState state) {
   }

   public boolean setBlockState(BlockPos pos, BlockState state) {
      return this.setBlockState(pos, state, Block.NOTIFY_ALL);
   }

   public abstract void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags);

   public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
   }

   public void updateNeighborsAlways(BlockPos pos, Block sourceBlock) {
   }

   public void updateNeighborsExcept(BlockPos pos, Block sourceBlock, Direction direction) {
   }

   public void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos sourcePos) {
   }

   public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
   }

   public void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
      this.neighborUpdater.replaceWithStateForNeighborUpdate(direction, neighborState, pos, neighborPos, flags, maxUpdateDepth);
   }

   public int getTopY(Heightmap.Type heightmap, int x, int z) {
      int k;
      if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
         if (this.isChunkLoaded(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z))) {
            k = this.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z)).sampleHeightmap(heightmap, x & 15, z & 15) + 1;
         } else {
            k = this.getBottomY();
         }
      } else {
         k = this.getSeaLevel() + 1;
      }

      return k;
   }

   public LightingProvider getLightingProvider() {
      return this.getChunkManager().getLightingProvider();
   }

   public BlockState getBlockState(BlockPos pos) {
      if (this.isOutOfHeightLimit(pos)) {
         return Blocks.VOID_AIR.getDefaultState();
      } else {
         WorldChunk lv = this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
         return lv.getBlockState(pos);
      }
   }

   public FluidState getFluidState(BlockPos pos) {
      if (this.isOutOfHeightLimit(pos)) {
         return Fluids.EMPTY.getDefaultState();
      } else {
         WorldChunk lv = this.getWorldChunk(pos);
         return lv.getFluidState(pos);
      }
   }

   public boolean isDay() {
      return !this.getDimension().hasFixedTime() && this.ambientDarkness < 4;
   }

   public boolean isNight() {
      return !this.getDimension().hasFixedTime() && !this.isDay();
   }

   public void playSound(@Nullable Entity except, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
      PlayerEntity var10001;
      if (except instanceof PlayerEntity lv) {
         var10001 = lv;
      } else {
         var10001 = null;
      }

      this.playSound(var10001, pos, sound, category, volume, pitch);
   }

   public void playSound(@Nullable PlayerEntity except, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
      this.playSound(except, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound, category, volume, pitch);
   }

   public abstract void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry sound, SoundCategory category, float volume, float pitch, long seed);

   public void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) {
      this.playSound(except, x, y, z, Registries.SOUND_EVENT.getEntry((Object)sound), category, volume, pitch, seed);
   }

   public abstract void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry sound, SoundCategory category, float volume, float pitch, long seed);

   public void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
      this.playSound(except, x, y, z, sound, category, volume, pitch, this.threadSafeRandom.nextLong());
   }

   public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
      this.playSoundFromEntity(except, entity, Registries.SOUND_EVENT.getEntry((Object)sound), category, volume, pitch, this.threadSafeRandom.nextLong());
   }

   public void playSoundAtBlockCenter(BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
      this.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound, category, volume, pitch, useDistance);
   }

   public void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
   }

   public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
   }

   public void addParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
   }

   public void addImportantParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
   }

   public void addImportantParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
   }

   public float getSkyAngleRadians(float tickDelta) {
      float g = this.getSkyAngle(tickDelta);
      return g * 6.2831855F;
   }

   public void addBlockEntityTicker(BlockEntityTickInvoker ticker) {
      (this.iteratingTickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers).add(ticker);
   }

   protected void tickBlockEntities() {
      Profiler lv = this.getProfiler();
      lv.push("blockEntities");
      this.iteratingTickingBlockEntities = true;
      if (!this.pendingBlockEntityTickers.isEmpty()) {
         this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
         this.pendingBlockEntityTickers.clear();
      }

      Iterator iterator = this.blockEntityTickers.iterator();

      while(iterator.hasNext()) {
         BlockEntityTickInvoker lv2 = (BlockEntityTickInvoker)iterator.next();
         if (lv2.isRemoved()) {
            iterator.remove();
         } else if (this.shouldTickBlockPos(lv2.getPos())) {
            lv2.tick();
         }
      }

      this.iteratingTickingBlockEntities = false;
      lv.pop();
   }

   public void tickEntity(Consumer tickConsumer, Entity entity) {
      try {
         tickConsumer.accept(entity);
      } catch (Throwable var6) {
         CrashReport lv = CrashReport.create(var6, "Ticking entity");
         CrashReportSection lv2 = lv.addElement("Entity being ticked");
         entity.populateCrashReport(lv2);
         throw new CrashException(lv);
      }
   }

   public boolean shouldUpdatePostDeath(Entity entity) {
      return true;
   }

   public boolean shouldTickBlocksInChunk(long chunkPos) {
      return true;
   }

   public boolean shouldTickBlockPos(BlockPos pos) {
      return this.shouldTickBlocksInChunk(ChunkPos.toLong(pos));
   }

   public Explosion createExplosion(@Nullable Entity entity, double x, double y, double z, float power, ExplosionSourceType explosionSourceType) {
      return this.createExplosion(entity, (DamageSource)null, (ExplosionBehavior)null, x, y, z, power, false, explosionSourceType);
   }

   public Explosion createExplosion(@Nullable Entity entity, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
      return this.createExplosion(entity, (DamageSource)null, (ExplosionBehavior)null, x, y, z, power, createFire, explosionSourceType);
   }

   public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
      return this.createExplosion(entity, damageSource, behavior, pos.getX(), pos.getY(), pos.getZ(), power, createFire, explosionSourceType);
   }

   public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
      return this.createExplosion(entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType, true);
   }

   public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType, boolean particles) {
      Explosion.DestructionType var10000;
      switch (explosionSourceType) {
         case NONE:
            var10000 = Explosion.DestructionType.KEEP;
            break;
         case BLOCK:
            var10000 = this.getDestructionType(GameRules.BLOCK_EXPLOSION_DROP_DECAY);
            break;
         case MOB:
            var10000 = this.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) ? this.getDestructionType(GameRules.MOB_EXPLOSION_DROP_DECAY) : Explosion.DestructionType.KEEP;
            break;
         case TNT:
            var10000 = this.getDestructionType(GameRules.TNT_EXPLOSION_DROP_DECAY);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      Explosion.DestructionType lv = var10000;
      Explosion lv2 = new Explosion(this, entity, damageSource, behavior, x, y, z, power, createFire, lv);
      lv2.collectBlocksAndDamageEntities();
      lv2.affectWorld(particles);
      return lv2;
   }

   private Explosion.DestructionType getDestructionType(GameRules.Key gameRuleKey) {
      return this.getGameRules().getBoolean(gameRuleKey) ? Explosion.DestructionType.DESTROY_WITH_DECAY : Explosion.DestructionType.DESTROY;
   }

   public abstract String asString();

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      if (this.isOutOfHeightLimit(pos)) {
         return null;
      } else {
         return !this.isClient && Thread.currentThread() != this.thread ? null : this.getWorldChunk(pos).getBlockEntity(pos, WorldChunk.CreationType.IMMEDIATE);
      }
   }

   public void addBlockEntity(BlockEntity blockEntity) {
      BlockPos lv = blockEntity.getPos();
      if (!this.isOutOfHeightLimit(lv)) {
         this.getWorldChunk(lv).addBlockEntity(blockEntity);
      }
   }

   public void removeBlockEntity(BlockPos pos) {
      if (!this.isOutOfHeightLimit(pos)) {
         this.getWorldChunk(pos).removeBlockEntity(pos);
      }
   }

   public boolean canSetBlock(BlockPos pos) {
      return this.isOutOfHeightLimit(pos) ? false : this.getChunkManager().isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
   }

   public boolean isDirectionSolid(BlockPos pos, Entity entity, Direction direction) {
      if (this.isOutOfHeightLimit(pos)) {
         return false;
      } else {
         Chunk lv = this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
         return lv == null ? false : lv.getBlockState(pos).isSolidSurface(this, pos, entity, direction);
      }
   }

   public boolean isTopSolid(BlockPos pos, Entity entity) {
      return this.isDirectionSolid(pos, entity, Direction.UP);
   }

   public void calculateAmbientDarkness() {
      double d = 1.0 - (double)(this.getRainGradient(1.0F) * 5.0F) / 16.0;
      double e = 1.0 - (double)(this.getThunderGradient(1.0F) * 5.0F) / 16.0;
      double f = 0.5 + 2.0 * MathHelper.clamp((double)MathHelper.cos(this.getSkyAngle(1.0F) * 6.2831855F), -0.25, 0.25);
      this.ambientDarkness = (int)((1.0 - f * d * e) * 11.0);
   }

   public void setMobSpawnOptions(boolean spawnMonsters, boolean spawnAnimals) {
      this.getChunkManager().setMobSpawnOptions(spawnMonsters, spawnAnimals);
   }

   public BlockPos getSpawnPos() {
      BlockPos lv = new BlockPos(this.properties.getSpawnX(), this.properties.getSpawnY(), this.properties.getSpawnZ());
      if (!this.getWorldBorder().contains(lv)) {
         lv = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
      }

      return lv;
   }

   public float getSpawnAngle() {
      return this.properties.getSpawnAngle();
   }

   protected void initWeatherGradients() {
      if (this.properties.isRaining()) {
         this.rainGradient = 1.0F;
         if (this.properties.isThundering()) {
            this.thunderGradient = 1.0F;
         }
      }

   }

   public void close() throws IOException {
      this.getChunkManager().close();
   }

   @Nullable
   public BlockView getChunkAsView(int chunkX, int chunkZ) {
      return this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
   }

   public List getOtherEntities(@Nullable Entity except, Box box, Predicate predicate) {
      this.getProfiler().visit("getEntities");
      List list = Lists.newArrayList();
      this.getEntityLookup().forEachIntersects(box, (entity) -> {
         if (entity != except && predicate.test(entity)) {
            list.add(entity);
         }

         if (entity instanceof EnderDragonEntity) {
            EnderDragonPart[] var4 = ((EnderDragonEntity)entity).getBodyParts();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               EnderDragonPart lv = var4[var6];
               if (entity != except && predicate.test(lv)) {
                  list.add(lv);
               }
            }
         }

      });
      return list;
   }

   public List getEntitiesByType(TypeFilter filter, Box box, Predicate predicate) {
      List list = Lists.newArrayList();
      this.collectEntitiesByType(filter, box, predicate, list);
      return list;
   }

   public void collectEntitiesByType(TypeFilter filter, Box box, Predicate predicate, List result) {
      this.collectEntitiesByType(filter, box, predicate, result, Integer.MAX_VALUE);
   }

   public void collectEntitiesByType(TypeFilter filter, Box box, Predicate predicate, List result, int limit) {
      this.getProfiler().visit("getEntities");
      this.getEntityLookup().forEachIntersects(filter, box, (entity) -> {
         if (predicate.test(entity)) {
            result.add(entity);
            if (result.size() >= limit) {
               return LazyIterationConsumer.NextIteration.ABORT;
            }
         }

         if (entity instanceof EnderDragonEntity lv) {
            EnderDragonPart[] var6 = lv.getBodyParts();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               EnderDragonPart lv2 = var6[var8];
               Entity lv3 = (Entity)filter.downcast(lv2);
               if (lv3 != null && predicate.test(lv3)) {
                  result.add(lv3);
                  if (result.size() >= limit) {
                     return LazyIterationConsumer.NextIteration.ABORT;
                  }
               }
            }
         }

         return LazyIterationConsumer.NextIteration.CONTINUE;
      });
   }

   @Nullable
   public abstract Entity getEntityById(int id);

   public void markDirty(BlockPos pos) {
      if (this.isChunkLoaded(pos)) {
         this.getWorldChunk(pos).setNeedsSaving(true);
      }

   }

   public int getSeaLevel() {
      return 63;
   }

   public void disconnect() {
   }

   public long getTime() {
      return this.properties.getTime();
   }

   public long getTimeOfDay() {
      return this.properties.getTimeOfDay();
   }

   public boolean canPlayerModifyAt(PlayerEntity player, BlockPos pos) {
      return true;
   }

   public void sendEntityStatus(Entity entity, byte status) {
   }

   public void sendEntityDamage(Entity entity, DamageSource damageSource) {
   }

   public void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {
      this.getBlockState(pos).onSyncedBlockEvent(this, pos, type, data);
   }

   public WorldProperties getLevelProperties() {
      return this.properties;
   }

   public GameRules getGameRules() {
      return this.properties.getGameRules();
   }

   public float getThunderGradient(float delta) {
      return MathHelper.lerp(delta, this.thunderGradientPrev, this.thunderGradient) * this.getRainGradient(delta);
   }

   public void setThunderGradient(float thunderGradient) {
      float g = MathHelper.clamp(thunderGradient, 0.0F, 1.0F);
      this.thunderGradientPrev = g;
      this.thunderGradient = g;
   }

   public float getRainGradient(float delta) {
      return MathHelper.lerp(delta, this.rainGradientPrev, this.rainGradient);
   }

   public void setRainGradient(float rainGradient) {
      float g = MathHelper.clamp(rainGradient, 0.0F, 1.0F);
      this.rainGradientPrev = g;
      this.rainGradient = g;
   }

   public boolean isThundering() {
      if (this.getDimension().hasSkyLight() && !this.getDimension().hasCeiling()) {
         return (double)this.getThunderGradient(1.0F) > 0.9;
      } else {
         return false;
      }
   }

   public boolean isRaining() {
      return (double)this.getRainGradient(1.0F) > 0.2;
   }

   public boolean hasRain(BlockPos pos) {
      if (!this.isRaining()) {
         return false;
      } else if (!this.isSkyVisible(pos)) {
         return false;
      } else if (this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY() > pos.getY()) {
         return false;
      } else {
         Biome lv = (Biome)this.getBiome(pos).value();
         return lv.getPrecipitation(pos) == Biome.Precipitation.RAIN;
      }
   }

   @Nullable
   public abstract MapState getMapState(String id);

   public abstract void putMapState(String id, MapState state);

   public abstract int getNextMapId();

   public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
   }

   public CrashReportSection addDetailsToCrashReport(CrashReport report) {
      CrashReportSection lv = report.addElement("Affected level", 1);
      lv.add("All players", () -> {
         int var10000 = this.getPlayers().size();
         return "" + var10000 + " total; " + this.getPlayers();
      });
      ChunkManager var10002 = this.getChunkManager();
      Objects.requireNonNull(var10002);
      lv.add("Chunk stats", var10002::getDebugString);
      lv.add("Level dimension", () -> {
         return this.getRegistryKey().getValue().toString();
      });

      try {
         this.properties.populateCrashReport(lv, this);
      } catch (Throwable var4) {
         lv.add("Level Data Unobtainable", var4);
      }

      return lv;
   }

   public abstract void setBlockBreakingInfo(int entityId, BlockPos pos, int progress);

   public void addFireworkParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, @Nullable NbtCompound nbt) {
   }

   public abstract Scoreboard getScoreboard();

   public void updateComparators(BlockPos pos, Block block) {
      Iterator var3 = Direction.Type.HORIZONTAL.iterator();

      while(var3.hasNext()) {
         Direction lv = (Direction)var3.next();
         BlockPos lv2 = pos.offset(lv);
         if (this.isChunkLoaded(lv2)) {
            BlockState lv3 = this.getBlockState(lv2);
            if (lv3.isOf(Blocks.COMPARATOR)) {
               this.updateNeighbor(lv3, lv2, block, pos, false);
            } else if (lv3.isSolidBlock(this, lv2)) {
               lv2 = lv2.offset(lv);
               lv3 = this.getBlockState(lv2);
               if (lv3.isOf(Blocks.COMPARATOR)) {
                  this.updateNeighbor(lv3, lv2, block, pos, false);
               }
            }
         }
      }

   }

   public LocalDifficulty getLocalDifficulty(BlockPos pos) {
      long l = 0L;
      float f = 0.0F;
      if (this.isChunkLoaded(pos)) {
         f = this.getMoonSize();
         l = this.getWorldChunk(pos).getInhabitedTime();
      }

      return new LocalDifficulty(this.getDifficulty(), this.getTimeOfDay(), l, f);
   }

   public int getAmbientDarkness() {
      return this.ambientDarkness;
   }

   public void setLightningTicksLeft(int lightningTicksLeft) {
   }

   public WorldBorder getWorldBorder() {
      return this.border;
   }

   public void sendPacket(Packet packet) {
      throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
   }

   public DimensionType getDimension() {
      return (DimensionType)this.dimensionEntry.value();
   }

   public RegistryKey getDimensionKey() {
      return this.dimension;
   }

   public RegistryEntry getDimensionEntry() {
      return this.dimensionEntry;
   }

   public RegistryKey getRegistryKey() {
      return this.registryKey;
   }

   public Random getRandom() {
      return this.random;
   }

   public boolean testBlockState(BlockPos pos, Predicate state) {
      return state.test(this.getBlockState(pos));
   }

   public boolean testFluidState(BlockPos pos, Predicate state) {
      return state.test(this.getFluidState(pos));
   }

   public abstract RecipeManager getRecipeManager();

   public BlockPos getRandomPosInChunk(int x, int y, int z, int l) {
      this.lcgBlockSeed = this.lcgBlockSeed * 3 + 1013904223;
      int m = this.lcgBlockSeed >> 2;
      return new BlockPos(x + (m & 15), y + (m >> 16 & l), z + (m >> 8 & 15));
   }

   public boolean isSavingDisabled() {
      return false;
   }

   public Profiler getProfiler() {
      return (Profiler)this.profiler.get();
   }

   public Supplier getProfilerSupplier() {
      return this.profiler;
   }

   public BiomeAccess getBiomeAccess() {
      return this.biomeAccess;
   }

   public final boolean isDebugWorld() {
      return this.debugWorld;
   }

   protected abstract EntityLookup getEntityLookup();

   public long getTickOrder() {
      return (long)(this.tickOrder++);
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.registryManager;
   }

   public DamageSources getDamageSources() {
      return this.damageSources;
   }

   // $FF: synthetic method
   public Chunk getChunk(int chunkX, int chunkZ) {
      return this.getChunk(chunkX, chunkZ);
   }

   static {
      CODEC = RegistryKey.createCodec(RegistryKeys.WORLD);
      OVERWORLD = RegistryKey.of(RegistryKeys.WORLD, new Identifier("overworld"));
      NETHER = RegistryKey.of(RegistryKeys.WORLD, new Identifier("the_nether"));
      END = RegistryKey.of(RegistryKeys.WORLD, new Identifier("the_end"));
   }

   public static enum ExplosionSourceType {
      NONE,
      BLOCK,
      MOB,
      TNT;

      // $FF: synthetic method
      private static ExplosionSourceType[] method_46670() {
         return new ExplosionSourceType[]{NONE, BLOCK, MOB, TNT};
      }
   }
}
