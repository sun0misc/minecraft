package net.minecraft.test;

import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class GameTestBatch {
   public static final String DEFAULT_BATCH = "defaultBatch";
   private final String id;
   private final Collection testFunctions;
   @Nullable
   private final Consumer beforeBatchConsumer;
   @Nullable
   private final Consumer afterBatchConsumer;

   public GameTestBatch(String id, Collection testFunctions, @Nullable Consumer beforeBatchConsumer, @Nullable Consumer afterBatchConsumer) {
      if (testFunctions.isEmpty()) {
         throw new IllegalArgumentException("A GameTestBatch must include at least one TestFunction!");
      } else {
         this.id = id;
         this.testFunctions = testFunctions;
         this.beforeBatchConsumer = beforeBatchConsumer;
         this.afterBatchConsumer = afterBatchConsumer;
      }
   }

   public String getId() {
      return this.id;
   }

   public Collection getTestFunctions() {
      return this.testFunctions;
   }

   public void startBatch(ServerWorld world) {
      if (this.beforeBatchConsumer != null) {
         this.beforeBatchConsumer.accept(world);
      }

   }

   public void finishBatch(ServerWorld world) {
      if (this.afterBatchConsumer != null) {
         this.afterBatchConsumer.accept(world);
      }

   }
}
