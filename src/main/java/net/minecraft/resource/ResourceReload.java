package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;

public interface ResourceReload {
   CompletableFuture whenComplete();

   float getProgress();

   default boolean isComplete() {
      return this.whenComplete().isDone();
   }

   default void throwException() {
      CompletableFuture completableFuture = this.whenComplete();
      if (completableFuture.isCompletedExceptionally()) {
         completableFuture.join();
      }

   }
}
