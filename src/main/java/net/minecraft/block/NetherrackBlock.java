/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class NetherrackBlock
extends Block
implements Fertilizable {
    public static final MapCodec<NetherrackBlock> CODEC = NetherrackBlock.createCodec(NetherrackBlock::new);

    public MapCodec<NetherrackBlock> getCodec() {
        return CODEC;
    }

    public NetherrackBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        if (!world.getBlockState(pos.up()).isTransparent(world, pos)) {
            return false;
        }
        for (BlockPos lv : BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            if (!world.getBlockState(lv).isIn(BlockTags.NYLIUM)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        boolean bl = false;
        boolean bl2 = false;
        for (BlockPos lv : BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            BlockState lv2 = world.getBlockState(lv);
            if (lv2.isOf(Blocks.WARPED_NYLIUM)) {
                bl2 = true;
            }
            if (lv2.isOf(Blocks.CRIMSON_NYLIUM)) {
                bl = true;
            }
            if (!bl2 || !bl) continue;
            break;
        }
        if (bl2 && bl) {
            world.setBlockState(pos, random.nextBoolean() ? Blocks.WARPED_NYLIUM.getDefaultState() : Blocks.CRIMSON_NYLIUM.getDefaultState(), Block.NOTIFY_ALL);
        } else if (bl2) {
            world.setBlockState(pos, Blocks.WARPED_NYLIUM.getDefaultState(), Block.NOTIFY_ALL);
        } else if (bl) {
            world.setBlockState(pos, Blocks.CRIMSON_NYLIUM.getDefaultState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    public Fertilizable.FertilizableType getFertilizableType() {
        return Fertilizable.FertilizableType.NEIGHBOR_SPREADER;
    }
}

