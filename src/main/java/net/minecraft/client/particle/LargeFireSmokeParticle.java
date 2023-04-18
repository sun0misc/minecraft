package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class LargeFireSmokeParticle extends FireSmokeParticle {
   protected LargeFireSmokeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, velocityX, velocityY, velocityZ, 2.5F, spriteProvider);
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new LargeFireSmokeParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
