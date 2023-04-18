package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BlazeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class BlazeEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/blaze.png");

   public BlazeEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new BlazeEntityModel(arg.getPart(EntityModelLayers.BLAZE)), 0.5F);
   }

   protected int getBlockLight(BlazeEntity arg, BlockPos arg2) {
      return 15;
   }

   public Identifier getTexture(BlazeEntity arg) {
      return TEXTURE;
   }
}
