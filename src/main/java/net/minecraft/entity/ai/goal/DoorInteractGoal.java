package net.minecraft.entity.ai.goal;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

public abstract class DoorInteractGoal extends Goal {
   protected MobEntity mob;
   protected BlockPos doorPos;
   protected boolean doorValid;
   private boolean shouldStop;
   private float offsetX;
   private float offsetZ;

   public DoorInteractGoal(MobEntity mob) {
      this.doorPos = BlockPos.ORIGIN;
      this.mob = mob;
      if (!NavigationConditions.hasMobNavigation(mob)) {
         throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
      }
   }

   protected boolean isDoorOpen() {
      if (!this.doorValid) {
         return false;
      } else {
         BlockState lv = this.mob.world.getBlockState(this.doorPos);
         if (!(lv.getBlock() instanceof DoorBlock)) {
            this.doorValid = false;
            return false;
         } else {
            return (Boolean)lv.get(DoorBlock.OPEN);
         }
      }
   }

   protected void setDoorOpen(boolean open) {
      if (this.doorValid) {
         BlockState lv = this.mob.world.getBlockState(this.doorPos);
         if (lv.getBlock() instanceof DoorBlock) {
            ((DoorBlock)lv.getBlock()).setOpen(this.mob, this.mob.world, lv, this.doorPos, open);
         }
      }

   }

   public boolean canStart() {
      if (!NavigationConditions.hasMobNavigation(this.mob)) {
         return false;
      } else if (!this.mob.horizontalCollision) {
         return false;
      } else {
         MobNavigation lv = (MobNavigation)this.mob.getNavigation();
         Path lv2 = lv.getCurrentPath();
         if (lv2 != null && !lv2.isFinished() && lv.canEnterOpenDoors()) {
            for(int i = 0; i < Math.min(lv2.getCurrentNodeIndex() + 2, lv2.getLength()); ++i) {
               PathNode lv3 = lv2.getNode(i);
               this.doorPos = new BlockPos(lv3.x, lv3.y + 1, lv3.z);
               if (!(this.mob.squaredDistanceTo((double)this.doorPos.getX(), this.mob.getY(), (double)this.doorPos.getZ()) > 2.25)) {
                  this.doorValid = DoorBlock.canOpenByHand(this.mob.world, this.doorPos);
                  if (this.doorValid) {
                     return true;
                  }
               }
            }

            this.doorPos = this.mob.getBlockPos().up();
            this.doorValid = DoorBlock.canOpenByHand(this.mob.world, this.doorPos);
            return this.doorValid;
         } else {
            return false;
         }
      }
   }

   public boolean shouldContinue() {
      return !this.shouldStop;
   }

   public void start() {
      this.shouldStop = false;
      this.offsetX = (float)((double)this.doorPos.getX() + 0.5 - this.mob.getX());
      this.offsetZ = (float)((double)this.doorPos.getZ() + 0.5 - this.mob.getZ());
   }

   public boolean shouldRunEveryTick() {
      return true;
   }

   public void tick() {
      float f = (float)((double)this.doorPos.getX() + 0.5 - this.mob.getX());
      float g = (float)((double)this.doorPos.getZ() + 0.5 - this.mob.getZ());
      float h = this.offsetX * f + this.offsetZ * g;
      if (h < 0.0F) {
         this.shouldStop = true;
      }

   }
}
