package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class SuspendParticle extends SpriteBillboardParticle {
   SuspendParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
      super(arg, d, e, f, g, h, i);
      float j = this.random.nextFloat() * 0.1F + 0.2F;
      this.red = j;
      this.green = j;
      this.blue = j;
      this.setBoundingBoxSpacing(0.02F, 0.02F);
      this.scale *= this.random.nextFloat() * 0.6F + 0.5F;
      this.velocityX *= 0.019999999552965164;
      this.velocityY *= 0.019999999552965164;
      this.velocityZ *= 0.019999999552965164;
      this.maxAge = (int)(20.0 / (Math.random() * 0.8 + 0.2));
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double dx, double dy, double dz) {
      this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
      this.repositionFromBoundingBox();
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.maxAge-- <= 0) {
         this.markDead();
      } else {
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityX *= 0.99;
         this.velocityY *= 0.99;
         this.velocityZ *= 0.99;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class EggCrackFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public EggCrackFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         lv.setColor(1.0F, 1.0F, 1.0F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class DolphinFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public DolphinFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
         lv.setColor(0.3F, 0.5F, 1.0F);
         lv.setSprite(this.spriteProvider);
         lv.setAlpha(1.0F - arg2.random.nextFloat() * 0.7F);
         lv.setMaxAge(lv.getMaxAge() / 2);
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
         SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         lv.setColor(1.0F, 1.0F, 1.0F);
         lv.setMaxAge(3 + arg2.getRandom().nextInt(5));
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class HappyVillagerFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public HappyVillagerFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         lv.setColor(1.0F, 1.0F, 1.0F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class MyceliumFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public MyceliumFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         SuspendParticle lv = new SuspendParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
