package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;

@Environment(EnvType.CLIENT)
public class SquidInkParticle extends AnimatedParticle {
   SquidInkParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, int color, SpriteProvider spriteProvider) {
      super(world, x, y, z, spriteProvider, 0.0F);
      this.velocityMultiplier = 0.92F;
      this.scale = 0.5F;
      this.setAlpha(1.0F);
      this.setColor((float)ColorHelper.Argb.getRed(color), (float)ColorHelper.Argb.getGreen(color), (float)ColorHelper.Argb.getBlue(color));
      this.maxAge = (int)((double)(this.scale * 12.0F) / (Math.random() * 0.800000011920929 + 0.20000000298023224));
      this.setSpriteForAge(spriteProvider);
      this.collidesWithWorld = false;
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
   }

   public void tick() {
      super.tick();
      if (!this.dead) {
         this.setSpriteForAge(this.spriteProvider);
         if (this.age > this.maxAge / 2) {
            this.setAlpha(1.0F - ((float)this.age - (float)(this.maxAge / 2)) / (float)this.maxAge);
         }

         if (this.world.getBlockState(BlockPos.ofFloored(this.x, this.y, this.z)).isAir()) {
            this.velocityY -= 0.007400000002235174;
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public static class GlowSquidInkFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public GlowSquidInkFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new SquidInkParticle(arg2, d, e, f, g, h, i, ColorHelper.Argb.getArgb(255, 204, 31, 102), this.spriteProvider);
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
         return new SquidInkParticle(arg2, d, e, f, g, h, i, ColorHelper.Argb.getArgb(255, 255, 255, 255), this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
