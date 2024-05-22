/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

public class BlockPredicateArgumentType
implements ArgumentType<BlockPredicate> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
    private final RegistryWrapper<Block> registryWrapper;

    public BlockPredicateArgumentType(CommandRegistryAccess commandRegistryAccess) {
        this.registryWrapper = commandRegistryAccess.getWrapperOrThrow(RegistryKeys.BLOCK);
    }

    public static BlockPredicateArgumentType blockPredicate(CommandRegistryAccess commandRegistryAccess) {
        return new BlockPredicateArgumentType(commandRegistryAccess);
    }

    @Override
    public BlockPredicate parse(StringReader stringReader) throws CommandSyntaxException {
        return BlockPredicateArgumentType.parse(this.registryWrapper, stringReader);
    }

    public static BlockPredicate parse(RegistryWrapper<Block> registryWrapper, StringReader reader) throws CommandSyntaxException {
        return BlockArgumentParser.blockOrTag(registryWrapper, reader, true).map(result -> new StatePredicate(result.blockState(), result.properties().keySet(), result.nbt()), result -> new TagPredicate(result.tag(), result.vagueProperties(), result.nbt()));
    }

    public static Predicate<CachedBlockPosition> getBlockPredicate(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, BlockPredicate.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return BlockArgumentParser.getSuggestions(this.registryWrapper, builder, true, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public static interface BlockPredicate
    extends Predicate<CachedBlockPosition> {
        public boolean hasNbt();
    }

    static class TagPredicate
    implements BlockPredicate {
        private final RegistryEntryList<Block> tag;
        @Nullable
        private final NbtCompound nbt;
        private final Map<String, String> properties;

        TagPredicate(RegistryEntryList<Block> tag, Map<String, String> properties, @Nullable NbtCompound nbt) {
            this.tag = tag;
            this.properties = properties;
            this.nbt = nbt;
        }

        @Override
        public boolean test(CachedBlockPosition arg) {
            BlockState lv = arg.getBlockState();
            if (!lv.isIn(this.tag)) {
                return false;
            }
            for (Map.Entry<String, String> entry : this.properties.entrySet()) {
                Property<?> lv2 = lv.getBlock().getStateManager().getProperty(entry.getKey());
                if (lv2 == null) {
                    return false;
                }
                Comparable comparable = lv2.parse(entry.getValue()).orElse(null);
                if (comparable == null) {
                    return false;
                }
                if (lv.get(lv2) == comparable) continue;
                return false;
            }
            if (this.nbt != null) {
                BlockEntity lv3 = arg.getBlockEntity();
                return lv3 != null && NbtHelper.matches(this.nbt, lv3.createNbtWithIdentifyingData(arg.getWorld().getRegistryManager()), true);
            }
            return true;
        }

        @Override
        public boolean hasNbt() {
            return this.nbt != null;
        }

        @Override
        public /* synthetic */ boolean test(Object context) {
            return this.test((CachedBlockPosition)context);
        }
    }

    static class StatePredicate
    implements BlockPredicate {
        private final BlockState state;
        private final Set<Property<?>> properties;
        @Nullable
        private final NbtCompound nbt;

        public StatePredicate(BlockState state, Set<Property<?>> properties, @Nullable NbtCompound nbt) {
            this.state = state;
            this.properties = properties;
            this.nbt = nbt;
        }

        @Override
        public boolean test(CachedBlockPosition arg) {
            BlockState lv = arg.getBlockState();
            if (!lv.isOf(this.state.getBlock())) {
                return false;
            }
            for (Property<?> lv2 : this.properties) {
                if (lv.get(lv2) == this.state.get(lv2)) continue;
                return false;
            }
            if (this.nbt != null) {
                BlockEntity lv3 = arg.getBlockEntity();
                return lv3 != null && NbtHelper.matches(this.nbt, lv3.createNbtWithIdentifyingData(arg.getWorld().getRegistryManager()), true);
            }
            return true;
        }

        @Override
        public boolean hasNbt() {
            return this.nbt != null;
        }

        @Override
        public /* synthetic */ boolean test(Object context) {
            return this.test((CachedBlockPosition)context);
        }
    }
}

