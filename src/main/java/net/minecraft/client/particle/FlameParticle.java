package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class FlameParticle extends AbstractSlowingParticle {
   FlameParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
      super(arg, d, e, f, g, h, i);
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
      return this.scale * (1.0F - g * g * 0.5F);
   }

   public int getBrightness(float tint) {
      float g = ((float)this.age + tint) / (float)this.maxAge;
      g = MathHelper.clamp(g, 0.0F, 1.0F);
      int i = super.getBrightness(tint);
      int j = i & 255;
      int k = i >> 16 & 255;
      j += (int)(g * 15.0F * 16.0F);
      if (j > 240) {
         j = 240;
      }

      return j | k << 16;
   }

   @Environment(EnvType.CLIENT)
   public static class SmallFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public SmallFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         FlameParticle lv = new FlameParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         lv.scale(0.5F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         FlameParticle lv = new FlameParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
