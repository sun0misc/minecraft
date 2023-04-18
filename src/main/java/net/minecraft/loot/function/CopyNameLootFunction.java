package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Nameable;

public class CopyNameLootFunction extends ConditionalLootFunction {
   final Source source;

   CopyNameLootFunction(LootCondition[] conditions, Source source) {
      super(conditions);
      this.source = source;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.COPY_NAME;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(this.source.parameter);
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Object object = context.get(this.source.parameter);
      if (object instanceof Nameable lv) {
         if (lv.hasCustomName()) {
            stack.setCustomName(lv.getDisplayName());
         }
      }

      return stack;
   }

   public static ConditionalLootFunction.Builder builder(Source source) {
      return builder((conditions) -> {
         return new CopyNameLootFunction(conditions, source);
      });
   }

   public static enum Source {
      THIS("this", LootContextParameters.THIS_ENTITY),
      KILLER("killer", LootContextParameters.KILLER_ENTITY),
      KILLER_PLAYER("killer_player", LootContextParameters.LAST_DAMAGE_PLAYER),
      BLOCK_ENTITY("block_entity", LootContextParameters.BLOCK_ENTITY);

      public final String name;
      public final LootContextParameter parameter;

      private Source(String name, LootContextParameter parameter) {
         this.name = name;
         this.parameter = parameter;
      }

      public static Source get(String name) {
         Source[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Source lv = var1[var3];
            if (lv.name.equals(name)) {
               return lv;
            }
         }

         throw new IllegalArgumentException("Invalid name source " + name);
      }

      // $FF: synthetic method
      private static Source[] method_36794() {
         return new Source[]{THIS, KILLER, KILLER_PLAYER, BLOCK_ENTITY};
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, CopyNameLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.addProperty("source", arg.source.name);
      }

      public CopyNameLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         Source lv = CopyNameLootFunction.Source.get(JsonHelper.getString(jsonObject, "source"));
         return new CopyNameLootFunction(args, lv);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
