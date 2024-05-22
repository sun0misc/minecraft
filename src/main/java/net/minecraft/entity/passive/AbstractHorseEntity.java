/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.AmbientStandGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.HorseBondWithPlayerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.EntityView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractHorseEntity
extends AnimalEntity
implements InventoryChangedListener,
RideableInventory,
Tameable,
JumpingMount,
Saddleable {
    public static final int field_30413 = 400;
    public static final int field_30414 = 499;
    public static final int field_30415 = 500;
    public static final double field_42647 = 0.15;
    private static final float MIN_MOVEMENT_SPEED_BONUS = (float)AbstractHorseEntity.getChildMovementSpeedBonus(() -> 0.0);
    private static final float MAX_MOVEMENT_SPEED_BONUS = (float)AbstractHorseEntity.getChildMovementSpeedBonus(() -> 1.0);
    private static final float MIN_JUMP_STRENGTH_BONUS = (float)AbstractHorseEntity.getChildJumpStrengthBonus(() -> 0.0);
    private static final float MAX_JUMP_STRENGTH_BONUS = (float)AbstractHorseEntity.getChildJumpStrengthBonus(() -> 1.0);
    private static final float MIN_HEALTH_BONUS = AbstractHorseEntity.getChildHealthBonus(max -> 0);
    private static final float MAX_HEALTH_BONUS = AbstractHorseEntity.getChildHealthBonus(max -> max - 1);
    private static final float field_42979 = 0.25f;
    private static final float field_42980 = 0.5f;
    private static final Predicate<LivingEntity> IS_BRED_HORSE = entity -> entity instanceof AbstractHorseEntity && ((AbstractHorseEntity)entity).isBred();
    private static final TargetPredicate PARENT_HORSE_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(16.0).ignoreVisibility().setPredicate(IS_BRED_HORSE);
    private static final TrackedData<Byte> HORSE_FLAGS = DataTracker.registerData(AbstractHorseEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int TAMED_FLAG = 2;
    private static final int SADDLED_FLAG = 4;
    private static final int BRED_FLAG = 8;
    private static final int EATING_GRASS_FLAG = 16;
    private static final int ANGRY_FLAG = 32;
    private static final int EATING_FLAG = 64;
    public static final int field_30416 = 0;
    public static final int field_30418 = 1;
    private int eatingGrassTicks;
    private int eatingTicks;
    private int angryTicks;
    public int tailWagTicks;
    public int field_6958;
    protected boolean inAir;
    protected SimpleInventory items;
    protected int temper;
    protected float jumpStrength;
    protected boolean jumping;
    private float eatingGrassAnimationProgress;
    private float lastEatingGrassAnimationProgress;
    private float angryAnimationProgress;
    private float lastAngryAnimationProgress;
    private float eatingAnimationProgress;
    private float lastEatingAnimationProgress;
    protected boolean playExtraHorseSounds = true;
    protected int soundTicks;
    @Nullable
    private UUID ownerUuid;
    private final Inventory inventory = new SingleStackInventory(){

        @Override
        public ItemStack getStack() {
            return AbstractHorseEntity.this.getBodyArmor();
        }

        @Override
        public void setStack(ItemStack stack) {
            AbstractHorseEntity.this.equipBodyArmor(stack);
        }

        @Override
        public void markDirty() {
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return player.getVehicle() == AbstractHorseEntity.this || player.canInteractWithEntity(AbstractHorseEntity.this, 4.0);
        }
    };

    protected AbstractHorseEntity(EntityType<? extends AbstractHorseEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
        this.onChestedStatusChanged();
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.2));
        this.goalSelector.add(1, new HorseBondWithPlayerGoal(this, 1.2));
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0, AbstractHorseEntity.class));
        this.goalSelector.add(4, new FollowParentGoal(this, 1.0));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        if (this.shouldAmbientStand()) {
            this.goalSelector.add(9, new AmbientStandGoal(this));
        }
        this.initCustomGoals();
    }

    protected void initCustomGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(3, new TemptGoal(this, 1.25, stack -> stack.isIn(ItemTags.HORSE_TEMPT_ITEMS), false));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(HORSE_FLAGS, (byte)0);
    }

    protected boolean getHorseFlag(int bitmask) {
        return (this.dataTracker.get(HORSE_FLAGS) & bitmask) != 0;
    }

    protected void setHorseFlag(int bitmask, boolean flag) {
        byte b = this.dataTracker.get(HORSE_FLAGS);
        if (flag) {
            this.dataTracker.set(HORSE_FLAGS, (byte)(b | bitmask));
        } else {
            this.dataTracker.set(HORSE_FLAGS, (byte)(b & ~bitmask));
        }
    }

    public boolean isTame() {
        return this.getHorseFlag(TAMED_FLAG);
    }

    @Override
    @Nullable
    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public void setOwnerUuid(@Nullable UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public boolean isInAir() {
        return this.inAir;
    }

    public void setTame(boolean tame) {
        this.setHorseFlag(TAMED_FLAG, tame);
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    @Override
    protected void updateForLeashLength(float leashLength) {
        if (leashLength > 6.0f && this.isEatingGrass()) {
            this.setEatingGrass(false);
        }
    }

    public boolean isEatingGrass() {
        return this.getHorseFlag(EATING_GRASS_FLAG);
    }

    public boolean isAngry() {
        return this.getHorseFlag(ANGRY_FLAG);
    }

    public boolean isBred() {
        return this.getHorseFlag(BRED_FLAG);
    }

    public void setBred(boolean bred) {
        this.setHorseFlag(BRED_FLAG, bred);
    }

    @Override
    public boolean canBeSaddled() {
        return this.isAlive() && !this.isBaby() && this.isTame();
    }

    @Override
    public void saddle(@Nullable SoundCategory sound) {
        this.items.setStack(0, new ItemStack(Items.SADDLE));
    }

    public void equipHorseArmor(PlayerEntity player, ItemStack stack) {
        if (this.isHorseArmor(stack)) {
            this.equipBodyArmor(stack.copyWithCount(1));
            stack.decrementUnlessCreative(1, player);
        }
    }

    @Override
    public boolean isSaddled() {
        return this.getHorseFlag(SADDLED_FLAG);
    }

    public int getTemper() {
        return this.temper;
    }

    public void setTemper(int temper) {
        this.temper = temper;
    }

    public int addTemper(int difference) {
        int j = MathHelper.clamp(this.getTemper() + difference, 0, this.getMaxTemper());
        this.setTemper(j);
        return j;
    }

    @Override
    public boolean isPushable() {
        return !this.hasPassengers();
    }

    private void playEatingAnimation() {
        SoundEvent lv;
        this.setEating();
        if (!this.isSilent() && (lv = this.getEatSound()) != null) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), lv, this.getSoundCategory(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        int i;
        if (fallDistance > 1.0f) {
            this.playSound(SoundEvents.ENTITY_HORSE_LAND, 0.4f, 1.0f);
        }
        if ((i = this.computeFallDamage(fallDistance, damageMultiplier)) <= 0) {
            return false;
        }
        this.damage(damageSource, i);
        if (this.hasPassengers()) {
            for (Entity lv : this.getPassengersDeep()) {
                lv.damage(damageSource, i);
            }
        }
        this.playBlockFallSound();
        return true;
    }

    protected int getInventorySize() {
        return 1;
    }

    protected void onChestedStatusChanged() {
        SimpleInventory lv = this.items;
        this.items = new SimpleInventory(this.getInventorySize());
        if (lv != null) {
            lv.removeListener(this);
            int i = Math.min(lv.size(), this.items.size());
            for (int j = 0; j < i; ++j) {
                ItemStack lv2 = lv.getStack(j);
                if (lv2.isEmpty()) continue;
                this.items.setStack(j, lv2.copy());
            }
        }
        this.items.addListener(this);
        this.updateSaddledFlag();
    }

    protected void updateSaddledFlag() {
        if (this.getWorld().isClient) {
            return;
        }
        this.setHorseFlag(SADDLED_FLAG, !this.items.getStack(0).isEmpty());
    }

    @Override
    public void onInventoryChanged(Inventory sender) {
        boolean bl = this.isSaddled();
        this.updateSaddledFlag();
        if (this.age > 20 && !bl && this.isSaddled()) {
            this.playSound(this.getSaddleSound(), 0.5f, 1.0f);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean bl = super.damage(source, amount);
        if (bl && this.random.nextInt(3) == 0) {
            this.updateAnger();
        }
        return bl;
    }

    protected boolean shouldAmbientStand() {
        return true;
    }

    @Nullable
    protected SoundEvent getEatSound() {
        return null;
    }

    @Nullable
    protected SoundEvent getAngrySound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (state.isLiquid()) {
            return;
        }
        BlockState lv = this.getWorld().getBlockState(pos.up());
        BlockSoundGroup lv2 = state.getSoundGroup();
        if (lv.isOf(Blocks.SNOW)) {
            lv2 = lv.getSoundGroup();
        }
        if (this.hasPassengers() && this.playExtraHorseSounds) {
            ++this.soundTicks;
            if (this.soundTicks > 5 && this.soundTicks % 3 == 0) {
                this.playWalkSound(lv2);
            } else if (this.soundTicks <= 5) {
                this.playSound(SoundEvents.ENTITY_HORSE_STEP_WOOD, lv2.getVolume() * 0.15f, lv2.getPitch());
            }
        } else if (this.isWooden(lv2)) {
            this.playSound(SoundEvents.ENTITY_HORSE_STEP_WOOD, lv2.getVolume() * 0.15f, lv2.getPitch());
        } else {
            this.playSound(SoundEvents.ENTITY_HORSE_STEP, lv2.getVolume() * 0.15f, lv2.getPitch());
        }
    }

    private boolean isWooden(BlockSoundGroup soundGroup) {
        return soundGroup == BlockSoundGroup.WOOD || soundGroup == BlockSoundGroup.NETHER_WOOD || soundGroup == BlockSoundGroup.NETHER_STEM || soundGroup == BlockSoundGroup.CHERRY_WOOD || soundGroup == BlockSoundGroup.BAMBOO_WOOD;
    }

    protected void playWalkSound(BlockSoundGroup group) {
        this.playSound(SoundEvents.ENTITY_HORSE_GALLOP, group.getVolume() * 0.15f, group.getPitch());
    }

    public static DefaultAttributeContainer.Builder createBaseHorseAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_JUMP_STRENGTH, 0.7).add(EntityAttributes.GENERIC_MAX_HEALTH, 53.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.225f).add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.0).add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 6.0).add(EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER, 0.5);
    }

    @Override
    public int getLimitPerChunk() {
        return 6;
    }

    public int getMaxTemper() {
        return 100;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8f;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 400;
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!this.getWorld().isClient && (!this.hasPassengers() || this.hasPassenger(player)) && this.isTame()) {
            player.openHorseInventory(this, this.items);
        }
    }

    public ActionResult interactHorse(PlayerEntity player, ItemStack stack) {
        boolean bl = this.receiveFood(player, stack);
        if (bl) {
            stack.decrementUnlessCreative(1, player);
        }
        if (this.getWorld().isClient) {
            return ActionResult.CONSUME;
        }
        return bl ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    protected boolean receiveFood(PlayerEntity player, ItemStack item) {
        boolean bl = false;
        float f = 0.0f;
        int i = 0;
        int j = 0;
        if (item.isOf(Items.WHEAT)) {
            f = 2.0f;
            i = 20;
            j = 3;
        } else if (item.isOf(Items.SUGAR)) {
            f = 1.0f;
            i = 30;
            j = 3;
        } else if (item.isOf(Blocks.HAY_BLOCK.asItem())) {
            f = 20.0f;
            i = 180;
        } else if (item.isOf(Items.APPLE)) {
            f = 3.0f;
            i = 60;
            j = 3;
        } else if (item.isOf(Items.GOLDEN_CARROT)) {
            f = 4.0f;
            i = 60;
            j = 5;
            if (!this.getWorld().isClient && this.isTame() && this.getBreedingAge() == 0 && !this.isInLove()) {
                bl = true;
                this.lovePlayer(player);
            }
        } else if (item.isOf(Items.GOLDEN_APPLE) || item.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            f = 10.0f;
            i = 240;
            j = 10;
            if (!this.getWorld().isClient && this.isTame() && this.getBreedingAge() == 0 && !this.isInLove()) {
                bl = true;
                this.lovePlayer(player);
            }
        }
        if (this.getHealth() < this.getMaxHealth() && f > 0.0f) {
            this.heal(f);
            bl = true;
        }
        if (this.isBaby() && i > 0) {
            this.getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
            if (!this.getWorld().isClient) {
                this.growUp(i);
                bl = true;
            }
        }
        if (!(j <= 0 || !bl && this.isTame() || this.getTemper() >= this.getMaxTemper() || this.getWorld().isClient)) {
            this.addTemper(j);
            bl = true;
        }
        if (bl) {
            this.playEatingAnimation();
            this.emitGameEvent(GameEvent.EAT);
        }
        return bl;
    }

    protected void putPlayerOnBack(PlayerEntity player) {
        this.setEatingGrass(false);
        this.setAngry(false);
        if (!this.getWorld().isClient) {
            player.setYaw(this.getYaw());
            player.setPitch(this.getPitch());
            player.startRiding(this);
        }
    }

    @Override
    public boolean isImmobile() {
        return super.isImmobile() && this.hasPassengers() && this.isSaddled() || this.isEatingGrass() || this.isAngry();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.HORSE_FOOD);
    }

    private void wagTail() {
        this.tailWagTicks = 1;
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        if (this.items == null) {
            return;
        }
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack lv = this.items.getStack(i);
            if (lv.isEmpty() || EnchantmentHelper.hasAnyEnchantmentsWith(lv, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) continue;
            this.dropStack(lv);
        }
    }

    @Override
    public void tickMovement() {
        if (this.random.nextInt(200) == 0) {
            this.wagTail();
        }
        super.tickMovement();
        if (this.getWorld().isClient || !this.isAlive()) {
            return;
        }
        if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0f);
        }
        if (this.eatsGrass()) {
            if (!this.isEatingGrass() && !this.hasPassengers() && this.random.nextInt(300) == 0 && this.getWorld().getBlockState(this.getBlockPos().down()).isOf(Blocks.GRASS_BLOCK)) {
                this.setEatingGrass(true);
            }
            if (this.isEatingGrass() && ++this.eatingGrassTicks > 50) {
                this.eatingGrassTicks = 0;
                this.setEatingGrass(false);
            }
        }
        this.walkToParent();
    }

    protected void walkToParent() {
        AbstractHorseEntity lv;
        if (this.isBred() && this.isBaby() && !this.isEatingGrass() && (lv = this.getWorld().getClosestEntity(AbstractHorseEntity.class, PARENT_HORSE_PREDICATE, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().expand(16.0))) != null && this.squaredDistanceTo(lv) > 4.0) {
            this.navigation.findPathTo(lv, 0);
        }
    }

    public boolean eatsGrass() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.eatingTicks > 0 && ++this.eatingTicks > 30) {
            this.eatingTicks = 0;
            this.setHorseFlag(EATING_FLAG, false);
        }
        if (this.canMoveVoluntarily() && this.angryTicks > 0 && ++this.angryTicks > 20) {
            this.angryTicks = 0;
            this.setAngry(false);
        }
        if (this.tailWagTicks > 0 && ++this.tailWagTicks > 8) {
            this.tailWagTicks = 0;
        }
        if (this.field_6958 > 0) {
            ++this.field_6958;
            if (this.field_6958 > 300) {
                this.field_6958 = 0;
            }
        }
        this.lastEatingGrassAnimationProgress = this.eatingGrassAnimationProgress;
        if (this.isEatingGrass()) {
            this.eatingGrassAnimationProgress += (1.0f - this.eatingGrassAnimationProgress) * 0.4f + 0.05f;
            if (this.eatingGrassAnimationProgress > 1.0f) {
                this.eatingGrassAnimationProgress = 1.0f;
            }
        } else {
            this.eatingGrassAnimationProgress += (0.0f - this.eatingGrassAnimationProgress) * 0.4f - 0.05f;
            if (this.eatingGrassAnimationProgress < 0.0f) {
                this.eatingGrassAnimationProgress = 0.0f;
            }
        }
        this.lastAngryAnimationProgress = this.angryAnimationProgress;
        if (this.isAngry()) {
            this.lastEatingGrassAnimationProgress = this.eatingGrassAnimationProgress = 0.0f;
            this.angryAnimationProgress += (1.0f - this.angryAnimationProgress) * 0.4f + 0.05f;
            if (this.angryAnimationProgress > 1.0f) {
                this.angryAnimationProgress = 1.0f;
            }
        } else {
            this.jumping = false;
            this.angryAnimationProgress += (0.8f * this.angryAnimationProgress * this.angryAnimationProgress * this.angryAnimationProgress - this.angryAnimationProgress) * 0.6f - 0.05f;
            if (this.angryAnimationProgress < 0.0f) {
                this.angryAnimationProgress = 0.0f;
            }
        }
        this.lastEatingAnimationProgress = this.eatingAnimationProgress;
        if (this.getHorseFlag(EATING_FLAG)) {
            this.eatingAnimationProgress += (1.0f - this.eatingAnimationProgress) * 0.7f + 0.05f;
            if (this.eatingAnimationProgress > 1.0f) {
                this.eatingAnimationProgress = 1.0f;
            }
        } else {
            this.eatingAnimationProgress += (0.0f - this.eatingAnimationProgress) * 0.7f - 0.05f;
            if (this.eatingAnimationProgress < 0.0f) {
                this.eatingAnimationProgress = 0.0f;
            }
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.hasPassengers() || this.isBaby()) {
            return super.interactMob(player, hand);
        }
        if (this.isTame() && player.shouldCancelInteraction()) {
            this.openInventory(player);
            return ActionResult.success(this.getWorld().isClient);
        }
        ItemStack lv = player.getStackInHand(hand);
        if (!lv.isEmpty()) {
            ActionResult lv2 = lv.useOnEntity(player, this, hand);
            if (lv2.isAccepted()) {
                return lv2;
            }
            if (this.canUseSlot(EquipmentSlot.BODY) && this.isHorseArmor(lv) && !this.isWearingBodyArmor()) {
                this.equipHorseArmor(player, lv);
                return ActionResult.success(this.getWorld().isClient);
            }
        }
        this.putPlayerOnBack(player);
        return ActionResult.success(this.getWorld().isClient);
    }

    private void setEating() {
        if (!this.getWorld().isClient) {
            this.eatingTicks = 1;
            this.setHorseFlag(EATING_FLAG, true);
        }
    }

    public void setEatingGrass(boolean eatingGrass) {
        this.setHorseFlag(EATING_GRASS_FLAG, eatingGrass);
    }

    public void setAngry(boolean angry) {
        if (angry) {
            this.setEatingGrass(false);
        }
        this.setHorseFlag(ANGRY_FLAG, angry);
    }

    @Nullable
    public SoundEvent getAmbientStandSound() {
        return this.getAmbientSound();
    }

    public void updateAnger() {
        if (this.shouldAmbientStand() && this.canMoveVoluntarily()) {
            this.angryTicks = 1;
            this.setAngry(true);
        }
    }

    public void playAngrySound() {
        if (!this.isAngry()) {
            this.updateAnger();
            this.playSound(this.getAngrySound());
        }
    }

    public boolean bondWithPlayer(PlayerEntity player) {
        this.setOwnerUuid(player.getUuid());
        this.setTame(true);
        if (player instanceof ServerPlayerEntity) {
            Criteria.TAME_ANIMAL.trigger((ServerPlayerEntity)player, this);
        }
        this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
        return true;
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        Vec2f lv = this.getControlledRotation(controllingPlayer);
        this.setRotation(lv.y, lv.x);
        this.bodyYaw = this.headYaw = this.getYaw();
        this.prevYaw = this.headYaw;
        if (this.isLogicalSideForUpdatingMovement()) {
            if (movementInput.z <= 0.0) {
                this.soundTicks = 0;
            }
            if (this.isOnGround()) {
                this.setInAir(false);
                if (this.jumpStrength > 0.0f && !this.isInAir()) {
                    this.jump(this.jumpStrength, movementInput);
                }
                this.jumpStrength = 0.0f;
            }
        }
    }

    protected Vec2f getControlledRotation(LivingEntity controllingPassenger) {
        return new Vec2f(controllingPassenger.getPitch() * 0.5f, controllingPassenger.getYaw());
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        if (this.isOnGround() && this.jumpStrength == 0.0f && this.isAngry() && !this.jumping) {
            return Vec3d.ZERO;
        }
        float f = controllingPlayer.sidewaysSpeed * 0.5f;
        float g = controllingPlayer.forwardSpeed;
        if (g <= 0.0f) {
            g *= 0.25f;
        }
        return new Vec3d(f, 0.0, g);
    }

    @Override
    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    }

    protected void jump(float strength, Vec3d movementInput) {
        double d = this.getJumpVelocity(strength);
        Vec3d lv = this.getVelocity();
        this.setVelocity(lv.x, d, lv.z);
        this.setInAir(true);
        this.velocityDirty = true;
        if (movementInput.z > 0.0) {
            float g = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
            float h = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
            this.setVelocity(this.getVelocity().add(-0.4f * g * strength, 0.0, 0.4f * h * strength));
        }
    }

    protected void playJumpSound() {
        this.playSound(SoundEvents.ENTITY_HORSE_JUMP, 0.4f, 1.0f);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("EatingHaystack", this.isEatingGrass());
        nbt.putBoolean("Bred", this.isBred());
        nbt.putInt("Temper", this.getTemper());
        nbt.putBoolean("Tame", this.isTame());
        if (this.getOwnerUuid() != null) {
            nbt.putUuid("Owner", this.getOwnerUuid());
        }
        if (!this.items.getStack(0).isEmpty()) {
            nbt.put("SaddleItem", this.items.getStack(0).encode(this.getRegistryManager()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        ItemStack lv;
        UUID uUID;
        super.readCustomDataFromNbt(nbt);
        this.setEatingGrass(nbt.getBoolean("EatingHaystack"));
        this.setBred(nbt.getBoolean("Bred"));
        this.setTemper(nbt.getInt("Temper"));
        this.setTame(nbt.getBoolean("Tame"));
        if (nbt.containsUuid("Owner")) {
            uUID = nbt.getUuid("Owner");
        } else {
            String string = nbt.getString("Owner");
            uUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
        }
        if (uUID != null) {
            this.setOwnerUuid(uUID);
        }
        if (nbt.contains("SaddleItem", NbtElement.COMPOUND_TYPE) && (lv = ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound("SaddleItem")).orElse(ItemStack.EMPTY)).isOf(Items.SADDLE)) {
            this.items.setStack(0, lv);
        }
        this.updateSaddledFlag();
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return false;
    }

    protected boolean canBreed() {
        return !this.hasPassengers() && !this.hasVehicle() && this.isTame() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    protected void setChildAttributes(PassiveEntity other, AbstractHorseEntity child) {
        this.setChildAttribute(other, child, EntityAttributes.GENERIC_MAX_HEALTH, MIN_HEALTH_BONUS, MAX_HEALTH_BONUS);
        this.setChildAttribute(other, child, EntityAttributes.GENERIC_JUMP_STRENGTH, MIN_JUMP_STRENGTH_BONUS, MAX_JUMP_STRENGTH_BONUS);
        this.setChildAttribute(other, child, EntityAttributes.GENERIC_MOVEMENT_SPEED, MIN_MOVEMENT_SPEED_BONUS, MAX_MOVEMENT_SPEED_BONUS);
    }

    private void setChildAttribute(PassiveEntity other, AbstractHorseEntity child, RegistryEntry<EntityAttribute> attribute, double min, double max) {
        double f = AbstractHorseEntity.calculateAttributeBaseValue(this.getAttributeBaseValue(attribute), other.getAttributeBaseValue(attribute), min, max, this.random);
        child.getAttributeInstance(attribute).setBaseValue(f);
    }

    static double calculateAttributeBaseValue(double parentBase, double otherParentBase, double min, double max, Random random) {
        double k;
        if (max <= min) {
            throw new IllegalArgumentException("Incorrect range for an attribute");
        }
        parentBase = MathHelper.clamp(parentBase, min, max);
        otherParentBase = MathHelper.clamp(otherParentBase, min, max);
        double h = 0.15 * (max - min);
        double j = (parentBase + otherParentBase) / 2.0;
        double i = Math.abs(parentBase - otherParentBase) + h * 2.0;
        double l = j + i * (k = (random.nextDouble() + random.nextDouble() + random.nextDouble()) / 3.0 - 0.5);
        if (l > max) {
            double m = l - max;
            return max - m;
        }
        if (l < min) {
            double m = min - l;
            return min + m;
        }
        return l;
    }

    public float getEatingGrassAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastEatingGrassAnimationProgress, this.eatingGrassAnimationProgress);
    }

    public float getAngryAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastAngryAnimationProgress, this.angryAnimationProgress);
    }

    public float getEatingAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastEatingAnimationProgress, this.eatingAnimationProgress);
    }

    @Override
    public void setJumpStrength(int strength) {
        if (!this.isSaddled()) {
            return;
        }
        if (strength < 0) {
            strength = 0;
        } else {
            this.jumping = true;
            this.updateAnger();
        }
        this.jumpStrength = strength >= 90 ? 1.0f : 0.4f + 0.4f * (float)strength / 90.0f;
    }

    @Override
    public boolean canJump() {
        return this.isSaddled();
    }

    @Override
    public void startJumping(int height) {
        this.jumping = true;
        this.updateAnger();
        this.playJumpSound();
    }

    @Override
    public void stopJumping() {
    }

    protected void spawnPlayerReactionParticles(boolean positive) {
        SimpleParticleType lv = positive ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.getWorld().addParticle(lv, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES) {
            this.spawnPlayerReactionParticles(true);
        } else if (status == EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES) {
            this.spawnPlayerReactionParticles(false);
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);
        if (passenger instanceof LivingEntity) {
            ((LivingEntity)passenger).bodyYaw = this.bodyYaw;
        }
    }

    protected static float getChildHealthBonus(IntUnaryOperator randomIntGetter) {
        return 15.0f + (float)randomIntGetter.applyAsInt(8) + (float)randomIntGetter.applyAsInt(9);
    }

    protected static double getChildJumpStrengthBonus(DoubleSupplier randomDoubleGetter) {
        return (double)0.4f + randomDoubleGetter.getAsDouble() * 0.2 + randomDoubleGetter.getAsDouble() * 0.2 + randomDoubleGetter.getAsDouble() * 0.2;
    }

    protected static double getChildMovementSpeedBonus(DoubleSupplier randomDoubleGetter) {
        return ((double)0.45f + randomDoubleGetter.getAsDouble() * 0.3 + randomDoubleGetter.getAsDouble() * 0.3 + randomDoubleGetter.getAsDouble() * 0.3) * 0.25;
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        int j = mappedIndex - 400;
        if (j == 0) {
            return new StackReference(){

                @Override
                public ItemStack get() {
                    return AbstractHorseEntity.this.items.getStack(0);
                }

                @Override
                public boolean set(ItemStack stack) {
                    if (stack.isEmpty() || stack.isOf(Items.SADDLE)) {
                        AbstractHorseEntity.this.items.setStack(0, stack);
                        AbstractHorseEntity.this.updateSaddledFlag();
                        return true;
                    }
                    return false;
                }
            };
        }
        int k = mappedIndex - 500 + 1;
        if (k >= 1 && k < this.items.size()) {
            return StackReference.of(this.items, k);
        }
        return super.getStackReference(mappedIndex);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity;
        if (this.isSaddled() && (entity = this.getFirstPassenger()) instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)entity;
            return lv;
        }
        return super.getControllingPassenger();
    }

    @Nullable
    private Vec3d locateSafeDismountingPos(Vec3d offset, LivingEntity passenger) {
        double d = this.getX() + offset.x;
        double e = this.getBoundingBox().minY;
        double f = this.getZ() + offset.z;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        block0: for (EntityPose lv2 : passenger.getPoses()) {
            lv.set(d, e, f);
            double g = this.getBoundingBox().maxY + 0.75;
            do {
                double h = this.getWorld().getDismountHeight(lv);
                if ((double)lv.getY() + h > g) continue block0;
                if (Dismounting.canDismountInBlock(h)) {
                    Box lv3 = passenger.getBoundingBox(lv2);
                    Vec3d lv4 = new Vec3d(d, (double)lv.getY() + h, f);
                    if (Dismounting.canPlaceEntityAt(this.getWorld(), passenger, lv3.offset(lv4))) {
                        passenger.setPose(lv2);
                        return lv4;
                    }
                }
                lv.move(Direction.UP);
            } while ((double)lv.getY() < g);
        }
        return null;
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Vec3d lv = AbstractHorseEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.RIGHT ? 90.0f : -90.0f));
        Vec3d lv2 = this.locateSafeDismountingPos(lv, passenger);
        if (lv2 != null) {
            return lv2;
        }
        Vec3d lv3 = AbstractHorseEntity.getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), this.getYaw() + (passenger.getMainArm() == Arm.LEFT ? 90.0f : -90.0f));
        Vec3d lv4 = this.locateSafeDismountingPos(lv3, passenger);
        if (lv4 != null) {
            return lv4;
        }
        return this.getPos();
    }

    protected void initAttributes(Random random) {
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        if (entityData == null) {
            entityData = new PassiveEntity.PassiveData(0.2f);
        }
        this.initAttributes(world.getRandom());
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public boolean areInventoriesDifferent(Inventory inventory) {
        return this.items != inventory;
    }

    public int getMinAmbientStandDelay() {
        return this.getMinAmbientSoundDelay();
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        return super.getPassengerAttachmentPos(passenger, dimensions, scaleFactor).add(new Vec3d(0.0, 0.15 * (double)this.lastAngryAnimationProgress * (double)scaleFactor, -0.7 * (double)this.lastAngryAnimationProgress * (double)scaleFactor).rotateY(-this.getYaw() * ((float)Math.PI / 180)));
    }

    public final Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public /* synthetic */ EntityView method_48926() {
        return super.getWorld();
    }
}

