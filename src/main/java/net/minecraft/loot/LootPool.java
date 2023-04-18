package net.minecraft.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
   final LootPoolEntry[] entries;
   final LootCondition[] conditions;
   private final Predicate predicate;
   final LootFunction[] functions;
   private final BiFunction javaFunctions;
   final LootNumberProvider rolls;
   final LootNumberProvider bonusRolls;

   LootPool(LootPoolEntry[] entries, LootCondition[] conditions, LootFunction[] functions, LootNumberProvider rolls, LootNumberProvider bonusRolls) {
      this.entries = entries;
      this.conditions = conditions;
      this.predicate = LootConditionTypes.joinAnd(conditions);
      this.functions = functions;
      this.javaFunctions = LootFunctionTypes.join(functions);
      this.rolls = rolls;
      this.bonusRolls = bonusRolls;
   }

   private void supplyOnce(Consumer lootConsumer, LootContext context) {
      Random lv = context.getRandom();
      List list = Lists.newArrayList();
      MutableInt mutableInt = new MutableInt();
      LootPoolEntry[] var6 = this.entries;
      int j = var6.length;

      for(int var8 = 0; var8 < j; ++var8) {
         LootPoolEntry lv2 = var6[var8];
         lv2.expand(context, (choice) -> {
            int i = choice.getWeight(context.getLuck());
            if (i > 0) {
               list.add(choice);
               mutableInt.add(i);
            }

         });
      }

      int i = list.size();
      if (mutableInt.intValue() != 0 && i != 0) {
         if (i == 1) {
            ((LootChoice)list.get(0)).generateLoot(lootConsumer, context);
         } else {
            j = lv.nextInt(mutableInt.intValue());
            Iterator var11 = list.iterator();

            LootChoice lv3;
            do {
               if (!var11.hasNext()) {
                  return;
               }

               lv3 = (LootChoice)var11.next();
               j -= lv3.getWeight(context.getLuck());
            } while(j >= 0);

            lv3.generateLoot(lootConsumer, context);
         }
      }
   }

   public void addGeneratedLoot(Consumer lootConsumer, LootContext context) {
      if (this.predicate.test(context)) {
         Consumer consumer2 = LootFunction.apply(this.javaFunctions, lootConsumer, context);
         int i = this.rolls.nextInt(context) + MathHelper.floor(this.bonusRolls.nextFloat(context) * context.getLuck());

         for(int j = 0; j < i; ++j) {
            this.supplyOnce(consumer2, context);
         }

      }
   }

   public void validate(LootTableReporter reporter) {
      int i;
      for(i = 0; i < this.conditions.length; ++i) {
         this.conditions[i].validate(reporter.makeChild(".condition[" + i + "]"));
      }

      for(i = 0; i < this.functions.length; ++i) {
         this.functions[i].validate(reporter.makeChild(".functions[" + i + "]"));
      }

      for(i = 0; i < this.entries.length; ++i) {
         this.entries[i].validate(reporter.makeChild(".entries[" + i + "]"));
      }

      this.rolls.validate(reporter.makeChild(".rolls"));
      this.bonusRolls.validate(reporter.makeChild(".bonusRolls"));
   }

   public static Builder builder() {
      return new Builder();
   }

   public static class Builder implements LootFunctionConsumingBuilder, LootConditionConsumingBuilder {
      private final List entries = Lists.newArrayList();
      private final List conditions = Lists.newArrayList();
      private final List functions = Lists.newArrayList();
      private LootNumberProvider rolls = ConstantLootNumberProvider.create(1.0F);
      private LootNumberProvider bonusRollsRange = ConstantLootNumberProvider.create(0.0F);

      public Builder rolls(LootNumberProvider rolls) {
         this.rolls = rolls;
         return this;
      }

      public Builder getThisFunctionConsumingBuilder() {
         return this;
      }

      public Builder bonusRolls(LootNumberProvider bonusRolls) {
         this.bonusRollsRange = bonusRolls;
         return this;
      }

      public Builder with(LootPoolEntry.Builder entry) {
         this.entries.add(entry.build());
         return this;
      }

      public Builder conditionally(LootCondition.Builder arg) {
         this.conditions.add(arg.build());
         return this;
      }

      public Builder apply(LootFunction.Builder arg) {
         this.functions.add(arg.build());
         return this;
      }

      public LootPool build() {
         if (this.rolls == null) {
            throw new IllegalArgumentException("Rolls not set");
         } else {
            return new LootPool((LootPoolEntry[])this.entries.toArray(new LootPoolEntry[0]), (LootCondition[])this.conditions.toArray(new LootCondition[0]), (LootFunction[])this.functions.toArray(new LootFunction[0]), this.rolls, this.bonusRollsRange);
         }
      }

      // $FF: synthetic method
      public LootFunctionConsumingBuilder getThisFunctionConsumingBuilder() {
         return this.getThisFunctionConsumingBuilder();
      }

      // $FF: synthetic method
      public LootFunctionConsumingBuilder apply(LootFunction.Builder function) {
         return this.apply(function);
      }

      // $FF: synthetic method
      public LootConditionConsumingBuilder getThisConditionConsumingBuilder() {
         return this.getThisFunctionConsumingBuilder();
      }

      // $FF: synthetic method
      public LootConditionConsumingBuilder conditionally(LootCondition.Builder condition) {
         return this.conditionally(condition);
      }
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootPool deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = JsonHelper.asObject(jsonElement, "loot pool");
         LootPoolEntry[] lvs = (LootPoolEntry[])JsonHelper.deserialize(jsonObject, "entries", jsonDeserializationContext, LootPoolEntry[].class);
         LootCondition[] lvs2 = (LootCondition[])JsonHelper.deserialize(jsonObject, "conditions", new LootCondition[0], jsonDeserializationContext, LootCondition[].class);
         LootFunction[] lvs3 = (LootFunction[])JsonHelper.deserialize(jsonObject, "functions", new LootFunction[0], jsonDeserializationContext, LootFunction[].class);
         LootNumberProvider lv = (LootNumberProvider)JsonHelper.deserialize(jsonObject, "rolls", jsonDeserializationContext, LootNumberProvider.class);
         LootNumberProvider lv2 = (LootNumberProvider)JsonHelper.deserialize(jsonObject, "bonus_rolls", ConstantLootNumberProvider.create(0.0F), jsonDeserializationContext, LootNumberProvider.class);
         return new LootPool(lvs, lvs2, lvs3, lv, lv2);
      }

      public JsonElement serialize(LootPool arg, Type type, JsonSerializationContext jsonSerializationContext) {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("rolls", jsonSerializationContext.serialize(arg.rolls));
         jsonObject.add("bonus_rolls", jsonSerializationContext.serialize(arg.bonusRolls));
         jsonObject.add("entries", jsonSerializationContext.serialize(arg.entries));
         if (!ArrayUtils.isEmpty(arg.conditions)) {
            jsonObject.add("conditions", jsonSerializationContext.serialize(arg.conditions));
         }

         if (!ArrayUtils.isEmpty(arg.functions)) {
            jsonObject.add("functions", jsonSerializationContext.serialize(arg.functions));
         }

         return jsonObject;
      }

      // $FF: synthetic method
      public JsonElement serialize(Object entry, Type unused, JsonSerializationContext context) {
         return this.serialize((LootPool)entry, unused, context);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement json, Type unused, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, unused, context);
      }
   }
}
