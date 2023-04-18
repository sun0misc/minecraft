package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.SnowmanPumpkinFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SnowGolemEntityModel;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SnowGolemEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/snow_golem.png");

   public SnowGolemEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new SnowGolemEntityModel(arg.getPart(EntityModelLayers.SNOW_GOLEM)), 0.5F);
      this.addFeature(new SnowmanPumpkinFeatureRenderer(this, arg.getBlockRenderManager(), arg.getItemRenderer()));
   }

   public Identifier getTexture(SnowGolemEntity arg) {
      return TEXTURE;
   }
}
