package net.minecraft.client.util;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DefaultSkinHelper {
   private static final Skin[] SKINS;

   public static Identifier getTexture() {
      return SKINS[6].texture();
   }

   public static Identifier getTexture(UUID uuid) {
      return getSkin(uuid).texture;
   }

   public static String getModel(UUID uuid) {
      return getSkin(uuid).model.name;
   }

   private static Skin getSkin(UUID uuid) {
      return SKINS[Math.floorMod(uuid.hashCode(), SKINS.length)];
   }

   static {
      SKINS = new Skin[]{new Skin("textures/entity/player/slim/alex.png", DefaultSkinHelper.Model.SLIM), new Skin("textures/entity/player/slim/ari.png", DefaultSkinHelper.Model.SLIM), new Skin("textures/entity/player/slim/efe.png", DefaultSkinHelper.Model.SLIM), new Skin("textures/entity/player/slim/kai.png", DefaultSkinHelper.Model.SLIM), new Skin("textures/entity/player/slim/makena.png", DefaultSkinHelper.Model.SLIM), new Skin("textures/entity/player/slim/noor.png", DefaultSkinHelper.Model.SLIM), new Skin("textures/entity/player/slim/steve.png", DefaultSkinHelper.Model.SLIM), new Skin("textures/entity/player/slim/sunny.png", DefaultSkinHelper.Model.SLIM), new Skin("textures/entity/player/slim/zuri.png", DefaultSkinHelper.Model.SLIM), new Skin("textures/entity/player/wide/alex.png", DefaultSkinHelper.Model.WIDE), new Skin("textures/entity/player/wide/ari.png", DefaultSkinHelper.Model.WIDE), new Skin("textures/entity/player/wide/efe.png", DefaultSkinHelper.Model.WIDE), new Skin("textures/entity/player/wide/kai.png", DefaultSkinHelper.Model.WIDE), new Skin("textures/entity/player/wide/makena.png", DefaultSkinHelper.Model.WIDE), new Skin("textures/entity/player/wide/noor.png", DefaultSkinHelper.Model.WIDE), new Skin("textures/entity/player/wide/steve.png", DefaultSkinHelper.Model.WIDE), new Skin("textures/entity/player/wide/sunny.png", DefaultSkinHelper.Model.WIDE), new Skin("textures/entity/player/wide/zuri.png", DefaultSkinHelper.Model.WIDE)};
   }

   @Environment(EnvType.CLIENT)
   static record Skin(Identifier texture, Model model) {
      final Identifier texture;
      final Model model;

      public Skin(String texture, Model model) {
         this(new Identifier(texture), model);
      }

      private Skin(Identifier arg, Model arg2) {
         this.texture = arg;
         this.model = arg2;
      }

      public Identifier texture() {
         return this.texture;
      }

      public Model model() {
         return this.model;
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum Model {
      SLIM("slim"),
      WIDE("default");

      final String name;

      private Model(String name) {
         this.name = name;
      }

      // $FF: synthetic method
      private static Model[] method_47439() {
         return new Model[]{SLIM, WIDE};
      }
   }
}
