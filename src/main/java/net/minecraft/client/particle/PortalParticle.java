package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class PortalParticle extends SpriteBillboardParticle {
   private final double startX;
   private final double startY;
   private final double startZ;

   protected PortalParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
      super(arg, d, e, f);
      this.velocityX = g;
      this.velocityY = h;
      this.velocityZ = i;
      this.x = d;
      this.y = e;
      this.z = f;
      this.startX = this.x;
      this.startY = this.y;
      this.startZ = this.z;
      this.scale = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
      float j = this.random.nextFloat() * 0.6F + 0.4F;
      this.red = j * 0.9F;
      this.green = j * 0.3F;
      this.blue = j;
      this.maxAge = (int)(Math.random() * 10.0) + 40;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double dx, double dy, double dz) {
      this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
      this.repositionFromBoundingBox();
   }

   public float getSize(float tickDelta) {
      float g = ((float)this.age + tickDelta) / (float)this.maxAge;
      g = 1.0F - g;
      g *= g;
      g = 1.0F - g;
      return this.scale * g;
   }

   public int getBrightness(float tint) {
      int i = super.getBrightness(tint);
      float g = (float)this.age / (float)this.maxAge;
      g *= g;
      g *= g;
      int j = i & 255;
      int k = i >> 16 & 255;
      k += (int)(g * 15.0F * 16.0F);
      if (k > 240) {
         k = 240;
      }

      return j | k << 16;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         float f = (float)this.age / (float)this.maxAge;
         float g = f;
         f = -f + f * f * 2.0F;
         f = 1.0F - f;
         this.x = this.startX + this.velocityX * (double)f;
         this.y = this.startY + this.velocityY * (double)f + (double)(1.0F - g);
         this.z = this.startZ + this.velocityZ * (double)f;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         PortalParticle lv = new PortalParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
