package net.minecraft.data.server.loottable;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface LootTableGenerator {
   void accept(BiConsumer exporter);
}
