package net.minecraft.util;

import java.util.concurrent.TimeUnit;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class TimeHelper {
   public static final long SECOND_IN_NANOS;
   public static final long MILLI_IN_NANOS;

   public static UniformIntProvider betweenSeconds(int min, int max) {
      return UniformIntProvider.create(min * 20, max * 20);
   }

   static {
      SECOND_IN_NANOS = TimeUnit.SECONDS.toNanos(1L);
      MILLI_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
   }
}
