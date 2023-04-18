package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.Nullable;

public class FindPointOfInterestTask {
   public static final int POI_SORTING_RADIUS = 48;

   public static Task create(Predicate poiPredicate, MemoryModuleType poiPosModule, boolean onlyRunIfChild, Optional entityStatus) {
      return create(poiPredicate, poiPosModule, poiPosModule, onlyRunIfChild, entityStatus);
   }

   public static Task create(Predicate poiPredicate, MemoryModuleType poiPosModule, MemoryModuleType potentialPoiPosModule, boolean onlyRunIfChild, Optional entityStatus) {
      int i = true;
      int j = true;
      MutableLong mutableLong = new MutableLong(0L);
      Long2ObjectMap long2ObjectMap = new Long2ObjectOpenHashMap();
      SingleTickTask lv = TaskTriggerer.task((arg2) -> {
         return arg2.group(arg2.queryMemoryAbsent(potentialPoiPosModule)).apply(arg2, (queryResult) -> {
            return (world, entity, time) -> {
               if (onlyRunIfChild && entity.isBaby()) {
                  return false;
               } else if (mutableLong.getValue() == 0L) {
                  mutableLong.setValue(world.getTime() + (long)world.random.nextInt(20));
                  return false;
               } else if (world.getTime() < mutableLong.getValue()) {
                  return false;
               } else {
                  mutableLong.setValue(time + 20L + (long)world.getRandom().nextInt(20));
                  PointOfInterestStorage lv = world.getPointOfInterestStorage();
                  long2ObjectMap.long2ObjectEntrySet().removeIf((entry) -> {
                     return !((RetryMarker)entry.getValue()).isAttempting(time);
                  });
                  Predicate predicate2 = (pos) -> {
                     RetryMarker lv = (RetryMarker)long2ObjectMap.get(pos.asLong());
                     if (lv == null) {
                        return true;
                     } else if (!lv.shouldRetry(time)) {
                        return false;
                     } else {
                        lv.setAttemptTime(time);
                        return true;
                     }
                  };
                  Set set = (Set)lv.getSortedTypesAndPositions(poiPredicate, predicate2, entity.getBlockPos(), 48, PointOfInterestStorage.OccupationStatus.HAS_SPACE).limit(5L).collect(Collectors.toSet());
                  Path lv2 = findPathToPoi(entity, set);
                  if (lv2 != null && lv2.reachesTarget()) {
                     BlockPos lv3 = lv2.getTarget();
                     lv.getType(lv3).ifPresent((poiType) -> {
                        lv.getPosition(poiPredicate, (arg2, arg3) -> {
                           return arg3.equals(lv3);
                        }, lv3, 1);
                        queryResult.remember((Object)GlobalPos.create(world.getRegistryKey(), lv3));
                        entityStatus.ifPresent((status) -> {
                           world.sendEntityStatus(entity, status);
                        });
                        long2ObjectMap.clear();
                        DebugInfoSender.sendPointOfInterest(world, lv3);
                     });
                  } else {
                     Iterator var14 = set.iterator();

                     while(var14.hasNext()) {
                        Pair pair = (Pair)var14.next();
                        long2ObjectMap.computeIfAbsent(((BlockPos)pair.getSecond()).asLong(), (m) -> {
                           return new RetryMarker(world.random, time);
                        });
                     }
                  }

                  return true;
               }
            };
         });
      });
      return potentialPoiPosModule == poiPosModule ? lv : TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(poiPosModule)).apply(context, (poiPos) -> {
            return lv;
         });
      });
   }

   @Nullable
   public static Path findPathToPoi(MobEntity entity, Set pois) {
      if (pois.isEmpty()) {
         return null;
      } else {
         Set set2 = new HashSet();
         int i = 1;
         Iterator var4 = pois.iterator();

         while(var4.hasNext()) {
            Pair pair = (Pair)var4.next();
            i = Math.max(i, ((PointOfInterestType)((RegistryEntry)pair.getFirst()).value()).searchDistance());
            set2.add((BlockPos)pair.getSecond());
         }

         return entity.getNavigation().findPathTo((Set)set2, i);
      }
   }

   private static class RetryMarker {
      private static final int MIN_DELAY = 40;
      private static final int MAX_EXTRA_DELAY = 80;
      private static final int ATTEMPT_DURATION = 400;
      private final Random random;
      private long previousAttemptAt;
      private long nextScheduledAttemptAt;
      private int currentDelay;

      RetryMarker(Random random, long time) {
         this.random = random;
         this.setAttemptTime(time);
      }

      public void setAttemptTime(long time) {
         this.previousAttemptAt = time;
         int i = this.currentDelay + this.random.nextInt(40) + 40;
         this.currentDelay = Math.min(i, 400);
         this.nextScheduledAttemptAt = time + (long)this.currentDelay;
      }

      public boolean isAttempting(long time) {
         return time - this.previousAttemptAt < 400L;
      }

      public boolean shouldRetry(long time) {
         return time >= this.nextScheduledAttemptAt;
      }

      public String toString() {
         return "RetryMarker{, previousAttemptAt=" + this.previousAttemptAt + ", nextScheduledAttemptAt=" + this.nextScheduledAttemptAt + ", currentDelay=" + this.currentDelay + "}";
      }
   }
}
