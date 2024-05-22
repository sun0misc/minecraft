/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SonicBoomTask
extends MultiTickTask<WardenEntity> {
    private static final int HORIZONTAL_RANGE = 15;
    private static final int VERTICAL_RANGE = 20;
    private static final double field_38852 = 0.5;
    private static final double field_38853 = 2.5;
    public static final int COOLDOWN = 40;
    private static final int SOUND_DELAY = MathHelper.ceil(34.0);
    private static final int RUN_TIME = MathHelper.ceil(60.0f);

    public SonicBoomTask() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.SONIC_BOOM_COOLDOWN, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, MemoryModuleState.REGISTERED, MemoryModuleType.SONIC_BOOM_SOUND_DELAY, MemoryModuleState.REGISTERED), RUN_TIME);
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, WardenEntity arg2) {
        return arg2.isInRange(arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get(), 15.0, 20.0);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, WardenEntity arg2, long l) {
        return true;
    }

    @Override
    protected void run(ServerWorld arg, WardenEntity arg2, long l) {
        arg2.getBrain().remember(MemoryModuleType.ATTACK_COOLING_DOWN, true, RUN_TIME);
        arg2.getBrain().remember(MemoryModuleType.SONIC_BOOM_SOUND_DELAY, Unit.INSTANCE, SOUND_DELAY);
        arg.sendEntityStatus(arg2, EntityStatuses.SONIC_BOOM);
        arg2.playSound(SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, 3.0f, 1.0f);
    }

    @Override
    protected void keepRunning(ServerWorld arg, WardenEntity arg2, long l) {
        arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> arg2.getLookControl().lookAt(target.getPos()));
        if (arg2.getBrain().hasMemoryModule(MemoryModuleType.SONIC_BOOM_SOUND_DELAY) || arg2.getBrain().hasMemoryModule(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) {
            return;
        }
        arg2.getBrain().remember(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, Unit.INSTANCE, RUN_TIME - SOUND_DELAY);
        arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).filter(arg2::isValidTarget).filter(target -> arg2.isInRange((Entity)target, 15.0, 20.0)).ifPresent(target -> {
            Vec3d lv = arg2.getPos().add(arg2.getAttachments().getPoint(EntityAttachmentType.WARDEN_CHEST, 0, arg2.getYaw()));
            Vec3d lv2 = target.getEyePos().subtract(lv);
            Vec3d lv3 = lv2.normalize();
            int i = MathHelper.floor(lv2.length()) + 7;
            for (int j = 1; j < i; ++j) {
                Vec3d lv4 = lv.add(lv3.multiply(j));
                arg.spawnParticles(ParticleTypes.SONIC_BOOM, lv4.x, lv4.y, lv4.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            arg2.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 1.0f);
            if (target.damage(arg.getDamageSources().sonicBoom(arg2), 10.0f)) {
                double d = 0.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                double e = 2.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                target.addVelocity(lv3.getX() * e, lv3.getY() * d, lv3.getZ() * e);
            }
        });
    }

    @Override
    protected void finishRunning(ServerWorld arg, WardenEntity arg2, long l) {
        SonicBoomTask.cooldown(arg2, 40);
    }

    public static void cooldown(LivingEntity warden, int cooldown) {
        warden.getBrain().remember(MemoryModuleType.SONIC_BOOM_COOLDOWN, Unit.INSTANCE, cooldown);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (WardenEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (WardenEntity)entity, time);
    }
}

