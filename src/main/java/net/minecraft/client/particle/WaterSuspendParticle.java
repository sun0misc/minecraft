/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class WaterSuspendParticle
extends SpriteBillboardParticle {
    WaterSuspendParticle(ClientWorld world, SpriteProvider spriteProvider, double x, double y, double z) {
        super(world, x, y - 0.125, z);
        this.setBoundingBoxSpacing(0.01f, 0.01f);
        this.setSprite(spriteProvider);
        this.scale *= this.random.nextFloat() * 0.6f + 0.2f;
        this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.collidesWithWorld = false;
        this.velocityMultiplier = 1.0f;
        this.gravityStrength = 0.0f;
    }

    WaterSuspendParticle(ClientWorld world, SpriteProvider spriteProvider, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y - 0.125, z, velocityX, velocityY, velocityZ);
        this.setBoundingBoxSpacing(0.01f, 0.01f);
        this.setSprite(spriteProvider);
        this.scale *= this.random.nextFloat() * 0.6f + 0.6f;
        this.maxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.collidesWithWorld = false;
        this.velocityMultiplier = 1.0f;
        this.gravityStrength = 0.0f;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Environment(value=EnvType.CLIENT)
    public static class WarpedSporeFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public WarpedSporeFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            double j = (double)arg2.random.nextFloat() * -1.9 * (double)arg2.random.nextFloat() * 0.1;
            WaterSuspendParticle lv = new WaterSuspendParticle(arg2, this.spriteProvider, d, e, f, 0.0, j, 0.0);
            lv.setColor(0.1f, 0.1f, 0.3f);
            lv.setBoundingBoxSpacing(0.001f, 0.001f);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CrimsonSporeFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public CrimsonSporeFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            Random lv = arg2.random;
            double j = lv.nextGaussian() * (double)1.0E-6f;
            double k = lv.nextGaussian() * (double)1.0E-4f;
            double l = lv.nextGaussian() * (double)1.0E-6f;
            WaterSuspendParticle lv2 = new WaterSuspendParticle(arg2, this.spriteProvider, d, e, f, j, k, l);
            lv2.setColor(0.9f, 0.4f, 0.5f);
            return lv2;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SporeBlossomAirFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public SporeBlossomAirFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            WaterSuspendParticle lv = new WaterSuspendParticle(this, arg2, this.spriteProvider, d, e, f, 0.0, -0.8f, 0.0){

                @Override
                public Optional<ParticleGroup> getGroup() {
                    return Optional.of(ParticleGroup.SPORE_BLOSSOM_AIR);
                }
            };
            lv.maxAge = MathHelper.nextBetween(arg2.random, 500, 1000);
            lv.gravityStrength = 0.01f;
            lv.setColor(0.32f, 0.5f, 0.22f);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class UnderwaterFactory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public UnderwaterFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            WaterSuspendParticle lv = new WaterSuspendParticle(arg2, this.spriteProvider, d, e, f);
            lv.setColor(0.4f, 0.4f, 0.7f);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

