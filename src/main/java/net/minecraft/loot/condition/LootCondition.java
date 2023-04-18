package net.minecraft.loot.condition;

import java.util.function.Predicate;
import net.minecraft.loot.context.LootContextAware;

public interface LootCondition extends LootContextAware, Predicate {
   LootConditionType getType();

   @FunctionalInterface
   public interface Builder {
      LootCondition build();

      default Builder invert() {
         return InvertedLootCondition.builder(this);
      }

      default AlternativeLootCondition.Builder or(Builder condition) {
         return AlternativeLootCondition.builder(this, condition);
      }
   }
}
