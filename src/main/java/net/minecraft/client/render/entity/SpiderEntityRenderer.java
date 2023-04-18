package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.SpiderEyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SpiderEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/spider/spider.png");

   public SpiderEntityRenderer(EntityRendererFactory.Context arg) {
      this(arg, EntityModelLayers.SPIDER);
   }

   public SpiderEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
      super(ctx, new SpiderEntityModel(ctx.getPart(layer)), 0.8F);
      this.addFeature(new SpiderEyesFeatureRenderer(this));
   }

   protected float getLyingAngle(SpiderEntity arg) {
      return 180.0F;
   }

   public Identifier getTexture(SpiderEntity arg) {
      return TEXTURE;
   }

   // $FF: synthetic method
   protected float getLyingAngle(LivingEntity entity) {
      return this.getLyingAngle((SpiderEntity)entity);
   }
}
