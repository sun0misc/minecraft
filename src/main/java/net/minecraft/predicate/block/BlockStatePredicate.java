/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.predicate.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

public class BlockStatePredicate
implements Predicate<BlockState> {
    public static final Predicate<BlockState> ANY = state -> true;
    private final StateManager<Block, BlockState> manager;
    private final Map<Property<?>, Predicate<Object>> propertyTests = Maps.newHashMap();

    private BlockStatePredicate(StateManager<Block, BlockState> manager) {
        this.manager = manager;
    }

    public static BlockStatePredicate forBlock(Block block) {
        return new BlockStatePredicate(block.getStateManager());
    }

    @Override
    public boolean test(@Nullable BlockState arg) {
        if (arg == null || !arg.getBlock().equals(this.manager.getOwner())) {
            return false;
        }
        if (this.propertyTests.isEmpty()) {
            return true;
        }
        for (Map.Entry<Property<?>, Predicate<Object>> entry : this.propertyTests.entrySet()) {
            if (this.testProperty(arg, entry.getKey(), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    protected <T extends Comparable<T>> boolean testProperty(BlockState blockState, Property<T> property, Predicate<Object> predicate) {
        T comparable = blockState.get(property);
        return predicate.test(comparable);
    }

    public <V extends Comparable<V>> BlockStatePredicate with(Property<V> property, Predicate<Object> predicate) {
        if (!this.manager.getProperties().contains(property)) {
            throw new IllegalArgumentException(String.valueOf(this.manager) + " cannot support property " + String.valueOf(property));
        }
        this.propertyTests.put(property, predicate);
        return this;
    }

    @Override
    public /* synthetic */ boolean test(@Nullable Object state) {
        return this.test((BlockState)state);
    }
}

