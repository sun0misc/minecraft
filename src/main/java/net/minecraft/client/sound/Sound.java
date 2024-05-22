/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundContainer;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.FloatSupplier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Sound
implements SoundContainer<Sound> {
    public static final ResourceFinder FINDER = new ResourceFinder("sounds", ".ogg");
    private final Identifier id;
    private final FloatSupplier volume;
    private final FloatSupplier pitch;
    private final int weight;
    private final RegistrationType registrationType;
    private final boolean stream;
    private final boolean preload;
    private final int attenuation;

    public Sound(Identifier arg, FloatSupplier volume, FloatSupplier pitch, int weight, RegistrationType registrationType, boolean stream, boolean preload, int attenuation) {
        this.id = arg;
        this.volume = volume;
        this.pitch = pitch;
        this.weight = weight;
        this.registrationType = registrationType;
        this.stream = stream;
        this.preload = preload;
        this.attenuation = attenuation;
    }

    public Identifier getIdentifier() {
        return this.id;
    }

    public Identifier getLocation() {
        return FINDER.toResourcePath(this.id);
    }

    public FloatSupplier getVolume() {
        return this.volume;
    }

    public FloatSupplier getPitch() {
        return this.pitch;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public Sound getSound(Random arg) {
        return this;
    }

    @Override
    public void preload(SoundSystem soundSystem) {
        if (this.preload) {
            soundSystem.addPreloadedSound(this);
        }
    }

    public RegistrationType getRegistrationType() {
        return this.registrationType;
    }

    public boolean isStreamed() {
        return this.stream;
    }

    public boolean isPreloaded() {
        return this.preload;
    }

    public int getAttenuation() {
        return this.attenuation;
    }

    public String toString() {
        return "Sound[" + String.valueOf(this.id) + "]";
    }

    @Override
    public /* synthetic */ Object getSound(Random random) {
        return this.getSound(random);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum RegistrationType {
        FILE("file"),
        SOUND_EVENT("event");

        private final String name;

        private RegistrationType(String name) {
            this.name = name;
        }

        @Nullable
        public static RegistrationType getByName(String name) {
            for (RegistrationType lv : RegistrationType.values()) {
                if (!lv.name.equals(name)) continue;
                return lv;
            }
            return null;
        }
    }
}

