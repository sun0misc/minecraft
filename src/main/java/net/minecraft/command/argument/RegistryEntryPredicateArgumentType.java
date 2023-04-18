package net.minecraft.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RegistryEntryPredicateArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
   private static final Dynamic2CommandExceptionType NOT_FOUND_EXCEPTION = new Dynamic2CommandExceptionType((tag, type) -> {
      return Text.translatable("argument.resource_tag.not_found", tag, type);
   });
   private static final Dynamic3CommandExceptionType WRONG_TYPE_EXCEPTION = new Dynamic3CommandExceptionType((tag, type, expectedType) -> {
      return Text.translatable("argument.resource_tag.invalid_type", tag, type, expectedType);
   });
   private final RegistryWrapper registryWrapper;
   final RegistryKey registryRef;

   public RegistryEntryPredicateArgumentType(CommandRegistryAccess registryAccess, RegistryKey registryRef) {
      this.registryRef = registryRef;
      this.registryWrapper = registryAccess.createWrapper(registryRef);
   }

   public static RegistryEntryPredicateArgumentType registryEntryPredicate(CommandRegistryAccess registryRef, RegistryKey registryAccess) {
      return new RegistryEntryPredicateArgumentType(registryRef, registryAccess);
   }

   public static EntryPredicate getRegistryEntryPredicate(CommandContext context, String name, RegistryKey registryRef) throws CommandSyntaxException {
      EntryPredicate lv = (EntryPredicate)context.getArgument(name, EntryPredicate.class);
      Optional optional = lv.tryCast(registryRef);
      return (EntryPredicate)optional.orElseThrow(() -> {
         return (CommandSyntaxException)lv.getEntry().map((entry) -> {
            RegistryKey lv = entry.registryKey();
            return RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(lv.getValue(), lv.getRegistry(), registryRef.getValue());
         }, (entryList) -> {
            TagKey lv = entryList.getTag();
            return WRONG_TYPE_EXCEPTION.create(lv.id(), lv.registry(), registryRef.getValue());
         });
      });
   }

   public EntryPredicate parse(StringReader stringReader) throws CommandSyntaxException {
      if (stringReader.canRead() && stringReader.peek() == '#') {
         int i = stringReader.getCursor();

         try {
            stringReader.skip();
            Identifier lv = Identifier.fromCommandInput(stringReader);
            TagKey lv2 = TagKey.of(this.registryRef, lv);
            RegistryEntryList.Named lv3 = (RegistryEntryList.Named)this.registryWrapper.getOptional(lv2).orElseThrow(() -> {
               return NOT_FOUND_EXCEPTION.create(lv, this.registryRef.getValue());
            });
            return new TagBased(lv3);
         } catch (CommandSyntaxException var6) {
            stringReader.setCursor(i);
            throw var6;
         }
      } else {
         Identifier lv4 = Identifier.fromCommandInput(stringReader);
         RegistryKey lv5 = RegistryKey.of(this.registryRef, lv4);
         RegistryEntry.Reference lv6 = (RegistryEntry.Reference)this.registryWrapper.getOptional(lv5).orElseThrow(() -> {
            return RegistryEntryArgumentType.NOT_FOUND_EXCEPTION.create(lv4, this.registryRef.getValue());
         });
         return new EntryBased(lv6);
      }
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      CommandSource.suggestIdentifiers(this.registryWrapper.streamTagKeys().map(TagKey::id), builder, "#");
      return CommandSource.suggestIdentifiers(this.registryWrapper.streamKeys().map(RegistryKey::getValue), builder);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }

   public interface EntryPredicate extends Predicate {
      Either getEntry();

      Optional tryCast(RegistryKey registryRef);

      String asString();
   }

   private static record TagBased(RegistryEntryList.Named tag) implements EntryPredicate {
      TagBased(RegistryEntryList.Named arg) {
         this.tag = arg;
      }

      public Either getEntry() {
         return Either.right(this.tag);
      }

      public Optional tryCast(RegistryKey registryRef) {
         return this.tag.getTag().isOf(registryRef) ? Optional.of(this) : Optional.empty();
      }

      public boolean test(RegistryEntry arg) {
         return this.tag.contains(arg);
      }

      public String asString() {
         return "#" + this.tag.getTag().id();
      }

      public RegistryEntryList.Named tag() {
         return this.tag;
      }

      // $FF: synthetic method
      public boolean test(Object entry) {
         return this.test((RegistryEntry)entry);
      }
   }

   private static record EntryBased(RegistryEntry.Reference value) implements EntryPredicate {
      EntryBased(RegistryEntry.Reference arg) {
         this.value = arg;
      }

      public Either getEntry() {
         return Either.left(this.value);
      }

      public Optional tryCast(RegistryKey registryRef) {
         return this.value.registryKey().isOf(registryRef) ? Optional.of(this) : Optional.empty();
      }

      public boolean test(RegistryEntry arg) {
         return arg.equals(this.value);
      }

      public String asString() {
         return this.value.registryKey().getValue().toString();
      }

      public RegistryEntry.Reference value() {
         return this.value;
      }

      // $FF: synthetic method
      public boolean test(Object entry) {
         return this.test((RegistryEntry)entry);
      }
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

      public Properties getArgumentTypeProperties(RegistryEntryPredicateArgumentType arg) {
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

         public RegistryEntryPredicateArgumentType createType(CommandRegistryAccess arg) {
            return new RegistryEntryPredicateArgumentType(arg, this.registryRef);
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
