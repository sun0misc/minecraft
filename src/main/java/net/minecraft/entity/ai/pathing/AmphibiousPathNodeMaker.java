package net.minecraft.entity.ai.pathing;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class AmphibiousPathNodeMaker extends LandPathNodeMaker {
   private final boolean penalizeDeepWater;
   private float oldWalkablePenalty;
   private float oldWaterBorderPenalty;

   public AmphibiousPathNodeMaker(boolean penalizeDeepWater) {
      this.penalizeDeepWater = penalizeDeepWater;
   }

   public void init(ChunkCache cachedWorld, MobEntity entity) {
      super.init(cachedWorld, entity);
      entity.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
      this.oldWalkablePenalty = entity.getPathfindingPenalty(PathNodeType.WALKABLE);
      entity.setPathfindingPenalty(PathNodeType.WALKABLE, 6.0F);
      this.oldWaterBorderPenalty = entity.getPathfindingPenalty(PathNodeType.WATER_BORDER);
      entity.setPathfindingPenalty(PathNodeType.WATER_BORDER, 4.0F);
   }

   public void clear() {
      this.entity.setPathfindingPenalty(PathNodeType.WALKABLE, this.oldWalkablePenalty);
      this.entity.setPathfindingPenalty(PathNodeType.WATER_BORDER, this.oldWaterBorderPenalty);
      super.clear();
   }

   public PathNode getStart() {
      return !this.entity.isTouchingWater() ? super.getStart() : this.getStart(new BlockPos(MathHelper.floor(this.entity.getBoundingBox().minX), MathHelper.floor(this.entity.getBoundingBox().minY + 0.5), MathHelper.floor(this.entity.getBoundingBox().minZ)));
   }

   public TargetPathNode getNode(double x, double y, double z) {
      return this.asTargetPathNode(this.getNode(MathHelper.floor(x), MathHelper.floor(y + 0.5), MathHelper.floor(z)));
   }

   public int getSuccessors(PathNode[] successors, PathNode node) {
      int i = super.getSuccessors(successors, node);
      PathNodeType lv = this.getNodeType(this.entity, node.x, node.y + 1, node.z);
      PathNodeType lv2 = this.getNodeType(this.entity, node.x, node.y, node.z);
      int j;
      if (this.entity.getPathfindingPenalty(lv) >= 0.0F && lv2 != PathNodeType.STICKY_HONEY) {
         j = MathHelper.floor(Math.max(1.0F, this.entity.getStepHeight()));
      } else {
         j = 0;
      }

      double d = this.getFeetY(new BlockPos(node.x, node.y, node.z));
      PathNode lv3 = this.getPathNode(node.x, node.y + 1, node.z, Math.max(0, j - 1), d, Direction.UP, lv2);
      PathNode lv4 = this.getPathNode(node.x, node.y - 1, node.z, j, d, Direction.DOWN, lv2);
      if (this.isValidAquaticAdjacentSuccessor(lv3, node)) {
         successors[i++] = lv3;
      }

      if (this.isValidAquaticAdjacentSuccessor(lv4, node) && lv2 != PathNodeType.TRAPDOOR) {
         successors[i++] = lv4;
      }

      for(int k = 0; k < i; ++k) {
         PathNode lv5 = successors[k];
         if (lv5.type == PathNodeType.WATER && this.penalizeDeepWater && lv5.y < this.entity.world.getSeaLevel() - 10) {
            ++lv5.penalty;
         }
      }

      return i;
   }

   private boolean isValidAquaticAdjacentSuccessor(@Nullable PathNode node, PathNode successor) {
      return this.isValidAdjacentSuccessor(node, successor) && node.type == PathNodeType.WATER;
   }

   protected boolean isAmphibious() {
      return true;
   }

   public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      PathNodeType lv2 = getCommonNodeType(world, lv.set(x, y, z));
      if (lv2 == PathNodeType.WATER) {
         Direction[] var7 = Direction.values();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Direction lv3 = var7[var9];
            PathNodeType lv4 = getCommonNodeType(world, lv.set(x, y, z).move(lv3));
            if (lv4 == PathNodeType.BLOCKED) {
               return PathNodeType.WATER_BORDER;
            }
         }

         return PathNodeType.WATER;
      } else {
         return getLandNodeType(world, lv);
      }
   }
}
