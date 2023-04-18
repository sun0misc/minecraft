package net.minecraft.world.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.SimpleGameEventDispatcher;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class WorldChunk extends Chunk {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final BlockEntityTickInvoker EMPTY_BLOCK_ENTITY_TICKER = new BlockEntityTickInvoker() {
      public void tick() {
      }

      public boolean isRemoved() {
         return true;
      }

      public BlockPos getPos() {
         return BlockPos.ORIGIN;
      }

      public String getName() {
         return "<null>";
      }
   };
   private final Map blockEntityTickers;
   private boolean loadedToWorld;
   private boolean shouldRenderOnUpdate;
   final World world;
   @Nullable
   private Supplier levelTypeProvider;
   @Nullable
   private EntityLoader entityLoader;
   private final Int2ObjectMap gameEventDispatchers;
   private final ChunkTickScheduler blockTickScheduler;
   private final ChunkTickScheduler fluidTickScheduler;

   public WorldChunk(World world, ChunkPos pos) {
      this(world, pos, UpgradeData.NO_UPGRADE_DATA, new ChunkTickScheduler(), new ChunkTickScheduler(), 0L, (ChunkSection[])null, (EntityLoader)null, (BlendingData)null);
   }

   public WorldChunk(World world, ChunkPos pos, UpgradeData upgradeData, ChunkTickScheduler blockTickScheduler, ChunkTickScheduler fluidTickScheduler, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable EntityLoader entityLoader, @Nullable BlendingData blendingData) {
      super(pos, upgradeData, world, world.getRegistryManager().get(RegistryKeys.BIOME), inhabitedTime, sectionArrayInitializer, blendingData);
      this.blockEntityTickers = Maps.newHashMap();
      this.shouldRenderOnUpdate = false;
      this.world = world;
      this.gameEventDispatchers = new Int2ObjectOpenHashMap();
      Heightmap.Type[] var11 = Heightmap.Type.values();
      int var12 = var11.length;

      for(int var13 = 0; var13 < var12; ++var13) {
         Heightmap.Type lv = var11[var13];
         if (ChunkStatus.FULL.getHeightmapTypes().contains(lv)) {
            this.heightmaps.put(lv, new Heightmap(this, lv));
         }
      }

      this.entityLoader = entityLoader;
      this.blockTickScheduler = blockTickScheduler;
      this.fluidTickScheduler = fluidTickScheduler;
   }

   public WorldChunk(ServerWorld world, ProtoChunk protoChunk, @Nullable EntityLoader entityLoader) {
      this(world, protoChunk.getPos(), protoChunk.getUpgradeData(), protoChunk.getBlockProtoTickScheduler(), protoChunk.getFluidProtoTickScheduler(), protoChunk.getInhabitedTime(), protoChunk.getSectionArray(), entityLoader, protoChunk.getBlendingData());
      Iterator var4 = protoChunk.getBlockEntities().values().iterator();

      while(var4.hasNext()) {
         BlockEntity lv = (BlockEntity)var4.next();
         this.setBlockEntity(lv);
      }

      this.blockEntityNbts.putAll(protoChunk.getBlockEntityNbts());

      for(int i = 0; i < protoChunk.getPostProcessingLists().length; ++i) {
         this.postProcessingLists[i] = protoChunk.getPostProcessingLists()[i];
      }

      this.setStructureStarts(protoChunk.getStructureStarts());
      this.setStructureReferences(protoChunk.getStructureReferences());
      var4 = protoChunk.getHeightmaps().iterator();

      while(var4.hasNext()) {
         Map.Entry entry = (Map.Entry)var4.next();
         if (ChunkStatus.FULL.getHeightmapTypes().contains(entry.getKey())) {
            this.setHeightmap((Heightmap.Type)entry.getKey(), ((Heightmap)entry.getValue()).asLongArray());
         }
      }

      this.setLightOn(protoChunk.isLightOn());
      this.needsSaving = true;
   }

   public BasicTickScheduler getBlockTickScheduler() {
      return this.blockTickScheduler;
   }

   public BasicTickScheduler getFluidTickScheduler() {
      return this.fluidTickScheduler;
   }

   public Chunk.TickSchedulers getTickSchedulers() {
      return new Chunk.TickSchedulers(this.blockTickScheduler, this.fluidTickScheduler);
   }

   public GameEventDispatcher getGameEventDispatcher(int ySectionCoord) {
      World var3 = this.world;
      if (var3 instanceof ServerWorld lv) {
         return (GameEventDispatcher)this.gameEventDispatchers.computeIfAbsent(ySectionCoord, (sectionCoord) -> {
            return new SimpleGameEventDispatcher(lv);
         });
      } else {
         return super.getGameEventDispatcher(ySectionCoord);
      }
   }

   public BlockState getBlockState(BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      if (this.world.isDebugWorld()) {
         BlockState lv = null;
         if (j == 60) {
            lv = Blocks.BARRIER.getDefaultState();
         }

         if (j == 70) {
            lv = DebugChunkGenerator.getBlockState(i, k);
         }

         return lv == null ? Blocks.AIR.getDefaultState() : lv;
      } else {
         try {
            int l = this.getSectionIndex(j);
            if (l >= 0 && l < this.sectionArray.length) {
               ChunkSection lv2 = this.sectionArray[l];
               if (!lv2.isEmpty()) {
                  return lv2.getBlockState(i & 15, j & 15, k & 15);
               }
            }

            return Blocks.AIR.getDefaultState();
         } catch (Throwable var8) {
            CrashReport lv3 = CrashReport.create(var8, "Getting block state");
            CrashReportSection lv4 = lv3.addElement("Block being got");
            lv4.add("Location", () -> {
               return CrashReportSection.createPositionString(this, i, j, k);
            });
            throw new CrashException(lv3);
         }
      }
   }

   public FluidState getFluidState(BlockPos pos) {
      return this.getFluidState(pos.getX(), pos.getY(), pos.getZ());
   }

   public FluidState getFluidState(int x, int y, int z) {
      try {
         int l = this.getSectionIndex(y);
         if (l >= 0 && l < this.sectionArray.length) {
            ChunkSection lv = this.sectionArray[l];
            if (!lv.isEmpty()) {
               return lv.getFluidState(x & 15, y & 15, z & 15);
            }
         }

         return Fluids.EMPTY.getDefaultState();
      } catch (Throwable var7) {
         CrashReport lv2 = CrashReport.create(var7, "Getting fluid state");
         CrashReportSection lv3 = lv2.addElement("Block being got");
         lv3.add("Location", () -> {
            return CrashReportSection.createPositionString(this, x, y, z);
         });
         throw new CrashException(lv2);
      }
   }

   @Nullable
   public BlockState setBlockState(BlockPos pos, BlockState state, boolean moved) {
      int i = pos.getY();
      ChunkSection lv = this.getSection(this.getSectionIndex(i));
      boolean bl2 = lv.isEmpty();
      if (bl2 && state.isAir()) {
         return null;
      } else {
         int j = pos.getX() & 15;
         int k = i & 15;
         int l = pos.getZ() & 15;
         BlockState lv2 = lv.setBlockState(j, k, l, state);
         if (lv2 == state) {
            return null;
         } else {
            Block lv3 = state.getBlock();
            ((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING)).trackUpdate(j, i, l, state);
            ((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES)).trackUpdate(j, i, l, state);
            ((Heightmap)this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR)).trackUpdate(j, i, l, state);
            ((Heightmap)this.heightmaps.get(Heightmap.Type.WORLD_SURFACE)).trackUpdate(j, i, l, state);
            boolean bl3 = lv.isEmpty();
            if (bl2 != bl3) {
               this.world.getChunkManager().getLightingProvider().setSectionStatus(pos, bl3);
            }

            boolean bl4 = lv2.hasBlockEntity();
            if (!this.world.isClient) {
               lv2.onStateReplaced(this.world, pos, state, moved);
            } else if (!lv2.isOf(lv3) && bl4) {
               this.removeBlockEntity(pos);
            }

            if (!lv.getBlockState(j, k, l).isOf(lv3)) {
               return null;
            } else {
               if (!this.world.isClient) {
                  state.onBlockAdded(this.world, pos, lv2, moved);
               }

               if (state.hasBlockEntity()) {
                  BlockEntity lv4 = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
                  if (lv4 == null) {
                     lv4 = ((BlockEntityProvider)lv3).createBlockEntity(pos, state);
                     if (lv4 != null) {
                        this.addBlockEntity(lv4);
                     }
                  } else {
                     lv4.setCachedState(state);
                     this.updateTicker(lv4);
                  }
               }

               this.needsSaving = true;
               return lv2;
            }
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public void addEntity(Entity entity) {
   }

   @Nullable
   private BlockEntity createBlockEntity(BlockPos pos) {
      BlockState lv = this.getBlockState(pos);
      return !lv.hasBlockEntity() ? null : ((BlockEntityProvider)lv.getBlock()).createBlockEntity(pos, lv);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      return this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos, CreationType creationType) {
      BlockEntity lv = (BlockEntity)this.blockEntities.get(pos);
      if (lv == null) {
         NbtCompound lv2 = (NbtCompound)this.blockEntityNbts.remove(pos);
         if (lv2 != null) {
            BlockEntity lv3 = this.loadBlockEntity(pos, lv2);
            if (lv3 != null) {
               return lv3;
            }
         }
      }

      if (lv == null) {
         if (creationType == WorldChunk.CreationType.IMMEDIATE) {
            lv = this.createBlockEntity(pos);
            if (lv != null) {
               this.addBlockEntity(lv);
            }
         }
      } else if (lv.isRemoved()) {
         this.blockEntities.remove(pos);
         return null;
      }

      return lv;
   }

   public void addBlockEntity(BlockEntity blockEntity) {
      this.setBlockEntity(blockEntity);
      if (this.canTickBlockEntities()) {
         World var3 = this.world;
         if (var3 instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)var3;
            this.updateGameEventListener(blockEntity, lv);
         }

         this.updateTicker(blockEntity);
      }

   }

   private boolean canTickBlockEntities() {
      return this.loadedToWorld || this.world.isClient();
   }

   boolean canTickBlockEntity(BlockPos pos) {
      if (!this.world.getWorldBorder().contains(pos)) {
         return false;
      } else {
         World var3 = this.world;
         if (!(var3 instanceof ServerWorld)) {
            return true;
         } else {
            ServerWorld lv = (ServerWorld)var3;
            return this.getLevelType().isAfter(ChunkHolder.LevelType.TICKING) && lv.isChunkLoaded(ChunkPos.toLong(pos));
         }
      }
   }

   public void setBlockEntity(BlockEntity blockEntity) {
      BlockPos lv = blockEntity.getPos();
      if (this.getBlockState(lv).hasBlockEntity()) {
         blockEntity.setWorld(this.world);
         blockEntity.cancelRemoval();
         BlockEntity lv2 = (BlockEntity)this.blockEntities.put(lv.toImmutable(), blockEntity);
         if (lv2 != null && lv2 != blockEntity) {
            lv2.markRemoved();
         }

      }
   }

   @Nullable
   public NbtCompound getPackedBlockEntityNbt(BlockPos pos) {
      BlockEntity lv = this.getBlockEntity(pos);
      NbtCompound lv2;
      if (lv != null && !lv.isRemoved()) {
         lv2 = lv.createNbtWithIdentifyingData();
         lv2.putBoolean("keepPacked", false);
         return lv2;
      } else {
         lv2 = (NbtCompound)this.blockEntityNbts.get(pos);
         if (lv2 != null) {
            lv2 = lv2.copy();
            lv2.putBoolean("keepPacked", true);
         }

         return lv2;
      }
   }

   public void removeBlockEntity(BlockPos pos) {
      if (this.canTickBlockEntities()) {
         BlockEntity lv = (BlockEntity)this.blockEntities.remove(pos);
         if (lv != null) {
            World var4 = this.world;
            if (var4 instanceof ServerWorld) {
               ServerWorld lv2 = (ServerWorld)var4;
               this.removeGameEventListener(lv, lv2);
            }

            lv.markRemoved();
         }
      }

      this.removeBlockEntityTicker(pos);
   }

   private void removeGameEventListener(BlockEntity blockEntity, ServerWorld world) {
      Block lv = blockEntity.getCachedState().getBlock();
      if (lv instanceof BlockEntityProvider) {
         GameEventListener lv2 = ((BlockEntityProvider)lv).getGameEventListener(world, blockEntity);
         if (lv2 != null) {
            int i = ChunkSectionPos.getSectionCoord(blockEntity.getPos().getY());
            GameEventDispatcher lv3 = this.getGameEventDispatcher(i);
            lv3.removeListener(lv2);
            if (lv3.isEmpty()) {
               this.gameEventDispatchers.remove(i);
            }
         }
      }

   }

   private void removeBlockEntityTicker(BlockPos pos) {
      WrappedBlockEntityTickInvoker lv = (WrappedBlockEntityTickInvoker)this.blockEntityTickers.remove(pos);
      if (lv != null) {
         lv.setWrapped(EMPTY_BLOCK_ENTITY_TICKER);
      }

   }

   public void loadEntities() {
      if (this.entityLoader != null) {
         this.entityLoader.run(this);
         this.entityLoader = null;
      }

   }

   public boolean isEmpty() {
      return false;
   }

   public void loadFromPacket(PacketByteBuf buf, NbtCompound nbt, Consumer consumer) {
      this.clear();
      ChunkSection[] var4 = this.sectionArray;
      int var5 = var4.length;

      int var6;
      for(var6 = 0; var6 < var5; ++var6) {
         ChunkSection lv = var4[var6];
         lv.readDataPacket(buf);
      }

      Heightmap.Type[] var9 = Heightmap.Type.values();
      var5 = var9.length;

      for(var6 = 0; var6 < var5; ++var6) {
         Heightmap.Type lv2 = var9[var6];
         String string = lv2.getName();
         if (nbt.contains(string, NbtElement.LONG_ARRAY_TYPE)) {
            this.setHeightmap(lv2, nbt.getLongArray(string));
         }
      }

      consumer.accept((pos, blockEntityType, nbtx) -> {
         BlockEntity lv = this.getBlockEntity(pos, WorldChunk.CreationType.IMMEDIATE);
         if (lv != null && nbtx != null && lv.getType() == blockEntityType) {
            lv.readNbt(nbtx);
         }

      });
   }

   public void loadBiomeFromPacket(PacketByteBuf buf) {
      ChunkSection[] var2 = this.sectionArray;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ChunkSection lv = var2[var4];
         lv.readBiomePacket(buf);
      }

   }

   public void setLoadedToWorld(boolean loadedToWorld) {
      this.loadedToWorld = loadedToWorld;
   }

   public World getWorld() {
      return this.world;
   }

   public Map getBlockEntities() {
      return this.blockEntities;
   }

   public Stream getLightSourcesStream() {
      return StreamSupport.stream(BlockPos.iterate(this.pos.getStartX(), this.getBottomY(), this.pos.getStartZ(), this.pos.getEndX(), this.getTopY() - 1, this.pos.getEndZ()).spliterator(), false).filter((arg) -> {
         return this.getBlockState(arg).getLuminance() != 0;
      });
   }

   public void runPostProcessing() {
      ChunkPos lv = this.getPos();

      for(int i = 0; i < this.postProcessingLists.length; ++i) {
         if (this.postProcessingLists[i] != null) {
            ShortListIterator var3 = this.postProcessingLists[i].iterator();

            while(var3.hasNext()) {
               Short short_ = (Short)var3.next();
               BlockPos lv2 = ProtoChunk.joinBlockPos(short_, this.sectionIndexToCoord(i), lv);
               BlockState lv3 = this.getBlockState(lv2);
               FluidState lv4 = lv3.getFluidState();
               if (!lv4.isEmpty()) {
                  lv4.onScheduledTick(this.world, lv2);
               }

               if (!(lv3.getBlock() instanceof FluidBlock)) {
                  BlockState lv5 = Block.postProcessState(lv3, this.world, lv2);
                  this.world.setBlockState(lv2, lv5, Block.NO_REDRAW | Block.FORCE_STATE);
               }
            }

            this.postProcessingLists[i].clear();
         }
      }

      UnmodifiableIterator var9 = ImmutableList.copyOf(this.blockEntityNbts.keySet()).iterator();

      while(var9.hasNext()) {
         BlockPos lv6 = (BlockPos)var9.next();
         this.getBlockEntity(lv6);
      }

      this.blockEntityNbts.clear();
      this.upgradeData.upgrade(this);
   }

   @Nullable
   private BlockEntity loadBlockEntity(BlockPos pos, NbtCompound nbt) {
      BlockState lv = this.getBlockState(pos);
      BlockEntity lv2;
      if ("DUMMY".equals(nbt.getString("id"))) {
         if (lv.hasBlockEntity()) {
            lv2 = ((BlockEntityProvider)lv.getBlock()).createBlockEntity(pos, lv);
         } else {
            lv2 = null;
            LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", pos, lv);
         }
      } else {
         lv2 = BlockEntity.createFromNbt(pos, lv, nbt);
      }

      if (lv2 != null) {
         lv2.setWorld(this.world);
         this.addBlockEntity(lv2);
      } else {
         LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", lv, pos);
      }

      return lv2;
   }

   public void disableTickSchedulers(long time) {
      this.blockTickScheduler.disable(time);
      this.fluidTickScheduler.disable(time);
   }

   public void addChunkTickSchedulers(ServerWorld world) {
      world.getBlockTickScheduler().addChunkTickScheduler(this.pos, this.blockTickScheduler);
      world.getFluidTickScheduler().addChunkTickScheduler(this.pos, this.fluidTickScheduler);
   }

   public void removeChunkTickSchedulers(ServerWorld world) {
      world.getBlockTickScheduler().removeChunkTickScheduler(this.pos);
      world.getFluidTickScheduler().removeChunkTickScheduler(this.pos);
   }

   public ChunkStatus getStatus() {
      return ChunkStatus.FULL;
   }

   public ChunkHolder.LevelType getLevelType() {
      return this.levelTypeProvider == null ? ChunkHolder.LevelType.BORDER : (ChunkHolder.LevelType)this.levelTypeProvider.get();
   }

   public void setLevelTypeProvider(Supplier levelTypeProvider) {
      this.levelTypeProvider = levelTypeProvider;
   }

   public void clear() {
      this.blockEntities.values().forEach(BlockEntity::markRemoved);
      this.blockEntities.clear();
      this.blockEntityTickers.values().forEach((ticker) -> {
         ticker.setWrapped(EMPTY_BLOCK_ENTITY_TICKER);
      });
      this.blockEntityTickers.clear();
   }

   public void updateAllBlockEntities() {
      this.blockEntities.values().forEach((blockEntity) -> {
         World lv = this.world;
         if (lv instanceof ServerWorld lv2) {
            this.updateGameEventListener(blockEntity, lv2);
         }

         this.updateTicker(blockEntity);
      });
   }

   private void updateGameEventListener(BlockEntity blockEntity, ServerWorld world) {
      Block lv = blockEntity.getCachedState().getBlock();
      if (lv instanceof BlockEntityProvider) {
         GameEventListener lv2 = ((BlockEntityProvider)lv).getGameEventListener(world, blockEntity);
         if (lv2 != null) {
            this.getGameEventDispatcher(ChunkSectionPos.getSectionCoord(blockEntity.getPos().getY())).addListener(lv2);
         }
      }

   }

   private void updateTicker(BlockEntity blockEntity) {
      BlockState lv = blockEntity.getCachedState();
      BlockEntityTicker lv2 = lv.getBlockEntityTicker(this.world, blockEntity.getType());
      if (lv2 == null) {
         this.removeBlockEntityTicker(blockEntity.getPos());
      } else {
         this.blockEntityTickers.compute(blockEntity.getPos(), (pos, ticker) -> {
            BlockEntityTickInvoker lv = this.wrapTicker(blockEntity, lv2);
            if (ticker != null) {
               ticker.setWrapped(lv);
               return ticker;
            } else if (this.canTickBlockEntities()) {
               WrappedBlockEntityTickInvoker lv2x = new WrappedBlockEntityTickInvoker(lv);
               this.world.addBlockEntityTicker(lv2x);
               return lv2x;
            } else {
               return null;
            }
         });
      }

   }

   private BlockEntityTickInvoker wrapTicker(BlockEntity blockEntity, BlockEntityTicker blockEntityTicker) {
      return new DirectBlockEntityTickInvoker(blockEntity, blockEntityTicker);
   }

   public boolean shouldRenderOnUpdate() {
      return this.shouldRenderOnUpdate;
   }

   public void setShouldRenderOnUpdate(boolean shouldRenderOnUpdate) {
      this.shouldRenderOnUpdate = shouldRenderOnUpdate;
   }

   @FunctionalInterface
   public interface EntityLoader {
      void run(WorldChunk chunk);
   }

   public static enum CreationType {
      IMMEDIATE,
      QUEUED,
      CHECK;

      // $FF: synthetic method
      private static CreationType[] method_36742() {
         return new CreationType[]{IMMEDIATE, QUEUED, CHECK};
      }
   }

   class WrappedBlockEntityTickInvoker implements BlockEntityTickInvoker {
      private BlockEntityTickInvoker wrapped;

      WrappedBlockEntityTickInvoker(BlockEntityTickInvoker wrapped) {
         this.wrapped = wrapped;
      }

      void setWrapped(BlockEntityTickInvoker wrapped) {
         this.wrapped = wrapped;
      }

      public void tick() {
         this.wrapped.tick();
      }

      public boolean isRemoved() {
         return this.wrapped.isRemoved();
      }

      public BlockPos getPos() {
         return this.wrapped.getPos();
      }

      public String getName() {
         return this.wrapped.getName();
      }

      public String toString() {
         return this.wrapped.toString() + " <wrapped>";
      }
   }

   class DirectBlockEntityTickInvoker implements BlockEntityTickInvoker {
      private final BlockEntity blockEntity;
      private final BlockEntityTicker ticker;
      private boolean hasWarned;

      DirectBlockEntityTickInvoker(BlockEntity blockEntity, BlockEntityTicker ticker) {
         this.blockEntity = blockEntity;
         this.ticker = ticker;
      }

      public void tick() {
         if (!this.blockEntity.isRemoved() && this.blockEntity.hasWorld()) {
            BlockPos lv = this.blockEntity.getPos();
            if (WorldChunk.this.canTickBlockEntity(lv)) {
               try {
                  Profiler lv2 = WorldChunk.this.world.getProfiler();
                  lv2.push(this::getName);
                  BlockState lv3 = WorldChunk.this.getBlockState(lv);
                  if (this.blockEntity.getType().supports(lv3)) {
                     this.ticker.tick(WorldChunk.this.world, this.blockEntity.getPos(), lv3, this.blockEntity);
                     this.hasWarned = false;
                  } else if (!this.hasWarned) {
                     this.hasWarned = true;
                     WorldChunk.LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", new Object[]{LogUtils.defer(this::getName), LogUtils.defer(this::getPos), lv3});
                  }

                  lv2.pop();
               } catch (Throwable var5) {
                  CrashReport lv4 = CrashReport.create(var5, "Ticking block entity");
                  CrashReportSection lv5 = lv4.addElement("Block entity being ticked");
                  this.blockEntity.populateCrashReport(lv5);
                  throw new CrashException(lv4);
               }
            }
         }

      }

      public boolean isRemoved() {
         return this.blockEntity.isRemoved();
      }

      public BlockPos getPos() {
         return this.blockEntity.getPos();
      }

      public String getName() {
         return BlockEntityType.getId(this.blockEntity.getType()).toString();
      }

      public String toString() {
         String var10000 = this.getName();
         return "Level ticker for " + var10000 + "@" + this.getPos();
      }
   }
}
