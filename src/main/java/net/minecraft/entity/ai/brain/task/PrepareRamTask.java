package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PrepareRamTask extends MultiTickTask {
   public static final int RUN_TIME = 160;
   private final ToIntFunction cooldownFactory;
   private final int minRamDistance;
   private final int maxRamDistance;
   private final float speed;
   private final TargetPredicate targetPredicate;
   private final int prepareTime;
   private final Function soundFactory;
   private Optional prepareStartTime = Optional.empty();
   private Optional ram = Optional.empty();

   public PrepareRamTask(ToIntFunction cooldownFactory, int minDistance, int maxDistance, float speed, TargetPredicate targetPredicate, int prepareTime, Function soundFactory) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.RAM_TARGET, MemoryModuleState.VALUE_ABSENT), 160);
      this.cooldownFactory = cooldownFactory;
      this.minRamDistance = minDistance;
      this.maxRamDistance = maxDistance;
      this.speed = speed;
      this.targetPredicate = targetPredicate;
      this.prepareTime = prepareTime;
      this.soundFactory = soundFactory;
   }

   protected void run(ServerWorld arg, PathAwareEntity arg2, long l) {
      Brain lv = arg2.getBrain();
      lv.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).flatMap((mob) -> {
         return mob.findFirst((mobx) -> {
            return this.targetPredicate.test(arg2, mobx);
         });
      }).ifPresent((mob) -> {
         this.findRam(arg2, mob);
      });
   }

   protected void finishRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
      Brain lv = arg2.getBrain();
      if (!lv.hasMemoryModule(MemoryModuleType.RAM_TARGET)) {
         arg.sendEntityStatus(arg2, EntityStatuses.FINISH_RAM);
         lv.remember(MemoryModuleType.RAM_COOLDOWN_TICKS, (Object)this.cooldownFactory.applyAsInt(arg2));
      }

   }

   protected boolean shouldKeepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
      return this.ram.isPresent() && ((Ram)this.ram.get()).getEntity().isAlive();
   }

   protected void keepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
      if (!this.ram.isEmpty()) {
         arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(((Ram)this.ram.get()).getStart(), this.speed, 0)));
         arg2.getBrain().remember(MemoryModuleType.LOOK_TARGET, (Object)(new EntityLookTarget(((Ram)this.ram.get()).getEntity(), true)));
         boolean bl = !((Ram)this.ram.get()).getEntity().getBlockPos().equals(((Ram)this.ram.get()).getEnd());
         if (bl) {
            arg.sendEntityStatus(arg2, EntityStatuses.FINISH_RAM);
            arg2.getNavigation().stop();
            this.findRam(arg2, ((Ram)this.ram.get()).entity);
         } else {
            BlockPos lv = arg2.getBlockPos();
            if (lv.equals(((Ram)this.ram.get()).getStart())) {
               arg.sendEntityStatus(arg2, EntityStatuses.PREPARE_RAM);
               if (this.prepareStartTime.isEmpty()) {
                  this.prepareStartTime = Optional.of(l);
               }

               if (l - (Long)this.prepareStartTime.get() >= (long)this.prepareTime) {
                  arg2.getBrain().remember(MemoryModuleType.RAM_TARGET, (Object)this.calculateRamTarget(lv, ((Ram)this.ram.get()).getEnd()));
                  arg.playSoundFromEntity((PlayerEntity)null, arg2, (SoundEvent)this.soundFactory.apply(arg2), SoundCategory.NEUTRAL, 1.0F, arg2.getSoundPitch());
                  this.ram = Optional.empty();
               }
            }
         }

      }
   }

   private Vec3d calculateRamTarget(BlockPos start, BlockPos end) {
      double d = 0.5;
      double e = 0.5 * (double)MathHelper.sign((double)(end.getX() - start.getX()));
      double f = 0.5 * (double)MathHelper.sign((double)(end.getZ() - start.getZ()));
      return Vec3d.ofBottomCenter(end).add(e, 0.0, f);
   }

   private Optional findRamStart(PathAwareEntity entity, LivingEntity target) {
      BlockPos lv = target.getBlockPos();
      if (!this.canReach(entity, lv)) {
         return Optional.empty();
      } else {
         List list = Lists.newArrayList();
         BlockPos.Mutable lv2 = lv.mutableCopy();
         Iterator var6 = Direction.Type.HORIZONTAL.iterator();

         while(var6.hasNext()) {
            Direction lv3 = (Direction)var6.next();
            lv2.set(lv);

            for(int i = 0; i < this.maxRamDistance; ++i) {
               if (!this.canReach(entity, lv2.move(lv3))) {
                  lv2.move(lv3.getOpposite());
                  break;
               }
            }

            if (lv2.getManhattanDistance(lv) >= this.minRamDistance) {
               list.add(lv2.toImmutable());
            }
         }

         EntityNavigation lv4 = entity.getNavigation();
         Stream var10000 = list.stream();
         BlockPos var10001 = entity.getBlockPos();
         Objects.requireNonNull(var10001);
         return var10000.sorted(Comparator.comparingDouble(var10001::getSquaredDistance)).filter((start) -> {
            Path lv = lv4.findPathTo((BlockPos)start, 0);
            return lv != null && lv.reachesTarget();
         }).findFirst();
      }
   }

   private boolean canReach(PathAwareEntity entity, BlockPos target) {
      return entity.getNavigation().isValidPosition(target) && entity.getPathfindingPenalty(LandPathNodeMaker.getLandNodeType(entity.world, target.mutableCopy())) == 0.0F;
   }

   private void findRam(PathAwareEntity entity, LivingEntity target) {
      this.prepareStartTime = Optional.empty();
      this.ram = this.findRamStart(entity, target).map((start) -> {
         return new Ram(start, target.getBlockPos(), target);
      });
   }

   // $FF: synthetic method
   protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
      return this.shouldKeepRunning(world, (PathAwareEntity)entity, time);
   }

   // $FF: synthetic method
   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      this.keepRunning(world, (PathAwareEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (PathAwareEntity)entity, time);
   }

   public static class Ram {
      private final BlockPos start;
      private final BlockPos end;
      final LivingEntity entity;

      public Ram(BlockPos start, BlockPos end, LivingEntity entity) {
         this.start = start;
         this.end = end;
         this.entity = entity;
      }

      public BlockPos getStart() {
         return this.start;
      }

      public BlockPos getEnd() {
         return this.end;
      }

      public LivingEntity getEntity() {
         return this.entity;
      }
   }
}
