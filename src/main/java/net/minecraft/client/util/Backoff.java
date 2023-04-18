package net.minecraft.client.util;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public interface Backoff {
   Backoff ONE_CYCLE = new Backoff() {
      public long success() {
         return 1L;
      }

      public long fail() {
         return 1L;
      }
   };

   long success();

   long fail();

   static Backoff exponential(final int maxSkippableCycles) {
      return new Backoff() {
         private static final Logger LOGGER = LogUtils.getLogger();
         private int failureCount;

         public long success() {
            this.failureCount = 0;
            return 1L;
         }

         public long fail() {
            ++this.failureCount;
            long l = Math.min(1L << this.failureCount, (long)maxSkippableCycles);
            LOGGER.debug("Skipping for {} extra cycles", l);
            return l;
         }
      };
   }
}
