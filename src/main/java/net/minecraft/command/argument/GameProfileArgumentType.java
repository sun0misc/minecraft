package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GameProfileArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("Player", "0123", "dd12be42-52a9-4a91-a8a1-11c01849e498", "@e");
   public static final SimpleCommandExceptionType UNKNOWN_PLAYER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.player.unknown"));

   public static Collection getProfileArgument(CommandContext context, String name) throws CommandSyntaxException {
      return ((GameProfileArgument)context.getArgument(name, GameProfileArgument.class)).getNames((ServerCommandSource)context.getSource());
   }

   public static GameProfileArgumentType gameProfile() {
      return new GameProfileArgumentType();
   }

   public GameProfileArgument parse(StringReader stringReader) throws CommandSyntaxException {
      if (stringReader.canRead() && stringReader.peek() == '@') {
         EntitySelectorReader lv = new EntitySelectorReader(stringReader);
         EntitySelector lv2 = lv.read();
         if (lv2.includesNonPlayers()) {
            throw EntityArgumentType.PLAYER_SELECTOR_HAS_ENTITIES_EXCEPTION.create();
         } else {
            return new SelectorBacked(lv2);
         }
      } else {
         int i = stringReader.getCursor();

         while(stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
         }

         String string = stringReader.getString().substring(i, stringReader.getCursor());
         return (source) -> {
            Optional optional = source.getServer().getUserCache().findByName(string);
            SimpleCommandExceptionType var10001 = UNKNOWN_PLAYER_EXCEPTION;
            Objects.requireNonNull(var10001);
            return Collections.singleton((GameProfile)optional.orElseThrow(var10001::create));
         };
      }
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      if (context.getSource() instanceof CommandSource) {
         StringReader stringReader = new StringReader(builder.getInput());
         stringReader.setCursor(builder.getStart());
         EntitySelectorReader lv = new EntitySelectorReader(stringReader);

         try {
            lv.read();
         } catch (CommandSyntaxException var6) {
         }

         return lv.listSuggestions(builder, (builderx) -> {
            CommandSource.suggestMatching((Iterable)((CommandSource)context.getSource()).getPlayerNames(), builderx);
         });
      } else {
         return Suggestions.empty();
      }
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }

   @FunctionalInterface
   public interface GameProfileArgument {
      Collection getNames(ServerCommandSource source) throws CommandSyntaxException;
   }

   public static class SelectorBacked implements GameProfileArgument {
      private final EntitySelector selector;

      public SelectorBacked(EntitySelector selector) {
         this.selector = selector;
      }

      public Collection getNames(ServerCommandSource arg) throws CommandSyntaxException {
         List list = this.selector.getPlayers(arg);
         if (list.isEmpty()) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
         } else {
            List list2 = Lists.newArrayList();
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
               ServerPlayerEntity lv = (ServerPlayerEntity)var4.next();
               list2.add(lv.getGameProfile());
            }

            return list2;
         }
      }
   }
}
