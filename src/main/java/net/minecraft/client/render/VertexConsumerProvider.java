/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface VertexConsumerProvider {
    public static Immediate immediate(class_9799 buffer) {
        return VertexConsumerProvider.immediate(ImmutableMap.of(), buffer);
    }

    public static Immediate immediate(Map<RenderLayer, class_9799> layerBuffers, class_9799 fallbackBuffer) {
        return new Immediate(fallbackBuffer, layerBuffers);
    }

    public VertexConsumer getBuffer(RenderLayer var1);

    @Environment(value=EnvType.CLIENT)
    public static class Immediate
    implements VertexConsumerProvider {
        protected final class_9799 field_52156;
        protected final Map<RenderLayer, class_9799> layerBuffers;
        protected final Map<RenderLayer, BufferBuilder> field_52157 = new HashMap<RenderLayer, BufferBuilder>();
        @Nullable
        protected RenderLayer field_52158;

        protected Immediate(class_9799 fallbackBuffer, Map<RenderLayer, class_9799> layerBuffers) {
            this.field_52156 = fallbackBuffer;
            this.layerBuffers = layerBuffers;
        }

        @Override
        public VertexConsumer getBuffer(RenderLayer arg) {
            BufferBuilder lv = this.field_52157.get(arg);
            if (lv != null && !arg.areVerticesNotShared()) {
                this.method_60893(arg, lv);
                lv = null;
            }
            if (lv != null) {
                return lv;
            }
            class_9799 lv2 = this.layerBuffers.get(arg);
            if (lv2 != null) {
                lv = new BufferBuilder(lv2, arg.getDrawMode(), arg.getVertexFormat());
            } else {
                if (this.field_52158 != null) {
                    this.draw(this.field_52158);
                }
                lv = new BufferBuilder(this.field_52156, arg.getDrawMode(), arg.getVertexFormat());
                this.field_52158 = arg;
            }
            this.field_52157.put(arg, lv);
            return lv;
        }

        public void drawCurrentLayer() {
            if (this.field_52158 != null && !this.layerBuffers.containsKey(this.field_52158)) {
                this.draw(this.field_52158);
            }
            this.field_52158 = null;
        }

        public void draw() {
            this.field_52157.forEach(this::method_60893);
            this.field_52157.clear();
        }

        public void draw(RenderLayer layer) {
            BufferBuilder lv = this.field_52157.remove(layer);
            if (lv != null) {
                this.method_60893(layer, lv);
            }
        }

        private void method_60893(RenderLayer arg, BufferBuilder arg2) {
            class_9801 lv = arg2.method_60794();
            if (lv != null) {
                if (arg.method_60894()) {
                    class_9799 lv2 = this.layerBuffers.getOrDefault(arg, this.field_52156);
                    lv.method_60819(lv2, RenderSystem.getVertexSorting());
                }
                arg.method_60895(lv);
            }
            if (arg.equals(this.field_52158)) {
                this.field_52158 = null;
            }
        }
    }
}

