/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class SporeBlossomBlock
extends Block {
    public static final MapCodec<SporeBlossomBlock> CODEC = SporeBlossomBlock.createCodec(SporeBlossomBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 13.0, 2.0, 14.0, 16.0, 14.0);
    private static final int field_31252 = 14;
    private static final int field_31253 = 10;
    private static final int field_31254 = 10;

    public MapCodec<SporeBlossomBlock> getCodec() {
        return CODEC;
    }

    public SporeBlossomBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Block.sideCoversSmallSquare(world, pos.up(), Direction.DOWN) && !world.isWater(pos);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP && !this.canPlaceAt(state, world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        double d = (double)i + random.nextDouble();
        double e = (double)j + 0.7;
        double f = (double)k + random.nextDouble();
        world.addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, d, e, f, 0.0, 0.0, 0.0);
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int l = 0; l < 14; ++l) {
            lv.set(i + MathHelper.nextInt(random, -10, 10), j - random.nextInt(10), k + MathHelper.nextInt(random, -10, 10));
            BlockState lv2 = world.getBlockState(lv);
            if (lv2.isFullCube(world, lv)) continue;
            world.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, (double)lv.getX() + random.nextDouble(), (double)lv.getY() + random.nextDouble(), (double)lv.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}

