package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TadpoleEntityModel;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TadpoleEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/tadpole/tadpole.png");

   public TadpoleEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new TadpoleEntityModel(arg.getPart(EntityModelLayers.TADPOLE)), 0.14F);
   }

   public Identifier getTexture(TadpoleEntity arg) {
      return TEXTURE;
   }
}
