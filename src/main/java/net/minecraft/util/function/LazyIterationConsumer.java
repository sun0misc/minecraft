/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface LazyIterationConsumer<T> {
    public NextIteration accept(T var1);

    public static <T> LazyIterationConsumer<T> forConsumer(Consumer<T> consumer) {
        return value -> {
            consumer.accept(value);
            return NextIteration.CONTINUE;
        };
    }

    public static enum NextIteration {
        CONTINUE,
        ABORT;


        public boolean shouldAbort() {
            return this == ABORT;
        }
    }
}

