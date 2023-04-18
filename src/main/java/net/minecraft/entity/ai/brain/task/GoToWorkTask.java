package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;

public class GoToWorkTask {
   public static Task create() {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE), context.queryMemoryOptional(MemoryModuleType.JOB_SITE)).apply(context, (potentialJobSite, jobSite) -> {
            return (world, entity, time) -> {
               GlobalPos lv = (GlobalPos)context.getValue(potentialJobSite);
               if (!lv.getPos().isWithinDistance(entity.getPos(), 2.0) && !entity.isNatural()) {
                  return false;
               } else {
                  potentialJobSite.forget();
                  jobSite.remember((Object)lv);
                  world.sendEntityStatus(entity, EntityStatuses.ADD_VILLAGER_HAPPY_PARTICLES);
                  if (entity.getVillagerData().getProfession() != VillagerProfession.NONE) {
                     return true;
                  } else {
                     MinecraftServer minecraftServer = world.getServer();
                     Optional.ofNullable(minecraftServer.getWorld(lv.getDimension())).flatMap((jobSiteWorld) -> {
                        return jobSiteWorld.getPointOfInterestStorage().getType(lv.getPos());
                     }).flatMap((poiType) -> {
                        return Registries.VILLAGER_PROFESSION.stream().filter((profession) -> {
                           return profession.heldWorkstation().test(poiType);
                        }).findFirst();
                     }).ifPresent((profession) -> {
                        entity.setVillagerData(entity.getVillagerData().withProfession(profession));
                        entity.reinitializeBrain(world);
                     });
                     return true;
                  }
               }
            };
         });
      });
   }
}
