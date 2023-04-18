package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public class VillagerBreedTask extends MultiTickTask {
   private static final int MAX_DISTANCE = 5;
   private static final float APPROACH_SPEED = 0.5F;
   private long breedEndTime;

   public VillagerBreedTask() {
      super(ImmutableMap.of(MemoryModuleType.BREED_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT), 350, 350);
   }

   protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
      return this.isReadyToBreed(arg2);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      return l <= this.breedEndTime && this.isReadyToBreed(arg2);
   }

   protected void run(ServerWorld arg, VillagerEntity arg2, long l) {
      PassiveEntity lv = (PassiveEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get();
      LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, 0.5F);
      arg.sendEntityStatus(lv, EntityStatuses.ADD_BREEDING_PARTICLES);
      arg.sendEntityStatus(arg2, EntityStatuses.ADD_BREEDING_PARTICLES);
      int i = 275 + arg2.getRandom().nextInt(50);
      this.breedEndTime = l + (long)i;
   }

   protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      VillagerEntity lv = (VillagerEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get();
      if (!(arg2.squaredDistanceTo(lv) > 5.0)) {
         LookTargetUtil.lookAtAndWalkTowardsEachOther(arg2, lv, 0.5F);
         if (l >= this.breedEndTime) {
            arg2.eatForBreeding();
            lv.eatForBreeding();
            this.goHome(arg, arg2, lv);
         } else if (arg2.getRandom().nextInt(35) == 0) {
            arg.sendEntityStatus(lv, EntityStatuses.ADD_VILLAGER_HEART_PARTICLES);
            arg.sendEntityStatus(arg2, EntityStatuses.ADD_VILLAGER_HEART_PARTICLES);
         }

      }
   }

   private void goHome(ServerWorld world, VillagerEntity first, VillagerEntity second) {
      Optional optional = this.getReachableHome(world, first);
      if (!optional.isPresent()) {
         world.sendEntityStatus(second, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
         world.sendEntityStatus(first, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
      } else {
         Optional optional2 = this.createChild(world, first, second);
         if (optional2.isPresent()) {
            this.setChildHome(world, (VillagerEntity)optional2.get(), (BlockPos)optional.get());
         } else {
            world.getPointOfInterestStorage().releaseTicket((BlockPos)optional.get());
            DebugInfoSender.sendPointOfInterest(world, (BlockPos)optional.get());
         }
      }

   }

   protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      arg2.getBrain().forget(MemoryModuleType.BREED_TARGET);
   }

   private boolean isReadyToBreed(VillagerEntity villager) {
      Brain lv = villager.getBrain();
      Optional optional = lv.getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).filter((arg) -> {
         return arg.getType() == EntityType.VILLAGER;
      });
      if (!optional.isPresent()) {
         return false;
      } else {
         return LookTargetUtil.canSee(lv, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && villager.isReadyToBreed() && ((PassiveEntity)optional.get()).isReadyToBreed();
      }
   }

   private Optional getReachableHome(ServerWorld world, VillagerEntity villager) {
      return world.getPointOfInterestStorage().getPosition((poiType) -> {
         return poiType.matchesKey(PointOfInterestTypes.HOME);
      }, (poiType, pos) -> {
         return this.canReachHome(villager, pos, poiType);
      }, villager.getBlockPos(), 48);
   }

   private boolean canReachHome(VillagerEntity villager, BlockPos pos, RegistryEntry poiType) {
      Path lv = villager.getNavigation().findPathTo(pos, ((PointOfInterestType)poiType.value()).searchDistance());
      return lv != null && lv.reachesTarget();
   }

   private Optional createChild(ServerWorld world, VillagerEntity parent, VillagerEntity partner) {
      VillagerEntity lv = parent.createChild(world, partner);
      if (lv == null) {
         return Optional.empty();
      } else {
         parent.setBreedingAge(6000);
         partner.setBreedingAge(6000);
         lv.setBreedingAge(-24000);
         lv.refreshPositionAndAngles(parent.getX(), parent.getY(), parent.getZ(), 0.0F, 0.0F);
         world.spawnEntityAndPassengers(lv);
         world.sendEntityStatus(lv, EntityStatuses.ADD_VILLAGER_HEART_PARTICLES);
         return Optional.of(lv);
      }
   }

   private void setChildHome(ServerWorld world, VillagerEntity child, BlockPos pos) {
      GlobalPos lv = GlobalPos.create(world.getRegistryKey(), pos);
      child.getBrain().remember(MemoryModuleType.HOME, (Object)lv);
   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (VillagerEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (VillagerEntity)entity, time);
   }
}
