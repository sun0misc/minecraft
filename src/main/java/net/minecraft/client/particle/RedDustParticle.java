/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.AbstractDustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;

@Environment(value=EnvType.CLIENT)
public class RedDustParticle
extends AbstractDustParticle<DustParticleEffect> {
    protected RedDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, DustParticleEffect parameters, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, parameters, spriteProvider);
        float j = this.random.nextFloat() * 0.4f + 0.6f;
        this.red = this.darken(parameters.getColor().x(), j);
        this.green = this.darken(parameters.getColor().y(), j);
        this.blue = this.darken(parameters.getColor().z(), j);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<DustParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DustParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            return new RedDustParticle(arg2, d, e, f, g, h, i, arg, this.spriteProvider);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((DustParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

