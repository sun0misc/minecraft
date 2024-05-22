/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;

@Environment(value=EnvType.CLIENT)
public class VertexConsumers {
    public static VertexConsumer union() {
        throw new IllegalArgumentException();
    }

    public static VertexConsumer union(VertexConsumer first) {
        return first;
    }

    public static VertexConsumer union(VertexConsumer first, VertexConsumer second) {
        return new Dual(first, second);
    }

    public static VertexConsumer union(VertexConsumer ... delegates) {
        return new Union(delegates);
    }

    @Environment(value=EnvType.CLIENT)
    static class Dual
    implements VertexConsumer {
        private final VertexConsumer first;
        private final VertexConsumer second;

        public Dual(VertexConsumer first, VertexConsumer second) {
            if (first == second) {
                throw new IllegalArgumentException("Duplicate delegates");
            }
            this.first = first;
            this.second = second;
        }

        @Override
        public VertexConsumer vertex(float f, float g, float h) {
            this.first.vertex(f, g, h);
            this.second.vertex(f, g, h);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.first.color(red, green, blue, alpha);
            this.second.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            this.first.texture(u, v);
            this.second.texture(u, v);
            return this;
        }

        @Override
        public VertexConsumer method_60796(int i, int j) {
            this.first.method_60796(i, j);
            this.second.method_60796(i, j);
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            this.first.light(u, v);
            this.second.light(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.first.normal(x, y, z);
            this.second.normal(x, y, z);
            return this;
        }

        @Override
        public void vertex(float x, float y, float z, int i, float green, float blue, int l, int m, float v, float o, float p) {
            this.first.vertex(x, y, z, i, green, blue, l, m, v, o, p);
            this.second.vertex(x, y, z, i, green, blue, l, m, v, o, p);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Union(VertexConsumer[] delegates) implements VertexConsumer
    {
        Union {
            for (int i = 0; i < delegates.length; ++i) {
                for (int j = i + 1; j < delegates.length; ++j) {
                    if (delegates[i] != delegates[j]) continue;
                    throw new IllegalArgumentException("Duplicate delegates");
                }
            }
        }

        private void delegate(Consumer<VertexConsumer> action) {
            for (VertexConsumer lv : this.delegates) {
                action.accept(lv);
            }
        }

        @Override
        public VertexConsumer vertex(float f, float g, float h) {
            this.delegate(arg -> arg.vertex(f, g, h));
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.delegate(arg -> arg.color(red, green, blue, alpha));
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            this.delegate(arg -> arg.texture(u, v));
            return this;
        }

        @Override
        public VertexConsumer method_60796(int i, int j) {
            this.delegate(arg -> arg.method_60796(i, j));
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            this.delegate(arg -> arg.light(u, v));
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.delegate(arg -> arg.normal(x, y, z));
            return this;
        }

        @Override
        public void vertex(float x, float y, float z, int i, float green, float blue, int l, int m, float v, float o, float p) {
            this.delegate(arg -> arg.vertex(x, y, z, i, green, blue, l, m, v, o, p));
        }
    }
}

