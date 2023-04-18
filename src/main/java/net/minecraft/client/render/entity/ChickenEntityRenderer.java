package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.ChickenEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ChickenEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/chicken.png");

   public ChickenEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new ChickenEntityModel(arg.getPart(EntityModelLayers.CHICKEN)), 0.3F);
   }

   public Identifier getTexture(ChickenEntity arg) {
      return TEXTURE;
   }

   protected float getAnimationProgress(ChickenEntity arg, float f) {
      float g = MathHelper.lerp(f, arg.prevFlapProgress, arg.flapProgress);
      float h = MathHelper.lerp(f, arg.prevMaxWingDeviation, arg.maxWingDeviation);
      return (MathHelper.sin(g) + 1.0F) * h;
   }
}
