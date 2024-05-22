/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.chunk;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9799;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class BlockBufferBuilderStorage
implements AutoCloseable {
    private static final List<RenderLayer> field_52159 = RenderLayer.getBlockLayers();
    public static final int EXPECTED_TOTAL_SIZE = field_52159.stream().mapToInt(RenderLayer::getExpectedBufferSize).sum();
    private final Map<RenderLayer, class_9799> builders = Util.make(new Reference2ObjectArrayMap(field_52159.size()), reference2ObjectArrayMap -> {
        for (RenderLayer lv : field_52159) {
            reference2ObjectArrayMap.put(lv, new class_9799(lv.getExpectedBufferSize()));
        }
    });

    public class_9799 get(RenderLayer layer) {
        return this.builders.get(layer);
    }

    public void clear() {
        this.builders.values().forEach(class_9799::method_60809);
    }

    public void reset() {
        this.builders.values().forEach(class_9799::method_60811);
    }

    @Override
    public void close() {
        this.builders.values().forEach(class_9799::close);
    }
}

