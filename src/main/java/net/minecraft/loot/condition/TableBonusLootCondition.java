package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class TableBonusLootCondition implements LootCondition {
   final Enchantment enchantment;
   final float[] chances;

   TableBonusLootCondition(Enchantment enchantment, float[] chances) {
      this.enchantment = enchantment;
      this.chances = chances;
   }

   public LootConditionType getType() {
      return LootConditionTypes.TABLE_BONUS;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.TOOL);
   }

   public boolean test(LootContext arg) {
      ItemStack lv = (ItemStack)arg.get(LootContextParameters.TOOL);
      int i = lv != null ? EnchantmentHelper.getLevel(this.enchantment, lv) : 0;
      float f = this.chances[Math.min(i, this.chances.length - 1)];
      return arg.getRandom().nextFloat() < f;
   }

   public static LootCondition.Builder builder(Enchantment enchantment, float... chances) {
      return () -> {
         return new TableBonusLootCondition(enchantment, chances);
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, TableBonusLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("enchantment", Registries.ENCHANTMENT.getId(arg.enchantment).toString());
         jsonObject.add("chances", jsonSerializationContext.serialize(arg.chances));
      }

      public TableBonusLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "enchantment"));
         Enchantment lv2 = (Enchantment)Registries.ENCHANTMENT.getOrEmpty(lv).orElseThrow(() -> {
            return new JsonParseException("Invalid enchantment id: " + lv);
         });
         float[] fs = (float[])JsonHelper.deserialize(jsonObject, "chances", jsonDeserializationContext, float[].class);
         return new TableBonusLootCondition(lv2, fs);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
