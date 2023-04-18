package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SalmonEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class SalmonEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/fish/salmon.png");

   public SalmonEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new SalmonEntityModel(arg.getPart(EntityModelLayers.SALMON)), 0.4F);
   }

   public Identifier getTexture(SalmonEntity arg) {
      return TEXTURE;
   }

   protected void setupTransforms(SalmonEntity arg, MatrixStack arg2, float f, float g, float h) {
      super.setupTransforms(arg, arg2, f, g, h);
      float i = 1.0F;
      float j = 1.0F;
      if (!arg.isTouchingWater()) {
         i = 1.3F;
         j = 1.7F;
      }

      float k = i * 4.3F * MathHelper.sin(j * 0.6F * f);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(k));
      arg2.translate(0.0F, 0.0F, -0.4F);
      if (!arg.isTouchingWater()) {
         arg2.translate(0.2F, 0.1F, 0.0F);
         arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
      }

   }
}
