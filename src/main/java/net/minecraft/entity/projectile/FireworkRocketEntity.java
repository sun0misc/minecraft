/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class FireworkRocketEntity
extends ProjectileEntity
implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(FireworkRocketEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<OptionalInt> SHOOTER_ENTITY_ID = DataTracker.registerData(FireworkRocketEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
    private static final TrackedData<Boolean> SHOT_AT_ANGLE = DataTracker.registerData(FireworkRocketEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int life;
    private int lifeTime;
    @Nullable
    private LivingEntity shooter;

    public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> arg, World arg2) {
        super((EntityType<? extends ProjectileEntity>)arg, arg2);
    }

    public FireworkRocketEntity(World world, double x, double y, double z, ItemStack stack) {
        super((EntityType<? extends ProjectileEntity>)EntityType.FIREWORK_ROCKET, world);
        this.life = 0;
        this.setPosition(x, y, z);
        this.dataTracker.set(ITEM, stack.copy());
        int i = 1;
        FireworksComponent lv = stack.get(DataComponentTypes.FIREWORKS);
        if (lv != null) {
            i += lv.flightDuration();
        }
        this.setVelocity(this.random.nextTriangular(0.0, 0.002297), 0.05, this.random.nextTriangular(0.0, 0.002297));
        this.lifeTime = 10 * i + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public FireworkRocketEntity(World world, @Nullable Entity entity, double x, double y, double z, ItemStack stack) {
        this(world, x, y, z, stack);
        this.setOwner(entity);
    }

    public FireworkRocketEntity(World world, ItemStack stack, LivingEntity shooter) {
        this(world, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
        this.dataTracker.set(SHOOTER_ENTITY_ID, OptionalInt.of(shooter.getId()));
        this.shooter = shooter;
    }

    public FireworkRocketEntity(World world, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
        this(world, x, y, z, stack);
        this.dataTracker.set(SHOT_AT_ANGLE, shotAtAngle);
    }

    public FireworkRocketEntity(World world, ItemStack stack, Entity entity, double x, double y, double z, boolean shotAtAngle) {
        this(world, stack, x, y, z, shotAtAngle);
        this.setOwner(entity);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ITEM, FireworkRocketEntity.getDefaultStack());
        builder.add(SHOOTER_ENTITY_ID, OptionalInt.empty());
        builder.add(SHOT_AT_ANGLE, false);
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 4096.0 && !this.wasShotByEntity();
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return super.shouldRender(cameraX, cameraY, cameraZ) && !this.wasShotByEntity();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.wasShotByEntity()) {
            if (this.shooter == null) {
                this.dataTracker.get(SHOOTER_ENTITY_ID).ifPresent(id -> {
                    Entity lv = this.getWorld().getEntityById(id);
                    if (lv instanceof LivingEntity) {
                        this.shooter = (LivingEntity)lv;
                    }
                });
            }
            if (this.shooter != null) {
                if (this.shooter.isFallFlying()) {
                    Vec3d lv = this.shooter.getRotationVector();
                    double d = 1.5;
                    double e = 0.1;
                    Vec3d lv2 = this.shooter.getVelocity();
                    this.shooter.setVelocity(lv2.add(lv.x * 0.1 + (lv.x * 1.5 - lv2.x) * 0.5, lv.y * 0.1 + (lv.y * 1.5 - lv2.y) * 0.5, lv.z * 0.1 + (lv.z * 1.5 - lv2.z) * 0.5));
                    lv3 = this.shooter.getHandPosOffset(Items.FIREWORK_ROCKET);
                } else {
                    lv3 = Vec3d.ZERO;
                }
                this.setPosition(this.shooter.getX() + lv3.x, this.shooter.getY() + lv3.y, this.shooter.getZ() + lv3.z);
                this.setVelocity(this.shooter.getVelocity());
            }
        } else {
            if (!this.wasShotAtAngle()) {
                double f = this.horizontalCollision ? 1.0 : 1.15;
                this.setVelocity(this.getVelocity().multiply(f, 1.0, f).add(0.0, 0.04, 0.0));
            }
            lv3 = this.getVelocity();
            this.move(MovementType.SELF, lv3);
            this.setVelocity(lv3);
        }
        HitResult lv4 = ProjectileUtil.getCollision(this, this::canHit);
        if (!this.noClip) {
            this.hitOrDeflect(lv4);
            this.velocityDirty = true;
        }
        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0f, 1.0f);
        }
        ++this.life;
        if (this.getWorld().isClient && this.life % 2 < 2) {
            this.getWorld().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, -this.getVelocity().y * 0.5, this.random.nextGaussian() * 0.05);
        }
        if (!this.getWorld().isClient && this.life > this.lifeTime) {
            this.explodeAndRemove();
        }
    }

    private void explodeAndRemove() {
        this.getWorld().sendEntityStatus(this, EntityStatuses.EXPLODE_FIREWORK_CLIENT);
        this.emitGameEvent(GameEvent.EXPLODE, this.getOwner());
        this.explode();
        this.discard();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.getWorld().isClient) {
            return;
        }
        this.explodeAndRemove();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        BlockPos lv = new BlockPos(blockHitResult.getBlockPos());
        this.getWorld().getBlockState(lv).onEntityCollision(this.getWorld(), lv, this);
        if (!this.getWorld().isClient() && this.hasExplosionEffects()) {
            this.explodeAndRemove();
        }
        super.onBlockHit(blockHitResult);
    }

    private boolean hasExplosionEffects() {
        return !this.getExplosions().isEmpty();
    }

    private void explode() {
        float f = 0.0f;
        List<FireworkExplosionComponent> list = this.getExplosions();
        if (!list.isEmpty()) {
            f = 5.0f + (float)(list.size() * 2);
        }
        if (f > 0.0f) {
            if (this.shooter != null) {
                this.shooter.damage(this.getDamageSources().fireworks(this, this.getOwner()), 5.0f + (float)(list.size() * 2));
            }
            double d = 5.0;
            Vec3d lv = this.getPos();
            List<LivingEntity> list2 = this.getWorld().getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(5.0));
            for (LivingEntity lv2 : list2) {
                if (lv2 == this.shooter || this.squaredDistanceTo(lv2) > 25.0) continue;
                boolean bl = false;
                for (int i = 0; i < 2; ++i) {
                    Vec3d lv3 = new Vec3d(lv2.getX(), lv2.getBodyY(0.5 * (double)i), lv2.getZ());
                    BlockHitResult lv4 = this.getWorld().raycast(new RaycastContext(lv, lv3, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
                    if (((HitResult)lv4).getType() != HitResult.Type.MISS) continue;
                    bl = true;
                    break;
                }
                if (!bl) continue;
                float g = f * (float)Math.sqrt((5.0 - (double)this.distanceTo(lv2)) / 5.0);
                lv2.damage(this.getDamageSources().fireworks(this, this.getOwner()), g);
            }
        }
    }

    private boolean wasShotByEntity() {
        return this.dataTracker.get(SHOOTER_ENTITY_ID).isPresent();
    }

    public boolean wasShotAtAngle() {
        return this.dataTracker.get(SHOT_AT_ANGLE);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.EXPLODE_FIREWORK_CLIENT && this.getWorld().isClient) {
            Vec3d lv = this.getVelocity();
            this.getWorld().addFireworkParticle(this.getX(), this.getY(), this.getZ(), lv.x, lv.y, lv.z, this.getExplosions());
        }
        super.handleStatus(status);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Life", this.life);
        nbt.putInt("LifeTime", this.lifeTime);
        nbt.put("FireworksItem", this.getStack().encode(this.getRegistryManager()));
        nbt.putBoolean("ShotAtAngle", this.dataTracker.get(SHOT_AT_ANGLE));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.life = nbt.getInt("Life");
        this.lifeTime = nbt.getInt("LifeTime");
        if (nbt.contains("FireworksItem", NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(ITEM, ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound("FireworksItem")).orElseGet(FireworkRocketEntity::getDefaultStack));
        } else {
            this.dataTracker.set(ITEM, FireworkRocketEntity.getDefaultStack());
        }
        if (nbt.contains("ShotAtAngle")) {
            this.dataTracker.set(SHOT_AT_ANGLE, nbt.getBoolean("ShotAtAngle"));
        }
    }

    private List<FireworkExplosionComponent> getExplosions() {
        ItemStack lv = this.dataTracker.get(ITEM);
        FireworksComponent lv2 = lv.get(DataComponentTypes.FIREWORKS);
        return lv2 != null ? lv2.explosions() : List.of();
    }

    @Override
    public ItemStack getStack() {
        return this.dataTracker.get(ITEM);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    private static ItemStack getDefaultStack() {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public DoubleDoubleImmutablePair getKnockback(LivingEntity target, DamageSource source) {
        double d = target.getPos().x - this.getPos().x;
        double e = target.getPos().z - this.getPos().z;
        return DoubleDoubleImmutablePair.of(d, e);
    }
}

