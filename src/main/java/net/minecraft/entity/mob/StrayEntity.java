package net.minecraft.entity.mob;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class StrayEntity extends AbstractSkeletonEntity {
   public StrayEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public static boolean canSpawn(EntityType type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      BlockPos lv = pos;

      do {
         lv = lv.up();
      } while(world.getBlockState(lv).isOf(Blocks.POWDER_SNOW));

      return canSpawnInDark(type, world, spawnReason, pos, random) && (spawnReason == SpawnReason.SPAWNER || world.isSkyVisible(lv.down()));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_STRAY_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_STRAY_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_STRAY_DEATH;
   }

   SoundEvent getStepSound() {
      return SoundEvents.ENTITY_STRAY_STEP;
   }

   protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
      PersistentProjectileEntity lv = super.createArrowProjectile(arrow, damageModifier);
      if (lv instanceof ArrowEntity) {
         ((ArrowEntity)lv).addEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 600));
      }

      return lv;
   }
}
