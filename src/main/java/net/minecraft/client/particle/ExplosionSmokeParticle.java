package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class ExplosionSmokeParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   protected ExplosionSmokeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z);
      this.gravityStrength = -0.1F;
      this.velocityMultiplier = 0.9F;
      this.spriteProvider = spriteProvider;
      this.velocityX = velocityX + (Math.random() * 2.0 - 1.0) * 0.05000000074505806;
      this.velocityY = velocityY + (Math.random() * 2.0 - 1.0) * 0.05000000074505806;
      this.velocityZ = velocityZ + (Math.random() * 2.0 - 1.0) * 0.05000000074505806;
      float j = this.random.nextFloat() * 0.3F + 0.7F;
      this.red = j;
      this.green = j;
      this.blue = j;
      this.scale = 0.1F * (this.random.nextFloat() * this.random.nextFloat() * 6.0F + 1.0F);
      this.maxAge = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
      this.setSpriteForAge(spriteProvider);
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      super.tick();
      this.setSpriteForAge(this.spriteProvider);
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new ExplosionSmokeParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
