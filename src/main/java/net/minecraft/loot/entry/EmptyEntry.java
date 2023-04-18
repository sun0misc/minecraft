package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;

public class EmptyEntry extends LeafEntry {
   EmptyEntry(int i, int j, LootCondition[] args, LootFunction[] args2) {
      super(i, j, args, args2);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntryTypes.EMPTY;
   }

   public void generateLoot(Consumer lootConsumer, LootContext context) {
   }

   public static LeafEntry.Builder builder() {
      return builder(EmptyEntry::new);
   }

   public static class Serializer extends LeafEntry.Serializer {
      public EmptyEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] args, LootFunction[] args2) {
         return new EmptyEntry(i, j, args, args2);
      }

      // $FF: synthetic method
      public LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
         return this.fromJson(entryJson, context, weight, quality, conditions, functions);
      }
   }
}
