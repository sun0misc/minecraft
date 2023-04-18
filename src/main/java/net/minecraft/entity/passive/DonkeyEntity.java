package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DonkeyEntity extends AbstractDonkeyEntity {
   public DonkeyEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_DONKEY_AMBIENT;
   }

   protected SoundEvent getAngrySound() {
      return SoundEvents.ENTITY_DONKEY_ANGRY;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_DONKEY_DEATH;
   }

   @Nullable
   protected SoundEvent getEatSound() {
      return SoundEvents.ENTITY_DONKEY_EAT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_DONKEY_HURT;
   }

   public boolean canBreedWith(AnimalEntity other) {
      if (other == this) {
         return false;
      } else if (!(other instanceof DonkeyEntity) && !(other instanceof HorseEntity)) {
         return false;
      } else {
         return this.canBreed() && ((AbstractHorseEntity)other).canBreed();
      }
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      EntityType lv = entity instanceof HorseEntity ? EntityType.MULE : EntityType.DONKEY;
      AbstractHorseEntity lv2 = (AbstractHorseEntity)lv.create(world);
      if (lv2 != null) {
         this.setChildAttributes(entity, lv2);
      }

      return lv2;
   }
}
