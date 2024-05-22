/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MuleEntity
extends AbstractDonkeyEntity {
    public MuleEntity(EntityType<? extends MuleEntity> arg, World arg2) {
        super((EntityType<? extends AbstractDonkeyEntity>)arg, arg2);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_MULE_AMBIENT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.ENTITY_MULE_ANGRY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_MULE_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_MULE_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_MULE_HURT;
    }

    @Override
    protected void playJumpSound() {
        this.playSound(SoundEvents.ENTITY_MULE_JUMP, 0.4f, 1.0f);
    }

    @Override
    protected void playAddChestSound() {
        this.playSound(SoundEvents.ENTITY_MULE_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityType.MULE.create(world);
    }
}

