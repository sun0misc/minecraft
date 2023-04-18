package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SonicBoomTask extends MultiTickTask {
   private static final int HORIZONTAL_RANGE = 15;
   private static final int VERTICAL_RANGE = 20;
   private static final double field_38852 = 0.5;
   private static final double field_38853 = 2.5;
   public static final int COOLDOWN = 40;
   private static final int SOUND_DELAY = MathHelper.ceil(34.0);
   private static final int RUN_TIME = MathHelper.ceil(60.0F);

   public SonicBoomTask() {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.SONIC_BOOM_COOLDOWN, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, MemoryModuleState.REGISTERED, MemoryModuleType.SONIC_BOOM_SOUND_DELAY, MemoryModuleState.REGISTERED), RUN_TIME);
   }

   protected boolean shouldRun(ServerWorld arg, WardenEntity arg2) {
      return arg2.isInRange((Entity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get(), 15.0, 20.0);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, WardenEntity arg2, long l) {
      return true;
   }

   protected void run(ServerWorld arg, WardenEntity arg2, long l) {
      arg2.getBrain().remember(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)RUN_TIME);
      arg2.getBrain().remember(MemoryModuleType.SONIC_BOOM_SOUND_DELAY, Unit.INSTANCE, (long)SOUND_DELAY);
      arg.sendEntityStatus(arg2, EntityStatuses.SONIC_BOOM);
      arg2.playSound(SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, 3.0F, 1.0F);
   }

   protected void keepRunning(ServerWorld arg, WardenEntity arg2, long l) {
      arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((target) -> {
         arg2.getLookControl().lookAt(target.getPos());
      });
      if (!arg2.getBrain().hasMemoryModule(MemoryModuleType.SONIC_BOOM_SOUND_DELAY) && !arg2.getBrain().hasMemoryModule(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) {
         arg2.getBrain().remember(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, Unit.INSTANCE, (long)(RUN_TIME - SOUND_DELAY));
         Optional var10000 = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
         Objects.requireNonNull(arg2);
         var10000.filter(arg2::isValidTarget).filter((target) -> {
            return arg2.isInRange(target, 15.0, 20.0);
         }).ifPresent((target) -> {
            Vec3d lv = arg2.getPos().add(0.0, 1.600000023841858, 0.0);
            Vec3d lv2 = target.getEyePos().subtract(lv);
            Vec3d lv3 = lv2.normalize();

            for(int i = 1; i < MathHelper.floor(lv2.length()) + 7; ++i) {
               Vec3d lv4 = lv.add(lv3.multiply((double)i));
               arg.spawnParticles(ParticleTypes.SONIC_BOOM, lv4.x, lv4.y, lv4.z, 1, 0.0, 0.0, 0.0, 0.0);
            }

            arg2.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 3.0F, 1.0F);
            target.damage(arg.getDamageSources().sonicBoom(arg2), 10.0F);
            double d = 0.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
            double e = 2.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
            target.addVelocity(lv3.getX() * e, lv3.getY() * d, lv3.getZ() * e);
         });
      }
   }

   protected void finishRunning(ServerWorld arg, WardenEntity arg2, long l) {
      cooldown(arg2, 40);
   }

   public static void cooldown(LivingEntity warden, int cooldown) {
      warden.getBrain().remember(MemoryModuleType.SONIC_BOOM_COOLDOWN, Unit.INSTANCE, (long)cooldown);
   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (WardenEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (WardenEntity)entity, time);
   }
}
