/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Environment(value=EnvType.CLIENT)
public class AmbientSoundLoops {

    @Environment(value=EnvType.CLIENT)
    public static class Underwater
    extends MovingSoundInstance {
        public static final int MAX_TRANSITION_TIMER = 40;
        private final ClientPlayerEntity player;
        private int transitionTimer;

        public Underwater(ClientPlayerEntity player) {
            super(SoundEvents.AMBIENT_UNDERWATER_LOOP, SoundCategory.AMBIENT, SoundInstance.createRandom());
            this.player = player;
            this.repeat = true;
            this.repeatDelay = 0;
            this.volume = 1.0f;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.player.isRemoved() || this.transitionTimer < 0) {
                this.setDone();
                return;
            }
            this.transitionTimer = this.player.isSubmergedInWater() ? ++this.transitionTimer : (this.transitionTimer -= 2);
            this.transitionTimer = Math.min(this.transitionTimer, 40);
            this.volume = Math.max(0.0f, Math.min((float)this.transitionTimer / 40.0f, 1.0f));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class MusicLoop
    extends MovingSoundInstance {
        private final ClientPlayerEntity player;

        protected MusicLoop(ClientPlayerEntity player, SoundEvent soundEvent) {
            super(soundEvent, SoundCategory.AMBIENT, SoundInstance.createRandom());
            this.player = player;
            this.repeat = false;
            this.repeatDelay = 0;
            this.volume = 1.0f;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.player.isRemoved() || !this.player.isSubmergedInWater()) {
                this.setDone();
            }
        }
    }
}

