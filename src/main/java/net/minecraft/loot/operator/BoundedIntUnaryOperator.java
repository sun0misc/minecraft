package net.minecraft.loot.operator;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class BoundedIntUnaryOperator {
   @Nullable
   final LootNumberProvider min;
   @Nullable
   final LootNumberProvider max;
   private final Applier applier;
   private final Tester tester;

   public Set getRequiredParameters() {
      ImmutableSet.Builder builder = ImmutableSet.builder();
      if (this.min != null) {
         builder.addAll(this.min.getRequiredParameters());
      }

      if (this.max != null) {
         builder.addAll(this.max.getRequiredParameters());
      }

      return builder.build();
   }

   BoundedIntUnaryOperator(@Nullable LootNumberProvider min, @Nullable LootNumberProvider max) {
      this.min = min;
      this.max = max;
      if (min == null) {
         if (max == null) {
            this.applier = (context, value) -> {
               return value;
            };
            this.tester = (context, value) -> {
               return true;
            };
         } else {
            this.applier = (context, value) -> {
               return Math.min(max.nextInt(context), value);
            };
            this.tester = (context, value) -> {
               return value <= max.nextInt(context);
            };
         }
      } else if (max == null) {
         this.applier = (context, value) -> {
            return Math.max(min.nextInt(context), value);
         };
         this.tester = (context, value) -> {
            return value >= min.nextInt(context);
         };
      } else {
         this.applier = (context, value) -> {
            return MathHelper.clamp(value, min.nextInt(context), max.nextInt(context));
         };
         this.tester = (context, value) -> {
            return value >= min.nextInt(context) && value <= max.nextInt(context);
         };
      }

   }

   public static BoundedIntUnaryOperator create(int value) {
      ConstantLootNumberProvider lv = ConstantLootNumberProvider.create((float)value);
      return new BoundedIntUnaryOperator(lv, lv);
   }

   public static BoundedIntUnaryOperator create(int min, int max) {
      return new BoundedIntUnaryOperator(ConstantLootNumberProvider.create((float)min), ConstantLootNumberProvider.create((float)max));
   }

   public static BoundedIntUnaryOperator createMin(int min) {
      return new BoundedIntUnaryOperator(ConstantLootNumberProvider.create((float)min), (LootNumberProvider)null);
   }

   public static BoundedIntUnaryOperator createMax(int max) {
      return new BoundedIntUnaryOperator((LootNumberProvider)null, ConstantLootNumberProvider.create((float)max));
   }

   public int apply(LootContext context, int value) {
      return this.applier.apply(context, value);
   }

   public boolean test(LootContext context, int value) {
      return this.tester.test(context, value);
   }

   @FunctionalInterface
   private interface Applier {
      int apply(LootContext context, int value);
   }

   @FunctionalInterface
   private interface Tester {
      boolean test(LootContext context, int value);
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public BoundedIntUnaryOperator deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
         if (jsonElement.isJsonPrimitive()) {
            return BoundedIntUnaryOperator.create(jsonElement.getAsInt());
         } else {
            JsonObject jsonObject = JsonHelper.asObject(jsonElement, "value");
            LootNumberProvider lv = jsonObject.has("min") ? (LootNumberProvider)JsonHelper.deserialize(jsonObject, "min", jsonDeserializationContext, LootNumberProvider.class) : null;
            LootNumberProvider lv2 = jsonObject.has("max") ? (LootNumberProvider)JsonHelper.deserialize(jsonObject, "max", jsonDeserializationContext, LootNumberProvider.class) : null;
            return new BoundedIntUnaryOperator(lv, lv2);
         }
      }

      public JsonElement serialize(BoundedIntUnaryOperator arg, Type type, JsonSerializationContext jsonSerializationContext) {
         JsonObject jsonObject = new JsonObject();
         if (Objects.equals(arg.max, arg.min)) {
            return jsonSerializationContext.serialize(arg.min);
         } else {
            if (arg.max != null) {
               jsonObject.add("max", jsonSerializationContext.serialize(arg.max));
            }

            if (arg.min != null) {
               jsonObject.add("min", jsonSerializationContext.serialize(arg.min));
            }

            return jsonObject;
         }
      }

      // $FF: synthetic method
      public JsonElement serialize(Object entry, Type unused, JsonSerializationContext context) {
         return this.serialize((BoundedIntUnaryOperator)entry, unused, context);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement json, Type unused, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, unused, context);
      }
   }
}
