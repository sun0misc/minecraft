package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.StriderEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class StriderEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/strider/strider.png");
   private static final Identifier COLD_TEXTURE = new Identifier("textures/entity/strider/strider_cold.png");

   public StriderEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new StriderEntityModel(arg.getPart(EntityModelLayers.STRIDER)), 0.5F);
      this.addFeature(new SaddleFeatureRenderer(this, new StriderEntityModel(arg.getPart(EntityModelLayers.STRIDER_SADDLE)), new Identifier("textures/entity/strider/strider_saddle.png")));
   }

   public Identifier getTexture(StriderEntity arg) {
      return arg.isCold() ? COLD_TEXTURE : TEXTURE;
   }

   protected void scale(StriderEntity arg, MatrixStack arg2, float f) {
      if (arg.isBaby()) {
         arg2.scale(0.5F, 0.5F, 0.5F);
         this.shadowRadius = 0.25F;
      } else {
         this.shadowRadius = 0.5F;
      }

   }

   protected boolean isShaking(StriderEntity arg) {
      return super.isShaking(arg) || arg.isCold();
   }

   // $FF: synthetic method
   protected boolean isShaking(LivingEntity entity) {
      return this.isShaking((StriderEntity)entity);
   }
}
