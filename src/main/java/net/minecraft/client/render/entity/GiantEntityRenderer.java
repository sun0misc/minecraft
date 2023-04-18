package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GiantEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GiantEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/zombie/zombie.png");
   private final float scale;

   public GiantEntityRenderer(EntityRendererFactory.Context ctx, float scale) {
      super(ctx, new GiantEntityModel(ctx.getPart(EntityModelLayers.GIANT)), 0.5F * scale);
      this.scale = scale;
      this.addFeature(new HeldItemFeatureRenderer(this, ctx.getHeldItemRenderer()));
      this.addFeature(new ArmorFeatureRenderer(this, new GiantEntityModel(ctx.getPart(EntityModelLayers.GIANT_INNER_ARMOR)), new GiantEntityModel(ctx.getPart(EntityModelLayers.GIANT_OUTER_ARMOR)), ctx.getModelManager()));
   }

   protected void scale(GiantEntity arg, MatrixStack arg2, float f) {
      arg2.scale(this.scale, this.scale, this.scale);
   }

   public Identifier getTexture(GiantEntity arg) {
      return TEXTURE;
   }
}
