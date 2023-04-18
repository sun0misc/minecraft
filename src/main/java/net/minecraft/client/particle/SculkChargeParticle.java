package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SculkChargeParticleEffect;

@Environment(EnvType.CLIENT)
public class SculkChargeParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   SculkChargeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, velocityX, velocityY, velocityZ);
      this.velocityMultiplier = 0.96F;
      this.spriteProvider = spriteProvider;
      this.scale(1.5F);
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

      public Particle createParticle(SculkChargeParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         SculkChargeParticle lv = new SculkChargeParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
         lv.setAlpha(1.0F);
         lv.setVelocity(g, h, i);
         lv.prevAngle = arg.roll();
         lv.angle = arg.roll();
         lv.setMaxAge(arg2.random.nextInt(12) + 8);
         return lv;
      }

      public SpriteProvider spriteProvider() {
         return this.spriteProvider;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((SculkChargeParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
