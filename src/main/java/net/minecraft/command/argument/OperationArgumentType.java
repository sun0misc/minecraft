package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class OperationArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("=", ">", "<");
   private static final SimpleCommandExceptionType INVALID_OPERATION = new SimpleCommandExceptionType(Text.translatable("arguments.operation.invalid"));
   private static final SimpleCommandExceptionType DIVISION_ZERO_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.operation.div0"));

   public static OperationArgumentType operation() {
      return new OperationArgumentType();
   }

   public static Operation getOperation(CommandContext context, String name) {
      return (Operation)context.getArgument(name, Operation.class);
   }

   public Operation parse(StringReader stringReader) throws CommandSyntaxException {
      if (!stringReader.canRead()) {
         throw INVALID_OPERATION.create();
      } else {
         int i = stringReader.getCursor();

         while(stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
         }

         return getOperator(stringReader.getString().substring(i, stringReader.getCursor()));
      }
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, builder);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   private static Operation getOperator(String operator) throws CommandSyntaxException {
      return (Operation)(operator.equals("><") ? (a, b) -> {
         int i = a.getScore();
         a.setScore(b.getScore());
         b.setScore(i);
      } : getIntOperator(operator));
   }

   private static IntOperator getIntOperator(String operator) throws CommandSyntaxException {
      switch (operator) {
         case "=":
            return (a, b) -> {
               return b;
            };
         case "+=":
            return (a, b) -> {
               return a + b;
            };
         case "-=":
            return (a, b) -> {
               return a - b;
            };
         case "*=":
            return (a, b) -> {
               return a * b;
            };
         case "/=":
            return (a, b) -> {
               if (b == 0) {
                  throw DIVISION_ZERO_EXCEPTION.create();
               } else {
                  return MathHelper.floorDiv(a, b);
               }
            };
         case "%=":
            return (a, b) -> {
               if (b == 0) {
                  throw DIVISION_ZERO_EXCEPTION.create();
               } else {
                  return MathHelper.floorMod(a, b);
               }
            };
         case "<":
            return Math::min;
         case ">":
            return Math::max;
         default:
            throw INVALID_OPERATION.create();
      }
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }

   @FunctionalInterface
   public interface Operation {
      void apply(ScoreboardPlayerScore a, ScoreboardPlayerScore b) throws CommandSyntaxException;
   }

   @FunctionalInterface
   private interface IntOperator extends Operation {
      int apply(int a, int b) throws CommandSyntaxException;

      default void apply(ScoreboardPlayerScore arg, ScoreboardPlayerScore arg2) throws CommandSyntaxException {
         arg.setScore(this.apply(arg.getScore(), arg2.getScore()));
      }
   }
}
