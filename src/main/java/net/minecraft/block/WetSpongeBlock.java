/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class WetSpongeBlock
extends Block {
    public static final MapCodec<WetSpongeBlock> CODEC = WetSpongeBlock.createCodec(WetSpongeBlock::new);

    public MapCodec<WetSpongeBlock> getCodec() {
        return CODEC;
    }

    protected WetSpongeBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (world.getDimension().ultrawarm()) {
            world.setBlockState(pos, Blocks.SPONGE.getDefaultState(), Block.NOTIFY_ALL);
            world.syncWorldEvent(WorldEvents.WET_SPONGE_DRIES_OUT, pos, 0);
            world.playSound(null, pos, SoundEvents.BLOCK_WET_SPONGE_DRIES, SoundCategory.BLOCKS, 1.0f, (1.0f + world.getRandom().nextFloat() * 0.2f) * 0.7f);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Direction lv = Direction.random(random);
        if (lv == Direction.UP) {
            return;
        }
        BlockPos lv2 = pos.offset(lv);
        BlockState lv3 = world.getBlockState(lv2);
        if (state.isOpaque() && lv3.isSideSolidFullSquare(world, lv2, lv.getOpposite())) {
            return;
        }
        double d = pos.getX();
        double e = pos.getY();
        double f = pos.getZ();
        if (lv == Direction.DOWN) {
            e -= 0.05;
            d += random.nextDouble();
            f += random.nextDouble();
        } else {
            e += random.nextDouble() * 0.8;
            if (lv.getAxis() == Direction.Axis.X) {
                f += random.nextDouble();
                d = lv == Direction.EAST ? (d += 1.1) : (d += 0.05);
            } else {
                d += random.nextDouble();
                f = lv == Direction.SOUTH ? (f += 1.1) : (f += 0.05);
            }
        }
        world.addParticle(ParticleTypes.DRIPPING_WATER, d, e, f, 0.0, 0.0, 0.0);
    }
}

