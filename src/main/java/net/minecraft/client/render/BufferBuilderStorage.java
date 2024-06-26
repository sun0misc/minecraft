/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9799;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.chunk.BlockBufferBuilderPool;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class BufferBuilderStorage {
    private final BlockBufferBuilderStorage blockBufferBuilders = new BlockBufferBuilderStorage();
    private final BlockBufferBuilderPool blockBufferBuildersPool;
    private final VertexConsumerProvider.Immediate entityVertexConsumers;
    private final VertexConsumerProvider.Immediate effectVertexConsumers;
    private final OutlineVertexConsumerProvider outlineVertexConsumers;

    public BufferBuilderStorage(int maxBlockBuildersPoolSize) {
        this.blockBufferBuildersPool = BlockBufferBuilderPool.allocate(maxBlockBuildersPoolSize);
        SortedMap sortedMap = Util.make(new Object2ObjectLinkedOpenHashMap(), map -> {
            map.put(TexturedRenderLayers.getEntitySolid(), this.blockBufferBuilders.get(RenderLayer.getSolid()));
            map.put(TexturedRenderLayers.getEntityCutout(), this.blockBufferBuilders.get(RenderLayer.getCutout()));
            map.put(TexturedRenderLayers.getBannerPatterns(), this.blockBufferBuilders.get(RenderLayer.getCutoutMipped()));
            map.put(TexturedRenderLayers.getEntityTranslucentCull(), this.blockBufferBuilders.get(RenderLayer.getTranslucent()));
            BufferBuilderStorage.assignBufferBuilder(map, TexturedRenderLayers.getShieldPatterns());
            BufferBuilderStorage.assignBufferBuilder(map, TexturedRenderLayers.getBeds());
            BufferBuilderStorage.assignBufferBuilder(map, TexturedRenderLayers.getShulkerBoxes());
            BufferBuilderStorage.assignBufferBuilder(map, TexturedRenderLayers.getSign());
            BufferBuilderStorage.assignBufferBuilder(map, TexturedRenderLayers.getHangingSign());
            map.put(TexturedRenderLayers.getChest(), new class_9799(786432));
            BufferBuilderStorage.assignBufferBuilder(map, RenderLayer.getArmorEntityGlint());
            BufferBuilderStorage.assignBufferBuilder(map, RenderLayer.getGlint());
            BufferBuilderStorage.assignBufferBuilder(map, RenderLayer.getGlintTranslucent());
            BufferBuilderStorage.assignBufferBuilder(map, RenderLayer.getEntityGlint());
            BufferBuilderStorage.assignBufferBuilder(map, RenderLayer.getDirectEntityGlint());
            BufferBuilderStorage.assignBufferBuilder(map, RenderLayer.getWaterMask());
            ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.forEach(renderLayer -> BufferBuilderStorage.assignBufferBuilder(map, renderLayer));
        });
        this.effectVertexConsumers = VertexConsumerProvider.immediate(new class_9799(1536));
        this.entityVertexConsumers = VertexConsumerProvider.immediate(sortedMap, new class_9799(786432));
        this.outlineVertexConsumers = new OutlineVertexConsumerProvider(this.entityVertexConsumers);
    }

    private static void assignBufferBuilder(Object2ObjectLinkedOpenHashMap<RenderLayer, class_9799> builderStorage, RenderLayer layer) {
        builderStorage.put(layer, new class_9799(layer.getExpectedBufferSize()));
    }

    public BlockBufferBuilderStorage getBlockBufferBuilders() {
        return this.blockBufferBuilders;
    }

    public BlockBufferBuilderPool getBlockBufferBuildersPool() {
        return this.blockBufferBuildersPool;
    }

    public VertexConsumerProvider.Immediate getEntityVertexConsumers() {
        return this.entityVertexConsumers;
    }

    public VertexConsumerProvider.Immediate getEffectVertexConsumers() {
        return this.effectVertexConsumers;
    }

    public OutlineVertexConsumerProvider getOutlineVertexConsumers() {
        return this.outlineVertexConsumers;
    }
}

