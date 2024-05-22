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
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class NoteParticle
extends SpriteBillboardParticle {
    NoteParticle(ClientWorld world, double x, double y, double z, double g) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.velocityMultiplier = 0.66f;
        this.ascending = true;
        this.velocityX *= (double)0.01f;
        this.velocityY *= (double)0.01f;
        this.velocityZ *= (double)0.01f;
        this.velocityY += 0.2;
        this.red = Math.max(0.0f, MathHelper.sin(((float)g + 0.0f) * ((float)Math.PI * 2)) * 0.65f + 0.35f);
        this.green = Math.max(0.0f, MathHelper.sin(((float)g + 0.33333334f) * ((float)Math.PI * 2)) * 0.65f + 0.35f);
        this.blue = Math.max(0.0f, MathHelper.sin(((float)g + 0.6666667f) * ((float)Math.PI * 2)) * 0.65f + 0.35f);
        this.scale *= 1.5f;
        this.maxAge = 6;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getSize(float tickDelta) {
        return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0f, 0.0f, 1.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            NoteParticle lv = new NoteParticle(arg2, d, e, f, g);
            lv.setSprite(this.spriteProvider);
            return lv;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return this.createParticle((SimpleParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}

