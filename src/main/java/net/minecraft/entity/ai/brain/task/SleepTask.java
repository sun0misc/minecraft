package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class SleepTask extends MultiTickTask {
   public static final int RUN_TIME = 100;
   private long startTime;

   public SleepTask() {
      super(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryModuleState.REGISTERED));
   }

   protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
      if (entity.hasVehicle()) {
         return false;
      } else {
         Brain lv = entity.getBrain();
         GlobalPos lv2 = (GlobalPos)lv.getOptionalRegisteredMemory(MemoryModuleType.HOME).get();
         if (world.getRegistryKey() != lv2.getDimension()) {
            return false;
         } else {
            Optional optional = lv.getOptionalRegisteredMemory(MemoryModuleType.LAST_WOKEN);
            if (optional.isPresent()) {
               long l = world.getTime() - (Long)optional.get();
               if (l > 0L && l < 100L) {
                  return false;
               }
            }

            BlockState lv3 = world.getBlockState(lv2.getPos());
            return lv2.getPos().isWithinDistance(entity.getPos(), 2.0) && lv3.isIn(BlockTags.BEDS) && !(Boolean)lv3.get(BedBlock.OCCUPIED);
         }
      }
   }

   protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
      Optional optional = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME);
      if (!optional.isPresent()) {
         return false;
      } else {
         BlockPos lv = ((GlobalPos)optional.get()).getPos();
         return entity.getBrain().hasActivity(Activity.REST) && entity.getY() > (double)lv.getY() + 0.4 && lv.isWithinDistance(entity.getPos(), 1.14);
      }
   }

   protected void run(ServerWorld world, LivingEntity entity, long time) {
      if (time > this.startTime) {
         Brain lv = entity.getBrain();
         if (lv.hasMemoryModule(MemoryModuleType.DOORS_TO_CLOSE)) {
            Set set = (Set)lv.getOptionalRegisteredMemory(MemoryModuleType.DOORS_TO_CLOSE).get();
            Optional optional;
            if (lv.hasMemoryModule(MemoryModuleType.MOBS)) {
               optional = lv.getOptionalRegisteredMemory(MemoryModuleType.MOBS);
            } else {
               optional = Optional.empty();
            }

            OpenDoorsTask.pathToDoor(world, entity, (PathNode)null, (PathNode)null, set, optional);
         }

         entity.sleep(((GlobalPos)entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME).get()).getPos());
      }

   }

   protected boolean isTimeLimitExceeded(long time) {
      return false;
   }

   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      if (entity.isSleeping()) {
         entity.wakeUp();
         this.startTime = time + 40L;
      }

   }
}
