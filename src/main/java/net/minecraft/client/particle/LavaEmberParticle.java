package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

@Environment(EnvType.CLIENT)
public class LavaEmberParticle extends SpriteBillboardParticle {
   LavaEmberParticle(ClientWorld arg, double d, double e, double f) {
      super(arg, d, e, f, 0.0, 0.0, 0.0);
      this.gravityStrength = 0.75F;
      this.velocityMultiplier = 0.999F;
      this.velocityX *= 0.800000011920929;
      this.velocityY *= 0.800000011920929;
      this.velocityZ *= 0.800000011920929;
      this.velocityY = (double)(this.random.nextFloat() * 0.4F + 0.05F);
      this.scale *= this.random.nextFloat() * 2.0F + 0.2F;
      this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public int getBrightness(float tint) {
      int i = super.getBrightness(tint);
      int j = true;
      int k = i >> 16 & 255;
      return 240 | k << 16;
   }

   public float getSize(float tickDelta) {
      float g = ((float)this.age + tickDelta) / (float)this.maxAge;
      return this.scale * (1.0F - g * g);
   }

   public void tick() {
      super.tick();
      if (!this.dead) {
         float f = (float)this.age / (float)this.maxAge;
         if (this.random.nextFloat() > f) {
            this.world.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.velocityX, this.velocityY, this.velocityZ);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         LavaEmberParticle lv = new LavaEmberParticle(arg2, d, e, f);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
