/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateSupplier;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.BlockStateVariantMap;
import net.minecraft.data.client.PropertiesMap;
import net.minecraft.state.property.Property;
import net.minecraft.util.Util;

public class VariantsBlockStateSupplier
implements BlockStateSupplier {
    private final Block block;
    private final List<BlockStateVariant> variants;
    private final Set<Property<?>> definedProperties = Sets.newHashSet();
    private final List<BlockStateVariantMap> variantMaps = Lists.newArrayList();

    private VariantsBlockStateSupplier(Block block, List<BlockStateVariant> variants) {
        this.block = block;
        this.variants = variants;
    }

    public VariantsBlockStateSupplier coordinate(BlockStateVariantMap map) {
        map.getProperties().forEach(property -> {
            if (this.block.getStateManager().getProperty(property.getName()) != property) {
                throw new IllegalStateException("Property " + String.valueOf(property) + " is not defined for block " + String.valueOf(this.block));
            }
            if (!this.definedProperties.add((Property<?>)property)) {
                throw new IllegalStateException("Values of property " + String.valueOf(property) + " already defined for block " + String.valueOf(this.block));
            }
        });
        this.variantMaps.add(map);
        return this;
    }

    @Override
    public JsonElement get() {
        Stream<Pair<PropertiesMap, List<BlockStateVariant>>> stream = Stream.of(Pair.of(PropertiesMap.empty(), this.variants));
        for (BlockStateVariantMap lv : this.variantMaps) {
            Map<PropertiesMap, List<BlockStateVariant>> map = lv.getVariants();
            stream = stream.flatMap(pair -> map.entrySet().stream().map(entry -> {
                PropertiesMap lv = ((PropertiesMap)pair.getFirst()).copyOf((PropertiesMap)entry.getKey());
                List<BlockStateVariant> list = VariantsBlockStateSupplier.intersect((List)pair.getSecond(), (List)entry.getValue());
                return Pair.of(lv, list);
            }));
        }
        TreeMap map2 = new TreeMap();
        stream.forEach(pair -> map2.put(((PropertiesMap)pair.getFirst()).asString(), BlockStateVariant.toJson((List)pair.getSecond())));
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("variants", Util.make(new JsonObject(), json -> map2.forEach(json::add)));
        return jsonObject;
    }

    private static List<BlockStateVariant> intersect(List<BlockStateVariant> left, List<BlockStateVariant> right) {
        ImmutableList.Builder builder = ImmutableList.builder();
        left.forEach(leftVariant -> right.forEach(rightVariant -> builder.add(BlockStateVariant.union(leftVariant, rightVariant))));
        return builder.build();
    }

    @Override
    public Block getBlock() {
        return this.block;
    }

    public static VariantsBlockStateSupplier create(Block block) {
        return new VariantsBlockStateSupplier(block, ImmutableList.of(BlockStateVariant.create()));
    }

    public static VariantsBlockStateSupplier create(Block block, BlockStateVariant variant) {
        return new VariantsBlockStateSupplier(block, ImmutableList.of(variant));
    }

    public static VariantsBlockStateSupplier create(Block block, BlockStateVariant ... variants) {
        return new VariantsBlockStateSupplier(block, ImmutableList.copyOf(variants));
    }

    @Override
    public /* synthetic */ Object get() {
        return this.get();
    }
}

