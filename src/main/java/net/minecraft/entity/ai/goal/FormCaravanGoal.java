package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.math.Vec3d;

public class FormCaravanGoal extends Goal {
   public final LlamaEntity llama;
   private double speed;
   private static final int MAX_CARAVAN_LENGTH = 8;
   private int counter;

   public FormCaravanGoal(LlamaEntity llama, double speed) {
      this.llama = llama;
      this.speed = speed;
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      if (!this.llama.isLeashed() && !this.llama.isFollowing()) {
         List list = this.llama.world.getOtherEntities(this.llama, this.llama.getBoundingBox().expand(9.0, 4.0, 9.0), (entity) -> {
            EntityType lv = entity.getType();
            return lv == EntityType.LLAMA || lv == EntityType.TRADER_LLAMA;
         });
         LlamaEntity lv = null;
         double d = Double.MAX_VALUE;
         Iterator var5 = list.iterator();

         Entity lv2;
         LlamaEntity lv3;
         double e;
         while(var5.hasNext()) {
            lv2 = (Entity)var5.next();
            lv3 = (LlamaEntity)lv2;
            if (lv3.isFollowing() && !lv3.hasFollower()) {
               e = this.llama.squaredDistanceTo(lv3);
               if (!(e > d)) {
                  d = e;
                  lv = lv3;
               }
            }
         }

         if (lv == null) {
            var5 = list.iterator();

            while(var5.hasNext()) {
               lv2 = (Entity)var5.next();
               lv3 = (LlamaEntity)lv2;
               if (lv3.isLeashed() && !lv3.hasFollower()) {
                  e = this.llama.squaredDistanceTo(lv3);
                  if (!(e > d)) {
                     d = e;
                     lv = lv3;
                  }
               }
            }
         }

         if (lv == null) {
            return false;
         } else if (d < 4.0) {
            return false;
         } else if (!lv.isLeashed() && !this.canFollow(lv, 1)) {
            return false;
         } else {
            this.llama.follow(lv);
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean shouldContinue() {
      if (this.llama.isFollowing() && this.llama.getFollowing().isAlive() && this.canFollow(this.llama, 0)) {
         double d = this.llama.squaredDistanceTo(this.llama.getFollowing());
         if (d > 676.0) {
            if (this.speed <= 3.0) {
               this.speed *= 1.2;
               this.counter = toGoalTicks(40);
               return true;
            }

            if (this.counter == 0) {
               return false;
            }
         }

         if (this.counter > 0) {
            --this.counter;
         }

         return true;
      } else {
         return false;
      }
   }

   public void stop() {
      this.llama.stopFollowing();
      this.speed = 2.1;
   }

   public void tick() {
      if (this.llama.isFollowing()) {
         if (!(this.llama.getHoldingEntity() instanceof LeashKnotEntity)) {
            LlamaEntity lv = this.llama.getFollowing();
            double d = (double)this.llama.distanceTo(lv);
            float f = 2.0F;
            Vec3d lv2 = (new Vec3d(lv.getX() - this.llama.getX(), lv.getY() - this.llama.getY(), lv.getZ() - this.llama.getZ())).normalize().multiply(Math.max(d - 2.0, 0.0));
            this.llama.getNavigation().startMovingTo(this.llama.getX() + lv2.x, this.llama.getY() + lv2.y, this.llama.getZ() + lv2.z, this.speed);
         }
      }
   }

   private boolean canFollow(LlamaEntity llama, int length) {
      if (length > 8) {
         return false;
      } else if (llama.isFollowing()) {
         if (llama.getFollowing().isLeashed()) {
            return true;
         } else {
            LlamaEntity var10001 = llama.getFollowing();
            ++length;
            return this.canFollow(var10001, length);
         }
      } else {
         return false;
      }
   }
}
