/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface PosArgument {
    public Vec3d toAbsolutePos(ServerCommandSource var1);

    public Vec2f toAbsoluteRotation(ServerCommandSource var1);

    default public BlockPos toAbsoluteBlockPos(ServerCommandSource source) {
        return BlockPos.ofFloored(this.toAbsolutePos(source));
    }

    public boolean isXRelative();

    public boolean isYRelative();

    public boolean isZRelative();
}

