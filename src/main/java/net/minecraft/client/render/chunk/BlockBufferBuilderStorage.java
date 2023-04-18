package net.minecraft.client.render.chunk;

import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class BlockBufferBuilderStorage {
   private final Map builders = (Map)RenderLayer.getBlockLayers().stream().collect(Collectors.toMap((arg) -> {
      return arg;
   }, (renderLayer) -> {
      return new BufferBuilder(renderLayer.getExpectedBufferSize());
   }));

   public BufferBuilder get(RenderLayer layer) {
      return (BufferBuilder)this.builders.get(layer);
   }

   public void clear() {
      this.builders.values().forEach(BufferBuilder::clear);
   }

   public void reset() {
      this.builders.values().forEach(BufferBuilder::reset);
   }
}
