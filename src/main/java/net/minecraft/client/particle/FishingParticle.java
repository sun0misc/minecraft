package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class FishingParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   FishingParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.0, 0.0, 0.0);
      this.spriteProvider = spriteProvider;
      this.velocityX *= 0.30000001192092896;
      this.velocityY = Math.random() * 0.20000000298023224 + 0.10000000149011612;
      this.velocityZ *= 0.30000001192092896;
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.maxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2));
      this.setSpriteForAge(spriteProvider);
      this.gravityStrength = 0.0F;
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      int i = 60 - this.maxAge;
      if (this.maxAge-- <= 0) {
         this.markDead();
      } else {
         this.velocityY -= (double)this.gravityStrength;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityX *= 0.9800000190734863;
         this.velocityY *= 0.9800000190734863;
         this.velocityZ *= 0.9800000190734863;
         float f = (float)i * 0.001F;
         this.setBoundingBoxSpacing(f, f);
         this.setSprite(this.spriteProvider.getSprite(i % 4, 4));
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new FishingParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
