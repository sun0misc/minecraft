package net.minecraft.loot.entry;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LeafEntry extends LootPoolEntry {
   public static final int field_31847 = 1;
   public static final int field_31848 = 0;
   protected final int weight;
   protected final int quality;
   protected final LootFunction[] functions;
   final BiFunction compiledFunctions;
   private final LootChoice choice = new Choice() {
      public void generateLoot(Consumer lootConsumer, LootContext context) {
         LeafEntry.this.generateLoot(LootFunction.apply(LeafEntry.this.compiledFunctions, lootConsumer, context), context);
      }
   };

   protected LeafEntry(int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
      super(conditions);
      this.weight = weight;
      this.quality = quality;
      this.functions = functions;
      this.compiledFunctions = LootFunctionTypes.join(functions);
   }

   public void validate(LootTableReporter reporter) {
      super.validate(reporter);

      for(int i = 0; i < this.functions.length; ++i) {
         this.functions[i].validate(reporter.makeChild(".functions[" + i + "]"));
      }

   }

   protected abstract void generateLoot(Consumer lootConsumer, LootContext context);

   public boolean expand(LootContext arg, Consumer consumer) {
      if (this.test(arg)) {
         consumer.accept(this.choice);
         return true;
      } else {
         return false;
      }
   }

   public static Builder builder(Factory factory) {
      return new BasicBuilder(factory);
   }

   private static class BasicBuilder extends Builder {
      private final Factory factory;

      public BasicBuilder(Factory factory) {
         this.factory = factory;
      }

      protected BasicBuilder getThisBuilder() {
         return this;
      }

      public LootPoolEntry build() {
         return this.factory.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
      }

      // $FF: synthetic method
      protected LootPoolEntry.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   @FunctionalInterface
   protected interface Factory {
      LeafEntry build(int weight, int quality, LootCondition[] conditions, LootFunction[] functions);
   }

   public abstract static class Serializer extends LootPoolEntry.Serializer {
      public void addEntryFields(JsonObject jsonObject, LeafEntry arg, JsonSerializationContext jsonSerializationContext) {
         if (arg.weight != 1) {
            jsonObject.addProperty("weight", arg.weight);
         }

         if (arg.quality != 0) {
            jsonObject.addProperty("quality", arg.quality);
         }

         if (!ArrayUtils.isEmpty(arg.functions)) {
            jsonObject.add("functions", jsonSerializationContext.serialize(arg.functions));
         }

      }

      public final LeafEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         int i = JsonHelper.getInt(jsonObject, "weight", 1);
         int j = JsonHelper.getInt(jsonObject, "quality", 0);
         LootFunction[] lvs = (LootFunction[])JsonHelper.deserialize(jsonObject, "functions", new LootFunction[0], jsonDeserializationContext, LootFunction[].class);
         return this.fromJson(jsonObject, jsonDeserializationContext, i, j, args, lvs);
      }

      protected abstract LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions);

      // $FF: synthetic method
      public LootPoolEntry fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }

   public abstract static class Builder extends LootPoolEntry.Builder implements LootFunctionConsumingBuilder {
      protected int weight = 1;
      protected int quality = 0;
      private final List functions = Lists.newArrayList();

      public Builder apply(LootFunction.Builder arg) {
         this.functions.add(arg.build());
         return (Builder)this.getThisBuilder();
      }

      protected LootFunction[] getFunctions() {
         return (LootFunction[])this.functions.toArray(new LootFunction[0]);
      }

      public Builder weight(int weight) {
         this.weight = weight;
         return (Builder)this.getThisBuilder();
      }

      public Builder quality(int quality) {
         this.quality = quality;
         return (Builder)this.getThisBuilder();
      }

      // $FF: synthetic method
      public LootFunctionConsumingBuilder getThisFunctionConsumingBuilder() {
         return (LootFunctionConsumingBuilder)super.getThisConditionConsumingBuilder();
      }

      // $FF: synthetic method
      public LootFunctionConsumingBuilder apply(LootFunction.Builder function) {
         return this.apply(function);
      }
   }

   protected abstract class Choice implements LootChoice {
      public int getWeight(float luck) {
         return Math.max(MathHelper.floor((float)LeafEntry.this.weight + (float)LeafEntry.this.quality * luck), 0);
      }
   }
}
