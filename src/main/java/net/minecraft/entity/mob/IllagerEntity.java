package net.minecraft.entity.mob;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.world.World;

public abstract class IllagerEntity extends RaiderEntity {
   protected IllagerEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      super.initGoals();
   }

   public EntityGroup getGroup() {
      return EntityGroup.ILLAGER;
   }

   public State getState() {
      return IllagerEntity.State.CROSSED;
   }

   public boolean canTarget(LivingEntity target) {
      return target instanceof MerchantEntity && target.isBaby() ? false : super.canTarget(target);
   }

   public static enum State {
      CROSSED,
      ATTACKING,
      SPELLCASTING,
      BOW_AND_ARROW,
      CROSSBOW_HOLD,
      CROSSBOW_CHARGE,
      CELEBRATING,
      NEUTRAL;

      // $FF: synthetic method
      private static State[] method_36647() {
         return new State[]{CROSSED, ATTACKING, SPELLCASTING, BOW_AND_ARROW, CROSSBOW_HOLD, CROSSBOW_CHARGE, CELEBRATING, NEUTRAL};
      }
   }

   protected class LongDoorInteractGoal extends net.minecraft.entity.ai.goal.LongDoorInteractGoal {
      public LongDoorInteractGoal(RaiderEntity raider) {
         super(raider, false);
      }

      public boolean canStart() {
         return super.canStart() && IllagerEntity.this.hasActiveRaid();
      }
   }
}
