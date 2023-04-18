package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FindPointOfInterestTask;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

public class NearestBedSensor extends Sensor {
   private static final int REMEMBER_TIME = 40;
   private static final int MAX_TRIES = 5;
   private static final int MAX_EXPIRY_TIME = 20;
   private final Long2LongMap positionToExpiryTime = new Long2LongOpenHashMap();
   private int tries;
   private long expiryTime;

   public NearestBedSensor() {
      super(20);
   }

   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_BED);
   }

   protected void sense(ServerWorld arg, MobEntity arg2) {
      if (arg2.isBaby()) {
         this.tries = 0;
         this.expiryTime = arg.getTime() + (long)arg.getRandom().nextInt(20);
         PointOfInterestStorage lv = arg.getPointOfInterestStorage();
         Predicate predicate = (pos) -> {
            long l = pos.asLong();
            if (this.positionToExpiryTime.containsKey(l)) {
               return false;
            } else if (++this.tries >= 5) {
               return false;
            } else {
               this.positionToExpiryTime.put(l, this.expiryTime + 40L);
               return true;
            }
         };
         Set set = (Set)lv.getTypesAndPositions((argx) -> {
            return argx.matchesKey(PointOfInterestTypes.HOME);
         }, predicate, arg2.getBlockPos(), 48, PointOfInterestStorage.OccupationStatus.ANY).collect(Collectors.toSet());
         Path lv2 = FindPointOfInterestTask.findPathToPoi(arg2, set);
         if (lv2 != null && lv2.reachesTarget()) {
            BlockPos lv3 = lv2.getTarget();
            Optional optional = lv.getType(lv3);
            if (optional.isPresent()) {
               arg2.getBrain().remember(MemoryModuleType.NEAREST_BED, (Object)lv3);
            }
         } else if (this.tries < 5) {
            this.positionToExpiryTime.long2LongEntrySet().removeIf((entry) -> {
               return entry.getLongValue() < this.expiryTime;
            });
         }

      }
   }
}
