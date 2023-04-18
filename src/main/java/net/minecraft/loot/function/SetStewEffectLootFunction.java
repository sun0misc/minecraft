package net.minecraft.loot.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;

public class SetStewEffectLootFunction extends ConditionalLootFunction {
   final Map effects;

   SetStewEffectLootFunction(LootCondition[] conditions, Map effects) {
      super(conditions);
      this.effects = ImmutableMap.copyOf(effects);
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_STEW_EFFECT;
   }

   public Set getRequiredParameters() {
      return (Set)this.effects.values().stream().flatMap((numberProvider) -> {
         return numberProvider.getRequiredParameters().stream();
      }).collect(ImmutableSet.toImmutableSet());
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      if (stack.isOf(Items.SUSPICIOUS_STEW) && !this.effects.isEmpty()) {
         Random lv = context.getRandom();
         int i = lv.nextInt(this.effects.size());
         Map.Entry entry = (Map.Entry)Iterables.get(this.effects.entrySet(), i);
         StatusEffect lv2 = (StatusEffect)entry.getKey();
         int j = ((LootNumberProvider)entry.getValue()).nextInt(context);
         if (!lv2.isInstant()) {
            j *= 20;
         }

         SuspiciousStewItem.addEffectToStew(stack, lv2, j);
         return stack;
      } else {
         return stack;
      }
   }

   public static Builder builder() {
      return new Builder();
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final Map map = Maps.newLinkedHashMap();

      protected Builder getThisBuilder() {
         return this;
      }

      public Builder withEffect(StatusEffect effect, LootNumberProvider durationRange) {
         this.map.put(effect, durationRange);
         return this;
      }

      public LootFunction build() {
         return new SetStewEffectLootFunction(this.getConditions(), this.map);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetStewEffectLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         if (!arg.effects.isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            Iterator var5 = arg.effects.keySet().iterator();

            while(var5.hasNext()) {
               StatusEffect lv = (StatusEffect)var5.next();
               JsonObject jsonObject2 = new JsonObject();
               Identifier lv2 = Registries.STATUS_EFFECT.getId(lv);
               if (lv2 == null) {
                  throw new IllegalArgumentException("Don't know how to serialize mob effect " + lv);
               }

               jsonObject2.add("type", new JsonPrimitive(lv2.toString()));
               jsonObject2.add("duration", jsonSerializationContext.serialize(arg.effects.get(lv)));
               jsonArray.add(jsonObject2);
            }

            jsonObject.add("effects", jsonArray);
         }

      }

      public SetStewEffectLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         Map map = Maps.newLinkedHashMap();
         if (jsonObject.has("effects")) {
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "effects");
            Iterator var6 = jsonArray.iterator();

            while(var6.hasNext()) {
               JsonElement jsonElement = (JsonElement)var6.next();
               String string = JsonHelper.getString(jsonElement.getAsJsonObject(), "type");
               StatusEffect lv = (StatusEffect)Registries.STATUS_EFFECT.getOrEmpty(new Identifier(string)).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown mob effect '" + string + "'");
               });
               LootNumberProvider lv2 = (LootNumberProvider)JsonHelper.deserialize(jsonElement.getAsJsonObject(), "duration", jsonDeserializationContext, LootNumberProvider.class);
               map.put(lv, lv2);
            }
         }

         return new SetStewEffectLootFunction(args, map);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
