package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class FishingBobberEntityRenderer extends EntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/fishing_hook.png");
   private static final RenderLayer LAYER;
   private static final double field_33632 = 960.0;

   public FishingBobberEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
   }

   public void render(FishingBobberEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      PlayerEntity lv = arg.getPlayerOwner();
      if (lv != null) {
         arg2.push();
         arg2.push();
         arg2.scale(0.5F, 0.5F, 0.5F);
         arg2.multiply(this.dispatcher.getRotation());
         arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
         MatrixStack.Entry lv2 = arg2.peek();
         Matrix4f matrix4f = lv2.getPositionMatrix();
         Matrix3f matrix3f = lv2.getNormalMatrix();
         VertexConsumer lv3 = arg3.getBuffer(LAYER);
         vertex(lv3, matrix4f, matrix3f, i, 0.0F, 0, 0, 1);
         vertex(lv3, matrix4f, matrix3f, i, 1.0F, 0, 1, 1);
         vertex(lv3, matrix4f, matrix3f, i, 1.0F, 1, 1, 0);
         vertex(lv3, matrix4f, matrix3f, i, 0.0F, 1, 0, 0);
         arg2.pop();
         int j = lv.getMainArm() == Arm.RIGHT ? 1 : -1;
         ItemStack lv4 = lv.getMainHandStack();
         if (!lv4.isOf(Items.FISHING_ROD)) {
            j = -j;
         }

         float h = lv.getHandSwingProgress(g);
         float k = MathHelper.sin(MathHelper.sqrt(h) * 3.1415927F);
         float l = MathHelper.lerp(g, lv.prevBodyYaw, lv.bodyYaw) * 0.017453292F;
         double d = (double)MathHelper.sin(l);
         double e = (double)MathHelper.cos(l);
         double m = (double)j * 0.35;
         double n = 0.8;
         double o;
         double p;
         double q;
         float r;
         double s;
         if ((this.dispatcher.gameOptions == null || this.dispatcher.gameOptions.getPerspective().isFirstPerson()) && lv == MinecraftClient.getInstance().player) {
            s = 960.0 / (double)(Integer)this.dispatcher.gameOptions.getFov().getValue();
            Vec3d lv5 = this.dispatcher.camera.getProjection().getPosition((float)j * 0.525F, -0.1F);
            lv5 = lv5.multiply(s);
            lv5 = lv5.rotateY(k * 0.5F);
            lv5 = lv5.rotateX(-k * 0.7F);
            o = MathHelper.lerp((double)g, lv.prevX, lv.getX()) + lv5.x;
            p = MathHelper.lerp((double)g, lv.prevY, lv.getY()) + lv5.y;
            q = MathHelper.lerp((double)g, lv.prevZ, lv.getZ()) + lv5.z;
            r = lv.getStandingEyeHeight();
         } else {
            o = MathHelper.lerp((double)g, lv.prevX, lv.getX()) - e * m - d * 0.8;
            p = lv.prevY + (double)lv.getStandingEyeHeight() + (lv.getY() - lv.prevY) * (double)g - 0.45;
            q = MathHelper.lerp((double)g, lv.prevZ, lv.getZ()) - d * m + e * 0.8;
            r = lv.isInSneakingPose() ? -0.1875F : 0.0F;
         }

         s = MathHelper.lerp((double)g, arg.prevX, arg.getX());
         double t = MathHelper.lerp((double)g, arg.prevY, arg.getY()) + 0.25;
         double u = MathHelper.lerp((double)g, arg.prevZ, arg.getZ());
         float v = (float)(o - s);
         float w = (float)(p - t) + r;
         float x = (float)(q - u);
         VertexConsumer lv6 = arg3.getBuffer(RenderLayer.getLineStrip());
         MatrixStack.Entry lv7 = arg2.peek();
         int y = true;

         for(int z = 0; z <= 16; ++z) {
            renderFishingLine(v, w, x, lv6, lv7, percentage(z, 16), percentage(z + 1, 16));
         }

         arg2.pop();
         super.render(arg, f, g, arg2, arg3, i);
      }
   }

   private static float percentage(int value, int max) {
      return (float)value / (float)max;
   }

   private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {
      buffer.vertex(matrix, x - 0.5F, (float)y - 0.5F, 0.0F).color(255, 255, 255, 255).texture((float)u, (float)v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
   }

   private static void renderFishingLine(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd) {
      float k = x * segmentStart;
      float l = y * (segmentStart * segmentStart + segmentStart) * 0.5F + 0.25F;
      float m = z * segmentStart;
      float n = x * segmentEnd - k;
      float o = y * (segmentEnd * segmentEnd + segmentEnd) * 0.5F + 0.25F - l;
      float p = z * segmentEnd - m;
      float q = MathHelper.sqrt(n * n + o * o + p * p);
      n /= q;
      o /= q;
      p /= q;
      buffer.vertex(matrices.getPositionMatrix(), k, l, m).color(0, 0, 0, 255).normal(matrices.getNormalMatrix(), n, o, p).next();
   }

   public Identifier getTexture(FishingBobberEntity arg) {
      return TEXTURE;
   }

   static {
      LAYER = RenderLayer.getEntityCutout(TEXTURE);
   }
}
