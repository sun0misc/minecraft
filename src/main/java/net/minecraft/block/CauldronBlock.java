/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

public class CauldronBlock
extends AbstractCauldronBlock {
    public static final MapCodec<CauldronBlock> CODEC = CauldronBlock.createCodec(CauldronBlock::new);
    private static final float FILL_WITH_RAIN_CHANCE = 0.05f;
    private static final float FILL_WITH_SNOW_CHANCE = 0.1f;

    public MapCodec<CauldronBlock> getCodec() {
        return CODEC;
    }

    public CauldronBlock(AbstractBlock.Settings arg) {
        super(arg, CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR);
    }

    @Override
    public boolean isFull(BlockState state) {
        return false;
    }

    protected static boolean canFillWithPrecipitation(World world, Biome.Precipitation precipitation) {
        if (precipitation == Biome.Precipitation.RAIN) {
            return world.getRandom().nextFloat() < 0.05f;
        }
        if (precipitation == Biome.Precipitation.SNOW) {
            return world.getRandom().nextFloat() < 0.1f;
        }
        return false;
    }

    @Override
    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
        if (!CauldronBlock.canFillWithPrecipitation(world, precipitation)) {
            return;
        }
        if (precipitation == Biome.Precipitation.RAIN) {
            world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState());
            world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        } else if (precipitation == Biome.Precipitation.SNOW) {
            world.setBlockState(pos, Blocks.POWDER_SNOW_CAULDRON.getDefaultState());
            world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    @Override
    protected boolean canBeFilledByDripstone(Fluid fluid) {
        return true;
    }

    @Override
    protected void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid) {
        if (fluid == Fluids.WATER) {
            BlockState lv = Blocks.WATER_CAULDRON.getDefaultState();
            world.setBlockState(pos, lv);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(lv));
            world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS_WATER_INTO_CAULDRON, pos, 0);
        } else if (fluid == Fluids.LAVA) {
            BlockState lv = Blocks.LAVA_CAULDRON.getDefaultState();
            world.setBlockState(pos, lv);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(lv));
            world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS_LAVA_INTO_CAULDRON, pos, 0);
        }
    }
}

