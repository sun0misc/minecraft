package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SittingScanningPhase extends AbstractSittingPhase {
   private static final int DURATION = 100;
   private static final int MAX_HEIGHT_CLOSE_PLAYER_RANGE = 10;
   private static final int MAX_HORIZONTAL_CLOSE_PLAYER_RANGE = 20;
   private static final int MAX_PLAYER_RANGE = 150;
   private static final TargetPredicate PLAYER_WITHIN_RANGE_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(150.0);
   private final TargetPredicate CLOSE_PLAYER_PREDICATE;
   private int ticks;

   public SittingScanningPhase(EnderDragonEntity arg) {
      super(arg);
      this.CLOSE_PLAYER_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(20.0).setPredicate((player) -> {
         return Math.abs(player.getY() - arg.getY()) <= 10.0;
      });
   }

   public void serverTick() {
      ++this.ticks;
      LivingEntity lv = this.dragon.world.getClosestPlayer(this.CLOSE_PLAYER_PREDICATE, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (lv != null) {
         if (this.ticks > 25) {
            this.dragon.getPhaseManager().setPhase(PhaseType.SITTING_ATTACKING);
         } else {
            Vec3d lv2 = (new Vec3d(lv.getX() - this.dragon.getX(), 0.0, lv.getZ() - this.dragon.getZ())).normalize();
            Vec3d lv3 = (new Vec3d((double)MathHelper.sin(this.dragon.getYaw() * 0.017453292F), 0.0, (double)(-MathHelper.cos(this.dragon.getYaw() * 0.017453292F)))).normalize();
            float f = (float)lv3.dotProduct(lv2);
            float g = (float)(Math.acos((double)f) * 57.2957763671875) + 0.5F;
            if (g < 0.0F || g > 10.0F) {
               double d = lv.getX() - this.dragon.head.getX();
               double e = lv.getZ() - this.dragon.head.getZ();
               double h = MathHelper.clamp(MathHelper.wrapDegrees(180.0 - MathHelper.atan2(d, e) * 57.2957763671875 - (double)this.dragon.getYaw()), -100.0, 100.0);
               EnderDragonEntity var10000 = this.dragon;
               var10000.yawAcceleration *= 0.8F;
               float i = (float)Math.sqrt(d * d + e * e) + 1.0F;
               float j = i;
               if (i > 40.0F) {
                  i = 40.0F;
               }

               var10000 = this.dragon;
               var10000.yawAcceleration += (float)h * (0.7F / i / j);
               this.dragon.setYaw(this.dragon.getYaw() + this.dragon.yawAcceleration);
            }
         }
      } else if (this.ticks >= 100) {
         lv = this.dragon.world.getClosestPlayer(PLAYER_WITHIN_RANGE_PREDICATE, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
         this.dragon.getPhaseManager().setPhase(PhaseType.TAKEOFF);
         if (lv != null) {
            this.dragon.getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
            ((ChargingPlayerPhase)this.dragon.getPhaseManager().create(PhaseType.CHARGING_PLAYER)).setPathTarget(new Vec3d(lv.getX(), lv.getY(), lv.getZ()));
         }
      }

   }

   public void beginPhase() {
      this.ticks = 0;
   }

   public PhaseType getType() {
      return PhaseType.SITTING_SCANNING;
   }
}
