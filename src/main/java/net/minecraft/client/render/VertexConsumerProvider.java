package net.minecraft.client.render;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface VertexConsumerProvider {
   static Immediate immediate(BufferBuilder buffer) {
      return immediate(ImmutableMap.of(), buffer);
   }

   static Immediate immediate(Map layerBuffers, BufferBuilder fallbackBuffer) {
      return new Immediate(fallbackBuffer, layerBuffers);
   }

   VertexConsumer getBuffer(RenderLayer layer);

   @Environment(EnvType.CLIENT)
   public static class Immediate implements VertexConsumerProvider {
      protected final BufferBuilder fallbackBuffer;
      protected final Map layerBuffers;
      protected Optional currentLayer = Optional.empty();
      protected final Set activeConsumers = Sets.newHashSet();

      protected Immediate(BufferBuilder fallbackBuffer, Map layerBuffers) {
         this.fallbackBuffer = fallbackBuffer;
         this.layerBuffers = layerBuffers;
      }

      public VertexConsumer getBuffer(RenderLayer arg) {
         Optional optional = arg.asOptional();
         BufferBuilder lv = this.getBufferInternal(arg);
         if (!Objects.equals(this.currentLayer, optional) || !arg.areVerticesNotShared()) {
            if (this.currentLayer.isPresent()) {
               RenderLayer lv2 = (RenderLayer)this.currentLayer.get();
               if (!this.layerBuffers.containsKey(lv2)) {
                  this.draw(lv2);
               }
            }

            if (this.activeConsumers.add(lv)) {
               lv.begin(arg.getDrawMode(), arg.getVertexFormat());
            }

            this.currentLayer = optional;
         }

         return lv;
      }

      private BufferBuilder getBufferInternal(RenderLayer layer) {
         return (BufferBuilder)this.layerBuffers.getOrDefault(layer, this.fallbackBuffer);
      }

      public void drawCurrentLayer() {
         if (this.currentLayer.isPresent()) {
            RenderLayer lv = (RenderLayer)this.currentLayer.get();
            if (!this.layerBuffers.containsKey(lv)) {
               this.draw(lv);
            }

            this.currentLayer = Optional.empty();
         }

      }

      public void draw() {
         this.currentLayer.ifPresent((layer) -> {
            VertexConsumer lv = this.getBuffer(layer);
            if (lv == this.fallbackBuffer) {
               this.draw(layer);
            }

         });
         Iterator var1 = this.layerBuffers.keySet().iterator();

         while(var1.hasNext()) {
            RenderLayer lv = (RenderLayer)var1.next();
            this.draw(lv);
         }

      }

      public void draw(RenderLayer layer) {
         BufferBuilder lv = this.getBufferInternal(layer);
         boolean bl = Objects.equals(this.currentLayer, layer.asOptional());
         if (bl || lv != this.fallbackBuffer) {
            if (this.activeConsumers.remove(lv)) {
               layer.draw(lv, RenderSystem.getVertexSorting());
               if (bl) {
                  this.currentLayer = Optional.empty();
               }

            }
         }
      }
   }
}
