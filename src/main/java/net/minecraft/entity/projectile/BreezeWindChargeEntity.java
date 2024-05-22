/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class BreezeWindChargeEntity
extends AbstractWindChargeEntity {
    private static final float EXPLOSION_POWER = 3.0f;

    public BreezeWindChargeEntity(EntityType<? extends AbstractWindChargeEntity> arg, World arg2) {
        super(arg, arg2);
    }

    public BreezeWindChargeEntity(BreezeEntity breeze, World world) {
        super(EntityType.BREEZE_WIND_CHARGE, world, breeze, breeze.getX(), breeze.getChargeY(), breeze.getZ());
    }

    @Override
    protected void createExplosion() {
        this.getWorld().createExplosion(this, null, EXPLOSION_BEHAVIOR, this.getX(), this.getY(), this.getZ(), 3.0f, false, World.ExplosionSourceType.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.ENTITY_BREEZE_WIND_BURST);
    }
}

