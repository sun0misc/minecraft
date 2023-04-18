package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ScoreHolderArgumentType implements ArgumentType {
   public static final SuggestionProvider SUGGESTION_PROVIDER = (context, builder) -> {
      StringReader stringReader = new StringReader(builder.getInput());
      stringReader.setCursor(builder.getStart());
      EntitySelectorReader lv = new EntitySelectorReader(stringReader);

      try {
         lv.read();
      } catch (CommandSyntaxException var5) {
      }

      return lv.listSuggestions(builder, (builderx) -> {
         CommandSource.suggestMatching((Iterable)((ServerCommandSource)context.getSource()).getPlayerNames(), builderx);
      });
   };
   private static final Collection EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
   private static final SimpleCommandExceptionType EMPTY_SCORE_HOLDER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.scoreHolder.empty"));
   final boolean multiple;

   public ScoreHolderArgumentType(boolean multiple) {
      this.multiple = multiple;
   }

   public static String getScoreHolder(CommandContext context, String name) throws CommandSyntaxException {
      return (String)getScoreHolders(context, name).iterator().next();
   }

   public static Collection getScoreHolders(CommandContext context, String name) throws CommandSyntaxException {
      return getScoreHolders(context, name, Collections::emptyList);
   }

   public static Collection getScoreboardScoreHolders(CommandContext context, String name) throws CommandSyntaxException {
      ServerScoreboard var10002 = ((ServerCommandSource)context.getSource()).getServer().getScoreboard();
      Objects.requireNonNull(var10002);
      return getScoreHolders(context, name, var10002::getKnownPlayers);
   }

   public static Collection getScoreHolders(CommandContext context, String name, Supplier players) throws CommandSyntaxException {
      Collection collection = ((ScoreHolder)context.getArgument(name, ScoreHolder.class)).getNames((ServerCommandSource)context.getSource(), players);
      if (collection.isEmpty()) {
         throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
      } else {
         return collection;
      }
   }

   public static ScoreHolderArgumentType scoreHolder() {
      return new ScoreHolderArgumentType(false);
   }

   public static ScoreHolderArgumentType scoreHolders() {
      return new ScoreHolderArgumentType(true);
   }

   public ScoreHolder parse(StringReader stringReader) throws CommandSyntaxException {
      if (stringReader.canRead() && stringReader.peek() == '@') {
         EntitySelectorReader lv = new EntitySelectorReader(stringReader);
         EntitySelector lv2 = lv.read();
         if (!this.multiple && lv2.getLimit() > 1) {
            throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
         } else {
            return new SelectorScoreHolder(lv2);
         }
      } else {
         int i = stringReader.getCursor();

         while(stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
         }

         String string = stringReader.getString().substring(i, stringReader.getCursor());
         if (string.equals("*")) {
            return (source, players) -> {
               Collection collection = (Collection)players.get();
               if (collection.isEmpty()) {
                  throw EMPTY_SCORE_HOLDER_EXCEPTION.create();
               } else {
                  return collection;
               }
            };
         } else {
            Collection collection = Collections.singleton(string);
            return (source, players) -> {
               return collection;
            };
         }
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
   public interface ScoreHolder {
      Collection getNames(ServerCommandSource source, Supplier players) throws CommandSyntaxException;
   }

   public static class SelectorScoreHolder implements ScoreHolder {
      private final EntitySelector selector;

      public SelectorScoreHolder(EntitySelector selector) {
         this.selector = selector;
      }

      public Collection getNames(ServerCommandSource arg, Supplier supplier) throws CommandSyntaxException {
         List list = this.selector.getEntities(arg);
         if (list.isEmpty()) {
            throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
         } else {
            List list2 = Lists.newArrayList();
            Iterator var5 = list.iterator();

            while(var5.hasNext()) {
               Entity lv = (Entity)var5.next();
               list2.add(lv.getEntityName());
            }

            return list2;
         }
      }
   }

   public static class Serializer implements ArgumentSerializer {
      private static final byte MULTIPLE_FLAG = 1;

      public void writePacket(Properties arg, PacketByteBuf arg2) {
         int i = 0;
         if (arg.multiple) {
            i |= 1;
         }

         arg2.writeByte(i);
      }

      public Properties fromPacket(PacketByteBuf arg) {
         byte b = arg.readByte();
         boolean bl = (b & 1) != 0;
         return new Properties(bl);
      }

      public void writeJson(Properties arg, JsonObject jsonObject) {
         jsonObject.addProperty("amount", arg.multiple ? "multiple" : "single");
      }

      public Properties getArgumentTypeProperties(ScoreHolderArgumentType arg) {
         return new Properties(arg.multiple);
      }

      // $FF: synthetic method
      public ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
         return this.fromPacket(buf);
      }

      public final class Properties implements ArgumentSerializer.ArgumentTypeProperties {
         final boolean multiple;

         Properties(boolean multiple) {
            this.multiple = multiple;
         }

         public ScoreHolderArgumentType createType(CommandRegistryAccess arg) {
            return new ScoreHolderArgumentType(this.multiple);
         }

         public ArgumentSerializer getSerializer() {
            return Serializer.this;
         }

         // $FF: synthetic method
         public ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
            return this.createType(commandRegistryAccess);
         }
      }
   }
}
