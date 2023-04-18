package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class GlowParticle extends SpriteBillboardParticle {
   static final Random RANDOM = Random.create();
   private final SpriteProvider spriteProvider;

   GlowParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, velocityX, velocityY, velocityZ);
      this.velocityMultiplier = 0.96F;
      this.field_28787 = true;
      this.spriteProvider = spriteProvider;
      this.scale *= 0.75F;
      this.collidesWithWorld = false;
      this.setSpriteForAge(spriteProvider);
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
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

   public void tick() {
      super.tick();
      this.setSpriteForAge(this.spriteProvider);
   }

   @Environment(EnvType.CLIENT)
   public static class ScrapeFactory implements ParticleFactory {
      private final double velocityMultiplier = 0.01;
      private final SpriteProvider spriteProvider;

      public ScrapeFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
         if (arg2.random.nextBoolean()) {
            lv.setColor(0.29F, 0.58F, 0.51F);
         } else {
            lv.setColor(0.43F, 0.77F, 0.62F);
         }

         lv.setVelocity(g * 0.01, h * 0.01, i * 0.01);
         int j = true;
         int k = true;
         lv.setMaxAge(arg2.random.nextInt(30) + 10);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ElectricSparkFactory implements ParticleFactory {
      private final double velocityMultiplier = 0.25;
      private final SpriteProvider spriteProvider;

      public ElectricSparkFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
         lv.setColor(1.0F, 0.9F, 1.0F);
         lv.setVelocity(g * 0.25, h * 0.25, i * 0.25);
         int j = true;
         int k = true;
         lv.setMaxAge(arg2.random.nextInt(2) + 2);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WaxOffFactory implements ParticleFactory {
      private final double velocityMultiplier = 0.01;
      private final SpriteProvider spriteProvider;

      public WaxOffFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
         lv.setColor(1.0F, 0.9F, 1.0F);
         lv.setVelocity(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
         int j = true;
         int k = true;
         lv.setMaxAge(arg2.random.nextInt(30) + 10);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WaxOnFactory implements ParticleFactory {
      private final double velocityMultiplier = 0.01;
      private final SpriteProvider spriteProvider;

      public WaxOnFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.0, 0.0, 0.0, this.spriteProvider);
         lv.setColor(0.91F, 0.55F, 0.08F);
         lv.setVelocity(g * 0.01 / 2.0, h * 0.01, i * 0.01 / 2.0);
         int j = true;
         int k = true;
         lv.setMaxAge(arg2.random.nextInt(30) + 10);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class GlowFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public GlowFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         GlowParticle lv = new GlowParticle(arg2, d, e, f, 0.5 - GlowParticle.RANDOM.nextDouble(), h, 0.5 - GlowParticle.RANDOM.nextDouble(), this.spriteProvider);
         if (arg2.random.nextBoolean()) {
            lv.setColor(0.6F, 1.0F, 0.8F);
         } else {
            lv.setColor(0.08F, 0.4F, 0.4F);
         }

         lv.velocityY *= 0.20000000298023224;
         if (g == 0.0 && i == 0.0) {
            lv.velocityX *= 0.10000000149011612;
            lv.velocityZ *= 0.10000000149011612;
         }

         lv.setMaxAge((int)(8.0 / (arg2.random.nextDouble() * 0.8 + 0.2)));
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
