package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GhastEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GhastEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/ghast/ghast.png");
   private static final Identifier ANGRY_TEXTURE = new Identifier("textures/entity/ghast/ghast_shooting.png");

   public GhastEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new GhastEntityModel(arg.getPart(EntityModelLayers.GHAST)), 1.5F);
   }

   public Identifier getTexture(GhastEntity arg) {
      return arg.isShooting() ? ANGRY_TEXTURE : TEXTURE;
   }

   protected void scale(GhastEntity arg, MatrixStack arg2, float f) {
      float g = 1.0F;
      float h = 4.5F;
      float i = 4.5F;
      arg2.scale(4.5F, 4.5F, 4.5F);
   }
}
