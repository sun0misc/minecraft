package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ExperienceOrbEntityRenderer extends EntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/experience_orb.png");
   private static final RenderLayer LAYER;

   public ExperienceOrbEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.shadowRadius = 0.15F;
      this.shadowOpacity = 0.75F;
   }

   protected int getBlockLight(ExperienceOrbEntity arg, BlockPos arg2) {
      return MathHelper.clamp(super.getBlockLight(arg, arg2) + 7, 0, 15);
   }

   public void render(ExperienceOrbEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      int j = arg.getOrbSize();
      float h = (float)(j % 4 * 16 + 0) / 64.0F;
      float k = (float)(j % 4 * 16 + 16) / 64.0F;
      float l = (float)(j / 4 * 16 + 0) / 64.0F;
      float m = (float)(j / 4 * 16 + 16) / 64.0F;
      float n = 1.0F;
      float o = 0.5F;
      float p = 0.25F;
      float q = 255.0F;
      float r = ((float)arg.age + g) / 2.0F;
      int s = (int)((MathHelper.sin(r + 0.0F) + 1.0F) * 0.5F * 255.0F);
      int t = true;
      int u = (int)((MathHelper.sin(r + 4.1887903F) + 1.0F) * 0.1F * 255.0F);
      arg2.translate(0.0F, 0.1F, 0.0F);
      arg2.multiply(this.dispatcher.getRotation());
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
      float v = 0.3F;
      arg2.scale(0.3F, 0.3F, 0.3F);
      VertexConsumer lv = arg3.getBuffer(LAYER);
      MatrixStack.Entry lv2 = arg2.peek();
      Matrix4f matrix4f = lv2.getPositionMatrix();
      Matrix3f matrix3f = lv2.getNormalMatrix();
      vertex(lv, matrix4f, matrix3f, -0.5F, -0.25F, s, 255, u, h, m, i);
      vertex(lv, matrix4f, matrix3f, 0.5F, -0.25F, s, 255, u, k, m, i);
      vertex(lv, matrix4f, matrix3f, 0.5F, 0.75F, s, 255, u, k, l, i);
      vertex(lv, matrix4f, matrix3f, -0.5F, 0.75F, s, 255, u, h, l, i);
      arg2.pop();
      super.render(arg, f, g, arg2, arg3, i);
   }

   private static void vertex(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Matrix3f normalMatrix, float x, float y, int red, int green, int blue, float u, float v, int light) {
      vertexConsumer.vertex(positionMatrix, x, y, 0.0F).color(red, green, blue, 128).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
   }

   public Identifier getTexture(ExperienceOrbEntity arg) {
      return TEXTURE;
   }

   static {
      LAYER = RenderLayer.getItemEntityTranslucentCull(TEXTURE);
   }
}
