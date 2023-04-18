package net.minecraft.client.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.TranslatableOption;

@Environment(EnvType.CLIENT)
public enum CloudRenderMode implements TranslatableOption {
   OFF(0, "options.off"),
   FAST(1, "options.clouds.fast"),
   FANCY(2, "options.clouds.fancy");

   private final int id;
   private final String translationKey;

   private CloudRenderMode(int id, String translationKey) {
      this.id = id;
      this.translationKey = translationKey;
   }

   public int getId() {
      return this.id;
   }

   public String getTranslationKey() {
      return this.translationKey;
   }

   // $FF: synthetic method
   private static CloudRenderMode[] method_36860() {
      return new CloudRenderMode[]{OFF, FAST, FANCY};
   }
}
