package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WolfCollarFeatureRenderer extends FeatureRenderer {
   private static final Identifier SKIN = new Identifier("textures/entity/wolf/wolf_collar.png");

   public WolfCollarFeatureRenderer(FeatureRendererContext arg) {
      super(arg);
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, WolfEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (arg3.isTamed() && !arg3.isInvisible()) {
         float[] fs = arg3.getCollarColor().getColorComponents();
         renderModel(this.getContextModel(), SKIN, arg, arg2, i, arg3, fs[0], fs[1], fs[2]);
      }
   }
}
