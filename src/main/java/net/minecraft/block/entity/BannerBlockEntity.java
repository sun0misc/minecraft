/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BannerBlockEntity
extends BlockEntity
implements Nameable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MAX_PATTERN_COUNT = 6;
    private static final String PATTERNS_KEY = "patterns";
    @Nullable
    private Text customName;
    private DyeColor baseColor;
    private BannerPatternsComponent patterns = BannerPatternsComponent.DEFAULT;

    public BannerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.BANNER, pos, state);
        this.baseColor = ((AbstractBannerBlock)state.getBlock()).getColor();
    }

    public BannerBlockEntity(BlockPos pos, BlockState state, DyeColor baseColor) {
        this(pos, state);
        this.baseColor = baseColor;
    }

    public void readFrom(ItemStack stack, DyeColor baseColor) {
        this.baseColor = baseColor;
        this.readComponents(stack);
    }

    @Override
    public Text getName() {
        if (this.customName != null) {
            return this.customName;
        }
        return Text.translatable("block.minecraft.banner");
    }

    @Override
    @Nullable
    public Text getCustomName() {
        return this.customName;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!this.patterns.equals(BannerPatternsComponent.DEFAULT)) {
            nbt.put(PATTERNS_KEY, BannerPatternsComponent.CODEC.encodeStart(registryLookup.getOps(NbtOps.INSTANCE), this.patterns).getOrThrow());
        }
        if (this.customName != null) {
            nbt.putString("CustomName", Text.Serialization.toJsonString(this.customName, registryLookup));
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("CustomName", NbtElement.STRING_TYPE)) {
            this.customName = BannerBlockEntity.tryParseCustomName(nbt.getString("CustomName"), registryLookup);
        }
        if (nbt.contains(PATTERNS_KEY)) {
            BannerPatternsComponent.CODEC.parse(registryLookup.getOps(NbtOps.INSTANCE), nbt.get(PATTERNS_KEY)).resultOrPartial(patterns -> LOGGER.error("Failed to parse banner patterns: '{}'", patterns)).ifPresent(patterns -> {
                this.patterns = patterns;
            });
        }
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return this.createNbt(registryLookup);
    }

    public BannerPatternsComponent getPatterns() {
        return this.patterns;
    }

    public ItemStack getPickStack() {
        ItemStack lv = new ItemStack(BannerBlock.getForColor(this.baseColor));
        lv.applyComponentsFrom(this.createComponentMap());
        return lv;
    }

    public DyeColor getColorForState() {
        return this.baseColor;
    }

    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        this.patterns = components.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
        this.customName = components.get(DataComponentTypes.CUSTOM_NAME);
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(DataComponentTypes.BANNER_PATTERNS, this.patterns);
        componentMapBuilder.add(DataComponentTypes.CUSTOM_NAME, this.customName);
    }

    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        nbt.remove(PATTERNS_KEY);
        nbt.remove("CustomName");
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

