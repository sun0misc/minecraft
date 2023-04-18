package net.minecraft.entity.projectile;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class SmallFireballEntity extends AbstractFireballEntity {
   public SmallFireballEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public SmallFireballEntity(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ) {
      super(EntityType.SMALL_FIREBALL, owner, velocityX, velocityY, velocityZ, world);
   }

   public SmallFireballEntity(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      super(EntityType.SMALL_FIREBALL, x, y, z, velocityX, velocityY, velocityZ, world);
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      if (!this.world.isClient) {
         Entity lv = entityHitResult.getEntity();
         Entity lv2 = this.getOwner();
         int i = lv.getFireTicks();
         lv.setOnFireFor(5);
         if (!lv.damage(this.getDamageSources().fireball(this, lv2), 5.0F)) {
            lv.setFireTicks(i);
         } else if (lv2 instanceof LivingEntity) {
            this.applyDamageEffects((LivingEntity)lv2, lv);
         }

      }
   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      super.onBlockHit(blockHitResult);
      if (!this.world.isClient) {
         Entity lv = this.getOwner();
         if (!(lv instanceof MobEntity) || this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            BlockPos lv2 = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
            if (this.world.isAir(lv2)) {
               this.world.setBlockState(lv2, AbstractFireBlock.getState(this.world, lv2));
            }
         }

      }
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      if (!this.world.isClient) {
         this.discard();
      }

   }

   public boolean canHit() {
      return false;
   }

   public boolean damage(DamageSource source, float amount) {
      return false;
   }
}
