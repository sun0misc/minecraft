package net.minecraft.util.thread;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MessageListener extends AutoCloseable {
   String getName();

   void send(Object message);

   default void close() {
   }

   default CompletableFuture ask(Function messageProvider) {
      CompletableFuture completableFuture = new CompletableFuture();
      Objects.requireNonNull(completableFuture);
      Object object = messageProvider.apply(create("ask future procesor handle", completableFuture::complete));
      this.send(object);
      return completableFuture;
   }

   default CompletableFuture askFallible(Function messageProvider) {
      CompletableFuture completableFuture = new CompletableFuture();
      Object object = messageProvider.apply(create("ask future procesor handle", (either) -> {
         Objects.requireNonNull(completableFuture);
         either.ifLeft(completableFuture::complete);
         Objects.requireNonNull(completableFuture);
         either.ifRight(completableFuture::completeExceptionally);
      }));
      this.send(object);
      return completableFuture;
   }

   static MessageListener create(final String name, final Consumer action) {
      return new MessageListener() {
         public String getName() {
            return name;
         }

         public void send(Object message) {
            action.accept(message);
         }

         public String toString() {
            return name;
         }
      };
   }
}
