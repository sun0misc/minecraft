package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EvokerFangsEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class EvokerFangsEntityRenderer extends EntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/illager/evoker_fangs.png");
   private final EvokerFangsEntityModel model;

   public EvokerFangsEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.model = new EvokerFangsEntityModel(arg.getPart(EntityModelLayers.EVOKER_FANGS));
   }

   public void render(EvokerFangsEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      float h = arg.getAnimationProgress(g);
      if (h != 0.0F) {
         float j = 2.0F;
         if (h > 0.9F) {
            j *= (1.0F - h) / 0.1F;
         }

         arg2.push();
         arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F - arg.getYaw()));
         arg2.scale(-j, -j, j);
         float k = 0.03125F;
         arg2.translate(0.0, -0.626, 0.0);
         arg2.scale(0.5F, 0.5F, 0.5F);
         this.model.setAngles(arg, h, 0.0F, 0.0F, arg.getYaw(), arg.getPitch());
         VertexConsumer lv = arg3.getBuffer(this.model.getLayer(TEXTURE));
         this.model.render(arg2, lv, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
         arg2.pop();
         super.render(arg, f, g, arg2, arg3, i);
      }
   }

   public Identifier getTexture(EvokerFangsEntity arg) {
      return TEXTURE;
   }
}
