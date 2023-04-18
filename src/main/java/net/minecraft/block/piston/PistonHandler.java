package net.minecraft.block.piston;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PistonHandler {
   public static final int MAX_MOVABLE_BLOCKS = 12;
   private final World world;
   private final BlockPos posFrom;
   private final boolean retracted;
   private final BlockPos posTo;
   private final Direction motionDirection;
   private final List movedBlocks = Lists.newArrayList();
   private final List brokenBlocks = Lists.newArrayList();
   private final Direction pistonDirection;

   public PistonHandler(World world, BlockPos pos, Direction dir, boolean retracted) {
      this.world = world;
      this.posFrom = pos;
      this.pistonDirection = dir;
      this.retracted = retracted;
      if (retracted) {
         this.motionDirection = dir;
         this.posTo = pos.offset(dir);
      } else {
         this.motionDirection = dir.getOpposite();
         this.posTo = pos.offset((Direction)dir, 2);
      }

   }

   public boolean calculatePush() {
      this.movedBlocks.clear();
      this.brokenBlocks.clear();
      BlockState lv = this.world.getBlockState(this.posTo);
      if (!PistonBlock.isMovable(lv, this.world, this.posTo, this.motionDirection, false, this.pistonDirection)) {
         if (this.retracted && lv.getPistonBehavior() == PistonBehavior.DESTROY) {
            this.brokenBlocks.add(this.posTo);
            return true;
         } else {
            return false;
         }
      } else if (!this.tryMove(this.posTo, this.motionDirection)) {
         return false;
      } else {
         for(int i = 0; i < this.movedBlocks.size(); ++i) {
            BlockPos lv2 = (BlockPos)this.movedBlocks.get(i);
            if (isBlockSticky(this.world.getBlockState(lv2)) && !this.tryMoveAdjacentBlock(lv2)) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isBlockSticky(BlockState state) {
      return state.isOf(Blocks.SLIME_BLOCK) || state.isOf(Blocks.HONEY_BLOCK);
   }

   private static boolean isAdjacentBlockStuck(BlockState state, BlockState adjacentState) {
      if (state.isOf(Blocks.HONEY_BLOCK) && adjacentState.isOf(Blocks.SLIME_BLOCK)) {
         return false;
      } else if (state.isOf(Blocks.SLIME_BLOCK) && adjacentState.isOf(Blocks.HONEY_BLOCK)) {
         return false;
      } else {
         return isBlockSticky(state) || isBlockSticky(adjacentState);
      }
   }

   private boolean tryMove(BlockPos pos, Direction dir) {
      BlockState lv = this.world.getBlockState(pos);
      if (lv.isAir()) {
         return true;
      } else if (!PistonBlock.isMovable(lv, this.world, pos, this.motionDirection, false, dir)) {
         return true;
      } else if (pos.equals(this.posFrom)) {
         return true;
      } else if (this.movedBlocks.contains(pos)) {
         return true;
      } else {
         int i = 1;
         if (i + this.movedBlocks.size() > 12) {
            return false;
         } else {
            while(isBlockSticky(lv)) {
               BlockPos lv2 = pos.offset(this.motionDirection.getOpposite(), i);
               BlockState lv3 = lv;
               lv = this.world.getBlockState(lv2);
               if (lv.isAir() || !isAdjacentBlockStuck(lv3, lv) || !PistonBlock.isMovable(lv, this.world, lv2, this.motionDirection, false, this.motionDirection.getOpposite()) || lv2.equals(this.posFrom)) {
                  break;
               }

               ++i;
               if (i + this.movedBlocks.size() > 12) {
                  return false;
               }
            }

            int j = 0;

            int k;
            for(k = i - 1; k >= 0; --k) {
               this.movedBlocks.add(pos.offset(this.motionDirection.getOpposite(), k));
               ++j;
            }

            k = 1;

            while(true) {
               BlockPos lv4 = pos.offset(this.motionDirection, k);
               int l = this.movedBlocks.indexOf(lv4);
               if (l > -1) {
                  this.setMovedBlocks(j, l);

                  for(int m = 0; m <= l + j; ++m) {
                     BlockPos lv5 = (BlockPos)this.movedBlocks.get(m);
                     if (isBlockSticky(this.world.getBlockState(lv5)) && !this.tryMoveAdjacentBlock(lv5)) {
                        return false;
                     }
                  }

                  return true;
               }

               lv = this.world.getBlockState(lv4);
               if (lv.isAir()) {
                  return true;
               }

               if (!PistonBlock.isMovable(lv, this.world, lv4, this.motionDirection, true, this.motionDirection) || lv4.equals(this.posFrom)) {
                  return false;
               }

               if (lv.getPistonBehavior() == PistonBehavior.DESTROY) {
                  this.brokenBlocks.add(lv4);
                  return true;
               }

               if (this.movedBlocks.size() >= 12) {
                  return false;
               }

               this.movedBlocks.add(lv4);
               ++j;
               ++k;
            }
         }
      }
   }

   private void setMovedBlocks(int from, int to) {
      List list = Lists.newArrayList();
      List list2 = Lists.newArrayList();
      List list3 = Lists.newArrayList();
      list.addAll(this.movedBlocks.subList(0, to));
      list2.addAll(this.movedBlocks.subList(this.movedBlocks.size() - from, this.movedBlocks.size()));
      list3.addAll(this.movedBlocks.subList(to, this.movedBlocks.size() - from));
      this.movedBlocks.clear();
      this.movedBlocks.addAll(list);
      this.movedBlocks.addAll(list2);
      this.movedBlocks.addAll(list3);
   }

   private boolean tryMoveAdjacentBlock(BlockPos pos) {
      BlockState lv = this.world.getBlockState(pos);
      Direction[] var3 = Direction.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Direction lv2 = var3[var5];
         if (lv2.getAxis() != this.motionDirection.getAxis()) {
            BlockPos lv3 = pos.offset(lv2);
            BlockState lv4 = this.world.getBlockState(lv3);
            if (isAdjacentBlockStuck(lv4, lv) && !this.tryMove(lv3, lv2)) {
               return false;
            }
         }
      }

      return true;
   }

   public Direction getMotionDirection() {
      return this.motionDirection;
   }

   public List getMovedBlocks() {
      return this.movedBlocks;
   }

   public List getBrokenBlocks() {
      return this.brokenBlocks;
   }
}
