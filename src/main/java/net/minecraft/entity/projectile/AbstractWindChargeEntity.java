/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.AdvancedExplosionBehavior;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractWindChargeEntity
extends ExplosiveProjectileEntity
implements FlyingItemEntity {
    public static final ExplosionBehavior EXPLOSION_BEHAVIOR = new AdvancedExplosionBehavior(true, false, Optional.empty(), Registries.BLOCK.getEntryList(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity()));

    public AbstractWindChargeEntity(EntityType<? extends AbstractWindChargeEntity> arg, World arg2) {
        super((EntityType<? extends ExplosiveProjectileEntity>)arg, arg2);
        this.accelerationPower = 0.0;
    }

    public AbstractWindChargeEntity(EntityType<? extends AbstractWindChargeEntity> type, World world, Entity owner, double x, double y, double z) {
        super(type, x, y, z, world);
        this.setOwner(owner);
        this.accelerationPower = 0.0;
    }

    AbstractWindChargeEntity(EntityType<? extends AbstractWindChargeEntity> arg, double d, double e, double f, Vec3d arg2, World arg3) {
        super(arg, d, e, f, arg2, arg3);
        this.accelerationPower = 0.0;
    }

    @Override
    protected Box calculateBoundingBox() {
        float f = this.getType().getDimensions().width() / 2.0f;
        float g = this.getType().getDimensions().height();
        float h = 0.15f;
        return new Box(this.getPos().x - (double)f, this.getPos().y - (double)0.15f, this.getPos().z - (double)f, this.getPos().x + (double)f, this.getPos().y - (double)0.15f + (double)g, this.getPos().z + (double)f);
    }

    @Override
    public boolean collidesWith(Entity other) {
        if (other instanceof AbstractWindChargeEntity) {
            return false;
        }
        return super.collidesWith(other);
    }

    @Override
    protected boolean canHit(Entity entity) {
        if (entity instanceof AbstractWindChargeEntity) {
            return false;
        }
        if (entity.getType() == EntityType.END_CRYSTAL) {
            return false;
        }
        return super.canHit(entity);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        DamageSource lv4;
        LivingEntity lv;
        super.onEntityHit(entityHitResult);
        if (this.getWorld().isClient) {
            return;
        }
        Entity entity = this.getOwner();
        LivingEntity lv2 = entity instanceof LivingEntity ? (lv = (LivingEntity)entity) : null;
        Entity lv3 = entityHitResult.getEntity();
        if (lv2 != null) {
            lv2.onAttacking(lv3);
        }
        if (lv3.damage(lv4 = this.getDamageSources().windCharge(this, lv2), 1.0f) && lv3 instanceof LivingEntity) {
            LivingEntity lv5 = (LivingEntity)lv3;
            EnchantmentHelper.onTargetDamaged((ServerWorld)this.getWorld(), lv5, lv4);
        }
        this.createExplosion();
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
    }

    protected abstract void createExplosion();

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient) {
            this.createExplosion();
            this.discard();
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.discard();
        }
    }

    @Override
    protected boolean isBurning() {
        return false;
    }

    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getDrag() {
        return 1.0f;
    }

    @Override
    protected float getDragInWater() {
        return this.getDrag();
    }

    @Override
    @Nullable
    protected ParticleEffect getParticleType() {
        return null;
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient && this.getBlockY() > this.getWorld().getTopY() + 30) {
            this.createExplosion();
            this.discard();
        } else {
            super.tick();
        }
    }
}

