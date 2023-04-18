package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataKey;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetLootTableLootFunction extends ConditionalLootFunction {
   final Identifier id;
   final long seed;
   final BlockEntityType type;

   SetLootTableLootFunction(LootCondition[] conditions, Identifier id, long seed, BlockEntityType type) {
      super(conditions);
      this.id = id;
      this.seed = seed;
      this.type = type;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_LOOT_TABLE;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      if (stack.isEmpty()) {
         return stack;
      } else {
         NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
         if (lv == null) {
            lv = new NbtCompound();
         }

         lv.putString("LootTable", this.id.toString());
         if (this.seed != 0L) {
            lv.putLong("LootTableSeed", this.seed);
         }

         BlockItem.setBlockEntityNbt(stack, this.type, lv);
         return stack;
      }
   }

   public void validate(LootTableReporter reporter) {
      super.validate(reporter);
      LootDataKey lv = new LootDataKey(LootDataType.LOOT_TABLES, this.id);
      if (reporter.getDataLookup().getElementOptional(lv).isEmpty()) {
         reporter.report("Missing loot table used for container: " + this.id);
      }

   }

   public static ConditionalLootFunction.Builder builder(BlockEntityType type, Identifier id) {
      return builder((conditions) -> {
         return new SetLootTableLootFunction(conditions, id, 0L, type);
      });
   }

   public static ConditionalLootFunction.Builder builder(BlockEntityType type, Identifier id, long seed) {
      return builder((conditions) -> {
         return new SetLootTableLootFunction(conditions, id, seed, type);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetLootTableLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.addProperty("name", arg.id.toString());
         jsonObject.addProperty("type", Registries.BLOCK_ENTITY_TYPE.getId(arg.type).toString());
         if (arg.seed != 0L) {
            jsonObject.addProperty("seed", arg.seed);
         }

      }

      public SetLootTableLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "name"));
         long l = JsonHelper.getLong(jsonObject, "seed", 0L);
         Identifier lv2 = new Identifier(JsonHelper.getString(jsonObject, "type"));
         BlockEntityType lv3 = (BlockEntityType)Registries.BLOCK_ENTITY_TYPE.getOrEmpty(lv2).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block entity type id '" + lv2 + "'");
         });
         return new SetLootTableLootFunction(args, lv, l, lv3);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
