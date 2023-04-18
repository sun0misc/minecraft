package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ShulkerBulletEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class ShulkerBulletEntityRenderer extends EntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/shulker/spark.png");
   private static final RenderLayer LAYER;
   private final ShulkerBulletEntityModel model;

   public ShulkerBulletEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.model = new ShulkerBulletEntityModel(arg.getPart(EntityModelLayers.SHULKER_BULLET));
   }

   protected int getBlockLight(ShulkerBulletEntity arg, BlockPos arg2) {
      return 15;
   }

   public void render(ShulkerBulletEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      float h = MathHelper.lerpAngleDegrees(g, arg.prevYaw, arg.getYaw());
      float j = MathHelper.lerp(g, arg.prevPitch, arg.getPitch());
      float k = (float)arg.age + g;
      arg2.translate(0.0F, 0.15F, 0.0F);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.sin(k * 0.1F) * 180.0F));
      arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.cos(k * 0.1F) * 180.0F));
      arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(k * 0.15F) * 360.0F));
      arg2.scale(-0.5F, -0.5F, 0.5F);
      this.model.setAngles(arg, 0.0F, 0.0F, 0.0F, h, j);
      VertexConsumer lv = arg3.getBuffer(this.model.getLayer(TEXTURE));
      this.model.render(arg2, lv, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
      arg2.scale(1.5F, 1.5F, 1.5F);
      VertexConsumer lv2 = arg3.getBuffer(LAYER);
      this.model.render(arg2, lv2, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 0.15F);
      arg2.pop();
      super.render(arg, f, g, arg2, arg3, i);
   }

   public Identifier getTexture(ShulkerBulletEntity arg) {
      return TEXTURE;
   }

   static {
      LAYER = RenderLayer.getEntityTranslucent(TEXTURE);
   }
}
