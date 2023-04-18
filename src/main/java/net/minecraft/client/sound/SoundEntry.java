package net.minecraft.client.sound;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SoundEntry {
   private final List sounds;
   private final boolean replace;
   @Nullable
   private final String subtitle;

   public SoundEntry(List sounds, boolean replace, @Nullable String subtitle) {
      this.sounds = sounds;
      this.replace = replace;
      this.subtitle = subtitle;
   }

   public List getSounds() {
      return this.sounds;
   }

   public boolean canReplace() {
      return this.replace;
   }

   @Nullable
   public String getSubtitle() {
      return this.subtitle;
   }
}
