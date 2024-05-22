/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.color.block;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockColors {
    private static final int NO_COLOR = -1;
    private final IdList<BlockColorProvider> providers = new IdList(32);
    private final Map<Block, Set<Property<?>>> properties = Maps.newHashMap();

    public static BlockColors create() {
        BlockColors lv = new BlockColors();
        lv.registerColorProvider((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return GrassColors.getDefaultColor();
            }
            return BiomeColors.getGrassColor(world, state.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos);
        }, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
        lv.registerColorProperty(TallPlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
        lv.registerColorProvider((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return GrassColors.getDefaultColor();
            }
            return BiomeColors.getGrassColor(world, pos);
        }, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.SHORT_GRASS, Blocks.POTTED_FERN);
        lv.registerColorProvider((state, world, pos, tintIndex) -> {
            if (tintIndex != 0) {
                if (world == null || pos == null) {
                    return GrassColors.getDefaultColor();
                }
                return BiomeColors.getGrassColor(world, pos);
            }
            return -1;
        }, Blocks.PINK_PETALS);
        lv.registerColorProvider((state, world, pos, tintIndex) -> FoliageColors.getSpruceColor(), Blocks.SPRUCE_LEAVES);
        lv.registerColorProvider((state, world, pos, tintIndex) -> FoliageColors.getBirchColor(), Blocks.BIRCH_LEAVES);
        lv.registerColorProvider((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return FoliageColors.getDefaultColor();
            }
            return BiomeColors.getFoliageColor(world, pos);
        }, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE, Blocks.MANGROVE_LEAVES);
        lv.registerColorProvider((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return -1;
            }
            return BiomeColors.getWaterColor(world, pos);
        }, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.WATER_CAULDRON);
        lv.registerColorProvider((state, world, pos, tintIndex) -> RedstoneWireBlock.getWireColor(state.get(RedstoneWireBlock.POWER)), Blocks.REDSTONE_WIRE);
        lv.registerColorProperty(RedstoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
        lv.registerColorProvider((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return -1;
            }
            return BiomeColors.getGrassColor(world, pos);
        }, Blocks.SUGAR_CANE);
        lv.registerColorProvider((state, world, pos, tintIndex) -> -2046180, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        lv.registerColorProvider((state, world, pos, tintIndex) -> {
            int j = state.get(StemBlock.AGE);
            return ColorHelper.Argb.getArgb(j * 32, 255 - j * 8, j * 4);
        }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        lv.registerColorProperty(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        lv.registerColorProvider((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return -9321636;
            }
            return -14647248;
        }, Blocks.LILY_PAD);
        return lv;
    }

    public int getParticleColor(BlockState state, World world, BlockPos pos) {
        BlockColorProvider lv = this.providers.get(Registries.BLOCK.getRawId(state.getBlock()));
        if (lv != null) {
            return lv.getColor(state, null, null, 0);
        }
        MapColor lv2 = state.getMapColor(world, pos);
        return lv2 != null ? lv2.color : -1;
    }

    public int getColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex) {
        BlockColorProvider lv = this.providers.get(Registries.BLOCK.getRawId(state.getBlock()));
        return lv == null ? -1 : lv.getColor(state, world, pos, tintIndex);
    }

    public void registerColorProvider(BlockColorProvider provider, Block ... blocks) {
        for (Block lv : blocks) {
            this.providers.set(provider, Registries.BLOCK.getRawId(lv));
        }
    }

    private void registerColorProperties(Set<Property<?>> properties, Block ... blocks) {
        for (Block lv : blocks) {
            this.properties.put(lv, properties);
        }
    }

    private void registerColorProperty(Property<?> property, Block ... blocks) {
        this.registerColorProperties(ImmutableSet.of(property), blocks);
    }

    public Set<Property<?>> getProperties(Block block) {
        return this.properties.getOrDefault(block, ImmutableSet.of());
    }
}

