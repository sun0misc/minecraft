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
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;

@Environment(value=EnvType.CLIENT)
public abstract class SpriteBillboardParticle
extends BillboardParticle {
    protected Sprite sprite;

    protected SpriteBillboardParticle(ClientWorld arg, double d, double e, double f) {
        super(arg, d, e, f);
    }

    protected SpriteBillboardParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
        super(arg, d, e, f, g, h, i);
    }

    protected void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    @Override
    protected float getMinU() {
        return this.sprite.getMinU();
    }

    @Override
    protected float getMaxU() {
        return this.sprite.getMaxU();
    }

    @Override
    protected float getMinV() {
        return this.sprite.getMinV();
    }

    @Override
    protected float getMaxV() {
        return this.sprite.getMaxV();
    }

    public void setSprite(SpriteProvider spriteProvider) {
        this.setSprite(spriteProvider.getSprite(this.random));
    }

    public void setSpriteForAge(SpriteProvider spriteProvider) {
        if (!this.dead) {
            this.setSprite(spriteProvider.getSprite(this.age, this.maxAge));
        }
    }
}

