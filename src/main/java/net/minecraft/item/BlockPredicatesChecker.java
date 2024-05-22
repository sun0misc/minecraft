/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public class BlockPredicatesChecker {
    private static final Codec<BlockPredicatesChecker> SINGLE_CODEC = BlockPredicate.CODEC.flatComapMap(predicate -> new BlockPredicatesChecker(List.of(predicate), true), checker -> DataResult.error(() -> "Cannot encode"));
    private static final Codec<BlockPredicatesChecker> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.nonEmptyList(BlockPredicate.CODEC.listOf()).fieldOf("predicates")).forGetter(checker -> checker.predicates), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(BlockPredicatesChecker::showInTooltip)).apply((Applicative<BlockPredicatesChecker, ?>)instance, BlockPredicatesChecker::new));
    public static final Codec<BlockPredicatesChecker> CODEC = Codec.withAlternative(FULL_CODEC, SINGLE_CODEC);
    public static final PacketCodec<RegistryByteBuf, BlockPredicatesChecker> PACKET_CODEC = PacketCodec.tuple(BlockPredicate.PACKET_CODEC.collect(PacketCodecs.toList()), arg -> arg.predicates, PacketCodecs.BOOL, BlockPredicatesChecker::showInTooltip, BlockPredicatesChecker::new);
    public static final Text CAN_BREAK_TEXT = Text.translatable("item.canBreak").formatted(Formatting.GRAY);
    public static final Text CAN_PLACE_TEXT = Text.translatable("item.canPlace").formatted(Formatting.GRAY);
    private static final Text CAN_USE_UNKNOWN_TEXT = Text.translatable("item.canUse.unknown").formatted(Formatting.GRAY);
    private final List<BlockPredicate> predicates;
    private final boolean showInTooltip;
    private final List<Text> tooltipText;
    @Nullable
    private CachedBlockPosition cachedPos;
    private boolean lastResult;
    private boolean nbtAware;

    private BlockPredicatesChecker(List<BlockPredicate> predicates, boolean showInTooltip, List<Text> tooltipText) {
        this.predicates = predicates;
        this.showInTooltip = showInTooltip;
        this.tooltipText = tooltipText;
    }

    public BlockPredicatesChecker(List<BlockPredicate> predicates, boolean showInTooltip) {
        this.predicates = predicates;
        this.showInTooltip = showInTooltip;
        this.tooltipText = BlockPredicatesChecker.getTooltipText(predicates);
    }

    private static boolean canUseCache(CachedBlockPosition pos, @Nullable CachedBlockPosition cachedPos, boolean nbtAware) {
        if (cachedPos == null || pos.getBlockState() != cachedPos.getBlockState()) {
            return false;
        }
        if (!nbtAware) {
            return true;
        }
        if (pos.getBlockEntity() == null && cachedPos.getBlockEntity() == null) {
            return true;
        }
        if (pos.getBlockEntity() == null || cachedPos.getBlockEntity() == null) {
            return false;
        }
        DynamicRegistryManager lv = pos.getWorld().getRegistryManager();
        return Objects.equals(pos.getBlockEntity().createNbtWithId(lv), cachedPos.getBlockEntity().createNbtWithId(lv));
    }

    public boolean check(CachedBlockPosition cachedPos) {
        if (BlockPredicatesChecker.canUseCache(cachedPos, this.cachedPos, this.nbtAware)) {
            return this.lastResult;
        }
        this.cachedPos = cachedPos;
        this.nbtAware = false;
        for (BlockPredicate lv : this.predicates) {
            if (!lv.test(cachedPos)) continue;
            this.nbtAware |= lv.hasNbt();
            this.lastResult = true;
            return true;
        }
        this.lastResult = false;
        return false;
    }

    public void addTooltips(Consumer<Text> adder) {
        this.tooltipText.forEach(adder);
    }

    public BlockPredicatesChecker withShowInTooltip(boolean showInTooltip) {
        return new BlockPredicatesChecker(this.predicates, showInTooltip, this.tooltipText);
    }

    private static List<Text> getTooltipText(List<BlockPredicate> blockPredicates) {
        for (BlockPredicate lv : blockPredicates) {
            if (!lv.blocks().isEmpty()) continue;
            return List.of(CAN_USE_UNKNOWN_TEXT);
        }
        return blockPredicates.stream().flatMap(predicate -> predicate.blocks().orElseThrow().stream()).distinct().map(arg -> ((Block)arg.value()).getName().formatted(Formatting.DARK_GRAY)).toList();
    }

    public boolean showInTooltip() {
        return this.showInTooltip;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BlockPredicatesChecker) {
            BlockPredicatesChecker lv = (BlockPredicatesChecker)o;
            return this.predicates.equals(lv.predicates) && this.showInTooltip == lv.showInTooltip;
        }
        return false;
    }

    public int hashCode() {
        return this.predicates.hashCode() * 31 + (this.showInTooltip ? 1 : 0);
    }

    public String toString() {
        return "AdventureModePredicate{predicates=" + String.valueOf(this.predicates) + ", showInTooltip=" + this.showInTooltip + "}";
    }
}

