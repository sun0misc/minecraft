package net.minecraft.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Angriness;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class WardenAngerManager {
   @VisibleForTesting
   protected static final int field_38733 = 2;
   @VisibleForTesting
   protected static final int maxAnger = 150;
   private static final int angerDecreasePerTick = 1;
   private int updateTimer = MathHelper.nextBetween(Random.create(), 0, 2);
   int primeAnger;
   private static final Codec SUSPECT_CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(Pair::getFirst), Codecs.NONNEGATIVE_INT.fieldOf("anger").forGetter(Pair::getSecond)).apply(instance, Pair::of);
   });
   private final Predicate suspectPredicate;
   @VisibleForTesting
   protected final ArrayList suspects;
   private final SuspectComparator suspectComparator;
   @VisibleForTesting
   protected final Object2IntMap suspectsToAngerLevel;
   @VisibleForTesting
   protected final Object2IntMap suspectUuidsToAngerLevel;

   public static Codec createCodec(Predicate suspectPredicate) {
      return RecordCodecBuilder.create((instance) -> {
         return instance.group(SUSPECT_CODEC.listOf().fieldOf("suspects").orElse(Collections.emptyList()).forGetter(WardenAngerManager::getSuspects)).apply(instance, (suspectUuidsToAngerLevel) -> {
            return new WardenAngerManager(suspectPredicate, suspectUuidsToAngerLevel);
         });
      });
   }

   public WardenAngerManager(Predicate suspectPredicate, List suspectUuidsToAngerLevel) {
      this.suspectPredicate = suspectPredicate;
      this.suspects = new ArrayList();
      this.suspectComparator = new SuspectComparator(this);
      this.suspectsToAngerLevel = new Object2IntOpenHashMap();
      this.suspectUuidsToAngerLevel = new Object2IntOpenHashMap(suspectUuidsToAngerLevel.size());
      suspectUuidsToAngerLevel.forEach((suspect) -> {
         this.suspectUuidsToAngerLevel.put((UUID)suspect.getFirst(), (Integer)suspect.getSecond());
      });
   }

   private List getSuspects() {
      return (List)Streams.concat(new Stream[]{this.suspects.stream().map((suspect) -> {
         return Pair.of(suspect.getUuid(), this.suspectsToAngerLevel.getInt(suspect));
      }), this.suspectUuidsToAngerLevel.object2IntEntrySet().stream().map((suspect) -> {
         return Pair.of((UUID)suspect.getKey(), suspect.getIntValue());
      })}).collect(Collectors.toList());
   }

   public void tick(ServerWorld world, Predicate suspectPredicate) {
      --this.updateTimer;
      if (this.updateTimer <= 0) {
         this.updateSuspectsMap(world);
         this.updateTimer = 2;
      }

      ObjectIterator objectIterator = this.suspectUuidsToAngerLevel.object2IntEntrySet().iterator();

      while(objectIterator.hasNext()) {
         Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
         int i = entry.getIntValue();
         if (i <= 1) {
            objectIterator.remove();
         } else {
            entry.setValue(i - 1);
         }
      }

      ObjectIterator objectIterator2 = this.suspectsToAngerLevel.object2IntEntrySet().iterator();

      while(true) {
         while(objectIterator2.hasNext()) {
            Object2IntMap.Entry entry2 = (Object2IntMap.Entry)objectIterator2.next();
            int j = entry2.getIntValue();
            Entity lv = (Entity)entry2.getKey();
            Entity.RemovalReason lv2 = lv.getRemovalReason();
            if (j > 1 && suspectPredicate.test(lv) && lv2 == null) {
               entry2.setValue(j - 1);
            } else {
               this.suspects.remove(lv);
               objectIterator2.remove();
               if (j > 1 && lv2 != null) {
                  switch (lv2) {
                     case CHANGED_DIMENSION:
                     case UNLOADED_TO_CHUNK:
                     case UNLOADED_WITH_PLAYER:
                        this.suspectUuidsToAngerLevel.put(lv.getUuid(), j - 1);
                  }
               }
            }
         }

         this.updatePrimeAnger();
         return;
      }
   }

   private void updatePrimeAnger() {
      this.primeAnger = 0;
      this.suspects.sort(this.suspectComparator);
      if (this.suspects.size() == 1) {
         this.primeAnger = this.suspectsToAngerLevel.getInt(this.suspects.get(0));
      }

   }

   private void updateSuspectsMap(ServerWorld world) {
      ObjectIterator objectIterator = this.suspectUuidsToAngerLevel.object2IntEntrySet().iterator();

      while(objectIterator.hasNext()) {
         Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
         int i = entry.getIntValue();
         Entity lv = world.getEntity((UUID)entry.getKey());
         if (lv != null) {
            this.suspectsToAngerLevel.put(lv, i);
            this.suspects.add(lv);
            objectIterator.remove();
         }
      }

   }

   public int increaseAngerAt(Entity entity, int amount) {
      boolean bl = !this.suspectsToAngerLevel.containsKey(entity);
      int j = this.suspectsToAngerLevel.computeInt(entity, (suspect, anger) -> {
         return Math.min(150, (anger == null ? 0 : anger) + amount);
      });
      if (bl) {
         int k = this.suspectUuidsToAngerLevel.removeInt(entity.getUuid());
         j += k;
         this.suspectsToAngerLevel.put(entity, j);
         this.suspects.add(entity);
      }

      this.updatePrimeAnger();
      return j;
   }

   public void removeSuspect(Entity entity) {
      this.suspectsToAngerLevel.removeInt(entity);
      this.suspects.remove(entity);
      this.updatePrimeAnger();
   }

   @Nullable
   private Entity getPrimeSuspectInternal() {
      return (Entity)this.suspects.stream().filter(this.suspectPredicate).findFirst().orElse((Object)null);
   }

   public int getAngerFor(@Nullable Entity entity) {
      return entity == null ? this.primeAnger : this.suspectsToAngerLevel.getInt(entity);
   }

   public Optional getPrimeSuspect() {
      return Optional.ofNullable(this.getPrimeSuspectInternal()).filter((suspect) -> {
         return suspect instanceof LivingEntity;
      }).map((suspect) -> {
         return (LivingEntity)suspect;
      });
   }

   @VisibleForTesting
   protected static record SuspectComparator(WardenAngerManager angerManagement) implements Comparator {
      protected SuspectComparator(WardenAngerManager arg) {
         this.angerManagement = arg;
      }

      public int compare(Entity arg, Entity arg2) {
         if (arg.equals(arg2)) {
            return 0;
         } else {
            int i = this.angerManagement.suspectsToAngerLevel.getOrDefault(arg, 0);
            int j = this.angerManagement.suspectsToAngerLevel.getOrDefault(arg2, 0);
            this.angerManagement.primeAnger = Math.max(this.angerManagement.primeAnger, Math.max(i, j));
            boolean bl = Angriness.getForAnger(i).isAngry();
            boolean bl2 = Angriness.getForAnger(j).isAngry();
            if (bl != bl2) {
               return bl ? -1 : 1;
            } else {
               boolean bl3 = arg instanceof PlayerEntity;
               boolean bl4 = arg2 instanceof PlayerEntity;
               if (bl3 != bl4) {
                  return bl3 ? -1 : 1;
               } else {
                  return Integer.compare(j, i);
               }
            }
         }
      }

      public WardenAngerManager angerManagement() {
         return this.angerManagement;
      }

      // $FF: synthetic method
      public int compare(Object first, Object second) {
         return this.compare((Entity)first, (Entity)second);
      }
   }
}
