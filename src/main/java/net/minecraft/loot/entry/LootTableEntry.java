package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.loot.LootDataKey;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class LootTableEntry extends LeafEntry {
   final Identifier id;

   LootTableEntry(Identifier id, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
      super(weight, quality, conditions, functions);
      this.id = id;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntryTypes.LOOT_TABLE;
   }

   public void generateLoot(Consumer lootConsumer, LootContext context) {
      LootTable lv = context.getDataLookup().getLootTable(this.id);
      lv.generateUnprocessedLoot(context, lootConsumer);
   }

   public void validate(LootTableReporter reporter) {
      LootDataKey lv = new LootDataKey(LootDataType.LOOT_TABLES, this.id);
      if (reporter.isInStack(lv)) {
         reporter.report("Table " + this.id + " is recursively called");
      } else {
         super.validate(reporter);
         reporter.getDataLookup().getElementOptional(lv).ifPresentOrElse((table) -> {
            table.validate(reporter.makeChild("->{" + this.id + "}", lv));
         }, () -> {
            reporter.report("Unknown loot table called " + this.id);
         });
      }
   }

   public static LeafEntry.Builder builder(Identifier id) {
      return builder((weight, quality, conditions, functions) -> {
         return new LootTableEntry(id, weight, quality, conditions, functions);
      });
   }

   public static class Serializer extends LeafEntry.Serializer {
      public void addEntryFields(JsonObject jsonObject, LootTableEntry arg, JsonSerializationContext jsonSerializationContext) {
         super.addEntryFields(jsonObject, (LeafEntry)arg, jsonSerializationContext);
         jsonObject.addProperty("name", arg.id.toString());
      }

      protected LootTableEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] args, LootFunction[] args2) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "name"));
         return new LootTableEntry(lv, i, j, args, args2);
      }

      // $FF: synthetic method
      protected LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
         return this.fromJson(entryJson, context, weight, quality, conditions, functions);
      }
   }
}
