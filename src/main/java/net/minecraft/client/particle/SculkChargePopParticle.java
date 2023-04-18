package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class SculkChargePopParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   SculkChargePopParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, velocityX, velocityY, velocityZ);
      this.velocityMultiplier = 0.96F;
      this.spriteProvider = spriteProvider;
      this.scale(1.0F);
      this.collidesWithWorld = false;
      this.setSpriteForAge(spriteProvider);
   }

   public int getBrightness(float tint) {
      return 240;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      super.tick();
      this.setSpriteForAge(this.spriteProvider);
   }

   @Environment(EnvType.CLIENT)
   public static record Factory(SpriteProvider spriteProvider) implements ParticleFactory {
      public Factory(SpriteProvider arg) {
         this.spriteProvider = arg;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         SculkChargePopParticle lv = new SculkChargePopParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
         lv.setAlpha(1.0F);
         lv.setVelocity(g, h, i);
         lv.setMaxAge(arg2.random.nextInt(4) + 6);
         return lv;
      }

      public SpriteProvider spriteProvider() {
         return this.spriteProvider;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
