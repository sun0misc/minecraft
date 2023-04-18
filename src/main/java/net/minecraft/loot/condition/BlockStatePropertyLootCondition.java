package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class BlockStatePropertyLootCondition implements LootCondition {
   final Block block;
   final StatePredicate properties;

   BlockStatePropertyLootCondition(Block block, StatePredicate properties) {
      this.block = block;
      this.properties = properties;
   }

   public LootConditionType getType() {
      return LootConditionTypes.BLOCK_STATE_PROPERTY;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.BLOCK_STATE);
   }

   public boolean test(LootContext arg) {
      BlockState lv = (BlockState)arg.get(LootContextParameters.BLOCK_STATE);
      return lv != null && lv.isOf(this.block) && this.properties.test(lv);
   }

   public static Builder builder(Block block) {
      return new Builder(block);
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Builder implements LootCondition.Builder {
      private final Block block;
      private StatePredicate propertyValues;

      public Builder(Block block) {
         this.propertyValues = StatePredicate.ANY;
         this.block = block;
      }

      public Builder properties(StatePredicate.Builder builder) {
         this.propertyValues = builder.build();
         return this;
      }

      public LootCondition build() {
         return new BlockStatePropertyLootCondition(this.block, this.propertyValues);
      }
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, BlockStatePropertyLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("block", Registries.BLOCK.getId(arg.block).toString());
         jsonObject.add("properties", arg.properties.toJson());
      }

      public BlockStatePropertyLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "block"));
         Block lv2 = (Block)Registries.BLOCK.getOrEmpty(lv).orElseThrow(() -> {
            return new IllegalArgumentException("Can't find block " + lv);
         });
         StatePredicate lv3 = StatePredicate.fromJson(jsonObject.get("properties"));
         lv3.check(lv2.getStateManager(), (propertyName) -> {
            throw new JsonSyntaxException("Block " + lv2 + " has no property " + propertyName);
         });
         return new BlockStatePropertyLootCondition(lv2, lv3);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
