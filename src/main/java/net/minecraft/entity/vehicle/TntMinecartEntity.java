/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.vehicle;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class TntMinecartEntity
extends AbstractMinecartEntity {
    private static final byte PRIME_TNT_STATUS = 10;
    private int fuseTicks = -1;

    public TntMinecartEntity(EntityType<? extends TntMinecartEntity> arg, World arg2) {
        super(arg, arg2);
    }

    public TntMinecartEntity(World world, double x, double y, double z) {
        super(EntityType.TNT_MINECART, world, x, y, z);
    }

    @Override
    public AbstractMinecartEntity.Type getMinecartType() {
        return AbstractMinecartEntity.Type.TNT;
    }

    @Override
    public BlockState getDefaultContainedBlock() {
        return Blocks.TNT.getDefaultState();
    }

    @Override
    public void tick() {
        double d;
        super.tick();
        if (this.fuseTicks > 0) {
            --this.fuseTicks;
            this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.fuseTicks == 0) {
            this.explode(this.getVelocity().horizontalLengthSquared());
        }
        if (this.horizontalCollision && (d = this.getVelocity().horizontalLengthSquared()) >= (double)0.01f) {
            this.explode(d);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        PersistentProjectileEntity lv2;
        Entity lv = source.getSource();
        if (lv instanceof PersistentProjectileEntity && (lv2 = (PersistentProjectileEntity)lv).isOnFire()) {
            DamageSource lv3 = this.getDamageSources().explosion(this, source.getAttacker());
            this.explode(lv3, lv2.getVelocity().lengthSquared());
        }
        return super.damage(source, amount);
    }

    @Override
    public void killAndDropSelf(DamageSource source) {
        double d = this.getVelocity().horizontalLengthSquared();
        if (TntMinecartEntity.shouldDetonate(source) || d >= (double)0.01f) {
            if (this.fuseTicks < 0) {
                this.prime();
                this.fuseTicks = this.random.nextInt(20) + this.random.nextInt(20);
            }
            return;
        }
        this.killAndDropItem(this.asItem());
    }

    @Override
    protected Item asItem() {
        return Items.TNT_MINECART;
    }

    protected void explode(double power) {
        this.explode(null, power);
    }

    protected void explode(@Nullable DamageSource damageSource, double power) {
        if (!this.getWorld().isClient) {
            double e = Math.sqrt(power);
            if (e > 5.0) {
                e = 5.0;
            }
            this.getWorld().createExplosion(this, damageSource, null, this.getX(), this.getY(), this.getZ(), (float)(4.0 + this.random.nextDouble() * 1.5 * e), false, World.ExplosionSourceType.TNT);
            this.discard();
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (fallDistance >= 3.0f) {
            float h = fallDistance / 10.0f;
            this.explode(h * h);
        }
        return super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
    }

    @Override
    public void onActivatorRail(int x, int y, int z, boolean powered) {
        if (powered && this.fuseTicks < 0) {
            this.prime();
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.SET_SHEEP_EAT_GRASS_TIMER_OR_PRIME_TNT_MINECART) {
            this.prime();
        } else {
            super.handleStatus(status);
        }
    }

    public void prime() {
        this.fuseTicks = 80;
        if (!this.getWorld().isClient) {
            this.getWorld().sendEntityStatus(this, EntityStatuses.SET_SHEEP_EAT_GRASS_TIMER_OR_PRIME_TNT_MINECART);
            if (!this.isSilent()) {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    public int getFuseTicks() {
        return this.fuseTicks;
    }

    public boolean isPrimed() {
        return this.fuseTicks > -1;
    }

    @Override
    public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
        if (this.isPrimed() && (blockState.isIn(BlockTags.RAILS) || world.getBlockState(pos.up()).isIn(BlockTags.RAILS))) {
            return 0.0f;
        }
        return super.getEffectiveExplosionResistance(explosion, world, pos, blockState, fluidState, max);
    }

    @Override
    public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
        if (this.isPrimed() && (state.isIn(BlockTags.RAILS) || world.getBlockState(pos.up()).isIn(BlockTags.RAILS))) {
            return false;
        }
        return super.canExplosionDestroyBlock(explosion, world, pos, state, explosionPower);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("TNTFuse", NbtElement.NUMBER_TYPE)) {
            this.fuseTicks = nbt.getInt("TNTFuse");
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("TNTFuse", this.fuseTicks);
    }

    @Override
    boolean shouldAlwaysKill(DamageSource source) {
        return TntMinecartEntity.shouldDetonate(source);
    }

    private static boolean shouldDetonate(DamageSource source) {
        return source.isIn(DamageTypeTags.IS_FIRE) || source.isIn(DamageTypeTags.IS_EXPLOSION);
    }
}

