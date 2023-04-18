package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataKey;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.slf4j.Logger;

public class ReferenceLootFunction extends ConditionalLootFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final Identifier name;

   ReferenceLootFunction(LootCondition[] conditions, Identifier name) {
      super(conditions);
      this.name = name;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.REFERENCE;
   }

   public void validate(LootTableReporter reporter) {
      LootDataKey lv = new LootDataKey(LootDataType.ITEM_MODIFIERS, this.name);
      if (reporter.isInStack(lv)) {
         reporter.report("Function " + this.name + " is recursively called");
      } else {
         super.validate(reporter);
         reporter.getDataLookup().getElementOptional(lv).ifPresentOrElse((itemModifier) -> {
            itemModifier.validate(reporter.makeChild(".{" + this.name + "}", lv));
         }, () -> {
            reporter.report("Unknown function table called " + this.name);
         });
      }
   }

   protected ItemStack process(ItemStack stack, LootContext context) {
      LootFunction lv = (LootFunction)context.getDataLookup().getElement(LootDataType.ITEM_MODIFIERS, this.name);
      if (lv == null) {
         LOGGER.warn("Unknown function: {}", this.name);
         return stack;
      } else {
         LootContext.Entry lv2 = LootContext.itemModifier(lv);
         if (context.markActive(lv2)) {
            ItemStack var5;
            try {
               var5 = (ItemStack)lv.apply(stack, context);
            } finally {
               context.markInactive(lv2);
            }

            return var5;
         } else {
            LOGGER.warn("Detected infinite loop in loot tables");
            return stack;
         }
      }
   }

   public static ConditionalLootFunction.Builder builder(Identifier name) {
      return builder((conditions) -> {
         return new ReferenceLootFunction(conditions, name);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, ReferenceLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("name", arg.name.toString());
      }

      public ReferenceLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "name"));
         return new ReferenceLootFunction(args, lv);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
