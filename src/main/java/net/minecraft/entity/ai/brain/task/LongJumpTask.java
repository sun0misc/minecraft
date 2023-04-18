package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LongJumpTask extends MultiTickTask {
   protected static final int MAX_COOLDOWN = 20;
   private static final int TARGET_RETAIN_TIME = 40;
   protected static final int PATHING_DISTANCE = 8;
   private static final int RUN_TIME = 200;
   private static final List RAM_RANGES = Lists.newArrayList(new Integer[]{65, 70, 75, 80});
   private final UniformIntProvider cooldownRange;
   protected final int verticalRange;
   protected final int horizontalRange;
   protected final float maxRange;
   protected List targets;
   protected Optional lastPos;
   @Nullable
   protected Vec3d lastTarget;
   protected int cooldown;
   protected long targetTime;
   private final Function entityToSound;
   private final BiPredicate jumpToPredicate;

   public LongJumpTask(UniformIntProvider cooldownRange, int verticalRange, int horizontalRange, float maxRange, Function entityToSound) {
      this(cooldownRange, verticalRange, horizontalRange, maxRange, entityToSound, LongJumpTask::shouldJumpTo);
   }

   public static boolean shouldJumpTo(MobEntity entity, BlockPos pos) {
      World lv = entity.world;
      BlockPos lv2 = pos.down();
      return lv.getBlockState(lv2).isOpaqueFullCube(lv, lv2) && entity.getPathfindingPenalty(LandPathNodeMaker.getLandNodeType(lv, pos.mutableCopy())) == 0.0F;
   }

   public LongJumpTask(UniformIntProvider cooldownRange, int verticalRange, int horizontalRange, float maxRange, Function entityToSound, BiPredicate jumpToPredicate) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleState.VALUE_ABSENT), 200);
      this.targets = Lists.newArrayList();
      this.lastPos = Optional.empty();
      this.cooldownRange = cooldownRange;
      this.verticalRange = verticalRange;
      this.horizontalRange = horizontalRange;
      this.maxRange = maxRange;
      this.entityToSound = entityToSound;
      this.jumpToPredicate = jumpToPredicate;
   }

   protected boolean shouldRun(ServerWorld arg, MobEntity arg2) {
      boolean bl = arg2.isOnGround() && !arg2.isTouchingWater() && !arg2.isInLava() && !arg.getBlockState(arg2.getBlockPos()).isOf(Blocks.HONEY_BLOCK);
      if (!bl) {
         arg2.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, (Object)(this.cooldownRange.get(arg.random) / 2));
      }

      return bl;
   }

   protected boolean shouldKeepRunning(ServerWorld arg, MobEntity arg2, long l) {
      boolean bl = this.lastPos.isPresent() && ((Vec3d)this.lastPos.get()).equals(arg2.getPos()) && this.cooldown > 0 && !arg2.isInsideWaterOrBubbleColumn() && (this.lastTarget != null || !this.targets.isEmpty());
      if (!bl && arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
         arg2.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, (Object)(this.cooldownRange.get(arg.random) / 2));
         arg2.getBrain().forget(MemoryModuleType.LOOK_TARGET);
      }

      return bl;
   }

   protected void run(ServerWorld arg, MobEntity arg2, long l) {
      this.lastTarget = null;
      this.cooldown = 20;
      this.lastPos = Optional.of(arg2.getPos());
      BlockPos lv = arg2.getBlockPos();
      int i = lv.getX();
      int j = lv.getY();
      int k = lv.getZ();
      this.targets = (List)BlockPos.stream(i - this.horizontalRange, j - this.verticalRange, k - this.horizontalRange, i + this.horizontalRange, j + this.verticalRange, k + this.horizontalRange).filter((arg2x) -> {
         return !arg2x.equals(lv);
      }).map((arg2x) -> {
         return new Target(arg2x.toImmutable(), MathHelper.ceil(lv.getSquaredDistance(arg2x)));
      }).collect(Collectors.toCollection(Lists::newArrayList));
   }

   protected void keepRunning(ServerWorld arg, MobEntity arg2, long l) {
      if (this.lastTarget != null) {
         if (l - this.targetTime >= 40L) {
            arg2.setYaw(arg2.bodyYaw);
            arg2.setNoDrag(true);
            double d = this.lastTarget.length();
            double e = d + arg2.getJumpBoostVelocityModifier();
            arg2.setVelocity(this.lastTarget.multiply(e / d));
            arg2.getBrain().remember(MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)true);
            arg.playSoundFromEntity((PlayerEntity)null, arg2, (SoundEvent)this.entityToSound.apply(arg2), SoundCategory.NEUTRAL, 1.0F, 1.0F);
         }
      } else {
         --this.cooldown;
         this.findTarget(arg, arg2, l);
      }

   }

   protected void findTarget(ServerWorld world, MobEntity entity, long time) {
      while(true) {
         if (!this.targets.isEmpty()) {
            Optional optional = this.getTarget(world);
            if (optional.isEmpty()) {
               continue;
            }

            Target lv = (Target)optional.get();
            BlockPos lv2 = lv.getPos();
            if (!this.canJumpTo(world, entity, lv2)) {
               continue;
            }

            Vec3d lv3 = Vec3d.ofCenter(lv2);
            Vec3d lv4 = this.getJumpingVelocity(entity, lv3);
            if (lv4 == null) {
               continue;
            }

            entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, (Object)(new BlockPosLookTarget(lv2)));
            EntityNavigation lv5 = entity.getNavigation();
            Path lv6 = lv5.findPathTo(lv2, 0, 8);
            if (lv6 != null && lv6.reachesTarget()) {
               continue;
            }

            this.lastTarget = lv4;
            this.targetTime = time;
            return;
         }

         return;
      }
   }

   protected Optional getTarget(ServerWorld world) {
      Optional optional = Weighting.getRandom(world.random, this.targets);
      List var10001 = this.targets;
      Objects.requireNonNull(var10001);
      optional.ifPresent(var10001::remove);
      return optional;
   }

   private boolean canJumpTo(ServerWorld world, MobEntity entity, BlockPos pos) {
      BlockPos lv = entity.getBlockPos();
      int i = lv.getX();
      int j = lv.getZ();
      return i == pos.getX() && j == pos.getZ() ? false : this.jumpToPredicate.test(entity, pos);
   }

   @Nullable
   protected Vec3d getJumpingVelocity(MobEntity entity, Vec3d pos) {
      List list = Lists.newArrayList(RAM_RANGES);
      Collections.shuffle(list);
      Iterator var4 = list.iterator();

      Vec3d lv;
      do {
         if (!var4.hasNext()) {
            return null;
         }

         int i = (Integer)var4.next();
         lv = this.getJumpingVelocity(entity, pos, i);
      } while(lv == null);

      return lv;
   }

   @Nullable
   private Vec3d getJumpingVelocity(MobEntity entity, Vec3d pos, int range) {
      Vec3d lv = entity.getPos();
      Vec3d lv2 = (new Vec3d(pos.x - lv.x, 0.0, pos.z - lv.z)).normalize().multiply(0.5);
      pos = pos.subtract(lv2);
      Vec3d lv3 = pos.subtract(lv);
      float f = (float)range * 3.1415927F / 180.0F;
      double d = Math.atan2(lv3.z, lv3.x);
      double e = lv3.subtract(0.0, lv3.y, 0.0).lengthSquared();
      double g = Math.sqrt(e);
      double h = lv3.y;
      double j = Math.sin((double)(2.0F * f));
      double k = 0.08;
      double l = Math.pow(Math.cos((double)f), 2.0);
      double m = Math.sin((double)f);
      double n = Math.cos((double)f);
      double o = Math.sin(d);
      double p = Math.cos(d);
      double q = e * 0.08 / (g * j - 2.0 * h * l);
      if (q < 0.0) {
         return null;
      } else {
         double r = Math.sqrt(q);
         if (r > (double)this.maxRange) {
            return null;
         } else {
            double s = r * n;
            double t = r * m;
            int u = MathHelper.ceil(g / s) * 2;
            double v = 0.0;
            Vec3d lv4 = null;
            EntityDimensions lv5 = entity.getDimensions(EntityPose.LONG_JUMPING);

            for(int w = 0; w < u - 1; ++w) {
               v += g / (double)u;
               double x = m / n * v - Math.pow(v, 2.0) * 0.08 / (2.0 * q * Math.pow(n, 2.0));
               double y = v * p;
               double z = v * o;
               Vec3d lv6 = new Vec3d(lv.x + y, lv.y + x, lv.z + z);
               if (lv4 != null && !this.canReach(entity, lv5, lv4, lv6)) {
                  return null;
               }

               lv4 = lv6;
            }

            return (new Vec3d(s * p, t, s * o)).multiply(0.949999988079071);
         }
      }
   }

   private boolean canReach(MobEntity entity, EntityDimensions dimensions, Vec3d arg3, Vec3d arg4) {
      Vec3d lv = arg4.subtract(arg3);
      double d = (double)Math.min(dimensions.width, dimensions.height);
      int i = MathHelper.ceil(lv.length() / d);
      Vec3d lv2 = lv.normalize();
      Vec3d lv3 = arg3;

      for(int j = 0; j < i; ++j) {
         lv3 = j == i - 1 ? arg4 : lv3.add(lv2.multiply(d * 0.8999999761581421));
         if (!entity.world.isSpaceEmpty(entity, dimensions.getBoxAt(lv3))) {
            return false;
         }
      }

      return true;
   }

   // $FF: synthetic method
   protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
      return this.shouldKeepRunning(world, (MobEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (MobEntity)entity, time);
   }

   public static class Target extends Weighted.Absent {
      private final BlockPos pos;

      public Target(BlockPos pos, int weight) {
         super(weight);
         this.pos = pos;
      }

      public BlockPos getPos() {
         return this.pos;
      }
   }
}
