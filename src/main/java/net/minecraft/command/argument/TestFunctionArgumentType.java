package net.minecraft.command.argument;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestFunctions;
import net.minecraft.text.Text;

public class TestFunctionArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("techtests.piston", "techtests");

   public TestFunction parse(StringReader stringReader) throws CommandSyntaxException {
      String string = stringReader.readUnquotedString();
      Optional optional = TestFunctions.getTestFunction(string);
      if (optional.isPresent()) {
         return (TestFunction)optional.get();
      } else {
         Message message = Text.literal("No such test: " + string);
         throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
      }
   }

   public static TestFunctionArgumentType testFunction() {
      return new TestFunctionArgumentType();
   }

   public static TestFunction getFunction(CommandContext context, String name) {
      return (TestFunction)context.getArgument(name, TestFunction.class);
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      Stream stream = TestFunctions.getTestFunctions().stream().map(TestFunction::getTemplatePath);
      return CommandSource.suggestMatching(stream, builder);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }
}
