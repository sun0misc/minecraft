/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class TakeJobSiteTask {
    public static Task<VillagerEntity> create(float speed) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE), context.queryMemoryAbsent(MemoryModuleType.JOB_SITE), context.queryMemoryValue(MemoryModuleType.MOBS), context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (potentialJobSite, jobSite, mobs, walkTarget, lookTarget) -> (world, entity, time) -> {
            if (entity.isBaby()) {
                return false;
            }
            if (entity.getVillagerData().getProfession() != VillagerProfession.NONE) {
                return false;
            }
            BlockPos lv = ((GlobalPos)context.getValue(potentialJobSite)).pos();
            Optional<RegistryEntry<PointOfInterestType>> optional = world.getPointOfInterestStorage().getType(lv);
            if (optional.isEmpty()) {
                return true;
            }
            ((List)context.getValue(mobs)).stream().filter(mob -> mob instanceof VillagerEntity && mob != entity).map(villager -> (VillagerEntity)villager).filter(LivingEntity::isAlive).filter(villager -> TakeJobSiteTask.canUseJobSite((RegistryEntry)optional.get(), villager, lv)).findFirst().ifPresent(villager -> {
                walkTarget.forget();
                lookTarget.forget();
                potentialJobSite.forget();
                if (villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE).isEmpty()) {
                    LookTargetUtil.walkTowards((LivingEntity)villager, lv, speed, 1);
                    villager.getBrain().remember(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.create(world.getRegistryKey(), lv));
                    DebugInfoSender.sendPointOfInterest(world, lv);
                }
            });
            return true;
        }));
    }

    private static boolean canUseJobSite(RegistryEntry<PointOfInterestType> poiType, VillagerEntity villager, BlockPos pos) {
        boolean bl = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
        if (bl) {
            return false;
        }
        Optional<GlobalPos> optional = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
        VillagerProfession lv = villager.getVillagerData().getProfession();
        if (lv.heldWorkstation().test(poiType)) {
            if (optional.isEmpty()) {
                return TakeJobSiteTask.canReachJobSite(villager, pos, poiType.value());
            }
            return optional.get().pos().equals(pos);
        }
        return false;
    }

    private static boolean canReachJobSite(PathAwareEntity entity, BlockPos pos, PointOfInterestType poiType) {
        Path lv = entity.getNavigation().findPathTo(pos, poiType.searchDistance());
        return lv != null && lv.reachesTarget();
    }
}

