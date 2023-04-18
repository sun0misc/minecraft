package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class StuckStingersFeatureRenderer extends StuckObjectsFeatureRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/bee/bee_stinger.png");

   public StuckStingersFeatureRenderer(LivingEntityRenderer arg) {
      super(arg);
   }

   protected int getObjectCount(LivingEntity entity) {
      return entity.getStingerCount();
   }

   protected void renderObject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float directionX, float directionY, float directionZ, float tickDelta) {
      float k = MathHelper.sqrt(directionX * directionX + directionZ * directionZ);
      float l = (float)(Math.atan2((double)directionX, (double)directionZ) * 57.2957763671875);
      float m = (float)(Math.atan2((double)directionY, (double)k) * 57.2957763671875);
      matrices.translate(0.0F, 0.0F, 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(l - 90.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(m));
      float n = 0.0F;
      float o = 0.125F;
      float p = 0.0F;
      float q = 0.0625F;
      float r = 0.03125F;
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F));
      matrices.scale(0.03125F, 0.03125F, 0.03125F);
      matrices.translate(2.5F, 0.0F, 0.0F);
      VertexConsumer lv = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));

      for(int s = 0; s < 4; ++s) {
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
         MatrixStack.Entry lv2 = matrices.peek();
         Matrix4f matrix4f = lv2.getPositionMatrix();
         Matrix3f matrix3f = lv2.getNormalMatrix();
         produceVertex(lv, matrix4f, matrix3f, -4.5F, -1, 0.0F, 0.0F, light);
         produceVertex(lv, matrix4f, matrix3f, 4.5F, -1, 0.125F, 0.0F, light);
         produceVertex(lv, matrix4f, matrix3f, 4.5F, 1, 0.125F, 0.0625F, light);
         produceVertex(lv, matrix4f, matrix3f, -4.5F, 1, 0.0F, 0.0625F, light);
      }

   }

   private static void produceVertex(VertexConsumer vertexConsumer, Matrix4f vertexTransform, Matrix3f normalTransform, float x, int y, float u, float v, int light) {
      vertexConsumer.vertex(vertexTransform, x, (float)y, 0.0F).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalTransform, 0.0F, 1.0F, 0.0F).next();
   }
}
