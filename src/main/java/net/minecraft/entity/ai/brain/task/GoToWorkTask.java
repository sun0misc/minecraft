/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class GoToWorkTask {
    public static Task<VillagerEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE), context.queryMemoryOptional(MemoryModuleType.JOB_SITE)).apply(context, (potentialJobSite, jobSite) -> (world, entity, time) -> {
            GlobalPos lv = (GlobalPos)context.getValue(potentialJobSite);
            if (!lv.pos().isWithinDistance(entity.getPos(), 2.0) && !entity.isNatural()) {
                return false;
            }
            potentialJobSite.forget();
            jobSite.remember(lv);
            world.sendEntityStatus(entity, EntityStatuses.ADD_VILLAGER_HAPPY_PARTICLES);
            if (entity.getVillagerData().getProfession() != VillagerProfession.NONE) {
                return true;
            }
            MinecraftServer minecraftServer = world.getServer();
            Optional.ofNullable(minecraftServer.getWorld(lv.dimension())).flatMap(jobSiteWorld -> jobSiteWorld.getPointOfInterestStorage().getType(lv.pos())).flatMap(poiType -> Registries.VILLAGER_PROFESSION.stream().filter(profession -> profession.heldWorkstation().test((RegistryEntry<PointOfInterestType>)poiType)).findFirst()).ifPresent(profession -> {
                entity.setVillagerData(entity.getVillagerData().withProfession((VillagerProfession)profession));
                entity.reinitializeBrain(world);
            });
            return true;
        }));
    }
}

