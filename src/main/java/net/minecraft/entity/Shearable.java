package net.minecraft.entity;

import net.minecraft.sound.SoundCategory;

public interface Shearable {
   void sheared(SoundCategory shearedSoundCategory);

   boolean isShearable();
}
