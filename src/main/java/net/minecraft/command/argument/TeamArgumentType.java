package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TeamArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("foo", "123");
   private static final DynamicCommandExceptionType UNKNOWN_TEAM_EXCEPTION = new DynamicCommandExceptionType((name) -> {
      return Text.translatable("team.notFound", name);
   });

   public static TeamArgumentType team() {
      return new TeamArgumentType();
   }

   public static Team getTeam(CommandContext context, String name) throws CommandSyntaxException {
      String string2 = (String)context.getArgument(name, String.class);
      Scoreboard lv = ((ServerCommandSource)context.getSource()).getServer().getScoreboard();
      Team lv2 = lv.getTeam(string2);
      if (lv2 == null) {
         throw UNKNOWN_TEAM_EXCEPTION.create(string2);
      } else {
         return lv2;
      }
   }

   public String parse(StringReader stringReader) throws CommandSyntaxException {
      return stringReader.readUnquotedString();
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return context.getSource() instanceof CommandSource ? CommandSource.suggestMatching((Iterable)((CommandSource)context.getSource()).getTeamNames(), builder) : Suggestions.empty();
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }
}
