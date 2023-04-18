package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class SpellParticle extends SpriteBillboardParticle {
   private static final Random RANDOM = Random.create();
   private final SpriteProvider spriteProvider;

   SpellParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.5 - RANDOM.nextDouble(), velocityY, 0.5 - RANDOM.nextDouble());
      this.velocityMultiplier = 0.96F;
      this.gravityStrength = -0.1F;
      this.field_28787 = true;
      this.spriteProvider = spriteProvider;
      this.velocityY *= 0.20000000298023224;
      if (velocityX == 0.0 && velocityZ == 0.0) {
         this.velocityX *= 0.10000000149011612;
         this.velocityZ *= 0.10000000149011612;
      }

      this.scale *= 0.75F;
      this.maxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2));
      this.collidesWithWorld = false;
      this.setSpriteForAge(spriteProvider);
      if (this.isInvisible()) {
         this.setAlpha(0.0F);
      }

   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      super.tick();
      this.setSpriteForAge(this.spriteProvider);
      if (this.isInvisible()) {
         this.setAlpha(0.0F);
      } else {
         this.setAlpha(MathHelper.lerp(0.05F, this.alpha, 1.0F));
      }

   }

   private boolean isInvisible() {
      MinecraftClient lv = MinecraftClient.getInstance();
      ClientPlayerEntity lv2 = lv.player;
      return lv2 != null && lv2.getEyePos().squaredDistanceTo(this.x, this.y, this.z) <= 9.0 && lv.options.getPerspective().isFirstPerson() && lv2.isUsingSpyglass();
   }

   @Environment(EnvType.CLIENT)
   public static class InstantFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public InstantFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new SpellParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WitchFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public WitchFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         SpellParticle lv = new SpellParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
         float j = arg2.random.nextFloat() * 0.5F + 0.35F;
         lv.setColor(1.0F * j, 0.0F * j, 1.0F * j);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class EntityAmbientFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public EntityAmbientFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         Particle lv = new SpellParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
         lv.setAlpha(0.15F);
         lv.setColor((float)g, (float)h, (float)i);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class EntityFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public EntityFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         Particle lv = new SpellParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
         lv.setColor((float)g, (float)h, (float)i);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class DefaultFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public DefaultFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new SpellParticle(arg2, d, e, f, g, h, i, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
