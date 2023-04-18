package net.minecraft.entity.ai.goal;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.sound.SoundEvent;

public class AmbientStandGoal extends Goal {
   private final AbstractHorseEntity entity;
   private int cooldown;

   public AmbientStandGoal(AbstractHorseEntity entity) {
      this.entity = entity;
      this.resetCooldown(entity);
   }

   public void start() {
      this.entity.updateAnger();
      this.playAmbientStandSound();
   }

   private void playAmbientStandSound() {
      SoundEvent lv = this.entity.getAmbientStandSound();
      if (lv != null) {
         this.entity.playSoundIfNotSilent(lv);
      }

   }

   public boolean shouldContinue() {
      return false;
   }

   public boolean canStart() {
      ++this.cooldown;
      if (this.cooldown > 0 && this.entity.getRandom().nextInt(1000) < this.cooldown) {
         this.resetCooldown(this.entity);
         return !this.entity.isImmobile() && this.entity.getRandom().nextInt(10) == 0;
      } else {
         return false;
      }
   }

   private void resetCooldown(AbstractHorseEntity entity) {
      this.cooldown = -entity.getMinAmbientStandDelay();
   }

   public boolean shouldRunEveryTick() {
      return true;
   }
}
