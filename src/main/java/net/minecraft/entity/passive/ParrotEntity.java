/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.FollowMobGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SitOnOwnerShoulderGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TameableShoulderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class ParrotEntity
extends TameableShoulderEntity
implements VariantHolder<Variant>,
Flutterer {
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(ParrotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final Predicate<MobEntity> CAN_IMITATE = new Predicate<MobEntity>(){

        @Override
        public boolean test(@Nullable MobEntity arg) {
            return arg != null && MOB_SOUNDS.containsKey(arg.getType());
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object entity) {
            return this.test((MobEntity)entity);
        }
    };
    static final Map<EntityType<?>, SoundEvent> MOB_SOUNDS = Util.make(Maps.newHashMap(), map -> {
        map.put(EntityType.BLAZE, SoundEvents.ENTITY_PARROT_IMITATE_BLAZE);
        map.put(EntityType.BOGGED, SoundEvents.ENTITY_PARROT_IMITATE_BOGGED);
        map.put(EntityType.BREEZE, SoundEvents.ENTITY_PARROT_IMITATE_BREEZE);
        map.put(EntityType.CAVE_SPIDER, SoundEvents.ENTITY_PARROT_IMITATE_SPIDER);
        map.put(EntityType.CREEPER, SoundEvents.ENTITY_PARROT_IMITATE_CREEPER);
        map.put(EntityType.DROWNED, SoundEvents.ENTITY_PARROT_IMITATE_DROWNED);
        map.put(EntityType.ELDER_GUARDIAN, SoundEvents.ENTITY_PARROT_IMITATE_ELDER_GUARDIAN);
        map.put(EntityType.ENDER_DRAGON, SoundEvents.ENTITY_PARROT_IMITATE_ENDER_DRAGON);
        map.put(EntityType.ENDERMITE, SoundEvents.ENTITY_PARROT_IMITATE_ENDERMITE);
        map.put(EntityType.EVOKER, SoundEvents.ENTITY_PARROT_IMITATE_EVOKER);
        map.put(EntityType.GHAST, SoundEvents.ENTITY_PARROT_IMITATE_GHAST);
        map.put(EntityType.GUARDIAN, SoundEvents.ENTITY_PARROT_IMITATE_GUARDIAN);
        map.put(EntityType.HOGLIN, SoundEvents.ENTITY_PARROT_IMITATE_HOGLIN);
        map.put(EntityType.HUSK, SoundEvents.ENTITY_PARROT_IMITATE_HUSK);
        map.put(EntityType.ILLUSIONER, SoundEvents.ENTITY_PARROT_IMITATE_ILLUSIONER);
        map.put(EntityType.MAGMA_CUBE, SoundEvents.ENTITY_PARROT_IMITATE_MAGMA_CUBE);
        map.put(EntityType.PHANTOM, SoundEvents.ENTITY_PARROT_IMITATE_PHANTOM);
        map.put(EntityType.PIGLIN, SoundEvents.ENTITY_PARROT_IMITATE_PIGLIN);
        map.put(EntityType.PIGLIN_BRUTE, SoundEvents.ENTITY_PARROT_IMITATE_PIGLIN_BRUTE);
        map.put(EntityType.PILLAGER, SoundEvents.ENTITY_PARROT_IMITATE_PILLAGER);
        map.put(EntityType.RAVAGER, SoundEvents.ENTITY_PARROT_IMITATE_RAVAGER);
        map.put(EntityType.SHULKER, SoundEvents.ENTITY_PARROT_IMITATE_SHULKER);
        map.put(EntityType.SILVERFISH, SoundEvents.ENTITY_PARROT_IMITATE_SILVERFISH);
        map.put(EntityType.SKELETON, SoundEvents.ENTITY_PARROT_IMITATE_SKELETON);
        map.put(EntityType.SLIME, SoundEvents.ENTITY_PARROT_IMITATE_SLIME);
        map.put(EntityType.SPIDER, SoundEvents.ENTITY_PARROT_IMITATE_SPIDER);
        map.put(EntityType.STRAY, SoundEvents.ENTITY_PARROT_IMITATE_STRAY);
        map.put(EntityType.VEX, SoundEvents.ENTITY_PARROT_IMITATE_VEX);
        map.put(EntityType.VINDICATOR, SoundEvents.ENTITY_PARROT_IMITATE_VINDICATOR);
        map.put(EntityType.WARDEN, SoundEvents.ENTITY_PARROT_IMITATE_WARDEN);
        map.put(EntityType.WITCH, SoundEvents.ENTITY_PARROT_IMITATE_WITCH);
        map.put(EntityType.WITHER, SoundEvents.ENTITY_PARROT_IMITATE_WITHER);
        map.put(EntityType.WITHER_SKELETON, SoundEvents.ENTITY_PARROT_IMITATE_WITHER_SKELETON);
        map.put(EntityType.ZOGLIN, SoundEvents.ENTITY_PARROT_IMITATE_ZOGLIN);
        map.put(EntityType.ZOMBIE, SoundEvents.ENTITY_PARROT_IMITATE_ZOMBIE);
        map.put(EntityType.ZOMBIE_VILLAGER, SoundEvents.ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER);
    });
    public float flapProgress;
    public float maxWingDeviation;
    public float prevMaxWingDeviation;
    public float prevFlapProgress;
    private float flapSpeed = 1.0f;
    private float field_28640 = 1.0f;
    private boolean songPlaying;
    @Nullable
    private BlockPos songSource;

    public ParrotEntity(EntityType<? extends ParrotEntity> arg, World arg2) {
        super((EntityType<? extends TameableShoulderEntity>)arg, arg2);
        this.moveControl = new FlightMoveControl(this, 10, false);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0f);
        this.setPathfindingPenalty(PathNodeType.COCOA, -1.0f);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.setVariant(Util.getRandom(Variant.values(), world.getRandom()));
        if (entityData == null) {
            entityData = new PassiveEntity.PassiveData(false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new TameableEntity.class_9788(1.25));
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(2, new FollowOwnerGoal(this, 1.0, 5.0f, 1.0f));
        this.goalSelector.add(2, new FlyOntoTreeGoal(this, 1.0));
        this.goalSelector.add(3, new SitOnOwnerShoulderGoal(this));
        this.goalSelector.add(3, new FollowMobGoal(this, 1.0, 3.0f, 7.0f));
    }

    public static DefaultAttributeContainer.Builder createParrotAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.4f).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation lv = new BirdNavigation(this, world);
        lv.setCanPathThroughDoors(false);
        lv.setCanSwim(true);
        lv.setCanEnterOpenDoors(true);
        return lv;
    }

    @Override
    public void tickMovement() {
        if (this.songSource == null || !this.songSource.isWithinDistance(this.getPos(), 3.46) || !this.getWorld().getBlockState(this.songSource).isOf(Blocks.JUKEBOX)) {
            this.songPlaying = false;
            this.songSource = null;
        }
        if (this.getWorld().random.nextInt(400) == 0) {
            ParrotEntity.imitateNearbyMob(this.getWorld(), this);
        }
        super.tickMovement();
        this.flapWings();
    }

    @Override
    public void setNearbySongPlaying(BlockPos songPosition, boolean playing) {
        this.songSource = songPosition;
        this.songPlaying = playing;
    }

    public boolean isSongPlaying() {
        return this.songPlaying;
    }

    private void flapWings() {
        this.prevFlapProgress = this.flapProgress;
        this.prevMaxWingDeviation = this.maxWingDeviation;
        this.maxWingDeviation += (float)(this.isOnGround() || this.hasVehicle() ? -1 : 4) * 0.3f;
        this.maxWingDeviation = MathHelper.clamp(this.maxWingDeviation, 0.0f, 1.0f);
        if (!this.isOnGround() && this.flapSpeed < 1.0f) {
            this.flapSpeed = 1.0f;
        }
        this.flapSpeed *= 0.9f;
        Vec3d lv = this.getVelocity();
        if (!this.isOnGround() && lv.y < 0.0) {
            this.setVelocity(lv.multiply(1.0, 0.6, 1.0));
        }
        this.flapProgress += this.flapSpeed * 2.0f;
    }

    public static boolean imitateNearbyMob(World world, Entity parrot) {
        MobEntity lv;
        if (!parrot.isAlive() || parrot.isSilent() || world.random.nextInt(2) != 0) {
            return false;
        }
        List<MobEntity> list = world.getEntitiesByClass(MobEntity.class, parrot.getBoundingBox().expand(20.0), CAN_IMITATE);
        if (!list.isEmpty() && !(lv = list.get(world.random.nextInt(list.size()))).isSilent()) {
            SoundEvent lv2 = ParrotEntity.getSound(lv.getType());
            world.playSound(null, parrot.getX(), parrot.getY(), parrot.getZ(), lv2, parrot.getSoundCategory(), 0.7f, ParrotEntity.getSoundPitch(world.random));
            return true;
        }
        return false;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (!this.isTamed() && lv.isIn(ItemTags.PARROT_FOOD)) {
            lv.decrementUnlessCreative(1, player);
            if (!this.isSilent()) {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PARROT_EAT, this.getSoundCategory(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            }
            if (!this.getWorld().isClient) {
                if (this.random.nextInt(10) == 0) {
                    this.setOwner(player);
                    this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
                } else {
                    this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
                }
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        if (lv.isIn(ItemTags.PARROT_POISONOUS_FOOD)) {
            lv.decrementUnlessCreative(1, player);
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 900));
            if (player.isCreative() || !this.isInvulnerable()) {
                this.damage(this.getDamageSources().playerAttack(player), Float.MAX_VALUE);
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        if (!this.isInAir() && this.isTamed() && this.isOwner(player)) {
            if (!this.getWorld().isClient) {
                this.setSitting(!this.isSitting());
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    public static boolean canSpawn(EntityType<ParrotEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.PARROTS_SPAWNABLE_ON) && ParrotEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return false;
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    @Nullable
    public SoundEvent getAmbientSound() {
        return ParrotEntity.getRandomSound(this.getWorld(), this.getWorld().random);
    }

    public static SoundEvent getRandomSound(World world, Random random) {
        if (world.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(1000) == 0) {
            ArrayList<EntityType<?>> list = Lists.newArrayList(MOB_SOUNDS.keySet());
            return ParrotEntity.getSound((EntityType)list.get(random.nextInt(list.size())));
        }
        return SoundEvents.ENTITY_PARROT_AMBIENT;
    }

    private static SoundEvent getSound(EntityType<?> imitate) {
        return MOB_SOUNDS.getOrDefault(imitate, SoundEvents.ENTITY_PARROT_AMBIENT);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PARROT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_PARROT_STEP, 0.15f, 1.0f);
    }

    @Override
    protected boolean isFlappingWings() {
        return this.speed > this.field_28640;
    }

    @Override
    protected void addFlapEffects() {
        this.playSound(SoundEvents.ENTITY_PARROT_FLY, 0.15f, 1.0f);
        this.field_28640 = this.speed + this.maxWingDeviation / 2.0f;
    }

    @Override
    public float getSoundPitch() {
        return ParrotEntity.getSoundPitch(this.random);
    }

    public static float getSoundPitch(Random random) {
        return (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void pushAway(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return;
        }
        super.pushAway(entity);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.getWorld().isClient) {
            this.setSitting(false);
        }
        return super.damage(source, amount);
    }

    @Override
    public Variant getVariant() {
        return Variant.byIndex(this.dataTracker.get(VARIANT));
    }

    @Override
    public void setVariant(Variant arg) {
        this.dataTracker.set(VARIANT, arg.id);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getVariant().id);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setVariant(Variant.byIndex(nbt.getInt("Variant")));
    }

    @Override
    public boolean isInAir() {
        return !this.isOnGround();
    }

    @Override
    protected boolean method_60716() {
        return true;
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.5f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    public static enum Variant implements StringIdentifiable
    {
        RED_BLUE(0, "red_blue"),
        BLUE(1, "blue"),
        GREEN(2, "green"),
        YELLOW_BLUE(3, "yellow_blue"),
        GRAY(4, "gray");

        public static final Codec<Variant> CODEC;
        private static final IntFunction<Variant> BY_ID;
        final int id;
        private final String name;

        private Variant(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return this.id;
        }

        public static Variant byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Variant::values);
            BY_ID = ValueLists.createIdToValueFunction(Variant::getId, Variant.values(), ValueLists.OutOfBoundsHandling.CLAMP);
        }
    }

    static class FlyOntoTreeGoal
    extends FlyGoal {
        public FlyOntoTreeGoal(PathAwareEntity arg, double d) {
            super(arg, d);
        }

        @Override
        @Nullable
        protected Vec3d getWanderTarget() {
            Vec3d lv = null;
            if (this.mob.isTouchingWater()) {
                lv = FuzzyTargeting.find(this.mob, 15, 15);
            }
            if (this.mob.getRandom().nextFloat() >= this.probability) {
                lv = this.locateTree();
            }
            return lv == null ? super.getWanderTarget() : lv;
        }

        @Nullable
        private Vec3d locateTree() {
            BlockPos lv = this.mob.getBlockPos();
            BlockPos.Mutable lv2 = new BlockPos.Mutable();
            BlockPos.Mutable lv3 = new BlockPos.Mutable();
            Iterable<BlockPos> iterable = BlockPos.iterate(MathHelper.floor(this.mob.getX() - 3.0), MathHelper.floor(this.mob.getY() - 6.0), MathHelper.floor(this.mob.getZ() - 3.0), MathHelper.floor(this.mob.getX() + 3.0), MathHelper.floor(this.mob.getY() + 6.0), MathHelper.floor(this.mob.getZ() + 3.0));
            for (BlockPos lv4 : iterable) {
                BlockState lv5;
                boolean bl;
                if (lv.equals(lv4) || !(bl = (lv5 = this.mob.getWorld().getBlockState(lv3.set((Vec3i)lv4, Direction.DOWN))).getBlock() instanceof LeavesBlock || lv5.isIn(BlockTags.LOGS)) || !this.mob.getWorld().isAir(lv4) || !this.mob.getWorld().isAir(lv2.set((Vec3i)lv4, Direction.UP))) continue;
                return Vec3d.ofBottomCenter(lv4);
            }
            return null;
        }
    }
}

