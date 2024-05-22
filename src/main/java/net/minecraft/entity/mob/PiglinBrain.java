/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.task.AdmireItemTask;
import net.minecraft.entity.ai.brain.task.AdmireItemTimeLimitTask;
import net.minecraft.entity.ai.brain.task.AttackTask;
import net.minecraft.entity.ai.brain.task.CrossbowAttackTask;
import net.minecraft.entity.ai.brain.task.DefeatTargetTask;
import net.minecraft.entity.ai.brain.task.FindEntityTask;
import net.minecraft.entity.ai.brain.task.FindInteractionTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetAngryAtTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetTask;
import net.minecraft.entity.ai.brain.task.GoToRememberedPositionTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.HuntFinishTask;
import net.minecraft.entity.ai.brain.task.HuntHoglinTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.MemoryTransferTask;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.RemoveOffHandItemTask;
import net.minecraft.entity.ai.brain.task.RidingTask;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.StartRidingTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.Tasks;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkToNearestVisibleWantedItemTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsPosTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.ai.brain.task.WantNewItemTask;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;

public class PiglinBrain {
    public static final int field_30565 = 8;
    public static final int field_30566 = 4;
    public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
    private static final int field_30567 = 16;
    private static final int field_30568 = 600;
    private static final int field_30569 = 119;
    private static final int field_30570 = 9;
    private static final int field_30571 = 200;
    private static final int field_30572 = 200;
    private static final int field_30573 = 300;
    protected static final UniformIntProvider HUNT_MEMORY_DURATION = TimeHelper.betweenSeconds(30, 120);
    private static final int AVOID_TARGET_EXPIRY = 100;
    private static final int ADMIRING_DISABLED_EXPIRY = 400;
    private static final int field_30576 = 8;
    private static final UniformIntProvider MEMORY_TRANSFER_TASK_DURATION = TimeHelper.betweenSeconds(10, 40);
    private static final UniformIntProvider RIDE_TARGET_MEMORY_DURATION = TimeHelper.betweenSeconds(10, 30);
    private static final UniformIntProvider AVOID_MEMORY_DURATION = TimeHelper.betweenSeconds(5, 20);
    private static final int field_30577 = 20;
    private static final int field_30578 = 200;
    private static final int field_30579 = 12;
    private static final int field_30580 = 8;
    private static final int field_30581 = 14;
    private static final int field_30582 = 8;
    private static final int field_30583 = 5;
    private static final float CROSSBOW_ATTACK_FORWARD_MOVEMENT = 0.75f;
    private static final int field_30585 = 6;
    private static final UniformIntProvider GO_TO_ZOMBIFIED_MEMORY_DURATION = TimeHelper.betweenSeconds(5, 7);
    private static final UniformIntProvider GO_TO_NEMESIS_MEMORY_DURATION = TimeHelper.betweenSeconds(5, 7);
    private static final float field_30557 = 0.1f;
    private static final float field_30558 = 1.0f;
    private static final float field_30559 = 1.0f;
    private static final float START_RIDING_SPEED = 0.8f;
    private static final float field_30561 = 1.0f;
    private static final float field_30562 = 1.0f;
    private static final float field_30563 = 0.6f;
    private static final float field_30564 = 0.6f;

    protected static Brain<?> create(PiglinEntity piglin, Brain<PiglinEntity> brain) {
        PiglinBrain.addCoreActivities(brain);
        PiglinBrain.addIdleActivities(brain);
        PiglinBrain.addAdmireItemActivities(brain);
        PiglinBrain.addFightActivities(piglin, brain);
        PiglinBrain.addCelebrateActivities(brain);
        PiglinBrain.addAvoidActivities(brain);
        PiglinBrain.addRideActivities(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    protected static void setHuntedRecently(PiglinEntity piglin, Random random) {
        int i = HUNT_MEMORY_DURATION.get(random);
        piglin.getBrain().remember(MemoryModuleType.HUNTED_RECENTLY, true, i);
    }

    private static void addCoreActivities(Brain<PiglinEntity> piglin) {
        piglin.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask(), OpenDoorsTask.create(), PiglinBrain.goToNemesisTask(), PiglinBrain.makeFleeFromZombifiedPiglinTask(), RemoveOffHandItemTask.create(), AdmireItemTask.create(119), DefeatTargetTask.create(300, PiglinBrain::isHuntingTarget), ForgetAngryAtTargetTask.create()));
    }

    private static void addIdleActivities(Brain<PiglinEntity> piglin) {
        piglin.setTaskList(Activity.IDLE, 10, ImmutableList.of(LookAtMobTask.create(PiglinBrain::isGoldHoldingPlayer, 14.0f), UpdateAttackTargetTask.create(AbstractPiglinEntity::isAdult, PiglinBrain::getPreferredTarget), TaskTriggerer.runIf(PiglinEntity::canHunt, HuntHoglinTask.create()), PiglinBrain.makeGoToSoulFireTask(), PiglinBrain.makeRememberRideableHoglinTask(), PiglinBrain.makeRandomFollowTask(), PiglinBrain.makeRandomWanderTask(), FindInteractionTargetTask.create(EntityType.PLAYER, 4)));
    }

    private static void addFightActivities(PiglinEntity piglin, Brain<PiglinEntity> brain) {
        brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(ForgetAttackTargetTask.create(target -> !PiglinBrain.isPreferredAttackTarget(piglin, target)), TaskTriggerer.runIf(PiglinBrain::isHoldingCrossbow, AttackTask.create(5, 0.75f)), RangedApproachTask.create(1.0f), MeleeAttackTask.create(20), new CrossbowAttackTask(), HuntFinishTask.create(), ForgetTask.create(PiglinBrain::getNearestZombifiedPiglin, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void addCelebrateActivities(Brain<PiglinEntity> brain) {
        brain.setTaskList(Activity.CELEBRATE, 10, ImmutableList.of(PiglinBrain.makeGoToSoulFireTask(), LookAtMobTask.create(PiglinBrain::isGoldHoldingPlayer, 14.0f), UpdateAttackTargetTask.create(AbstractPiglinEntity::isAdult, PiglinBrain::getPreferredTarget), TaskTriggerer.runIf(piglin -> !piglin.isDancing(), WalkTowardsPosTask.create(MemoryModuleType.CELEBRATE_LOCATION, 2, 1.0f)), TaskTriggerer.runIf(PiglinEntity::isDancing, WalkTowardsPosTask.create(MemoryModuleType.CELEBRATE_LOCATION, 4, 0.6f)), new RandomTask(ImmutableList.of(Pair.of(LookAtMobTask.create(EntityType.PIGLIN, 8.0f), 1), Pair.of(StrollTask.create(0.6f, 2, 1), 1), Pair.of(new WaitTask(10, 20), 1)))), MemoryModuleType.CELEBRATE_LOCATION);
    }

    private static void addAdmireItemActivities(Brain<PiglinEntity> brain) {
        brain.setTaskList(Activity.ADMIRE_ITEM, 10, ImmutableList.of(WalkToNearestVisibleWantedItemTask.create(PiglinBrain::doesNotHaveGoldInOffHand, 1.0f, true, 9), WantNewItemTask.create(9), AdmireItemTimeLimitTask.create(200, 200)), MemoryModuleType.ADMIRING_ITEM);
    }

    private static void addAvoidActivities(Brain<PiglinEntity> brain) {
        brain.setTaskList(Activity.AVOID, 10, ImmutableList.of(GoToRememberedPositionTask.createEntityBased(MemoryModuleType.AVOID_TARGET, 1.0f, 12, true), PiglinBrain.makeRandomFollowTask(), PiglinBrain.makeRandomWanderTask(), ForgetTask.create(PiglinBrain::shouldRunAwayFromHoglins, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    }

    private static void addRideActivities(Brain<PiglinEntity> brain) {
        brain.setTaskList(Activity.RIDE, 10, ImmutableList.of(StartRidingTask.create(0.8f), LookAtMobTask.create(PiglinBrain::isGoldHoldingPlayer, 8.0f), TaskTriggerer.runIf(TaskTriggerer.predicate(Entity::hasVehicle), Tasks.pickRandomly(((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builder().addAll(PiglinBrain.makeFollowTasks())).add(Pair.of(TaskTriggerer.predicate(arg -> true), 1))).build())), RidingTask.create(8, PiglinBrain::canRide)), MemoryModuleType.RIDE_TARGET);
    }

    private static ImmutableList<Pair<SingleTickTask<LivingEntity>, Integer>> makeFollowTasks() {
        return ImmutableList.of(Pair.of(LookAtMobTask.create(EntityType.PLAYER, 8.0f), 1), Pair.of(LookAtMobTask.create(EntityType.PIGLIN, 8.0f), 1), Pair.of(LookAtMobTask.create(8.0f), 1));
    }

    private static RandomTask<LivingEntity> makeRandomFollowTask() {
        return new RandomTask<LivingEntity>((List<Pair<Task<LivingEntity>, Integer>>)((Object)((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builder().addAll(PiglinBrain.makeFollowTasks())).add(Pair.of(new WaitTask(30, 60), 1))).build()));
    }

    private static RandomTask<PiglinEntity> makeRandomWanderTask() {
        return new RandomTask<PiglinEntity>(ImmutableList.of(Pair.of(StrollTask.create(0.6f), 2), Pair.of(FindEntityTask.create(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), 2), Pair.of(TaskTriggerer.runIf(PiglinBrain::canWander, GoTowardsLookTargetTask.create(0.6f, 3)), 2), Pair.of(new WaitTask(30, 60), 1)));
    }

    private static Task<PathAwareEntity> makeGoToSoulFireTask() {
        return GoToRememberedPositionTask.createPosBased(MemoryModuleType.NEAREST_REPELLENT, 1.0f, 8, false);
    }

    private static Task<PiglinEntity> goToNemesisTask() {
        return MemoryTransferTask.create(PiglinEntity::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, GO_TO_NEMESIS_MEMORY_DURATION);
    }

    private static Task<PiglinEntity> makeFleeFromZombifiedPiglinTask() {
        return MemoryTransferTask.create(PiglinBrain::getNearestZombifiedPiglin, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, GO_TO_ZOMBIFIED_MEMORY_DURATION);
    }

    protected static void tickActivities(PiglinEntity piglin) {
        Brain<PiglinEntity> lv = piglin.getBrain();
        Activity lv2 = lv.getFirstPossibleNonCoreActivity().orElse(null);
        lv.resetPossibleActivities(ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
        Activity lv3 = lv.getFirstPossibleNonCoreActivity().orElse(null);
        if (lv2 != lv3) {
            PiglinBrain.getCurrentActivitySound(piglin).ifPresent(piglin::playSound);
        }
        piglin.setAttacking(lv.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
        if (!lv.hasMemoryModule(MemoryModuleType.RIDE_TARGET) && PiglinBrain.canRideHoglin(piglin)) {
            piglin.stopRiding();
        }
        if (!lv.hasMemoryModule(MemoryModuleType.CELEBRATE_LOCATION)) {
            lv.forget(MemoryModuleType.DANCING);
        }
        piglin.setDancing(lv.hasMemoryModule(MemoryModuleType.DANCING));
    }

    private static boolean canRideHoglin(PiglinEntity piglin) {
        if (!piglin.isBaby()) {
            return false;
        }
        Entity lv = piglin.getVehicle();
        return lv instanceof PiglinEntity && ((PiglinEntity)lv).isBaby() || lv instanceof HoglinEntity && ((HoglinEntity)lv).isBaby();
    }

    protected static void loot(PiglinEntity piglin, ItemEntity drop) {
        boolean bl;
        ItemStack lv;
        PiglinBrain.stopWalking(piglin);
        if (drop.getStack().isOf(Items.GOLD_NUGGET)) {
            piglin.sendPickup(drop, drop.getStack().getCount());
            lv = drop.getStack();
            drop.discard();
        } else {
            piglin.sendPickup(drop, 1);
            lv = PiglinBrain.getItemFromStack(drop);
        }
        if (PiglinBrain.isGoldenItem(lv)) {
            piglin.getBrain().forget(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            PiglinBrain.swapItemWithOffHand(piglin, lv);
            PiglinBrain.setAdmiringItem(piglin);
            return;
        }
        if (PiglinBrain.isFood(lv) && !PiglinBrain.hasAteRecently(piglin)) {
            PiglinBrain.setEatenRecently(piglin);
            return;
        }
        boolean bl2 = bl = !piglin.tryEquip(lv).equals(ItemStack.EMPTY);
        if (bl) {
            return;
        }
        PiglinBrain.barterItem(piglin, lv);
    }

    private static void swapItemWithOffHand(PiglinEntity piglin, ItemStack stack) {
        if (PiglinBrain.hasItemInOffHand(piglin)) {
            piglin.dropStack(piglin.getStackInHand(Hand.OFF_HAND));
        }
        piglin.equipToOffHand(stack);
    }

    private static ItemStack getItemFromStack(ItemEntity stack) {
        ItemStack lv = stack.getStack();
        ItemStack lv2 = lv.split(1);
        if (lv.isEmpty()) {
            stack.discard();
        } else {
            stack.setStack(lv);
        }
        return lv2;
    }

    protected static void consumeOffHandItem(PiglinEntity piglin, boolean barter) {
        ItemStack lv = piglin.getStackInHand(Hand.OFF_HAND);
        piglin.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        if (piglin.isAdult()) {
            boolean bl2 = PiglinBrain.acceptsForBarter(lv);
            if (barter && bl2) {
                PiglinBrain.doBarter(piglin, PiglinBrain.getBarteredItem(piglin));
            } else if (!bl2) {
                boolean bl3;
                boolean bl = bl3 = !piglin.tryEquip(lv).isEmpty();
                if (!bl3) {
                    PiglinBrain.barterItem(piglin, lv);
                }
            }
        } else {
            boolean bl2;
            boolean bl = bl2 = !piglin.tryEquip(lv).isEmpty();
            if (!bl2) {
                ItemStack lv2 = piglin.getMainHandStack();
                if (PiglinBrain.isGoldenItem(lv2)) {
                    PiglinBrain.barterItem(piglin, lv2);
                } else {
                    PiglinBrain.doBarter(piglin, Collections.singletonList(lv2));
                }
                piglin.equipToMainHand(lv);
            }
        }
    }

    protected static void pickupItemWithOffHand(PiglinEntity piglin) {
        if (PiglinBrain.isAdmiringItem(piglin) && !piglin.getOffHandStack().isEmpty()) {
            piglin.dropStack(piglin.getOffHandStack());
            piglin.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    private static void barterItem(PiglinEntity piglin, ItemStack stack) {
        ItemStack lv = piglin.addItem(stack);
        PiglinBrain.dropBarteredItem(piglin, Collections.singletonList(lv));
    }

    private static void doBarter(PiglinEntity piglin, List<ItemStack> items) {
        Optional<PlayerEntity> optional = piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (optional.isPresent()) {
            PiglinBrain.dropBarteredItem(piglin, optional.get(), items);
        } else {
            PiglinBrain.dropBarteredItem(piglin, items);
        }
    }

    private static void dropBarteredItem(PiglinEntity piglin, List<ItemStack> items) {
        PiglinBrain.drop(piglin, items, PiglinBrain.findGround(piglin));
    }

    private static void dropBarteredItem(PiglinEntity piglin, PlayerEntity player, List<ItemStack> items) {
        PiglinBrain.drop(piglin, items, player.getPos());
    }

    private static void drop(PiglinEntity piglin, List<ItemStack> items, Vec3d pos) {
        if (!items.isEmpty()) {
            piglin.swingHand(Hand.OFF_HAND);
            for (ItemStack lv : items) {
                LookTargetUtil.give(piglin, lv, pos.add(0.0, 1.0, 0.0));
            }
        }
    }

    private static List<ItemStack> getBarteredItem(PiglinEntity piglin) {
        LootTable lv = piglin.getWorld().getServer().getReloadableRegistries().getLootTable(LootTables.PIGLIN_BARTERING_GAMEPLAY);
        ObjectArrayList<ItemStack> list = lv.generateLoot(new LootContextParameterSet.Builder((ServerWorld)piglin.getWorld()).add(LootContextParameters.THIS_ENTITY, piglin).build(LootContextTypes.BARTER));
        return list;
    }

    private static boolean isHuntingTarget(LivingEntity piglin, LivingEntity target) {
        if (target.getType() != EntityType.HOGLIN) {
            return false;
        }
        return Random.create(piglin.getWorld().getTime()).nextFloat() < 0.1f;
    }

    protected static boolean canGather(PiglinEntity piglin, ItemStack stack) {
        if (piglin.isBaby() && stack.isIn(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
            return false;
        }
        if (stack.isIn(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        }
        if (PiglinBrain.hasBeenHitByPlayer(piglin) && piglin.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        if (PiglinBrain.acceptsForBarter(stack)) {
            return PiglinBrain.doesNotHaveGoldInOffHand(piglin);
        }
        boolean bl = piglin.canInsertIntoInventory(stack);
        if (stack.isOf(Items.GOLD_NUGGET)) {
            return bl;
        }
        if (PiglinBrain.isFood(stack)) {
            return !PiglinBrain.hasAteRecently(piglin) && bl;
        }
        if (PiglinBrain.isGoldenItem(stack)) {
            return PiglinBrain.doesNotHaveGoldInOffHand(piglin) && bl;
        }
        return piglin.canEquipStack(stack);
    }

    protected static boolean isGoldenItem(ItemStack stack) {
        return stack.isIn(ItemTags.PIGLIN_LOVED);
    }

    private static boolean canRide(PiglinEntity piglin, Entity ridden) {
        if (ridden instanceof MobEntity) {
            MobEntity lv = (MobEntity)ridden;
            return !lv.isBaby() || !lv.isAlive() || PiglinBrain.hasBeenHurt(piglin) || PiglinBrain.hasBeenHurt(lv) || lv instanceof PiglinEntity && lv.getVehicle() == null;
        }
        return false;
    }

    private static boolean isPreferredAttackTarget(PiglinEntity piglin, LivingEntity target) {
        return PiglinBrain.getPreferredTarget(piglin).filter(preferredTarget -> preferredTarget == target).isPresent();
    }

    private static boolean getNearestZombifiedPiglin(PiglinEntity piglin) {
        Brain<PiglinEntity> lv = piglin.getBrain();
        if (lv.hasMemoryModule(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            LivingEntity lv2 = lv.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
            return piglin.isInRange(lv2, 6.0);
        }
        return false;
    }

    private static Optional<? extends LivingEntity> getPreferredTarget(PiglinEntity piglin) {
        Optional<LivingEntity> optional2;
        Brain<PiglinEntity> lv = piglin.getBrain();
        if (PiglinBrain.getNearestZombifiedPiglin(piglin)) {
            return Optional.empty();
        }
        Optional<LivingEntity> optional = LookTargetUtil.getEntity(piglin, MemoryModuleType.ANGRY_AT);
        if (optional.isPresent() && Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, optional.get())) {
            return optional;
        }
        if (lv.hasMemoryModule(MemoryModuleType.UNIVERSAL_ANGER) && (optional2 = lv.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER)).isPresent()) {
            return optional2;
        }
        optional2 = lv.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
        if (optional2.isPresent()) {
            return optional2;
        }
        Optional<PlayerEntity> optional3 = lv.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
        if (optional3.isPresent() && Sensor.testAttackableTargetPredicate(piglin, optional3.get())) {
            return optional3;
        }
        return Optional.empty();
    }

    public static void onGuardedBlockInteracted(PlayerEntity player, boolean blockOpen) {
        List<PiglinEntity> list = player.getWorld().getNonSpectatingEntities(PiglinEntity.class, player.getBoundingBox().expand(16.0));
        list.stream().filter(PiglinBrain::hasIdleActivity).filter(piglin -> !blockOpen || LookTargetUtil.isVisibleInMemory(piglin, player)).forEach(piglin -> {
            if (piglin.getWorld().getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
                PiglinBrain.becomeAngryWithPlayer(piglin, player);
            } else {
                PiglinBrain.becomeAngryWith(piglin, player);
            }
        });
    }

    public static ActionResult playerInteract(PiglinEntity piglin, PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        if (PiglinBrain.isWillingToTrade(piglin, lv)) {
            ItemStack lv2 = lv.splitUnlessCreative(1, player);
            PiglinBrain.swapItemWithOffHand(piglin, lv2);
            PiglinBrain.setAdmiringItem(piglin);
            PiglinBrain.stopWalking(piglin);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    protected static boolean isWillingToTrade(PiglinEntity piglin, ItemStack nearbyItems) {
        return !PiglinBrain.hasBeenHitByPlayer(piglin) && !PiglinBrain.isAdmiringItem(piglin) && piglin.isAdult() && PiglinBrain.acceptsForBarter(nearbyItems);
    }

    protected static void onAttacked(PiglinEntity piglin, LivingEntity attacker) {
        if (attacker instanceof PiglinEntity) {
            return;
        }
        if (PiglinBrain.hasItemInOffHand(piglin)) {
            PiglinBrain.consumeOffHandItem(piglin, false);
        }
        Brain<PiglinEntity> lv = piglin.getBrain();
        lv.forget(MemoryModuleType.CELEBRATE_LOCATION);
        lv.forget(MemoryModuleType.DANCING);
        lv.forget(MemoryModuleType.ADMIRING_ITEM);
        if (attacker instanceof PlayerEntity) {
            lv.remember(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
        }
        PiglinBrain.getAvoiding(piglin).ifPresent(avoiding -> {
            if (avoiding.getType() != attacker.getType()) {
                lv.forget(MemoryModuleType.AVOID_TARGET);
            }
        });
        if (piglin.isBaby()) {
            lv.remember(MemoryModuleType.AVOID_TARGET, attacker, 100L);
            if (Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, attacker)) {
                PiglinBrain.angerAtCloserTargets(piglin, attacker);
            }
            return;
        }
        if (attacker.getType() == EntityType.HOGLIN && PiglinBrain.hasOutnumberedHoglins(piglin)) {
            PiglinBrain.runAwayFrom(piglin, attacker);
            PiglinBrain.groupRunAwayFrom(piglin, attacker);
            return;
        }
        PiglinBrain.tryRevenge(piglin, attacker);
    }

    protected static void tryRevenge(AbstractPiglinEntity piglin, LivingEntity target) {
        if (piglin.getBrain().hasActivity(Activity.AVOID)) {
            return;
        }
        if (!Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, target)) {
            return;
        }
        if (LookTargetUtil.isNewTargetTooFar(piglin, target, 4.0)) {
            return;
        }
        if (target.getType() == EntityType.PLAYER && piglin.getWorld().getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
            PiglinBrain.becomeAngryWithPlayer(piglin, target);
            PiglinBrain.angerNearbyPiglins(piglin);
        } else {
            PiglinBrain.becomeAngryWith(piglin, target);
            PiglinBrain.angerAtCloserTargets(piglin, target);
        }
    }

    public static Optional<SoundEvent> getCurrentActivitySound(PiglinEntity piglin) {
        return piglin.getBrain().getFirstPossibleNonCoreActivity().map(activity -> PiglinBrain.getSound(piglin, activity));
    }

    private static SoundEvent getSound(PiglinEntity piglin, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.ENTITY_PIGLIN_ANGRY;
        }
        if (piglin.shouldZombify()) {
            return SoundEvents.ENTITY_PIGLIN_RETREAT;
        }
        if (activity == Activity.AVOID && PiglinBrain.hasTargetToAvoid(piglin)) {
            return SoundEvents.ENTITY_PIGLIN_RETREAT;
        }
        if (activity == Activity.ADMIRE_ITEM) {
            return SoundEvents.ENTITY_PIGLIN_ADMIRING_ITEM;
        }
        if (activity == Activity.CELEBRATE) {
            return SoundEvents.ENTITY_PIGLIN_CELEBRATE;
        }
        if (PiglinBrain.hasPlayerHoldingWantedItemNearby(piglin)) {
            return SoundEvents.ENTITY_PIGLIN_JEALOUS;
        }
        if (PiglinBrain.hasSoulFireNearby(piglin)) {
            return SoundEvents.ENTITY_PIGLIN_RETREAT;
        }
        return SoundEvents.ENTITY_PIGLIN_AMBIENT;
    }

    private static boolean hasTargetToAvoid(PiglinEntity piglin) {
        Brain<PiglinEntity> lv = piglin.getBrain();
        if (!lv.hasMemoryModule(MemoryModuleType.AVOID_TARGET)) {
            return false;
        }
        return lv.getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET).get().isInRange(piglin, 12.0);
    }

    protected static List<AbstractPiglinEntity> getNearbyVisiblePiglins(PiglinEntity piglin) {
        return piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    private static List<AbstractPiglinEntity> getNearbyPiglins(AbstractPiglinEntity piglin) {
        return piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
    }

    public static boolean wearsGoldArmor(LivingEntity entity) {
        Iterable<ItemStack> iterable = entity.getAllArmorItems();
        for (ItemStack lv : iterable) {
            Item lv2 = lv.getItem();
            if (!(lv2 instanceof ArmorItem) || !((ArmorItem)lv2).getMaterial().matches(ArmorMaterials.GOLD)) continue;
            return true;
        }
        return false;
    }

    private static void stopWalking(PiglinEntity piglin) {
        piglin.getBrain().forget(MemoryModuleType.WALK_TARGET);
        piglin.getNavigation().stop();
    }

    private static Task<LivingEntity> makeRememberRideableHoglinTask() {
        LookAtMobWithIntervalTask.Interval lv = new LookAtMobWithIntervalTask.Interval(MEMORY_TRANSFER_TASK_DURATION);
        return MemoryTransferTask.create(entity -> entity.isBaby() && lv.shouldRun(entity.getWorld().random), MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_TARGET_MEMORY_DURATION);
    }

    protected static void angerAtCloserTargets(AbstractPiglinEntity piglin, LivingEntity target) {
        PiglinBrain.getNearbyPiglins(piglin).forEach(nearbyPiglin -> {
            if (!(target.getType() != EntityType.HOGLIN || nearbyPiglin.canHunt() && ((HoglinEntity)target).canBeHunted())) {
                return;
            }
            PiglinBrain.angerAtIfCloser(nearbyPiglin, target);
        });
    }

    protected static void angerNearbyPiglins(AbstractPiglinEntity piglin) {
        PiglinBrain.getNearbyPiglins(piglin).forEach(nearbyPiglin -> PiglinBrain.getNearestDetectedPlayer(nearbyPiglin).ifPresent(player -> PiglinBrain.becomeAngryWith(nearbyPiglin, player)));
    }

    protected static void becomeAngryWith(AbstractPiglinEntity piglin, LivingEntity target) {
        if (!Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, target)) {
            return;
        }
        piglin.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        piglin.getBrain().remember(MemoryModuleType.ANGRY_AT, target.getUuid(), 600L);
        if (target.getType() == EntityType.HOGLIN && piglin.canHunt()) {
            PiglinBrain.rememberHunting(piglin);
        }
        if (target.getType() == EntityType.PLAYER && piglin.getWorld().getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
            piglin.getBrain().remember(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
        }
    }

    private static void becomeAngryWithPlayer(AbstractPiglinEntity piglin, LivingEntity player) {
        Optional<PlayerEntity> optional = PiglinBrain.getNearestDetectedPlayer(piglin);
        if (optional.isPresent()) {
            PiglinBrain.becomeAngryWith(piglin, optional.get());
        } else {
            PiglinBrain.becomeAngryWith(piglin, player);
        }
    }

    private static void angerAtIfCloser(AbstractPiglinEntity piglin, LivingEntity target) {
        Optional<LivingEntity> optional = PiglinBrain.getAngryAt(piglin);
        LivingEntity lv = LookTargetUtil.getCloserEntity((LivingEntity)piglin, optional, target);
        if (optional.isPresent() && optional.get() == lv) {
            return;
        }
        PiglinBrain.becomeAngryWith(piglin, lv);
    }

    private static Optional<LivingEntity> getAngryAt(AbstractPiglinEntity piglin) {
        return LookTargetUtil.getEntity(piglin, MemoryModuleType.ANGRY_AT);
    }

    public static Optional<LivingEntity> getAvoiding(PiglinEntity piglin) {
        if (piglin.getBrain().hasMemoryModule(MemoryModuleType.AVOID_TARGET)) {
            return piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET);
        }
        return Optional.empty();
    }

    public static Optional<PlayerEntity> getNearestDetectedPlayer(AbstractPiglinEntity piglin) {
        if (piglin.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER)) {
            return piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
        }
        return Optional.empty();
    }

    private static void groupRunAwayFrom(PiglinEntity piglin, LivingEntity target) {
        PiglinBrain.getNearbyVisiblePiglins(piglin).stream().filter(nearbyVisiblePiglin -> nearbyVisiblePiglin instanceof PiglinEntity).forEach(piglinx -> PiglinBrain.runAwayFromClosestTarget((PiglinEntity)piglinx, target));
    }

    private static void runAwayFromClosestTarget(PiglinEntity piglin, LivingEntity target) {
        Brain<PiglinEntity> lv = piglin.getBrain();
        LivingEntity lv2 = target;
        lv2 = LookTargetUtil.getCloserEntity((LivingEntity)piglin, lv.getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET), lv2);
        lv2 = LookTargetUtil.getCloserEntity((LivingEntity)piglin, lv.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET), lv2);
        PiglinBrain.runAwayFrom(piglin, lv2);
    }

    private static boolean shouldRunAwayFromHoglins(PiglinEntity piglin) {
        Brain<PiglinEntity> lv = piglin.getBrain();
        if (!lv.hasMemoryModule(MemoryModuleType.AVOID_TARGET)) {
            return true;
        }
        LivingEntity lv2 = lv.getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET).get();
        EntityType<?> lv3 = lv2.getType();
        if (lv3 == EntityType.HOGLIN) {
            return PiglinBrain.hasNoAdvantageAgainstHoglins(piglin);
        }
        if (PiglinBrain.isZombified(lv3)) {
            return !lv.hasMemoryModuleWithValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, lv2);
        }
        return false;
    }

    private static boolean hasNoAdvantageAgainstHoglins(PiglinEntity piglin) {
        return !PiglinBrain.hasOutnumberedHoglins(piglin);
    }

    private static boolean hasOutnumberedHoglins(PiglinEntity piglins) {
        int i = piglins.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int j = piglins.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
        return j > i;
    }

    private static void runAwayFrom(PiglinEntity piglin, LivingEntity target) {
        piglin.getBrain().forget(MemoryModuleType.ANGRY_AT);
        piglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        piglin.getBrain().forget(MemoryModuleType.WALK_TARGET);
        piglin.getBrain().remember(MemoryModuleType.AVOID_TARGET, target, AVOID_MEMORY_DURATION.get(piglin.getWorld().random));
        PiglinBrain.rememberHunting(piglin);
    }

    protected static void rememberHunting(AbstractPiglinEntity piglin) {
        piglin.getBrain().remember(MemoryModuleType.HUNTED_RECENTLY, true, HUNT_MEMORY_DURATION.get(piglin.getWorld().random));
    }

    private static void setEatenRecently(PiglinEntity piglin) {
        piglin.getBrain().remember(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    private static Vec3d findGround(PiglinEntity piglin) {
        Vec3d lv = FuzzyTargeting.find(piglin, 4, 2);
        return lv == null ? piglin.getPos() : lv;
    }

    private static boolean hasAteRecently(PiglinEntity piglin) {
        return piglin.getBrain().hasMemoryModule(MemoryModuleType.ATE_RECENTLY);
    }

    protected static boolean hasIdleActivity(AbstractPiglinEntity piglin) {
        return piglin.getBrain().hasActivity(Activity.IDLE);
    }

    private static boolean isHoldingCrossbow(LivingEntity piglin) {
        return piglin.isHolding(Items.CROSSBOW);
    }

    private static void setAdmiringItem(LivingEntity entity) {
        entity.getBrain().remember(MemoryModuleType.ADMIRING_ITEM, true, 119L);
    }

    private static boolean isAdmiringItem(PiglinEntity entity) {
        return entity.getBrain().hasMemoryModule(MemoryModuleType.ADMIRING_ITEM);
    }

    private static boolean acceptsForBarter(ItemStack stack) {
        return stack.isOf(BARTERING_ITEM);
    }

    private static boolean isFood(ItemStack stack) {
        return stack.isIn(ItemTags.PIGLIN_FOOD);
    }

    private static boolean hasSoulFireNearby(PiglinEntity piglin) {
        return piglin.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean hasPlayerHoldingWantedItemNearby(LivingEntity entity) {
        return entity.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean canWander(LivingEntity piglin) {
        return !PiglinBrain.hasPlayerHoldingWantedItemNearby(piglin);
    }

    public static boolean isGoldHoldingPlayer(LivingEntity target) {
        return target.getType() == EntityType.PLAYER && target.isHolding(PiglinBrain::isGoldenItem);
    }

    private static boolean hasBeenHitByPlayer(PiglinEntity piglin) {
        return piglin.getBrain().hasMemoryModule(MemoryModuleType.ADMIRING_DISABLED);
    }

    private static boolean hasBeenHurt(LivingEntity piglin) {
        return piglin.getBrain().hasMemoryModule(MemoryModuleType.HURT_BY);
    }

    private static boolean hasItemInOffHand(PiglinEntity piglin) {
        return !piglin.getOffHandStack().isEmpty();
    }

    private static boolean doesNotHaveGoldInOffHand(PiglinEntity piglin) {
        return piglin.getOffHandStack().isEmpty() || !PiglinBrain.isGoldenItem(piglin.getOffHandStack());
    }

    public static boolean isZombified(EntityType<?> entityType) {
        return entityType == EntityType.ZOMBIFIED_PIGLIN || entityType == EntityType.ZOGLIN;
    }
}

