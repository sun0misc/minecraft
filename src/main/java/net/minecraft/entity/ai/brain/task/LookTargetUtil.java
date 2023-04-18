package net.minecraft.entity.ai.brain.task;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class LookTargetUtil {
   private LookTargetUtil() {
   }

   public static void lookAtAndWalkTowardsEachOther(LivingEntity first, LivingEntity second, float speed) {
      lookAtEachOther(first, second);
      walkTowardsEachOther(first, second, speed);
   }

   public static boolean canSee(Brain brain, LivingEntity target) {
      Optional optional = brain.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS);
      return optional.isPresent() && ((LivingTargetCache)optional.get()).contains(target);
   }

   public static boolean canSee(Brain brain, MemoryModuleType memoryModuleType, EntityType entityType) {
      return canSee(brain, memoryModuleType, (entity) -> {
         return entity.getType() == entityType;
      });
   }

   private static boolean canSee(Brain brain, MemoryModuleType memoryType, Predicate filter) {
      return brain.getOptionalRegisteredMemory(memoryType).filter(filter).filter(LivingEntity::isAlive).filter((target) -> {
         return canSee(brain, target);
      }).isPresent();
   }

   private static void lookAtEachOther(LivingEntity first, LivingEntity second) {
      lookAt(first, second);
      lookAt(second, first);
   }

   public static void lookAt(LivingEntity entity, LivingEntity target) {
      entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, (Object)(new EntityLookTarget(target, true)));
   }

   private static void walkTowardsEachOther(LivingEntity first, LivingEntity second, float speed) {
      int i = true;
      walkTowards(first, (Entity)second, speed, 2);
      walkTowards(second, (Entity)first, speed, 2);
   }

   public static void walkTowards(LivingEntity entity, Entity target, float speed, int completionRange) {
      walkTowards(entity, (LookTarget)(new EntityLookTarget(target, true)), speed, completionRange);
   }

   public static void walkTowards(LivingEntity entity, BlockPos target, float speed, int completionRange) {
      walkTowards(entity, (LookTarget)(new BlockPosLookTarget(target)), speed, completionRange);
   }

   public static void walkTowards(LivingEntity entity, LookTarget target, float speed, int completionRange) {
      WalkTarget lv = new WalkTarget(target, speed, completionRange);
      entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, (Object)target);
      entity.getBrain().remember(MemoryModuleType.WALK_TARGET, (Object)lv);
   }

   public static void give(LivingEntity entity, ItemStack stack, Vec3d targetLocation) {
      Vec3d lv = new Vec3d(0.30000001192092896, 0.30000001192092896, 0.30000001192092896);
      give(entity, stack, targetLocation, lv, 0.3F);
   }

   public static void give(LivingEntity entity, ItemStack stack, Vec3d targetLocation, Vec3d velocityFactor, float yOffset) {
      double d = entity.getEyeY() - (double)yOffset;
      ItemEntity lv = new ItemEntity(entity.world, entity.getX(), d, entity.getZ(), stack);
      lv.setThrower(entity.getUuid());
      Vec3d lv2 = targetLocation.subtract(entity.getPos());
      lv2 = lv2.normalize().multiply(velocityFactor.x, velocityFactor.y, velocityFactor.z);
      lv.setVelocity(lv2);
      lv.setToDefaultPickupDelay();
      entity.world.spawnEntity(lv);
   }

   public static ChunkSectionPos getPosClosestToOccupiedPointOfInterest(ServerWorld world, ChunkSectionPos center, int radius) {
      int j = world.getOccupiedPointOfInterestDistance(center);
      Stream var10000 = ChunkSectionPos.stream(center, radius).filter((sectionPos) -> {
         return world.getOccupiedPointOfInterestDistance(sectionPos) < j;
      });
      Objects.requireNonNull(world);
      return (ChunkSectionPos)var10000.min(Comparator.comparingInt(world::getOccupiedPointOfInterestDistance)).orElse(center);
   }

   public static boolean isTargetWithinAttackRange(MobEntity mob, LivingEntity target, int rangedWeaponReachReduction) {
      Item var4 = mob.getMainHandStack().getItem();
      if (var4 instanceof RangedWeaponItem lv) {
         if (mob.canUseRangedWeapon(lv)) {
            int j = lv.getRange() - rangedWeaponReachReduction;
            return mob.isInRange(target, (double)j);
         }
      }

      return mob.isInAttackRange(target);
   }

   public static boolean isNewTargetTooFar(LivingEntity source, LivingEntity target, double extraDistance) {
      Optional optional = source.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
      if (optional.isEmpty()) {
         return false;
      } else {
         double e = source.squaredDistanceTo(((LivingEntity)optional.get()).getPos());
         double f = source.squaredDistanceTo(target.getPos());
         return f > e + extraDistance * extraDistance;
      }
   }

   public static boolean isVisibleInMemory(LivingEntity source, LivingEntity target) {
      Brain lv = source.getBrain();
      return !lv.hasMemoryModule(MemoryModuleType.VISIBLE_MOBS) ? false : ((LivingTargetCache)lv.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).get()).contains(target);
   }

   public static LivingEntity getCloserEntity(LivingEntity source, Optional first, LivingEntity second) {
      return first.isEmpty() ? second : getCloserEntity(source, (LivingEntity)first.get(), second);
   }

   public static LivingEntity getCloserEntity(LivingEntity source, LivingEntity first, LivingEntity second) {
      Vec3d lv = first.getPos();
      Vec3d lv2 = second.getPos();
      return source.squaredDistanceTo(lv) < source.squaredDistanceTo(lv2) ? first : second;
   }

   public static Optional getEntity(LivingEntity entity, MemoryModuleType uuidMemoryModule) {
      Optional optional = entity.getBrain().getOptionalRegisteredMemory(uuidMemoryModule);
      return optional.map((uuid) -> {
         return ((ServerWorld)entity.world).getEntity(uuid);
      }).map((target) -> {
         LivingEntity var10000;
         if (target instanceof LivingEntity lv) {
            var10000 = lv;
         } else {
            var10000 = null;
         }

         return var10000;
      });
   }

   @Nullable
   public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange) {
      Vec3d lv = NoPenaltyTargeting.find(entity, horizontalRange, verticalRange);

      for(int k = 0; lv != null && !entity.world.getBlockState(BlockPos.ofFloored(lv)).canPathfindThrough(entity.world, BlockPos.ofFloored(lv), NavigationType.WATER) && k++ < 10; lv = NoPenaltyTargeting.find(entity, horizontalRange, verticalRange)) {
      }

      return lv;
   }

   public static boolean hasBreedTarget(LivingEntity entity) {
      return entity.getBrain().hasMemoryModule(MemoryModuleType.BREED_TARGET);
   }
}
