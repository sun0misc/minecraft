package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ItemEntry extends LeafEntry {
   final Item item;

   ItemEntry(Item item, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
      super(weight, quality, conditions, functions);
      this.item = item;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntryTypes.ITEM;
   }

   public void generateLoot(Consumer lootConsumer, LootContext context) {
      lootConsumer.accept(new ItemStack(this.item));
   }

   public static LeafEntry.Builder builder(ItemConvertible drop) {
      return builder((weight, quality, conditions, functions) -> {
         return new ItemEntry(drop.asItem(), weight, quality, conditions, functions);
      });
   }

   public static class Serializer extends LeafEntry.Serializer {
      public void addEntryFields(JsonObject jsonObject, ItemEntry arg, JsonSerializationContext jsonSerializationContext) {
         super.addEntryFields(jsonObject, (LeafEntry)arg, jsonSerializationContext);
         Identifier lv = Registries.ITEM.getId(arg.item);
         if (lv == null) {
            throw new IllegalArgumentException("Can't serialize unknown item " + arg.item);
         } else {
            jsonObject.addProperty("name", lv.toString());
         }
      }

      protected ItemEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] args, LootFunction[] args2) {
         Item lv = JsonHelper.getItem(jsonObject, "name");
         return new ItemEntry(lv, i, j, args, args2);
      }

      // $FF: synthetic method
      protected LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
         return this.fromJson(entryJson, context, weight, quality, conditions, functions);
      }
   }
}
