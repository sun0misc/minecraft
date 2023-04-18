package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.DolphinHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.DolphinEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DolphinEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/dolphin.png");

   public DolphinEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new DolphinEntityModel(arg.getPart(EntityModelLayers.DOLPHIN)), 0.7F);
      this.addFeature(new DolphinHeldItemFeatureRenderer(this, arg.getHeldItemRenderer()));
   }

   public Identifier getTexture(DolphinEntity arg) {
      return TEXTURE;
   }
}
