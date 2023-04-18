package net.minecraft.entity.ai.brain.task;

import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;

public class WorkStationCompetitionTask {
   public static Task create() {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryValue(MemoryModuleType.JOB_SITE), context.queryMemoryValue(MemoryModuleType.MOBS)).apply(context, (jobSite, mobs) -> {
            return (world, entity, time) -> {
               GlobalPos lv = (GlobalPos)context.getValue(jobSite);
               world.getPointOfInterestStorage().getType(lv.getPos()).ifPresent((poiType) -> {
                  ((List)context.getValue(mobs)).stream().filter((mob) -> {
                     return mob instanceof VillagerEntity && mob != entity;
                  }).map((villager) -> {
                     return (VillagerEntity)villager;
                  }).filter(LivingEntity::isAlive).filter((villager) -> {
                     return isUsingWorkStationAt(lv, poiType, villager);
                  }).reduce(entity, WorkStationCompetitionTask::keepJobSiteForMoreExperiencedVillager);
               });
               return true;
            };
         });
      });
   }

   private static VillagerEntity keepJobSiteForMoreExperiencedVillager(VillagerEntity first, VillagerEntity second) {
      VillagerEntity lv;
      VillagerEntity lv2;
      if (first.getExperience() > second.getExperience()) {
         lv = first;
         lv2 = second;
      } else {
         lv = second;
         lv2 = first;
      }

      lv2.getBrain().forget(MemoryModuleType.JOB_SITE);
      return lv;
   }

   private static boolean isUsingWorkStationAt(GlobalPos pos, RegistryEntry poiType, VillagerEntity villager) {
      Optional optional = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
      return optional.isPresent() && pos.equals(optional.get()) && isCompletedWorkStation(poiType, villager.getVillagerData().getProfession());
   }

   private static boolean isCompletedWorkStation(RegistryEntry poiType, VillagerProfession profession) {
      return profession.heldWorkstation().test(poiType);
   }
}
