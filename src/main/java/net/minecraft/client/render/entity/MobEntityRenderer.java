package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class MobEntityRenderer extends LivingEntityRenderer {
   public static final int LEASH_PIECE_COUNT = 24;

   public MobEntityRenderer(EntityRendererFactory.Context arg, EntityModel arg2, float f) {
      super(arg, arg2, f);
   }

   protected boolean hasLabel(MobEntity arg) {
      return super.hasLabel((LivingEntity)arg) && (arg.shouldRenderName() || arg.hasCustomName() && arg == this.dispatcher.targetedEntity);
   }

   public boolean shouldRender(MobEntity arg, Frustum arg2, double d, double e, double f) {
      if (super.shouldRender(arg, arg2, d, e, f)) {
         return true;
      } else {
         Entity lv = arg.getHoldingEntity();
         return lv != null ? arg2.isVisible(lv.getVisibilityBoundingBox()) : false;
      }
   }

   public void render(MobEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      super.render((LivingEntity)arg, f, g, arg2, arg3, i);
      Entity lv = arg.getHoldingEntity();
      if (lv != null) {
         this.renderLeash(arg, g, arg2, arg3, lv);
      }
   }

   private void renderLeash(MobEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, Entity holdingEntity) {
      matrices.push();
      Vec3d lv = holdingEntity.getLeashPos(tickDelta);
      double d = (double)(MathHelper.lerp(tickDelta, entity.prevBodyYaw, entity.bodyYaw) * 0.017453292F) + 1.5707963267948966;
      Vec3d lv2 = entity.getLeashOffset(tickDelta);
      double e = Math.cos(d) * lv2.z + Math.sin(d) * lv2.x;
      double g = Math.sin(d) * lv2.z - Math.cos(d) * lv2.x;
      double h = MathHelper.lerp((double)tickDelta, entity.prevX, entity.getX()) + e;
      double i = MathHelper.lerp((double)tickDelta, entity.prevY, entity.getY()) + lv2.y;
      double j = MathHelper.lerp((double)tickDelta, entity.prevZ, entity.getZ()) + g;
      matrices.translate(e, lv2.y, g);
      float k = (float)(lv.x - h);
      float l = (float)(lv.y - i);
      float m = (float)(lv.z - j);
      float n = 0.025F;
      VertexConsumer lv3 = provider.getBuffer(RenderLayer.getLeash());
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      float o = MathHelper.inverseSqrt(k * k + m * m) * 0.025F / 2.0F;
      float p = m * o;
      float q = k * o;
      BlockPos lv4 = BlockPos.ofFloored(entity.getCameraPosVec(tickDelta));
      BlockPos lv5 = BlockPos.ofFloored(holdingEntity.getCameraPosVec(tickDelta));
      int r = this.getBlockLight(entity, lv4);
      int s = this.dispatcher.getRenderer(holdingEntity).getBlockLight(holdingEntity, lv5);
      int t = entity.world.getLightLevel(LightType.SKY, lv4);
      int u = entity.world.getLightLevel(LightType.SKY, lv5);

      int v;
      for(v = 0; v <= 24; ++v) {
         renderLeashPiece(lv3, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.025F, p, q, v, false);
      }

      for(v = 24; v >= 0; --v) {
         renderLeashPiece(lv3, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.0F, p, q, v, true);
      }

      matrices.pop();
   }

   private static void renderLeashPiece(VertexConsumer vertexConsumer, Matrix4f positionMatrix, float f, float g, float h, int leashedEntityBlockLight, int holdingEntityBlockLight, int leashedEntitySkyLight, int holdingEntitySkyLight, float m, float n, float o, float p, int pieceIndex, boolean isLeashKnot) {
      float r = (float)pieceIndex / 24.0F;
      int s = (int)MathHelper.lerp(r, (float)leashedEntityBlockLight, (float)holdingEntityBlockLight);
      int t = (int)MathHelper.lerp(r, (float)leashedEntitySkyLight, (float)holdingEntitySkyLight);
      int u = LightmapTextureManager.pack(s, t);
      float v = pieceIndex % 2 == (isLeashKnot ? 1 : 0) ? 0.7F : 1.0F;
      float w = 0.5F * v;
      float x = 0.4F * v;
      float y = 0.3F * v;
      float z = f * r;
      float aa = g > 0.0F ? g * r * r : g - g * (1.0F - r) * (1.0F - r);
      float ab = h * r;
      vertexConsumer.vertex(positionMatrix, z - o, aa + n, ab + p).color(w, x, y, 1.0F).light(u).next();
      vertexConsumer.vertex(positionMatrix, z + o, aa + m - n, ab - p).color(w, x, y, 1.0F).light(u).next();
   }

   // $FF: synthetic method
   protected boolean hasLabel(LivingEntity arg) {
      return this.hasLabel((MobEntity)arg);
   }

   // $FF: synthetic method
   protected boolean hasLabel(Entity entity) {
      return this.hasLabel((MobEntity)entity);
   }
}
