package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface SoundInstance {
   Identifier getId();

   @Nullable
   WeightedSoundSet getSoundSet(SoundManager soundManager);

   Sound getSound();

   SoundCategory getCategory();

   boolean isRepeatable();

   boolean isRelative();

   int getRepeatDelay();

   float getVolume();

   float getPitch();

   double getX();

   double getY();

   double getZ();

   AttenuationType getAttenuationType();

   default boolean shouldAlwaysPlay() {
      return false;
   }

   default boolean canPlay() {
      return true;
   }

   static Random createRandom() {
      return Random.create();
   }

   @Environment(EnvType.CLIENT)
   public static enum AttenuationType {
      NONE,
      LINEAR;

      // $FF: synthetic method
      private static AttenuationType[] method_36927() {
         return new AttenuationType[]{NONE, LINEAR};
      }
   }
}
