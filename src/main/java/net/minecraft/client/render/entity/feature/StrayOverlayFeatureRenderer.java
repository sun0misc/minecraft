package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class StrayOverlayFeatureRenderer extends FeatureRenderer {
   private static final Identifier SKIN = new Identifier("textures/entity/skeleton/stray_overlay.png");
   private final SkeletonEntityModel model;

   public StrayOverlayFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new SkeletonEntityModel(loader.getModelPart(EntityModelLayers.STRAY_OUTER));
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, MobEntity arg3, float f, float g, float h, float j, float k, float l) {
      render(this.getContextModel(), this.model, SKIN, arg, arg2, i, arg3, f, g, j, k, l, h, 1.0F, 1.0F, 1.0F);
   }
}
