package net.minecraft.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public abstract class NumberRange {
   public static final SimpleCommandExceptionType EXCEPTION_EMPTY = new SimpleCommandExceptionType(Text.translatable("argument.range.empty"));
   public static final SimpleCommandExceptionType EXCEPTION_SWAPPED = new SimpleCommandExceptionType(Text.translatable("argument.range.swapped"));
   @Nullable
   protected final Number min;
   @Nullable
   protected final Number max;

   protected NumberRange(@Nullable Number min, @Nullable Number max) {
      this.min = min;
      this.max = max;
   }

   @Nullable
   public Number getMin() {
      return this.min;
   }

   @Nullable
   public Number getMax() {
      return this.max;
   }

   public boolean isDummy() {
      return this.min == null && this.max == null;
   }

   public JsonElement toJson() {
      if (this.isDummy()) {
         return JsonNull.INSTANCE;
      } else if (this.min != null && this.min.equals(this.max)) {
         return new JsonPrimitive(this.min);
      } else {
         JsonObject jsonObject = new JsonObject();
         if (this.min != null) {
            jsonObject.addProperty("min", this.min);
         }

         if (this.max != null) {
            jsonObject.addProperty("max", this.max);
         }

         return jsonObject;
      }
   }

   protected static NumberRange fromJson(@Nullable JsonElement json, NumberRange fallback, BiFunction asNumber, Factory factory) {
      if (json != null && !json.isJsonNull()) {
         if (JsonHelper.isNumber(json)) {
            Number number = (Number)asNumber.apply(json, "value");
            return factory.create(number, number);
         } else {
            JsonObject jsonObject = JsonHelper.asObject(json, "value");
            Number number2 = jsonObject.has("min") ? (Number)asNumber.apply(jsonObject.get("min"), "min") : null;
            Number number3 = jsonObject.has("max") ? (Number)asNumber.apply(jsonObject.get("max"), "max") : null;
            return factory.create(number2, number3);
         }
      } else {
         return fallback;
      }
   }

   protected static NumberRange parse(StringReader commandReader, CommandFactory commandFactory, Function converter, Supplier exceptionTypeSupplier, Function mapper) throws CommandSyntaxException {
      if (!commandReader.canRead()) {
         throw EXCEPTION_EMPTY.createWithContext(commandReader);
      } else {
         int i = commandReader.getCursor();

         try {
            Number number = (Number)map(fromStringReader(commandReader, converter, exceptionTypeSupplier), mapper);
            Number number2;
            if (commandReader.canRead(2) && commandReader.peek() == '.' && commandReader.peek(1) == '.') {
               commandReader.skip();
               commandReader.skip();
               number2 = (Number)map(fromStringReader(commandReader, converter, exceptionTypeSupplier), mapper);
               if (number == null && number2 == null) {
                  throw EXCEPTION_EMPTY.createWithContext(commandReader);
               }
            } else {
               number2 = number;
            }

            if (number == null && number2 == null) {
               throw EXCEPTION_EMPTY.createWithContext(commandReader);
            } else {
               return commandFactory.create(commandReader, number, number2);
            }
         } catch (CommandSyntaxException var8) {
            commandReader.setCursor(i);
            throw new CommandSyntaxException(var8.getType(), var8.getRawMessage(), var8.getInput(), i);
         }
      }
   }

   @Nullable
   private static Number fromStringReader(StringReader reader, Function converter, Supplier exceptionTypeSupplier) throws CommandSyntaxException {
      int i = reader.getCursor();

      while(reader.canRead() && isNextCharValid(reader)) {
         reader.skip();
      }

      String string = reader.getString().substring(i, reader.getCursor());
      if (string.isEmpty()) {
         return null;
      } else {
         try {
            return (Number)converter.apply(string);
         } catch (NumberFormatException var6) {
            throw ((DynamicCommandExceptionType)exceptionTypeSupplier.get()).createWithContext(reader, string);
         }
      }
   }

   private static boolean isNextCharValid(StringReader reader) {
      char c = reader.peek();
      if ((c < '0' || c > '9') && c != '-') {
         if (c != '.') {
            return false;
         } else {
            return !reader.canRead(2) || reader.peek(1) != '.';
         }
      } else {
         return true;
      }
   }

   @Nullable
   private static Object map(@Nullable Object object, Function function) {
      return object == null ? null : function.apply(object);
   }

   @FunctionalInterface
   protected interface Factory {
      NumberRange create(@Nullable Number min, @Nullable Number max);
   }

   @FunctionalInterface
   protected interface CommandFactory {
      NumberRange create(StringReader reader, @Nullable Number min, @Nullable Number max) throws CommandSyntaxException;
   }

   public static class FloatRange extends NumberRange {
      public static final FloatRange ANY = new FloatRange((Double)null, (Double)null);
      @Nullable
      private final Double squaredMin;
      @Nullable
      private final Double squaredMax;

      private static FloatRange create(StringReader reader, @Nullable Double min, @Nullable Double max) throws CommandSyntaxException {
         if (min != null && max != null && min > max) {
            throw EXCEPTION_SWAPPED.createWithContext(reader);
         } else {
            return new FloatRange(min, max);
         }
      }

      @Nullable
      private static Double square(@Nullable Double value) {
         return value == null ? null : value * value;
      }

      private FloatRange(@Nullable Double min, @Nullable Double max) {
         super(min, max);
         this.squaredMin = square(min);
         this.squaredMax = square(max);
      }

      public static FloatRange exactly(double value) {
         return new FloatRange(value, value);
      }

      public static FloatRange between(double min, double max) {
         return new FloatRange(min, max);
      }

      public static FloatRange atLeast(double value) {
         return new FloatRange(value, (Double)null);
      }

      public static FloatRange atMost(double value) {
         return new FloatRange((Double)null, value);
      }

      public boolean test(double value) {
         if (this.min != null && (Double)this.min > value) {
            return false;
         } else {
            return this.max == null || !((Double)this.max < value);
         }
      }

      public boolean testSqrt(double value) {
         if (this.squaredMin != null && this.squaredMin > value) {
            return false;
         } else {
            return this.squaredMax == null || !(this.squaredMax < value);
         }
      }

      public static FloatRange fromJson(@Nullable JsonElement element) {
         return (FloatRange)fromJson(element, ANY, JsonHelper::asDouble, FloatRange::new);
      }

      public static FloatRange parse(StringReader reader) throws CommandSyntaxException {
         return parse(reader, (value) -> {
            return value;
         });
      }

      public static FloatRange parse(StringReader reader, Function mapper) throws CommandSyntaxException {
         CommandFactory var10001 = FloatRange::create;
         Function var10002 = Double::parseDouble;
         BuiltInExceptionProvider var10003 = CommandSyntaxException.BUILT_IN_EXCEPTIONS;
         Objects.requireNonNull(var10003);
         return (FloatRange)parse(reader, var10001, var10002, var10003::readerInvalidDouble, mapper);
      }
   }

   public static class IntRange extends NumberRange {
      public static final IntRange ANY = new IntRange((Integer)null, (Integer)null);
      @Nullable
      private final Long minSquared;
      @Nullable
      private final Long maxSquared;

      private static IntRange parse(StringReader reader, @Nullable Integer min, @Nullable Integer max) throws CommandSyntaxException {
         if (min != null && max != null && min > max) {
            throw EXCEPTION_SWAPPED.createWithContext(reader);
         } else {
            return new IntRange(min, max);
         }
      }

      @Nullable
      private static Long squared(@Nullable Integer value) {
         return value == null ? null : value.longValue() * value.longValue();
      }

      private IntRange(@Nullable Integer min, @Nullable Integer max) {
         super(min, max);
         this.minSquared = squared(min);
         this.maxSquared = squared(max);
      }

      public static IntRange exactly(int value) {
         return new IntRange(value, value);
      }

      public static IntRange between(int min, int max) {
         return new IntRange(min, max);
      }

      public static IntRange atLeast(int value) {
         return new IntRange(value, (Integer)null);
      }

      public static IntRange atMost(int value) {
         return new IntRange((Integer)null, value);
      }

      public boolean test(int value) {
         if (this.min != null && (Integer)this.min > value) {
            return false;
         } else {
            return this.max == null || (Integer)this.max >= value;
         }
      }

      public boolean testSqrt(long value) {
         if (this.minSquared != null && this.minSquared > value) {
            return false;
         } else {
            return this.maxSquared == null || this.maxSquared >= value;
         }
      }

      public static IntRange fromJson(@Nullable JsonElement element) {
         return (IntRange)fromJson(element, ANY, JsonHelper::asInt, IntRange::new);
      }

      public static IntRange parse(StringReader reader) throws CommandSyntaxException {
         return fromStringReader(reader, (value) -> {
            return value;
         });
      }

      public static IntRange fromStringReader(StringReader reader, Function converter) throws CommandSyntaxException {
         CommandFactory var10001 = IntRange::parse;
         Function var10002 = Integer::parseInt;
         BuiltInExceptionProvider var10003 = CommandSyntaxException.BUILT_IN_EXCEPTIONS;
         Objects.requireNonNull(var10003);
         return (IntRange)parse(reader, var10001, var10002, var10003::readerInvalidInt, converter);
      }
   }
}
