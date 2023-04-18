package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LlamaSpitEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class LlamaSpitEntityRenderer extends EntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/llama/spit.png");
   private final LlamaSpitEntityModel model;

   public LlamaSpitEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.model = new LlamaSpitEntityModel(arg.getPart(EntityModelLayers.LLAMA_SPIT));
   }

   public void render(LlamaSpitEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      arg2.translate(0.0F, 0.15F, 0.0F);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(g, arg.prevYaw, arg.getYaw()) - 90.0F));
      arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(g, arg.prevPitch, arg.getPitch())));
      this.model.setAngles(arg, g, 0.0F, -0.1F, 0.0F, 0.0F);
      VertexConsumer lv = arg3.getBuffer(this.model.getLayer(TEXTURE));
      this.model.render(arg2, lv, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
      arg2.pop();
      super.render(arg, f, g, arg2, arg3, i);
   }

   public Identifier getTexture(LlamaSpitEntity arg) {
      return TEXTURE;
   }
}
