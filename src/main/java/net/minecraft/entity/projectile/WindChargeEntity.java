/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.AdvancedExplosionBehavior;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public class WindChargeEntity
extends AbstractWindChargeEntity {
    private static final ExplosionBehavior EXPLOSION_BEHAVIOR = new AdvancedExplosionBehavior(true, false, Optional.of(Float.valueOf(1.1f)), Registries.BLOCK.getEntryList(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity()));
    private static final float EXPLOSION_POWER = 1.2f;
    private int field_52019 = 5;

    public WindChargeEntity(EntityType<? extends AbstractWindChargeEntity> arg, World arg2) {
        super(arg, arg2);
    }

    public WindChargeEntity(PlayerEntity player, World world, double x, double y, double z) {
        super(EntityType.WIND_CHARGE, world, player, x, y, z);
    }

    public WindChargeEntity(World world, double x, double y, double z, Vec3d velocity) {
        super((EntityType<? extends AbstractWindChargeEntity>)EntityType.WIND_CHARGE, x, y, z, velocity, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.field_52019 > 0) {
            --this.field_52019;
        }
    }

    @Override
    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflector, @Nullable Entity owner, boolean fromAttack) {
        if (this.field_52019 > 0) {
            return false;
        }
        return super.deflect(deflection, deflector, owner, fromAttack);
    }

    @Override
    protected void createExplosion() {
        this.getWorld().createExplosion(this, null, EXPLOSION_BEHAVIOR, this.getX(), this.getY(), this.getZ(), 1.2f, false, World.ExplosionSourceType.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST);
    }
}

