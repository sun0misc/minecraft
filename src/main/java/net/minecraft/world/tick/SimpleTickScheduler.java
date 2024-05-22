/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.tick;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.SerializableTickScheduler;
import net.minecraft.world.tick.Tick;

public class SimpleTickScheduler<T>
implements SerializableTickScheduler<T>,
BasicTickScheduler<T> {
    private final List<Tick<T>> scheduledTicks = Lists.newArrayList();
    private final Set<Tick<?>> scheduledTicksSet = new ObjectOpenCustomHashSet(Tick.HASH_STRATEGY);

    @Override
    public void scheduleTick(OrderedTick<T> orderedTick) {
        Tick<T> lv = new Tick<T>(orderedTick.type(), orderedTick.pos(), 0, orderedTick.priority());
        this.scheduleTick(lv);
    }

    @Override
    private void scheduleTick(Tick<T> tick) {
        if (this.scheduledTicksSet.add(tick)) {
            this.scheduledTicks.add(tick);
        }
    }

    @Override
    public boolean isQueued(BlockPos pos, T type) {
        return this.scheduledTicksSet.contains(Tick.create(type, pos));
    }

    @Override
    public int getTickCount() {
        return this.scheduledTicks.size();
    }

    @Override
    public NbtElement toNbt(long time, Function<T, String> typeToNameFunction) {
        NbtList lv = new NbtList();
        for (Tick<T> lv2 : this.scheduledTicks) {
            lv.add(lv2.toNbt(typeToNameFunction));
        }
        return lv;
    }

    public List<Tick<T>> getTicks() {
        return List.copyOf(this.scheduledTicks);
    }

    public static <T> SimpleTickScheduler<T> tick(NbtList tickList, Function<String, Optional<T>> typeToNameFunction, ChunkPos pos) {
        SimpleTickScheduler<T> lv = new SimpleTickScheduler<T>();
        Tick.tick(tickList, typeToNameFunction, pos, lv::scheduleTick);
        return lv;
    }
}

