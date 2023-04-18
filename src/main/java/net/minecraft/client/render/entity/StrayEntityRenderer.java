package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.StrayOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class StrayEntityRenderer extends SkeletonEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/skeleton/stray.png");

   public StrayEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, EntityModelLayers.STRAY, EntityModelLayers.STRAY_INNER_ARMOR, EntityModelLayers.STRAY_OUTER_ARMOR);
      this.addFeature(new StrayOverlayFeatureRenderer(this, arg.getModelLoader()));
   }

   public Identifier getTexture(AbstractSkeletonEntity arg) {
      return TEXTURE;
   }
}
