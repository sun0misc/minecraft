package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CurrentDownParticle extends SpriteBillboardParticle {
   private float accelerationAngle;

   CurrentDownParticle(ClientWorld arg, double d, double e, double f) {
      super(arg, d, e, f);
      this.maxAge = (int)(Math.random() * 60.0) + 30;
      this.collidesWithWorld = false;
      this.velocityX = 0.0;
      this.velocityY = -0.05;
      this.velocityZ = 0.0;
      this.setBoundingBoxSpacing(0.02F, 0.02F);
      this.scale *= this.random.nextFloat() * 0.6F + 0.2F;
      this.gravityStrength = 0.002F;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         float f = 0.6F;
         this.velocityX += (double)(0.6F * MathHelper.cos(this.accelerationAngle));
         this.velocityZ += (double)(0.6F * MathHelper.sin(this.accelerationAngle));
         this.velocityX *= 0.07;
         this.velocityZ *= 0.07;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         if (!this.world.getFluidState(BlockPos.ofFloored(this.x, this.y, this.z)).isIn(FluidTags.WATER) || this.onGround) {
            this.markDead();
         }

         this.accelerationAngle += 0.08F;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         CurrentDownParticle lv = new CurrentDownParticle(arg2, d, e, f);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
