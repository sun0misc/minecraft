package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class SquidEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/squid/squid.png");

   public SquidEntityRenderer(EntityRendererFactory.Context ctx, SquidEntityModel model) {
      super(ctx, model, 0.7F);
   }

   public Identifier getTexture(SquidEntity arg) {
      return TEXTURE;
   }

   protected void setupTransforms(SquidEntity arg, MatrixStack arg2, float f, float g, float h) {
      float i = MathHelper.lerp(h, arg.prevTiltAngle, arg.tiltAngle);
      float j = MathHelper.lerp(h, arg.prevRollAngle, arg.rollAngle);
      arg2.translate(0.0F, 0.5F, 0.0F);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - g));
      arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i));
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
      arg2.translate(0.0F, -1.2F, 0.0F);
   }

   protected float getAnimationProgress(SquidEntity arg, float f) {
      return MathHelper.lerp(f, arg.prevTentacleAngle, arg.tentacleAngle);
   }
}
