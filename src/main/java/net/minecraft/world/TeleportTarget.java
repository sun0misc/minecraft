/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record TeleportTarget(ServerWorld newLevel, Vec3d pos, Vec3d velocity, float yaw, float pitch, boolean missingRespawnBlock) {
    public TeleportTarget(ServerWorld world, Vec3d pos, Vec3d velocity, float yaw, float pitch) {
        this(world, pos, velocity, yaw, pitch, false);
    }

    public TeleportTarget(ServerWorld world) {
        this(world, world.getSpawnPos().toCenterPos(), Vec3d.ZERO, 0.0f, 0.0f, false);
    }

    public static TeleportTarget missingSpawnBlock(ServerWorld world) {
        return new TeleportTarget(world, world.getSpawnPos().toCenterPos(), Vec3d.ZERO, 0.0f, 0.0f, true);
    }
}

