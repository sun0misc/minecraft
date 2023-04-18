package net.minecraft.entity.ai.brain.task;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GiveInventoryToLookTargetTask extends MultiTickTask {
   private static final int COMPLETION_RANGE = 3;
   private static final int ITEM_PICKUP_COOLDOWN_TICKS = 60;
   private final Function lookTargetFunction;
   private final float speed;

   public GiveInventoryToLookTargetTask(Function lookTargetFunction, float speed, int runTime) {
      super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleState.REGISTERED), runTime);
      this.lookTargetFunction = lookTargetFunction;
      this.speed = speed;
   }

   protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
      return this.hasItemAndTarget(entity);
   }

   protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
      return this.hasItemAndTarget(entity);
   }

   protected void run(ServerWorld world, LivingEntity entity, long time) {
      ((Optional)this.lookTargetFunction.apply(entity)).ifPresent((target) -> {
         LookTargetUtil.walkTowards(entity, (LookTarget)target, this.speed, 3);
      });
   }

   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      Optional optional = (Optional)this.lookTargetFunction.apply(entity);
      if (!optional.isEmpty()) {
         LookTarget lv = (LookTarget)optional.get();
         double d = lv.getPos().distanceTo(entity.getEyePos());
         if (d < 3.0) {
            ItemStack lv2 = ((InventoryOwner)entity).getInventory().removeStack(0, 1);
            if (!lv2.isEmpty()) {
               playThrowSound(entity, lv2, offsetTarget(lv));
               if (entity instanceof AllayEntity) {
                  AllayEntity lv3 = (AllayEntity)entity;
                  AllayBrain.getLikedPlayer(lv3).ifPresent((player) -> {
                     this.triggerCriterion(lv, lv2, player);
                  });
               }

               entity.getBrain().remember(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, (int)60);
            }
         }

      }
   }

   private void triggerCriterion(LookTarget target, ItemStack stack, ServerPlayerEntity player) {
      BlockPos lv = target.getBlockPos().down();
      Criteria.ALLAY_DROP_ITEM_ON_BLOCK.trigger(player, lv, stack);
   }

   private boolean hasItemAndTarget(LivingEntity entity) {
      if (((InventoryOwner)entity).getInventory().isEmpty()) {
         return false;
      } else {
         Optional optional = (Optional)this.lookTargetFunction.apply(entity);
         return optional.isPresent();
      }
   }

   private static Vec3d offsetTarget(LookTarget target) {
      return target.getPos().add(0.0, 1.0, 0.0);
   }

   public static void playThrowSound(LivingEntity entity, ItemStack stack, Vec3d target) {
      Vec3d lv = new Vec3d(0.20000000298023224, 0.30000001192092896, 0.20000000298023224);
      LookTargetUtil.give(entity, stack, target, lv, 0.2F);
      World lv2 = entity.world;
      if (lv2.getTime() % 7L == 0L && lv2.random.nextDouble() < 0.9) {
         float f = (Float)Util.getRandom((List)AllayEntity.THROW_SOUND_PITCHES, lv2.getRandom());
         lv2.playSoundFromEntity((PlayerEntity)null, entity, SoundEvents.ENTITY_ALLAY_ITEM_THROWN, SoundCategory.NEUTRAL, 1.0F, f);
      }

   }
}
