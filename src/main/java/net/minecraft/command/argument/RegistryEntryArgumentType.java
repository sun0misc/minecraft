package net.minecraft.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RegistryEntryArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType NOT_SUMMONABLE_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("entity.not_summonable", id);
   });
   public static final Dynamic2CommandExceptionType NOT_FOUND_EXCEPTION = new Dynamic2CommandExceptionType((element, type) -> {
      return Text.translatable("argument.resource.not_found", element, type);
   });
   public static final Dynamic3CommandExceptionType INVALID_TYPE_EXCEPTION = new Dynamic3CommandExceptionType((element, type, expectedType) -> {
      return Text.translatable("argument.resource.invalid_type", element, type, expectedType);
   });
   final RegistryKey registryRef;
   private final RegistryWrapper registryWrapper;

   public RegistryEntryArgumentType(CommandRegistryAccess registryAccess, RegistryKey registryRef) {
      this.registryRef = registryRef;
      this.registryWrapper = registryAccess.createWrapper(registryRef);
   }

   public static RegistryEntryArgumentType registryEntry(CommandRegistryAccess registryAccess, RegistryKey registryRef) {
      return new RegistryEntryArgumentType(registryAccess, registryRef);
   }

   public static RegistryEntry.Reference getRegistryEntry(CommandContext context, String name, RegistryKey registryRef) throws CommandSyntaxException {
      RegistryEntry.Reference lv = (RegistryEntry.Reference)context.getArgument(name, RegistryEntry.Reference.class);
      RegistryKey lv2 = lv.registryKey();
      if (lv2.isOf(registryRef)) {
         return lv;
      } else {
         throw INVALID_TYPE_EXCEPTION.create(lv2.getValue(), lv2.getRegistry(), registryRef.getValue());
      }
   }

   public static RegistryEntry.Reference getEntityAttribute(CommandContext context, String name) throws CommandSyntaxException {
      return getRegistryEntry(context, name, RegistryKeys.ATTRIBUTE);
   }

   public static RegistryEntry.Reference getConfiguredFeature(CommandContext context, String name) throws CommandSyntaxException {
      return getRegistryEntry(context, name, RegistryKeys.CONFIGURED_FEATURE);
   }

   public static RegistryEntry.Reference getStructure(CommandContext context, String name) throws CommandSyntaxException {
      return getRegistryEntry(context, name, RegistryKeys.STRUCTURE);
   }

   public static RegistryEntry.Reference getEntityType(CommandContext context, String name) throws CommandSyntaxException {
      return getRegistryEntry(context, name, RegistryKeys.ENTITY_TYPE);
   }

   public static RegistryEntry.Reference getSummonableEntityType(CommandContext context, String name) throws CommandSyntaxException {
      RegistryEntry.Reference lv = getRegistryEntry(context, name, RegistryKeys.ENTITY_TYPE);
      if (!((EntityType)lv.value()).isSummonable()) {
         throw NOT_SUMMONABLE_EXCEPTION.create(lv.registryKey().getValue().toString());
      } else {
         return lv;
      }
   }

   public static RegistryEntry.Reference getStatusEffect(CommandContext context, String name) throws CommandSyntaxException {
      return getRegistryEntry(context, name, RegistryKeys.STATUS_EFFECT);
   }

   public static RegistryEntry.Reference getEnchantment(CommandContext context, String name) throws CommandSyntaxException {
      return getRegistryEntry(context, name, RegistryKeys.ENCHANTMENT);
   }

   public RegistryEntry.Reference parse(StringReader stringReader) throws CommandSyntaxException {
      Identifier lv = Identifier.fromCommandInput(stringReader);
      RegistryKey lv2 = RegistryKey.of(this.registryRef, lv);
      return (RegistryEntry.Reference)this.registryWrapper.getOptional(lv2).orElseThrow(() -> {
         return NOT_FOUND_EXCEPTION.create(lv, this.registryRef.getValue());
      });
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return CommandSource.suggestIdentifiers(this.registryWrapper.streamKeys().map(RegistryKey::getValue), builder);
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

      public Properties getArgumentTypeProperties(RegistryEntryArgumentType arg) {
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

         public RegistryEntryArgumentType createType(CommandRegistryAccess arg) {
            return new RegistryEntryArgumentType(arg, this.registryRef);
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
