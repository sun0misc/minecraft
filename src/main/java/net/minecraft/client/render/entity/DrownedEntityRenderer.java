package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.DrownedOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.DrownedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class DrownedEntityRenderer extends ZombieBaseEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/zombie/drowned.png");

   public DrownedEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new DrownedEntityModel(arg.getPart(EntityModelLayers.DROWNED)), new DrownedEntityModel(arg.getPart(EntityModelLayers.DROWNED_INNER_ARMOR)), new DrownedEntityModel(arg.getPart(EntityModelLayers.DROWNED_OUTER_ARMOR)));
      this.addFeature(new DrownedOverlayFeatureRenderer(this, arg.getModelLoader()));
   }

   public Identifier getTexture(ZombieEntity arg) {
      return TEXTURE;
   }

   protected void setupTransforms(DrownedEntity arg, MatrixStack arg2, float f, float g, float h) {
      super.setupTransforms(arg, arg2, f, g, h);
      float i = arg.getLeaningPitch(h);
      if (i > 0.0F) {
         float j = -10.0F - arg.getPitch();
         float k = MathHelper.lerp(i, 0.0F, j);
         arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k), 0.0F, arg.getHeight() / 2.0F, 0.0F);
      }

   }
}
