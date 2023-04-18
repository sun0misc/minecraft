package net.minecraft.potion;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class Potion {
   @Nullable
   private final String baseName;
   private final ImmutableList effects;

   public static Potion byId(String id) {
      return (Potion)Registries.POTION.get(Identifier.tryParse(id));
   }

   public Potion(StatusEffectInstance... effects) {
      this((String)null, effects);
   }

   public Potion(@Nullable String baseName, StatusEffectInstance... effects) {
      this.baseName = baseName;
      this.effects = ImmutableList.copyOf(effects);
   }

   public String finishTranslationKey(String prefix) {
      return prefix + (this.baseName == null ? Registries.POTION.getId(this).getPath() : this.baseName);
   }

   public List getEffects() {
      return this.effects;
   }

   public boolean hasInstantEffect() {
      if (!this.effects.isEmpty()) {
         UnmodifiableIterator var1 = this.effects.iterator();

         while(var1.hasNext()) {
            StatusEffectInstance lv = (StatusEffectInstance)var1.next();
            if (lv.getEffectType().isInstant()) {
               return true;
            }
         }
      }

      return false;
   }
}
