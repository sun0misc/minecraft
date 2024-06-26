/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface ModifiableWorld {
    public boolean setBlockState(BlockPos var1, BlockState var2, int var3, int var4);

    default public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
        return this.setBlockState(pos, state, flags, 512);
    }

    public boolean removeBlock(BlockPos var1, boolean var2);

    default public boolean breakBlock(BlockPos pos, boolean drop) {
        return this.breakBlock(pos, drop, null);
    }

    default public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity) {
        return this.breakBlock(pos, drop, breakingEntity, 512);
    }

    public boolean breakBlock(BlockPos var1, boolean var2, @Nullable Entity var3, int var4);

    default public boolean spawnEntity(Entity entity) {
        return false;
    }
}

