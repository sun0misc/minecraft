package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.FrogEntityModel;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class FrogEntityRenderer extends MobEntityRenderer {
   public FrogEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new FrogEntityModel(arg.getPart(EntityModelLayers.FROG)), 0.3F);
   }

   public Identifier getTexture(FrogEntity arg) {
      return arg.getVariant().texture();
   }
}
