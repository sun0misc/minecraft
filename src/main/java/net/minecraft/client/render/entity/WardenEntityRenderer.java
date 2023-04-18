package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.WardenFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WardenEntityModel;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class WardenEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/warden/warden.png");
   private static final Identifier BIOLUMINESCENT_LAYER_TEXTURE = new Identifier("textures/entity/warden/warden_bioluminescent_layer.png");
   private static final Identifier HEART_TEXTURE = new Identifier("textures/entity/warden/warden_heart.png");
   private static final Identifier PULSATING_SPOTS_1_TEXTURE = new Identifier("textures/entity/warden/warden_pulsating_spots_1.png");
   private static final Identifier PULSATING_SPOTS_2_TEXTURE = new Identifier("textures/entity/warden/warden_pulsating_spots_2.png");

   public WardenEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new WardenEntityModel(arg.getPart(EntityModelLayers.WARDEN)), 0.9F);
      this.addFeature(new WardenFeatureRenderer(this, BIOLUMINESCENT_LAYER_TEXTURE, (warden, tickDelta, animationProgress) -> {
         return 1.0F;
      }, WardenEntityModel::getHeadAndLimbs));
      this.addFeature(new WardenFeatureRenderer(this, PULSATING_SPOTS_1_TEXTURE, (warden, tickDelta, animationProgress) -> {
         return Math.max(0.0F, MathHelper.cos(animationProgress * 0.045F) * 0.25F);
      }, WardenEntityModel::getBodyHeadAndLimbs));
      this.addFeature(new WardenFeatureRenderer(this, PULSATING_SPOTS_2_TEXTURE, (warden, tickDelta, animationProgress) -> {
         return Math.max(0.0F, MathHelper.cos(animationProgress * 0.045F + 3.1415927F) * 0.25F);
      }, WardenEntityModel::getBodyHeadAndLimbs));
      this.addFeature(new WardenFeatureRenderer(this, TEXTURE, (warden, tickDelta, animationProgress) -> {
         return warden.getTendrilPitch(tickDelta);
      }, WardenEntityModel::getTendrils));
      this.addFeature(new WardenFeatureRenderer(this, HEART_TEXTURE, (warden, tickDelta, animationProgress) -> {
         return warden.getHeartPitch(tickDelta);
      }, WardenEntityModel::getBody));
   }

   public Identifier getTexture(WardenEntity arg) {
      return TEXTURE;
   }
}
