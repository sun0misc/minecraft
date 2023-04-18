package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class EnergySwirlOverlayFeatureRenderer extends FeatureRenderer {
   public EnergySwirlOverlayFeatureRenderer(FeatureRendererContext arg) {
      super(arg);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
      if (((SkinOverlayOwner)entity).shouldRenderOverlay()) {
         float m = (float)entity.age + tickDelta;
         EntityModel lv = this.getEnergySwirlModel();
         lv.animateModel(entity, limbAngle, limbDistance, tickDelta);
         this.getContextModel().copyStateTo(lv);
         VertexConsumer lv2 = vertexConsumers.getBuffer(RenderLayer.getEnergySwirl(this.getEnergySwirlTexture(), this.getEnergySwirlX(m) % 1.0F, m * 0.01F % 1.0F));
         lv.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
         lv.render(matrices, lv2, light, OverlayTexture.DEFAULT_UV, 0.5F, 0.5F, 0.5F, 1.0F);
      }
   }

   protected abstract float getEnergySwirlX(float partialAge);

   protected abstract Identifier getEnergySwirlTexture();

   protected abstract EntityModel getEnergySwirlModel();
}
