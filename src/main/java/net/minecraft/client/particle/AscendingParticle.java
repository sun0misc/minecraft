package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class AscendingParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   protected AscendingParticle(ClientWorld world, double x, double y, double z, float randomVelocityXMultiplier, float randomVelocityYMultiplier, float randomVelocityZMultiplier, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteProvider spriteProvider, float colorMultiplier, int baseMaxAge, float gravityStrength, boolean collidesWithWorld) {
      super(world, x, y, z, 0.0, 0.0, 0.0);
      this.velocityMultiplier = 0.96F;
      this.gravityStrength = gravityStrength;
      this.field_28787 = true;
      this.spriteProvider = spriteProvider;
      this.velocityX *= (double)randomVelocityXMultiplier;
      this.velocityY *= (double)randomVelocityYMultiplier;
      this.velocityZ *= (double)randomVelocityZMultiplier;
      this.velocityX += velocityX;
      this.velocityY += velocityY;
      this.velocityZ += velocityZ;
      float q = world.random.nextFloat() * colorMultiplier;
      this.red = q;
      this.green = q;
      this.blue = q;
      this.scale *= 0.75F * scaleMultiplier;
      this.maxAge = (int)((double)baseMaxAge / ((double)world.random.nextFloat() * 0.8 + 0.2) * (double)scaleMultiplier);
      this.maxAge = Math.max(this.maxAge, 1);
      this.setSpriteForAge(spriteProvider);
      this.collidesWithWorld = collidesWithWorld;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      super.tick();
      this.setSpriteForAge(this.spriteProvider);
   }
}
