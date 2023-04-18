package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GuardianEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class GuardianEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/guardian.png");
   private static final Identifier EXPLOSION_BEAM_TEXTURE = new Identifier("textures/entity/guardian_beam.png");
   private static final RenderLayer LAYER;

   public GuardianEntityRenderer(EntityRendererFactory.Context arg) {
      this(arg, 0.5F, EntityModelLayers.GUARDIAN);
   }

   protected GuardianEntityRenderer(EntityRendererFactory.Context ctx, float shadowRadius, EntityModelLayer layer) {
      super(ctx, new GuardianEntityModel(ctx.getPart(layer)), shadowRadius);
   }

   public boolean shouldRender(GuardianEntity arg, Frustum arg2, double d, double e, double f) {
      if (super.shouldRender((MobEntity)arg, arg2, d, e, f)) {
         return true;
      } else {
         if (arg.hasBeamTarget()) {
            LivingEntity lv = arg.getBeamTarget();
            if (lv != null) {
               Vec3d lv2 = this.fromLerpedPosition(lv, (double)lv.getHeight() * 0.5, 1.0F);
               Vec3d lv3 = this.fromLerpedPosition(arg, (double)arg.getStandingEyeHeight(), 1.0F);
               return arg2.isVisible(new Box(lv3.x, lv3.y, lv3.z, lv2.x, lv2.y, lv2.z));
            }
         }

         return false;
      }
   }

   private Vec3d fromLerpedPosition(LivingEntity entity, double yOffset, float delta) {
      double e = MathHelper.lerp((double)delta, entity.lastRenderX, entity.getX());
      double g = MathHelper.lerp((double)delta, entity.lastRenderY, entity.getY()) + yOffset;
      double h = MathHelper.lerp((double)delta, entity.lastRenderZ, entity.getZ());
      return new Vec3d(e, g, h);
   }

   public void render(GuardianEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      super.render((MobEntity)arg, f, g, arg2, arg3, i);
      LivingEntity lv = arg.getBeamTarget();
      if (lv != null) {
         float h = arg.getBeamProgress(g);
         float j = arg.getBeamTicks() + g;
         float k = j * 0.5F % 1.0F;
         float l = arg.getStandingEyeHeight();
         arg2.push();
         arg2.translate(0.0F, l, 0.0F);
         Vec3d lv2 = this.fromLerpedPosition(lv, (double)lv.getHeight() * 0.5, g);
         Vec3d lv3 = this.fromLerpedPosition(arg, (double)l, g);
         Vec3d lv4 = lv2.subtract(lv3);
         float m = (float)(lv4.length() + 1.0);
         lv4 = lv4.normalize();
         float n = (float)Math.acos(lv4.y);
         float o = (float)Math.atan2(lv4.z, lv4.x);
         arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((1.5707964F - o) * 57.295776F));
         arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(n * 57.295776F));
         int p = true;
         float q = j * 0.05F * -1.5F;
         float r = h * h;
         int s = 64 + (int)(r * 191.0F);
         int t = 32 + (int)(r * 191.0F);
         int u = 128 - (int)(r * 64.0F);
         float v = 0.2F;
         float w = 0.282F;
         float x = MathHelper.cos(q + 2.3561945F) * 0.282F;
         float y = MathHelper.sin(q + 2.3561945F) * 0.282F;
         float z = MathHelper.cos(q + 0.7853982F) * 0.282F;
         float aa = MathHelper.sin(q + 0.7853982F) * 0.282F;
         float ab = MathHelper.cos(q + 3.926991F) * 0.282F;
         float ac = MathHelper.sin(q + 3.926991F) * 0.282F;
         float ad = MathHelper.cos(q + 5.4977875F) * 0.282F;
         float ae = MathHelper.sin(q + 5.4977875F) * 0.282F;
         float af = MathHelper.cos(q + 3.1415927F) * 0.2F;
         float ag = MathHelper.sin(q + 3.1415927F) * 0.2F;
         float ah = MathHelper.cos(q + 0.0F) * 0.2F;
         float ai = MathHelper.sin(q + 0.0F) * 0.2F;
         float aj = MathHelper.cos(q + 1.5707964F) * 0.2F;
         float ak = MathHelper.sin(q + 1.5707964F) * 0.2F;
         float al = MathHelper.cos(q + 4.712389F) * 0.2F;
         float am = MathHelper.sin(q + 4.712389F) * 0.2F;
         float ao = 0.0F;
         float ap = 0.4999F;
         float aq = -1.0F + k;
         float ar = m * 2.5F + aq;
         VertexConsumer lv5 = arg3.getBuffer(LAYER);
         MatrixStack.Entry lv6 = arg2.peek();
         Matrix4f matrix4f = lv6.getPositionMatrix();
         Matrix3f matrix3f = lv6.getNormalMatrix();
         vertex(lv5, matrix4f, matrix3f, af, m, ag, s, t, u, 0.4999F, ar);
         vertex(lv5, matrix4f, matrix3f, af, 0.0F, ag, s, t, u, 0.4999F, aq);
         vertex(lv5, matrix4f, matrix3f, ah, 0.0F, ai, s, t, u, 0.0F, aq);
         vertex(lv5, matrix4f, matrix3f, ah, m, ai, s, t, u, 0.0F, ar);
         vertex(lv5, matrix4f, matrix3f, aj, m, ak, s, t, u, 0.4999F, ar);
         vertex(lv5, matrix4f, matrix3f, aj, 0.0F, ak, s, t, u, 0.4999F, aq);
         vertex(lv5, matrix4f, matrix3f, al, 0.0F, am, s, t, u, 0.0F, aq);
         vertex(lv5, matrix4f, matrix3f, al, m, am, s, t, u, 0.0F, ar);
         float as = 0.0F;
         if (arg.age % 2 == 0) {
            as = 0.5F;
         }

         vertex(lv5, matrix4f, matrix3f, x, m, y, s, t, u, 0.5F, as + 0.5F);
         vertex(lv5, matrix4f, matrix3f, z, m, aa, s, t, u, 1.0F, as + 0.5F);
         vertex(lv5, matrix4f, matrix3f, ad, m, ae, s, t, u, 1.0F, as);
         vertex(lv5, matrix4f, matrix3f, ab, m, ac, s, t, u, 0.5F, as);
         arg2.pop();
      }

   }

   private static void vertex(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Matrix3f normalMatrix, float x, float y, float z, int red, int green, int blue, float u, float v) {
      vertexConsumer.vertex(positionMatrix, x, y, z).color(red, green, blue, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
   }

   public Identifier getTexture(GuardianEntity arg) {
      return TEXTURE;
   }

   static {
      LAYER = RenderLayer.getEntityCutoutNoCull(EXPLOSION_BEAM_TEXTURE);
   }
}
