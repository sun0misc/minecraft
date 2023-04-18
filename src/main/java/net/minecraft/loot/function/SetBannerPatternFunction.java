package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SetBannerPatternFunction extends ConditionalLootFunction {
   final List patterns;
   final boolean append;

   SetBannerPatternFunction(LootCondition[] conditions, List patterns, boolean append) {
      super(conditions);
      this.patterns = patterns;
      this.append = append;
   }

   protected ItemStack process(ItemStack stack, LootContext context) {
      NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
      if (lv == null) {
         lv = new NbtCompound();
      }

      BannerPattern.Patterns lv2 = new BannerPattern.Patterns();
      List var10000 = this.patterns;
      Objects.requireNonNull(lv2);
      var10000.forEach(lv2::add);
      NbtList lv3 = lv2.toNbt();
      NbtList lv4;
      if (this.append) {
         lv4 = lv.getList("Patterns", NbtElement.COMPOUND_TYPE).copy();
         lv4.addAll(lv3);
      } else {
         lv4 = lv3;
      }

      lv.put("Patterns", lv4);
      BlockItem.setBlockEntityNbt(stack, BlockEntityType.BANNER, lv);
      return stack;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_BANNER_PATTERN;
   }

   public static Builder builder(boolean append) {
      return new Builder(append);
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final ImmutableList.Builder patterns = ImmutableList.builder();
      private final boolean append;

      Builder(boolean append) {
         this.append = append;
      }

      protected Builder getThisBuilder() {
         return this;
      }

      public LootFunction build() {
         return new SetBannerPatternFunction(this.getConditions(), this.patterns.build(), this.append);
      }

      public Builder pattern(RegistryKey pattern, DyeColor color) {
         return this.pattern((RegistryEntry)Registries.BANNER_PATTERN.entryOf(pattern), color);
      }

      public Builder pattern(RegistryEntry pattern, DyeColor color) {
         this.patterns.add(Pair.of(pattern, color));
         return this;
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetBannerPatternFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         JsonArray jsonArray = new JsonArray();
         arg.patterns.forEach((pair) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("pattern", ((RegistryKey)((RegistryEntry)pair.getFirst()).getKey().orElseThrow(() -> {
               return new JsonSyntaxException("Unknown pattern: " + pair.getFirst());
            })).getValue().toString());
            jsonObject.addProperty("color", ((DyeColor)pair.getSecond()).getName());
            jsonArray.add(jsonObject);
         });
         jsonObject.add("patterns", jsonArray);
         jsonObject.addProperty("append", arg.append);
      }

      public SetBannerPatternFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         ImmutableList.Builder builder = ImmutableList.builder();
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "patterns");

         for(int i = 0; i < jsonArray.size(); ++i) {
            JsonObject jsonObject2 = JsonHelper.asObject(jsonArray.get(i), "pattern[" + i + "]");
            String string = JsonHelper.getString(jsonObject2, "pattern");
            Optional optional = Registries.BANNER_PATTERN.getEntry(RegistryKey.of(RegistryKeys.BANNER_PATTERN, new Identifier(string)));
            if (optional.isEmpty()) {
               throw new JsonSyntaxException("Unknown pattern: " + string);
            }

            String string2 = JsonHelper.getString(jsonObject2, "color");
            DyeColor lv = DyeColor.byName(string2, (DyeColor)null);
            if (lv == null) {
               throw new JsonSyntaxException("Unknown color: " + string2);
            }

            builder.add(Pair.of((RegistryEntry)optional.get(), lv));
         }

         boolean bl = JsonHelper.getBoolean(jsonObject, "append");
         return new SetBannerPatternFunction(args, builder.build(), bl);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
