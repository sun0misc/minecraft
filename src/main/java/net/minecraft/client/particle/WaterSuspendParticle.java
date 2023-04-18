package net.minecraft.client.particle;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class WaterSuspendParticle extends SpriteBillboardParticle {
   WaterSuspendParticle(ClientWorld world, SpriteProvider spriteProvider, double x, double y, double z) {
      super(world, x, y - 0.125, z);
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.setSprite(spriteProvider);
      this.scale *= this.random.nextFloat() * 0.6F + 0.2F;
      this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
      this.collidesWithWorld = false;
      this.velocityMultiplier = 1.0F;
      this.gravityStrength = 0.0F;
   }

   WaterSuspendParticle(ClientWorld world, SpriteProvider spriteProvider, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      super(world, x, y - 0.125, z, velocityX, velocityY, velocityZ);
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.setSprite(spriteProvider);
      this.scale *= this.random.nextFloat() * 0.6F + 0.6F;
      this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
      this.collidesWithWorld = false;
      this.velocityMultiplier = 1.0F;
      this.gravityStrength = 0.0F;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   @Environment(EnvType.CLIENT)
   public static class WarpedSporeFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public WarpedSporeFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         double j = (double)arg2.random.nextFloat() * -1.9 * (double)arg2.random.nextFloat() * 0.1;
         WaterSuspendParticle lv = new WaterSuspendParticle(arg2, this.spriteProvider, d, e, f, 0.0, j, 0.0);
         lv.setColor(0.1F, 0.1F, 0.3F);
         lv.setBoundingBoxSpacing(0.001F, 0.001F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class CrimsonSporeFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public CrimsonSporeFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         Random lv = arg2.random;
         double j = lv.nextGaussian() * 9.999999974752427E-7;
         double k = lv.nextGaussian() * 9.999999747378752E-5;
         double l = lv.nextGaussian() * 9.999999974752427E-7;
         WaterSuspendParticle lv2 = new WaterSuspendParticle(arg2, this.spriteProvider, d, e, f, j, k, l);
         lv2.setColor(0.9F, 0.4F, 0.5F);
         return lv2;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class SporeBlossomAirFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public SporeBlossomAirFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         WaterSuspendParticle lv = new WaterSuspendParticle(arg2, this.spriteProvider, d, e, f, 0.0, -0.800000011920929, 0.0) {
            public Optional getGroup() {
               return Optional.of(ParticleGroup.SPORE_BLOSSOM_AIR);
            }
         };
         lv.maxAge = MathHelper.nextBetween(arg2.random, 500, 1000);
         lv.gravityStrength = 0.01F;
         lv.setColor(0.32F, 0.5F, 0.22F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class UnderwaterFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public UnderwaterFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         WaterSuspendParticle lv = new WaterSuspendParticle(arg2, this.spriteProvider, d, e, f);
         lv.setColor(0.4F, 0.4F, 0.7F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
