package net.minecraft.recipe;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public final class Ingredient implements Predicate {
   public static final Ingredient EMPTY = new Ingredient(Stream.empty());
   private final Entry[] entries;
   @Nullable
   private ItemStack[] matchingStacks;
   @Nullable
   private IntList ids;

   private Ingredient(Stream entries) {
      this.entries = (Entry[])entries.toArray((i) -> {
         return new Entry[i];
      });
   }

   public ItemStack[] getMatchingStacks() {
      if (this.matchingStacks == null) {
         this.matchingStacks = (ItemStack[])Arrays.stream(this.entries).flatMap((entry) -> {
            return entry.getStacks().stream();
         }).distinct().toArray((i) -> {
            return new ItemStack[i];
         });
      }

      return this.matchingStacks;
   }

   public boolean test(@Nullable ItemStack arg) {
      if (arg == null) {
         return false;
      } else if (this.isEmpty()) {
         return arg.isEmpty();
      } else {
         ItemStack[] var2 = this.getMatchingStacks();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            ItemStack lv = var2[var4];
            if (lv.isOf(arg.getItem())) {
               return true;
            }
         }

         return false;
      }
   }

   public IntList getMatchingItemIds() {
      if (this.ids == null) {
         ItemStack[] lvs = this.getMatchingStacks();
         this.ids = new IntArrayList(lvs.length);
         ItemStack[] var2 = lvs;
         int var3 = lvs.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            ItemStack lv = var2[var4];
            this.ids.add(RecipeMatcher.getItemId(lv));
         }

         this.ids.sort(IntComparators.NATURAL_COMPARATOR);
      }

      return this.ids;
   }

   public void write(PacketByteBuf buf) {
      buf.writeCollection(Arrays.asList(this.getMatchingStacks()), PacketByteBuf::writeItemStack);
   }

   public JsonElement toJson() {
      if (this.entries.length == 1) {
         return this.entries[0].toJson();
      } else {
         JsonArray jsonArray = new JsonArray();
         Entry[] var2 = this.entries;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Entry lv = var2[var4];
            jsonArray.add(lv.toJson());
         }

         return jsonArray;
      }
   }

   public boolean isEmpty() {
      return this.entries.length == 0;
   }

   private static Ingredient ofEntries(Stream entries) {
      Ingredient lv = new Ingredient(entries);
      return lv.isEmpty() ? EMPTY : lv;
   }

   public static Ingredient empty() {
      return EMPTY;
   }

   public static Ingredient ofItems(ItemConvertible... items) {
      return ofStacks(Arrays.stream(items).map(ItemStack::new));
   }

   public static Ingredient ofStacks(ItemStack... stacks) {
      return ofStacks(Arrays.stream(stacks));
   }

   public static Ingredient ofStacks(Stream stacks) {
      return ofEntries(stacks.filter((stack) -> {
         return !stack.isEmpty();
      }).map(StackEntry::new));
   }

   public static Ingredient fromTag(TagKey tag) {
      return ofEntries(Stream.of(new TagEntry(tag)));
   }

   public static Ingredient fromPacket(PacketByteBuf buf) {
      return ofEntries(buf.readList(PacketByteBuf::readItemStack).stream().map(StackEntry::new));
   }

   public static Ingredient fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         if (json.isJsonObject()) {
            return ofEntries(Stream.of(entryFromJson(json.getAsJsonObject())));
         } else if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            if (jsonArray.size() == 0) {
               throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
            } else {
               return ofEntries(StreamSupport.stream(jsonArray.spliterator(), false).map((jsonElement) -> {
                  return entryFromJson(JsonHelper.asObject(jsonElement, "item"));
               }));
            }
         } else {
            throw new JsonSyntaxException("Expected item to be object or array of objects");
         }
      } else {
         throw new JsonSyntaxException("Item cannot be null");
      }
   }

   private static Entry entryFromJson(JsonObject json) {
      if (json.has("item") && json.has("tag")) {
         throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
      } else if (json.has("item")) {
         Item lv = ShapedRecipe.getItem(json);
         return new StackEntry(new ItemStack(lv));
      } else if (json.has("tag")) {
         Identifier lv2 = new Identifier(JsonHelper.getString(json, "tag"));
         TagKey lv3 = TagKey.of(RegistryKeys.ITEM, lv2);
         return new TagEntry(lv3);
      } else {
         throw new JsonParseException("An ingredient entry needs either a tag or an item");
      }
   }

   // $FF: synthetic method
   public boolean test(@Nullable Object stack) {
      return this.test((ItemStack)stack);
   }

   private interface Entry {
      Collection getStacks();

      JsonObject toJson();
   }

   private static class TagEntry implements Entry {
      private final TagKey tag;

      TagEntry(TagKey tag) {
         this.tag = tag;
      }

      public Collection getStacks() {
         List list = Lists.newArrayList();
         Iterator var2 = Registries.ITEM.iterateEntries(this.tag).iterator();

         while(var2.hasNext()) {
            RegistryEntry lv = (RegistryEntry)var2.next();
            list.add(new ItemStack(lv));
         }

         return list;
      }

      public JsonObject toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("tag", this.tag.id().toString());
         return jsonObject;
      }
   }

   static class StackEntry implements Entry {
      private final ItemStack stack;

      StackEntry(ItemStack stack) {
         this.stack = stack;
      }

      public Collection getStacks() {
         return Collections.singleton(this.stack);
      }

      public JsonObject toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("item", Registries.ITEM.getId(this.stack.getItem()).toString());
         return jsonObject;
      }
   }
}
