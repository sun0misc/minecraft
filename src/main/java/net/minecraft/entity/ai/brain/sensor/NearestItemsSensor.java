package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

public class NearestItemsSensor extends Sensor {
   private static final long HORIZONTAL_RANGE = 32L;
   private static final long VERTICAL_RANGE = 16L;
   public static final int MAX_RANGE = 32;

   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
   }

   protected void sense(ServerWorld arg, MobEntity arg2) {
      Brain lv = arg2.getBrain();
      List list = arg.getEntitiesByClass(ItemEntity.class, arg2.getBoundingBox().expand(32.0, 16.0, 32.0), (itemEntity) -> {
         return true;
      });
      Objects.requireNonNull(arg2);
      list.sort(Comparator.comparingDouble(arg2::squaredDistanceTo));
      Stream var10000 = list.stream().filter((itemEntity) -> {
         return arg2.canGather(itemEntity.getStack());
      }).filter((itemEntity) -> {
         return itemEntity.isInRange(arg2, 32.0);
      });
      Objects.requireNonNull(arg2);
      Optional optional = var10000.filter(arg2::canSee).findFirst();
      lv.remember(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, optional);
   }
}
