package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ElderGuardianEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GuardianEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class ElderGuardianAppearanceParticle extends Particle {
   private final Model model;
   private final RenderLayer layer;

   ElderGuardianAppearanceParticle(ClientWorld arg, double d, double e, double f) {
      super(arg, d, e, f);
      this.layer = RenderLayer.getEntityTranslucent(ElderGuardianEntityRenderer.TEXTURE);
      this.model = new GuardianEntityModel(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.ELDER_GUARDIAN));
      this.gravityStrength = 0.0F;
      this.maxAge = 30;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.CUSTOM;
   }

   public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
      float g = ((float)this.age + tickDelta) / (float)this.maxAge;
      float h = 0.05F + 0.5F * MathHelper.sin(g * 3.1415927F);
      MatrixStack lv = new MatrixStack();
      lv.multiply(camera.getRotation());
      lv.multiply(RotationAxis.POSITIVE_X.rotationDegrees(150.0F * g - 60.0F));
      lv.scale(-1.0F, -1.0F, 1.0F);
      lv.translate(0.0F, -1.101F, 1.5F);
      VertexConsumerProvider.Immediate lv2 = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
      VertexConsumer lv3 = lv2.getBuffer(this.layer);
      this.model.render(lv, lv3, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, h);
      lv2.draw();
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new ElderGuardianAppearanceParticle(arg2, d, e, f);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
