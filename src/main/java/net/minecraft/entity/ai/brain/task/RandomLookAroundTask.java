package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;

public class RandomLookAroundTask extends MultiTickTask {
   private final IntProvider cooldown;
   private final float maxYaw;
   private final float minPitch;
   private final float pitchRange;

   public RandomLookAroundTask(IntProvider cooldown, float maxYaw, float minPitch, float maxPitch) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT));
      if (minPitch > maxPitch) {
         throw new IllegalArgumentException("Minimum pitch is larger than maximum pitch! " + minPitch + " > " + maxPitch);
      } else {
         this.cooldown = cooldown;
         this.maxYaw = maxYaw;
         this.minPitch = minPitch;
         this.pitchRange = maxPitch - minPitch;
      }
   }

   protected void run(ServerWorld arg, MobEntity arg2, long l) {
      Random lv = arg2.getRandom();
      float f = MathHelper.clamp(lv.nextFloat() * this.pitchRange + this.minPitch, -90.0F, 90.0F);
      float g = MathHelper.wrapDegrees(arg2.getYaw() + 2.0F * lv.nextFloat() * this.maxYaw - this.maxYaw);
      Vec3d lv2 = Vec3d.fromPolar(f, g);
      arg2.getBrain().remember(MemoryModuleType.LOOK_TARGET, (Object)(new BlockPosLookTarget(arg2.getEyePos().add(lv2))));
      arg2.getBrain().remember(MemoryModuleType.GAZE_COOLDOWN_TICKS, (Object)this.cooldown.get(lv));
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (MobEntity)entity, time);
   }
}
