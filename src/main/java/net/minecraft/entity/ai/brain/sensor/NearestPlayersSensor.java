package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

public class NearestPlayersSensor extends Sensor {
   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
   }

   protected void sense(ServerWorld world, LivingEntity entity) {
      Stream var10000 = world.getPlayers().stream().filter(EntityPredicates.EXCEPT_SPECTATOR).filter((player) -> {
         return entity.isInRange(player, 16.0);
      });
      Objects.requireNonNull(entity);
      List list = (List)var10000.sorted(Comparator.comparingDouble(entity::squaredDistanceTo)).collect(Collectors.toList());
      Brain lv = entity.getBrain();
      lv.remember(MemoryModuleType.NEAREST_PLAYERS, (Object)list);
      List list2 = (List)list.stream().filter((player) -> {
         return testTargetPredicate(entity, player);
      }).collect(Collectors.toList());
      lv.remember(MemoryModuleType.NEAREST_VISIBLE_PLAYER, (Object)(list2.isEmpty() ? null : (PlayerEntity)list2.get(0)));
      Optional optional = list2.stream().filter((player) -> {
         return testAttackableTargetPredicate(entity, player);
      }).findFirst();
      lv.remember(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, optional);
   }
}
