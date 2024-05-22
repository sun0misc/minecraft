/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TridentEntity
extends PersistentProjectileEntity {
    private static final TrackedData<Byte> LOYALTY = DataTracker.registerData(TridentEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> ENCHANTED = DataTracker.registerData(TridentEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private boolean dealtDamage;
    public int returnTimer;

    public TridentEntity(EntityType<? extends TridentEntity> arg, World arg2) {
        super((EntityType<? extends PersistentProjectileEntity>)arg, arg2);
    }

    public TridentEntity(World world, LivingEntity owner, ItemStack stack) {
        super(EntityType.TRIDENT, owner, world, stack, null);
        this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
        this.dataTracker.set(ENCHANTED, stack.hasGlint());
    }

    public TridentEntity(World world, double x, double y, double z, ItemStack stack) {
        super(EntityType.TRIDENT, x, y, z, world, stack, stack);
        this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
        this.dataTracker.set(ENCHANTED, stack.hasGlint());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LOYALTY, (byte)0);
        builder.add(ENCHANTED, false);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        Entity lv = this.getOwner();
        byte i = this.dataTracker.get(LOYALTY);
        if (i > 0 && (this.dealtDamage || this.isNoClip()) && lv != null) {
            if (!this.isOwnerAlive()) {
                if (!this.getWorld().isClient && this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1f);
                }
                this.discard();
            } else {
                this.setNoClip(true);
                Vec3d lv2 = lv.getEyePos().subtract(this.getPos());
                this.setPos(this.getX(), this.getY() + lv2.y * 0.015 * (double)i, this.getZ());
                if (this.getWorld().isClient) {
                    this.lastRenderY = this.getY();
                }
                double d = 0.05 * (double)i;
                this.setVelocity(this.getVelocity().multiply(0.95).add(lv2.normalize().multiply(d)));
                if (this.returnTimer == 0) {
                    this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0f, 1.0f);
                }
                ++this.returnTimer;
            }
        }
        super.tick();
    }

    private boolean isOwnerAlive() {
        Entity lv = this.getOwner();
        if (lv == null || !lv.isAlive()) {
            return false;
        }
        return !(lv instanceof ServerPlayerEntity) || !lv.isSpectator();
    }

    public boolean isEnchanted() {
        return this.dataTracker.get(ENCHANTED);
    }

    @Override
    @Nullable
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        if (this.dealtDamage) {
            return null;
        }
        return super.getEntityCollision(currentPosition, nextPosition);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        ServerWorld lv4;
        Entity lv = entityHitResult.getEntity();
        float f = 8.0f;
        Entity lv2 = this.getOwner();
        DamageSource lv3 = this.getDamageSources().trident(this, lv2 == null ? this : lv2);
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            lv4 = (ServerWorld)world;
            f = EnchantmentHelper.getDamage(lv4, this.getWeaponStack(), lv, lv3, f);
        }
        this.dealtDamage = true;
        if (lv.damage(lv3, f)) {
            if (lv.getType() == EntityType.ENDERMAN) {
                return;
            }
            world = this.getWorld();
            if (world instanceof ServerWorld) {
                lv4 = (ServerWorld)world;
                EnchantmentHelper.onTargetDamaged(lv4, lv, lv3, this.getWeaponStack());
            }
            if (lv instanceof LivingEntity) {
                LivingEntity lv5 = (LivingEntity)lv;
                this.knockback(lv5, lv3);
                this.onHit(lv5);
            }
        }
        this.setVelocity(this.getVelocity().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0f, 1.0f);
    }

    @Override
    protected void onBlockHitEnchantmentEffects(ServerWorld arg, BlockHitResult arg2, ItemStack shotFromStack) {
        LivingEntity lv2;
        Vec3d lv = arg2.getBlockPos().method_60913(arg2.getPos());
        Entity entity = this.getOwner();
        EnchantmentHelper.onHitBlock(arg, shotFromStack, entity instanceof LivingEntity ? (lv2 = (LivingEntity)entity) : null, this, null, lv, arg.getBlockState(arg2.getBlockPos()), item -> this.kill());
    }

    @Override
    @NotNull
    protected ItemStack getWeaponStack() {
        return this.getItemStack();
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return super.tryPickup(player) || this.isNoClip() && this.isOwner(player) && player.getInventory().insertStack(this.asItemStack());
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(Items.TRIDENT);
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (this.isOwner(player) || this.getOwner() == null) {
            super.onPlayerCollision(player);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dealtDamage = nbt.getBoolean("DealtDamage");
        this.dataTracker.set(LOYALTY, this.getLoyalty(this.getItemStack()));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("DealtDamage", this.dealtDamage);
    }

    private byte getLoyalty(ItemStack stack) {
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            return (byte)MathHelper.clamp(EnchantmentHelper.getTridentReturnAcceleration(lv, stack, this), 0, 127);
        }
        return 0;
    }

    @Override
    public void age() {
        byte i = this.dataTracker.get(LOYALTY);
        if (this.pickupType != PersistentProjectileEntity.PickupPermission.ALLOWED || i <= 0) {
            super.age();
        }
    }

    @Override
    protected float getDragInWater() {
        return 0.99f;
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }
}

