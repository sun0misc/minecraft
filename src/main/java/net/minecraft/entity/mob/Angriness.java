/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import java.util.Arrays;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;

public enum Angriness {
    CALM(0, SoundEvents.ENTITY_WARDEN_AMBIENT, SoundEvents.ENTITY_WARDEN_LISTENING),
    AGITATED(40, SoundEvents.ENTITY_WARDEN_AGITATED, SoundEvents.ENTITY_WARDEN_LISTENING_ANGRY),
    ANGRY(80, SoundEvents.ENTITY_WARDEN_ANGRY, SoundEvents.ENTITY_WARDEN_LISTENING_ANGRY);

    private static final Angriness[] VALUES;
    private final int threshold;
    private final SoundEvent sound;
    private final SoundEvent listeningSound;

    private Angriness(int threshold, SoundEvent sound, SoundEvent listeningSound) {
        this.threshold = threshold;
        this.sound = sound;
        this.listeningSound = listeningSound;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public SoundEvent getSound() {
        return this.sound;
    }

    public SoundEvent getListeningSound() {
        return this.listeningSound;
    }

    public static Angriness getForAnger(int anger) {
        for (Angriness lv : VALUES) {
            if (anger < lv.threshold) continue;
            return lv;
        }
        return CALM;
    }

    public boolean isAngry() {
        return this == ANGRY;
    }

    static {
        VALUES = Util.make(Angriness.values(), values -> Arrays.sort(values, (a, b) -> Integer.compare(b.threshold, a.threshold)));
    }
}

