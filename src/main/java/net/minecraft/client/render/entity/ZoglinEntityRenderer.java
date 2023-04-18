package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.HoglinEntityModel;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ZoglinEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/hoglin/zoglin.png");

   public ZoglinEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new HoglinEntityModel(arg.getPart(EntityModelLayers.ZOGLIN)), 0.7F);
   }

   public Identifier getTexture(ZoglinEntity arg) {
      return TEXTURE;
   }
}
