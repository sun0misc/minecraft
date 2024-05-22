/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.sound;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;

public class BiomeAdditionsSound {
    public static final Codec<BiomeAdditionsSound> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)SoundEvent.ENTRY_CODEC.fieldOf("sound")).forGetter(sound -> sound.sound), ((MapCodec)Codec.DOUBLE.fieldOf("tick_chance")).forGetter(sound -> sound.chance)).apply((Applicative<BiomeAdditionsSound, ?>)instance, BiomeAdditionsSound::new));
    private final RegistryEntry<SoundEvent> sound;
    private final double chance;

    public BiomeAdditionsSound(RegistryEntry<SoundEvent> sound, double chance) {
        this.sound = sound;
        this.chance = chance;
    }

    public RegistryEntry<SoundEvent> getSound() {
        return this.sound;
    }

    public double getChance() {
        return this.chance;
    }
}

