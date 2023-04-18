package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class PhantomEyesFeatureRenderer extends EyesFeatureRenderer {
   private static final RenderLayer SKIN = RenderLayer.getEyes(new Identifier("textures/entity/phantom_eyes.png"));

   public PhantomEyesFeatureRenderer(FeatureRendererContext arg) {
      super(arg);
   }

   public RenderLayer getEyesTexture() {
      return SKIN;
   }
}
