package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.IronGolemCrackFeatureRenderer;
import net.minecraft.client.render.entity.feature.IronGolemFlowerFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class IronGolemEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/iron_golem/iron_golem.png");

   public IronGolemEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new IronGolemEntityModel(arg.getPart(EntityModelLayers.IRON_GOLEM)), 0.7F);
      this.addFeature(new IronGolemCrackFeatureRenderer(this));
      this.addFeature(new IronGolemFlowerFeatureRenderer(this, arg.getBlockRenderManager()));
   }

   public Identifier getTexture(IronGolemEntity arg) {
      return TEXTURE;
   }

   protected void setupTransforms(IronGolemEntity arg, MatrixStack arg2, float f, float g, float h) {
      super.setupTransforms(arg, arg2, f, g, h);
      if (!((double)arg.limbAnimator.getSpeed() < 0.01)) {
         float i = 13.0F;
         float j = arg.limbAnimator.getPos(h) + 6.0F;
         float k = (Math.abs(j % 13.0F - 6.5F) - 3.25F) / 3.25F;
         arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(6.5F * k));
      }
   }
}
