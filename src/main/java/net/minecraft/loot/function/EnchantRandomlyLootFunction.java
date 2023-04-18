package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;

public class EnchantRandomlyLootFunction extends ConditionalLootFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final List enchantments;

   EnchantRandomlyLootFunction(LootCondition[] conditions, Collection enchantments) {
      super(conditions);
      this.enchantments = ImmutableList.copyOf(enchantments);
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.ENCHANT_RANDOMLY;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Random lv = context.getRandom();
      Enchantment lv2;
      if (this.enchantments.isEmpty()) {
         boolean bl = stack.isOf(Items.BOOK);
         List list = (List)Registries.ENCHANTMENT.stream().filter(Enchantment::isAvailableForRandomSelection).filter((enchantment) -> {
            return bl || enchantment.isAcceptableItem(stack);
         }).collect(Collectors.toList());
         if (list.isEmpty()) {
            LOGGER.warn("Couldn't find a compatible enchantment for {}", stack);
            return stack;
         }

         lv2 = (Enchantment)list.get(lv.nextInt(list.size()));
      } else {
         lv2 = (Enchantment)this.enchantments.get(lv.nextInt(this.enchantments.size()));
      }

      return addEnchantmentToStack(stack, lv2, lv);
   }

   private static ItemStack addEnchantmentToStack(ItemStack stack, Enchantment enchantment, Random random) {
      int i = MathHelper.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
      if (stack.isOf(Items.BOOK)) {
         stack = new ItemStack(Items.ENCHANTED_BOOK);
         EnchantedBookItem.addEnchantment(stack, new EnchantmentLevelEntry(enchantment, i));
      } else {
         stack.addEnchantment(enchantment, i);
      }

      return stack;
   }

   public static Builder create() {
      return new Builder();
   }

   public static ConditionalLootFunction.Builder builder() {
      return builder((conditions) -> {
         return new EnchantRandomlyLootFunction(conditions, ImmutableList.of());
      });
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final Set enchantments = Sets.newHashSet();

      protected Builder getThisBuilder() {
         return this;
      }

      public Builder add(Enchantment enchantment) {
         this.enchantments.add(enchantment);
         return this;
      }

      public LootFunction build() {
         return new EnchantRandomlyLootFunction(this.getConditions(), this.enchantments);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, EnchantRandomlyLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         if (!arg.enchantments.isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            Iterator var5 = arg.enchantments.iterator();

            while(var5.hasNext()) {
               Enchantment lv = (Enchantment)var5.next();
               Identifier lv2 = Registries.ENCHANTMENT.getId(lv);
               if (lv2 == null) {
                  throw new IllegalArgumentException("Don't know how to serialize enchantment " + lv);
               }

               jsonArray.add(new JsonPrimitive(lv2.toString()));
            }

            jsonObject.add("enchantments", jsonArray);
         }

      }

      public EnchantRandomlyLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         List list = Lists.newArrayList();
         if (jsonObject.has("enchantments")) {
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "enchantments");
            Iterator var6 = jsonArray.iterator();

            while(var6.hasNext()) {
               JsonElement jsonElement = (JsonElement)var6.next();
               String string = JsonHelper.asString(jsonElement, "enchantment");
               Enchantment lv = (Enchantment)Registries.ENCHANTMENT.getOrEmpty(new Identifier(string)).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown enchantment '" + string + "'");
               });
               list.add(lv);
            }
         }

         return new EnchantRandomlyLootFunction(args, list);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
