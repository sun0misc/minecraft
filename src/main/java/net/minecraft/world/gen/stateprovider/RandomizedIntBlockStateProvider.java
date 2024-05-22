/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.stateprovider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
import org.jetbrains.annotations.Nullable;

public class RandomizedIntBlockStateProvider
extends BlockStateProvider {
    public static final MapCodec<RandomizedIntBlockStateProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("source")).forGetter(arg -> arg.source), ((MapCodec)Codec.STRING.fieldOf("property")).forGetter(arg -> arg.propertyName), ((MapCodec)IntProvider.VALUE_CODEC.fieldOf("values")).forGetter(arg -> arg.values)).apply((Applicative<RandomizedIntBlockStateProvider, ?>)instance, RandomizedIntBlockStateProvider::new));
    private final BlockStateProvider source;
    private final String propertyName;
    @Nullable
    private IntProperty property;
    private final IntProvider values;

    public RandomizedIntBlockStateProvider(BlockStateProvider source, IntProperty property, IntProvider values) {
        this.source = source;
        this.property = property;
        this.propertyName = property.getName();
        this.values = values;
        Collection<Integer> collection = property.getValues();
        for (int i = values.getMin(); i <= values.getMax(); ++i) {
            if (collection.contains(i)) continue;
            throw new IllegalArgumentException("Property value out of range: " + property.getName() + ": " + i);
        }
    }

    public RandomizedIntBlockStateProvider(BlockStateProvider source, String propertyName, IntProvider values) {
        this.source = source;
        this.propertyName = propertyName;
        this.values = values;
    }

    @Override
    protected BlockStateProviderType<?> getType() {
        return BlockStateProviderType.RANDOMIZED_INT_STATE_PROVIDER;
    }

    @Override
    public BlockState get(Random random, BlockPos pos) {
        BlockState lv = this.source.get(random, pos);
        if (this.property == null || !lv.contains(this.property)) {
            IntProperty lv2 = RandomizedIntBlockStateProvider.getIntPropertyByName(lv, this.propertyName);
            if (lv2 == null) {
                return lv;
            }
            this.property = lv2;
        }
        return (BlockState)lv.with(this.property, this.values.get(random));
    }

    @Nullable
    private static IntProperty getIntPropertyByName(BlockState state, String propertyName) {
        Collection<Property<?>> collection = state.getProperties();
        Optional<IntProperty> optional = collection.stream().filter(property -> property.getName().equals(propertyName)).filter(property -> property instanceof IntProperty).map(property -> (IntProperty)property).findAny();
        return optional.orElse(null);
    }
}

