package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class NoteParticle extends SpriteBillboardParticle {
   NoteParticle(ClientWorld world, double x, double y, double z, double g) {
      super(world, x, y, z, 0.0, 0.0, 0.0);
      this.velocityMultiplier = 0.66F;
      this.field_28787 = true;
      this.velocityX *= 0.009999999776482582;
      this.velocityY *= 0.009999999776482582;
      this.velocityZ *= 0.009999999776482582;
      this.velocityY += 0.2;
      this.red = Math.max(0.0F, MathHelper.sin(((float)g + 0.0F) * 6.2831855F) * 0.65F + 0.35F);
      this.green = Math.max(0.0F, MathHelper.sin(((float)g + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F);
      this.blue = Math.max(0.0F, MathHelper.sin(((float)g + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F);
      this.scale *= 1.5F;
      this.maxAge = 6;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         NoteParticle lv = new NoteParticle(arg2, d, e, f, g);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
