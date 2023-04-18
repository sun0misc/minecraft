package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class FeatureRenderer {
   private final FeatureRendererContext context;

   public FeatureRenderer(FeatureRendererContext context) {
      this.context = context;
   }

   protected static void render(EntityModel contextModel, EntityModel model, Identifier texture, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta, float red, float green, float blue) {
      if (!entity.isInvisible()) {
         contextModel.copyStateTo(model);
         model.animateModel(entity, limbAngle, limbDistance, tickDelta);
         model.setAngles(entity, limbAngle, limbDistance, age, headYaw, headPitch);
         renderModel(model, texture, matrices, vertexConsumers, light, entity, red, green, blue);
      }

   }

   protected static void renderModel(EntityModel model, Identifier texture, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float red, float green, float blue) {
      VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
      model.render(matrices, lv, light, LivingEntityRenderer.getOverlay(entity, 0.0F), red, green, blue, 1.0F);
   }

   public EntityModel getContextModel() {
      return this.context.getModel();
   }

   protected Identifier getTexture(Entity entity) {
      return this.context.getTexture(entity);
   }

   public abstract void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch);
}
