package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class Deadmau5FeatureRenderer extends FeatureRenderer {
   public Deadmau5FeatureRenderer(FeatureRendererContext arg) {
      super(arg);
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, AbstractClientPlayerEntity arg3, float f, float g, float h, float j, float k, float l) {
      if ("deadmau5".equals(arg3.getName().getString()) && arg3.hasSkinTexture() && !arg3.isInvisible()) {
         VertexConsumer lv = arg2.getBuffer(RenderLayer.getEntitySolid(arg3.getSkinTexture()));
         int m = LivingEntityRenderer.getOverlay(arg3, 0.0F);

         for(int n = 0; n < 2; ++n) {
            float o = MathHelper.lerp(h, arg3.prevYaw, arg3.getYaw()) - MathHelper.lerp(h, arg3.prevBodyYaw, arg3.bodyYaw);
            float p = MathHelper.lerp(h, arg3.prevPitch, arg3.getPitch());
            arg.push();
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(o));
            arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p));
            arg.translate(0.375F * (float)(n * 2 - 1), 0.0F, 0.0F);
            arg.translate(0.0F, -0.375F, 0.0F);
            arg.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p));
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-o));
            float q = 1.3333334F;
            arg.scale(1.3333334F, 1.3333334F, 1.3333334F);
            ((PlayerEntityModel)this.getContextModel()).renderEars(arg, lv, i, m);
            arg.pop();
         }

      }
   }
}
