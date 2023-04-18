package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class WaterSplashParticle extends RainSplashParticle {
   WaterSplashParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
      super(arg, d, e, f);
      this.gravityStrength = 0.04F;
      if (h == 0.0 && (g != 0.0 || i != 0.0)) {
         this.velocityX = g;
         this.velocityY = 0.1;
         this.velocityZ = i;
      }

   }

   @Environment(EnvType.CLIENT)
   public static class SplashFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public SplashFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         WaterSplashParticle lv = new WaterSplashParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
