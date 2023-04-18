package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CloudParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   CloudParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.0, 0.0, 0.0);
      this.velocityMultiplier = 0.96F;
      this.spriteProvider = spriteProvider;
      float j = 2.5F;
      this.velocityX *= 0.10000000149011612;
      this.velocityY *= 0.10000000149011612;
      this.velocityZ *= 0.10000000149011612;
      this.velocityX += velocityX;
      this.velocityY += velocityY;
      this.velocityZ += velocityZ;
      float k = 1.0F - (float)(Math.random() * 0.30000001192092896);
      this.red = k;
      this.green = k;
      this.blue = k;
      this.scale *= 1.875F;
      int l = (int)(8.0 / (Math.random() * 0.8 + 0.3));
      this.maxAge = (int)Math.max((float)l * 2.5F, 1.0F);
      this.collidesWithWorld = false;
      this.setSpriteForAge(spriteProvider);
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      super.tick();
      if (!this.dead) {
         this.setSpriteForAge(this.spriteProvider);
         PlayerEntity lv = this.world.getClosestPlayer(this.x, this.y, this.z, 2.0, false);
         if (lv != null) {
            double d = lv.getY();
            if (this.y > d) {
               this.y += (d - this.y) * 0.2;
               this.velocityY += (lv.getVelocity().y - this.velocityY) * 0.2;
               this.setPos(this.x, this.y, this.z);
            }
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public static class SneezeFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public SneezeFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         Particle lv = new CloudParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
         lv.setColor(200.0F, 50.0F, 120.0F);
         lv.setAlpha(0.4F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class CloudFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public CloudFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new CloudParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
