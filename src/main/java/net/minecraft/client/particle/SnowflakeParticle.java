package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class SnowflakeParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   protected SnowflakeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z);
      this.gravityStrength = 0.225F;
      this.velocityMultiplier = 1.0F;
      this.spriteProvider = spriteProvider;
      this.velocityX = velocityX + (Math.random() * 2.0 - 1.0) * 0.05000000074505806;
      this.velocityY = velocityY + (Math.random() * 2.0 - 1.0) * 0.05000000074505806;
      this.velocityZ = velocityZ + (Math.random() * 2.0 - 1.0) * 0.05000000074505806;
      this.scale = 0.1F * (this.random.nextFloat() * this.random.nextFloat() * 1.0F + 1.0F);
      this.maxAge = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
      this.setSpriteForAge(spriteProvider);
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      super.tick();
      this.setSpriteForAge(this.spriteProvider);
      this.velocityX *= 0.949999988079071;
      this.velocityY *= 0.8999999761581421;
      this.velocityZ *= 0.949999988079071;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         SnowflakeParticle lv = new SnowflakeParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
         lv.setColor(0.923F, 0.964F, 0.999F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
