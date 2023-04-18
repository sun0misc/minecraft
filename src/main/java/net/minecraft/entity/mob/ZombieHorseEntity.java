package net.minecraft.entity.mob;

import java.util.Objects;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ZombieHorseEntity extends AbstractHorseEntity {
   public ZombieHorseEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public static DefaultAttributeContainer.Builder createZombieHorseAttributes() {
      return createBaseHorseAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20000000298023224);
   }

   protected void initAttributes(Random random) {
      EntityAttributeInstance var10000 = this.getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH);
      Objects.requireNonNull(random);
      var10000.setBaseValue(getChildJumpStrengthBonus(random::nextDouble));
   }

   public EntityGroup getGroup() {
      return EntityGroup.UNDEAD;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ZOMBIE_HORSE_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOMBIE_HORSE_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ZOMBIE_HORSE_HURT;
   }

   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return (PassiveEntity)EntityType.ZOMBIE_HORSE.create(world);
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      return !this.isTame() ? ActionResult.PASS : super.interactMob(player, hand);
   }

   protected void initCustomGoals() {
   }
}
