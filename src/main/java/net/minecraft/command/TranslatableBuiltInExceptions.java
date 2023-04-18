package net.minecraft.command;

import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;

public class TranslatableBuiltInExceptions implements BuiltInExceptionProvider {
   private static final Dynamic2CommandExceptionType DOUBLE_TOO_LOW = new Dynamic2CommandExceptionType((found, min) -> {
      return Text.translatable("argument.double.low", min, found);
   });
   private static final Dynamic2CommandExceptionType DOUBLE_TOO_HIGH = new Dynamic2CommandExceptionType((found, max) -> {
      return Text.translatable("argument.double.big", max, found);
   });
   private static final Dynamic2CommandExceptionType FLOAT_TOO_LOW = new Dynamic2CommandExceptionType((found, min) -> {
      return Text.translatable("argument.float.low", min, found);
   });
   private static final Dynamic2CommandExceptionType FLOAT_TOO_HIGH = new Dynamic2CommandExceptionType((found, max) -> {
      return Text.translatable("argument.float.big", max, found);
   });
   private static final Dynamic2CommandExceptionType INTEGER_TOO_LOW = new Dynamic2CommandExceptionType((found, min) -> {
      return Text.translatable("argument.integer.low", min, found);
   });
   private static final Dynamic2CommandExceptionType INTEGER_TOO_HIGH = new Dynamic2CommandExceptionType((found, max) -> {
      return Text.translatable("argument.integer.big", max, found);
   });
   private static final Dynamic2CommandExceptionType LONG_TOO_LOW = new Dynamic2CommandExceptionType((found, min) -> {
      return Text.translatable("argument.long.low", min, found);
   });
   private static final Dynamic2CommandExceptionType LONG_TOO_HIGH = new Dynamic2CommandExceptionType((found, max) -> {
      return Text.translatable("argument.long.big", max, found);
   });
   private static final DynamicCommandExceptionType LITERAL_INCORRECT = new DynamicCommandExceptionType((expected) -> {
      return Text.translatable("argument.literal.incorrect", expected);
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_START_QUOTE = new SimpleCommandExceptionType(Text.translatable("parsing.quote.expected.start"));
   private static final SimpleCommandExceptionType READER_EXPECTED_END_QUOTE = new SimpleCommandExceptionType(Text.translatable("parsing.quote.expected.end"));
   private static final DynamicCommandExceptionType READER_INVALID_ESCAPE = new DynamicCommandExceptionType((character) -> {
      return Text.translatable("parsing.quote.escape", character);
   });
   private static final DynamicCommandExceptionType READER_INVALID_BOOL = new DynamicCommandExceptionType((value) -> {
      return Text.translatable("parsing.bool.invalid", value);
   });
   private static final DynamicCommandExceptionType READER_INVALID_INT = new DynamicCommandExceptionType((value) -> {
      return Text.translatable("parsing.int.invalid", value);
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_INT = new SimpleCommandExceptionType(Text.translatable("parsing.int.expected"));
   private static final DynamicCommandExceptionType READER_INVALID_LONG = new DynamicCommandExceptionType((value) -> {
      return Text.translatable("parsing.long.invalid", value);
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_LONG = new SimpleCommandExceptionType(Text.translatable("parsing.long.expected"));
   private static final DynamicCommandExceptionType READER_INVALID_DOUBLE = new DynamicCommandExceptionType((value) -> {
      return Text.translatable("parsing.double.invalid", value);
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_DOUBLE = new SimpleCommandExceptionType(Text.translatable("parsing.double.expected"));
   private static final DynamicCommandExceptionType READER_INVALID_FLOAT = new DynamicCommandExceptionType((value) -> {
      return Text.translatable("parsing.float.invalid", value);
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_FLOAT = new SimpleCommandExceptionType(Text.translatable("parsing.float.expected"));
   private static final SimpleCommandExceptionType READER_EXPECTED_BOOL = new SimpleCommandExceptionType(Text.translatable("parsing.bool.expected"));
   private static final DynamicCommandExceptionType READER_EXPECTED_SYMBOL = new DynamicCommandExceptionType((symbol) -> {
      return Text.translatable("parsing.expected", symbol);
   });
   private static final SimpleCommandExceptionType DISPATCHER_UNKNOWN_COMMAND = new SimpleCommandExceptionType(Text.translatable("command.unknown.command"));
   private static final SimpleCommandExceptionType DISPATCHER_UNKNOWN_ARGUMENT = new SimpleCommandExceptionType(Text.translatable("command.unknown.argument"));
   private static final SimpleCommandExceptionType DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR = new SimpleCommandExceptionType(Text.translatable("command.expected.separator"));
   private static final DynamicCommandExceptionType DISPATCHER_PARSE_EXCEPTION = new DynamicCommandExceptionType((message) -> {
      return Text.translatable("command.exception", message);
   });

   public Dynamic2CommandExceptionType doubleTooLow() {
      return DOUBLE_TOO_LOW;
   }

   public Dynamic2CommandExceptionType doubleTooHigh() {
      return DOUBLE_TOO_HIGH;
   }

   public Dynamic2CommandExceptionType floatTooLow() {
      return FLOAT_TOO_LOW;
   }

   public Dynamic2CommandExceptionType floatTooHigh() {
      return FLOAT_TOO_HIGH;
   }

   public Dynamic2CommandExceptionType integerTooLow() {
      return INTEGER_TOO_LOW;
   }

   public Dynamic2CommandExceptionType integerTooHigh() {
      return INTEGER_TOO_HIGH;
   }

   public Dynamic2CommandExceptionType longTooLow() {
      return LONG_TOO_LOW;
   }

   public Dynamic2CommandExceptionType longTooHigh() {
      return LONG_TOO_HIGH;
   }

   public DynamicCommandExceptionType literalIncorrect() {
      return LITERAL_INCORRECT;
   }

   public SimpleCommandExceptionType readerExpectedStartOfQuote() {
      return READER_EXPECTED_START_QUOTE;
   }

   public SimpleCommandExceptionType readerExpectedEndOfQuote() {
      return READER_EXPECTED_END_QUOTE;
   }

   public DynamicCommandExceptionType readerInvalidEscape() {
      return READER_INVALID_ESCAPE;
   }

   public DynamicCommandExceptionType readerInvalidBool() {
      return READER_INVALID_BOOL;
   }

   public DynamicCommandExceptionType readerInvalidInt() {
      return READER_INVALID_INT;
   }

   public SimpleCommandExceptionType readerExpectedInt() {
      return READER_EXPECTED_INT;
   }

   public DynamicCommandExceptionType readerInvalidLong() {
      return READER_INVALID_LONG;
   }

   public SimpleCommandExceptionType readerExpectedLong() {
      return READER_EXPECTED_LONG;
   }

   public DynamicCommandExceptionType readerInvalidDouble() {
      return READER_INVALID_DOUBLE;
   }

   public SimpleCommandExceptionType readerExpectedDouble() {
      return READER_EXPECTED_DOUBLE;
   }

   public DynamicCommandExceptionType readerInvalidFloat() {
      return READER_INVALID_FLOAT;
   }

   public SimpleCommandExceptionType readerExpectedFloat() {
      return READER_EXPECTED_FLOAT;
   }

   public SimpleCommandExceptionType readerExpectedBool() {
      return READER_EXPECTED_BOOL;
   }

   public DynamicCommandExceptionType readerExpectedSymbol() {
      return READER_EXPECTED_SYMBOL;
   }

   public SimpleCommandExceptionType dispatcherUnknownCommand() {
      return DISPATCHER_UNKNOWN_COMMAND;
   }

   public SimpleCommandExceptionType dispatcherUnknownArgument() {
      return DISPATCHER_UNKNOWN_ARGUMENT;
   }

   public SimpleCommandExceptionType dispatcherExpectedArgumentSeparator() {
      return DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR;
   }

   public DynamicCommandExceptionType dispatcherParseException() {
      return DISPATCHER_PARSE_EXCEPTION;
   }
}
