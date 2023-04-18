package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CopyStateFunction extends ConditionalLootFunction {
   final Block block;
   final Set properties;

   CopyStateFunction(LootCondition[] conditions, Block block, Set properties) {
      super(conditions);
      this.block = block;
      this.properties = properties;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.COPY_STATE;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.BLOCK_STATE);
   }

   protected ItemStack process(ItemStack stack, LootContext context) {
      BlockState lv = (BlockState)context.get(LootContextParameters.BLOCK_STATE);
      if (lv != null) {
         NbtCompound lv2 = stack.getOrCreateNbt();
         NbtCompound lv3;
         if (lv2.contains("BlockStateTag", NbtElement.COMPOUND_TYPE)) {
            lv3 = lv2.getCompound("BlockStateTag");
         } else {
            lv3 = new NbtCompound();
            lv2.put("BlockStateTag", lv3);
         }

         Stream var10000 = this.properties.stream();
         Objects.requireNonNull(lv);
         var10000.filter(lv::contains).forEach((property) -> {
            lv3.putString(property.getName(), getPropertyName(lv, property));
         });
      }

      return stack;
   }

   public static Builder builder(Block block) {
      return new Builder(block);
   }

   private static String getPropertyName(BlockState state, Property property) {
      Comparable comparable = state.get(property);
      return property.name(comparable);
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final Block block;
      private final Set properties = Sets.newHashSet();

      Builder(Block block) {
         this.block = block;
      }

      public Builder addProperty(Property property) {
         if (!this.block.getStateManager().getProperties().contains(property)) {
            throw new IllegalStateException("Property " + property + " is not present on block " + this.block);
         } else {
            this.properties.add(property);
            return this;
         }
      }

      protected Builder getThisBuilder() {
         return this;
      }

      public LootFunction build() {
         return new CopyStateFunction(this.getConditions(), this.block, this.properties);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, CopyStateFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.addProperty("block", Registries.BLOCK.getId(arg.block).toString());
         JsonArray jsonArray = new JsonArray();
         arg.properties.forEach((property) -> {
            jsonArray.add(property.getName());
         });
         jsonObject.add("properties", jsonArray);
      }

      public CopyStateFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "block"));
         Block lv2 = (Block)Registries.BLOCK.getOrEmpty(lv).orElseThrow(() -> {
            return new IllegalArgumentException("Can't find block " + lv);
         });
         StateManager lv3 = lv2.getStateManager();
         Set set = Sets.newHashSet();
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "properties", (JsonArray)null);
         if (jsonArray != null) {
            jsonArray.forEach((property) -> {
               set.add(lv3.getProperty(JsonHelper.asString(property, "property")));
            });
         }

         return new CopyStateFunction(args, lv2, set);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
