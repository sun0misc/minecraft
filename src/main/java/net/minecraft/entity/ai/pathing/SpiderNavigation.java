package net.minecraft.entity.ai.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpiderNavigation extends MobNavigation {
   @Nullable
   private BlockPos targetPos;

   public SpiderNavigation(MobEntity arg, World arg2) {
      super(arg, arg2);
   }

   public Path findPathTo(BlockPos target, int distance) {
      this.targetPos = target;
      return super.findPathTo(target, distance);
   }

   public Path findPathTo(Entity entity, int distance) {
      this.targetPos = entity.getBlockPos();
      return super.findPathTo(entity, distance);
   }

   public boolean startMovingTo(Entity entity, double speed) {
      Path lv = this.findPathTo((Entity)entity, 0);
      if (lv != null) {
         return this.startMovingAlong(lv, speed);
      } else {
         this.targetPos = entity.getBlockPos();
         this.speed = speed;
         return true;
      }
   }

   public void tick() {
      if (!this.isIdle()) {
         super.tick();
      } else {
         if (this.targetPos != null) {
            if (!this.targetPos.isWithinDistance(this.entity.getPos(), (double)this.entity.getWidth()) && (!(this.entity.getY() > (double)this.targetPos.getY()) || !BlockPos.ofFloored((double)this.targetPos.getX(), this.entity.getY(), (double)this.targetPos.getZ()).isWithinDistance(this.entity.getPos(), (double)this.entity.getWidth()))) {
               this.entity.getMoveControl().moveTo((double)this.targetPos.getX(), (double)this.targetPos.getY(), (double)this.targetPos.getZ(), this.speed);
            } else {
               this.targetPos = null;
            }
         }

      }
   }
}
