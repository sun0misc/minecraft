package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;

public class ApplyBonusLootFunction extends ConditionalLootFunction {
   static final Map FACTORIES = Maps.newHashMap();
   final Enchantment enchantment;
   final Formula formula;

   ApplyBonusLootFunction(LootCondition[] conditions, Enchantment enchantment, Formula formula) {
      super(conditions);
      this.enchantment = enchantment;
      this.formula = formula;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.APPLY_BONUS;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.TOOL);
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      ItemStack lv = (ItemStack)context.get(LootContextParameters.TOOL);
      if (lv != null) {
         int i = EnchantmentHelper.getLevel(this.enchantment, lv);
         int j = this.formula.getValue(context.getRandom(), stack.getCount(), i);
         stack.setCount(j);
      }

      return stack;
   }

   public static ConditionalLootFunction.Builder binomialWithBonusCount(Enchantment enchantment, float probability, int extra) {
      return builder((conditions) -> {
         return new ApplyBonusLootFunction(conditions, enchantment, new BinomialWithBonusCount(extra, probability));
      });
   }

   public static ConditionalLootFunction.Builder oreDrops(Enchantment enchantment) {
      return builder((conditions) -> {
         return new ApplyBonusLootFunction(conditions, enchantment, new OreDrops());
      });
   }

   public static ConditionalLootFunction.Builder uniformBonusCount(Enchantment enchantment) {
      return builder((conditions) -> {
         return new ApplyBonusLootFunction(conditions, enchantment, new UniformBonusCount(1));
      });
   }

   public static ConditionalLootFunction.Builder uniformBonusCount(Enchantment enchantment, int bonusMultiplier) {
      return builder((conditions) -> {
         return new ApplyBonusLootFunction(conditions, enchantment, new UniformBonusCount(bonusMultiplier));
      });
   }

   static {
      FACTORIES.put(ApplyBonusLootFunction.BinomialWithBonusCount.ID, BinomialWithBonusCount::fromJson);
      FACTORIES.put(ApplyBonusLootFunction.OreDrops.ID, OreDrops::fromJson);
      FACTORIES.put(ApplyBonusLootFunction.UniformBonusCount.ID, UniformBonusCount::fromJson);
   }

   private interface Formula {
      int getValue(Random random, int initialCount, int enchantmentLevel);

      void toJson(JsonObject json, JsonSerializationContext context);

      Identifier getId();
   }

   private static final class UniformBonusCount implements Formula {
      public static final Identifier ID = new Identifier("uniform_bonus_count");
      private final int bonusMultiplier;

      public UniformBonusCount(int bonusMultiplier) {
         this.bonusMultiplier = bonusMultiplier;
      }

      public int getValue(Random random, int initialCount, int enchantmentLevel) {
         return initialCount + random.nextInt(this.bonusMultiplier * enchantmentLevel + 1);
      }

      public void toJson(JsonObject json, JsonSerializationContext context) {
         json.addProperty("bonusMultiplier", this.bonusMultiplier);
      }

      public static Formula fromJson(JsonObject json, JsonDeserializationContext context) {
         int i = JsonHelper.getInt(json, "bonusMultiplier");
         return new UniformBonusCount(i);
      }

      public Identifier getId() {
         return ID;
      }
   }

   private static final class OreDrops implements Formula {
      public static final Identifier ID = new Identifier("ore_drops");

      OreDrops() {
      }

      public int getValue(Random random, int initialCount, int enchantmentLevel) {
         if (enchantmentLevel > 0) {
            int k = random.nextInt(enchantmentLevel + 2) - 1;
            if (k < 0) {
               k = 0;
            }

            return initialCount * (k + 1);
         } else {
            return initialCount;
         }
      }

      public void toJson(JsonObject json, JsonSerializationContext context) {
      }

      public static Formula fromJson(JsonObject json, JsonDeserializationContext context) {
         return new OreDrops();
      }

      public Identifier getId() {
         return ID;
      }
   }

   private static final class BinomialWithBonusCount implements Formula {
      public static final Identifier ID = new Identifier("binomial_with_bonus_count");
      private final int extra;
      private final float probability;

      public BinomialWithBonusCount(int extra, float probability) {
         this.extra = extra;
         this.probability = probability;
      }

      public int getValue(Random random, int initialCount, int enchantmentLevel) {
         for(int k = 0; k < enchantmentLevel + this.extra; ++k) {
            if (random.nextFloat() < this.probability) {
               ++initialCount;
            }
         }

         return initialCount;
      }

      public void toJson(JsonObject json, JsonSerializationContext context) {
         json.addProperty("extra", this.extra);
         json.addProperty("probability", this.probability);
      }

      public static Formula fromJson(JsonObject json, JsonDeserializationContext context) {
         int i = JsonHelper.getInt(json, "extra");
         float f = JsonHelper.getFloat(json, "probability");
         return new BinomialWithBonusCount(i, f);
      }

      public Identifier getId() {
         return ID;
      }
   }

   private interface FormulaFactory {
      Formula deserialize(JsonObject functionJson, JsonDeserializationContext context);
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, ApplyBonusLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.addProperty("enchantment", Registries.ENCHANTMENT.getId(arg.enchantment).toString());
         jsonObject.addProperty("formula", arg.formula.getId().toString());
         JsonObject jsonObject2 = new JsonObject();
         arg.formula.toJson(jsonObject2, jsonSerializationContext);
         if (jsonObject2.size() > 0) {
            jsonObject.add("parameters", jsonObject2);
         }

      }

      public ApplyBonusLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "enchantment"));
         Enchantment lv2 = (Enchantment)Registries.ENCHANTMENT.getOrEmpty(lv).orElseThrow(() -> {
            return new JsonParseException("Invalid enchantment id: " + lv);
         });
         Identifier lv3 = new Identifier(JsonHelper.getString(jsonObject, "formula"));
         FormulaFactory lv4 = (FormulaFactory)ApplyBonusLootFunction.FACTORIES.get(lv3);
         if (lv4 == null) {
            throw new JsonParseException("Invalid formula id: " + lv3);
         } else {
            Formula lv5;
            if (jsonObject.has("parameters")) {
               lv5 = lv4.deserialize(JsonHelper.getObject(jsonObject, "parameters"), jsonDeserializationContext);
            } else {
               lv5 = lv4.deserialize(new JsonObject(), jsonDeserializationContext);
            }

            return new ApplyBonusLootFunction(args, lv2, lv5);
         }
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
