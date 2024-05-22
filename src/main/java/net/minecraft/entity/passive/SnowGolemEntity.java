/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class SnowGolemEntity
extends GolemEntity
implements Shearable,
RangedAttackMob {
    private static final TrackedData<Byte> SNOW_GOLEM_FLAGS = DataTracker.registerData(SnowGolemEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final byte HAS_PUMPKIN_FLAG = 16;

    public SnowGolemEntity(EntityType<? extends SnowGolemEntity> arg, World arg2) {
        super((EntityType<? extends GolemEntity>)arg, arg2);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new ProjectileAttackGoal(this, 1.25, 20, 10.0f));
        this.goalSelector.add(2, new WanderAroundFarGoal((PathAwareEntity)this, 1.0, 1.0000001E-5f));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<MobEntity>(this, MobEntity.class, 10, true, false, entity -> entity instanceof Monster));
    }

    public static DefaultAttributeContainer.Builder createSnowGolemAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 4.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SNOW_GOLEM_FLAGS, (byte)16);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Pumpkin", this.hasPumpkin());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Pumpkin")) {
            this.setHasPumpkin(nbt.getBoolean("Pumpkin"));
        }
    }

    @Override
    public boolean hurtByWater() {
        return true;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient) {
            if (this.getWorld().getBiome(this.getBlockPos()).isIn(BiomeTags.SNOW_GOLEM_MELTS)) {
                this.damage(this.getDamageSources().onFire(), 1.0f);
            }
            if (!this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                return;
            }
            BlockState lv = Blocks.SNOW.getDefaultState();
            for (int i = 0; i < 4; ++i) {
                int j = MathHelper.floor(this.getX() + (double)((float)(i % 2 * 2 - 1) * 0.25f));
                int k = MathHelper.floor(this.getY());
                int l = MathHelper.floor(this.getZ() + (double)((float)(i / 2 % 2 * 2 - 1) * 0.25f));
                BlockPos lv2 = new BlockPos(j, k, l);
                if (!this.getWorld().getBlockState(lv2).isAir() || !lv.canPlaceAt(this.getWorld(), lv2)) continue;
                this.getWorld().setBlockState(lv2, lv);
                this.getWorld().emitGameEvent(GameEvent.BLOCK_PLACE, lv2, GameEvent.Emitter.of(this, lv));
            }
        }
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        SnowballEntity lv = new SnowballEntity(this.getWorld(), this);
        double d = target.getEyeY() - (double)1.1f;
        double e = target.getX() - this.getX();
        double g = d - lv.getY();
        double h = target.getZ() - this.getZ();
        double i = Math.sqrt(e * e + h * h) * (double)0.2f;
        lv.setVelocity(e, g + i, h, 1.6f, 12.0f);
        this.playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.4f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.getWorld().spawnEntity(lv);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (lv.isOf(Items.SHEARS) && this.isShearable()) {
            this.sheared(SoundCategory.PLAYERS);
            this.emitGameEvent(GameEvent.SHEAR, player);
            if (!this.getWorld().isClient) {
                lv.damage(1, player, SnowGolemEntity.getSlotForHand(hand));
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return ActionResult.PASS;
    }

    @Override
    public void sheared(SoundCategory shearedSoundCategory) {
        this.getWorld().playSoundFromEntity(null, this, SoundEvents.ENTITY_SNOW_GOLEM_SHEAR, shearedSoundCategory, 1.0f, 1.0f);
        if (!this.getWorld().isClient()) {
            this.setHasPumpkin(false);
            this.dropStack(new ItemStack(Items.CARVED_PUMPKIN), this.getStandingEyeHeight());
        }
    }

    @Override
    public boolean isShearable() {
        return this.isAlive() && this.hasPumpkin();
    }

    public boolean hasPumpkin() {
        return (this.dataTracker.get(SNOW_GOLEM_FLAGS) & 0x10) != 0;
    }

    public void setHasPumpkin(boolean hasPumpkin) {
        byte b = this.dataTracker.get(SNOW_GOLEM_FLAGS);
        if (hasPumpkin) {
            this.dataTracker.set(SNOW_GOLEM_FLAGS, (byte)(b | 0x10));
        } else {
            this.dataTracker.set(SNOW_GOLEM_FLAGS, (byte)(b & 0xFFFFFFEF));
        }
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SNOW_GOLEM_AMBIENT;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SNOW_GOLEM_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SNOW_GOLEM_DEATH;
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.75f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }
}

