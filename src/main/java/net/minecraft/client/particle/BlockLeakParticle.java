package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BlockLeakParticle extends SpriteBillboardParticle {
   private final Fluid fluid;
   protected boolean obsidianTear;

   BlockLeakParticle(ClientWorld world, double x, double y, double z, Fluid fluid) {
      super(world, x, y, z);
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.gravityStrength = 0.06F;
      this.fluid = fluid;
   }

   protected Fluid getFluid() {
      return this.fluid;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public int getBrightness(float tint) {
      return this.obsidianTear ? 240 : super.getBrightness(tint);
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      this.updateAge();
      if (!this.dead) {
         this.velocityY -= (double)this.gravityStrength;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.updateVelocity();
         if (!this.dead) {
            this.velocityX *= 0.9800000190734863;
            this.velocityY *= 0.9800000190734863;
            this.velocityZ *= 0.9800000190734863;
            if (this.fluid != Fluids.EMPTY) {
               BlockPos lv = BlockPos.ofFloored(this.x, this.y, this.z);
               FluidState lv2 = this.world.getFluidState(lv);
               if (lv2.getFluid() == this.fluid && this.y < (double)((float)lv.getY() + lv2.getHeight(this.world, lv))) {
                  this.markDead();
               }

            }
         }
      }
   }

   protected void updateAge() {
      if (this.maxAge-- <= 0) {
         this.markDead();
      }

   }

   protected void updateVelocity() {
   }

   public static SpriteBillboardParticle createDrippingWater(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new Dripping(world, x, y, z, Fluids.WATER, ParticleTypes.FALLING_WATER);
      lv.setColor(0.2F, 0.3F, 1.0F);
      return lv;
   }

   public static SpriteBillboardParticle createFallingWater(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new ContinuousFalling(world, x, y, z, Fluids.WATER, ParticleTypes.SPLASH);
      lv.setColor(0.2F, 0.3F, 1.0F);
      return lv;
   }

   public static SpriteBillboardParticle createDrippingLava(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      return new DrippingLava(world, x, y, z, Fluids.LAVA, ParticleTypes.FALLING_LAVA);
   }

   public static SpriteBillboardParticle createFallingLava(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new ContinuousFalling(world, x, y, z, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
      lv.setColor(1.0F, 0.2857143F, 0.083333336F);
      return lv;
   }

   public static SpriteBillboardParticle createLandingLava(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new Landing(world, x, y, z, Fluids.LAVA);
      lv.setColor(1.0F, 0.2857143F, 0.083333336F);
      return lv;
   }

   public static SpriteBillboardParticle createDrippingHoney(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      Dripping lv = new Dripping(world, x, y, z, Fluids.EMPTY, ParticleTypes.FALLING_HONEY);
      lv.gravityStrength *= 0.01F;
      lv.maxAge = 100;
      lv.setColor(0.622F, 0.508F, 0.082F);
      return lv;
   }

   public static SpriteBillboardParticle createFallingHoney(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new FallingHoney(world, x, y, z, Fluids.EMPTY, ParticleTypes.LANDING_HONEY);
      lv.gravityStrength = 0.01F;
      lv.setColor(0.582F, 0.448F, 0.082F);
      return lv;
   }

   public static SpriteBillboardParticle createLandingHoney(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new Landing(world, x, y, z, Fluids.EMPTY);
      lv.maxAge = (int)(128.0 / (Math.random() * 0.8 + 0.2));
      lv.setColor(0.522F, 0.408F, 0.082F);
      return lv;
   }

   public static SpriteBillboardParticle createDrippingDripstoneWater(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new Dripping(world, x, y, z, Fluids.WATER, ParticleTypes.FALLING_DRIPSTONE_WATER);
      lv.setColor(0.2F, 0.3F, 1.0F);
      return lv;
   }

   public static SpriteBillboardParticle createFallingDripstoneWater(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new DripstoneLavaDrip(world, x, y, z, Fluids.WATER, ParticleTypes.SPLASH);
      lv.setColor(0.2F, 0.3F, 1.0F);
      return lv;
   }

   public static SpriteBillboardParticle createDrippingDripstoneLava(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      return new DrippingLava(world, x, y, z, Fluids.LAVA, ParticleTypes.FALLING_DRIPSTONE_LAVA);
   }

   public static SpriteBillboardParticle createFallingDripstoneLava(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new DripstoneLavaDrip(world, x, y, z, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
      lv.setColor(1.0F, 0.2857143F, 0.083333336F);
      return lv;
   }

   public static SpriteBillboardParticle createFallingNectar(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new Falling(world, x, y, z, Fluids.EMPTY);
      lv.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
      lv.gravityStrength = 0.007F;
      lv.setColor(0.92F, 0.782F, 0.72F);
      return lv;
   }

   public static SpriteBillboardParticle createFallingSporeBlossom(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      int j = (int)(64.0F / MathHelper.nextBetween(world.getRandom(), 0.1F, 0.9F));
      BlockLeakParticle lv = new Falling(world, x, y, z, Fluids.EMPTY, j);
      lv.gravityStrength = 0.005F;
      lv.setColor(0.32F, 0.5F, 0.22F);
      return lv;
   }

   public static SpriteBillboardParticle createDrippingObsidianTear(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      Dripping lv = new Dripping(world, x, y, z, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR);
      lv.obsidianTear = true;
      lv.gravityStrength *= 0.01F;
      lv.maxAge = 100;
      lv.setColor(0.51171875F, 0.03125F, 0.890625F);
      return lv;
   }

   public static SpriteBillboardParticle createFallingObsidianTear(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new ContinuousFalling(world, x, y, z, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR);
      lv.obsidianTear = true;
      lv.gravityStrength = 0.01F;
      lv.setColor(0.51171875F, 0.03125F, 0.890625F);
      return lv;
   }

   public static SpriteBillboardParticle createLandingObsidianTear(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      BlockLeakParticle lv = new Landing(world, x, y, z, Fluids.EMPTY);
      lv.obsidianTear = true;
      lv.maxAge = (int)(28.0 / (Math.random() * 0.8 + 0.2));
      lv.setColor(0.51171875F, 0.03125F, 0.890625F);
      return lv;
   }

   @Environment(EnvType.CLIENT)
   private static class Dripping extends BlockLeakParticle {
      private final ParticleEffect nextParticle;

      Dripping(ClientWorld world, double x, double y, double z, Fluid fluid, ParticleEffect nextParticle) {
         super(world, x, y, z, fluid);
         this.nextParticle = nextParticle;
         this.gravityStrength *= 0.02F;
         this.maxAge = 40;
      }

      protected void updateAge() {
         if (this.maxAge-- <= 0) {
            this.markDead();
            this.world.addParticle(this.nextParticle, this.x, this.y, this.z, this.velocityX, this.velocityY, this.velocityZ);
         }

      }

      protected void updateVelocity() {
         this.velocityX *= 0.02;
         this.velocityY *= 0.02;
         this.velocityZ *= 0.02;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class ContinuousFalling extends Falling {
      protected final ParticleEffect nextParticle;

      ContinuousFalling(ClientWorld world, double x, double y, double z, Fluid fluid, ParticleEffect nextParticle) {
         super(world, x, y, z, fluid);
         this.nextParticle = nextParticle;
      }

      protected void updateVelocity() {
         if (this.onGround) {
            this.markDead();
            this.world.addParticle(this.nextParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
         }

      }
   }

   @Environment(EnvType.CLIENT)
   static class DrippingLava extends Dripping {
      DrippingLava(ClientWorld arg, double d, double e, double f, Fluid arg2, ParticleEffect arg3) {
         super(arg, d, e, f, arg2, arg3);
      }

      protected void updateAge() {
         this.red = 1.0F;
         this.green = 16.0F / (float)(40 - this.maxAge + 16);
         this.blue = 4.0F / (float)(40 - this.maxAge + 8);
         super.updateAge();
      }
   }

   @Environment(EnvType.CLIENT)
   static class Landing extends BlockLeakParticle {
      Landing(ClientWorld arg, double d, double e, double f, Fluid arg2) {
         super(arg, d, e, f, arg2);
         this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
      }
   }

   @Environment(EnvType.CLIENT)
   static class FallingHoney extends ContinuousFalling {
      FallingHoney(ClientWorld arg, double d, double e, double f, Fluid arg2, ParticleEffect arg3) {
         super(arg, d, e, f, arg2, arg3);
      }

      protected void updateVelocity() {
         if (this.onGround) {
            this.markDead();
            this.world.addParticle(this.nextParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            float f = MathHelper.nextBetween(this.random, 0.3F, 1.0F);
            this.world.playSound(this.x, this.y, this.z, SoundEvents.BLOCK_BEEHIVE_DRIP, SoundCategory.BLOCKS, f, 1.0F, false);
         }

      }
   }

   @Environment(EnvType.CLIENT)
   private static class DripstoneLavaDrip extends ContinuousFalling {
      DripstoneLavaDrip(ClientWorld arg, double d, double e, double f, Fluid arg2, ParticleEffect arg3) {
         super(arg, d, e, f, arg2, arg3);
      }

      protected void updateVelocity() {
         if (this.onGround) {
            this.markDead();
            this.world.addParticle(this.nextParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            SoundEvent lv = this.getFluid() == Fluids.LAVA ? SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA : SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER;
            float f = MathHelper.nextBetween(this.random, 0.3F, 1.0F);
            this.world.playSound(this.x, this.y, this.z, lv, SoundCategory.BLOCKS, f, 1.0F, false);
         }

      }
   }

   @Environment(EnvType.CLIENT)
   private static class Falling extends BlockLeakParticle {
      Falling(ClientWorld arg, double d, double e, double f, Fluid arg2) {
         this(arg, d, e, f, arg2, (int)(64.0 / (Math.random() * 0.8 + 0.2)));
      }

      Falling(ClientWorld world, double x, double y, double z, Fluid fluid, int maxAge) {
         super(world, x, y, z, fluid);
         this.maxAge = maxAge;
      }

      protected void updateVelocity() {
         if (this.onGround) {
            this.markDead();
         }

      }
   }
}
