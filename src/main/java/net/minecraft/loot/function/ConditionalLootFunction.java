package net.minecraft.loot.function;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.apache.commons.lang3.ArrayUtils;

public abstract class ConditionalLootFunction implements LootFunction {
   protected final LootCondition[] conditions;
   private final Predicate predicate;

   protected ConditionalLootFunction(LootCondition[] conditions) {
      this.conditions = conditions;
      this.predicate = LootConditionTypes.joinAnd(conditions);
   }

   public final ItemStack apply(ItemStack arg, LootContext arg2) {
      return this.predicate.test(arg2) ? this.process(arg, arg2) : arg;
   }

   protected abstract ItemStack process(ItemStack stack, LootContext context);

   public void validate(LootTableReporter reporter) {
      LootFunction.super.validate(reporter);

      for(int i = 0; i < this.conditions.length; ++i) {
         this.conditions[i].validate(reporter.makeChild(".conditions[" + i + "]"));
      }

   }

   protected static Builder builder(Function joiner) {
      return new Joiner(joiner);
   }

   // $FF: synthetic method
   public Object apply(Object itemStack, Object context) {
      return this.apply((ItemStack)itemStack, (LootContext)context);
   }

   private static final class Joiner extends Builder {
      private final Function joiner;

      public Joiner(Function joiner) {
         this.joiner = joiner;
      }

      protected Joiner getThisBuilder() {
         return this;
      }

      public LootFunction build() {
         return (LootFunction)this.joiner.apply(this.getConditions());
      }

      // $FF: synthetic method
      protected Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public abstract static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, ConditionalLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         if (!ArrayUtils.isEmpty(arg.conditions)) {
            jsonObject.add("conditions", jsonSerializationContext.serialize(arg.conditions));
         }

      }

      public final ConditionalLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         LootCondition[] lvs = (LootCondition[])JsonHelper.deserialize(jsonObject, "conditions", new LootCondition[0], jsonDeserializationContext, LootCondition[].class);
         return this.fromJson(jsonObject, jsonDeserializationContext, lvs);
      }

      public abstract ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions);

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }

   public abstract static class Builder implements LootFunction.Builder, LootConditionConsumingBuilder {
      private final List conditionList = Lists.newArrayList();

      public Builder conditionally(LootCondition.Builder arg) {
         this.conditionList.add(arg.build());
         return this.getThisBuilder();
      }

      public final Builder getThisConditionConsumingBuilder() {
         return this.getThisBuilder();
      }

      protected abstract Builder getThisBuilder();

      protected LootCondition[] getConditions() {
         return (LootCondition[])this.conditionList.toArray(new LootCondition[0]);
      }

      // $FF: synthetic method
      public LootConditionConsumingBuilder getThisConditionConsumingBuilder() {
         return this.getThisConditionConsumingBuilder();
      }

      // $FF: synthetic method
      public LootConditionConsumingBuilder conditionally(LootCondition.Builder condition) {
         return this.conditionally(condition);
      }
   }
}
