/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnderPearlEntity
extends ThrownItemEntity {
    public EnderPearlEntity(EntityType<? extends EnderPearlEntity> arg, World arg2) {
        super((EntityType<? extends ThrownItemEntity>)arg, arg2);
    }

    public EnderPearlEntity(World world, LivingEntity owner) {
        super((EntityType<? extends ThrownItemEntity>)EntityType.ENDER_PEARL, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        entityHitResult.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        for (int i = 0; i < 32; ++i) {
            this.getWorld().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
        }
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            if (!this.isRemoved()) {
                Entity lv2 = this.getOwner();
                if (lv2 instanceof ServerPlayerEntity) {
                    ServerPlayerEntity lv3 = (ServerPlayerEntity)lv2;
                    if (lv3.networkHandler.isConnectionOpen() && lv3.canUsePortals()) {
                        EndermiteEntity lv4;
                        if (this.random.nextFloat() < 0.05f && lv.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && (lv4 = EntityType.ENDERMITE.create(lv)) != null) {
                            lv4.refreshPositionAndAngles(lv2.getX(), lv2.getY(), lv2.getZ(), lv2.getYaw(), lv2.getPitch());
                            lv.spawnEntity(lv4);
                        }
                        if (lv2.hasVehicle()) {
                            this.detach();
                        }
                        lv2.moveToWorld(new TeleportTarget(lv, this.getPos(), lv2.getVelocity(), lv2.getYaw(), lv2.getPitch()));
                        lv2.onLanding();
                        lv2.damage(this.getDamageSources().fall(), 5.0f);
                        this.method_60729(lv, this.getPos());
                    }
                } else if (lv2 != null) {
                    lv2.moveToWorld(new TeleportTarget(lv, this.getPos(), lv2.getVelocity(), lv2.getYaw(), lv2.getPitch()));
                    lv2.onLanding();
                }
                this.discard();
            }
        }
    }

    @Override
    public void tick() {
        Entity lv = this.getOwner();
        if (lv instanceof ServerPlayerEntity && !lv.isAlive() && this.getWorld().getGameRules().getBoolean(GameRules.ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard();
        } else {
            super.tick();
        }
    }

    @Override
    @Nullable
    public Entity moveToWorld(TeleportTarget arg) {
        if (this.getWorld().getRegistryKey() != arg.newLevel().getRegistryKey()) {
            this.method_60728();
        }
        return super.moveToWorld(arg);
    }

    private void method_60729(World arg, Vec3d arg2) {
        arg.playSound(null, arg2.x, arg2.y, arg2.z, SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.PLAYERS);
    }
}

