package net.minecraft.util.thread;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;

@FunctionalInterface
public interface FutureQueue {
   Logger LOGGER = LogUtils.getLogger();

   static FutureQueue immediate(Executor executor) {
      return (future) -> {
         future.submit(executor).exceptionally((throwable) -> {
            LOGGER.error("Task failed", throwable);
            return null;
         });
      };
   }

   void append(FutureSupplier future);

   public interface FutureSupplier {
      CompletableFuture submit(Executor executor);
   }
}
