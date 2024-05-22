/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PandaEntity
extends AnimalEntity {
    private static final TrackedData<Integer> ASK_FOR_BAMBOO_TICKS = DataTracker.registerData(PandaEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SNEEZE_PROGRESS = DataTracker.registerData(PandaEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> EATING_TICKS = DataTracker.registerData(PandaEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Byte> MAIN_GENE = DataTracker.registerData(PandaEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Byte> HIDDEN_GENE = DataTracker.registerData(PandaEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Byte> PANDA_FLAGS = DataTracker.registerData(PandaEntity.class, TrackedDataHandlerRegistry.BYTE);
    static final TargetPredicate ASK_FOR_BAMBOO_TARGET = TargetPredicate.createNonAttackable().setBaseMaxDistance(8.0);
    private static final EntityDimensions BABY_BASE_DIMENSIONS = EntityType.PANDA.getDimensions().scaled(0.5f).withAttachments(EntityAttachments.builder().add(EntityAttachmentType.PASSENGER, 0.0f, 0.40625f, 0.0f));
    private static final int SNEEZING_FLAG = 2;
    private static final int PLAYING_FLAG = 4;
    private static final int SITTING_FLAG = 8;
    private static final int LYING_ON_BACK_FLAG = 16;
    private static final int EATING_ANIMATION_INTERVAL = 5;
    public static final int MAIN_GENE_MUTATION_CHANCE = 32;
    private static final int HIDDEN_GENE_MUTATION_CHANCE = 32;
    boolean shouldGetRevenge;
    boolean shouldAttack;
    public int playingTicks;
    private Vec3d playingJump;
    private float sittingAnimationProgress;
    private float lastSittingAnimationProgress;
    private float lieOnBackAnimationProgress;
    private float lastLieOnBackAnimationProgress;
    private float rollOverAnimationProgress;
    private float lastRollOverAnimationProgress;
    LookAtEntityGoal lookAtPlayerGoal;
    static final Predicate<ItemEntity> IS_FOOD = item -> {
        ItemStack lv = item.getStack();
        return (lv.isOf(Blocks.BAMBOO.asItem()) || lv.isOf(Blocks.CAKE.asItem())) && item.isAlive() && !item.cannotPickup();
    };

    public PandaEntity(EntityType<? extends PandaEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
        this.moveControl = new PandaMoveControl(this);
        if (!this.isBaby()) {
            this.setCanPickUpLoot(true);
        }
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        EquipmentSlot lv = this.getPreferredEquipmentSlot(stack);
        if (!this.getEquippedStack(lv).isEmpty()) {
            return false;
        }
        return lv == EquipmentSlot.MAINHAND && super.canEquip(stack);
    }

    public int getAskForBambooTicks() {
        return this.dataTracker.get(ASK_FOR_BAMBOO_TICKS);
    }

    public void setAskForBambooTicks(int askForBambooTicks) {
        this.dataTracker.set(ASK_FOR_BAMBOO_TICKS, askForBambooTicks);
    }

    public boolean isSneezing() {
        return this.hasPandaFlag(SNEEZING_FLAG);
    }

    public boolean isSitting() {
        return this.hasPandaFlag(SITTING_FLAG);
    }

    public void setSitting(boolean sitting) {
        this.setPandaFlag(SITTING_FLAG, sitting);
    }

    public boolean isLyingOnBack() {
        return this.hasPandaFlag(LYING_ON_BACK_FLAG);
    }

    public void setLyingOnBack(boolean lyingOnBack) {
        this.setPandaFlag(LYING_ON_BACK_FLAG, lyingOnBack);
    }

    public boolean isEating() {
        return this.dataTracker.get(EATING_TICKS) > 0;
    }

    public void setEating(boolean eating) {
        this.dataTracker.set(EATING_TICKS, eating ? 1 : 0);
    }

    private int getEatingTicks() {
        return this.dataTracker.get(EATING_TICKS);
    }

    private void setEatingTicks(int eatingTicks) {
        this.dataTracker.set(EATING_TICKS, eatingTicks);
    }

    public void setSneezing(boolean sneezing) {
        this.setPandaFlag(SNEEZING_FLAG, sneezing);
        if (!sneezing) {
            this.setSneezeProgress(0);
        }
    }

    public int getSneezeProgress() {
        return this.dataTracker.get(SNEEZE_PROGRESS);
    }

    public void setSneezeProgress(int sneezeProgress) {
        this.dataTracker.set(SNEEZE_PROGRESS, sneezeProgress);
    }

    public Gene getMainGene() {
        return Gene.byId(this.dataTracker.get(MAIN_GENE).byteValue());
    }

    public void setMainGene(Gene gene) {
        if (gene.getId() > 6) {
            gene = Gene.createRandom(this.random);
        }
        this.dataTracker.set(MAIN_GENE, (byte)gene.getId());
    }

    public Gene getHiddenGene() {
        return Gene.byId(this.dataTracker.get(HIDDEN_GENE).byteValue());
    }

    public void setHiddenGene(Gene gene) {
        if (gene.getId() > 6) {
            gene = Gene.createRandom(this.random);
        }
        this.dataTracker.set(HIDDEN_GENE, (byte)gene.getId());
    }

    public boolean isPlaying() {
        return this.hasPandaFlag(PLAYING_FLAG);
    }

    public void setPlaying(boolean playing) {
        this.setPandaFlag(PLAYING_FLAG, playing);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ASK_FOR_BAMBOO_TICKS, 0);
        builder.add(SNEEZE_PROGRESS, 0);
        builder.add(MAIN_GENE, (byte)0);
        builder.add(HIDDEN_GENE, (byte)0);
        builder.add(PANDA_FLAGS, (byte)0);
        builder.add(EATING_TICKS, 0);
    }

    private boolean hasPandaFlag(int bitmask) {
        return (this.dataTracker.get(PANDA_FLAGS) & bitmask) != 0;
    }

    private void setPandaFlag(int mask, boolean value) {
        byte b = this.dataTracker.get(PANDA_FLAGS);
        if (value) {
            this.dataTracker.set(PANDA_FLAGS, (byte)(b | mask));
        } else {
            this.dataTracker.set(PANDA_FLAGS, (byte)(b & ~mask));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("MainGene", this.getMainGene().asString());
        nbt.putString("HiddenGene", this.getHiddenGene().asString());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setMainGene(Gene.byName(nbt.getString("MainGene")));
        this.setHiddenGene(Gene.byName(nbt.getString("HiddenGene")));
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        PandaEntity lv = EntityType.PANDA.create(world);
        if (lv != null) {
            if (entity instanceof PandaEntity) {
                PandaEntity lv2 = (PandaEntity)entity;
                lv.initGenes(this, lv2);
            }
            lv.resetAttributes();
        }
        return lv;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new PandaEscapeDangerGoal(this, 2.0));
        this.goalSelector.add(2, new PandaMateGoal(this, 1.0));
        this.goalSelector.add(3, new AttackGoal(this, (double)1.2f, true));
        this.goalSelector.add(4, new TemptGoal(this, 1.0, stack -> stack.isIn(ItemTags.PANDA_FOOD), false));
        this.goalSelector.add(6, new PandaFleeGoal<PlayerEntity>(this, PlayerEntity.class, 8.0f, 2.0, 2.0));
        this.goalSelector.add(6, new PandaFleeGoal<HostileEntity>(this, HostileEntity.class, 4.0f, 2.0, 2.0));
        this.goalSelector.add(7, new PickUpFoodGoal());
        this.goalSelector.add(8, new LieOnBackGoal(this));
        this.goalSelector.add(8, new SneezeGoal(this));
        this.lookAtPlayerGoal = new LookAtEntityGoal(this, PlayerEntity.class, 6.0f);
        this.goalSelector.add(9, this.lookAtPlayerGoal);
        this.goalSelector.add(10, new LookAroundGoal(this));
        this.goalSelector.add(12, new PlayGoal(this));
        this.goalSelector.add(13, new FollowParentGoal(this, 1.25));
        this.goalSelector.add(14, new WanderAroundFarGoal(this, 1.0));
        this.targetSelector.add(1, new PandaRevengeGoal(this, new Class[0]).setGroupRevenge(new Class[0]));
    }

    public static DefaultAttributeContainer.Builder createPandaAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0);
    }

    public Gene getProductGene() {
        return Gene.getProductGene(this.getMainGene(), this.getHiddenGene());
    }

    public boolean isLazy() {
        return this.getProductGene() == Gene.LAZY;
    }

    public boolean isWorried() {
        return this.getProductGene() == Gene.WORRIED;
    }

    public boolean isPlayful() {
        return this.getProductGene() == Gene.PLAYFUL;
    }

    public boolean isBrown() {
        return this.getProductGene() == Gene.BROWN;
    }

    public boolean isWeak() {
        return this.getProductGene() == Gene.WEAK;
    }

    @Override
    public boolean isAttacking() {
        return this.getProductGene() == Gene.AGGRESSIVE;
    }

    @Override
    public boolean canBeLeashedBy(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean tryAttack(Entity target) {
        if (!this.isAttacking()) {
            this.shouldAttack = true;
        }
        return super.tryAttack(target);
    }

    @Override
    public void playAttackSound() {
        this.playSound(SoundEvents.ENTITY_PANDA_BITE, 1.0f, 1.0f);
    }

    @Override
    public void tick() {
        LivingEntity lv;
        super.tick();
        if (this.isWorried()) {
            if (this.getWorld().isThundering() && !this.isTouchingWater()) {
                this.setSitting(true);
                this.setEating(false);
            } else if (!this.isEating()) {
                this.setSitting(false);
            }
        }
        if ((lv = this.getTarget()) == null) {
            this.shouldGetRevenge = false;
            this.shouldAttack = false;
        }
        if (this.getAskForBambooTicks() > 0) {
            if (lv != null) {
                this.lookAtEntity(lv, 90.0f, 90.0f);
            }
            if (this.getAskForBambooTicks() == 29 || this.getAskForBambooTicks() == 14) {
                this.playSound(SoundEvents.ENTITY_PANDA_CANT_BREED, 1.0f, 1.0f);
            }
            this.setAskForBambooTicks(this.getAskForBambooTicks() - 1);
        }
        if (this.isSneezing()) {
            this.setSneezeProgress(this.getSneezeProgress() + 1);
            if (this.getSneezeProgress() > 20) {
                this.setSneezing(false);
                this.sneeze();
            } else if (this.getSneezeProgress() == 1) {
                this.playSound(SoundEvents.ENTITY_PANDA_PRE_SNEEZE, 1.0f, 1.0f);
            }
        }
        if (this.isPlaying()) {
            this.updatePlaying();
        } else {
            this.playingTicks = 0;
        }
        if (this.isSitting()) {
            this.setPitch(0.0f);
        }
        this.updateSittingAnimation();
        this.updateEatingAnimation();
        this.updateLieOnBackAnimation();
        this.updateRollOverAnimation();
    }

    public boolean isScaredByThunderstorm() {
        return this.isWorried() && this.getWorld().isThundering();
    }

    private void updateEatingAnimation() {
        if (!this.isEating() && this.isSitting() && !this.isScaredByThunderstorm() && !this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty() && this.random.nextInt(80) == 1) {
            this.setEating(true);
        } else if (this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty() || !this.isSitting()) {
            this.setEating(false);
        }
        if (this.isEating()) {
            this.playEatingAnimation();
            if (!this.getWorld().isClient && this.getEatingTicks() > 80 && this.random.nextInt(20) == 1) {
                if (this.getEatingTicks() > 100 && this.canEat(this.getEquippedStack(EquipmentSlot.MAINHAND))) {
                    if (!this.getWorld().isClient) {
                        this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.emitGameEvent(GameEvent.EAT);
                    }
                    this.setSitting(false);
                }
                this.setEating(false);
                return;
            }
            this.setEatingTicks(this.getEatingTicks() + 1);
        }
    }

    private void playEatingAnimation() {
        if (this.getEatingTicks() % 5 == 0) {
            this.playSound(SoundEvents.ENTITY_PANDA_EAT, 0.5f + 0.5f * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
            for (int i = 0; i < 6; ++i) {
                Vec3d lv = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, ((double)this.random.nextFloat() - 0.5) * 0.1);
                lv = lv.rotateX(-this.getPitch() * ((float)Math.PI / 180));
                lv = lv.rotateY(-this.getYaw() * ((float)Math.PI / 180));
                double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
                Vec3d lv2 = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.8, d, 1.0 + ((double)this.random.nextFloat() - 0.5) * 0.4);
                lv2 = lv2.rotateY(-this.bodyYaw * ((float)Math.PI / 180));
                lv2 = lv2.add(this.getX(), this.getEyeY() + 1.0, this.getZ());
                this.getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, this.getEquippedStack(EquipmentSlot.MAINHAND)), lv2.x, lv2.y, lv2.z, lv.x, lv.y + 0.05, lv.z);
            }
        }
    }

    private void updateSittingAnimation() {
        this.lastSittingAnimationProgress = this.sittingAnimationProgress;
        this.sittingAnimationProgress = this.isSitting() ? Math.min(1.0f, this.sittingAnimationProgress + 0.15f) : Math.max(0.0f, this.sittingAnimationProgress - 0.19f);
    }

    private void updateLieOnBackAnimation() {
        this.lastLieOnBackAnimationProgress = this.lieOnBackAnimationProgress;
        this.lieOnBackAnimationProgress = this.isLyingOnBack() ? Math.min(1.0f, this.lieOnBackAnimationProgress + 0.15f) : Math.max(0.0f, this.lieOnBackAnimationProgress - 0.19f);
    }

    private void updateRollOverAnimation() {
        this.lastRollOverAnimationProgress = this.rollOverAnimationProgress;
        this.rollOverAnimationProgress = this.isPlaying() ? Math.min(1.0f, this.rollOverAnimationProgress + 0.15f) : Math.max(0.0f, this.rollOverAnimationProgress - 0.19f);
    }

    public float getSittingAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastSittingAnimationProgress, this.sittingAnimationProgress);
    }

    public float getLieOnBackAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastLieOnBackAnimationProgress, this.lieOnBackAnimationProgress);
    }

    public float getRollOverAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastRollOverAnimationProgress, this.rollOverAnimationProgress);
    }

    private void updatePlaying() {
        ++this.playingTicks;
        if (this.playingTicks > 32) {
            this.setPlaying(false);
            return;
        }
        if (!this.getWorld().isClient) {
            Vec3d lv = this.getVelocity();
            if (this.playingTicks == 1) {
                float f = this.getYaw() * ((float)Math.PI / 180);
                float g = this.isBaby() ? 0.1f : 0.2f;
                this.playingJump = new Vec3d(lv.x + (double)(-MathHelper.sin(f) * g), 0.0, lv.z + (double)(MathHelper.cos(f) * g));
                this.setVelocity(this.playingJump.add(0.0, 0.27, 0.0));
            } else if ((float)this.playingTicks == 7.0f || (float)this.playingTicks == 15.0f || (float)this.playingTicks == 23.0f) {
                this.setVelocity(0.0, this.isOnGround() ? 0.27 : lv.y, 0.0);
            } else {
                this.setVelocity(this.playingJump.x, lv.y, this.playingJump.z);
            }
        }
    }

    private void sneeze() {
        Vec3d lv = this.getVelocity();
        World lv2 = this.getWorld();
        lv2.addParticle(ParticleTypes.SNEEZE, this.getX() - (double)(this.getWidth() + 1.0f) * 0.5 * (double)MathHelper.sin(this.bodyYaw * ((float)Math.PI / 180)), this.getEyeY() - (double)0.1f, this.getZ() + (double)(this.getWidth() + 1.0f) * 0.5 * (double)MathHelper.cos(this.bodyYaw * ((float)Math.PI / 180)), lv.x, 0.0, lv.z);
        this.playSound(SoundEvents.ENTITY_PANDA_SNEEZE, 1.0f, 1.0f);
        List<PandaEntity> list = lv2.getNonSpectatingEntities(PandaEntity.class, this.getBoundingBox().expand(10.0));
        for (PandaEntity lv3 : list) {
            if (lv3.isBaby() || !lv3.isOnGround() || lv3.isTouchingWater() || !lv3.isIdle()) continue;
            lv3.jump();
        }
        if (!lv2.isClient() && lv2.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            ServerWorld lv4 = (ServerWorld)lv2;
            LootTable lv5 = lv4.getServer().getReloadableRegistries().getLootTable(LootTables.PANDA_SNEEZE_GAMEPLAY);
            LootContextParameterSet lv6 = new LootContextParameterSet.Builder(lv4).add(LootContextParameters.ORIGIN, this.getPos()).add(LootContextParameters.THIS_ENTITY, this).build(LootContextTypes.GIFT);
            ObjectArrayList<ItemStack> list2 = lv5.generateLoot(lv6);
            for (ItemStack lv7 : list2) {
                this.dropStack(lv7);
            }
        }
    }

    @Override
    protected void loot(ItemEntity item) {
        if (this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty() && IS_FOOD.test(item)) {
            this.triggerItemPickedUpByEntityCriteria(item);
            ItemStack lv = item.getStack();
            this.equipStack(EquipmentSlot.MAINHAND, lv);
            this.updateDropChances(EquipmentSlot.MAINHAND);
            this.sendPickup(item, lv.getCount());
            item.discard();
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.getWorld().isClient) {
            this.setSitting(false);
        }
        return super.damage(source, amount);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random lv = world.getRandom();
        this.setMainGene(Gene.createRandom(lv));
        this.setHiddenGene(Gene.createRandom(lv));
        this.resetAttributes();
        if (entityData == null) {
            entityData = new PassiveEntity.PassiveData(0.2f);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public void initGenes(PandaEntity mother, @Nullable PandaEntity father) {
        if (father == null) {
            if (this.random.nextBoolean()) {
                this.setMainGene(mother.getRandomGene());
                this.setHiddenGene(Gene.createRandom(this.random));
            } else {
                this.setMainGene(Gene.createRandom(this.random));
                this.setHiddenGene(mother.getRandomGene());
            }
        } else if (this.random.nextBoolean()) {
            this.setMainGene(mother.getRandomGene());
            this.setHiddenGene(father.getRandomGene());
        } else {
            this.setMainGene(father.getRandomGene());
            this.setHiddenGene(mother.getRandomGene());
        }
        if (this.random.nextInt(32) == 0) {
            this.setMainGene(Gene.createRandom(this.random));
        }
        if (this.random.nextInt(32) == 0) {
            this.setHiddenGene(Gene.createRandom(this.random));
        }
    }

    private Gene getRandomGene() {
        if (this.random.nextBoolean()) {
            return this.getMainGene();
        }
        return this.getHiddenGene();
    }

    public void resetAttributes() {
        if (this.isWeak()) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10.0);
        }
        if (this.isLazy()) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.07f);
        }
    }

    void stop() {
        if (!this.isTouchingWater()) {
            this.setForwardSpeed(0.0f);
            this.getNavigation().stop();
            this.setSitting(true);
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (this.isScaredByThunderstorm()) {
            return ActionResult.PASS;
        }
        if (this.isLyingOnBack()) {
            this.setLyingOnBack(false);
            return ActionResult.success(this.getWorld().isClient);
        }
        if (this.isBreedingItem(lv)) {
            if (this.getTarget() != null) {
                this.shouldGetRevenge = true;
            }
            if (this.isBaby()) {
                this.eat(player, hand, lv);
                this.growUp((int)((float)(-this.getBreedingAge() / 20) * 0.1f), true);
            } else if (!this.getWorld().isClient && this.getBreedingAge() == 0 && this.canEat()) {
                this.eat(player, hand, lv);
                this.lovePlayer(player);
            } else if (!(this.getWorld().isClient || this.isSitting() || this.isTouchingWater())) {
                this.stop();
                this.setEating(true);
                ItemStack lv2 = this.getEquippedStack(EquipmentSlot.MAINHAND);
                if (!lv2.isEmpty() && !player.isInCreativeMode()) {
                    this.dropStack(lv2);
                }
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(lv.getItem(), 1));
                this.eat(player, hand, lv);
            } else {
                return ActionResult.PASS;
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isAttacking()) {
            return SoundEvents.ENTITY_PANDA_AGGRESSIVE_AMBIENT;
        }
        if (this.isWorried()) {
            return SoundEvents.ENTITY_PANDA_WORRIED_AMBIENT;
        }
        return SoundEvents.ENTITY_PANDA_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_PANDA_STEP, 0.15f, 1.0f);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.PANDA_FOOD);
    }

    private boolean canEat(ItemStack stack) {
        return this.isBreedingItem(stack) || stack.isOf(Blocks.CAKE.asItem());
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PANDA_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PANDA_HURT;
    }

    public boolean isIdle() {
        return !this.isLyingOnBack() && !this.isScaredByThunderstorm() && !this.isEating() && !this.isPlaying() && !this.isSitting();
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? BABY_BASE_DIMENSIONS : super.getBaseDimensions(pose);
    }

    static class PandaMoveControl
    extends MoveControl {
        private final PandaEntity panda;

        public PandaMoveControl(PandaEntity panda) {
            super(panda);
            this.panda = panda;
        }

        @Override
        public void tick() {
            if (!this.panda.isIdle()) {
                return;
            }
            super.tick();
        }
    }

    public static enum Gene implements StringIdentifiable
    {
        NORMAL(0, "normal", false),
        LAZY(1, "lazy", false),
        WORRIED(2, "worried", false),
        PLAYFUL(3, "playful", false),
        BROWN(4, "brown", true),
        WEAK(5, "weak", true),
        AGGRESSIVE(6, "aggressive", false);

        public static final StringIdentifiable.EnumCodec<Gene> CODEC;
        private static final IntFunction<Gene> BY_ID;
        private static final int field_30350 = 6;
        private final int id;
        private final String name;
        private final boolean recessive;

        private Gene(int id, String name, boolean recessive) {
            this.id = id;
            this.name = name;
            this.recessive = recessive;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public boolean isRecessive() {
            return this.recessive;
        }

        static Gene getProductGene(Gene mainGene, Gene hiddenGene) {
            if (mainGene.isRecessive()) {
                if (mainGene == hiddenGene) {
                    return mainGene;
                }
                return NORMAL;
            }
            return mainGene;
        }

        public static Gene byId(int id) {
            return BY_ID.apply(id);
        }

        public static Gene byName(String name) {
            return CODEC.byId(name, NORMAL);
        }

        public static Gene createRandom(Random random) {
            int i = random.nextInt(16);
            if (i == 0) {
                return LAZY;
            }
            if (i == 1) {
                return WORRIED;
            }
            if (i == 2) {
                return PLAYFUL;
            }
            if (i == 4) {
                return AGGRESSIVE;
            }
            if (i < 9) {
                return WEAK;
            }
            if (i < 11) {
                return BROWN;
            }
            return NORMAL;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Gene::values);
            BY_ID = ValueLists.createIdToValueFunction(Gene::getId, Gene.values(), ValueLists.OutOfBoundsHandling.ZERO);
        }
    }

    static class PandaEscapeDangerGoal
    extends EscapeDangerGoal {
        private final PandaEntity panda;

        public PandaEscapeDangerGoal(PandaEntity panda, double speed) {
            super((PathAwareEntity)panda, speed, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES);
            this.panda = panda;
        }

        @Override
        public boolean shouldContinue() {
            if (this.panda.isSitting()) {
                this.panda.getNavigation().stop();
                return false;
            }
            return super.shouldContinue();
        }
    }

    static class PandaMateGoal
    extends AnimalMateGoal {
        private final PandaEntity panda;
        private int nextAskPlayerForBambooAge;

        public PandaMateGoal(PandaEntity panda, double chance) {
            super(panda, chance);
            this.panda = panda;
        }

        @Override
        public boolean canStart() {
            if (super.canStart() && this.panda.getAskForBambooTicks() == 0) {
                if (!this.isBambooClose()) {
                    if (this.nextAskPlayerForBambooAge <= this.panda.age) {
                        this.panda.setAskForBambooTicks(32);
                        this.nextAskPlayerForBambooAge = this.panda.age + 600;
                        if (this.panda.canMoveVoluntarily()) {
                            PlayerEntity lv = this.world.getClosestPlayer(ASK_FOR_BAMBOO_TARGET, this.panda);
                            this.panda.lookAtPlayerGoal.setTarget(lv);
                        }
                    }
                    return false;
                }
                return true;
            }
            return false;
        }

        private boolean isBambooClose() {
            BlockPos lv = this.panda.getBlockPos();
            BlockPos.Mutable lv2 = new BlockPos.Mutable();
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 8; ++j) {
                    int k = 0;
                    while (k <= j) {
                        int l;
                        int n = l = k < j && k > -j ? j : 0;
                        while (l <= j) {
                            lv2.set(lv, k, i, l);
                            if (this.world.getBlockState(lv2).isOf(Blocks.BAMBOO)) {
                                return true;
                            }
                            l = l > 0 ? -l : 1 - l;
                        }
                        k = k > 0 ? -k : 1 - k;
                    }
                }
            }
            return false;
        }
    }

    static class AttackGoal
    extends MeleeAttackGoal {
        private final PandaEntity panda;

        public AttackGoal(PandaEntity panda, double speed, boolean pauseWhenMobIdle) {
            super(panda, speed, pauseWhenMobIdle);
            this.panda = panda;
        }

        @Override
        public boolean canStart() {
            return this.panda.isIdle() && super.canStart();
        }
    }

    static class PandaFleeGoal<T extends LivingEntity>
    extends FleeEntityGoal<T> {
        private final PandaEntity panda;

        public PandaFleeGoal(PandaEntity panda, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
            super(panda, fleeFromType, distance, slowSpeed, fastSpeed, EntityPredicates.EXCEPT_SPECTATOR::test);
            this.panda = panda;
        }

        @Override
        public boolean canStart() {
            return this.panda.isWorried() && this.panda.isIdle() && super.canStart();
        }
    }

    class PickUpFoodGoal
    extends Goal {
        private int startAge;

        public PickUpFoodGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (this.startAge > PandaEntity.this.age || PandaEntity.this.isBaby() || PandaEntity.this.isTouchingWater() || !PandaEntity.this.isIdle() || PandaEntity.this.getAskForBambooTicks() > 0) {
                return false;
            }
            List<ItemEntity> list = PandaEntity.this.getWorld().getEntitiesByClass(ItemEntity.class, PandaEntity.this.getBoundingBox().expand(6.0, 6.0, 6.0), IS_FOOD);
            return !list.isEmpty() || !PandaEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public boolean shouldContinue() {
            if (PandaEntity.this.isTouchingWater() || !PandaEntity.this.isLazy() && PandaEntity.this.random.nextInt(PickUpFoodGoal.toGoalTicks(600)) == 1) {
                return false;
            }
            return PandaEntity.this.random.nextInt(PickUpFoodGoal.toGoalTicks(2000)) != 1;
        }

        @Override
        public void tick() {
            if (!PandaEntity.this.isSitting() && !PandaEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
                PandaEntity.this.stop();
            }
        }

        @Override
        public void start() {
            List<ItemEntity> list = PandaEntity.this.getWorld().getEntitiesByClass(ItemEntity.class, PandaEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), IS_FOOD);
            if (!list.isEmpty() && PandaEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
                PandaEntity.this.getNavigation().startMovingTo(list.get(0), 1.2f);
            } else if (!PandaEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
                PandaEntity.this.stop();
            }
            this.startAge = 0;
        }

        @Override
        public void stop() {
            ItemStack lv = PandaEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (!lv.isEmpty()) {
                PandaEntity.this.dropStack(lv);
                PandaEntity.this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                int i = PandaEntity.this.isLazy() ? PandaEntity.this.random.nextInt(50) + 10 : PandaEntity.this.random.nextInt(150) + 10;
                this.startAge = PandaEntity.this.age + i * 20;
            }
            PandaEntity.this.setSitting(false);
        }
    }

    static class LieOnBackGoal
    extends Goal {
        private final PandaEntity panda;
        private int nextLieOnBackAge;

        public LieOnBackGoal(PandaEntity panda) {
            this.panda = panda;
        }

        @Override
        public boolean canStart() {
            return this.nextLieOnBackAge < this.panda.age && this.panda.isLazy() && this.panda.isIdle() && this.panda.random.nextInt(LieOnBackGoal.toGoalTicks(400)) == 1;
        }

        @Override
        public boolean shouldContinue() {
            if (this.panda.isTouchingWater() || !this.panda.isLazy() && this.panda.random.nextInt(LieOnBackGoal.toGoalTicks(600)) == 1) {
                return false;
            }
            return this.panda.random.nextInt(LieOnBackGoal.toGoalTicks(2000)) != 1;
        }

        @Override
        public void start() {
            this.panda.setLyingOnBack(true);
            this.nextLieOnBackAge = 0;
        }

        @Override
        public void stop() {
            this.panda.setLyingOnBack(false);
            this.nextLieOnBackAge = this.panda.age + 200;
        }
    }

    static class SneezeGoal
    extends Goal {
        private final PandaEntity panda;

        public SneezeGoal(PandaEntity panda) {
            this.panda = panda;
        }

        @Override
        public boolean canStart() {
            if (!this.panda.isBaby() || !this.panda.isIdle()) {
                return false;
            }
            if (this.panda.isWeak() && this.panda.random.nextInt(SneezeGoal.toGoalTicks(500)) == 1) {
                return true;
            }
            return this.panda.random.nextInt(SneezeGoal.toGoalTicks(6000)) == 1;
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            this.panda.setSneezing(true);
        }
    }

    static class LookAtEntityGoal
    extends net.minecraft.entity.ai.goal.LookAtEntityGoal {
        private final PandaEntity panda;

        public LookAtEntityGoal(PandaEntity panda, Class<? extends LivingEntity> targetType, float range) {
            super(panda, targetType, range);
            this.panda = panda;
        }

        public void setTarget(LivingEntity target) {
            this.target = target;
        }

        @Override
        public boolean shouldContinue() {
            return this.target != null && super.shouldContinue();
        }

        @Override
        public boolean canStart() {
            if (this.mob.getRandom().nextFloat() >= this.chance) {
                return false;
            }
            if (this.target == null) {
                this.target = this.targetType == PlayerEntity.class ? this.mob.getWorld().getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()) : this.mob.getWorld().getClosestEntity(this.mob.getWorld().getEntitiesByClass(this.targetType, this.mob.getBoundingBox().expand(this.range, 3.0, this.range), arg -> true), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            }
            return this.panda.isIdle() && this.target != null;
        }

        @Override
        public void tick() {
            if (this.target != null) {
                super.tick();
            }
        }
    }

    static class PlayGoal
    extends Goal {
        private final PandaEntity panda;

        public PlayGoal(PandaEntity panda) {
            this.panda = panda;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
        }

        @Override
        public boolean canStart() {
            int j;
            if (!this.panda.isBaby() && !this.panda.isPlayful() || !this.panda.isOnGround()) {
                return false;
            }
            if (!this.panda.isIdle()) {
                return false;
            }
            float f = this.panda.getYaw() * ((float)Math.PI / 180);
            float g = -MathHelper.sin(f);
            float h = MathHelper.cos(f);
            int i = (double)Math.abs(g) > 0.5 ? MathHelper.sign(g) : 0;
            int n = j = (double)Math.abs(h) > 0.5 ? MathHelper.sign(h) : 0;
            if (this.panda.getWorld().getBlockState(this.panda.getBlockPos().add(i, -1, j)).isAir()) {
                return true;
            }
            if (this.panda.isPlayful() && this.panda.random.nextInt(PlayGoal.toGoalTicks(60)) == 1) {
                return true;
            }
            return this.panda.random.nextInt(PlayGoal.toGoalTicks(500)) == 1;
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            this.panda.setPlaying(true);
        }

        @Override
        public boolean canStop() {
            return false;
        }
    }

    static class PandaRevengeGoal
    extends RevengeGoal {
        private final PandaEntity panda;

        public PandaRevengeGoal(PandaEntity panda, Class<?> ... noRevengeTypes) {
            super(panda, noRevengeTypes);
            this.panda = panda;
        }

        @Override
        public boolean shouldContinue() {
            if (this.panda.shouldGetRevenge || this.panda.shouldAttack) {
                this.panda.setTarget(null);
                return false;
            }
            return super.shouldContinue();
        }

        @Override
        protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
            if (mob instanceof PandaEntity && mob.isAttacking()) {
                mob.setTarget(target);
            }
        }
    }
}

