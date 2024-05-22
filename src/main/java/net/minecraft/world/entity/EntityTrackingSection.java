/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.entity;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingStatus;
import org.slf4j.Logger;

public class EntityTrackingSection<T extends EntityLike> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final TypeFilterableList<T> collection;
    private EntityTrackingStatus status;

    public EntityTrackingSection(Class<T> entityClass, EntityTrackingStatus status) {
        this.status = status;
        this.collection = new TypeFilterableList<T>(entityClass);
    }

    public void add(T entity) {
        this.collection.add(entity);
    }

    public boolean remove(T entity) {
        return this.collection.remove(entity);
    }

    public LazyIterationConsumer.NextIteration forEach(Box box, LazyIterationConsumer<T> consumer) {
        for (EntityLike lv : this.collection) {
            if (!lv.getBoundingBox().intersects(box) || !consumer.accept(lv).shouldAbort()) continue;
            return LazyIterationConsumer.NextIteration.ABORT;
        }
        return LazyIterationConsumer.NextIteration.CONTINUE;
    }

    public <U extends T> LazyIterationConsumer.NextIteration forEach(TypeFilter<T, U> type, Box box, LazyIterationConsumer<? super U> consumer) {
        Collection<T> collection = this.collection.getAllOfType(type.getBaseClass());
        if (collection.isEmpty()) {
            return LazyIterationConsumer.NextIteration.CONTINUE;
        }
        for (EntityLike lv : collection) {
            EntityLike lv2 = (EntityLike)type.downcast(lv);
            if (lv2 == null || !lv.getBoundingBox().intersects(box) || !consumer.accept(lv2).shouldAbort()) continue;
            return LazyIterationConsumer.NextIteration.ABORT;
        }
        return LazyIterationConsumer.NextIteration.CONTINUE;
    }

    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    public Stream<T> stream() {
        return this.collection.stream();
    }

    public EntityTrackingStatus getStatus() {
        return this.status;
    }

    public EntityTrackingStatus swapStatus(EntityTrackingStatus status) {
        EntityTrackingStatus lv = this.status;
        this.status = status;
        return lv;
    }

    @Debug
    public int size() {
        return this.collection.size();
    }
}

