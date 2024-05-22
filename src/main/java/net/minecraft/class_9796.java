/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft;

import net.minecraft.class_9793;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface class_9796 {
    public static final RegistryKey<class_9793> field_52037 = class_9796.method_60764("13");
    public static final RegistryKey<class_9793> CAT = class_9796.method_60764("cat");
    public static final RegistryKey<class_9793> BLOCKS = class_9796.method_60764("blocks");
    public static final RegistryKey<class_9793> CHIRP = class_9796.method_60764("chirp");
    public static final RegistryKey<class_9793> FAR = class_9796.method_60764("far");
    public static final RegistryKey<class_9793> MALL = class_9796.method_60764("mall");
    public static final RegistryKey<class_9793> MELLOHI = class_9796.method_60764("mellohi");
    public static final RegistryKey<class_9793> STAL = class_9796.method_60764("stal");
    public static final RegistryKey<class_9793> STRAD = class_9796.method_60764("strad");
    public static final RegistryKey<class_9793> WARD = class_9796.method_60764("ward");
    public static final RegistryKey<class_9793> field_52047 = class_9796.method_60764("11");
    public static final RegistryKey<class_9793> WAIT = class_9796.method_60764("wait");
    public static final RegistryKey<class_9793> PIGSTEP = class_9796.method_60764("pigstep");
    public static final RegistryKey<class_9793> OTHERSIDE = class_9796.method_60764("otherside");
    public static final RegistryKey<class_9793> field_52051 = class_9796.method_60764("5");
    public static final RegistryKey<class_9793> RELIC = class_9796.method_60764("relic");
    public static final RegistryKey<class_9793> PRECIPICE = class_9796.method_60764("precipice");
    public static final RegistryKey<class_9793> CREATOR = class_9796.method_60764("creator");
    public static final RegistryKey<class_9793> CREATOR_MUSIC_BOX = class_9796.method_60764("creator_music_box");

    private static RegistryKey<class_9793> method_60764(String string) {
        return RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.method_60656(string));
    }

    private static void method_60766(Registerable<class_9793> arg, RegistryKey<class_9793> arg2, RegistryEntry.Reference<SoundEvent> arg3, int i, int j) {
        arg.register(arg2, new class_9793(arg3, Text.translatable(Util.createTranslationKey("jukebox_song", arg2.getValue())), i, j));
    }

    public static void method_60765(Registerable<class_9793> arg) {
        class_9796.method_60766(arg, field_52037, SoundEvents.MUSIC_DISC_13, 178, 1);
        class_9796.method_60766(arg, CAT, SoundEvents.MUSIC_DISC_CAT, 185, 2);
        class_9796.method_60766(arg, BLOCKS, SoundEvents.MUSIC_DISC_BLOCKS, 345, 3);
        class_9796.method_60766(arg, CHIRP, SoundEvents.MUSIC_DISC_CHIRP, 185, 4);
        class_9796.method_60766(arg, FAR, SoundEvents.MUSIC_DISC_FAR, 174, 5);
        class_9796.method_60766(arg, MALL, SoundEvents.MUSIC_DISC_MALL, 197, 6);
        class_9796.method_60766(arg, MELLOHI, SoundEvents.MUSIC_DISC_MELLOHI, 96, 7);
        class_9796.method_60766(arg, STAL, SoundEvents.MUSIC_DISC_STAL, 150, 8);
        class_9796.method_60766(arg, STRAD, SoundEvents.MUSIC_DISC_STRAD, 188, 9);
        class_9796.method_60766(arg, WARD, SoundEvents.MUSIC_DISC_WARD, 251, 10);
        class_9796.method_60766(arg, field_52047, SoundEvents.MUSIC_DISC_11, 71, 11);
        class_9796.method_60766(arg, WAIT, SoundEvents.MUSIC_DISC_WAIT, 238, 12);
        class_9796.method_60766(arg, PIGSTEP, SoundEvents.MUSIC_DISC_PIGSTEP, 149, 13);
        class_9796.method_60766(arg, OTHERSIDE, SoundEvents.MUSIC_DISC_OTHERSIDE, 195, 14);
        class_9796.method_60766(arg, field_52051, SoundEvents.MUSIC_DISC_5, 178, 15);
        class_9796.method_60766(arg, RELIC, SoundEvents.MUSIC_DISC_RELIC, 218, 14);
        class_9796.method_60766(arg, PRECIPICE, SoundEvents.MUSIC_DISC_PRECIPICE, 299, 13);
        class_9796.method_60766(arg, CREATOR, SoundEvents.MUSIC_DISC_CREATOR, 176, 12);
        class_9796.method_60766(arg, CREATOR_MUSIC_BOX, SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX, 73, 11);
    }
}

