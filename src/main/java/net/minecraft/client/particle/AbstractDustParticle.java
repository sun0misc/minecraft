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
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class AbstractDustParticle<T extends AbstractDustParticleEffect>
extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    protected AbstractDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, T parameters, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityMultiplier = 0.96f;
        this.ascending = true;
        this.spriteProvider = spriteProvider;
        this.velocityX *= (double)0.1f;
        this.velocityY *= (double)0.1f;
        this.velocityZ *= (double)0.1f;
        this.scale *= 0.75f * ((AbstractDustParticleEffect)parameters).getScale();
        int j = (int)(8.0 / (this.random.nextDouble() * 0.8 + 0.2));
        this.maxAge = (int)Math.max((float)j * ((AbstractDustParticleEffect)parameters).getScale(), 1.0f);
        this.setSpriteForAge(spriteProvider);
    }

    protected float darken(float colorComponent, float multiplier) {
        return (this.random.nextFloat() * 0.2f + 0.8f) * colorComponent * multiplier;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getSize(float tickDelta) {
        return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0f, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
    }
}

