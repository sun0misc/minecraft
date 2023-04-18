package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public class SetContentsLootFunction extends ConditionalLootFunction {
   final List entries;
   final BlockEntityType type;

   SetContentsLootFunction(LootCondition[] conditions, BlockEntityType type, List entries) {
      super(conditions);
      this.type = type;
      this.entries = ImmutableList.copyOf(entries);
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_CONTENTS;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      if (stack.isEmpty()) {
         return stack;
      } else {
         DefaultedList lv = DefaultedList.of();
         this.entries.forEach((entry) -> {
            entry.expand(context, (choice) -> {
               Objects.requireNonNull(lv);
               choice.generateLoot(LootTable.processStacks(context, lv::add), context);
            });
         });
         NbtCompound lv2 = new NbtCompound();
         Inventories.writeNbt(lv2, lv);
         NbtCompound lv3 = BlockItem.getBlockEntityNbt(stack);
         if (lv3 == null) {
            lv3 = lv2;
         } else {
            lv3.copyFrom(lv2);
         }

         BlockItem.setBlockEntityNbt(stack, this.type, lv3);
         return stack;
      }
   }

   public void validate(LootTableReporter reporter) {
      super.validate(reporter);

      for(int i = 0; i < this.entries.size(); ++i) {
         ((LootPoolEntry)this.entries.get(i)).validate(reporter.makeChild(".entry[" + i + "]"));
      }

   }

   public static Builder builder(BlockEntityType type) {
      return new Builder(type);
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final List entries = Lists.newArrayList();
      private final BlockEntityType type;

      public Builder(BlockEntityType type) {
         this.type = type;
      }

      protected Builder getThisBuilder() {
         return this;
      }

      public Builder withEntry(LootPoolEntry.Builder entryBuilder) {
         this.entries.add(entryBuilder.build());
         return this;
      }

      public LootFunction build() {
         return new SetContentsLootFunction(this.getConditions(), this.type, this.entries);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetContentsLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.addProperty("type", Registries.BLOCK_ENTITY_TYPE.getId(arg.type).toString());
         jsonObject.add("entries", jsonSerializationContext.serialize(arg.entries));
      }

      public SetContentsLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         LootPoolEntry[] lvs = (LootPoolEntry[])JsonHelper.deserialize(jsonObject, "entries", jsonDeserializationContext, LootPoolEntry[].class);
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "type"));
         BlockEntityType lv2 = (BlockEntityType)Registries.BLOCK_ENTITY_TYPE.getOrEmpty(lv).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block entity type id '" + lv + "'");
         });
         return new SetContentsLootFunction(args, lv2, Arrays.asList(lvs));
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
