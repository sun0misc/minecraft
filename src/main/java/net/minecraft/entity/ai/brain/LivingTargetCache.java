/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.Sensor;

public class LivingTargetCache {
    private static final LivingTargetCache EMPTY = new LivingTargetCache();
    private final List<LivingEntity> entities;
    private final Predicate<LivingEntity> targetPredicate;

    private LivingTargetCache() {
        this.entities = List.of();
        this.targetPredicate = entity -> false;
    }

    public LivingTargetCache(LivingEntity owner, List<LivingEntity> entities) {
        this.entities = entities;
        Object2BooleanOpenHashMap object2BooleanOpenHashMap = new Object2BooleanOpenHashMap(entities.size());
        Predicate<LivingEntity> predicate = entity -> Sensor.testTargetPredicate(owner, entity);
        this.targetPredicate = entity -> object2BooleanOpenHashMap.computeIfAbsent(entity, predicate);
    }

    public static LivingTargetCache empty() {
        return EMPTY;
    }

    public Optional<LivingEntity> findFirst(Predicate<LivingEntity> predicate) {
        for (LivingEntity lv : this.entities) {
            if (!predicate.test(lv) || !this.targetPredicate.test(lv)) continue;
            return Optional.of(lv);
        }
        return Optional.empty();
    }

    public Iterable<LivingEntity> iterate(Predicate<LivingEntity> predicate) {
        return Iterables.filter(this.entities, entity -> predicate.test((LivingEntity)entity) && this.targetPredicate.test((LivingEntity)entity));
    }

    public Stream<LivingEntity> stream(Predicate<LivingEntity> predicate) {
        return this.entities.stream().filter(entity -> predicate.test((LivingEntity)entity) && this.targetPredicate.test((LivingEntity)entity));
    }

    public boolean contains(LivingEntity entity) {
        return this.entities.contains(entity) && this.targetPredicate.test(entity);
    }

    public boolean anyMatch(Predicate<LivingEntity> predicate) {
        for (LivingEntity lv : this.entities) {
            if (!predicate.test(lv) || !this.targetPredicate.test(lv)) continue;
            return true;
        }
        return false;
    }
}

