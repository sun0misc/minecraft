package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CreeperEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/creeper/creeper.png");

   public CreeperEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new CreeperEntityModel(arg.getPart(EntityModelLayers.CREEPER)), 0.5F);
      this.addFeature(new CreeperChargeFeatureRenderer(this, arg.getModelLoader()));
   }

   protected void scale(CreeperEntity arg, MatrixStack arg2, float f) {
      float g = arg.getClientFuseTime(f);
      float h = 1.0F + MathHelper.sin(g * 100.0F) * g * 0.01F;
      g = MathHelper.clamp(g, 0.0F, 1.0F);
      g *= g;
      g *= g;
      float i = (1.0F + g * 0.4F) * h;
      float j = (1.0F + g * 0.1F) / h;
      arg2.scale(i, j, i);
   }

   protected float getAnimationCounter(CreeperEntity arg, float f) {
      float g = arg.getClientFuseTime(f);
      return (int)(g * 10.0F) % 2 == 0 ? 0.0F : MathHelper.clamp(g, 0.5F, 1.0F);
   }

   public Identifier getTexture(CreeperEntity arg) {
      return TEXTURE;
   }

   // $FF: synthetic method
   protected float getAnimationCounter(LivingEntity entity, float tickDelta) {
      return this.getAnimationCounter((CreeperEntity)entity, tickDelta);
   }
}
