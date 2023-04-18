package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
import net.minecraft.entity.ai.brain.task.StartRidingTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskRunnable;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.Tasks;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkToNearestVisibleWantedItemTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsPosTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.ai.brain.task.WantNewItemTask;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
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
   public static final Item BARTERING_ITEM;
   private static final int field_30567 = 16;
   private static final int field_30568 = 600;
   private static final int field_30569 = 120;
   private static final int field_30570 = 9;
   private static final int field_30571 = 200;
   private static final int field_30572 = 200;
   private static final int field_30573 = 300;
   protected static final UniformIntProvider HUNT_MEMORY_DURATION;
   private static final int field_30574 = 100;
   private static final int field_30575 = 400;
   private static final int field_30576 = 8;
   private static final UniformIntProvider MEMORY_TRANSFER_TASK_DURATION;
   private static final UniformIntProvider RIDE_TARGET_MEMORY_DURATION;
   private static final UniformIntProvider AVOID_MEMORY_DURATION;
   private static final int field_30577 = 20;
   private static final int field_30578 = 200;
   private static final int field_30579 = 12;
   private static final int field_30580 = 8;
   private static final int field_30581 = 14;
   private static final int field_30582 = 8;
   private static final int field_30583 = 5;
   private static final float field_30584 = 0.75F;
   private static final int field_30585 = 6;
   private static final UniformIntProvider GO_TO_ZOMBIFIED_MEMORY_DURATION;
   private static final UniformIntProvider GO_TO_NEMESIS_MEMORY_DURATION;
   private static final float field_30557 = 0.1F;
   private static final float field_30558 = 1.0F;
   private static final float field_30559 = 1.0F;
   private static final float field_30560 = 0.8F;
   private static final float field_30561 = 1.0F;
   private static final float field_30562 = 1.0F;
   private static final float field_30563 = 0.6F;
   private static final float field_30564 = 0.6F;

   protected static Brain create(PiglinEntity piglin, Brain brain) {
      addCoreActivities(brain);
      addIdleActivities(brain);
      addAdmireItemActivities(brain);
      addFightActivities(piglin, brain);
      addCelebrateActivities(brain);
      addAvoidActivities(brain);
      addRideActivities(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.resetPossibleActivities();
      return brain;
   }

   protected static void setHuntedRecently(PiglinEntity piglin, Random random) {
      int i = HUNT_MEMORY_DURATION.get(random);
      piglin.getBrain().remember(MemoryModuleType.HUNTED_RECENTLY, true, (long)i);
   }

   private static void addCoreActivities(Brain piglin) {
      piglin.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask(), OpenDoorsTask.create(), goToNemesisTask(), makeGoToZombifiedPiglinTask(), RemoveOffHandItemTask.create(), AdmireItemTask.create(120), DefeatTargetTask.create(300, PiglinBrain::isHuntingTarget), ForgetAngryAtTargetTask.create()));
   }

   private static void addIdleActivities(Brain piglin) {
      piglin.setTaskList(Activity.IDLE, 10, ImmutableList.of(LookAtMobTask.create(PiglinBrain::isGoldHoldingPlayer, 14.0F), UpdateAttackTargetTask.create(AbstractPiglinEntity::isAdult, PiglinBrain::getPreferredTarget), TaskTriggerer.runIf(PiglinEntity::canHunt, HuntHoglinTask.create()), makeGoToSoulFireTask(), makeRememberRideableHoglinTask(), makeRandomFollowTask(), makeRandomWanderTask(), FindInteractionTargetTask.create(EntityType.PLAYER, 4)));
   }

   private static void addFightActivities(PiglinEntity piglin, Brain brain) {
      brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(ForgetAttackTargetTask.create((target) -> {
         return !isPreferredAttackTarget(piglin, target);
      }), TaskTriggerer.runIf(PiglinBrain::isHoldingCrossbow, AttackTask.create(5, 0.75F)), RangedApproachTask.create(1.0F), MeleeAttackTask.create(20), new CrossbowAttackTask(), HuntFinishTask.create(), ForgetTask.create(PiglinBrain::getNearestZombifiedPiglin, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
   }

   private static void addCelebrateActivities(Brain brain) {
      brain.setTaskList(Activity.CELEBRATE, 10, ImmutableList.of(makeGoToSoulFireTask(), LookAtMobTask.create(PiglinBrain::isGoldHoldingPlayer, 14.0F), UpdateAttackTargetTask.create(AbstractPiglinEntity::isAdult, PiglinBrain::getPreferredTarget), TaskTriggerer.runIf((piglin) -> {
         return !piglin.isDancing();
      }, WalkTowardsPosTask.create(MemoryModuleType.CELEBRATE_LOCATION, 2, 1.0F)), TaskTriggerer.runIf(PiglinEntity::isDancing, WalkTowardsPosTask.create(MemoryModuleType.CELEBRATE_LOCATION, 4, 0.6F)), new RandomTask(ImmutableList.of(Pair.of(LookAtMobTask.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(StrollTask.create(0.6F, 2, 1), 1), Pair.of(new WaitTask(10, 20), 1)))), MemoryModuleType.CELEBRATE_LOCATION);
   }

   private static void addAdmireItemActivities(Brain brain) {
      brain.setTaskList(Activity.ADMIRE_ITEM, 10, ImmutableList.of(WalkToNearestVisibleWantedItemTask.create(PiglinBrain::doesNotHaveGoldInOffHand, 1.0F, true, 9), WantNewItemTask.create(9), AdmireItemTimeLimitTask.create(200, 200)), MemoryModuleType.ADMIRING_ITEM);
   }

   private static void addAvoidActivities(Brain brain) {
      brain.setTaskList(Activity.AVOID, 10, ImmutableList.of(GoToRememberedPositionTask.createEntityBased(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true), makeRandomFollowTask(), makeRandomWanderTask(), ForgetTask.create(PiglinBrain::shouldRunAwayFromHoglins, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
   }

   private static void addRideActivities(Brain brain) {
      brain.setTaskList(Activity.RIDE, 10, ImmutableList.of(StartRidingTask.create(0.8F), LookAtMobTask.create(PiglinBrain::isGoldHoldingPlayer, 8.0F), TaskTriggerer.runIf((TaskRunnable)TaskTriggerer.predicate(Entity::hasVehicle), (TaskRunnable)Tasks.pickRandomly(ImmutableList.builder().addAll(makeFollowTasks()).add(Pair.of(TaskTriggerer.predicate((arg) -> {
         return true;
      }), 1)).build())), RidingTask.create(8, PiglinBrain::canRide)), MemoryModuleType.RIDE_TARGET);
   }

   private static ImmutableList makeFollowTasks() {
      return ImmutableList.of(Pair.of(LookAtMobTask.create(EntityType.PLAYER, 8.0F), 1), Pair.of(LookAtMobTask.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(LookAtMobTask.create(8.0F), 1));
   }

   private static RandomTask makeRandomFollowTask() {
      return new RandomTask(ImmutableList.builder().addAll(makeFollowTasks()).add(Pair.of(new WaitTask(30, 60), 1)).build());
   }

   private static RandomTask makeRandomWanderTask() {
      return new RandomTask(ImmutableList.of(Pair.of(StrollTask.create(0.6F), 2), Pair.of(FindEntityTask.create(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(TaskTriggerer.runIf(PiglinBrain::canWander, GoTowardsLookTargetTask.create(0.6F, 3)), 2), Pair.of(new WaitTask(30, 60), 1)));
   }

   private static Task makeGoToSoulFireTask() {
      return GoToRememberedPositionTask.createPosBased(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
   }

   private static Task goToNemesisTask() {
      return MemoryTransferTask.create(PiglinEntity::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, GO_TO_NEMESIS_MEMORY_DURATION);
   }

   private static Task makeGoToZombifiedPiglinTask() {
      return MemoryTransferTask.create(PiglinBrain::getNearestZombifiedPiglin, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, GO_TO_ZOMBIFIED_MEMORY_DURATION);
   }

   protected static void tickActivities(PiglinEntity piglin) {
      Brain lv = piglin.getBrain();
      Activity lv2 = (Activity)lv.getFirstPossibleNonCoreActivity().orElse((Object)null);
      lv.resetPossibleActivities((List)ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
      Activity lv3 = (Activity)lv.getFirstPossibleNonCoreActivity().orElse((Object)null);
      if (lv2 != lv3) {
         Optional var10000 = getCurrentActivitySound(piglin);
         Objects.requireNonNull(piglin);
         var10000.ifPresent(piglin::playSound);
      }

      piglin.setAttacking(lv.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
      if (!lv.hasMemoryModule(MemoryModuleType.RIDE_TARGET) && canRideHoglin(piglin)) {
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
      } else {
         Entity lv = piglin.getVehicle();
         return lv instanceof PiglinEntity && ((PiglinEntity)lv).isBaby() || lv instanceof HoglinEntity && ((HoglinEntity)lv).isBaby();
      }
   }

   protected static void loot(PiglinEntity piglin, ItemEntity drop) {
      stopWalking(piglin);
      ItemStack lv;
      if (drop.getStack().isOf(Items.GOLD_NUGGET)) {
         piglin.sendPickup(drop, drop.getStack().getCount());
         lv = drop.getStack();
         drop.discard();
      } else {
         piglin.sendPickup(drop, 1);
         lv = getItemFromStack(drop);
      }

      if (isGoldenItem(lv)) {
         piglin.getBrain().forget(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
         swapItemWithOffHand(piglin, lv);
         setAdmiringItem(piglin);
      } else if (isFood(lv) && !hasAteRecently(piglin)) {
         setEatenRecently(piglin);
      } else {
         boolean bl = !piglin.tryEquip(lv).equals(ItemStack.EMPTY);
         if (!bl) {
            barterItem(piglin, lv);
         }
      }
   }

   private static void swapItemWithOffHand(PiglinEntity piglin, ItemStack stack) {
      if (hasItemInOffHand(piglin)) {
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
      boolean bl2;
      if (piglin.isAdult()) {
         bl2 = acceptsForBarter(lv);
         if (barter && bl2) {
            doBarter(piglin, getBarteredItem(piglin));
         } else if (!bl2) {
            boolean bl3 = !piglin.tryEquip(lv).isEmpty();
            if (!bl3) {
               barterItem(piglin, lv);
            }
         }
      } else {
         bl2 = !piglin.tryEquip(lv).isEmpty();
         if (!bl2) {
            ItemStack lv2 = piglin.getMainHandStack();
            if (isGoldenItem(lv2)) {
               barterItem(piglin, lv2);
            } else {
               doBarter(piglin, Collections.singletonList(lv2));
            }

            piglin.equipToMainHand(lv);
         }
      }

   }

   protected static void pickupItemWithOffHand(PiglinEntity piglin) {
      if (isAdmiringItem(piglin) && !piglin.getOffHandStack().isEmpty()) {
         piglin.dropStack(piglin.getOffHandStack());
         piglin.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
      }

   }

   private static void barterItem(PiglinEntity piglin, ItemStack stack) {
      ItemStack lv = piglin.addItem(stack);
      dropBarteredItem(piglin, Collections.singletonList(lv));
   }

   private static void doBarter(PiglinEntity piglin, List items) {
      Optional optional = piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
      if (optional.isPresent()) {
         dropBarteredItem(piglin, (PlayerEntity)optional.get(), items);
      } else {
         dropBarteredItem(piglin, items);
      }

   }

   private static void dropBarteredItem(PiglinEntity piglin, List items) {
      drop(piglin, items, findGround(piglin));
   }

   private static void dropBarteredItem(PiglinEntity piglin, PlayerEntity player, List items) {
      drop(piglin, items, player.getPos());
   }

   private static void drop(PiglinEntity piglin, List items, Vec3d pos) {
      if (!items.isEmpty()) {
         piglin.swingHand(Hand.OFF_HAND);
         Iterator var3 = items.iterator();

         while(var3.hasNext()) {
            ItemStack lv = (ItemStack)var3.next();
            LookTargetUtil.give(piglin, lv, pos.add(0.0, 1.0, 0.0));
         }
      }

   }

   private static List getBarteredItem(PiglinEntity piglin) {
      LootTable lv = piglin.world.getServer().getLootManager().getLootTable(LootTables.PIGLIN_BARTERING_GAMEPLAY);
      List list = lv.generateLoot((new LootContext.Builder((ServerWorld)piglin.world)).parameter(LootContextParameters.THIS_ENTITY, piglin).random(piglin.world.random).build(LootContextTypes.BARTER));
      return list;
   }

   private static boolean isHuntingTarget(LivingEntity piglin, LivingEntity target) {
      if (target.getType() != EntityType.HOGLIN) {
         return false;
      } else {
         return Random.create(piglin.world.getTime()).nextFloat() < 0.1F;
      }
   }

   protected static boolean canGather(PiglinEntity piglin, ItemStack stack) {
      if (piglin.isBaby() && stack.isIn(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
         return false;
      } else if (stack.isIn(ItemTags.PIGLIN_REPELLENTS)) {
         return false;
      } else if (hasBeenHitByPlayer(piglin) && piglin.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
         return false;
      } else if (acceptsForBarter(stack)) {
         return doesNotHaveGoldInOffHand(piglin);
      } else {
         boolean bl = piglin.canInsertIntoInventory(stack);
         if (stack.isOf(Items.GOLD_NUGGET)) {
            return bl;
         } else if (isFood(stack)) {
            return !hasAteRecently(piglin) && bl;
         } else if (!isGoldenItem(stack)) {
            return piglin.canEquipStack(stack);
         } else {
            return doesNotHaveGoldInOffHand(piglin) && bl;
         }
      }
   }

   protected static boolean isGoldenItem(ItemStack stack) {
      return stack.isIn(ItemTags.PIGLIN_LOVED);
   }

   private static boolean canRide(PiglinEntity piglin, Entity ridden) {
      if (!(ridden instanceof MobEntity lv)) {
         return false;
      } else {
         return !lv.isBaby() || !lv.isAlive() || hasBeenHurt(piglin) || hasBeenHurt(lv) || lv instanceof PiglinEntity && lv.getVehicle() == null;
      }
   }

   private static boolean isPreferredAttackTarget(PiglinEntity piglin, LivingEntity target) {
      return getPreferredTarget(piglin).filter((preferredTarget) -> {
         return preferredTarget == target;
      }).isPresent();
   }

   private static boolean getNearestZombifiedPiglin(PiglinEntity piglin) {
      Brain lv = piglin.getBrain();
      if (lv.hasMemoryModule(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
         LivingEntity lv2 = (LivingEntity)lv.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
         return piglin.isInRange(lv2, 6.0);
      } else {
         return false;
      }
   }

   private static Optional getPreferredTarget(PiglinEntity piglin) {
      Brain lv = piglin.getBrain();
      if (getNearestZombifiedPiglin(piglin)) {
         return Optional.empty();
      } else {
         Optional optional = LookTargetUtil.getEntity(piglin, MemoryModuleType.ANGRY_AT);
         if (optional.isPresent() && Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, (LivingEntity)optional.get())) {
            return optional;
         } else {
            Optional optional2;
            if (lv.hasMemoryModule(MemoryModuleType.UNIVERSAL_ANGER)) {
               optional2 = lv.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
               if (optional2.isPresent()) {
                  return optional2;
               }
            }

            optional2 = lv.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
            if (optional2.isPresent()) {
               return optional2;
            } else {
               Optional optional3 = lv.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
               return optional3.isPresent() && Sensor.testAttackableTargetPredicate(piglin, (LivingEntity)optional3.get()) ? optional3 : Optional.empty();
            }
         }
      }
   }

   public static void onGuardedBlockInteracted(PlayerEntity player, boolean blockOpen) {
      List list = player.world.getNonSpectatingEntities(PiglinEntity.class, player.getBoundingBox().expand(16.0));
      list.stream().filter(PiglinBrain::hasIdleActivity).filter((piglin) -> {
         return !blockOpen || LookTargetUtil.isVisibleInMemory(piglin, player);
      }).forEach((piglin) -> {
         if (piglin.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
            becomeAngryWithPlayer(piglin, player);
         } else {
            becomeAngryWith(piglin, player);
         }

      });
   }

   public static ActionResult playerInteract(PiglinEntity piglin, PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (isWillingToTrade(piglin, lv)) {
         ItemStack lv2 = lv.split(1);
         swapItemWithOffHand(piglin, lv2);
         setAdmiringItem(piglin);
         stopWalking(piglin);
         return ActionResult.CONSUME;
      } else {
         return ActionResult.PASS;
      }
   }

   protected static boolean isWillingToTrade(PiglinEntity piglin, ItemStack nearbyItems) {
      return !hasBeenHitByPlayer(piglin) && !isAdmiringItem(piglin) && piglin.isAdult() && acceptsForBarter(nearbyItems);
   }

   protected static void onAttacked(PiglinEntity piglin, LivingEntity attacker) {
      if (!(attacker instanceof PiglinEntity)) {
         if (hasItemInOffHand(piglin)) {
            consumeOffHandItem(piglin, false);
         }

         Brain lv = piglin.getBrain();
         lv.forget(MemoryModuleType.CELEBRATE_LOCATION);
         lv.forget(MemoryModuleType.DANCING);
         lv.forget(MemoryModuleType.ADMIRING_ITEM);
         if (attacker instanceof PlayerEntity) {
            lv.remember(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
         }

         getAvoiding(piglin).ifPresent((avoiding) -> {
            if (avoiding.getType() != attacker.getType()) {
               lv.forget(MemoryModuleType.AVOID_TARGET);
            }

         });
         if (piglin.isBaby()) {
            lv.remember(MemoryModuleType.AVOID_TARGET, attacker, 100L);
            if (Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, attacker)) {
               angerAtCloserTargets(piglin, attacker);
            }

         } else if (attacker.getType() == EntityType.HOGLIN && hasOutnumberedHoglins(piglin)) {
            runAwayFrom(piglin, attacker);
            groupRunAwayFrom(piglin, attacker);
         } else {
            tryRevenge(piglin, attacker);
         }
      }
   }

   protected static void tryRevenge(AbstractPiglinEntity piglin, LivingEntity target) {
      if (!piglin.getBrain().hasActivity(Activity.AVOID)) {
         if (Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, target)) {
            if (!LookTargetUtil.isNewTargetTooFar(piglin, target, 4.0)) {
               if (target.getType() == EntityType.PLAYER && piglin.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
                  becomeAngryWithPlayer(piglin, target);
                  angerNearbyPiglins(piglin);
               } else {
                  becomeAngryWith(piglin, target);
                  angerAtCloserTargets(piglin, target);
               }

            }
         }
      }
   }

   public static Optional getCurrentActivitySound(PiglinEntity piglin) {
      return piglin.getBrain().getFirstPossibleNonCoreActivity().map((activity) -> {
         return getSound(piglin, activity);
      });
   }

   private static SoundEvent getSound(PiglinEntity piglin, Activity activity) {
      if (activity == Activity.FIGHT) {
         return SoundEvents.ENTITY_PIGLIN_ANGRY;
      } else if (piglin.shouldZombify()) {
         return SoundEvents.ENTITY_PIGLIN_RETREAT;
      } else if (activity == Activity.AVOID && hasTargetToAvoid(piglin)) {
         return SoundEvents.ENTITY_PIGLIN_RETREAT;
      } else if (activity == Activity.ADMIRE_ITEM) {
         return SoundEvents.ENTITY_PIGLIN_ADMIRING_ITEM;
      } else if (activity == Activity.CELEBRATE) {
         return SoundEvents.ENTITY_PIGLIN_CELEBRATE;
      } else if (hasPlayerHoldingWantedItemNearby(piglin)) {
         return SoundEvents.ENTITY_PIGLIN_JEALOUS;
      } else {
         return hasSoulFireNearby(piglin) ? SoundEvents.ENTITY_PIGLIN_RETREAT : SoundEvents.ENTITY_PIGLIN_AMBIENT;
      }
   }

   private static boolean hasTargetToAvoid(PiglinEntity piglin) {
      Brain lv = piglin.getBrain();
      return !lv.hasMemoryModule(MemoryModuleType.AVOID_TARGET) ? false : ((LivingEntity)lv.getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET).get()).isInRange(piglin, 12.0);
   }

   protected static List getNearbyVisiblePiglins(PiglinEntity piglin) {
      return (List)piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
   }

   private static List getNearbyPiglins(AbstractPiglinEntity piglin) {
      return (List)piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
   }

   public static boolean wearsGoldArmor(LivingEntity entity) {
      Iterable iterable = entity.getArmorItems();
      Iterator var2 = iterable.iterator();

      Item lv2;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         ItemStack lv = (ItemStack)var2.next();
         lv2 = lv.getItem();
      } while(!(lv2 instanceof ArmorItem) || ((ArmorItem)lv2).getMaterial() != ArmorMaterials.GOLD);

      return true;
   }

   private static void stopWalking(PiglinEntity piglin) {
      piglin.getBrain().forget(MemoryModuleType.WALK_TARGET);
      piglin.getNavigation().stop();
   }

   private static Task makeRememberRideableHoglinTask() {
      LookAtMobWithIntervalTask.Interval lv = new LookAtMobWithIntervalTask.Interval(MEMORY_TRANSFER_TASK_DURATION);
      return MemoryTransferTask.create((entity) -> {
         return entity.isBaby() && lv.shouldRun(entity.world.random);
      }, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_TARGET_MEMORY_DURATION);
   }

   protected static void angerAtCloserTargets(AbstractPiglinEntity piglin, LivingEntity target) {
      getNearbyPiglins(piglin).forEach((nearbyPiglin) -> {
         if (target.getType() != EntityType.HOGLIN || nearbyPiglin.canHunt() && ((HoglinEntity)target).canBeHunted()) {
            angerAtIfCloser(nearbyPiglin, target);
         }
      });
   }

   protected static void angerNearbyPiglins(AbstractPiglinEntity piglin) {
      getNearbyPiglins(piglin).forEach((nearbyPiglin) -> {
         getNearestDetectedPlayer(nearbyPiglin).ifPresent((player) -> {
            becomeAngryWith(nearbyPiglin, player);
         });
      });
   }

   protected static void becomeAngryWith(AbstractPiglinEntity piglin, LivingEntity target) {
      if (Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, target)) {
         piglin.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         piglin.getBrain().remember(MemoryModuleType.ANGRY_AT, target.getUuid(), 600L);
         if (target.getType() == EntityType.HOGLIN && piglin.canHunt()) {
            rememberHunting(piglin);
         }

         if (target.getType() == EntityType.PLAYER && piglin.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
            piglin.getBrain().remember(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
         }

      }
   }

   private static void becomeAngryWithPlayer(AbstractPiglinEntity piglin, LivingEntity player) {
      Optional optional = getNearestDetectedPlayer(piglin);
      if (optional.isPresent()) {
         becomeAngryWith(piglin, (LivingEntity)optional.get());
      } else {
         becomeAngryWith(piglin, player);
      }

   }

   private static void angerAtIfCloser(AbstractPiglinEntity piglin, LivingEntity target) {
      Optional optional = getAngryAt(piglin);
      LivingEntity lv = LookTargetUtil.getCloserEntity(piglin, (Optional)optional, target);
      if (!optional.isPresent() || optional.get() != lv) {
         becomeAngryWith(piglin, lv);
      }
   }

   private static Optional getAngryAt(AbstractPiglinEntity piglin) {
      return LookTargetUtil.getEntity(piglin, MemoryModuleType.ANGRY_AT);
   }

   public static Optional getAvoiding(PiglinEntity piglin) {
      return piglin.getBrain().hasMemoryModule(MemoryModuleType.AVOID_TARGET) ? piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
   }

   public static Optional getNearestDetectedPlayer(AbstractPiglinEntity piglin) {
      return piglin.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER) ? piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER) : Optional.empty();
   }

   private static void groupRunAwayFrom(PiglinEntity piglin, LivingEntity target) {
      getNearbyVisiblePiglins(piglin).stream().filter((nearbyVisiblePiglin) -> {
         return nearbyVisiblePiglin instanceof PiglinEntity;
      }).forEach((piglinx) -> {
         runAwayFromClosestTarget((PiglinEntity)piglinx, target);
      });
   }

   private static void runAwayFromClosestTarget(PiglinEntity piglin, LivingEntity target) {
      Brain lv = piglin.getBrain();
      LivingEntity lv2 = LookTargetUtil.getCloserEntity(piglin, (Optional)lv.getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET), target);
      lv2 = LookTargetUtil.getCloserEntity(piglin, (Optional)lv.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET), lv2);
      runAwayFrom(piglin, lv2);
   }

   private static boolean shouldRunAwayFromHoglins(PiglinEntity piglin) {
      Brain lv = piglin.getBrain();
      if (!lv.hasMemoryModule(MemoryModuleType.AVOID_TARGET)) {
         return true;
      } else {
         LivingEntity lv2 = (LivingEntity)lv.getOptionalRegisteredMemory(MemoryModuleType.AVOID_TARGET).get();
         EntityType lv3 = lv2.getType();
         if (lv3 == EntityType.HOGLIN) {
            return hasNoAdvantageAgainstHoglins(piglin);
         } else if (isZombified(lv3)) {
            return !lv.hasMemoryModuleWithValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, lv2);
         } else {
            return false;
         }
      }
   }

   private static boolean hasNoAdvantageAgainstHoglins(PiglinEntity piglin) {
      return !hasOutnumberedHoglins(piglin);
   }

   private static boolean hasOutnumberedHoglins(PiglinEntity piglins) {
      int i = (Integer)piglins.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
      int j = (Integer)piglins.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
      return j > i;
   }

   private static void runAwayFrom(PiglinEntity piglin, LivingEntity target) {
      piglin.getBrain().forget(MemoryModuleType.ANGRY_AT);
      piglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
      piglin.getBrain().forget(MemoryModuleType.WALK_TARGET);
      piglin.getBrain().remember(MemoryModuleType.AVOID_TARGET, target, (long)AVOID_MEMORY_DURATION.get(piglin.world.random));
      rememberHunting(piglin);
   }

   protected static void rememberHunting(AbstractPiglinEntity piglin) {
      piglin.getBrain().remember(MemoryModuleType.HUNTED_RECENTLY, true, (long)HUNT_MEMORY_DURATION.get(piglin.world.random));
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
      entity.getBrain().remember(MemoryModuleType.ADMIRING_ITEM, true, 120L);
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
      return !hasPlayerHoldingWantedItemNearby(piglin);
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
      return piglin.getOffHandStack().isEmpty() || !isGoldenItem(piglin.getOffHandStack());
   }

   public static boolean isZombified(EntityType entityType) {
      return entityType == EntityType.ZOMBIFIED_PIGLIN || entityType == EntityType.ZOGLIN;
   }

   static {
      BARTERING_ITEM = Items.GOLD_INGOT;
      HUNT_MEMORY_DURATION = TimeHelper.betweenSeconds(30, 120);
      MEMORY_TRANSFER_TASK_DURATION = TimeHelper.betweenSeconds(10, 40);
      RIDE_TARGET_MEMORY_DURATION = TimeHelper.betweenSeconds(10, 30);
      AVOID_MEMORY_DURATION = TimeHelper.betweenSeconds(5, 20);
      GO_TO_ZOMBIFIED_MEMORY_DURATION = TimeHelper.betweenSeconds(5, 7);
      GO_TO_NEMESIS_MEMORY_DURATION = TimeHelper.betweenSeconds(5, 7);
   }
}
