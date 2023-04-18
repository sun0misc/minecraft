package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class RedDustParticle extends AbstractDustParticle {
   protected RedDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, DustParticleEffect parameters, SpriteProvider spriteProvider) {
      super(world, x, y, z, velocityX, velocityY, velocityZ, parameters, spriteProvider);
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DustParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new RedDustParticle(arg2, d, e, f, g, h, i, arg, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DustParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
