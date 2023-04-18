package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;

public class SetDamageLootFunction extends ConditionalLootFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final LootNumberProvider durabilityRange;
   final boolean add;

   SetDamageLootFunction(LootCondition[] conditions, LootNumberProvider durabilityRange, boolean add) {
      super(conditions);
      this.durabilityRange = durabilityRange;
      this.add = add;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_DAMAGE;
   }

   public Set getRequiredParameters() {
      return this.durabilityRange.getRequiredParameters();
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      if (stack.isDamageable()) {
         int i = stack.getMaxDamage();
         float f = this.add ? 1.0F - (float)stack.getDamage() / (float)i : 0.0F;
         float g = 1.0F - MathHelper.clamp(this.durabilityRange.nextFloat(context) + f, 0.0F, 1.0F);
         stack.setDamage(MathHelper.floor(g * (float)i));
      } else {
         LOGGER.warn("Couldn't set damage of loot item {}", stack);
      }

      return stack;
   }

   public static ConditionalLootFunction.Builder builder(LootNumberProvider durabilityRange) {
      return builder((conditions) -> {
         return new SetDamageLootFunction(conditions, durabilityRange, false);
      });
   }

   public static ConditionalLootFunction.Builder builder(LootNumberProvider durabilityRange, boolean add) {
      return builder((conditions) -> {
         return new SetDamageLootFunction(conditions, durabilityRange, add);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetDamageLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.add("damage", jsonSerializationContext.serialize(arg.durabilityRange));
         jsonObject.addProperty("add", arg.add);
      }

      public SetDamageLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         LootNumberProvider lv = (LootNumberProvider)JsonHelper.deserialize(jsonObject, "damage", jsonDeserializationContext, LootNumberProvider.class);
         boolean bl = JsonHelper.getBoolean(jsonObject, "add", false);
         return new SetDamageLootFunction(args, lv, bl);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
