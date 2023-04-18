package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.MinecartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class MinecartEntityRenderer extends EntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/minecart.png");
   protected final EntityModel model;
   private final BlockRenderManager blockRenderManager;

   public MinecartEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
      super(ctx);
      this.shadowRadius = 0.7F;
      this.model = new MinecartEntityModel(ctx.getPart(layer));
      this.blockRenderManager = ctx.getBlockRenderManager();
   }

   public void render(AbstractMinecartEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      super.render(arg, f, g, arg2, arg3, i);
      arg2.push();
      long l = (long)arg.getId() * 493286711L;
      l = l * l * 4392167121L + l * 98761L;
      float h = (((float)(l >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float j = (((float)(l >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float k = (((float)(l >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      arg2.translate(h, j, k);
      double d = MathHelper.lerp((double)g, arg.lastRenderX, arg.getX());
      double e = MathHelper.lerp((double)g, arg.lastRenderY, arg.getY());
      double m = MathHelper.lerp((double)g, arg.lastRenderZ, arg.getZ());
      double n = 0.30000001192092896;
      Vec3d lv = arg.snapPositionToRail(d, e, m);
      float o = MathHelper.lerp(g, arg.prevPitch, arg.getPitch());
      if (lv != null) {
         Vec3d lv2 = arg.snapPositionToRailWithOffset(d, e, m, 0.30000001192092896);
         Vec3d lv3 = arg.snapPositionToRailWithOffset(d, e, m, -0.30000001192092896);
         if (lv2 == null) {
            lv2 = lv;
         }

         if (lv3 == null) {
            lv3 = lv;
         }

         arg2.translate(lv.x - d, (lv2.y + lv3.y) / 2.0 - e, lv.z - m);
         Vec3d lv4 = lv3.add(-lv2.x, -lv2.y, -lv2.z);
         if (lv4.length() != 0.0) {
            lv4 = lv4.normalize();
            f = (float)(Math.atan2(lv4.z, lv4.x) * 180.0 / Math.PI);
            o = (float)(Math.atan(lv4.y) * 73.0);
         }
      }

      arg2.translate(0.0F, 0.375F, 0.0F);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - f));
      arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-o));
      float p = (float)arg.getDamageWobbleTicks() - g;
      float q = arg.getDamageWobbleStrength() - g;
      if (q < 0.0F) {
         q = 0.0F;
      }

      if (p > 0.0F) {
         arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(p) * p * q / 10.0F * (float)arg.getDamageWobbleSide()));
      }

      int r = arg.getBlockOffset();
      BlockState lv5 = arg.getContainedBlock();
      if (lv5.getRenderType() != BlockRenderType.INVISIBLE) {
         arg2.push();
         float s = 0.75F;
         arg2.scale(0.75F, 0.75F, 0.75F);
         arg2.translate(-0.5F, (float)(r - 8) / 16.0F, 0.5F);
         arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
         this.renderBlock(arg, g, lv5, arg2, arg3, i);
         arg2.pop();
      }

      arg2.scale(-1.0F, -1.0F, 1.0F);
      this.model.setAngles(arg, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      VertexConsumer lv6 = arg3.getBuffer(this.model.getLayer(this.getTexture(arg)));
      this.model.render(arg2, lv6, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
      arg2.pop();
   }

   public Identifier getTexture(AbstractMinecartEntity arg) {
      return TEXTURE;
   }

   protected void renderBlock(AbstractMinecartEntity entity, float delta, BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      this.blockRenderManager.renderBlockAsEntity(state, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
   }
}
