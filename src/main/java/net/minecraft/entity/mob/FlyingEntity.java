package net.minecraft.entity.mob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class FlyingEntity extends MobEntity {
   protected FlyingEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
   }

   public void travel(Vec3d movementInput) {
      if (this.isLogicalSideForUpdatingMovement()) {
         if (this.isTouchingWater()) {
            this.updateVelocity(0.02F, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.800000011920929));
         } else if (this.isInLava()) {
            this.updateVelocity(0.02F, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.5));
         } else {
            float f = 0.91F;
            if (this.onGround) {
               f = this.world.getBlockState(BlockPos.ofFloored(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getSlipperiness() * 0.91F;
            }

            float g = 0.16277137F / (f * f * f);
            f = 0.91F;
            if (this.onGround) {
               f = this.world.getBlockState(BlockPos.ofFloored(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getSlipperiness() * 0.91F;
            }

            this.updateVelocity(this.onGround ? 0.1F * g : 0.02F, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply((double)f));
         }
      }

      this.updateLimbs(false);
   }

   public boolean isClimbing() {
      return false;
   }
}
