package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class WhiteAshParticle extends AscendingParticle {
   private static final int COLOR = 12235202;

   protected WhiteAshParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.1F, -0.1F, 0.1F, velocityX, velocityY, velocityZ, scaleMultiplier, spriteProvider, 0.0F, 20, 0.0125F, false);
      this.red = 0.7294118F;
      this.green = 0.69411767F;
      this.blue = 0.7607843F;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         Random lv = arg2.random;
         double j = (double)lv.nextFloat() * -1.9 * (double)lv.nextFloat() * 0.1;
         double k = (double)lv.nextFloat() * -0.5 * (double)lv.nextFloat() * 0.1 * 5.0;
         double l = (double)lv.nextFloat() * -1.9 * (double)lv.nextFloat() * 0.1;
         return new WhiteAshParticle(arg2, d, e, f, j, k, l, 1.0F, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
