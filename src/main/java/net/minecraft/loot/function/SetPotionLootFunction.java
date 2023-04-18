package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetPotionLootFunction extends ConditionalLootFunction {
   final Potion potion;

   SetPotionLootFunction(LootCondition[] conditions, Potion potion) {
      super(conditions);
      this.potion = potion;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_POTION;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      PotionUtil.setPotion(stack, this.potion);
      return stack;
   }

   public static ConditionalLootFunction.Builder builder(Potion potion) {
      return builder((conditions) -> {
         return new SetPotionLootFunction(conditions, potion);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetPotionLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.addProperty("id", Registries.POTION.getId(arg.potion).toString());
      }

      public SetPotionLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         String string = JsonHelper.getString(jsonObject, "id");
         Potion lv = (Potion)Registries.POTION.getOrEmpty(Identifier.tryParse(string)).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown potion '" + string + "'");
         });
         return new SetPotionLootFunction(args, lv);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
