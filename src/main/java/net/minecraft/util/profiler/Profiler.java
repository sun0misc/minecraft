/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.profiler;

import java.util.function.Supplier;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.SampleType;

public interface Profiler {
    public static final String ROOT_NAME = "root";

    public void startTick();

    public void endTick();

    public void push(String var1);

    public void push(Supplier<String> var1);

    public void pop();

    public void swap(String var1);

    public void swap(Supplier<String> var1);

    public void markSampleType(SampleType var1);

    default public void visit(String marker) {
        this.visit(marker, 1);
    }

    public void visit(String var1, int var2);

    default public void visit(Supplier<String> markerGetter) {
        this.visit(markerGetter, 1);
    }

    public void visit(Supplier<String> var1, int var2);

    public static Profiler union(final Profiler a, final Profiler b) {
        if (a == DummyProfiler.INSTANCE) {
            return b;
        }
        if (b == DummyProfiler.INSTANCE) {
            return a;
        }
        return new Profiler(){

            @Override
            public void startTick() {
                a.startTick();
                b.startTick();
            }

            @Override
            public void endTick() {
                a.endTick();
                b.endTick();
            }

            @Override
            public void push(String location) {
                a.push(location);
                b.push(location);
            }

            @Override
            public void push(Supplier<String> locationGetter) {
                a.push(locationGetter);
                b.push(locationGetter);
            }

            @Override
            public void markSampleType(SampleType type) {
                a.markSampleType(type);
                b.markSampleType(type);
            }

            @Override
            public void pop() {
                a.pop();
                b.pop();
            }

            @Override
            public void swap(String location) {
                a.swap(location);
                b.swap(location);
            }

            @Override
            public void swap(Supplier<String> locationGetter) {
                a.swap(locationGetter);
                b.swap(locationGetter);
            }

            @Override
            public void visit(String marker, int num) {
                a.visit(marker, num);
                b.visit(marker, num);
            }

            @Override
            public void visit(Supplier<String> markerGetter, int num) {
                a.visit(markerGetter, num);
                b.visit(markerGetter, num);
            }
        };
    }
}

