package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

public class LoseJobOnSiteLossTask {
   public static Task create() {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.JOB_SITE)).apply(context, (jobSite) -> {
            return (world, entity, time) -> {
               VillagerData lv = entity.getVillagerData();
               if (lv.getProfession() != VillagerProfession.NONE && lv.getProfession() != VillagerProfession.NITWIT && entity.getExperience() == 0 && lv.getLevel() <= 1) {
                  entity.setVillagerData(entity.getVillagerData().withProfession(VillagerProfession.NONE));
                  entity.reinitializeBrain(world);
                  return true;
               } else {
                  return false;
               }
            };
         });
      });
   }
}
