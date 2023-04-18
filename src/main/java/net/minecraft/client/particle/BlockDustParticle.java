package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class BlockDustParticle extends SpriteBillboardParticle {
   private final BlockPos blockPos;
   private final float sampleU;
   private final float sampleV;

   public BlockDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state) {
      this(world, x, y, z, velocityX, velocityY, velocityZ, state, BlockPos.ofFloored(x, y, z));
   }

   public BlockDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockPos blockPos) {
      super(world, x, y, z, velocityX, velocityY, velocityZ);
      this.blockPos = blockPos;
      this.setSprite(MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(state));
      this.gravityStrength = 1.0F;
      this.red = 0.6F;
      this.green = 0.6F;
      this.blue = 0.6F;
      if (!state.isOf(Blocks.GRASS_BLOCK)) {
         int j = MinecraftClient.getInstance().getBlockColors().getColor(state, world, blockPos, 0);
         this.red *= (float)(j >> 16 & 255) / 255.0F;
         this.green *= (float)(j >> 8 & 255) / 255.0F;
         this.blue *= (float)(j & 255) / 255.0F;
      }

      this.scale /= 2.0F;
      this.sampleU = this.random.nextFloat() * 3.0F;
      this.sampleV = this.random.nextFloat() * 3.0F;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.TERRAIN_SHEET;
   }

   protected float getMinU() {
      return this.sprite.getFrameU((double)((this.sampleU + 1.0F) / 4.0F * 16.0F));
   }

   protected float getMaxU() {
      return this.sprite.getFrameU((double)(this.sampleU / 4.0F * 16.0F));
   }

   protected float getMinV() {
      return this.sprite.getFrameV((double)(this.sampleV / 4.0F * 16.0F));
   }

   protected float getMaxV() {
      return this.sprite.getFrameV((double)((this.sampleV + 1.0F) / 4.0F * 16.0F));
   }

   public int getBrightness(float tint) {
      int i = super.getBrightness(tint);
      return i == 0 && this.world.isChunkLoaded(this.blockPos) ? WorldRenderer.getLightmapCoordinates(this.world, this.blockPos) : i;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      public Particle createParticle(BlockStateParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         BlockState lv = arg.getBlockState();
         return !lv.isAir() && !lv.isOf(Blocks.MOVING_PISTON) ? new BlockDustParticle(arg2, d, e, f, g, h, i, lv) : null;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((BlockStateParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
