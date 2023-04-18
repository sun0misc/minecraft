package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.WolfCollarFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WolfEntityRenderer extends MobEntityRenderer {
   private static final Identifier WILD_TEXTURE = new Identifier("textures/entity/wolf/wolf.png");
   private static final Identifier TAMED_TEXTURE = new Identifier("textures/entity/wolf/wolf_tame.png");
   private static final Identifier ANGRY_TEXTURE = new Identifier("textures/entity/wolf/wolf_angry.png");

   public WolfEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new WolfEntityModel(arg.getPart(EntityModelLayers.WOLF)), 0.5F);
      this.addFeature(new WolfCollarFeatureRenderer(this));
   }

   protected float getAnimationProgress(WolfEntity arg, float f) {
      return arg.getTailAngle();
   }

   public void render(WolfEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      if (arg.isFurWet()) {
         float h = arg.getFurWetBrightnessMultiplier(g);
         ((WolfEntityModel)this.model).setColorMultiplier(h, h, h);
      }

      super.render((MobEntity)arg, f, g, arg2, arg3, i);
      if (arg.isFurWet()) {
         ((WolfEntityModel)this.model).setColorMultiplier(1.0F, 1.0F, 1.0F);
      }

   }

   public Identifier getTexture(WolfEntity arg) {
      if (arg.isTamed()) {
         return TAMED_TEXTURE;
      } else {
         return arg.hasAngerTime() ? ANGRY_TEXTURE : WILD_TEXTURE;
      }
   }
}
