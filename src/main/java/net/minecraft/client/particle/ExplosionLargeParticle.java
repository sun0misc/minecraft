package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class ExplosionLargeParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   protected ExplosionLargeParticle(ClientWorld world, double x, double y, double z, double g, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.0, 0.0, 0.0);
      this.maxAge = 6 + this.random.nextInt(4);
      float h = this.random.nextFloat() * 0.6F + 0.4F;
      this.red = h;
      this.green = h;
      this.blue = h;
      this.scale = 2.0F * (1.0F - (float)g * 0.5F);
      this.spriteProvider = spriteProvider;
      this.setSpriteForAge(spriteProvider);
   }

   public int getBrightness(float tint) {
      return 15728880;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.setSpriteForAge(this.spriteProvider);
      }
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_LIT;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new ExplosionLargeParticle(arg2, d, e, f, g, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
