package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EvokerEntityRenderer extends IllagerEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/illager/evoker.png");

   public EvokerEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new IllagerEntityModel(arg.getPart(EntityModelLayers.EVOKER)), 0.5F);
      this.addFeature(new HeldItemFeatureRenderer(this, arg.getHeldItemRenderer()) {
         public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, SpellcastingIllagerEntity arg3, float f, float g, float h, float j, float k, float l) {
            if (arg3.isSpellcasting()) {
               super.render(arg, arg2, i, (LivingEntity)arg3, f, g, h, j, k, l);
            }

         }
      });
   }

   public Identifier getTexture(SpellcastingIllagerEntity arg) {
      return TEXTURE;
   }
}
