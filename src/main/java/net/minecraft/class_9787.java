/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft;

import net.minecraft.class_9797;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;

public class class_9787 {
    private class_9797 field_51998;
    private BlockPos field_51999;
    private int field_52000;
    private boolean field_52001;

    public class_9787(class_9797 arg, BlockPos arg2) {
        this.field_51998 = arg;
        this.field_51999 = arg2;
        this.field_52001 = true;
    }

    public boolean method_60702(ServerWorld arg, Entity arg2, boolean bl) {
        if (this.field_52001) {
            this.field_52001 = false;
            return bl && this.field_52000++ >= this.field_51998.method_60772(arg, arg2);
        }
        this.method_60710();
        return false;
    }

    @Nullable
    public TeleportTarget method_60701(ServerWorld arg, Entity arg2) {
        return this.field_51998.method_60770(arg, arg2, this.field_51999);
    }

    public class_9797.class_9798 method_60700() {
        return this.field_51998.method_60778();
    }

    private void method_60710() {
        this.field_52000 = Math.max(this.field_52000 - 4, 0);
    }

    public boolean method_60706() {
        return this.field_52000 <= 0;
    }

    public BlockPos method_60707() {
        return this.field_51999;
    }

    public void method_60704(BlockPos arg) {
        this.field_51999 = arg;
    }

    public int method_60708() {
        return this.field_52000;
    }

    public boolean method_60709() {
        return this.field_52001;
    }

    public void method_60705(boolean bl) {
        this.field_52001 = bl;
    }

    public boolean method_60703(class_9797 arg) {
        return this.field_51998 == arg;
    }
}

