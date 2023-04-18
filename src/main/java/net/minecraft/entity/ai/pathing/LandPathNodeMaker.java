package net.minecraft.entity.ai.pathing;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import java.util.Iterator;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class LandPathNodeMaker extends PathNodeMaker {
   public static final double Y_OFFSET = 0.5;
   private static final double MIN_STEP_HEIGHT = 1.125;
   protected float waterPathNodeTypeWeight;
   private final Long2ObjectMap nodeTypes = new Long2ObjectOpenHashMap();
   private final Object2BooleanMap collidedBoxes = new Object2BooleanOpenHashMap();

   public void init(ChunkCache cachedWorld, MobEntity entity) {
      super.init(cachedWorld, entity);
      this.waterPathNodeTypeWeight = entity.getPathfindingPenalty(PathNodeType.WATER);
   }

   public void clear() {
      this.entity.setPathfindingPenalty(PathNodeType.WATER, this.waterPathNodeTypeWeight);
      this.nodeTypes.clear();
      this.collidedBoxes.clear();
      super.clear();
   }

   public PathNode getStart() {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      int i = this.entity.getBlockY();
      BlockState lv2 = this.cachedWorld.getBlockState(lv.set(this.entity.getX(), (double)i, this.entity.getZ()));
      BlockPos lv3;
      if (!this.entity.canWalkOnFluid(lv2.getFluidState())) {
         if (this.canSwim() && this.entity.isTouchingWater()) {
            while(true) {
               if (!lv2.isOf(Blocks.WATER) && lv2.getFluidState() != Fluids.WATER.getStill(false)) {
                  --i;
                  break;
               }

               ++i;
               lv2 = this.cachedWorld.getBlockState(lv.set(this.entity.getX(), (double)i, this.entity.getZ()));
            }
         } else if (this.entity.isOnGround()) {
            i = MathHelper.floor(this.entity.getY() + 0.5);
         } else {
            for(lv3 = this.entity.getBlockPos(); (this.cachedWorld.getBlockState(lv3).isAir() || this.cachedWorld.getBlockState(lv3).canPathfindThrough(this.cachedWorld, lv3, NavigationType.LAND)) && lv3.getY() > this.entity.world.getBottomY(); lv3 = lv3.down()) {
            }

            i = lv3.up().getY();
         }
      } else {
         while(true) {
            if (!this.entity.canWalkOnFluid(lv2.getFluidState())) {
               --i;
               break;
            }

            ++i;
            lv2 = this.cachedWorld.getBlockState(lv.set(this.entity.getX(), (double)i, this.entity.getZ()));
         }
      }

      lv3 = this.entity.getBlockPos();
      if (!this.canPathThrough(lv.set(lv3.getX(), i, lv3.getZ()))) {
         Box lv4 = this.entity.getBoundingBox();
         if (this.canPathThrough(lv.set(lv4.minX, (double)i, lv4.minZ)) || this.canPathThrough(lv.set(lv4.minX, (double)i, lv4.maxZ)) || this.canPathThrough(lv.set(lv4.maxX, (double)i, lv4.minZ)) || this.canPathThrough(lv.set(lv4.maxX, (double)i, lv4.maxZ))) {
            return this.getStart(lv);
         }
      }

      return this.getStart(new BlockPos(lv3.getX(), i, lv3.getZ()));
   }

   protected PathNode getStart(BlockPos pos) {
      PathNode lv = this.getNode(pos);
      lv.type = this.getNodeType(this.entity, lv.getBlockPos());
      lv.penalty = this.entity.getPathfindingPenalty(lv.type);
      return lv;
   }

   protected boolean canPathThrough(BlockPos pos) {
      PathNodeType lv = this.getNodeType(this.entity, pos);
      return lv != PathNodeType.OPEN && this.entity.getPathfindingPenalty(lv) >= 0.0F;
   }

   public TargetPathNode getNode(double x, double y, double z) {
      return this.asTargetPathNode(this.getNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)));
   }

   public int getSuccessors(PathNode[] successors, PathNode node) {
      int i = 0;
      int j = 0;
      PathNodeType lv = this.getNodeType(this.entity, node.x, node.y + 1, node.z);
      PathNodeType lv2 = this.getNodeType(this.entity, node.x, node.y, node.z);
      if (this.entity.getPathfindingPenalty(lv) >= 0.0F && lv2 != PathNodeType.STICKY_HONEY) {
         j = MathHelper.floor(Math.max(1.0F, this.entity.getStepHeight()));
      }

      double d = this.getFeetY(new BlockPos(node.x, node.y, node.z));
      PathNode lv3 = this.getPathNode(node.x, node.y, node.z + 1, j, d, Direction.SOUTH, lv2);
      if (this.isValidAdjacentSuccessor(lv3, node)) {
         successors[i++] = lv3;
      }

      PathNode lv4 = this.getPathNode(node.x - 1, node.y, node.z, j, d, Direction.WEST, lv2);
      if (this.isValidAdjacentSuccessor(lv4, node)) {
         successors[i++] = lv4;
      }

      PathNode lv5 = this.getPathNode(node.x + 1, node.y, node.z, j, d, Direction.EAST, lv2);
      if (this.isValidAdjacentSuccessor(lv5, node)) {
         successors[i++] = lv5;
      }

      PathNode lv6 = this.getPathNode(node.x, node.y, node.z - 1, j, d, Direction.NORTH, lv2);
      if (this.isValidAdjacentSuccessor(lv6, node)) {
         successors[i++] = lv6;
      }

      PathNode lv7 = this.getPathNode(node.x - 1, node.y, node.z - 1, j, d, Direction.NORTH, lv2);
      if (this.isValidDiagonalSuccessor(node, lv4, lv6, lv7)) {
         successors[i++] = lv7;
      }

      PathNode lv8 = this.getPathNode(node.x + 1, node.y, node.z - 1, j, d, Direction.NORTH, lv2);
      if (this.isValidDiagonalSuccessor(node, lv5, lv6, lv8)) {
         successors[i++] = lv8;
      }

      PathNode lv9 = this.getPathNode(node.x - 1, node.y, node.z + 1, j, d, Direction.SOUTH, lv2);
      if (this.isValidDiagonalSuccessor(node, lv4, lv3, lv9)) {
         successors[i++] = lv9;
      }

      PathNode lv10 = this.getPathNode(node.x + 1, node.y, node.z + 1, j, d, Direction.SOUTH, lv2);
      if (this.isValidDiagonalSuccessor(node, lv5, lv3, lv10)) {
         successors[i++] = lv10;
      }

      return i;
   }

   protected boolean isValidAdjacentSuccessor(@Nullable PathNode node, PathNode successor1) {
      return node != null && !node.visited && (node.penalty >= 0.0F || successor1.penalty < 0.0F);
   }

   protected boolean isValidDiagonalSuccessor(PathNode xNode, @Nullable PathNode zNode, @Nullable PathNode xDiagNode, @Nullable PathNode zDiagNode) {
      if (zDiagNode != null && xDiagNode != null && zNode != null) {
         if (zDiagNode.visited) {
            return false;
         } else if (xDiagNode.y <= xNode.y && zNode.y <= xNode.y) {
            if (zNode.type != PathNodeType.WALKABLE_DOOR && xDiagNode.type != PathNodeType.WALKABLE_DOOR && zDiagNode.type != PathNodeType.WALKABLE_DOOR) {
               boolean bl = xDiagNode.type == PathNodeType.FENCE && zNode.type == PathNodeType.FENCE && (double)this.entity.getWidth() < 0.5;
               return zDiagNode.penalty >= 0.0F && (xDiagNode.y < xNode.y || xDiagNode.penalty >= 0.0F || bl) && (zNode.y < xNode.y || zNode.penalty >= 0.0F || bl);
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean isBlocked(PathNodeType nodeType) {
      return nodeType == PathNodeType.FENCE || nodeType == PathNodeType.DOOR_WOOD_CLOSED || nodeType == PathNodeType.DOOR_IRON_CLOSED;
   }

   private boolean isBlocked(PathNode node) {
      Box lv = this.entity.getBoundingBox();
      Vec3d lv2 = new Vec3d((double)node.x - this.entity.getX() + lv.getXLength() / 2.0, (double)node.y - this.entity.getY() + lv.getYLength() / 2.0, (double)node.z - this.entity.getZ() + lv.getZLength() / 2.0);
      int i = MathHelper.ceil(lv2.length() / lv.getAverageSideLength());
      lv2 = lv2.multiply((double)(1.0F / (float)i));

      for(int j = 1; j <= i; ++j) {
         lv = lv.offset(lv2);
         if (this.checkBoxCollision(lv)) {
            return false;
         }
      }

      return true;
   }

   protected double getFeetY(BlockPos pos) {
      return (this.canSwim() || this.isAmphibious()) && this.cachedWorld.getFluidState(pos).isIn(FluidTags.WATER) ? (double)pos.getY() + 0.5 : getFeetY(this.cachedWorld, pos);
   }

   public static double getFeetY(BlockView world, BlockPos pos) {
      BlockPos lv = pos.down();
      VoxelShape lv2 = world.getBlockState(lv).getCollisionShape(world, lv);
      return (double)lv.getY() + (lv2.isEmpty() ? 0.0 : lv2.getMax(Direction.Axis.Y));
   }

   protected boolean isAmphibious() {
      return false;
   }

   @Nullable
   protected PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
      PathNode lv = null;
      BlockPos.Mutable lv2 = new BlockPos.Mutable();
      double e = this.getFeetY(lv2.set(x, y, z));
      if (e - prevFeetY > this.getStepHeight()) {
         return null;
      } else {
         PathNodeType lv3 = this.getNodeType(this.entity, x, y, z);
         float f = this.entity.getPathfindingPenalty(lv3);
         double g = (double)this.entity.getWidth() / 2.0;
         if (f >= 0.0F) {
            lv = this.getNodeWith(x, y, z, lv3, f);
         }

         if (isBlocked(nodeType) && lv != null && lv.penalty >= 0.0F && !this.isBlocked(lv)) {
            lv = null;
         }

         if (lv3 == PathNodeType.WALKABLE || this.isAmphibious() && lv3 == PathNodeType.WATER) {
            return lv;
         } else {
            if ((lv == null || lv.penalty < 0.0F) && maxYStep > 0 && (lv3 != PathNodeType.FENCE || this.canWalkOverFences()) && lv3 != PathNodeType.UNPASSABLE_RAIL && lv3 != PathNodeType.TRAPDOOR && lv3 != PathNodeType.POWDER_SNOW) {
               lv = this.getPathNode(x, y + 1, z, maxYStep - 1, prevFeetY, direction, nodeType);
               if (lv != null && (lv.type == PathNodeType.OPEN || lv.type == PathNodeType.WALKABLE) && this.entity.getWidth() < 1.0F) {
                  double h = (double)(x - direction.getOffsetX()) + 0.5;
                  double m = (double)(z - direction.getOffsetZ()) + 0.5;
                  Box lv4 = new Box(h - g, this.getFeetY(lv2.set(h, (double)(y + 1), m)) + 0.001, m - g, h + g, (double)this.entity.getHeight() + this.getFeetY(lv2.set((double)lv.x, (double)lv.y, (double)lv.z)) - 0.002, m + g);
                  if (this.checkBoxCollision(lv4)) {
                     lv = null;
                  }
               }
            }

            if (!this.isAmphibious() && lv3 == PathNodeType.WATER && !this.canSwim()) {
               if (this.getNodeType(this.entity, x, y - 1, z) != PathNodeType.WATER) {
                  return lv;
               }

               while(y > this.entity.world.getBottomY()) {
                  --y;
                  lv3 = this.getNodeType(this.entity, x, y, z);
                  if (lv3 != PathNodeType.WATER) {
                     return lv;
                  }

                  lv = this.getNodeWith(x, y, z, lv3, this.entity.getPathfindingPenalty(lv3));
               }
            }

            if (lv3 == PathNodeType.OPEN) {
               int n = 0;
               int o = y;

               while(lv3 == PathNodeType.OPEN) {
                  --y;
                  if (y < this.entity.world.getBottomY()) {
                     return this.getBlockedNode(x, o, z);
                  }

                  if (n++ >= this.entity.getSafeFallDistance()) {
                     return this.getBlockedNode(x, y, z);
                  }

                  lv3 = this.getNodeType(this.entity, x, y, z);
                  f = this.entity.getPathfindingPenalty(lv3);
                  if (lv3 != PathNodeType.OPEN && f >= 0.0F) {
                     lv = this.getNodeWith(x, y, z, lv3, f);
                     break;
                  }

                  if (f < 0.0F) {
                     return this.getBlockedNode(x, y, z);
                  }
               }
            }

            if (isBlocked(lv3) && lv == null) {
               lv = this.getNode(x, y, z);
               lv.visited = true;
               lv.type = lv3;
               lv.penalty = lv3.getDefaultPenalty();
            }

            return lv;
         }
      }
   }

   private double getStepHeight() {
      return Math.max(1.125, (double)this.entity.getStepHeight());
   }

   private PathNode getNodeWith(int x, int y, int z, PathNodeType type, float penalty) {
      PathNode lv = this.getNode(x, y, z);
      lv.type = type;
      lv.penalty = Math.max(lv.penalty, penalty);
      return lv;
   }

   private PathNode getBlockedNode(int x, int y, int z) {
      PathNode lv = this.getNode(x, y, z);
      lv.type = PathNodeType.BLOCKED;
      lv.penalty = -1.0F;
      return lv;
   }

   private boolean checkBoxCollision(Box box) {
      return this.collidedBoxes.computeIfAbsent(box, (box2) -> {
         return !this.cachedWorld.isSpaceEmpty(this.entity, box);
      });
   }

   public PathNodeType getNodeType(BlockView world, int x, int y, int z, MobEntity mob) {
      EnumSet enumSet = EnumSet.noneOf(PathNodeType.class);
      PathNodeType lv = PathNodeType.BLOCKED;
      lv = this.findNearbyNodeTypes(world, x, y, z, enumSet, lv, mob.getBlockPos());
      if (enumSet.contains(PathNodeType.FENCE)) {
         return PathNodeType.FENCE;
      } else if (enumSet.contains(PathNodeType.UNPASSABLE_RAIL)) {
         return PathNodeType.UNPASSABLE_RAIL;
      } else {
         PathNodeType lv2 = PathNodeType.BLOCKED;
         Iterator var9 = enumSet.iterator();

         while(var9.hasNext()) {
            PathNodeType lv3 = (PathNodeType)var9.next();
            if (mob.getPathfindingPenalty(lv3) < 0.0F) {
               return lv3;
            }

            if (mob.getPathfindingPenalty(lv3) >= mob.getPathfindingPenalty(lv2)) {
               lv2 = lv3;
            }
         }

         if (lv == PathNodeType.OPEN && mob.getPathfindingPenalty(lv2) == 0.0F && this.entityBlockXSize <= 1) {
            return PathNodeType.OPEN;
         } else {
            return lv2;
         }
      }
   }

   public PathNodeType findNearbyNodeTypes(BlockView world, int x, int y, int z, EnumSet nearbyTypes, PathNodeType type, BlockPos pos) {
      for(int l = 0; l < this.entityBlockXSize; ++l) {
         for(int m = 0; m < this.entityBlockYSize; ++m) {
            for(int n = 0; n < this.entityBlockZSize; ++n) {
               int o = l + x;
               int p = m + y;
               int q = n + z;
               PathNodeType lv = this.getDefaultNodeType(world, o, p, q);
               lv = this.adjustNodeType(world, pos, lv);
               if (l == 0 && m == 0 && n == 0) {
                  type = lv;
               }

               nearbyTypes.add(lv);
            }
         }
      }

      return type;
   }

   protected PathNodeType adjustNodeType(BlockView world, BlockPos pos, PathNodeType type) {
      boolean bl = this.canEnterOpenDoors();
      if (type == PathNodeType.DOOR_WOOD_CLOSED && this.canOpenDoors() && bl) {
         type = PathNodeType.WALKABLE_DOOR;
      }

      if (type == PathNodeType.DOOR_OPEN && !bl) {
         type = PathNodeType.BLOCKED;
      }

      if (type == PathNodeType.RAIL && !(world.getBlockState(pos).getBlock() instanceof AbstractRailBlock) && !(world.getBlockState(pos.down()).getBlock() instanceof AbstractRailBlock)) {
         type = PathNodeType.UNPASSABLE_RAIL;
      }

      return type;
   }

   protected PathNodeType getNodeType(MobEntity entity, BlockPos pos) {
      return this.getNodeType(entity, pos.getX(), pos.getY(), pos.getZ());
   }

   protected PathNodeType getNodeType(MobEntity entity, int x, int y, int z) {
      return (PathNodeType)this.nodeTypes.computeIfAbsent(BlockPos.asLong(x, y, z), (l) -> {
         return this.getNodeType(this.cachedWorld, x, y, z, entity);
      });
   }

   public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
      return getLandNodeType(world, new BlockPos.Mutable(x, y, z));
   }

   public static PathNodeType getLandNodeType(BlockView world, BlockPos.Mutable pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      PathNodeType lv = getCommonNodeType(world, pos);
      if (lv == PathNodeType.OPEN && j >= world.getBottomY() + 1) {
         PathNodeType lv2 = getCommonNodeType(world, pos.set(i, j - 1, k));
         lv = lv2 != PathNodeType.WALKABLE && lv2 != PathNodeType.OPEN && lv2 != PathNodeType.WATER && lv2 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
         if (lv2 == PathNodeType.DAMAGE_FIRE) {
            lv = PathNodeType.DAMAGE_FIRE;
         }

         if (lv2 == PathNodeType.DAMAGE_OTHER) {
            lv = PathNodeType.DAMAGE_OTHER;
         }

         if (lv2 == PathNodeType.STICKY_HONEY) {
            lv = PathNodeType.STICKY_HONEY;
         }

         if (lv2 == PathNodeType.POWDER_SNOW) {
            lv = PathNodeType.DANGER_POWDER_SNOW;
         }

         if (lv2 == PathNodeType.DAMAGE_CAUTIOUS) {
            lv = PathNodeType.DAMAGE_CAUTIOUS;
         }
      }

      if (lv == PathNodeType.WALKABLE) {
         lv = getNodeTypeFromNeighbors(world, pos.set(i, j, k), lv);
      }

      return lv;
   }

   public static PathNodeType getNodeTypeFromNeighbors(BlockView world, BlockPos.Mutable pos, PathNodeType nodeType) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();

      for(int l = -1; l <= 1; ++l) {
         for(int m = -1; m <= 1; ++m) {
            for(int n = -1; n <= 1; ++n) {
               if (l != 0 || n != 0) {
                  pos.set(i + l, j + m, k + n);
                  BlockState lv = world.getBlockState(pos);
                  if (lv.isOf(Blocks.CACTUS) || lv.isOf(Blocks.SWEET_BERRY_BUSH)) {
                     return PathNodeType.DANGER_OTHER;
                  }

                  if (inflictsFireDamage(lv)) {
                     return PathNodeType.DANGER_FIRE;
                  }

                  if (world.getFluidState(pos).isIn(FluidTags.WATER)) {
                     return PathNodeType.WATER_BORDER;
                  }

                  if (lv.isOf(Blocks.WITHER_ROSE) || lv.isOf(Blocks.POINTED_DRIPSTONE)) {
                     return PathNodeType.DAMAGE_CAUTIOUS;
                  }
               }
            }
         }
      }

      return nodeType;
   }

   protected static PathNodeType getCommonNodeType(BlockView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      Block lv2 = lv.getBlock();
      if (lv.isAir()) {
         return PathNodeType.OPEN;
      } else if (!lv.isIn(BlockTags.TRAPDOORS) && !lv.isOf(Blocks.LILY_PAD) && !lv.isOf(Blocks.BIG_DRIPLEAF)) {
         if (lv.isOf(Blocks.POWDER_SNOW)) {
            return PathNodeType.POWDER_SNOW;
         } else if (!lv.isOf(Blocks.CACTUS) && !lv.isOf(Blocks.SWEET_BERRY_BUSH)) {
            if (lv.isOf(Blocks.HONEY_BLOCK)) {
               return PathNodeType.STICKY_HONEY;
            } else if (lv.isOf(Blocks.COCOA)) {
               return PathNodeType.COCOA;
            } else if (!lv.isOf(Blocks.WITHER_ROSE) && !lv.isOf(Blocks.POINTED_DRIPSTONE)) {
               FluidState lv3 = world.getFluidState(pos);
               if (lv3.isIn(FluidTags.LAVA)) {
                  return PathNodeType.LAVA;
               } else if (inflictsFireDamage(lv)) {
                  return PathNodeType.DAMAGE_FIRE;
               } else if (lv2 instanceof DoorBlock) {
                  DoorBlock lv4 = (DoorBlock)lv2;
                  if ((Boolean)lv.get(DoorBlock.OPEN)) {
                     return PathNodeType.DOOR_OPEN;
                  } else {
                     return lv4.getBlockSetType().canOpenByHand() ? PathNodeType.DOOR_WOOD_CLOSED : PathNodeType.DOOR_IRON_CLOSED;
                  }
               } else if (lv2 instanceof AbstractRailBlock) {
                  return PathNodeType.RAIL;
               } else if (lv2 instanceof LeavesBlock) {
                  return PathNodeType.LEAVES;
               } else if (lv.isIn(BlockTags.FENCES) || lv.isIn(BlockTags.WALLS) || lv2 instanceof FenceGateBlock && !(Boolean)lv.get(FenceGateBlock.OPEN)) {
                  return PathNodeType.FENCE;
               } else if (!lv.canPathfindThrough(world, pos, NavigationType.LAND)) {
                  return PathNodeType.BLOCKED;
               } else {
                  return lv3.isIn(FluidTags.WATER) ? PathNodeType.WATER : PathNodeType.OPEN;
               }
            } else {
               return PathNodeType.DAMAGE_CAUTIOUS;
            }
         } else {
            return PathNodeType.DAMAGE_OTHER;
         }
      } else {
         return PathNodeType.TRAPDOOR;
      }
   }

   public static boolean inflictsFireDamage(BlockState state) {
      return state.isIn(BlockTags.FIRE) || state.isOf(Blocks.LAVA) || state.isOf(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state) || state.isOf(Blocks.LAVA_CAULDRON);
   }
}
