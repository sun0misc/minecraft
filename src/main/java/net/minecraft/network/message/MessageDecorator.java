package net.minecraft.network.message;

import java.util.concurrent.CompletableFuture;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MessageDecorator {
   MessageDecorator NOOP = (sender, message) -> {
      return CompletableFuture.completedFuture(message);
   };

   CompletableFuture decorate(@Nullable ServerPlayerEntity sender, Text message);
}
