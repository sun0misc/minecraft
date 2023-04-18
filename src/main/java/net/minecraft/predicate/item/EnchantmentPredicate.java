package net.minecraft.predicate.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class EnchantmentPredicate {
   public static final EnchantmentPredicate ANY = new EnchantmentPredicate();
   public static final EnchantmentPredicate[] ARRAY_OF_ANY = new EnchantmentPredicate[0];
   @Nullable
   private final Enchantment enchantment;
   private final NumberRange.IntRange levels;

   public EnchantmentPredicate() {
      this.enchantment = null;
      this.levels = NumberRange.IntRange.ANY;
   }

   public EnchantmentPredicate(@Nullable Enchantment enchantment, NumberRange.IntRange levels) {
      this.enchantment = enchantment;
      this.levels = levels;
   }

   public boolean test(Map enchantments) {
      if (this.enchantment != null) {
         if (!enchantments.containsKey(this.enchantment)) {
            return false;
         }

         int i = (Integer)enchantments.get(this.enchantment);
         if (this.levels != NumberRange.IntRange.ANY && !this.levels.test(i)) {
            return false;
         }
      } else if (this.levels != NumberRange.IntRange.ANY) {
         Iterator var4 = enchantments.values().iterator();

         Integer integer;
         do {
            if (!var4.hasNext()) {
               return false;
            }

            integer = (Integer)var4.next();
         } while(!this.levels.test(integer));

         return true;
      }

      return true;
   }

   public JsonElement serialize() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         if (this.enchantment != null) {
            jsonObject.addProperty("enchantment", Registries.ENCHANTMENT.getId(this.enchantment).toString());
         }

         jsonObject.add("levels", this.levels.toJson());
         return jsonObject;
      }
   }

   public static EnchantmentPredicate deserialize(@Nullable JsonElement el) {
      if (el != null && !el.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(el, "enchantment");
         Enchantment lv = null;
         if (jsonObject.has("enchantment")) {
            Identifier lv2 = new Identifier(JsonHelper.getString(jsonObject, "enchantment"));
            lv = (Enchantment)Registries.ENCHANTMENT.getOrEmpty(lv2).orElseThrow(() -> {
               return new JsonSyntaxException("Unknown enchantment '" + lv2 + "'");
            });
         }

         NumberRange.IntRange lv3 = NumberRange.IntRange.fromJson(jsonObject.get("levels"));
         return new EnchantmentPredicate(lv, lv3);
      } else {
         return ANY;
      }
   }

   public static EnchantmentPredicate[] deserializeAll(@Nullable JsonElement el) {
      if (el != null && !el.isJsonNull()) {
         JsonArray jsonArray = JsonHelper.asArray(el, "enchantments");
         EnchantmentPredicate[] lvs = new EnchantmentPredicate[jsonArray.size()];

         for(int i = 0; i < lvs.length; ++i) {
            lvs[i] = deserialize(jsonArray.get(i));
         }

         return lvs;
      } else {
         return ARRAY_OF_ANY;
      }
   }
}
