package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VexEntityModel;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class VexEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/illager/vex.png");
   private static final Identifier CHARGING_TEXTURE = new Identifier("textures/entity/illager/vex_charging.png");

   public VexEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new VexEntityModel(arg.getPart(EntityModelLayers.VEX)), 0.3F);
      this.addFeature(new HeldItemFeatureRenderer(this, arg.getHeldItemRenderer()));
   }

   protected int getBlockLight(VexEntity arg, BlockPos arg2) {
      return 15;
   }

   public Identifier getTexture(VexEntity arg) {
      return arg.isCharging() ? CHARGING_TEXTURE : TEXTURE;
   }
}
