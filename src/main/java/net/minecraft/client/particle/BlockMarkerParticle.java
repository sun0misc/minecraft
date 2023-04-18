package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class BlockMarkerParticle extends SpriteBillboardParticle {
   BlockMarkerParticle(ClientWorld world, double x, double y, double z, BlockState state) {
      super(world, x, y, z);
      this.setSprite(MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(state));
      this.gravityStrength = 0.0F;
      this.maxAge = 80;
      this.collidesWithWorld = false;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.TERRAIN_SHEET;
   }

   public float getSize(float tickDelta) {
      return 0.5F;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      public Particle createParticle(BlockStateParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new BlockMarkerParticle(arg2, d, e, f, arg.getBlockState());
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((BlockStateParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
