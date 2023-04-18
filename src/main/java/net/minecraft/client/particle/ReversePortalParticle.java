package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class ReversePortalParticle extends PortalParticle {
   ReversePortalParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
      super(arg, d, e, f, g, h, i);
      this.scale *= 1.5F;
      this.maxAge = (int)(Math.random() * 2.0) + 60;
   }

   public float getSize(float tickDelta) {
      float g = 1.0F - ((float)this.age + tickDelta) / ((float)this.maxAge * 1.5F);
      return this.scale * g;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         float f = (float)this.age / (float)this.maxAge;
         this.x += this.velocityX * (double)f;
         this.y += this.velocityY * (double)f;
         this.z += this.velocityZ * (double)f;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         ReversePortalParticle lv = new ReversePortalParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
