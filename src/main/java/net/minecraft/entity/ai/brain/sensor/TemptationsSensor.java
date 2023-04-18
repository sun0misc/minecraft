package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;

public class TemptationsSensor extends Sensor {
   public static final int MAX_DISTANCE = 10;
   private static final TargetPredicate TEMPTER_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(10.0).ignoreVisibility();
   private final Ingredient ingredient;

   public TemptationsSensor(Ingredient ingredient) {
      this.ingredient = ingredient;
   }

   protected void sense(ServerWorld arg, PathAwareEntity arg2) {
      Brain lv = arg2.getBrain();
      Stream var10000 = arg.getPlayers().stream().filter(EntityPredicates.EXCEPT_SPECTATOR).filter((player) -> {
         return TEMPTER_PREDICATE.test(arg2, player);
      }).filter((player) -> {
         return arg2.isInRange(player, 10.0);
      }).filter(this::test).filter((arg2x) -> {
         return !arg2.hasPassenger(arg2x);
      });
      Objects.requireNonNull(arg2);
      List list = (List)var10000.sorted(Comparator.comparingDouble(arg2::squaredDistanceTo)).collect(Collectors.toList());
      if (!list.isEmpty()) {
         PlayerEntity lv2 = (PlayerEntity)list.get(0);
         lv.remember(MemoryModuleType.TEMPTING_PLAYER, (Object)lv2);
      } else {
         lv.forget(MemoryModuleType.TEMPTING_PLAYER);
      }

   }

   private boolean test(PlayerEntity player) {
      return this.test(player.getMainHandStack()) || this.test(player.getOffHandStack());
   }

   private boolean test(ItemStack stack) {
      return this.ingredient.test(stack);
   }

   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
   }
}
