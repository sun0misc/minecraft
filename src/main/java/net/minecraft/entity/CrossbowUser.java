package net.minecraft.entity;

import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public interface CrossbowUser extends RangedAttackMob {
   void setCharging(boolean charging);

   void shoot(LivingEntity target, ItemStack crossbow, ProjectileEntity projectile, float multiShotSpray);

   @Nullable
   LivingEntity getTarget();

   void postShoot();

   default void shoot(LivingEntity entity, float speed) {
      Hand lv = ProjectileUtil.getHandPossiblyHolding(entity, Items.CROSSBOW);
      ItemStack lv2 = entity.getStackInHand(lv);
      if (entity.isHolding(Items.CROSSBOW)) {
         CrossbowItem.shootAll(entity.world, entity, lv, lv2, speed, (float)(14 - entity.world.getDifficulty().getId() * 4));
      }

      this.postShoot();
   }

   default void shoot(LivingEntity entity, LivingEntity target, ProjectileEntity projectile, float multishotSpray, float speed) {
      double d = target.getX() - entity.getX();
      double e = target.getZ() - entity.getZ();
      double h = Math.sqrt(d * d + e * e);
      double i = target.getBodyY(0.3333333333333333) - projectile.getY() + h * 0.20000000298023224;
      Vector3f vector3f = this.getProjectileLaunchVelocity(entity, new Vec3d(d, i, e), multishotSpray);
      projectile.setVelocity((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), speed, (float)(14 - entity.world.getDifficulty().getId() * 4));
      entity.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0F, 1.0F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   default Vector3f getProjectileLaunchVelocity(LivingEntity entity, Vec3d positionDelta, float multishotSpray) {
      Vector3f vector3f = positionDelta.toVector3f().normalize();
      Vector3f vector3f2 = (new Vector3f(vector3f)).cross(new Vector3f(0.0F, 1.0F, 0.0F));
      if ((double)vector3f2.lengthSquared() <= 1.0E-7) {
         Vec3d lv = entity.getOppositeRotationVector(1.0F);
         vector3f2 = (new Vector3f(vector3f)).cross(lv.toVector3f());
      }

      Vector3f vector3f3 = (new Vector3f(vector3f)).rotateAxis(1.5707964F, vector3f2.x, vector3f2.y, vector3f2.z);
      return (new Vector3f(vector3f)).rotateAxis(multishotSpray * 0.017453292F, vector3f3.x, vector3f3.y, vector3f3.z);
   }
}
