package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.authlib.GameProfile;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.JsonHelper;

public class FillPlayerHeadLootFunction extends ConditionalLootFunction {
   final LootContext.EntityTarget entity;

   public FillPlayerHeadLootFunction(LootCondition[] conditions, LootContext.EntityTarget entity) {
      super(conditions);
      this.entity = entity;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.FILL_PLAYER_HEAD;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(this.entity.getParameter());
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      if (stack.isOf(Items.PLAYER_HEAD)) {
         Entity lv = (Entity)context.get(this.entity.getParameter());
         if (lv instanceof PlayerEntity) {
            GameProfile gameProfile = ((PlayerEntity)lv).getGameProfile();
            stack.getOrCreateNbt().put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), gameProfile));
         }
      }

      return stack;
   }

   public static ConditionalLootFunction.Builder builder(LootContext.EntityTarget target) {
      return builder((conditions) -> {
         return new FillPlayerHeadLootFunction(conditions, target);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, FillPlayerHeadLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.add("entity", jsonSerializationContext.serialize(arg.entity));
      }

      public FillPlayerHeadLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         LootContext.EntityTarget lv = (LootContext.EntityTarget)JsonHelper.deserialize(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class);
         return new FillPlayerHeadLootFunction(args, lv);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
