package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.SheepWoolFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SheepEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/sheep/sheep.png");

   public SheepEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new SheepEntityModel(arg.getPart(EntityModelLayers.SHEEP)), 0.7F);
      this.addFeature(new SheepWoolFeatureRenderer(this, arg.getModelLoader()));
   }

   public Identifier getTexture(SheepEntity arg) {
      return TEXTURE;
   }
}
