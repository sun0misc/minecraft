package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MusicTracker {
   private static final int DEFAULT_TIME_UNTIL_NEXT_SONG = 100;
   private final Random random = Random.create();
   private final MinecraftClient client;
   @Nullable
   private SoundInstance current;
   private int timeUntilNextSong = 100;

   public MusicTracker(MinecraftClient client) {
      this.client = client;
   }

   public void tick() {
      MusicSound lv = this.client.getMusicType();
      if (this.current != null) {
         if (!((SoundEvent)lv.getSound().value()).getId().equals(this.current.getId()) && lv.shouldReplaceCurrentMusic()) {
            this.client.getSoundManager().stop(this.current);
            this.timeUntilNextSong = MathHelper.nextInt(this.random, 0, lv.getMinDelay() / 2);
         }

         if (!this.client.getSoundManager().isPlaying(this.current)) {
            this.current = null;
            this.timeUntilNextSong = Math.min(this.timeUntilNextSong, MathHelper.nextInt(this.random, lv.getMinDelay(), lv.getMaxDelay()));
         }
      }

      this.timeUntilNextSong = Math.min(this.timeUntilNextSong, lv.getMaxDelay());
      if (this.current == null && this.timeUntilNextSong-- <= 0) {
         this.play(lv);
      }

   }

   public void play(MusicSound type) {
      this.current = PositionedSoundInstance.music((SoundEvent)type.getSound().value());
      if (this.current.getSound() != SoundManager.MISSING_SOUND) {
         this.client.getSoundManager().play(this.current);
      }

      this.timeUntilNextSong = Integer.MAX_VALUE;
   }

   public void stop(MusicSound type) {
      if (this.isPlayingType(type)) {
         this.stop();
      }

   }

   public void stop() {
      if (this.current != null) {
         this.client.getSoundManager().stop(this.current);
         this.current = null;
      }

      this.timeUntilNextSong += 100;
   }

   public boolean isPlayingType(MusicSound type) {
      return this.current == null ? false : ((SoundEvent)type.getSound().value()).getId().equals(this.current.getId());
   }
}
