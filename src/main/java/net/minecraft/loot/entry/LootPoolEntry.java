package net.minecraft.loot.entry;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootPoolEntry implements EntryCombiner {
   protected final LootCondition[] conditions;
   private final Predicate conditionPredicate;

   protected LootPoolEntry(LootCondition[] conditions) {
      this.conditions = conditions;
      this.conditionPredicate = LootConditionTypes.joinAnd(conditions);
   }

   public void validate(LootTableReporter reporter) {
      for(int i = 0; i < this.conditions.length; ++i) {
         this.conditions[i].validate(reporter.makeChild(".condition[" + i + "]"));
      }

   }

   protected final boolean test(LootContext context) {
      return this.conditionPredicate.test(context);
   }

   public abstract LootPoolEntryType getType();

   public abstract static class Serializer implements JsonSerializer {
      public final void toJson(JsonObject jsonObject, LootPoolEntry arg, JsonSerializationContext jsonSerializationContext) {
         if (!ArrayUtils.isEmpty(arg.conditions)) {
            jsonObject.add("conditions", jsonSerializationContext.serialize(arg.conditions));
         }

         this.addEntryFields(jsonObject, arg, jsonSerializationContext);
      }

      public final LootPoolEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         LootCondition[] lvs = (LootCondition[])JsonHelper.deserialize(jsonObject, "conditions", new LootCondition[0], jsonDeserializationContext, LootCondition[].class);
         return this.fromJson(jsonObject, jsonDeserializationContext, lvs);
      }

      public abstract void addEntryFields(JsonObject json, LootPoolEntry entry, JsonSerializationContext context);

      public abstract LootPoolEntry fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions);

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }

      // $FF: synthetic method
      public void toJson(JsonObject json, Object object, JsonSerializationContext context) {
         this.toJson(json, (LootPoolEntry)object, context);
      }
   }

   public abstract static class Builder implements LootConditionConsumingBuilder {
      private final List conditions = Lists.newArrayList();

      protected abstract Builder getThisBuilder();

      public Builder conditionally(LootCondition.Builder arg) {
         this.conditions.add(arg.build());
         return this.getThisBuilder();
      }

      public final Builder getThisConditionConsumingBuilder() {
         return this.getThisBuilder();
      }

      protected LootCondition[] getConditions() {
         return (LootCondition[])this.conditions.toArray(new LootCondition[0]);
      }

      public AlternativeEntry.Builder alternatively(Builder builder) {
         return new AlternativeEntry.Builder(new Builder[]{this, builder});
      }

      public GroupEntry.Builder sequenceEntry(Builder entry) {
         return new GroupEntry.Builder(new Builder[]{this, entry});
      }

      public SequenceEntry.Builder groupEntry(Builder entry) {
         return new SequenceEntry.Builder(new Builder[]{this, entry});
      }

      public abstract LootPoolEntry build();

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
