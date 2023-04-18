package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class EndermanBlockFeatureRenderer extends FeatureRenderer {
   private final BlockRenderManager blockRenderManager;

   public EndermanBlockFeatureRenderer(FeatureRendererContext context, BlockRenderManager blockRenderManager) {
      super(context);
      this.blockRenderManager = blockRenderManager;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, EndermanEntity arg3, float f, float g, float h, float j, float k, float l) {
      BlockState lv = arg3.getCarriedBlock();
      if (lv != null) {
         arg.push();
         arg.translate(0.0F, 0.6875F, -0.75F);
         arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20.0F));
         arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F));
         arg.translate(0.25F, 0.1875F, 0.25F);
         float m = 0.5F;
         arg.scale(-0.5F, -0.5F, 0.5F);
         arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
         this.blockRenderManager.renderBlockAsEntity(lv, arg, arg2, i, OverlayTexture.DEFAULT_UV);
         arg.pop();
      }
   }
}
