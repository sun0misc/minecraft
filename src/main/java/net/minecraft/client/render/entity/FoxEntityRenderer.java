package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.FoxHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class FoxEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/fox/fox.png");
   private static final Identifier SLEEPING_TEXTURE = new Identifier("textures/entity/fox/fox_sleep.png");
   private static final Identifier SNOW_TEXTURE = new Identifier("textures/entity/fox/snow_fox.png");
   private static final Identifier SLEEPING_SNOW_TEXTURE = new Identifier("textures/entity/fox/snow_fox_sleep.png");

   public FoxEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new FoxEntityModel(arg.getPart(EntityModelLayers.FOX)), 0.4F);
      this.addFeature(new FoxHeldItemFeatureRenderer(this, arg.getHeldItemRenderer()));
   }

   protected void setupTransforms(FoxEntity arg, MatrixStack arg2, float f, float g, float h) {
      super.setupTransforms(arg, arg2, f, g, h);
      if (arg.isChasing() || arg.isWalking()) {
         float i = -MathHelper.lerp(h, arg.prevPitch, arg.getPitch());
         arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i));
      }

   }

   public Identifier getTexture(FoxEntity arg) {
      if (arg.getVariant() == FoxEntity.Type.RED) {
         return arg.isSleeping() ? SLEEPING_TEXTURE : TEXTURE;
      } else {
         return arg.isSleeping() ? SLEEPING_SNOW_TEXTURE : SNOW_TEXTURE;
      }
   }
}
