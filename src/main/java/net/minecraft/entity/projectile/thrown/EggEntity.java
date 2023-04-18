package net.minecraft.entity.projectile.thrown;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class EggEntity extends ThrownItemEntity {
   public EggEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public EggEntity(World world, LivingEntity owner) {
      super(EntityType.EGG, owner, world);
   }

   public EggEntity(World world, double x, double y, double z) {
      super(EntityType.EGG, x, y, z, world);
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
         double d = 0.08;

         for(int i = 0; i < 8; ++i) {
            this.world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, this.getStack()), this.getX(), this.getY(), this.getZ(), ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08);
         }
      }

   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      entityHitResult.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), 0.0F);
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      if (!this.world.isClient) {
         if (this.random.nextInt(8) == 0) {
            int i = 1;
            if (this.random.nextInt(32) == 0) {
               i = 4;
            }

            for(int j = 0; j < i; ++j) {
               ChickenEntity lv = (ChickenEntity)EntityType.CHICKEN.create(this.world);
               if (lv != null) {
                  lv.setBreedingAge(-24000);
                  lv.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0F);
                  this.world.spawnEntity(lv);
               }
            }
         }

         this.world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
         this.discard();
      }

   }

   protected Item getDefaultItem() {
      return Items.EGG;
   }
}
