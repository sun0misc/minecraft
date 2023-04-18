package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.world.gen.WorldPresets;

@Environment(EnvType.CLIENT)
public enum RealmsWorldGeneratorType {
   DEFAULT(0, WorldPresets.DEFAULT),
   FLAT(1, WorldPresets.FLAT),
   LARGE_BIOMES(2, WorldPresets.LARGE_BIOMES),
   AMPLIFIED(3, WorldPresets.AMPLIFIED);

   private final int id;
   private final Text text;

   private RealmsWorldGeneratorType(int id, RegistryKey presetKey) {
      this.id = id;
      this.text = Text.translatable(presetKey.getValue().toTranslationKey("generator"));
   }

   public Text getText() {
      return this.text;
   }

   public int getId() {
      return this.id;
   }

   // $FF: synthetic method
   private static RealmsWorldGeneratorType[] method_36856() {
      return new RealmsWorldGeneratorType[]{DEFAULT, FLAT, LARGE_BIOMES, AMPLIFIED};
   }
}
