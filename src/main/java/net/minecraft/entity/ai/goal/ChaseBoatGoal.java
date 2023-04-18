package net.minecraft.entity.ai.goal;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ChaseBoatGoal extends Goal {
   private int updateCountdownTicks;
   private final PathAwareEntity mob;
   @Nullable
   private PlayerEntity passenger;
   private ChaseBoatState state;

   public ChaseBoatGoal(PathAwareEntity mob) {
      this.mob = mob;
   }

   public boolean canStart() {
      List list = this.mob.world.getNonSpectatingEntities(BoatEntity.class, this.mob.getBoundingBox().expand(5.0));
      boolean bl = false;
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         BoatEntity lv = (BoatEntity)var3.next();
         Entity lv2 = lv.getControllingPassenger();
         if (lv2 instanceof PlayerEntity && (MathHelper.abs(((PlayerEntity)lv2).sidewaysSpeed) > 0.0F || MathHelper.abs(((PlayerEntity)lv2).forwardSpeed) > 0.0F)) {
            bl = true;
            break;
         }
      }

      return this.passenger != null && (MathHelper.abs(this.passenger.sidewaysSpeed) > 0.0F || MathHelper.abs(this.passenger.forwardSpeed) > 0.0F) || bl;
   }

   public boolean canStop() {
      return true;
   }

   public boolean shouldContinue() {
      return this.passenger != null && this.passenger.hasVehicle() && (MathHelper.abs(this.passenger.sidewaysSpeed) > 0.0F || MathHelper.abs(this.passenger.forwardSpeed) > 0.0F);
   }

   public void start() {
      List list = this.mob.world.getNonSpectatingEntities(BoatEntity.class, this.mob.getBoundingBox().expand(5.0));
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         BoatEntity lv = (BoatEntity)var2.next();
         if (lv.getControllingPassenger() != null && lv.getControllingPassenger() instanceof PlayerEntity) {
            this.passenger = (PlayerEntity)lv.getControllingPassenger();
            break;
         }
      }

      this.updateCountdownTicks = 0;
      this.state = ChaseBoatState.GO_TO_BOAT;
   }

   public void stop() {
      this.passenger = null;
   }

   public void tick() {
      boolean bl = MathHelper.abs(this.passenger.sidewaysSpeed) > 0.0F || MathHelper.abs(this.passenger.forwardSpeed) > 0.0F;
      float f = this.state == ChaseBoatState.GO_IN_BOAT_DIRECTION ? (bl ? 0.01F : 0.0F) : 0.015F;
      this.mob.updateVelocity(f, new Vec3d((double)this.mob.sidewaysSpeed, (double)this.mob.upwardSpeed, (double)this.mob.forwardSpeed));
      this.mob.move(MovementType.SELF, this.mob.getVelocity());
      if (--this.updateCountdownTicks <= 0) {
         this.updateCountdownTicks = this.getTickCount(10);
         if (this.state == ChaseBoatState.GO_TO_BOAT) {
            BlockPos lv = this.passenger.getBlockPos().offset(this.passenger.getHorizontalFacing().getOpposite());
            lv = lv.add(0, -1, 0);
            this.mob.getNavigation().startMovingTo((double)lv.getX(), (double)lv.getY(), (double)lv.getZ(), 1.0);
            if (this.mob.distanceTo(this.passenger) < 4.0F) {
               this.updateCountdownTicks = 0;
               this.state = ChaseBoatState.GO_IN_BOAT_DIRECTION;
            }
         } else if (this.state == ChaseBoatState.GO_IN_BOAT_DIRECTION) {
            Direction lv2 = this.passenger.getMovementDirection();
            BlockPos lv3 = this.passenger.getBlockPos().offset((Direction)lv2, 10);
            this.mob.getNavigation().startMovingTo((double)lv3.getX(), (double)(lv3.getY() - 1), (double)lv3.getZ(), 1.0);
            if (this.mob.distanceTo(this.passenger) > 12.0F) {
               this.updateCountdownTicks = 0;
               this.state = ChaseBoatState.GO_TO_BOAT;
            }
         }

      }
   }
}
