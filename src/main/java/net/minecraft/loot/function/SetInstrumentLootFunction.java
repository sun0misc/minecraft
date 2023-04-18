package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetInstrumentLootFunction extends ConditionalLootFunction {
   final TagKey options;

   SetInstrumentLootFunction(LootCondition[] conditions, TagKey options) {
      super(conditions);
      this.options = options;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_INSTRUMENT;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      GoatHornItem.setRandomInstrumentFromTag(stack, this.options, context.getRandom());
      return stack;
   }

   public static ConditionalLootFunction.Builder builder(TagKey options) {
      return builder((conditions) -> {
         return new SetInstrumentLootFunction(conditions, options);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetInstrumentLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.addProperty("options", "#" + arg.options.id());
      }

      public SetInstrumentLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         String string = JsonHelper.getString(jsonObject, "options");
         if (!string.startsWith("#")) {
            throw new JsonSyntaxException("Inline tag value not supported: " + string);
         } else {
            return new SetInstrumentLootFunction(args, TagKey.of(RegistryKeys.INSTRUMENT, new Identifier(string.substring(1))));
         }
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
