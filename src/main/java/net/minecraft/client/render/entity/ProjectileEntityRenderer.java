package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class ProjectileEntityRenderer extends EntityRenderer {
   public ProjectileEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
   }

   public void render(PersistentProjectileEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(g, arg.prevYaw, arg.getYaw()) - 90.0F));
      arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(g, arg.prevPitch, arg.getPitch())));
      int j = false;
      float h = 0.0F;
      float k = 0.5F;
      float l = 0.0F;
      float m = 0.15625F;
      float n = 0.0F;
      float o = 0.15625F;
      float p = 0.15625F;
      float q = 0.3125F;
      float r = 0.05625F;
      float s = (float)arg.shake - g;
      if (s > 0.0F) {
         float t = -MathHelper.sin(s * 3.0F) * s;
         arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(t));
      }

      arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F));
      arg2.scale(0.05625F, 0.05625F, 0.05625F);
      arg2.translate(-4.0F, 0.0F, 0.0F);
      VertexConsumer lv = arg3.getBuffer(RenderLayer.getEntityCutout(this.getTexture(arg)));
      MatrixStack.Entry lv2 = arg2.peek();
      Matrix4f matrix4f = lv2.getPositionMatrix();
      Matrix3f matrix3f = lv2.getNormalMatrix();
      this.vertex(matrix4f, matrix3f, lv, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, lv, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, lv, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, lv, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, lv, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, lv, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, lv, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, i);
      this.vertex(matrix4f, matrix3f, lv, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, i);

      for(int u = 0; u < 4; ++u) {
         arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
         this.vertex(matrix4f, matrix3f, lv, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, i);
         this.vertex(matrix4f, matrix3f, lv, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, i);
         this.vertex(matrix4f, matrix3f, lv, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, i);
         this.vertex(matrix4f, matrix3f, lv, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, i);
      }

      arg2.pop();
      super.render(arg, f, g, arg2, arg3, i);
   }

   public void vertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, int x, int y, int z, float u, float v, int normalX, int normalZ, int normalY, int light) {
      vertexConsumer.vertex(positionMatrix, (float)x, (float)y, (float)z).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, (float)normalX, (float)normalY, (float)normalZ).next();
   }
}
