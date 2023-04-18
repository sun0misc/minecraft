package net.minecraft.world.chunk.light;

import java.util.Arrays;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkLightProvider extends LevelPropagator implements ChunkLightingView {
   private static final Direction[] DIRECTIONS = Direction.values();
   protected final ChunkProvider chunkProvider;
   protected final LightStorage lightStorage;
   private boolean field_15794;
   protected final BlockPos.Mutable reusableBlockPos = new BlockPos.Mutable();
   private static final int field_31709 = 2;
   private final long[] cachedChunkPositions = new long[2];
   private final BlockView[] cachedChunks = new BlockView[2];

   public ChunkLightProvider(ChunkProvider chunkProvider, LightStorage lightStorage) {
      super(16, 256, 8192);
      this.chunkProvider = chunkProvider;
      this.lightStorage = lightStorage;
      this.clearChunkCache();
   }

   protected void resetLevel(long id) {
      this.lightStorage.updateAll();
      if (this.lightStorage.hasSection(ChunkSectionPos.fromBlockPos(id))) {
         super.resetLevel(id);
      }

   }

   @Nullable
   private BlockView getChunk(int chunkX, int chunkZ) {
      long l = ChunkPos.toLong(chunkX, chunkZ);

      for(int k = 0; k < 2; ++k) {
         if (l == this.cachedChunkPositions[k]) {
            return this.cachedChunks[k];
         }
      }

      BlockView lv = this.chunkProvider.getChunk(chunkX, chunkZ);

      for(int m = 1; m > 0; --m) {
         this.cachedChunkPositions[m] = this.cachedChunkPositions[m - 1];
         this.cachedChunks[m] = this.cachedChunks[m - 1];
      }

      this.cachedChunkPositions[0] = l;
      this.cachedChunks[0] = lv;
      return lv;
   }

   private void clearChunkCache() {
      Arrays.fill(this.cachedChunkPositions, ChunkPos.MARKER);
      Arrays.fill(this.cachedChunks, (Object)null);
   }

   protected BlockState getStateForLighting(BlockPos pos) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX());
      int j = ChunkSectionPos.getSectionCoord(pos.getZ());
      BlockView lv = this.getChunk(i, j);
      return lv == null ? Blocks.BEDROCK.getDefaultState() : lv.getBlockState(pos);
   }

   protected int getOpacity(BlockState state, BlockPos pos) {
      return state.getOpacity(this.chunkProvider.getWorld(), pos);
   }

   protected boolean shapesCoverFullCube(long sourceId, BlockState sourceState, long targetId, BlockState targetState, Direction direction) {
      VoxelShape lv = this.getOpaqueShape(sourceState, sourceId, direction);
      VoxelShape lv2 = this.getOpaqueShape(targetState, targetId, direction.getOpposite());
      return VoxelShapes.unionCoversFullCube(lv, lv2);
   }

   private VoxelShape getOpaqueShape(BlockState world, long pos, Direction facing) {
      return world.isOpaque() && world.hasSidedTransparency() ? world.getCullingFace(this.chunkProvider.getWorld(), this.reusableBlockPos.set(pos), facing) : VoxelShapes.empty();
   }

   public static int getRealisticOpacity(BlockView world, BlockState state1, BlockPos pos1, BlockState state2, BlockPos pos2, Direction direction, int opacity2) {
      boolean bl = state1.isOpaque() && state1.hasSidedTransparency();
      boolean bl2 = state2.isOpaque() && state2.hasSidedTransparency();
      if (!bl && !bl2) {
         return opacity2;
      } else {
         VoxelShape lv = bl ? state1.getCullingShape(world, pos1) : VoxelShapes.empty();
         VoxelShape lv2 = bl2 ? state2.getCullingShape(world, pos2) : VoxelShapes.empty();
         return VoxelShapes.adjacentSidesCoverSquare(lv, lv2, direction) ? 16 : opacity2;
      }
   }

   @Nullable
   protected static Direction getDirection(long source, long target) {
      int i = BlockPos.unpackLongX(target) - BlockPos.unpackLongX(source);
      int j = BlockPos.unpackLongY(target) - BlockPos.unpackLongY(source);
      int k = BlockPos.unpackLongZ(target) - BlockPos.unpackLongZ(source);
      return Direction.fromVector(i, j, k);
   }

   protected int recalculateLevel(long id, long excludedId, int maxLevel) {
      return 0;
   }

   protected int getLevel(long id) {
      return this.isMarker(id) ? 0 : 15 - this.lightStorage.get(id);
   }

   protected int getCurrentLevelFromSection(ChunkNibbleArray section, long blockPos) {
      return 15 - section.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
   }

   protected void setLevel(long id, int level) {
      this.lightStorage.set(id, Math.min(15, 15 - level));
   }

   protected int getPropagatedLevel(long sourceId, long targetId, int level) {
      return 0;
   }

   public boolean hasUpdates() {
      return this.hasPendingUpdates() || this.lightStorage.hasPendingUpdates() || this.lightStorage.hasLightUpdates();
   }

   public int doLightUpdates(int i, boolean doSkylight, boolean skipEdgeLightPropagation) {
      if (!this.field_15794) {
         if (this.lightStorage.hasPendingUpdates()) {
            i = this.lightStorage.applyPendingUpdates(i);
            if (i == 0) {
               return i;
            }
         }

         this.lightStorage.updateLight(this, doSkylight, skipEdgeLightPropagation);
      }

      this.field_15794 = true;
      if (this.hasPendingUpdates()) {
         i = this.applyPendingUpdates(i);
         this.clearChunkCache();
         if (i == 0) {
            return i;
         }
      }

      this.field_15794 = false;
      this.lightStorage.notifyChanges();
      return i;
   }

   protected void enqueueSectionData(long sectionPos, @Nullable ChunkNibbleArray lightArray, boolean nonEdge) {
      this.lightStorage.enqueueSectionData(sectionPos, lightArray, nonEdge);
   }

   @Nullable
   public ChunkNibbleArray getLightSection(ChunkSectionPos pos) {
      return this.lightStorage.getLightSection(pos.asLong());
   }

   public int getLightLevel(BlockPos pos) {
      return this.lightStorage.getLight(pos.asLong());
   }

   public String displaySectionLevel(long sectionPos) {
      int var10000 = this.lightStorage.getLevel(sectionPos);
      return "" + var10000;
   }

   public void checkBlock(BlockPos pos) {
      long l = pos.asLong();
      this.resetLevel(l);
      Direction[] var4 = DIRECTIONS;
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Direction lv = var4[var6];
         this.resetLevel(BlockPos.offset(l, lv));
      }

   }

   public void addLightSource(BlockPos pos, int level) {
   }

   public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
      this.lightStorage.setSectionStatus(pos.asLong(), notReady);
   }

   public void setColumnEnabled(ChunkPos pos, boolean retainData) {
      long l = ChunkSectionPos.withZeroY(ChunkSectionPos.asLong(pos.x, 0, pos.z));
      this.lightStorage.setColumnEnabled(l, retainData);
   }

   public void setRetainColumn(ChunkPos pos, boolean retainData) {
      long l = ChunkSectionPos.withZeroY(ChunkSectionPos.asLong(pos.x, 0, pos.z));
      this.lightStorage.setRetainColumn(l, retainData);
   }
}
