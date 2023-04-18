package net.minecraft.world.event.listener;

import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class EntityGameEventHandler {
   private GameEventListener listener;
   @Nullable
   private ChunkSectionPos sectionPos;

   public EntityGameEventHandler(GameEventListener listener) {
      this.listener = listener;
   }

   public void onEntitySetPosCallback(ServerWorld world) {
      this.onEntitySetPos(world);
   }

   public void setListener(GameEventListener listener, @Nullable World world) {
      GameEventListener lv = this.listener;
      if (lv != listener) {
         if (world instanceof ServerWorld) {
            ServerWorld lv2 = (ServerWorld)world;
            updateDispatcher(lv2, this.sectionPos, (dispatcher) -> {
               dispatcher.removeListener(lv);
            });
            updateDispatcher(lv2, this.sectionPos, (dispatcher) -> {
               dispatcher.addListener(listener);
            });
         }

         this.listener = listener;
      }
   }

   public GameEventListener getListener() {
      return this.listener;
   }

   public void onEntityRemoval(ServerWorld world) {
      updateDispatcher(world, this.sectionPos, (dispatcher) -> {
         dispatcher.removeListener(this.listener);
      });
   }

   public void onEntitySetPos(ServerWorld world) {
      this.listener.getPositionSource().getPos(world).map(ChunkSectionPos::from).ifPresent((sectionPos) -> {
         if (this.sectionPos == null || !this.sectionPos.equals(sectionPos)) {
            updateDispatcher(world, this.sectionPos, (dispatcher) -> {
               dispatcher.removeListener(this.listener);
            });
            this.sectionPos = sectionPos;
            updateDispatcher(world, this.sectionPos, (dispatcher) -> {
               dispatcher.addListener(this.listener);
            });
         }

      });
   }

   private static void updateDispatcher(WorldView world, @Nullable ChunkSectionPos sectionPos, Consumer dispatcherConsumer) {
      if (sectionPos != null) {
         Chunk lv = world.getChunk(sectionPos.getSectionX(), sectionPos.getSectionZ(), ChunkStatus.FULL, false);
         if (lv != null) {
            dispatcherConsumer.accept(lv.getGameEventDispatcher(sectionPos.getSectionY()));
         }

      }
   }
}
