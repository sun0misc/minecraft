package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class RamImpactTask extends MultiTickTask {
   public static final int RUN_TIME = 200;
   public static final float SPEED_STRENGTH_MULTIPLIER = 1.65F;
   private final Function cooldownRangeFactory;
   private final TargetPredicate targetPredicate;
   private final float speed;
   private final ToDoubleFunction strengthMultiplierFactory;
   private Vec3d direction;
   private final Function impactSoundFactory;
   private final Function hornBreakSoundFactory;

   public RamImpactTask(Function cooldownRangeFactory, TargetPredicate targetPredicate, float speed, ToDoubleFunction strengthMultiplierFactory, Function impactSoundFactory, Function hornBreakSoundFactory) {
      super(ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.RAM_TARGET, MemoryModuleState.VALUE_PRESENT), 200);
      this.cooldownRangeFactory = cooldownRangeFactory;
      this.targetPredicate = targetPredicate;
      this.speed = speed;
      this.strengthMultiplierFactory = strengthMultiplierFactory;
      this.impactSoundFactory = impactSoundFactory;
      this.hornBreakSoundFactory = hornBreakSoundFactory;
      this.direction = Vec3d.ZERO;
   }

   protected boolean shouldRun(ServerWorld arg, GoatEntity arg2) {
      return arg2.getBrain().hasMemoryModule(MemoryModuleType.RAM_TARGET);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, GoatEntity arg2, long l) {
      return arg2.getBrain().hasMemoryModule(MemoryModuleType.RAM_TARGET);
   }

   protected void run(ServerWorld arg, GoatEntity arg2, long l) {
      BlockPos lv = arg2.getBlockPos();
      Brain lv2 = arg2.getBrain();
      Vec3d lv3 = (Vec3d)lv2.getOptionalRegisteredMemory(MemoryModuleType.RAM_TARGET).get();
      this.direction = (new Vec3d((double)lv.getX() - lv3.getX(), 0.0, (double)lv.getZ() - lv3.getZ())).normalize();
      lv2.remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(lv3, this.speed, 0)));
   }

   protected void keepRunning(ServerWorld arg, GoatEntity arg2, long l) {
      List list = arg.getTargets(LivingEntity.class, this.targetPredicate, arg2, arg2.getBoundingBox());
      Brain lv = arg2.getBrain();
      if (!list.isEmpty()) {
         LivingEntity lv2 = (LivingEntity)list.get(0);
         lv2.damage(arg.getDamageSources().mobAttackNoAggro(arg2), (float)arg2.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
         int i = arg2.hasStatusEffect(StatusEffects.SPEED) ? arg2.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1 : 0;
         int j = arg2.hasStatusEffect(StatusEffects.SLOWNESS) ? arg2.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1 : 0;
         float f = 0.25F * (float)(i - j);
         float g = MathHelper.clamp(arg2.getMovementSpeed() * 1.65F, 0.2F, 3.0F) + f;
         float h = lv2.blockedByShield(arg.getDamageSources().mobAttack(arg2)) ? 0.5F : 1.0F;
         lv2.takeKnockback((double)(h * g) * this.strengthMultiplierFactory.applyAsDouble(arg2), this.direction.getX(), this.direction.getZ());
         this.finishRam(arg, arg2);
         arg.playSoundFromEntity((PlayerEntity)null, arg2, (SoundEvent)this.impactSoundFactory.apply(arg2), SoundCategory.NEUTRAL, 1.0F, 1.0F);
      } else if (this.shouldSnapHorn(arg, arg2)) {
         arg.playSoundFromEntity((PlayerEntity)null, arg2, (SoundEvent)this.impactSoundFactory.apply(arg2), SoundCategory.NEUTRAL, 1.0F, 1.0F);
         boolean bl = arg2.dropHorn();
         if (bl) {
            arg.playSoundFromEntity((PlayerEntity)null, arg2, (SoundEvent)this.hornBreakSoundFactory.apply(arg2), SoundCategory.NEUTRAL, 1.0F, 1.0F);
         }

         this.finishRam(arg, arg2);
      } else {
         Optional optional = lv.getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET);
         Optional optional2 = lv.getOptionalRegisteredMemory(MemoryModuleType.RAM_TARGET);
         boolean bl2 = optional.isEmpty() || optional2.isEmpty() || ((WalkTarget)optional.get()).getLookTarget().getPos().isInRange((Position)optional2.get(), 0.25);
         if (bl2) {
            this.finishRam(arg, arg2);
         }
      }

   }

   private boolean shouldSnapHorn(ServerWorld world, GoatEntity goat) {
      Vec3d lv = goat.getVelocity().multiply(1.0, 0.0, 1.0).normalize();
      BlockPos lv2 = BlockPos.ofFloored(goat.getPos().add(lv));
      return world.getBlockState(lv2).isIn(BlockTags.SNAPS_GOAT_HORN) || world.getBlockState(lv2.up()).isIn(BlockTags.SNAPS_GOAT_HORN);
   }

   protected void finishRam(ServerWorld world, GoatEntity goat) {
      world.sendEntityStatus(goat, EntityStatuses.FINISH_RAM);
      goat.getBrain().remember(MemoryModuleType.RAM_COOLDOWN_TICKS, (Object)((UniformIntProvider)this.cooldownRangeFactory.apply(goat)).get(world.random));
      goat.getBrain().forget(MemoryModuleType.RAM_TARGET);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (GoatEntity)entity, time);
   }
}
