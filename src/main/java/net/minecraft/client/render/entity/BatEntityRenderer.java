package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BatEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/bat.png");

   public BatEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new BatEntityModel(arg.getPart(EntityModelLayers.BAT)), 0.25F);
   }

   public Identifier getTexture(BatEntity arg) {
      return TEXTURE;
   }

   protected void scale(BatEntity arg, MatrixStack arg2, float f) {
      arg2.scale(0.35F, 0.35F, 0.35F);
   }

   protected void setupTransforms(BatEntity arg, MatrixStack arg2, float f, float g, float h) {
      if (arg.isRoosting()) {
         arg2.translate(0.0F, -0.1F, 0.0F);
      } else {
         arg2.translate(0.0F, MathHelper.cos(f * 0.3F) * 0.1F, 0.0F);
      }

      super.setupTransforms(arg, arg2, f, g, h);
   }
}
