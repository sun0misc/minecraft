package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.TropicalFishColorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LargeTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.SmallTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.TintableCompositeModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class TropicalFishEntityRenderer extends MobEntityRenderer {
   private final TintableCompositeModel smallModel = (TintableCompositeModel)this.getModel();
   private final TintableCompositeModel largeModel;
   private static final Identifier A_TEXTURE = new Identifier("textures/entity/fish/tropical_a.png");
   private static final Identifier B_TEXTURE = new Identifier("textures/entity/fish/tropical_b.png");

   public TropicalFishEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new SmallTropicalFishEntityModel(arg.getPart(EntityModelLayers.TROPICAL_FISH_SMALL)), 0.15F);
      this.largeModel = new LargeTropicalFishEntityModel(arg.getPart(EntityModelLayers.TROPICAL_FISH_LARGE));
      this.addFeature(new TropicalFishColorFeatureRenderer(this, arg.getModelLoader()));
   }

   public Identifier getTexture(TropicalFishEntity arg) {
      Identifier var10000;
      switch (arg.getVariant().getSize()) {
         case SMALL:
            var10000 = A_TEXTURE;
            break;
         case LARGE:
            var10000 = B_TEXTURE;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public void render(TropicalFishEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      TintableCompositeModel var10000;
      switch (arg.getVariant().getSize()) {
         case SMALL:
            var10000 = this.smallModel;
            break;
         case LARGE:
            var10000 = this.largeModel;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      TintableCompositeModel lv = var10000;
      this.model = lv;
      float[] fs = arg.getBaseColorComponents().getColorComponents();
      lv.setColorMultiplier(fs[0], fs[1], fs[2]);
      super.render((MobEntity)arg, f, g, arg2, arg3, i);
      lv.setColorMultiplier(1.0F, 1.0F, 1.0F);
   }

   protected void setupTransforms(TropicalFishEntity arg, MatrixStack arg2, float f, float g, float h) {
      super.setupTransforms(arg, arg2, f, g, h);
      float i = 4.3F * MathHelper.sin(0.6F * f);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i));
      if (!arg.isTouchingWater()) {
         arg2.translate(0.2F, 0.1F, 0.0F);
         arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
      }

   }
}
