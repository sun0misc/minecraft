package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.text.Text;

public class ScoreboardSlotArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("sidebar", "foo.bar");
   public static final DynamicCommandExceptionType INVALID_SLOT_EXCEPTION = new DynamicCommandExceptionType((name) -> {
      return Text.translatable("argument.scoreboardDisplaySlot.invalid", name);
   });

   private ScoreboardSlotArgumentType() {
   }

   public static ScoreboardSlotArgumentType scoreboardSlot() {
      return new ScoreboardSlotArgumentType();
   }

   public static int getScoreboardSlot(CommandContext context, String name) {
      return (Integer)context.getArgument(name, Integer.class);
   }

   public Integer parse(StringReader stringReader) throws CommandSyntaxException {
      String string = stringReader.readUnquotedString();
      int i = Scoreboard.getDisplaySlotId(string);
      if (i == -1) {
         throw INVALID_SLOT_EXCEPTION.create(string);
      } else {
         return i;
      }
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(Scoreboard.getDisplaySlotNames(), builder);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }
}
