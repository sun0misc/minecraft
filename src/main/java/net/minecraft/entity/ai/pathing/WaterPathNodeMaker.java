package net.minecraft.entity.ai.pathing;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class WaterPathNodeMaker extends PathNodeMaker {
   private final boolean canJumpOutOfWater;
   private final Long2ObjectMap nodePosToType = new Long2ObjectOpenHashMap();

   public WaterPathNodeMaker(boolean canJumpOutOfWater) {
      this.canJumpOutOfWater = canJumpOutOfWater;
   }

   public void init(ChunkCache cachedWorld, MobEntity entity) {
      super.init(cachedWorld, entity);
      this.nodePosToType.clear();
   }

   public void clear() {
      super.clear();
      this.nodePosToType.clear();
   }

   public PathNode getStart() {
      return this.getNode(MathHelper.floor(this.entity.getBoundingBox().minX), MathHelper.floor(this.entity.getBoundingBox().minY + 0.5), MathHelper.floor(this.entity.getBoundingBox().minZ));
   }

   public TargetPathNode getNode(double x, double y, double z) {
      return this.asTargetPathNode(this.getNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)));
   }

   public int getSuccessors(PathNode[] successors, PathNode node) {
      int i = 0;
      Map map = Maps.newEnumMap(Direction.class);
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction lv = var5[var7];
         PathNode lv2 = this.getPassableNode(node.x + lv.getOffsetX(), node.y + lv.getOffsetY(), node.z + lv.getOffsetZ());
         map.put(lv, lv2);
         if (this.hasNotVisited(lv2)) {
            successors[i++] = lv2;
         }
      }

      Iterator var10 = Direction.Type.HORIZONTAL.iterator();

      while(var10.hasNext()) {
         Direction lv3 = (Direction)var10.next();
         Direction lv4 = lv3.rotateYClockwise();
         PathNode lv5 = this.getPassableNode(node.x + lv3.getOffsetX() + lv4.getOffsetX(), node.y, node.z + lv3.getOffsetZ() + lv4.getOffsetZ());
         if (this.canPathThrough(lv5, (PathNode)map.get(lv3), (PathNode)map.get(lv4))) {
            successors[i++] = lv5;
         }
      }

      return i;
   }

   protected boolean hasNotVisited(@Nullable PathNode node) {
      return node != null && !node.visited;
   }

   protected boolean canPathThrough(@Nullable PathNode diagonalNode, @Nullable PathNode node1, @Nullable PathNode node2) {
      return this.hasNotVisited(diagonalNode) && node1 != null && node1.penalty >= 0.0F && node2 != null && node2.penalty >= 0.0F;
   }

   @Nullable
   protected PathNode getPassableNode(int x, int y, int z) {
      PathNode lv = null;
      PathNodeType lv2 = this.addPathNodePos(x, y, z);
      if (this.canJumpOutOfWater && lv2 == PathNodeType.BREACH || lv2 == PathNodeType.WATER) {
         float f = this.entity.getPathfindingPenalty(lv2);
         if (f >= 0.0F) {
            lv = this.getNode(x, y, z);
            lv.type = lv2;
            lv.penalty = Math.max(lv.penalty, f);
            if (this.cachedWorld.getFluidState(new BlockPos(x, y, z)).isEmpty()) {
               lv.penalty += 8.0F;
            }
         }
      }

      return lv;
   }

   protected PathNodeType addPathNodePos(int x, int y, int z) {
      return (PathNodeType)this.nodePosToType.computeIfAbsent(BlockPos.asLong(x, y, z), (pos) -> {
         return this.getDefaultNodeType(this.cachedWorld, x, y, z);
      });
   }

   public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
      return this.getNodeType(world, x, y, z, this.entity);
   }

   public PathNodeType getNodeType(BlockView world, int x, int y, int z, MobEntity mob) {
      BlockPos.Mutable lv = new BlockPos.Mutable();

      for(int l = x; l < x + this.entityBlockXSize; ++l) {
         for(int m = y; m < y + this.entityBlockYSize; ++m) {
            for(int n = z; n < z + this.entityBlockZSize; ++n) {
               FluidState lv2 = world.getFluidState(lv.set(l, m, n));
               BlockState lv3 = world.getBlockState(lv.set(l, m, n));
               if (lv2.isEmpty() && lv3.canPathfindThrough(world, lv.down(), NavigationType.WATER) && lv3.isAir()) {
                  return PathNodeType.BREACH;
               }

               if (!lv2.isIn(FluidTags.WATER)) {
                  return PathNodeType.BLOCKED;
               }
            }
         }
      }

      BlockState lv4 = world.getBlockState(lv);
      if (lv4.canPathfindThrough(world, lv, NavigationType.WATER)) {
         return PathNodeType.WATER;
      } else {
         return PathNodeType.BLOCKED;
      }
   }
}
