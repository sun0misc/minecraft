/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.sound;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BiomeEffectSoundPlayer
implements ClientPlayerTickable {
    private static final int MAX_STRENGTH = 40;
    private static final float field_32995 = 0.001f;
    private final ClientPlayerEntity player;
    private final SoundManager soundManager;
    private final BiomeAccess biomeAccess;
    private final Random random;
    private final Object2ObjectArrayMap<Biome, MusicLoop> soundLoops = new Object2ObjectArrayMap();
    private Optional<BiomeMoodSound> moodSound = Optional.empty();
    private Optional<BiomeAdditionsSound> additionsSound = Optional.empty();
    private float moodPercentage;
    @Nullable
    private Biome activeBiome;

    public BiomeEffectSoundPlayer(ClientPlayerEntity player, SoundManager soundManager, BiomeAccess biomeAccess) {
        this.random = player.getWorld().getRandom();
        this.player = player;
        this.soundManager = soundManager;
        this.biomeAccess = biomeAccess;
    }

    public float getMoodPercentage() {
        return this.moodPercentage;
    }

    @Override
    public void tick() {
        this.soundLoops.values().removeIf(MovingSoundInstance::isDone);
        Biome lv = this.biomeAccess.getBiomeForNoiseGen(this.player.getX(), this.player.getY(), this.player.getZ()).value();
        if (lv != this.activeBiome) {
            this.activeBiome = lv;
            this.moodSound = lv.getMoodSound();
            this.additionsSound = lv.getAdditionsSound();
            this.soundLoops.values().forEach(MusicLoop::fadeOut);
            lv.getLoopSound().ifPresent(sound2 -> this.soundLoops.compute(lv, (sound, loop) -> {
                if (loop == null) {
                    loop = new MusicLoop((SoundEvent)sound2.value());
                    this.soundManager.play((SoundInstance)loop);
                }
                loop.fadeIn();
                return loop;
            }));
        }
        this.additionsSound.ifPresent(sound -> {
            if (this.random.nextDouble() < sound.getChance()) {
                this.soundManager.play(PositionedSoundInstance.ambient(sound.getSound().value()));
            }
        });
        this.moodSound.ifPresent(sound -> {
            World lv = this.player.getWorld();
            int i = sound.getSpawnRange() * 2 + 1;
            BlockPos lv2 = BlockPos.ofFloored(this.player.getX() + (double)this.random.nextInt(i) - (double)sound.getSpawnRange(), this.player.getEyeY() + (double)this.random.nextInt(i) - (double)sound.getSpawnRange(), this.player.getZ() + (double)this.random.nextInt(i) - (double)sound.getSpawnRange());
            int j = lv.getLightLevel(LightType.SKY, lv2);
            this.moodPercentage = j > 0 ? (this.moodPercentage -= (float)j / (float)lv.getMaxLightLevel() * 0.001f) : (this.moodPercentage -= (float)(lv.getLightLevel(LightType.BLOCK, lv2) - 1) / (float)sound.getCultivationTicks());
            if (this.moodPercentage >= 1.0f) {
                double d = (double)lv2.getX() + 0.5;
                double e = (double)lv2.getY() + 0.5;
                double f = (double)lv2.getZ() + 0.5;
                double g = d - this.player.getX();
                double h = e - this.player.getEyeY();
                double k = f - this.player.getZ();
                double l = Math.sqrt(g * g + h * h + k * k);
                double m = l + sound.getExtraDistance();
                PositionedSoundInstance lv3 = PositionedSoundInstance.ambient(sound.getSound().value(), this.random, this.player.getX() + g / l * m, this.player.getEyeY() + h / l * m, this.player.getZ() + k / l * m);
                this.soundManager.play(lv3);
                this.moodPercentage = 0.0f;
            } else {
                this.moodPercentage = Math.max(this.moodPercentage, 0.0f);
            }
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static class MusicLoop
    extends MovingSoundInstance {
        private int delta;
        private int strength;

        public MusicLoop(SoundEvent sound) {
            super(sound, SoundCategory.AMBIENT, SoundInstance.createRandom());
            this.repeat = true;
            this.repeatDelay = 0;
            this.volume = 1.0f;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.strength < 0) {
                this.setDone();
            }
            this.strength += this.delta;
            this.volume = MathHelper.clamp((float)this.strength / 40.0f, 0.0f, 1.0f);
        }

        public void fadeOut() {
            this.strength = Math.min(this.strength, 40);
            this.delta = -1;
        }

        public void fadeIn() {
            this.strength = Math.max(0, this.strength);
            this.delta = 1;
        }
    }
}

