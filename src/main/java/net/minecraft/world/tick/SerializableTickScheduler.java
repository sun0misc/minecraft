package net.minecraft.world.tick;

import java.util.function.Function;
import net.minecraft.nbt.NbtElement;

public interface SerializableTickScheduler {
   NbtElement toNbt(long time, Function typeToNameFunction);
}
