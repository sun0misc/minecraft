/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.shape;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class OffsetDoubleList
extends AbstractDoubleList {
    private final DoubleList oldList;
    private final double offset;

    public OffsetDoubleList(DoubleList oldList, double offset) {
        this.oldList = oldList;
        this.offset = offset;
    }

    @Override
    public double getDouble(int position) {
        return this.oldList.getDouble(position) + this.offset;
    }

    @Override
    public int size() {
        return this.oldList.size();
    }
}

