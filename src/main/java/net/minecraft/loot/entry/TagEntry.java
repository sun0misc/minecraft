package net.minecraft.loot.entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Iterator;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class TagEntry extends LeafEntry {
   final TagKey name;
   final boolean expand;

   TagEntry(TagKey name, boolean expand, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
      super(weight, quality, conditions, functions);
      this.name = name;
      this.expand = expand;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntryTypes.TAG;
   }

   public void generateLoot(Consumer lootConsumer, LootContext context) {
      Registries.ITEM.iterateEntries(this.name).forEach((entry) -> {
         lootConsumer.accept(new ItemStack(entry));
      });
   }

   private boolean grow(LootContext context, Consumer lootChoiceExpander) {
      if (!this.test(context)) {
         return false;
      } else {
         Iterator var3 = Registries.ITEM.iterateEntries(this.name).iterator();

         while(var3.hasNext()) {
            final RegistryEntry lv = (RegistryEntry)var3.next();
            lootChoiceExpander.accept(new LeafEntry.Choice() {
               public void generateLoot(Consumer lootConsumer, LootContext context) {
                  lootConsumer.accept(new ItemStack(lv));
               }
            });
         }

         return true;
      }
   }

   public boolean expand(LootContext arg, Consumer consumer) {
      return this.expand ? this.grow(arg, consumer) : super.expand(arg, consumer);
   }

   public static LeafEntry.Builder builder(TagKey name) {
      return builder((weight, quality, conditions, functions) -> {
         return new TagEntry(name, false, weight, quality, conditions, functions);
      });
   }

   public static LeafEntry.Builder expandBuilder(TagKey name) {
      return builder((weight, quality, conditions, functions) -> {
         return new TagEntry(name, true, weight, quality, conditions, functions);
      });
   }

   public static class Serializer extends LeafEntry.Serializer {
      public void addEntryFields(JsonObject jsonObject, TagEntry arg, JsonSerializationContext jsonSerializationContext) {
         super.addEntryFields(jsonObject, (LeafEntry)arg, jsonSerializationContext);
         jsonObject.addProperty("name", arg.name.id().toString());
         jsonObject.addProperty("expand", arg.expand);
      }

      protected TagEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] args, LootFunction[] args2) {
         Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "name"));
         TagKey lv2 = TagKey.of(RegistryKeys.ITEM, lv);
         boolean bl = JsonHelper.getBoolean(jsonObject, "expand");
         return new TagEntry(lv2, bl, i, j, args, args2);
      }

      // $FF: synthetic method
      protected LeafEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
         return this.fromJson(entryJson, context, weight, quality, conditions, functions);
      }
   }
}
