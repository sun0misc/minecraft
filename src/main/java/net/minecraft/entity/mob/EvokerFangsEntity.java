/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.List;
import java.util.UUID;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EvokerFangsEntity
extends Entity
implements Ownable {
    public static final int field_30662 = 20;
    public static final int field_30663 = 2;
    public static final int field_30664 = 14;
    private int warmup;
    private boolean startedAttack;
    private int ticksLeft = 22;
    private boolean playingAnimation;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUuid;

    public EvokerFangsEntity(EntityType<? extends EvokerFangsEntity> arg, World arg2) {
        super(arg, arg2);
    }

    public EvokerFangsEntity(World world, double x, double y, double z, float yaw, int warmup, LivingEntity owner) {
        this((EntityType<? extends EvokerFangsEntity>)EntityType.EVOKER_FANGS, world);
        this.warmup = warmup;
        this.setOwner(owner);
        this.setYaw(yaw * 57.295776f);
        this.setPosition(x, y, z);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        this.ownerUuid = owner == null ? null : owner.getUuid();
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        Entity lv;
        if (this.owner == null && this.ownerUuid != null && this.getWorld() instanceof ServerWorld && (lv = ((ServerWorld)this.getWorld()).getEntity(this.ownerUuid)) instanceof LivingEntity) {
            this.owner = (LivingEntity)lv;
        }
        return this.owner;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.warmup = nbt.getInt("Warmup");
        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Warmup", this.warmup);
        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", this.ownerUuid);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            if (this.playingAnimation) {
                --this.ticksLeft;
                if (this.ticksLeft == 14) {
                    for (int i = 0; i < 12; ++i) {
                        double d = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getWidth() * 0.5;
                        double e = this.getY() + 0.05 + this.random.nextDouble();
                        double f = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getWidth() * 0.5;
                        double g = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        double h = 0.3 + this.random.nextDouble() * 0.3;
                        double j = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        this.getWorld().addParticle(ParticleTypes.CRIT, d, e + 1.0, f, g, h, j);
                    }
                }
            }
        } else if (--this.warmup < 0) {
            if (this.warmup == -8) {
                List<LivingEntity> list = this.getWorld().getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(0.2, 0.0, 0.2));
                for (LivingEntity lv : list) {
                    this.damage(lv);
                }
            }
            if (!this.startedAttack) {
                this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_ATTACK_SOUND);
                this.startedAttack = true;
            }
            if (--this.ticksLeft < 0) {
                this.discard();
            }
        }
    }

    private void damage(LivingEntity target) {
        LivingEntity lv = this.getOwner();
        if (!target.isAlive() || target.isInvulnerable() || target == lv) {
            return;
        }
        if (lv == null) {
            target.damage(this.getDamageSources().magic(), 6.0f);
        } else {
            World world;
            if (lv.isTeammate(target)) {
                return;
            }
            DamageSource lv2 = this.getDamageSources().indirectMagic(this, lv);
            if (target.damage(lv2, 6.0f) && (world = this.getWorld()) instanceof ServerWorld) {
                ServerWorld lv3 = (ServerWorld)world;
                EnchantmentHelper.onTargetDamaged(lv3, target, lv2);
            }
        }
    }

    @Override
    public void handleStatus(byte status) {
        super.handleStatus(status);
        if (status == EntityStatuses.PLAY_ATTACK_SOUND) {
            this.playingAnimation = true;
            if (!this.isSilent()) {
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, this.getSoundCategory(), 1.0f, this.random.nextFloat() * 0.2f + 0.85f, false);
            }
        }
    }

    public float getAnimationProgress(float tickDelta) {
        if (!this.playingAnimation) {
            return 0.0f;
        }
        int i = this.ticksLeft - 2;
        if (i <= 0) {
            return 1.0f;
        }
        return 1.0f - ((float)i - tickDelta) / 20.0f;
    }

    @Override
    @Nullable
    public /* synthetic */ Entity getOwner() {
        return this.getOwner();
    }
}

