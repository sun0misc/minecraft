package net.minecraft.entity.projectile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class WitherSkullEntity extends ExplosiveProjectileEntity {
   private static final TrackedData CHARGED;

   public WitherSkullEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public WitherSkullEntity(World world, LivingEntity owner, double directionX, double directionY, double directionZ) {
      super(EntityType.WITHER_SKULL, owner, directionX, directionY, directionZ, world);
   }

   protected float getDrag() {
      return this.isCharged() ? 0.73F : super.getDrag();
   }

   public boolean isOnFire() {
      return false;
   }

   public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
      return this.isCharged() && WitherEntity.canDestroy(blockState) ? Math.min(0.8F, max) : max;
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      if (!this.world.isClient) {
         Entity lv = entityHitResult.getEntity();
         Entity lv2 = this.getOwner();
         boolean bl;
         LivingEntity lv3;
         if (lv2 instanceof LivingEntity) {
            lv3 = (LivingEntity)lv2;
            bl = lv.damage(this.getDamageSources().witherSkull(this, lv3), 8.0F);
            if (bl) {
               if (lv.isAlive()) {
                  this.applyDamageEffects(lv3, lv);
               } else {
                  lv3.heal(5.0F);
               }
            }
         } else {
            bl = lv.damage(this.getDamageSources().magic(), 5.0F);
         }

         if (bl && lv instanceof LivingEntity) {
            lv3 = (LivingEntity)lv;
            int i = 0;
            if (this.world.getDifficulty() == Difficulty.NORMAL) {
               i = 10;
            } else if (this.world.getDifficulty() == Difficulty.HARD) {
               i = 40;
            }

            if (i > 0) {
               lv3.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 20 * i, 1), this.getEffectCause());
            }
         }

      }
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      if (!this.world.isClient) {
         this.world.createExplosion(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, World.ExplosionSourceType.MOB);
         this.discard();
      }

   }

   public boolean canHit() {
      return false;
   }

   public boolean damage(DamageSource source, float amount) {
      return false;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(CHARGED, false);
   }

   public boolean isCharged() {
      return (Boolean)this.dataTracker.get(CHARGED);
   }

   public void setCharged(boolean charged) {
      this.dataTracker.set(CHARGED, charged);
   }

   protected boolean isBurning() {
      return false;
   }

   static {
      CHARGED = DataTracker.registerData(WitherSkullEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
