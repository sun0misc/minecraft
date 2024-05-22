/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class WorkStationCompetitionTask {
    public static Task<VillagerEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.JOB_SITE), context.queryMemoryValue(MemoryModuleType.MOBS)).apply(context, (jobSite, mobs) -> (world, entity, time) -> {
            GlobalPos lv = (GlobalPos)context.getValue(jobSite);
            world.getPointOfInterestStorage().getType(lv.pos()).ifPresent(poiType -> ((List)context.getValue(mobs)).stream().filter(mob -> mob instanceof VillagerEntity && mob != entity).map(villager -> (VillagerEntity)villager).filter(LivingEntity::isAlive).filter(villager -> WorkStationCompetitionTask.isUsingWorkStationAt(lv, poiType, villager)).reduce((VillagerEntity)entity, WorkStationCompetitionTask::keepJobSiteForMoreExperiencedVillager));
            return true;
        }));
    }

    private static VillagerEntity keepJobSiteForMoreExperiencedVillager(VillagerEntity first, VillagerEntity second) {
        VillagerEntity lv2;
        VillagerEntity lv;
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

    private static boolean isUsingWorkStationAt(GlobalPos pos, RegistryEntry<PointOfInterestType> poiType, VillagerEntity villager) {
        Optional<GlobalPos> optional = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
        return optional.isPresent() && pos.equals(optional.get()) && WorkStationCompetitionTask.isCompletedWorkStation(poiType, villager.getVillagerData().getProfession());
    }

    private static boolean isCompletedWorkStation(RegistryEntry<PointOfInterestType> poiType, VillagerProfession profession) {
        return profession.heldWorkstation().test(poiType);
    }
}

