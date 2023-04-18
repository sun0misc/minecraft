package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class Vec3ArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "0.1 -0.5 .9", "~0.5 ~1 ~-5");
   public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos3d.incomplete"));
   public static final SimpleCommandExceptionType MIXED_COORDINATE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.mixed"));
   private final boolean centerIntegers;

   public Vec3ArgumentType(boolean centerIntegers) {
      this.centerIntegers = centerIntegers;
   }

   public static Vec3ArgumentType vec3() {
      return new Vec3ArgumentType(true);
   }

   public static Vec3ArgumentType vec3(boolean centerIntegers) {
      return new Vec3ArgumentType(centerIntegers);
   }

   public static Vec3d getVec3(CommandContext context, String name) {
      return ((PosArgument)context.getArgument(name, PosArgument.class)).toAbsolutePos((ServerCommandSource)context.getSource());
   }

   public static PosArgument getPosArgument(CommandContext context, String name) {
      return (PosArgument)context.getArgument(name, PosArgument.class);
   }

   public PosArgument parse(StringReader stringReader) throws CommandSyntaxException {
      return (PosArgument)(stringReader.canRead() && stringReader.peek() == '^' ? LookingPosArgument.parse(stringReader) : DefaultPosArgument.parse(stringReader, this.centerIntegers));
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      if (!(context.getSource() instanceof CommandSource)) {
         return Suggestions.empty();
      } else {
         String string = builder.getRemaining();
         Object collection;
         if (!string.isEmpty() && string.charAt(0) == '^') {
            collection = Collections.singleton(CommandSource.RelativePosition.ZERO_LOCAL);
         } else {
            collection = ((CommandSource)context.getSource()).getPositionSuggestions();
         }

         return CommandSource.suggestPositions(string, (Collection)collection, builder, CommandManager.getCommandValidator(this::parse));
      }
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }
}
