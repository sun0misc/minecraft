package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class DynamicEntry extends LeafEntry {
   final Identifier name;

   DynamicEntry(Identifier name, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
      super(weight, quality, conditions, functions);
      this.name = name;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntryTypes.DYNAMIC;
   }

   public void generateLoot(Consumer lootConsumer, LootContext context) {
      context.drop(this.name, lootConsumer);
   }

   public static LeafEntry.Builder builder(Identifier name) {
      return builder((weight, quality, conditions, functions) -> {
         return new DynamicEntry(name, weight, quality, conditions, functions);
      });
   }

   public static class Serializer extends LeafEntry.Serializer {
      public void addEntryFields(JsonObject jsonObject, DynamicEntry arg, JsonSerializationContext jsonSerializationContext) {
         super.addEntryFields(jsonObject, (LeafEntry)arg, jsonSerializationContext);
         jsonObject.addProperty("name", arg.name.toString());
      }

      protected DynamicEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] args, LootFunction[] args2) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "name"));
         return new DynamicEntry(lv, i, j, args, args2);
      }

      // $FF: synthetic method
      protected LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
         return this.fromJson(entryJson, context, weight, quality, conditions, functions);
      }
   }
}
