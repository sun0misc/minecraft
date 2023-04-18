package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestStorage;

public class WalkTowardJobSiteTask extends MultiTickTask {
   private static final int RUN_TIME = 1200;
   final float speed;

   public WalkTowardJobSiteTask(float speed) {
      super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleState.VALUE_PRESENT), 1200);
      this.speed = speed;
   }

   protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
      return (Boolean)arg2.getBrain().getFirstPossibleNonCoreActivity().map((activity) -> {
         return activity == Activity.IDLE || activity == Activity.WORK || activity == Activity.PLAY;
      }).orElse(true);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      return arg2.getBrain().hasMemoryModule(MemoryModuleType.POTENTIAL_JOB_SITE);
   }

   protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      LookTargetUtil.walkTowards(arg2, (BlockPos)((GlobalPos)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get()).getPos(), this.speed, 1);
   }

   protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      Optional optional = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
      optional.ifPresent((pos) -> {
         BlockPos lv = pos.getPos();
         ServerWorld lv2 = arg.getServer().getWorld(pos.getDimension());
         if (lv2 != null) {
            PointOfInterestStorage lv3 = lv2.getPointOfInterestStorage();
            if (lv3.test(lv, (poiType) -> {
               return true;
            })) {
               lv3.releaseTicket(lv);
            }

            DebugInfoSender.sendPointOfInterest(arg, lv);
         }
      });
      arg2.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (VillagerEntity)entity, time);
   }

   // $FF: synthetic method
   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      this.keepRunning(world, (VillagerEntity)entity, time);
   }
}
