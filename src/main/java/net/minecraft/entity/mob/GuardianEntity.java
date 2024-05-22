/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class GuardianEntity
extends HostileEntity {
    protected static final int WARMUP_TIME = 80;
    private static final TrackedData<Boolean> SPIKES_RETRACTED = DataTracker.registerData(GuardianEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> BEAM_TARGET_ID = DataTracker.registerData(GuardianEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private float tailAngle;
    private float prevTailAngle;
    private float spikesExtensionRate;
    private float spikesExtension;
    private float prevSpikesExtension;
    @Nullable
    private LivingEntity cachedBeamTarget;
    private int beamTicks;
    private boolean flopping;
    @Nullable
    protected WanderAroundGoal wanderGoal;

    public GuardianEntity(EntityType<? extends GuardianEntity> arg, World arg2) {
        super((EntityType<? extends HostileEntity>)arg, arg2);
        this.experiencePoints = 10;
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        this.moveControl = new GuardianMoveControl(this);
        this.prevTailAngle = this.tailAngle = this.random.nextFloat();
    }

    @Override
    protected void initGoals() {
        GoToWalkTargetGoal lv = new GoToWalkTargetGoal(this, 1.0);
        this.wanderGoal = new WanderAroundGoal(this, 1.0, 80);
        this.goalSelector.add(4, new FireBeamGoal(this));
        this.goalSelector.add(5, lv);
        this.goalSelector.add(7, this.wanderGoal);
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAtEntityGoal(this, GuardianEntity.class, 12.0f, 0.01f));
        this.goalSelector.add(9, new LookAroundGoal(this));
        this.wanderGoal.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        lv.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        this.targetSelector.add(1, new ActiveTargetGoal<LivingEntity>(this, LivingEntity.class, 10, true, false, new GuardianTargetPredicate(this)));
    }

    public static DefaultAttributeContainer.Builder createGuardianAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SwimNavigation(this, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SPIKES_RETRACTED, false);
        builder.add(BEAM_TARGET_ID, 0);
    }

    public boolean areSpikesRetracted() {
        return this.dataTracker.get(SPIKES_RETRACTED);
    }

    void setSpikesRetracted(boolean retracted) {
        this.dataTracker.set(SPIKES_RETRACTED, retracted);
    }

    public int getWarmupTime() {
        return 80;
    }

    void setBeamTarget(int entityId) {
        this.dataTracker.set(BEAM_TARGET_ID, entityId);
    }

    public boolean hasBeamTarget() {
        return this.dataTracker.get(BEAM_TARGET_ID) != 0;
    }

    @Nullable
    public LivingEntity getBeamTarget() {
        if (!this.hasBeamTarget()) {
            return null;
        }
        if (this.getWorld().isClient) {
            if (this.cachedBeamTarget != null) {
                return this.cachedBeamTarget;
            }
            Entity lv = this.getWorld().getEntityById(this.dataTracker.get(BEAM_TARGET_ID));
            if (lv instanceof LivingEntity) {
                this.cachedBeamTarget = (LivingEntity)lv;
                return this.cachedBeamTarget;
            }
            return null;
        }
        return this.getTarget();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (BEAM_TARGET_ID.equals(data)) {
            this.beamTicks = 0;
            this.cachedBeamTarget = null;
        }
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_GUARDIAN_AMBIENT : SoundEvents.ENTITY_GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_GUARDIAN_HURT : SoundEvents.ENTITY_GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_GUARDIAN_DEATH : SoundEvents.ENTITY_GUARDIAN_DEATH_LAND;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (world.getFluidState(pos).isIn(FluidTags.WATER)) {
            return 10.0f + world.getPhototaxisFavor(pos);
        }
        return super.getPathfindingFavor(pos, world);
    }

    @Override
    public void tickMovement() {
        if (this.isAlive()) {
            if (this.getWorld().isClient) {
                Vec3d lv;
                this.prevTailAngle = this.tailAngle;
                if (!this.isTouchingWater()) {
                    this.spikesExtensionRate = 2.0f;
                    lv = this.getVelocity();
                    if (lv.y > 0.0 && this.flopping && !this.isSilent()) {
                        this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), this.getFlopSound(), this.getSoundCategory(), 1.0f, 1.0f, false);
                    }
                    this.flopping = lv.y < 0.0 && this.getWorld().isTopSolid(this.getBlockPos().down(), this);
                } else {
                    this.spikesExtensionRate = this.areSpikesRetracted() ? (this.spikesExtensionRate < 0.5f ? 4.0f : (this.spikesExtensionRate += (0.5f - this.spikesExtensionRate) * 0.1f)) : (this.spikesExtensionRate += (0.125f - this.spikesExtensionRate) * 0.2f);
                }
                this.tailAngle += this.spikesExtensionRate;
                this.prevSpikesExtension = this.spikesExtension;
                this.spikesExtension = !this.isInsideWaterOrBubbleColumn() ? this.random.nextFloat() : (this.areSpikesRetracted() ? (this.spikesExtension += (0.0f - this.spikesExtension) * 0.25f) : (this.spikesExtension += (1.0f - this.spikesExtension) * 0.06f));
                if (this.areSpikesRetracted() && this.isTouchingWater()) {
                    lv = this.getRotationVec(0.0f);
                    for (int i = 0; i < 2; ++i) {
                        this.getWorld().addParticle(ParticleTypes.BUBBLE, this.getParticleX(0.5) - lv.x * 1.5, this.getRandomBodyY() - lv.y * 1.5, this.getParticleZ(0.5) - lv.z * 1.5, 0.0, 0.0, 0.0);
                    }
                }
                if (this.hasBeamTarget()) {
                    LivingEntity lv2;
                    if (this.beamTicks < this.getWarmupTime()) {
                        ++this.beamTicks;
                    }
                    if ((lv2 = this.getBeamTarget()) != null) {
                        this.getLookControl().lookAt(lv2, 90.0f, 90.0f);
                        this.getLookControl().tick();
                        double d = this.getBeamProgress(0.0f);
                        double e = lv2.getX() - this.getX();
                        double f = lv2.getBodyY(0.5) - this.getEyeY();
                        double g = lv2.getZ() - this.getZ();
                        double h = Math.sqrt(e * e + f * f + g * g);
                        e /= h;
                        f /= h;
                        g /= h;
                        double j = this.random.nextDouble();
                        while (j < h) {
                            this.getWorld().addParticle(ParticleTypes.BUBBLE, this.getX() + e * (j += 1.8 - d + this.random.nextDouble() * (1.7 - d)), this.getEyeY() + f * j, this.getZ() + g * j, 0.0, 0.0, 0.0);
                        }
                    }
                }
            }
            if (this.isInsideWaterOrBubbleColumn()) {
                this.setAir(300);
            } else if (this.isOnGround()) {
                this.setVelocity(this.getVelocity().add((this.random.nextFloat() * 2.0f - 1.0f) * 0.4f, 0.5, (this.random.nextFloat() * 2.0f - 1.0f) * 0.4f));
                this.setYaw(this.random.nextFloat() * 360.0f);
                this.setOnGround(false);
                this.velocityDirty = true;
            }
            if (this.hasBeamTarget()) {
                this.setYaw(this.headYaw);
            }
        }
        super.tickMovement();
    }

    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_GUARDIAN_FLOP;
    }

    public float getTailAngle(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevTailAngle, this.tailAngle);
    }

    public float getSpikesExtension(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevSpikesExtension, this.spikesExtension);
    }

    public float getBeamProgress(float tickDelta) {
        return ((float)this.beamTicks + tickDelta) / (float)this.getWarmupTime();
    }

    public float getBeamTicks() {
        return this.beamTicks;
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    public static boolean canSpawn(EntityType<? extends GuardianEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return !(random.nextInt(20) != 0 && world.isSkyVisibleAllowingSea(pos) || world.getDifficulty() == Difficulty.PEACEFUL || !SpawnReason.isAnySpawner(spawnReason) && !world.getFluidState(pos).isIn(FluidTags.WATER) || !world.getFluidState(pos.down()).isIn(FluidTags.WATER));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity entity;
        if (this.getWorld().isClient) {
            return false;
        }
        if (!this.areSpikesRetracted() && !source.isIn(DamageTypeTags.AVOIDS_GUARDIAN_THORNS) && !source.isOf(DamageTypes.THORNS) && (entity = source.getSource()) instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            lv.damage(this.getDamageSources().thorns(this), 2.0f);
        }
        if (this.wanderGoal != null) {
            this.wanderGoal.ignoreChanceOnce();
        }
        return super.damage(source, amount);
    }

    @Override
    public int getMaxLookPitchChange() {
        return 180;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement() && this.isTouchingWater()) {
            this.updateVelocity(0.1f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9));
            if (!this.areSpikesRetracted() && this.getTarget() == null) {
                this.setVelocity(this.getVelocity().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(movementInput);
        }
    }

    static class GuardianMoveControl
    extends MoveControl {
        private final GuardianEntity guardian;

        public GuardianMoveControl(GuardianEntity guardian) {
            super(guardian);
            this.guardian = guardian;
        }

        @Override
        public void tick() {
            if (this.state != MoveControl.State.MOVE_TO || this.guardian.getNavigation().isIdle()) {
                this.guardian.setMovementSpeed(0.0f);
                this.guardian.setSpikesRetracted(false);
                return;
            }
            Vec3d lv = new Vec3d(this.targetX - this.guardian.getX(), this.targetY - this.guardian.getY(), this.targetZ - this.guardian.getZ());
            double d = lv.length();
            double e = lv.x / d;
            double f = lv.y / d;
            double g = lv.z / d;
            float h = (float)(MathHelper.atan2(lv.z, lv.x) * 57.2957763671875) - 90.0f;
            this.guardian.setYaw(this.wrapDegrees(this.guardian.getYaw(), h, 90.0f));
            this.guardian.bodyYaw = this.guardian.getYaw();
            float i = (float)(this.speed * this.guardian.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
            float j = MathHelper.lerp(0.125f, this.guardian.getMovementSpeed(), i);
            this.guardian.setMovementSpeed(j);
            double k = Math.sin((double)(this.guardian.age + this.guardian.getId()) * 0.5) * 0.05;
            double l = Math.cos(this.guardian.getYaw() * ((float)Math.PI / 180));
            double m = Math.sin(this.guardian.getYaw() * ((float)Math.PI / 180));
            double n = Math.sin((double)(this.guardian.age + this.guardian.getId()) * 0.75) * 0.05;
            this.guardian.setVelocity(this.guardian.getVelocity().add(k * l, n * (m + l) * 0.25 + (double)j * f * 0.1, k * m));
            LookControl lv2 = this.guardian.getLookControl();
            double o = this.guardian.getX() + e * 2.0;
            double p = this.guardian.getEyeY() + f / d;
            double q = this.guardian.getZ() + g * 2.0;
            double r = lv2.getLookX();
            double s = lv2.getLookY();
            double t = lv2.getLookZ();
            if (!lv2.isLookingAtSpecificPosition()) {
                r = o;
                s = p;
                t = q;
            }
            this.guardian.getLookControl().lookAt(MathHelper.lerp(0.125, r, o), MathHelper.lerp(0.125, s, p), MathHelper.lerp(0.125, t, q), 10.0f, 40.0f);
            this.guardian.setSpikesRetracted(true);
        }
    }

    static class FireBeamGoal
    extends Goal {
        private final GuardianEntity guardian;
        private int beamTicks;
        private final boolean elder;

        public FireBeamGoal(GuardianEntity guardian) {
            this.guardian = guardian;
            this.elder = guardian instanceof ElderGuardianEntity;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity lv = this.guardian.getTarget();
            return lv != null && lv.isAlive();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && (this.elder || this.guardian.getTarget() != null && this.guardian.squaredDistanceTo(this.guardian.getTarget()) > 9.0);
        }

        @Override
        public void start() {
            this.beamTicks = -10;
            this.guardian.getNavigation().stop();
            LivingEntity lv = this.guardian.getTarget();
            if (lv != null) {
                this.guardian.getLookControl().lookAt(lv, 90.0f, 90.0f);
            }
            this.guardian.velocityDirty = true;
        }

        @Override
        public void stop() {
            this.guardian.setBeamTarget(0);
            this.guardian.setTarget(null);
            this.guardian.wanderGoal.ignoreChanceOnce();
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity lv = this.guardian.getTarget();
            if (lv == null) {
                return;
            }
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().lookAt(lv, 90.0f, 90.0f);
            if (!this.guardian.canSee(lv)) {
                this.guardian.setTarget(null);
                return;
            }
            ++this.beamTicks;
            if (this.beamTicks == 0) {
                this.guardian.setBeamTarget(lv.getId());
                if (!this.guardian.isSilent()) {
                    this.guardian.getWorld().sendEntityStatus(this.guardian, EntityStatuses.PLAY_GUARDIAN_ATTACK_SOUND);
                }
            } else if (this.beamTicks >= this.guardian.getWarmupTime()) {
                float f = 1.0f;
                if (this.guardian.getWorld().getDifficulty() == Difficulty.HARD) {
                    f += 2.0f;
                }
                if (this.elder) {
                    f += 2.0f;
                }
                lv.damage(this.guardian.getDamageSources().indirectMagic(this.guardian, this.guardian), f);
                this.guardian.tryAttack(lv);
                this.guardian.setTarget(null);
            }
            super.tick();
        }
    }

    static class GuardianTargetPredicate
    implements Predicate<LivingEntity> {
        private final GuardianEntity owner;

        public GuardianTargetPredicate(GuardianEntity owner) {
            this.owner = owner;
        }

        @Override
        public boolean test(@Nullable LivingEntity arg) {
            return (arg instanceof PlayerEntity || arg instanceof SquidEntity || arg instanceof AxolotlEntity) && arg.squaredDistanceTo(this.owner) > 9.0;
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object context) {
            return this.test((LivingEntity)context);
        }
    }
}

