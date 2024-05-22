/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EntityView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public interface RegistryWorldView
extends EntityView,
WorldView,
ModifiableTestableWorld {
    @Override
    default public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        return WorldView.super.getBlockEntity(pos, type);
    }

    @Override
    default public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box) {
        return EntityView.super.getEntityCollisions(entity, box);
    }

    @Override
    default public boolean doesNotIntersectEntities(@Nullable Entity except, VoxelShape shape) {
        return EntityView.super.doesNotIntersectEntities(except, shape);
    }

    @Override
    default public BlockPos getTopPosition(Heightmap.Type heightmap, BlockPos pos) {
        return WorldView.super.getTopPosition(heightmap, pos);
    }
}

