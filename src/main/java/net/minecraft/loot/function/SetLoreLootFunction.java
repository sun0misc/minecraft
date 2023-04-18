package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class SetLoreLootFunction extends ConditionalLootFunction {
   final boolean replace;
   final List lore;
   @Nullable
   final LootContext.EntityTarget entity;

   public SetLoreLootFunction(LootCondition[] conditions, boolean replace, List lore, @Nullable LootContext.EntityTarget entity) {
      super(conditions);
      this.replace = replace;
      this.lore = ImmutableList.copyOf(lore);
      this.entity = entity;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_LORE;
   }

   public Set getRequiredParameters() {
      return this.entity != null ? ImmutableSet.of(this.entity.getParameter()) : ImmutableSet.of();
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      NbtList lv = this.getLoreForMerge(stack, !this.lore.isEmpty());
      if (lv != null) {
         if (this.replace) {
            lv.clear();
         }

         UnaryOperator unaryOperator = SetNameLootFunction.applySourceEntity(context, this.entity);
         Stream var10000 = this.lore.stream().map(unaryOperator).map(Text.Serializer::toJson).map(NbtString::of);
         Objects.requireNonNull(lv);
         var10000.forEach(lv::add);
      }

      return stack;
   }

   @Nullable
   private NbtList getLoreForMerge(ItemStack stack, boolean otherLoreExists) {
      NbtCompound lv;
      if (stack.hasNbt()) {
         lv = stack.getNbt();
      } else {
         if (!otherLoreExists) {
            return null;
         }

         lv = new NbtCompound();
         stack.setNbt(lv);
      }

      NbtCompound lv2;
      if (lv.contains("display", NbtElement.COMPOUND_TYPE)) {
         lv2 = lv.getCompound("display");
      } else {
         if (!otherLoreExists) {
            return null;
         }

         lv2 = new NbtCompound();
         lv.put("display", lv2);
      }

      if (lv2.contains("Lore", NbtElement.LIST_TYPE)) {
         return lv2.getList("Lore", NbtElement.STRING_TYPE);
      } else if (otherLoreExists) {
         NbtList lv3 = new NbtList();
         lv2.put("Lore", lv3);
         return lv3;
      } else {
         return null;
      }
   }

   public static Builder builder() {
      return new Builder();
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private boolean replace;
      private LootContext.EntityTarget target;
      private final List lore = Lists.newArrayList();

      public Builder replace(boolean replace) {
         this.replace = replace;
         return this;
      }

      public Builder target(LootContext.EntityTarget target) {
         this.target = target;
         return this;
      }

      public Builder lore(Text lore) {
         this.lore.add(lore);
         return this;
      }

      protected Builder getThisBuilder() {
         return this;
      }

      public LootFunction build() {
         return new SetLoreLootFunction(this.getConditions(), this.replace, this.lore, this.target);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetLoreLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.addProperty("replace", arg.replace);
         JsonArray jsonArray = new JsonArray();
         Iterator var5 = arg.lore.iterator();

         while(var5.hasNext()) {
            Text lv = (Text)var5.next();
            jsonArray.add(Text.Serializer.toJsonTree(lv));
         }

         jsonObject.add("lore", jsonArray);
         if (arg.entity != null) {
            jsonObject.add("entity", jsonSerializationContext.serialize(arg.entity));
         }

      }

      public SetLoreLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         boolean bl = JsonHelper.getBoolean(jsonObject, "replace", false);
         List list = (List)Streams.stream(JsonHelper.getArray(jsonObject, "lore")).map(Text.Serializer::fromJson).collect(ImmutableList.toImmutableList());
         LootContext.EntityTarget lv = (LootContext.EntityTarget)JsonHelper.deserialize(jsonObject, "entity", (Object)null, jsonDeserializationContext, LootContext.EntityTarget.class);
         return new SetLoreLootFunction(args, bl, list, lv);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
