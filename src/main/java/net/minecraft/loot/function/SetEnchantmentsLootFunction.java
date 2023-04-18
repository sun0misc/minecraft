package net.minecraft.loot.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetEnchantmentsLootFunction extends ConditionalLootFunction {
   final Map enchantments;
   final boolean add;

   SetEnchantmentsLootFunction(LootCondition[] conditions, Map enchantments, boolean add) {
      super(conditions);
      this.enchantments = ImmutableMap.copyOf(enchantments);
      this.add = add;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_ENCHANTMENTS;
   }

   public Set getRequiredParameters() {
      return (Set)this.enchantments.values().stream().flatMap((numberProvider) -> {
         return numberProvider.getRequiredParameters().stream();
      }).collect(ImmutableSet.toImmutableSet());
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Object2IntMap object2IntMap = new Object2IntOpenHashMap();
      this.enchantments.forEach((enchantment, numberProvider) -> {
         object2IntMap.put(enchantment, numberProvider.nextInt(context));
      });
      if (stack.getItem() == Items.BOOK) {
         ItemStack lv = new ItemStack(Items.ENCHANTED_BOOK);
         object2IntMap.forEach((enchantment, level) -> {
            EnchantedBookItem.addEnchantment(lv, new EnchantmentLevelEntry(enchantment, level));
         });
         return lv;
      } else {
         Map map = EnchantmentHelper.get(stack);
         if (this.add) {
            object2IntMap.forEach((enchantment, level) -> {
               addEnchantmentToMap(map, enchantment, Math.max((Integer)map.getOrDefault(enchantment, 0) + level, 0));
            });
         } else {
            object2IntMap.forEach((enchantment, level) -> {
               addEnchantmentToMap(map, enchantment, Math.max(level, 0));
            });
         }

         EnchantmentHelper.set(map, stack);
         return stack;
      }
   }

   private static void addEnchantmentToMap(Map map, Enchantment enchantment, int level) {
      if (level == 0) {
         map.remove(enchantment);
      } else {
         map.put(enchantment, level);
      }

   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetEnchantmentsLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         JsonObject jsonObject2 = new JsonObject();
         arg.enchantments.forEach((enchantment, numberProvider) -> {
            Identifier lv = Registries.ENCHANTMENT.getId(enchantment);
            if (lv == null) {
               throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
            } else {
               jsonObject2.add(lv.toString(), jsonSerializationContext.serialize(numberProvider));
            }
         });
         jsonObject.add("enchantments", jsonObject2);
         jsonObject.addProperty("add", arg.add);
      }

      public SetEnchantmentsLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         Map map = Maps.newHashMap();
         if (jsonObject.has("enchantments")) {
            JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "enchantments");
            Iterator var6 = jsonObject2.entrySet().iterator();

            while(var6.hasNext()) {
               Map.Entry entry = (Map.Entry)var6.next();
               String string = (String)entry.getKey();
               JsonElement jsonElement = (JsonElement)entry.getValue();
               Enchantment lv = (Enchantment)Registries.ENCHANTMENT.getOrEmpty(new Identifier(string)).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown enchantment '" + string + "'");
               });
               LootNumberProvider lv2 = (LootNumberProvider)jsonDeserializationContext.deserialize(jsonElement, LootNumberProvider.class);
               map.put(lv, lv2);
            }
         }

         boolean bl = JsonHelper.getBoolean(jsonObject, "add", false);
         return new SetEnchantmentsLootFunction(args, map, bl);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final Map enchantments;
      private final boolean add;

      public Builder() {
         this(false);
      }

      public Builder(boolean add) {
         this.enchantments = Maps.newHashMap();
         this.add = add;
      }

      protected Builder getThisBuilder() {
         return this;
      }

      public Builder enchantment(Enchantment enchantment, LootNumberProvider level) {
         this.enchantments.put(enchantment, level);
         return this;
      }

      public LootFunction build() {
         return new SetEnchantmentsLootFunction(this.getConditions(), this.enchantments, this.add);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }
}
