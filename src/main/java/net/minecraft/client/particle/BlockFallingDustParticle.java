package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BlockFallingDustParticle extends SpriteBillboardParticle {
   private final float rotationSpeed;
   private final SpriteProvider spriteProvider;

   BlockFallingDustParticle(ClientWorld world, double x, double y, double z, float red, float green, float blue, SpriteProvider spriteProvider) {
      super(world, x, y, z);
      this.spriteProvider = spriteProvider;
      this.red = red;
      this.green = green;
      this.blue = blue;
      float j = 0.9F;
      this.scale *= 0.67499995F;
      int k = (int)(32.0 / (Math.random() * 0.8 + 0.2));
      this.maxAge = (int)Math.max((float)k * 0.9F, 1.0F);
      this.setSpriteForAge(spriteProvider);
      this.rotationSpeed = ((float)Math.random() - 0.5F) * 0.1F;
      this.angle = (float)Math.random() * 6.2831855F;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.setSpriteForAge(this.spriteProvider);
         this.prevAngle = this.angle;
         this.angle += 3.1415927F * this.rotationSpeed * 2.0F;
         if (this.onGround) {
            this.prevAngle = this.angle = 0.0F;
         }

         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityY -= 0.003000000026077032;
         this.velocityY = Math.max(this.velocityY, -0.14000000059604645);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      @Nullable
      public Particle createParticle(BlockStateParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         BlockState lv = arg.getBlockState();
         if (!lv.isAir() && lv.getRenderType() == BlockRenderType.INVISIBLE) {
            return null;
         } else {
            BlockPos lv2 = BlockPos.ofFloored(d, e, f);
            int j = MinecraftClient.getInstance().getBlockColors().getParticleColor(lv, arg2, lv2);
            if (lv.getBlock() instanceof FallingBlock) {
               j = ((FallingBlock)lv.getBlock()).getColor(lv, arg2, lv2);
            }

            float k = (float)(j >> 16 & 255) / 255.0F;
            float l = (float)(j >> 8 & 255) / 255.0F;
            float m = (float)(j & 255) / 255.0F;
            return new BlockFallingDustParticle(arg2, d, e, f, k, l, m, this.spriteProvider);
         }
      }

      // $FF: synthetic method
      @Nullable
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((BlockStateParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
