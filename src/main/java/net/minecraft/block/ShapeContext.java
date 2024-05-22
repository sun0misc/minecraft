/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public interface ShapeContext {
    public static ShapeContext absent() {
        return EntityShapeContext.ABSENT;
    }

    public static ShapeContext of(Entity entity) {
        return new EntityShapeContext(entity);
    }

    public boolean isDescending();

    public boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3);

    public boolean isHolding(Item var1);

    public boolean canWalkOnFluid(FluidState var1, FluidState var2);
}

