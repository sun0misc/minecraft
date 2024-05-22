/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.GameEventTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AllayEntity
extends PathAwareEntity
implements InventoryOwner,
Vibrations {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Vec3i ITEM_PICKUP_RANGE_EXPANDER = new Vec3i(1, 1, 1);
    private static final int field_39461 = 5;
    private static final float field_39462 = 55.0f;
    private static final float field_39463 = 15.0f;
    private static final Ingredient DUPLICATION_INGREDIENT = Ingredient.ofItems(Items.AMETHYST_SHARD);
    private static final int DUPLICATION_COOLDOWN = 6000;
    private static final int field_39679 = 3;
    private static final TrackedData<Boolean> DANCING = DataTracker.registerData(AllayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CAN_DUPLICATE = DataTracker.registerData(AllayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final ImmutableList<SensorType<? extends Sensor<? super AllayEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.LIKED_PLAYER, MemoryModuleType.LIKED_NOTEBLOCK, MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.IS_PANICKING, new MemoryModuleType[0]);
    public static final ImmutableList<Float> THROW_SOUND_PITCHES = ImmutableList.of(Float.valueOf(0.5625f), Float.valueOf(0.625f), Float.valueOf(0.75f), Float.valueOf(0.9375f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.125f), Float.valueOf(1.25f), Float.valueOf(1.5f), Float.valueOf(1.875f), Float.valueOf(2.0f), Float.valueOf(2.25f), new Float[]{Float.valueOf(2.5f), Float.valueOf(3.0f), Float.valueOf(3.75f), Float.valueOf(4.0f)});
    private final EntityGameEventHandler<Vibrations.VibrationListener> gameEventHandler;
    private Vibrations.ListenerData vibrationListenerData;
    private final Vibrations.Callback vibrationCallback;
    private final EntityGameEventHandler<JukeboxEventListener> jukeboxEventHandler;
    private final SimpleInventory inventory = new SimpleInventory(1);
    @Nullable
    private BlockPos jukeboxPos;
    private long duplicationCooldown;
    private float field_38935;
    private float field_38936;
    private float danceTicks;
    private float field_39473;
    private float field_39474;

    public AllayEntity(EntityType<? extends AllayEntity> arg, World arg2) {
        super((EntityType<? extends PathAwareEntity>)arg, arg2);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.setCanPickUpLoot(this.canPickUpLoot());
        this.vibrationCallback = new VibrationCallback();
        this.vibrationListenerData = new Vibrations.ListenerData();
        this.gameEventHandler = new EntityGameEventHandler<Vibrations.VibrationListener>(new Vibrations.VibrationListener(this));
        this.jukeboxEventHandler = new EntityGameEventHandler<JukeboxEventListener>(new JukeboxEventListener(this.vibrationCallback.getPositionSource(), GameEvent.JUKEBOX_PLAY.value().notificationRadius()));
    }

    protected Brain.Profile<AllayEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return AllayBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<AllayEntity> getBrain() {
        return super.getBrain();
    }

    public static DefaultAttributeContainer.Builder createAllayAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.1f).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
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
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DANCING, false);
        builder.add(CAN_DUPLICATE, true);
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement()) {
            if (this.isTouchingWater()) {
                this.updateVelocity(0.02f, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.8f));
            } else if (this.isInLava()) {
                this.updateVelocity(0.02f, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.5));
            } else {
                this.updateVelocity(this.getMovementSpeed(), movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.91f));
            }
        }
        this.updateLimbs(false);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity entity = source.getAttacker();
        if (entity instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)entity;
            Optional<UUID> optional = this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
            if (optional.isPresent() && lv.getUuid().equals(optional.get())) {
                return false;
            }
        }
        return super.damage(source, amount);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasStackEquipped(EquipmentSlot.MAINHAND) ? SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM : SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ALLAY_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    protected void mobTick() {
        this.getWorld().getProfiler().push("allayBrain");
        this.getBrain().tick((ServerWorld)this.getWorld(), this);
        this.getWorld().getProfiler().pop();
        this.getWorld().getProfiler().push("allayActivityUpdate");
        AllayBrain.updateActivities(this);
        this.getWorld().getProfiler().pop();
        super.mobTick();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient && this.isAlive() && this.age % 10 == 0) {
            this.heal(1.0f);
        }
        if (this.isDancing() && this.shouldStopDancing() && this.age % 20 == 0) {
            this.setDancing(false);
            this.jukeboxPos = null;
        }
        this.tickDuplicationCooldown();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            this.field_38936 = this.field_38935;
            this.field_38935 = this.isHoldingItem() ? MathHelper.clamp(this.field_38935 + 1.0f, 0.0f, 5.0f) : MathHelper.clamp(this.field_38935 - 1.0f, 0.0f, 5.0f);
            if (this.isDancing()) {
                this.danceTicks += 1.0f;
                this.field_39474 = this.field_39473;
                this.field_39473 = this.isSpinning() ? (this.field_39473 += 1.0f) : (this.field_39473 -= 1.0f);
                this.field_39473 = MathHelper.clamp(this.field_39473, 0.0f, 15.0f);
            } else {
                this.danceTicks = 0.0f;
                this.field_39473 = 0.0f;
                this.field_39474 = 0.0f;
            }
        } else {
            Vibrations.Ticker.tick(this.getWorld(), this.vibrationListenerData, this.vibrationCallback);
            if (this.isPanicking()) {
                this.setDancing(false);
            }
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return !this.isItemPickupCoolingDown() && this.isHoldingItem();
    }

    public boolean isHoldingItem() {
        return !this.getStackInHand(Hand.MAIN_HAND).isEmpty();
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        return false;
    }

    private boolean isItemPickupCoolingDown() {
        return this.getBrain().isMemoryInState(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleState.VALUE_PRESENT);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        ItemStack lv2 = this.getStackInHand(Hand.MAIN_HAND);
        if (this.isDancing() && this.matchesDuplicationIngredient(lv) && this.canDuplicate()) {
            this.duplicate();
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_BREEDING_PARTICLES);
            this.getWorld().playSoundFromEntity(player, this, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 2.0f, 1.0f);
            this.decrementStackUnlessInCreative(player, lv);
            return ActionResult.SUCCESS;
        }
        if (lv2.isEmpty() && !lv.isEmpty()) {
            ItemStack lv3 = lv.copyWithCount(1);
            this.setStackInHand(Hand.MAIN_HAND, lv3);
            this.decrementStackUnlessInCreative(player, lv);
            this.getWorld().playSoundFromEntity(player, this, SoundEvents.ENTITY_ALLAY_ITEM_GIVEN, SoundCategory.NEUTRAL, 2.0f, 1.0f);
            this.getBrain().remember(MemoryModuleType.LIKED_PLAYER, player.getUuid());
            return ActionResult.SUCCESS;
        }
        if (!lv2.isEmpty() && hand == Hand.MAIN_HAND && lv.isEmpty()) {
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.getWorld().playSoundFromEntity(player, this, SoundEvents.ENTITY_ALLAY_ITEM_TAKEN, SoundCategory.NEUTRAL, 2.0f, 1.0f);
            this.swingHand(Hand.MAIN_HAND);
            for (ItemStack lv4 : this.getInventory().clearToList()) {
                LookTargetUtil.give(this, lv4, this.getPos());
            }
            this.getBrain().forget(MemoryModuleType.LIKED_PLAYER);
            player.giveItemStack(lv2);
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    public void updateJukeboxPos(BlockPos jukeboxPos, boolean playing) {
        if (playing) {
            if (!this.isDancing()) {
                this.jukeboxPos = jukeboxPos;
                this.setDancing(true);
            }
        } else if (jukeboxPos.equals(this.jukeboxPos) || this.jukeboxPos == null) {
            this.jukeboxPos = null;
            this.setDancing(false);
        }
    }

    @Override
    public SimpleInventory getInventory() {
        return this.inventory;
    }

    @Override
    protected Vec3i getItemPickUpRangeExpander() {
        return ITEM_PICKUP_RANGE_EXPANDER;
    }

    @Override
    public boolean canGather(ItemStack stack) {
        ItemStack lv = this.getStackInHand(Hand.MAIN_HAND);
        return !lv.isEmpty() && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.inventory.canInsert(stack) && this.areItemsEqual(lv, stack);
    }

    private boolean areItemsEqual(ItemStack stack, ItemStack stack2) {
        return ItemStack.areItemsEqual(stack, stack2) && !this.areDifferentPotions(stack, stack2);
    }

    private boolean areDifferentPotions(ItemStack stack, ItemStack stack2) {
        PotionContentsComponent lv2;
        PotionContentsComponent lv = stack.get(DataComponentTypes.POTION_CONTENTS);
        return !Objects.equals(lv, lv2 = stack2.get(DataComponentTypes.POTION_CONTENTS));
    }

    @Override
    protected void loot(ItemEntity item) {
        InventoryOwner.pickUpItem(this, this, item);
    }

    @Override
    protected void sendAiDebugData() {
        super.sendAiDebugData();
        DebugInfoSender.sendBrainDebugData(this);
    }

    @Override
    public boolean isFlappingWings() {
        return !this.isOnGround();
    }

    @Override
    public void updateEventHandler(BiConsumer<EntityGameEventHandler<?>, ServerWorld> callback) {
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            callback.accept(this.gameEventHandler, lv);
            callback.accept(this.jukeboxEventHandler, lv);
        }
    }

    public boolean isDancing() {
        return this.dataTracker.get(DANCING);
    }

    public void setDancing(boolean dancing) {
        if (this.getWorld().isClient || !this.canMoveVoluntarily() || dancing && this.isPanicking()) {
            return;
        }
        this.dataTracker.set(DANCING, dancing);
    }

    private boolean shouldStopDancing() {
        return this.jukeboxPos == null || !this.jukeboxPos.isWithinDistance(this.getPos(), (double)GameEvent.JUKEBOX_PLAY.value().notificationRadius()) || !this.getWorld().getBlockState(this.jukeboxPos).isOf(Blocks.JUKEBOX);
    }

    public float method_43397(float f) {
        return MathHelper.lerp(f, this.field_38936, this.field_38935) / 5.0f;
    }

    public boolean isSpinning() {
        float f = this.danceTicks % 55.0f;
        return f < 15.0f;
    }

    public float method_44368(float f) {
        return MathHelper.lerp(f, this.field_39474, this.field_39473) / 15.0f;
    }

    @Override
    public boolean areItemsDifferent(ItemStack stack, ItemStack stack2) {
        return !this.areItemsEqual(stack, stack2);
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        this.inventory.clearToList().forEach(this::dropStack);
        ItemStack lv = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!lv.isEmpty() && !EnchantmentHelper.hasAnyEnchantmentsWith(lv, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
            this.dropStack(lv);
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.writeInventory(nbt, this.getRegistryManager());
        Vibrations.ListenerData.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationListenerData).resultOrPartial(LOGGER::error).ifPresent(arg2 -> nbt.put("listener", (NbtElement)arg2));
        nbt.putLong("DuplicationCooldown", this.duplicationCooldown);
        nbt.putBoolean("CanDuplicate", this.canDuplicate());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.readInventory(nbt, this.getRegistryManager());
        if (nbt.contains("listener", NbtElement.COMPOUND_TYPE)) {
            Vibrations.ListenerData.CODEC.parse(new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbt.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent(arg -> {
                this.vibrationListenerData = arg;
            });
        }
        this.duplicationCooldown = nbt.getInt("DuplicationCooldown");
        this.dataTracker.set(CAN_DUPLICATE, nbt.getBoolean("CanDuplicate"));
    }

    @Override
    protected boolean shouldFollowLeash() {
        return false;
    }

    private void tickDuplicationCooldown() {
        if (this.duplicationCooldown > 0L) {
            --this.duplicationCooldown;
        }
        if (!this.getWorld().isClient() && this.duplicationCooldown == 0L && !this.canDuplicate()) {
            this.dataTracker.set(CAN_DUPLICATE, true);
        }
    }

    private boolean matchesDuplicationIngredient(ItemStack stack) {
        return DUPLICATION_INGREDIENT.test(stack);
    }

    private void duplicate() {
        AllayEntity lv = EntityType.ALLAY.create(this.getWorld());
        if (lv != null) {
            lv.refreshPositionAfterTeleport(this.getPos());
            lv.setPersistent();
            lv.startDuplicationCooldown();
            this.startDuplicationCooldown();
            this.getWorld().spawnEntity(lv);
        }
    }

    private void startDuplicationCooldown() {
        this.duplicationCooldown = 6000L;
        this.dataTracker.set(CAN_DUPLICATE, false);
    }

    private boolean canDuplicate() {
        return this.dataTracker.get(CAN_DUPLICATE);
    }

    private void decrementStackUnlessInCreative(PlayerEntity player, ItemStack stack) {
        stack.decrementUnlessCreative(1, player);
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, (double)this.getStandingEyeHeight() * 0.6, (double)this.getWidth() * 0.1);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.ADD_BREEDING_PARTICLES) {
            for (int i = 0; i < 3; ++i) {
                this.addHeartParticle();
            }
        } else {
            super.handleStatus(status);
        }
    }

    private void addHeartParticle() {
        double d = this.random.nextGaussian() * 0.02;
        double e = this.random.nextGaussian() * 0.02;
        double f = this.random.nextGaussian() * 0.02;
        this.getWorld().addParticle(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
    }

    @Override
    public Vibrations.ListenerData getVibrationListenerData() {
        return this.vibrationListenerData;
    }

    @Override
    public Vibrations.Callback getVibrationCallback() {
        return this.vibrationCallback;
    }

    class VibrationCallback
    implements Vibrations.Callback {
        private static final int RANGE = 16;
        private final PositionSource positionSource;

        VibrationCallback() {
            this.positionSource = new EntityPositionSource(AllayEntity.this, AllayEntity.this.getStandingEyeHeight());
        }

        @Override
        public int getRange() {
            return 16;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean accepts(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter) {
            if (AllayEntity.this.isAiDisabled()) {
                return false;
            }
            Optional<GlobalPos> optional = AllayEntity.this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK);
            if (optional.isEmpty()) {
                return true;
            }
            GlobalPos lv = optional.get();
            return lv.dimension().equals(world.getRegistryKey()) && lv.pos().equals(pos);
        }

        @Override
        public void accept(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance) {
            if (event.matches(GameEvent.NOTE_BLOCK_PLAY)) {
                AllayBrain.rememberNoteBlock(AllayEntity.this, new BlockPos(pos));
            }
        }

        @Override
        public TagKey<GameEvent> getTag() {
            return GameEventTags.ALLAY_CAN_LISTEN;
        }
    }

    class JukeboxEventListener
    implements GameEventListener {
        private final PositionSource positionSource;
        private final int range;

        public JukeboxEventListener(PositionSource positionSource, int range) {
            this.positionSource = positionSource;
            this.range = range;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public int getRange() {
            return this.range;
        }

        @Override
        public boolean listen(ServerWorld world, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter, Vec3d emitterPos) {
            if (event.matches(GameEvent.JUKEBOX_PLAY)) {
                AllayEntity.this.updateJukeboxPos(BlockPos.ofFloored(emitterPos), true);
                return true;
            }
            if (event.matches(GameEvent.JUKEBOX_STOP_PLAY)) {
                AllayEntity.this.updateJukeboxPos(BlockPos.ofFloored(emitterPos), false);
                return true;
            }
            return false;
        }
    }
}

