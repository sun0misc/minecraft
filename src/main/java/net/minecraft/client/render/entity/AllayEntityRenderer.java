package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.AllayEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class AllayEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/allay/allay.png");

   public AllayEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new AllayEntityModel(arg.getPart(EntityModelLayers.ALLAY)), 0.4F);
      this.addFeature(new HeldItemFeatureRenderer(this, arg.getHeldItemRenderer()));
   }

   public Identifier getTexture(AllayEntity arg) {
      return TEXTURE;
   }

   protected int getBlockLight(AllayEntity arg, BlockPos arg2) {
      return 15;
   }
}
