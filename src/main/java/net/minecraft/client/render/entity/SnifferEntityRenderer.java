package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SnifferEntityModel;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SnifferEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/sniffer/sniffer.png");

   public SnifferEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new SnifferEntityModel(arg.getPart(EntityModelLayers.SNIFFER)), 1.1F);
   }

   public Identifier getTexture(SnifferEntity arg) {
      return TEXTURE;
   }
}
