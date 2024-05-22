/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft;

import net.minecraft.block.BlockState;
import net.minecraft.class_9793;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class class_9794 {
    public static final int field_52032 = 20;
    private long field_52033;
    @Nullable
    private RegistryEntry<class_9793> field_52034;
    private final BlockPos field_52035;
    private final class_9795 field_52036;

    public class_9794(class_9795 arg, BlockPos arg2) {
        this.field_52036 = arg;
        this.field_52035 = arg2;
    }

    public boolean method_60754() {
        return this.field_52034 != null;
    }

    @Nullable
    public class_9793 method_60759() {
        if (this.field_52034 == null) {
            return null;
        }
        return this.field_52034.value();
    }

    public long method_60761() {
        return this.field_52033;
    }

    public void method_60758(RegistryEntry<class_9793> arg, long l) {
        if (arg.value().method_60751(l)) {
            return;
        }
        this.field_52034 = arg;
        this.field_52033 = l;
    }

    public int method_60762() {
        if (this.field_52034 != null) {
            return this.field_52034.value().comparatorOutput();
        }
        return 0;
    }

    public void method_60757(WorldAccess arg, RegistryEntry<class_9793> arg2) {
        this.field_52034 = arg2;
        this.field_52033 = 0L;
        int i = arg.getRegistryManager().get(RegistryKeys.JUKEBOX_SONG).getRawId(this.field_52034.value());
        arg.syncWorldEvent(null, WorldEvents.JUKEBOX_STARTS_PLAYING, this.field_52035, i);
        this.field_52036.notifyChange();
    }

    public void method_60755(WorldAccess arg, @Nullable BlockState arg2) {
        if (this.field_52034 == null) {
            return;
        }
        this.field_52034 = null;
        this.field_52033 = 0L;
        arg.emitGameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.field_52035, GameEvent.Emitter.of(arg2));
        arg.syncWorldEvent(WorldEvents.JUKEBOX_STOPS_PLAYING, this.field_52035, 0);
        this.field_52036.notifyChange();
    }

    public void method_60760(WorldAccess arg, @Nullable BlockState arg2) {
        if (this.field_52034 == null) {
            return;
        }
        if (this.field_52034.value().method_60751(this.field_52033)) {
            this.method_60755(arg, arg2);
            return;
        }
        if (this.method_60763()) {
            arg.emitGameEvent(GameEvent.JUKEBOX_PLAY, this.field_52035, GameEvent.Emitter.of(arg2));
            class_9794.method_60756(arg, this.field_52035);
        }
        ++this.field_52033;
    }

    private boolean method_60763() {
        return this.field_52033 % 20L == 0L;
    }

    private static void method_60756(WorldAccess arg, BlockPos arg2) {
        if (arg instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)arg;
            Vec3d lv2 = Vec3d.ofBottomCenter(arg2).add(0.0, 1.2f, 0.0);
            float f = (float)arg.getRandom().nextInt(4) / 24.0f;
            lv.spawnParticles(ParticleTypes.NOTE, lv2.getX(), lv2.getY(), lv2.getZ(), 0, f, 0.0, 0.0, 1.0);
        }
    }

    @FunctionalInterface
    public static interface class_9795 {
        public void notifyChange();
    }
}

