/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.function.Predicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.LeapingChargeTask;
import net.minecraft.entity.ai.brain.task.LongJumpTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.PrepareRamTask;
import net.minecraft.entity.ai.brain.task.RamImpactTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TemptTask;
import net.minecraft.entity.ai.brain.task.TemptationCooldownTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkTowardClosestAdultTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;

public class GoatBrain {
    public static final int PREPARE_RAM_DURATION = 20;
    public static final int MAX_RAM_TARGET_DISTANCE = 7;
    private static final UniformIntProvider WALKING_SPEED = UniformIntProvider.create(5, 16);
    private static final float FOLLOWING_TARGET_WALK_SPEED = 1.0f;
    private static final float TEMPTED_WALK_SPEED = 1.25f;
    private static final float FOLLOW_ADULT_WALK_SPEED = 1.25f;
    private static final float NORMAL_WALK_SPEED = 2.0f;
    private static final float PREPARING_RAM_WALK_SPEED = 1.25f;
    private static final UniformIntProvider LONG_JUMP_COOLDOWN_RANGE = UniformIntProvider.create(600, 1200);
    public static final int LONG_JUMP_VERTICAL_RANGE = 5;
    public static final int LONG_JUMP_HORIZONTAL_RANGE = 5;
    public static final float field_49093 = 3.5714288f;
    private static final UniformIntProvider RAM_COOLDOWN_RANGE = UniformIntProvider.create(600, 6000);
    private static final UniformIntProvider SCREAMING_RAM_COOLDOWN_RANGE = UniformIntProvider.create(100, 300);
    private static final TargetPredicate RAM_TARGET_PREDICATE = TargetPredicate.createAttackable().setPredicate(entity -> !entity.getType().equals(EntityType.GOAT) && entity.getWorld().getWorldBorder().contains(entity.getBoundingBox()));
    private static final float RAM_SPEED = 3.0f;
    public static final int MIN_RAM_TARGET_DISTANCE = 4;
    public static final float ADULT_RAM_STRENGTH_MULTIPLIER = 2.5f;
    public static final float BABY_RAM_STRENGTH_MULTIPLIER = 1.0f;

    protected static void resetLongJumpCooldown(GoatEntity goat, Random random) {
        goat.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, LONG_JUMP_COOLDOWN_RANGE.get(random));
        goat.getBrain().remember(MemoryModuleType.RAM_COOLDOWN_TICKS, RAM_COOLDOWN_RANGE.get(random));
    }

    protected static Brain<?> create(Brain<GoatEntity> brain) {
        GoatBrain.addCoreActivities(brain);
        GoatBrain.addIdleActivities(brain);
        GoatBrain.addLongJumpActivities(brain);
        GoatBrain.addRamActivities(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<GoatEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new StayAboveWaterTask(0.8f), new FleeTask(2.0f), new LookAroundTask(45, 90), new WanderAroundTask(), new TemptationCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), new TemptationCooldownTask(MemoryModuleType.LONG_JUMP_COOLING_DOWN), new TemptationCooldownTask(MemoryModuleType.RAM_COOLDOWN_TICKS)));
    }

    private static void addIdleActivities(Brain<GoatEntity> brain) {
        brain.setTaskList(Activity.IDLE, ImmutableList.of(Pair.of(0, LookAtMobWithIntervalTask.follow(EntityType.PLAYER, 6.0f, UniformIntProvider.create(30, 60))), Pair.of(0, new BreedTask(EntityType.GOAT)), Pair.of(1, new TemptTask(goat -> Float.valueOf(1.25f))), Pair.of(2, WalkTowardClosestAdultTask.create(WALKING_SPEED, 1.25f)), Pair.of(3, new RandomTask(ImmutableList.of(Pair.of(StrollTask.create(1.0f), 2), Pair.of(GoTowardsLookTargetTask.create(1.0f, 3), 2), Pair.of(new WaitTask(30, 60), 1))))), ImmutableSet.of(Pair.of(MemoryModuleType.RAM_TARGET, MemoryModuleState.VALUE_ABSENT), Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleState.VALUE_ABSENT)));
    }

    private static void addLongJumpActivities(Brain<GoatEntity> brain) {
        brain.setTaskList(Activity.LONG_JUMP, ImmutableList.of(Pair.of(0, new LeapingChargeTask(LONG_JUMP_COOLDOWN_RANGE, SoundEvents.ENTITY_GOAT_STEP)), Pair.of(1, new LongJumpTask<GoatEntity>(LONG_JUMP_COOLDOWN_RANGE, 5, 5, 3.5714288f, goat -> goat.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_LONG_JUMP : SoundEvents.ENTITY_GOAT_LONG_JUMP))), ImmutableSet.of(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT), Pair.of(MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleState.VALUE_ABSENT)));
    }

    private static void addRamActivities(Brain<GoatEntity> brain) {
        brain.setTaskList(Activity.RAM, ImmutableList.of(Pair.of(0, new RamImpactTask(goat -> goat.isScreaming() ? SCREAMING_RAM_COOLDOWN_RANGE : RAM_COOLDOWN_RANGE, RAM_TARGET_PREDICATE, 3.0f, goat -> goat.isBaby() ? 1.0 : 2.5, goat -> goat.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_RAM_IMPACT : SoundEvents.ENTITY_GOAT_RAM_IMPACT, goat -> goat.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_HORN_BREAK : SoundEvents.ENTITY_GOAT_HORN_BREAK)), Pair.of(1, new PrepareRamTask<GoatEntity>(goat -> goat.isScreaming() ? SCREAMING_RAM_COOLDOWN_RANGE.getMin() : RAM_COOLDOWN_RANGE.getMin(), 4, 7, 1.25f, RAM_TARGET_PREDICATE, 20, goat -> goat.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_PREPARE_RAM : SoundEvents.ENTITY_GOAT_PREPARE_RAM))), ImmutableSet.of(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_ABSENT), Pair.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT)));
    }

    public static void updateActivities(GoatEntity goat) {
        goat.getBrain().resetPossibleActivities(ImmutableList.of(Activity.RAM, Activity.LONG_JUMP, Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptItemPredicate() {
        return stack -> stack.isIn(ItemTags.GOAT_FOOD);
    }
}

