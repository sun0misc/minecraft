package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DamageParticle extends SpriteBillboardParticle {
   DamageParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
      super(arg, d, e, f, 0.0, 0.0, 0.0);
      this.velocityMultiplier = 0.7F;
      this.gravityStrength = 0.5F;
      this.velocityX *= 0.10000000149011612;
      this.velocityY *= 0.10000000149011612;
      this.velocityZ *= 0.10000000149011612;
      this.velocityX += g * 0.4;
      this.velocityY += h * 0.4;
      this.velocityZ += i * 0.4;
      float j = (float)(Math.random() * 0.30000001192092896 + 0.6000000238418579);
      this.red = j;
      this.green = j;
      this.blue = j;
      this.scale *= 0.75F;
      this.maxAge = Math.max((int)(6.0 / (Math.random() * 0.8 + 0.6)), 1);
      this.collidesWithWorld = false;
      this.tick();
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      super.tick();
      this.green *= 0.96F;
      this.blue *= 0.9F;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   @Environment(EnvType.CLIENT)
   public static class DefaultFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public DefaultFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         DamageParticle lv = new DamageParticle(arg2, d, e, f, g, h + 1.0, i);
         lv.setMaxAge(20);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class EnchantedHitFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public EnchantedHitFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         DamageParticle lv = new DamageParticle(arg2, d, e, f, g, h, i);
         lv.red *= 0.3F;
         lv.green *= 0.8F;
         lv.setSprite(this.spriteProvider);
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
         DamageParticle lv = new DamageParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
