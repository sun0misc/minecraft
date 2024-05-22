/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.command.argument;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BlockArgumentParser {
    public static final SimpleCommandExceptionType DISALLOWED_TAG_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.block.tag.disallowed"));
    public static final DynamicCommandExceptionType INVALID_BLOCK_ID_EXCEPTION = new DynamicCommandExceptionType(block -> Text.stringifiedTranslatable("argument.block.id.invalid", block));
    public static final Dynamic2CommandExceptionType UNKNOWN_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((block, property) -> Text.stringifiedTranslatable("argument.block.property.unknown", block, property));
    public static final Dynamic2CommandExceptionType DUPLICATE_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((block, property) -> Text.stringifiedTranslatable("argument.block.property.duplicate", property, block));
    public static final Dynamic3CommandExceptionType INVALID_PROPERTY_EXCEPTION = new Dynamic3CommandExceptionType((block, property, value) -> Text.stringifiedTranslatable("argument.block.property.invalid", block, value, property));
    public static final Dynamic2CommandExceptionType EMPTY_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((block, property) -> Text.stringifiedTranslatable("argument.block.property.novalue", block, property));
    public static final SimpleCommandExceptionType UNCLOSED_PROPERTIES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.block.property.unclosed"));
    public static final DynamicCommandExceptionType UNKNOWN_BLOCK_TAG_EXCEPTION = new DynamicCommandExceptionType(tag -> Text.stringifiedTranslatable("arguments.block.tag.unknown", tag));
    private static final char PROPERTIES_OPENING = '[';
    private static final char NBT_OPENING = '{';
    private static final char PROPERTIES_CLOSING = ']';
    private static final char PROPERTY_DEFINER = '=';
    private static final char PROPERTY_SEPARATOR = ',';
    private static final char TAG_PREFIX = '#';
    private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_DEFAULT = SuggestionsBuilder::buildFuture;
    private final RegistryWrapper<Block> registryWrapper;
    private final StringReader reader;
    private final boolean allowTag;
    private final boolean allowSnbt;
    private final Map<Property<?>, Comparable<?>> blockProperties = Maps.newHashMap();
    private final Map<String, String> tagProperties = Maps.newHashMap();
    private Identifier blockId = Identifier.method_60656("");
    @Nullable
    private StateManager<Block, BlockState> stateFactory;
    @Nullable
    private BlockState blockState;
    @Nullable
    private NbtCompound data;
    @Nullable
    private RegistryEntryList<Block> tagId;
    private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_DEFAULT;

    private BlockArgumentParser(RegistryWrapper<Block> registryWrapper, StringReader reader, boolean allowTag, boolean allowSnbt) {
        this.registryWrapper = registryWrapper;
        this.reader = reader;
        this.allowTag = allowTag;
        this.allowSnbt = allowSnbt;
    }

    public static BlockResult block(RegistryWrapper<Block> registryWrapper, String string, boolean allowSnbt) throws CommandSyntaxException {
        return BlockArgumentParser.block(registryWrapper, new StringReader(string), allowSnbt);
    }

    public static BlockResult block(RegistryWrapper<Block> registryWrapper, StringReader reader, boolean allowSnbt) throws CommandSyntaxException {
        int i = reader.getCursor();
        try {
            BlockArgumentParser lv = new BlockArgumentParser(registryWrapper, reader, false, allowSnbt);
            lv.parse();
            return new BlockResult(lv.blockState, lv.blockProperties, lv.data);
        } catch (CommandSyntaxException commandSyntaxException) {
            reader.setCursor(i);
            throw commandSyntaxException;
        }
    }

    public static Either<BlockResult, TagResult> blockOrTag(RegistryWrapper<Block> registryWrapper, String string, boolean allowSnbt) throws CommandSyntaxException {
        return BlockArgumentParser.blockOrTag(registryWrapper, new StringReader(string), allowSnbt);
    }

    public static Either<BlockResult, TagResult> blockOrTag(RegistryWrapper<Block> registryWrapper, StringReader reader, boolean allowSnbt) throws CommandSyntaxException {
        int i = reader.getCursor();
        try {
            BlockArgumentParser lv = new BlockArgumentParser(registryWrapper, reader, true, allowSnbt);
            lv.parse();
            if (lv.tagId != null) {
                return Either.right(new TagResult(lv.tagId, lv.tagProperties, lv.data));
            }
            return Either.left(new BlockResult(lv.blockState, lv.blockProperties, lv.data));
        } catch (CommandSyntaxException commandSyntaxException) {
            reader.setCursor(i);
            throw commandSyntaxException;
        }
    }

    public static CompletableFuture<Suggestions> getSuggestions(RegistryWrapper<Block> registryWrapper, SuggestionsBuilder builder, boolean allowTag, boolean allowSnbt) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        BlockArgumentParser lv = new BlockArgumentParser(registryWrapper, stringReader, allowTag, allowSnbt);
        try {
            lv.parse();
        } catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return lv.suggestions.apply(builder.createOffset(stringReader.getCursor()));
    }

    private void parse() throws CommandSyntaxException {
        this.suggestions = this.allowTag ? this::suggestBlockOrTagId : this::suggestBlockId;
        if (this.reader.canRead() && this.reader.peek() == '#') {
            this.parseTagId();
            this.suggestions = this::suggestSnbtOrTagProperties;
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.parseTagProperties();
                this.suggestions = this::suggestSnbt;
            }
        } else {
            this.parseBlockId();
            this.suggestions = this::suggestSnbtOrBlockProperties;
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.parseBlockProperties();
                this.suggestions = this::suggestSnbt;
            }
        }
        if (this.allowSnbt && this.reader.canRead() && this.reader.peek() == '{') {
            this.suggestions = SUGGEST_DEFAULT;
            this.parseSnbt();
        }
    }

    private CompletableFuture<Suggestions> suggestBlockPropertiesOrEnd(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            builder.suggest(String.valueOf(']'));
        }
        return this.suggestBlockProperties(builder);
    }

    private CompletableFuture<Suggestions> suggestTagPropertiesOrEnd(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            builder.suggest(String.valueOf(']'));
        }
        return this.suggestTagProperties(builder);
    }

    private CompletableFuture<Suggestions> suggestBlockProperties(SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (Property<?> lv : this.blockState.getProperties()) {
            if (this.blockProperties.containsKey(lv) || !lv.getName().startsWith(string)) continue;
            builder.suggest(lv.getName() + "=");
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTagProperties(SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        if (this.tagId != null) {
            for (RegistryEntry registryEntry : this.tagId) {
                for (Property<?> lv2 : ((Block)registryEntry.value()).getStateManager().getProperties()) {
                    if (this.tagProperties.containsKey(lv2.getName()) || !lv2.getName().startsWith(string)) continue;
                    builder.suggest(lv2.getName() + "=");
                }
            }
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestSnbt(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty() && this.hasBlockEntity()) {
            builder.suggest(String.valueOf('{'));
        }
        return builder.buildFuture();
    }

    private boolean hasBlockEntity() {
        if (this.blockState != null) {
            return this.blockState.hasBlockEntity();
        }
        if (this.tagId != null) {
            for (RegistryEntry registryEntry : this.tagId) {
                if (!((Block)registryEntry.value()).getDefaultState().hasBlockEntity()) continue;
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Suggestions> suggestEqualsCharacter(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            builder.suggest(String.valueOf('='));
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestCommaOrEnd(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            builder.suggest(String.valueOf(']'));
        }
        if (builder.getRemaining().isEmpty() && this.blockProperties.size() < this.blockState.getProperties().size()) {
            builder.suggest(String.valueOf(','));
        }
        return builder.buildFuture();
    }

    private static <T extends Comparable<T>> SuggestionsBuilder suggestPropertyValues(SuggestionsBuilder builder, Property<T> property) {
        for (Comparable comparable : property.getValues()) {
            if (comparable instanceof Integer) {
                Integer integer = (Integer)comparable;
                builder.suggest(integer);
                continue;
            }
            builder.suggest(property.name(comparable));
        }
        return builder;
    }

    private CompletableFuture<Suggestions> suggestTagPropertyValues(SuggestionsBuilder builder, String name) {
        boolean bl = false;
        if (this.tagId != null) {
            block0: for (RegistryEntry registryEntry : this.tagId) {
                Block lv2 = (Block)registryEntry.value();
                Property<?> lv3 = lv2.getStateManager().getProperty(name);
                if (lv3 != null) {
                    BlockArgumentParser.suggestPropertyValues(builder, lv3);
                }
                if (bl) continue;
                for (Property<?> lv4 : lv2.getStateManager().getProperties()) {
                    if (this.tagProperties.containsKey(lv4.getName())) continue;
                    bl = true;
                    continue block0;
                }
            }
        }
        if (bl) {
            builder.suggest(String.valueOf(','));
        }
        builder.suggest(String.valueOf(']'));
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestSnbtOrTagProperties(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty() && this.tagId != null) {
            RegistryEntry lv;
            Block lv2;
            boolean bl = false;
            boolean bl2 = false;
            Iterator iterator = this.tagId.iterator();
            while (!(!iterator.hasNext() || (bl |= !(lv2 = (Block)(lv = (RegistryEntry)iterator.next()).value()).getStateManager().getProperties().isEmpty()) && (bl2 |= lv2.getDefaultState().hasBlockEntity()))) {
            }
            if (bl) {
                builder.suggest(String.valueOf('['));
            }
            if (bl2) {
                builder.suggest(String.valueOf('{'));
            }
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestSnbtOrBlockProperties(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            if (!this.stateFactory.getProperties().isEmpty()) {
                builder.suggest(String.valueOf('['));
            }
            if (this.blockState.hasBlockEntity()) {
                builder.suggest(String.valueOf('{'));
            }
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestIdentifiers(SuggestionsBuilder builder) {
        return CommandSource.suggestIdentifiers(this.registryWrapper.streamTagKeys().map(TagKey::id), builder, String.valueOf('#'));
    }

    private CompletableFuture<Suggestions> suggestBlockId(SuggestionsBuilder builder) {
        return CommandSource.suggestIdentifiers(this.registryWrapper.streamKeys().map(RegistryKey::getValue), builder);
    }

    private CompletableFuture<Suggestions> suggestBlockOrTagId(SuggestionsBuilder builder) {
        this.suggestIdentifiers(builder);
        this.suggestBlockId(builder);
        return builder.buildFuture();
    }

    private void parseBlockId() throws CommandSyntaxException {
        int i = this.reader.getCursor();
        this.blockId = Identifier.fromCommandInput(this.reader);
        Block lv = this.registryWrapper.getOptional(RegistryKey.of(RegistryKeys.BLOCK, this.blockId)).orElseThrow(() -> {
            this.reader.setCursor(i);
            return INVALID_BLOCK_ID_EXCEPTION.createWithContext(this.reader, this.blockId.toString());
        }).value();
        this.stateFactory = lv.getStateManager();
        this.blockState = lv.getDefaultState();
    }

    private void parseTagId() throws CommandSyntaxException {
        if (!this.allowTag) {
            throw DISALLOWED_TAG_EXCEPTION.createWithContext(this.reader);
        }
        int i = this.reader.getCursor();
        this.reader.expect('#');
        this.suggestions = this::suggestIdentifiers;
        Identifier lv = Identifier.fromCommandInput(this.reader);
        this.tagId = this.registryWrapper.getOptional(TagKey.of(RegistryKeys.BLOCK, lv)).orElseThrow(() -> {
            this.reader.setCursor(i);
            return UNKNOWN_BLOCK_TAG_EXCEPTION.createWithContext(this.reader, lv.toString());
        });
    }

    private void parseBlockProperties() throws CommandSyntaxException {
        this.reader.skip();
        this.suggestions = this::suggestBlockPropertiesOrEnd;
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int i = this.reader.getCursor();
            String string = this.reader.readString();
            Property<?> lv = this.stateFactory.getProperty(string);
            if (lv == null) {
                this.reader.setCursor(i);
                throw UNKNOWN_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            if (this.blockProperties.containsKey(lv)) {
                this.reader.setCursor(i);
                throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            this.reader.skipWhitespace();
            this.suggestions = this::suggestEqualsCharacter;
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = builder -> BlockArgumentParser.suggestPropertyValues(builder, lv).buildFuture();
            int j = this.reader.getCursor();
            this.parsePropertyValue(lv, this.reader.readString(), j);
            this.suggestions = this::suggestCommaOrEnd;
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) continue;
            if (this.reader.peek() == ',') {
                this.reader.skip();
                this.suggestions = this::suggestBlockProperties;
                continue;
            }
            if (this.reader.peek() == ']') break;
            throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
        }
        if (!this.reader.canRead()) {
            throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
        }
        this.reader.skip();
    }

    private void parseTagProperties() throws CommandSyntaxException {
        this.reader.skip();
        this.suggestions = this::suggestTagPropertiesOrEnd;
        int i = -1;
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int j = this.reader.getCursor();
            String string = this.reader.readString();
            if (this.tagProperties.containsKey(string)) {
                this.reader.setCursor(j);
                throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                this.reader.setCursor(j);
                throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), string);
            }
            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = builder -> this.suggestTagPropertyValues((SuggestionsBuilder)builder, string);
            i = this.reader.getCursor();
            String string2 = this.reader.readString();
            this.tagProperties.put(string, string2);
            this.reader.skipWhitespace();
            if (!this.reader.canRead()) continue;
            i = -1;
            if (this.reader.peek() == ',') {
                this.reader.skip();
                this.suggestions = this::suggestTagProperties;
                continue;
            }
            if (this.reader.peek() == ']') break;
            throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
        }
        if (!this.reader.canRead()) {
            if (i >= 0) {
                this.reader.setCursor(i);
            }
            throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
        }
        this.reader.skip();
    }

    private void parseSnbt() throws CommandSyntaxException {
        this.data = new StringNbtReader(this.reader).parseCompound();
    }

    private <T extends Comparable<T>> void parsePropertyValue(Property<T> property, String value, int cursor) throws CommandSyntaxException {
        Optional<T> optional = property.parse(value);
        if (!optional.isPresent()) {
            this.reader.setCursor(cursor);
            throw INVALID_PROPERTY_EXCEPTION.createWithContext(this.reader, this.blockId.toString(), property.getName(), value);
        }
        this.blockState = (BlockState)this.blockState.with(property, (Comparable)optional.get());
        this.blockProperties.put(property, (Comparable)optional.get());
    }

    public static String stringifyBlockState(BlockState state) {
        StringBuilder stringBuilder = new StringBuilder(state.getRegistryEntry().getKey().map(key -> key.getValue().toString()).orElse("air"));
        if (!state.getProperties().isEmpty()) {
            stringBuilder.append('[');
            boolean bl = false;
            for (Map.Entry<Property<?>, Comparable<?>> entry : state.getEntries().entrySet()) {
                if (bl) {
                    stringBuilder.append(',');
                }
                BlockArgumentParser.stringifyProperty(stringBuilder, entry.getKey(), entry.getValue());
                bl = true;
            }
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    private static <T extends Comparable<T>> void stringifyProperty(StringBuilder builder, Property<T> property, Comparable<?> value) {
        builder.append(property.getName());
        builder.append('=');
        builder.append(property.name(value));
    }

    public record BlockResult(BlockState blockState, Map<Property<?>, Comparable<?>> properties, @Nullable NbtCompound nbt) {
        @Nullable
        public NbtCompound nbt() {
            return this.nbt;
        }
    }

    public record TagResult(RegistryEntryList<Block> tag, Map<String, String> vagueProperties, @Nullable NbtCompound nbt) {
        @Nullable
        public NbtCompound nbt() {
            return this.nbt;
        }
    }
}

