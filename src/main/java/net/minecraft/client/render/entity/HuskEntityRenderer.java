package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class HuskEntityRenderer extends ZombieEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/zombie/husk.png");

   public HuskEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, EntityModelLayers.HUSK, EntityModelLayers.HUSK_INNER_ARMOR, EntityModelLayers.HUSK_OUTER_ARMOR);
   }

   protected void scale(ZombieEntity arg, MatrixStack arg2, float f) {
      float g = 1.0625F;
      arg2.scale(1.0625F, 1.0625F, 1.0625F);
      super.scale(arg, arg2, f);
   }

   public Identifier getTexture(ZombieEntity arg) {
      return TEXTURE;
   }
}
