package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WitherSkeletonEntityRenderer extends SkeletonEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/skeleton/wither_skeleton.png");

   public WitherSkeletonEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, EntityModelLayers.WITHER_SKELETON, EntityModelLayers.WITHER_SKELETON_INNER_ARMOR, EntityModelLayers.WITHER_SKELETON_OUTER_ARMOR);
   }

   public Identifier getTexture(AbstractSkeletonEntity arg) {
      return TEXTURE;
   }

   protected void scale(AbstractSkeletonEntity arg, MatrixStack arg2, float f) {
      arg2.scale(1.2F, 1.2F, 1.2F);
   }
}
