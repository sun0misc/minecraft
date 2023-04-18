package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CowEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/cow/cow.png");

   public CowEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new CowEntityModel(arg.getPart(EntityModelLayers.COW)), 0.7F);
   }

   public Identifier getTexture(CowEntity arg) {
      return TEXTURE;
   }
}
