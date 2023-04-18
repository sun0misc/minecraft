package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ItemStringReader {
   private static final SimpleCommandExceptionType TAG_DISALLOWED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.item.tag.disallowed"));
   private static final DynamicCommandExceptionType ID_INVALID_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("argument.item.id.invalid", id);
   });
   private static final DynamicCommandExceptionType UNKNOWN_TAG_EXCEPTION = new DynamicCommandExceptionType((tag) -> {
      return Text.translatable("arguments.item.tag.unknown", tag);
   });
   private static final char LEFT_CURLY_BRACKET = '{';
   private static final char HASH_SIGN = '#';
   private static final Function NBT_SUGGESTION_PROVIDER = SuggestionsBuilder::buildFuture;
   private final RegistryWrapper registryWrapper;
   private final StringReader reader;
   private final boolean allowTag;
   private Either result;
   @Nullable
   private NbtCompound nbt;
   private Function suggestions;

   private ItemStringReader(RegistryWrapper registryWrapper, StringReader reader, boolean allowTag) {
      this.suggestions = NBT_SUGGESTION_PROVIDER;
      this.registryWrapper = registryWrapper;
      this.reader = reader;
      this.allowTag = allowTag;
   }

   public static ItemResult item(RegistryWrapper registryWrapper, StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();

      try {
         ItemStringReader lv = new ItemStringReader(registryWrapper, reader, false);
         lv.consume();
         RegistryEntry lv2 = (RegistryEntry)lv.result.left().orElseThrow(() -> {
            return new IllegalStateException("Parser returned unexpected tag name");
         });
         return new ItemResult(lv2, lv.nbt);
      } catch (CommandSyntaxException var5) {
         reader.setCursor(i);
         throw var5;
      }
   }

   public static Either itemOrTag(RegistryWrapper registryWrapper, StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();

      try {
         ItemStringReader lv = new ItemStringReader(registryWrapper, reader, true);
         lv.consume();
         return lv.result.mapBoth((item) -> {
            return new ItemResult(item, lv.nbt);
         }, (tag) -> {
            return new TagResult(tag, lv.nbt);
         });
      } catch (CommandSyntaxException var4) {
         reader.setCursor(i);
         throw var4;
      }
   }

   public static CompletableFuture getSuggestions(RegistryWrapper registryWrapper, SuggestionsBuilder builder, boolean allowTag) {
      StringReader stringReader = new StringReader(builder.getInput());
      stringReader.setCursor(builder.getStart());
      ItemStringReader lv = new ItemStringReader(registryWrapper, stringReader, allowTag);

      try {
         lv.consume();
      } catch (CommandSyntaxException var6) {
      }

      return (CompletableFuture)lv.suggestions.apply(builder.createOffset(stringReader.getCursor()));
   }

   private void readItem() throws CommandSyntaxException {
      int i = this.reader.getCursor();
      Identifier lv = Identifier.fromCommandInput(this.reader);
      Optional optional = this.registryWrapper.getOptional(RegistryKey.of(RegistryKeys.ITEM, lv));
      this.result = Either.left((RegistryEntry)optional.orElseThrow(() -> {
         this.reader.setCursor(i);
         return ID_INVALID_EXCEPTION.createWithContext(this.reader, lv);
      }));
   }

   private void readTag() throws CommandSyntaxException {
      if (!this.allowTag) {
         throw TAG_DISALLOWED_EXCEPTION.createWithContext(this.reader);
      } else {
         int i = this.reader.getCursor();
         this.reader.expect('#');
         this.suggestions = this::suggestTag;
         Identifier lv = Identifier.fromCommandInput(this.reader);
         Optional optional = this.registryWrapper.getOptional(TagKey.of(RegistryKeys.ITEM, lv));
         this.result = Either.right((RegistryEntryList)optional.orElseThrow(() -> {
            this.reader.setCursor(i);
            return UNKNOWN_TAG_EXCEPTION.createWithContext(this.reader, lv);
         }));
      }
   }

   private void readNbt() throws CommandSyntaxException {
      this.nbt = (new StringNbtReader(this.reader)).parseCompound();
   }

   private void consume() throws CommandSyntaxException {
      if (this.allowTag) {
         this.suggestions = this::suggestItemOrTagId;
      } else {
         this.suggestions = this::suggestItemId;
      }

      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.readTag();
      } else {
         this.readItem();
      }

      this.suggestions = this::suggestItem;
      if (this.reader.canRead() && this.reader.peek() == '{') {
         this.suggestions = NBT_SUGGESTION_PROVIDER;
         this.readNbt();
      }

   }

   private CompletableFuture suggestItem(SuggestionsBuilder builder) {
      if (builder.getRemaining().isEmpty()) {
         builder.suggest(String.valueOf('{'));
      }

      return builder.buildFuture();
   }

   private CompletableFuture suggestTag(SuggestionsBuilder builder) {
      return CommandSource.suggestIdentifiers(this.registryWrapper.streamTagKeys().map(TagKey::id), builder, String.valueOf('#'));
   }

   private CompletableFuture suggestItemId(SuggestionsBuilder builder) {
      return CommandSource.suggestIdentifiers(this.registryWrapper.streamKeys().map(RegistryKey::getValue), builder);
   }

   private CompletableFuture suggestItemOrTagId(SuggestionsBuilder builder) {
      this.suggestTag(builder);
      return this.suggestItemId(builder);
   }

   public static record ItemResult(RegistryEntry item, @Nullable NbtCompound nbt) {
      public ItemResult(RegistryEntry arg, @Nullable NbtCompound arg2) {
         this.item = arg;
         this.nbt = arg2;
      }

      public RegistryEntry item() {
         return this.item;
      }

      @Nullable
      public NbtCompound nbt() {
         return this.nbt;
      }
   }

   public static record TagResult(RegistryEntryList tag, @Nullable NbtCompound nbt) {
      public TagResult(RegistryEntryList arg, @Nullable NbtCompound arg2) {
         this.tag = arg;
         this.nbt = arg2;
      }

      public RegistryEntryList tag() {
         return this.tag;
      }

      @Nullable
      public NbtCompound nbt() {
         return this.nbt;
      }
   }
}
