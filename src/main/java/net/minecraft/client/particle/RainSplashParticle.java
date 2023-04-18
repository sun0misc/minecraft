package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class RainSplashParticle extends SpriteBillboardParticle {
   protected RainSplashParticle(ClientWorld arg, double d, double e, double f) {
      super(arg, d, e, f, 0.0, 0.0, 0.0);
      this.velocityX *= 0.30000001192092896;
      this.velocityY = Math.random() * 0.20000000298023224 + 0.10000000149011612;
      this.velocityZ *= 0.30000001192092896;
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.gravityStrength = 0.06F;
      this.maxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2));
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.maxAge-- <= 0) {
         this.markDead();
      } else {
         this.velocityY -= (double)this.gravityStrength;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityX *= 0.9800000190734863;
         this.velocityY *= 0.9800000190734863;
         this.velocityZ *= 0.9800000190734863;
         if (this.onGround) {
            if (Math.random() < 0.5) {
               this.markDead();
            }

            this.velocityX *= 0.699999988079071;
            this.velocityZ *= 0.699999988079071;
         }

         BlockPos lv = BlockPos.ofFloored(this.x, this.y, this.z);
         double d = Math.max(this.world.getBlockState(lv).getCollisionShape(this.world, lv).getEndingCoord(Direction.Axis.Y, this.x - (double)lv.getX(), this.z - (double)lv.getZ()), (double)this.world.getFluidState(lv).getHeight(this.world, lv));
         if (d > 0.0 && this.y < (double)lv.getY() + d) {
            this.markDead();
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
         RainSplashParticle lv = new RainSplashParticle(arg2, d, e, f);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
