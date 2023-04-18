package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.GiveInventoryToLookTargetTask;
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TemptationCooldownTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkToNearestVisibleWantedItemTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;

public class AllayBrain {
   private static final float field_38406 = 1.0F;
   private static final float field_38407 = 2.25F;
   private static final float field_38408 = 1.75F;
   private static final float field_39113 = 2.5F;
   private static final int field_38938 = 4;
   private static final int field_38939 = 16;
   private static final int field_38410 = 6;
   private static final int field_38411 = 30;
   private static final int field_38412 = 60;
   private static final int field_38413 = 600;
   private static final int field_38940 = 32;
   private static final int field_40130 = 20;

   protected static Brain create(Brain brain) {
      addCoreActivities(brain);
      addIdleActivities(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.resetPossibleActivities();
      return brain;
   }

   private static void addCoreActivities(Brain brain) {
      brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new StayAboveWaterTask(0.8F), new FleeTask(2.5F), new LookAroundTask(45, 90), new WanderAroundTask(), new TemptationCooldownTask(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS), new TemptationCooldownTask(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)));
   }

   private static void addIdleActivities(Brain brain) {
      brain.setTaskList(Activity.IDLE, ImmutableList.of(Pair.of(0, WalkToNearestVisibleWantedItemTask.create((allay) -> {
         return true;
      }, 1.75F, true, 32)), Pair.of(1, new GiveInventoryToLookTargetTask(AllayBrain::getLookTarget, 2.25F, 20)), Pair.of(2, WalkTowardsLookTargetTask.create(AllayBrain::getLookTarget, Predicate.not(AllayBrain::hasNearestVisibleWantedItem), 4, 16, 2.25F)), Pair.of(3, LookAtMobWithIntervalTask.follow(6.0F, UniformIntProvider.create(30, 60))), Pair.of(4, new RandomTask(ImmutableList.of(Pair.of(StrollTask.createSolidTargeting(1.0F), 2), Pair.of(GoTowardsLookTargetTask.create(1.0F, 3), 2), Pair.of(new WaitTask(30, 60), 1))))), ImmutableSet.of());
   }

   public static void updateActivities(AllayEntity allay) {
      allay.getBrain().resetPossibleActivities((List)ImmutableList.of(Activity.IDLE));
   }

   public static void rememberNoteBlock(LivingEntity allay, BlockPos pos) {
      Brain lv = allay.getBrain();
      GlobalPos lv2 = GlobalPos.create(allay.getWorld().getRegistryKey(), pos);
      Optional optional = lv.getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK);
      if (optional.isEmpty()) {
         lv.remember(MemoryModuleType.LIKED_NOTEBLOCK, (Object)lv2);
         lv.remember(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, (int)600);
      } else if (((GlobalPos)optional.get()).equals(lv2)) {
         lv.remember(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, (int)600);
      }

   }

   private static Optional getLookTarget(LivingEntity allay) {
      Brain lv = allay.getBrain();
      Optional optional = lv.getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK);
      if (optional.isPresent()) {
         GlobalPos lv2 = (GlobalPos)optional.get();
         if (shouldGoTowardsNoteBlock(allay, lv, lv2)) {
            return Optional.of(new BlockPosLookTarget(lv2.getPos().up()));
         }

         lv.forget(MemoryModuleType.LIKED_NOTEBLOCK);
      }

      return getLikedLookTarget(allay);
   }

   private static boolean hasNearestVisibleWantedItem(LivingEntity entity) {
      Brain lv = entity.getBrain();
      return lv.hasMemoryModule(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
   }

   private static boolean shouldGoTowardsNoteBlock(LivingEntity allay, Brain brain, GlobalPos pos) {
      Optional optional = brain.getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
      World lv = allay.getWorld();
      return lv.getRegistryKey() == pos.getDimension() && lv.getBlockState(pos.getPos()).isOf(Blocks.NOTE_BLOCK) && optional.isPresent();
   }

   private static Optional getLikedLookTarget(LivingEntity allay) {
      return getLikedPlayer(allay).map((player) -> {
         return new EntityLookTarget(player, true);
      });
   }

   public static Optional getLikedPlayer(LivingEntity allay) {
      World lv = allay.getWorld();
      if (!lv.isClient() && lv instanceof ServerWorld lv2) {
         Optional optional = allay.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
         if (optional.isPresent()) {
            Entity lv3 = lv2.getEntity((UUID)optional.get());
            if (lv3 instanceof ServerPlayerEntity) {
               ServerPlayerEntity lv4 = (ServerPlayerEntity)lv3;
               if ((lv4.interactionManager.isSurvivalLike() || lv4.interactionManager.isCreative()) && lv4.isInRange(allay, 64.0)) {
                  return Optional.of(lv4);
               }
            }

            return Optional.empty();
         }
      }

      return Optional.empty();
   }
}
