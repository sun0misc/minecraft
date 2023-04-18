package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

public class SetCountLootFunction extends ConditionalLootFunction {
   final LootNumberProvider countRange;
   final boolean add;

   SetCountLootFunction(LootCondition[] conditions, LootNumberProvider countRange, boolean add) {
      super(conditions);
      this.countRange = countRange;
      this.add = add;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_COUNT;
   }

   public Set getRequiredParameters() {
      return this.countRange.getRequiredParameters();
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      int i = this.add ? stack.getCount() : 0;
      stack.setCount(MathHelper.clamp(i + this.countRange.nextInt(context), 0, stack.getMaxCount()));
      return stack;
   }

   public static ConditionalLootFunction.Builder builder(LootNumberProvider countRange) {
      return builder((conditions) -> {
         return new SetCountLootFunction(conditions, countRange, false);
      });
   }

   public static ConditionalLootFunction.Builder builder(LootNumberProvider countRange, boolean add) {
      return builder((conditions) -> {
         return new SetCountLootFunction(conditions, countRange, add);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetCountLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.add("count", jsonSerializationContext.serialize(arg.countRange));
         jsonObject.addProperty("add", arg.add);
      }

      public SetCountLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         LootNumberProvider lv = (LootNumberProvider)JsonHelper.deserialize(jsonObject, "count", jsonDeserializationContext, LootNumberProvider.class);
         boolean bl = JsonHelper.getBoolean(jsonObject, "add", false);
         return new SetCountLootFunction(args, lv, bl);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
