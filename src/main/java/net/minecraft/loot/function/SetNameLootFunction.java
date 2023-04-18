package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SetNameLootFunction extends ConditionalLootFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final Text name;
   @Nullable
   final LootContext.EntityTarget entity;

   SetNameLootFunction(LootCondition[] conditions, @Nullable Text name, @Nullable LootContext.EntityTarget entity) {
      super(conditions);
      this.name = name;
      this.entity = entity;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_NAME;
   }

   public Set getRequiredParameters() {
      return this.entity != null ? ImmutableSet.of(this.entity.getParameter()) : ImmutableSet.of();
   }

   public static UnaryOperator applySourceEntity(LootContext context, @Nullable LootContext.EntityTarget sourceEntity) {
      if (sourceEntity != null) {
         Entity lv = (Entity)context.get(sourceEntity.getParameter());
         if (lv != null) {
            ServerCommandSource lv2 = lv.getCommandSource().withLevel(2);
            return (textComponent) -> {
               try {
                  return Texts.parse(lv2, (Text)textComponent, lv, 0);
               } catch (CommandSyntaxException var4) {
                  LOGGER.warn("Failed to resolve text component", var4);
                  return textComponent;
               }
            };
         }
      }

      return (textComponent) -> {
         return textComponent;
      };
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      if (this.name != null) {
         stack.setCustomName((Text)applySourceEntity(context, this.entity).apply(this.name));
      }

      return stack;
   }

   public static ConditionalLootFunction.Builder builder(Text name) {
      return builder((conditions) -> {
         return new SetNameLootFunction(conditions, name, (LootContext.EntityTarget)null);
      });
   }

   public static ConditionalLootFunction.Builder builder(Text name, LootContext.EntityTarget target) {
      return builder((conditions) -> {
         return new SetNameLootFunction(conditions, name, target);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetNameLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         if (arg.name != null) {
            jsonObject.add("name", Text.Serializer.toJsonTree(arg.name));
         }

         if (arg.entity != null) {
            jsonObject.add("entity", jsonSerializationContext.serialize(arg.entity));
         }

      }

      public SetNameLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         Text lv = Text.Serializer.fromJson(jsonObject.get("name"));
         LootContext.EntityTarget lv2 = (LootContext.EntityTarget)JsonHelper.deserialize(jsonObject, "entity", (Object)null, jsonDeserializationContext, LootContext.EntityTarget.class);
         return new SetNameLootFunction(args, lv, lv2);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
