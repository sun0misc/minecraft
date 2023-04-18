package net.minecraft.loot.function;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

public interface LootFunctionConsumingBuilder {
   LootFunctionConsumingBuilder apply(LootFunction.Builder function);

   default LootFunctionConsumingBuilder apply(Iterable functions, Function toBuilderFunction) {
      LootFunctionConsumingBuilder lv = this.getThisFunctionConsumingBuilder();

      Object object;
      for(Iterator var4 = functions.iterator(); var4.hasNext(); lv = lv.apply((LootFunction.Builder)toBuilderFunction.apply(object))) {
         object = var4.next();
      }

      return lv;
   }

   default LootFunctionConsumingBuilder apply(Object[] functions, Function toBuilderFunction) {
      return this.apply((Iterable)Arrays.asList(functions), toBuilderFunction);
   }

   LootFunctionConsumingBuilder getThisFunctionConsumingBuilder();
}
