package net.minecraft.predicate.item;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class ItemPredicate {
   public static final ItemPredicate ANY = new ItemPredicate();
   @Nullable
   private final TagKey tag;
   @Nullable
   private final Set items;
   private final NumberRange.IntRange count;
   private final NumberRange.IntRange durability;
   private final EnchantmentPredicate[] enchantments;
   private final EnchantmentPredicate[] storedEnchantments;
   @Nullable
   private final Potion potion;
   private final NbtPredicate nbt;

   public ItemPredicate() {
      this.tag = null;
      this.items = null;
      this.potion = null;
      this.count = NumberRange.IntRange.ANY;
      this.durability = NumberRange.IntRange.ANY;
      this.enchantments = EnchantmentPredicate.ARRAY_OF_ANY;
      this.storedEnchantments = EnchantmentPredicate.ARRAY_OF_ANY;
      this.nbt = NbtPredicate.ANY;
   }

   public ItemPredicate(@Nullable TagKey tag, @Nullable Set items, NumberRange.IntRange count, NumberRange.IntRange durability, EnchantmentPredicate[] enchantments, EnchantmentPredicate[] storedEnchantments, @Nullable Potion potion, NbtPredicate nbt) {
      this.tag = tag;
      this.items = items;
      this.count = count;
      this.durability = durability;
      this.enchantments = enchantments;
      this.storedEnchantments = storedEnchantments;
      this.potion = potion;
      this.nbt = nbt;
   }

   public boolean test(ItemStack stack) {
      if (this == ANY) {
         return true;
      } else if (this.tag != null && !stack.isIn(this.tag)) {
         return false;
      } else if (this.items != null && !this.items.contains(stack.getItem())) {
         return false;
      } else if (!this.count.test(stack.getCount())) {
         return false;
      } else if (!this.durability.isDummy() && !stack.isDamageable()) {
         return false;
      } else if (!this.durability.test(stack.getMaxDamage() - stack.getDamage())) {
         return false;
      } else if (!this.nbt.test(stack)) {
         return false;
      } else {
         Map map;
         EnchantmentPredicate[] var3;
         int var4;
         int var5;
         EnchantmentPredicate lv;
         if (this.enchantments.length > 0) {
            map = EnchantmentHelper.fromNbt(stack.getEnchantments());
            var3 = this.enchantments;
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               lv = var3[var5];
               if (!lv.test(map)) {
                  return false;
               }
            }
         }

         if (this.storedEnchantments.length > 0) {
            map = EnchantmentHelper.fromNbt(EnchantedBookItem.getEnchantmentNbt(stack));
            var3 = this.storedEnchantments;
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               lv = var3[var5];
               if (!lv.test(map)) {
                  return false;
               }
            }
         }

         Potion lv2 = PotionUtil.getPotion(stack);
         return this.potion == null || this.potion == lv2;
      }
   }

   public static ItemPredicate fromJson(@Nullable JsonElement el) {
      if (el != null && !el.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(el, "item");
         NumberRange.IntRange lv = NumberRange.IntRange.fromJson(jsonObject.get("count"));
         NumberRange.IntRange lv2 = NumberRange.IntRange.fromJson(jsonObject.get("durability"));
         if (jsonObject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
         } else {
            NbtPredicate lv3 = NbtPredicate.fromJson(jsonObject.get("nbt"));
            Set set = null;
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "items", (JsonArray)null);
            if (jsonArray != null) {
               ImmutableSet.Builder builder = ImmutableSet.builder();
               Iterator var8 = jsonArray.iterator();

               while(var8.hasNext()) {
                  JsonElement jsonElement2 = (JsonElement)var8.next();
                  Identifier lv4 = new Identifier(JsonHelper.asString(jsonElement2, "item"));
                  builder.add((Item)Registries.ITEM.getOrEmpty(lv4).orElseThrow(() -> {
                     return new JsonSyntaxException("Unknown item id '" + lv4 + "'");
                  }));
               }

               set = builder.build();
            }

            TagKey lv5 = null;
            if (jsonObject.has("tag")) {
               Identifier lv6 = new Identifier(JsonHelper.getString(jsonObject, "tag"));
               lv5 = TagKey.of(RegistryKeys.ITEM, lv6);
            }

            Potion lv7 = null;
            if (jsonObject.has("potion")) {
               Identifier lv8 = new Identifier(JsonHelper.getString(jsonObject, "potion"));
               lv7 = (Potion)Registries.POTION.getOrEmpty(lv8).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown potion '" + lv8 + "'");
               });
            }

            EnchantmentPredicate[] lvs = EnchantmentPredicate.deserializeAll(jsonObject.get("enchantments"));
            EnchantmentPredicate[] lvs2 = EnchantmentPredicate.deserializeAll(jsonObject.get("stored_enchantments"));
            return new ItemPredicate(lv5, set, lv, lv2, lvs, lvs2, lv7, lv3);
         }
      } else {
         return ANY;
      }
   }

   public JsonElement toJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         JsonArray jsonArray;
         if (this.items != null) {
            jsonArray = new JsonArray();
            Iterator var3 = this.items.iterator();

            while(var3.hasNext()) {
               Item lv = (Item)var3.next();
               jsonArray.add(Registries.ITEM.getId(lv).toString());
            }

            jsonObject.add("items", jsonArray);
         }

         if (this.tag != null) {
            jsonObject.addProperty("tag", this.tag.id().toString());
         }

         jsonObject.add("count", this.count.toJson());
         jsonObject.add("durability", this.durability.toJson());
         jsonObject.add("nbt", this.nbt.toJson());
         int var5;
         EnchantmentPredicate lv2;
         EnchantmentPredicate[] var7;
         int var8;
         if (this.enchantments.length > 0) {
            jsonArray = new JsonArray();
            var7 = this.enchantments;
            var8 = var7.length;

            for(var5 = 0; var5 < var8; ++var5) {
               lv2 = var7[var5];
               jsonArray.add(lv2.serialize());
            }

            jsonObject.add("enchantments", jsonArray);
         }

         if (this.storedEnchantments.length > 0) {
            jsonArray = new JsonArray();
            var7 = this.storedEnchantments;
            var8 = var7.length;

            for(var5 = 0; var5 < var8; ++var5) {
               lv2 = var7[var5];
               jsonArray.add(lv2.serialize());
            }

            jsonObject.add("stored_enchantments", jsonArray);
         }

         if (this.potion != null) {
            jsonObject.addProperty("potion", Registries.POTION.getId(this.potion).toString());
         }

         return jsonObject;
      }
   }

   public static ItemPredicate[] deserializeAll(@Nullable JsonElement el) {
      if (el != null && !el.isJsonNull()) {
         JsonArray jsonArray = JsonHelper.asArray(el, "items");
         ItemPredicate[] lvs = new ItemPredicate[jsonArray.size()];

         for(int i = 0; i < lvs.length; ++i) {
            lvs[i] = fromJson(jsonArray.get(i));
         }

         return lvs;
      } else {
         return new ItemPredicate[0];
      }
   }

   public static class Builder {
      private final List enchantments = Lists.newArrayList();
      private final List storedEnchantments = Lists.newArrayList();
      @Nullable
      private Set item;
      @Nullable
      private TagKey tag;
      private NumberRange.IntRange count;
      private NumberRange.IntRange durability;
      @Nullable
      private Potion potion;
      private NbtPredicate nbt;

      private Builder() {
         this.count = NumberRange.IntRange.ANY;
         this.durability = NumberRange.IntRange.ANY;
         this.nbt = NbtPredicate.ANY;
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder items(ItemConvertible... items) {
         this.item = (Set)Stream.of(items).map(ItemConvertible::asItem).collect(ImmutableSet.toImmutableSet());
         return this;
      }

      public Builder tag(TagKey tag) {
         this.tag = tag;
         return this;
      }

      public Builder count(NumberRange.IntRange count) {
         this.count = count;
         return this;
      }

      public Builder durability(NumberRange.IntRange durability) {
         this.durability = durability;
         return this;
      }

      public Builder potion(Potion potion) {
         this.potion = potion;
         return this;
      }

      public Builder nbt(NbtCompound nbt) {
         this.nbt = new NbtPredicate(nbt);
         return this;
      }

      public Builder enchantment(EnchantmentPredicate enchantment) {
         this.enchantments.add(enchantment);
         return this;
      }

      public Builder storedEnchantment(EnchantmentPredicate enchantment) {
         this.storedEnchantments.add(enchantment);
         return this;
      }

      public ItemPredicate build() {
         return new ItemPredicate(this.tag, this.item, this.count, this.durability, (EnchantmentPredicate[])this.enchantments.toArray(EnchantmentPredicate.ARRAY_OF_ANY), (EnchantmentPredicate[])this.storedEnchantments.toArray(EnchantmentPredicate.ARRAY_OF_ANY), this.potion, this.nbt);
      }
   }
}
