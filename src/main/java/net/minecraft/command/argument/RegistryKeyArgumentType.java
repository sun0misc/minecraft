package net.minecraft.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RegistryKeyArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType INVALID_FEATURE_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("commands.place.feature.invalid", id);
   });
   private static final DynamicCommandExceptionType INVALID_STRUCTURE_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("commands.place.structure.invalid", id);
   });
   private static final DynamicCommandExceptionType INVALID_JIGSAW_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("commands.place.jigsaw.invalid", id);
   });
   final RegistryKey registryRef;

   public RegistryKeyArgumentType(RegistryKey registryRef) {
      this.registryRef = registryRef;
   }

   public static RegistryKeyArgumentType registryKey(RegistryKey registryRef) {
      return new RegistryKeyArgumentType(registryRef);
   }

   private static RegistryKey getKey(CommandContext context, String name, RegistryKey registryRef, DynamicCommandExceptionType invalidException) throws CommandSyntaxException {
      RegistryKey lv = (RegistryKey)context.getArgument(name, RegistryKey.class);
      Optional optional = lv.tryCast(registryRef);
      return (RegistryKey)optional.orElseThrow(() -> {
         return invalidException.create(lv);
      });
   }

   private static Registry getRegistry(CommandContext context, RegistryKey registryRef) {
      return ((ServerCommandSource)context.getSource()).getServer().getRegistryManager().get(registryRef);
   }

   private static RegistryEntry.Reference getRegistryEntry(CommandContext context, String name, RegistryKey registryRef, DynamicCommandExceptionType invalidException) throws CommandSyntaxException {
      RegistryKey lv = getKey(context, name, registryRef, invalidException);
      return (RegistryEntry.Reference)getRegistry(context, registryRef).getEntry(lv).orElseThrow(() -> {
         return invalidException.create(lv.getValue());
      });
   }

   public static RegistryEntry.Reference getConfiguredFeatureEntry(CommandContext context, String name) throws CommandSyntaxException {
      return getRegistryEntry(context, name, RegistryKeys.CONFIGURED_FEATURE, INVALID_FEATURE_EXCEPTION);
   }

   public static RegistryEntry.Reference getStructureEntry(CommandContext context, String name) throws CommandSyntaxException {
      return getRegistryEntry(context, name, RegistryKeys.STRUCTURE, INVALID_STRUCTURE_EXCEPTION);
   }

   public static RegistryEntry.Reference getStructurePoolEntry(CommandContext context, String name) throws CommandSyntaxException {
      return getRegistryEntry(context, name, RegistryKeys.TEMPLATE_POOL, INVALID_JIGSAW_EXCEPTION);
   }

   public RegistryKey parse(StringReader stringReader) throws CommandSyntaxException {
      Identifier lv = Identifier.fromCommandInput(stringReader);
      return RegistryKey.of(this.registryRef, lv);
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      Object var4 = context.getSource();
      if (var4 instanceof CommandSource lv) {
         return lv.listIdSuggestions(this.registryRef, CommandSource.SuggestedIdType.ELEMENTS, builder, context);
      } else {
         return builder.buildFuture();
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
      public void writePacket(Properties arg, PacketByteBuf arg2) {
         arg2.writeIdentifier(arg.registryRef.getValue());
      }

      public Properties fromPacket(PacketByteBuf arg) {
         Identifier lv = arg.readIdentifier();
         return new Properties(RegistryKey.ofRegistry(lv));
      }

      public void writeJson(Properties arg, JsonObject jsonObject) {
         jsonObject.addProperty("registry", arg.registryRef.getValue().toString());
      }

      public Properties getArgumentTypeProperties(RegistryKeyArgumentType arg) {
         return new Properties(arg.registryRef);
      }

      // $FF: synthetic method
      public ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
         return this.fromPacket(buf);
      }

      public final class Properties implements ArgumentSerializer.ArgumentTypeProperties {
         final RegistryKey registryRef;

         Properties(RegistryKey registryRef) {
            this.registryRef = registryRef;
         }

         public RegistryKeyArgumentType createType(CommandRegistryAccess arg) {
            return new RegistryKeyArgumentType(this.registryRef);
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
