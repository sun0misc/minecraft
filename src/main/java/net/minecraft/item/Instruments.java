/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.item.Instrument;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public interface Instruments {
    public static final int GOAT_HORN_RANGE = 256;
    public static final int GOAT_HORN_USE_DURATION = 140;
    public static final RegistryKey<Instrument> PONDER_GOAT_HORN = Instruments.of("ponder_goat_horn");
    public static final RegistryKey<Instrument> SING_GOAT_HORN = Instruments.of("sing_goat_horn");
    public static final RegistryKey<Instrument> SEEK_GOAT_HORN = Instruments.of("seek_goat_horn");
    public static final RegistryKey<Instrument> FEEL_GOAT_HORN = Instruments.of("feel_goat_horn");
    public static final RegistryKey<Instrument> ADMIRE_GOAT_HORN = Instruments.of("admire_goat_horn");
    public static final RegistryKey<Instrument> CALL_GOAT_HORN = Instruments.of("call_goat_horn");
    public static final RegistryKey<Instrument> YEARN_GOAT_HORN = Instruments.of("yearn_goat_horn");
    public static final RegistryKey<Instrument> DREAM_GOAT_HORN = Instruments.of("dream_goat_horn");

    private static RegistryKey<Instrument> of(String id) {
        return RegistryKey.of(RegistryKeys.INSTRUMENT, Identifier.method_60656(id));
    }

    public static Instrument registerAndGetDefault(Registry<Instrument> registry) {
        Registry.register(registry, PONDER_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(0), 140, 256.0f));
        Registry.register(registry, SING_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(1), 140, 256.0f));
        Registry.register(registry, SEEK_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(2), 140, 256.0f));
        Registry.register(registry, FEEL_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(3), 140, 256.0f));
        Registry.register(registry, ADMIRE_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(4), 140, 256.0f));
        Registry.register(registry, CALL_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(5), 140, 256.0f));
        Registry.register(registry, YEARN_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(6), 140, 256.0f));
        return Registry.register(registry, DREAM_GOAT_HORN, new Instrument((RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(7), 140, 256.0f));
    }
}

