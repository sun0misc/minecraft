package net.minecraft.command.argument;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class EntityArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("Player", "0123", "@e", "@e[type=foo]", "dd12be42-52a9-4a91-a8a1-11c01849e498");
   public static final SimpleCommandExceptionType TOO_MANY_ENTITIES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.toomany"));
   public static final SimpleCommandExceptionType TOO_MANY_PLAYERS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.player.toomany"));
   public static final SimpleCommandExceptionType PLAYER_SELECTOR_HAS_ENTITIES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.player.entities"));
   public static final SimpleCommandExceptionType ENTITY_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.notfound.entity"));
   public static final SimpleCommandExceptionType PLAYER_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.notfound.player"));
   public static final SimpleCommandExceptionType NOT_ALLOWED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.selector.not_allowed"));
   final boolean singleTarget;
   final boolean playersOnly;

   protected EntityArgumentType(boolean singleTarget, boolean playersOnly) {
      this.singleTarget = singleTarget;
      this.playersOnly = playersOnly;
   }

   public static EntityArgumentType entity() {
      return new EntityArgumentType(true, false);
   }

   public static Entity getEntity(CommandContext context, String name) throws CommandSyntaxException {
      return ((EntitySelector)context.getArgument(name, EntitySelector.class)).getEntity((ServerCommandSource)context.getSource());
   }

   public static EntityArgumentType entities() {
      return new EntityArgumentType(false, false);
   }

   public static Collection getEntities(CommandContext context, String name) throws CommandSyntaxException {
      Collection collection = getOptionalEntities(context, name);
      if (collection.isEmpty()) {
         throw ENTITY_NOT_FOUND_EXCEPTION.create();
      } else {
         return collection;
      }
   }

   public static Collection getOptionalEntities(CommandContext context, String name) throws CommandSyntaxException {
      return ((EntitySelector)context.getArgument(name, EntitySelector.class)).getEntities((ServerCommandSource)context.getSource());
   }

   public static Collection getOptionalPlayers(CommandContext context, String name) throws CommandSyntaxException {
      return ((EntitySelector)context.getArgument(name, EntitySelector.class)).getPlayers((ServerCommandSource)context.getSource());
   }

   public static EntityArgumentType player() {
      return new EntityArgumentType(true, true);
   }

   public static ServerPlayerEntity getPlayer(CommandContext context, String name) throws CommandSyntaxException {
      return ((EntitySelector)context.getArgument(name, EntitySelector.class)).getPlayer((ServerCommandSource)context.getSource());
   }

   public static EntityArgumentType players() {
      return new EntityArgumentType(false, true);
   }

   public static Collection getPlayers(CommandContext context, String name) throws CommandSyntaxException {
      List list = ((EntitySelector)context.getArgument(name, EntitySelector.class)).getPlayers((ServerCommandSource)context.getSource());
      if (list.isEmpty()) {
         throw PLAYER_NOT_FOUND_EXCEPTION.create();
      } else {
         return list;
      }
   }

   public EntitySelector parse(StringReader stringReader) throws CommandSyntaxException {
      int i = false;
      EntitySelectorReader lv = new EntitySelectorReader(stringReader);
      EntitySelector lv2 = lv.read();
      if (lv2.getLimit() > 1 && this.singleTarget) {
         if (this.playersOnly) {
            stringReader.setCursor(0);
            throw TOO_MANY_PLAYERS_EXCEPTION.createWithContext(stringReader);
         } else {
            stringReader.setCursor(0);
            throw TOO_MANY_ENTITIES_EXCEPTION.createWithContext(stringReader);
         }
      } else if (lv2.includesNonPlayers() && this.playersOnly && !lv2.isSenderOnly()) {
         stringReader.setCursor(0);
         throw PLAYER_SELECTOR_HAS_ENTITIES_EXCEPTION.createWithContext(stringReader);
      } else {
         return lv2;
      }
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      Object var4 = context.getSource();
      if (var4 instanceof CommandSource lv) {
         StringReader stringReader = new StringReader(builder.getInput());
         stringReader.setCursor(builder.getStart());
         EntitySelectorReader lv2 = new EntitySelectorReader(stringReader, lv.hasPermissionLevel(2));

         try {
            lv2.read();
         } catch (CommandSyntaxException var7) {
         }

         return lv2.listSuggestions(builder, (builderx) -> {
            Collection collection = lv.getPlayerNames();
            Iterable iterable = this.playersOnly ? collection : Iterables.concat(collection, lv.getEntitySuggestions());
            CommandSource.suggestMatching((Iterable)iterable, builderx);
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

   public static class Serializer implements ArgumentSerializer {
      private static final byte SINGLE_FLAG = 1;
      private static final byte PLAYERS_ONLY_FLAG = 2;

      public void writePacket(Properties arg, PacketByteBuf arg2) {
         int i = 0;
         if (arg.single) {
            i |= 1;
         }

         if (arg.playersOnly) {
            i |= 2;
         }

         arg2.writeByte(i);
      }

      public Properties fromPacket(PacketByteBuf arg) {
         byte b = arg.readByte();
         return new Properties((b & 1) != 0, (b & 2) != 0);
      }

      public void writeJson(Properties arg, JsonObject jsonObject) {
         jsonObject.addProperty("amount", arg.single ? "single" : "multiple");
         jsonObject.addProperty("type", arg.playersOnly ? "players" : "entities");
      }

      public Properties getArgumentTypeProperties(EntityArgumentType arg) {
         return new Properties(arg.singleTarget, arg.playersOnly);
      }

      // $FF: synthetic method
      public ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
         return this.fromPacket(buf);
      }

      public final class Properties implements ArgumentSerializer.ArgumentTypeProperties {
         final boolean single;
         final boolean playersOnly;

         Properties(boolean single, boolean playersOnly) {
            this.single = single;
            this.playersOnly = playersOnly;
         }

         public EntityArgumentType createType(CommandRegistryAccess arg) {
            return new EntityArgumentType(this.single, this.playersOnly);
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
