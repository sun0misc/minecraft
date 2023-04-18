package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class EmotionParticle extends SpriteBillboardParticle {
   EmotionParticle(ClientWorld arg, double d, double e, double f) {
      super(arg, d, e, f, 0.0, 0.0, 0.0);
      this.field_28787 = true;
      this.velocityMultiplier = 0.86F;
      this.velocityX *= 0.009999999776482582;
      this.velocityY *= 0.009999999776482582;
      this.velocityZ *= 0.009999999776482582;
      this.velocityY += 0.1;
      this.scale *= 1.5F;
      this.maxAge = 16;
      this.collidesWithWorld = false;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   @Environment(EnvType.CLIENT)
   public static class AngryVillagerFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public AngryVillagerFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         EmotionParticle lv = new EmotionParticle(arg2, d, e + 0.5, f);
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
   public static class HeartFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public HeartFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         EmotionParticle lv = new EmotionParticle(arg2, d, e, f);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
