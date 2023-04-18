package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class PillagerEntityRenderer extends IllagerEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/illager/pillager.png");

   public PillagerEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new IllagerEntityModel(arg.getPart(EntityModelLayers.PILLAGER)), 0.5F);
      this.addFeature(new HeldItemFeatureRenderer(this, arg.getHeldItemRenderer()));
   }

   public Identifier getTexture(PillagerEntity arg) {
      return TEXTURE;
   }
}
