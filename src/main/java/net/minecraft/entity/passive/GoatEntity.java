/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GoatBrain;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.InstrumentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class GoatEntity
extends AnimalEntity {
    public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.changing(0.9f, 1.3f).scaled(0.7f);
    private static final int DEFAULT_ATTACK_DAMAGE = 2;
    private static final int BABY_ATTACK_DAMAGE = 1;
    protected static final ImmutableList<SensorType<? extends Sensor<? super GoatEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.GOAT_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, new MemoryModuleType[]{MemoryModuleType.IS_TEMPTED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleType.RAM_TARGET, MemoryModuleType.IS_PANICKING});
    public static final int FALL_DAMAGE_SUBTRACTOR = 10;
    public static final double SCREAMING_CHANCE = 0.02;
    public static final double field_39046 = (double)0.1f;
    private static final TrackedData<Boolean> SCREAMING = DataTracker.registerData(GoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> LEFT_HORN = DataTracker.registerData(GoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> RIGHT_HORN = DataTracker.registerData(GoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private boolean preparingRam;
    private int headPitch;

    public GoatEntity(EntityType<? extends GoatEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
        this.getNavigation().setCanSwim(true);
        this.setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1.0f);
    }

    public ItemStack getGoatHornStack() {
        Random lv = Random.create(this.getUuid().hashCode());
        TagKey<Instrument> lv2 = this.isScreaming() ? InstrumentTags.SCREAMING_GOAT_HORNS : InstrumentTags.REGULAR_GOAT_HORNS;
        RegistryEntryList.Named<Instrument> lv3 = Registries.INSTRUMENT.getOrCreateEntryList(lv2);
        return GoatHornItem.getStackForInstrument(Items.GOAT_HORN, lv3.getRandom(lv).get());
    }

    protected Brain.Profile<GoatEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return GoatBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public static DefaultAttributeContainer.Builder createGoatAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void onGrowUp() {
        if (this.isBaby()) {
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(1.0);
            this.removeHorns();
        } else {
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(2.0);
            this.addHorns();
        }
    }

    @Override
    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return super.computeFallDamage(fallDistance, damageMultiplier) - 10;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isScreaming()) {
            return SoundEvents.ENTITY_GOAT_SCREAMING_AMBIENT;
        }
        return SoundEvents.ENTITY_GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isScreaming()) {
            return SoundEvents.ENTITY_GOAT_SCREAMING_HURT;
        }
        return SoundEvents.ENTITY_GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isScreaming()) {
            return SoundEvents.ENTITY_GOAT_SCREAMING_DEATH;
        }
        return SoundEvents.ENTITY_GOAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_GOAT_STEP, 0.15f, 1.0f);
    }

    protected SoundEvent getMilkingSound() {
        if (this.isScreaming()) {
            return SoundEvents.ENTITY_GOAT_SCREAMING_MILK;
        }
        return SoundEvents.ENTITY_GOAT_MILK;
    }

    @Override
    @Nullable
    public GoatEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        GoatEntity lv = EntityType.GOAT.create(arg);
        if (lv != null) {
            PassiveEntity lv3;
            GoatBrain.resetLongJumpCooldown(lv, arg.getRandom());
            PassiveEntity lv2 = arg.getRandom().nextBoolean() ? this : arg2;
            boolean bl = lv2 instanceof GoatEntity && ((GoatEntity)(lv3 = lv2)).isScreaming() || arg.getRandom().nextDouble() < 0.02;
            lv.setScreaming(bl);
        }
        return lv;
    }

    public Brain<GoatEntity> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void mobTick() {
        this.getWorld().getProfiler().push("goatBrain");
        this.getBrain().tick((ServerWorld)this.getWorld(), this);
        this.getWorld().getProfiler().pop();
        this.getWorld().getProfiler().push("goatActivityUpdate");
        GoatBrain.updateActivities(this);
        this.getWorld().getProfiler().pop();
        super.mobTick();
    }

    @Override
    public int getMaxHeadRotation() {
        return 15;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        int i = this.getMaxHeadRotation();
        float g = MathHelper.subtractAngles(this.bodyYaw, headYaw);
        float h = MathHelper.clamp(g, (float)(-i), (float)i);
        super.setHeadYaw(this.bodyYaw + h);
    }

    @Override
    public SoundEvent getEatSound(ItemStack stack) {
        return this.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_EAT : SoundEvents.ENTITY_GOAT_EAT;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.GOAT_FOOD);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (lv.isOf(Items.BUCKET) && !this.isBaby()) {
            player.playSound(this.getMilkingSound(), 1.0f, 1.0f);
            ItemStack lv2 = ItemUsage.exchangeStack(lv, player, Items.MILK_BUCKET.getDefaultStack());
            player.setStackInHand(hand, lv2);
            return ActionResult.success(this.getWorld().isClient);
        }
        ActionResult lv3 = super.interactMob(player, hand);
        if (lv3.isAccepted() && this.isBreedingItem(lv)) {
            this.getWorld().playSoundFromEntity(null, this, this.getEatSound(lv), SoundCategory.NEUTRAL, 1.0f, MathHelper.nextBetween(this.getWorld().random, 0.8f, 1.2f));
        }
        return lv3;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random lv = world.getRandom();
        GoatBrain.resetLongJumpCooldown(this, lv);
        this.setScreaming(lv.nextDouble() < 0.02);
        this.onGrowUp();
        if (!this.isBaby() && (double)lv.nextFloat() < (double)0.1f) {
            TrackedData<Boolean> lv2 = lv.nextBoolean() ? LEFT_HORN : RIGHT_HORN;
            this.dataTracker.set(lv2, false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected void sendAiDebugData() {
        super.sendAiDebugData();
        DebugInfoSender.sendBrainDebugData(this);
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return pose == EntityPose.LONG_JUMPING ? LONG_JUMPING_DIMENSIONS.scaled(this.getScaleFactor()) : super.getBaseDimensions(pose);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsScreamingGoat", this.isScreaming());
        nbt.putBoolean("HasLeftHorn", this.hasLeftHorn());
        nbt.putBoolean("HasRightHorn", this.hasRightHorn());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setScreaming(nbt.getBoolean("IsScreamingGoat"));
        this.dataTracker.set(LEFT_HORN, nbt.getBoolean("HasLeftHorn"));
        this.dataTracker.set(RIGHT_HORN, nbt.getBoolean("HasRightHorn"));
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PREPARE_RAM) {
            this.preparingRam = true;
        } else if (status == EntityStatuses.FINISH_RAM) {
            this.preparingRam = false;
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public void tickMovement() {
        this.headPitch = this.preparingRam ? ++this.headPitch : (this.headPitch -= 2);
        this.headPitch = MathHelper.clamp(this.headPitch, 0, 20);
        super.tickMovement();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SCREAMING, false);
        builder.add(LEFT_HORN, true);
        builder.add(RIGHT_HORN, true);
    }

    public boolean hasLeftHorn() {
        return this.dataTracker.get(LEFT_HORN);
    }

    public boolean hasRightHorn() {
        return this.dataTracker.get(RIGHT_HORN);
    }

    public boolean dropHorn() {
        boolean bl = this.hasLeftHorn();
        boolean bl2 = this.hasRightHorn();
        if (!bl && !bl2) {
            return false;
        }
        TrackedData<Boolean> lv = !bl ? RIGHT_HORN : (!bl2 ? LEFT_HORN : (this.random.nextBoolean() ? LEFT_HORN : RIGHT_HORN));
        this.dataTracker.set(lv, false);
        Vec3d lv2 = this.getPos();
        ItemStack lv3 = this.getGoatHornStack();
        double d = MathHelper.nextBetween(this.random, -0.2f, 0.2f);
        double e = MathHelper.nextBetween(this.random, 0.3f, 0.7f);
        double f = MathHelper.nextBetween(this.random, -0.2f, 0.2f);
        ItemEntity lv4 = new ItemEntity(this.getWorld(), lv2.getX(), lv2.getY(), lv2.getZ(), lv3, d, e, f);
        this.getWorld().spawnEntity(lv4);
        return true;
    }

    public void addHorns() {
        this.dataTracker.set(LEFT_HORN, true);
        this.dataTracker.set(RIGHT_HORN, true);
    }

    public void removeHorns() {
        this.dataTracker.set(LEFT_HORN, false);
        this.dataTracker.set(RIGHT_HORN, false);
    }

    public boolean isScreaming() {
        return this.dataTracker.get(SCREAMING);
    }

    public void setScreaming(boolean screaming) {
        this.dataTracker.set(SCREAMING, screaming);
    }

    public float getHeadPitch() {
        return (float)this.headPitch / 20.0f * 30.0f * ((float)Math.PI / 180);
    }

    public static boolean canSpawn(EntityType<? extends AnimalEntity> entityType, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.GOATS_SPAWNABLE_ON) && GoatEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}

