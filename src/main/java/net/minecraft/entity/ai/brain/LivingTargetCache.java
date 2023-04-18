package net.minecraft.entity.ai.brain;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.Sensor;

public class LivingTargetCache {
   private static final LivingTargetCache EMPTY = new LivingTargetCache();
   private final List entities;
   private final Predicate targetPredicate;

   private LivingTargetCache() {
      this.entities = List.of();
      this.targetPredicate = (entity) -> {
         return false;
      };
   }

   public LivingTargetCache(LivingEntity owner, List entities) {
      this.entities = entities;
      Object2BooleanOpenHashMap object2BooleanOpenHashMap = new Object2BooleanOpenHashMap(entities.size());
      Predicate predicate = (entity) -> {
         return Sensor.testTargetPredicate(owner, entity);
      };
      this.targetPredicate = (entity) -> {
         return object2BooleanOpenHashMap.computeIfAbsent(entity, predicate);
      };
   }

   public static LivingTargetCache empty() {
      return EMPTY;
   }

   public Optional findFirst(Predicate predicate) {
      Iterator var2 = this.entities.iterator();

      LivingEntity lv;
      do {
         if (!var2.hasNext()) {
            return Optional.empty();
         }

         lv = (LivingEntity)var2.next();
      } while(!predicate.test(lv) || !this.targetPredicate.test(lv));

      return Optional.of(lv);
   }

   public Iterable iterate(Predicate predicate) {
      return Iterables.filter(this.entities, (entity) -> {
         return predicate.test(entity) && this.targetPredicate.test(entity);
      });
   }

   public Stream stream(Predicate predicate) {
      return this.entities.stream().filter((entity) -> {
         return predicate.test(entity) && this.targetPredicate.test(entity);
      });
   }

   public boolean contains(LivingEntity entity) {
      return this.entities.contains(entity) && this.targetPredicate.test(entity);
   }

   public boolean anyMatch(Predicate predicate) {
      Iterator var2 = this.entities.iterator();

      LivingEntity lv;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         lv = (LivingEntity)var2.next();
      } while(!predicate.test(lv) || !this.targetPredicate.test(lv));

      return true;
   }
}
