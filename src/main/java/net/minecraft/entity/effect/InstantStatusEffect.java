package net.minecraft.entity.effect;

public class InstantStatusEffect extends StatusEffect {
   public InstantStatusEffect(StatusEffectCategory arg, int i) {
      super(arg, i);
   }

   public boolean isInstant() {
      return true;
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return duration >= 1;
   }
}
