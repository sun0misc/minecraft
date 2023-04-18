package net.minecraft.loot.condition;

import java.util.Iterator;
import java.util.function.Function;

public interface LootConditionConsumingBuilder {
   LootConditionConsumingBuilder conditionally(LootCondition.Builder condition);

   default LootConditionConsumingBuilder conditionally(Iterable conditions, Function toBuilderFunction) {
      LootConditionConsumingBuilder lv = this.getThisConditionConsumingBuilder();

      Object object;
      for(Iterator var4 = conditions.iterator(); var4.hasNext(); lv = lv.conditionally((LootCondition.Builder)toBuilderFunction.apply(object))) {
         object = var4.next();
      }

      return lv;
   }

   LootConditionConsumingBuilder getThisConditionConsumingBuilder();
}
