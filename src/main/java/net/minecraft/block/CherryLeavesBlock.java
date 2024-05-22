/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CherryLeavesBlock
extends LeavesBlock {
    public static final MapCodec<CherryLeavesBlock> CODEC = CherryLeavesBlock.createCodec(CherryLeavesBlock::new);

    public MapCodec<CherryLeavesBlock> getCodec() {
        return CODEC;
    }

    public CherryLeavesBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (random.nextInt(10) != 0) {
            return;
        }
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        if (CherryLeavesBlock.isFaceFullSquare(lv2.getCollisionShape(world, lv), Direction.UP)) {
            return;
        }
        ParticleUtil.spawnParticle(world, pos, random, ParticleTypes.CHERRY_LEAVES);
    }
}

