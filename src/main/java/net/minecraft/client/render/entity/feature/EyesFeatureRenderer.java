package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class EyesFeatureRenderer extends FeatureRenderer {
   public EyesFeatureRenderer(FeatureRendererContext arg) {
      super(arg);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
      VertexConsumer lv = vertexConsumers.getBuffer(this.getEyesTexture());
      this.getContextModel().render(matrices, lv, LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public abstract RenderLayer getEyesTexture();
}
