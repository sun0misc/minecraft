package net.minecraft.client.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class BufferBuilderStorage {
   private final BlockBufferBuilderStorage blockBuilders = new BlockBufferBuilderStorage();
   private final SortedMap entityBuilders = (SortedMap)Util.make(new Object2ObjectLinkedOpenHashMap(), (map) -> {
      map.put(TexturedRenderLayers.getEntitySolid(), this.blockBuilders.get(RenderLayer.getSolid()));
      map.put(TexturedRenderLayers.getEntityCutout(), this.blockBuilders.get(RenderLayer.getCutout()));
      map.put(TexturedRenderLayers.getBannerPatterns(), this.blockBuilders.get(RenderLayer.getCutoutMipped()));
      map.put(TexturedRenderLayers.getEntityTranslucentCull(), this.blockBuilders.get(RenderLayer.getTranslucent()));
      assignBufferBuilder(map, TexturedRenderLayers.getShieldPatterns());
      assignBufferBuilder(map, TexturedRenderLayers.getBeds());
      assignBufferBuilder(map, TexturedRenderLayers.getShulkerBoxes());
      assignBufferBuilder(map, TexturedRenderLayers.getSign());
      assignBufferBuilder(map, TexturedRenderLayers.getHangingSign());
      assignBufferBuilder(map, TexturedRenderLayers.getChest());
      assignBufferBuilder(map, RenderLayer.getTranslucentNoCrumbling());
      assignBufferBuilder(map, RenderLayer.getArmorGlint());
      assignBufferBuilder(map, RenderLayer.getArmorEntityGlint());
      assignBufferBuilder(map, RenderLayer.getGlint());
      assignBufferBuilder(map, RenderLayer.getDirectGlint());
      assignBufferBuilder(map, RenderLayer.getGlintTranslucent());
      assignBufferBuilder(map, RenderLayer.getEntityGlint());
      assignBufferBuilder(map, RenderLayer.getDirectEntityGlint());
      assignBufferBuilder(map, RenderLayer.getWaterMask());
      ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.forEach((layer) -> {
         assignBufferBuilder(map, layer);
      });
   });
   private final VertexConsumerProvider.Immediate entityVertexConsumers;
   private final VertexConsumerProvider.Immediate effectVertexConsumers;
   private final OutlineVertexConsumerProvider outlineVertexConsumers;

   public BufferBuilderStorage() {
      this.entityVertexConsumers = VertexConsumerProvider.immediate(this.entityBuilders, new BufferBuilder(256));
      this.effectVertexConsumers = VertexConsumerProvider.immediate(new BufferBuilder(256));
      this.outlineVertexConsumers = new OutlineVertexConsumerProvider(this.entityVertexConsumers);
   }

   private static void assignBufferBuilder(Object2ObjectLinkedOpenHashMap builderStorage, RenderLayer layer) {
      builderStorage.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
   }

   public BlockBufferBuilderStorage getBlockBufferBuilders() {
      return this.blockBuilders;
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
