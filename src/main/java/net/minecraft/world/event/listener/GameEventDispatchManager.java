package net.minecraft.world.event.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.event.GameEvent;

public class GameEventDispatchManager {
   private final ServerWorld world;

   public GameEventDispatchManager(ServerWorld world) {
      this.world = world;
   }

   public void dispatch(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {
      int i = event.getRange();
      BlockPos lv = BlockPos.ofFloored(emitterPos);
      int j = ChunkSectionPos.getSectionCoord(lv.getX() - i);
      int k = ChunkSectionPos.getSectionCoord(lv.getY() - i);
      int l = ChunkSectionPos.getSectionCoord(lv.getZ() - i);
      int m = ChunkSectionPos.getSectionCoord(lv.getX() + i);
      int n = ChunkSectionPos.getSectionCoord(lv.getY() + i);
      int o = ChunkSectionPos.getSectionCoord(lv.getZ() + i);
      List list = new ArrayList();
      GameEventDispatcher.DispatchCallback lv2 = (listener, listenerPos) -> {
         if (listener.getTriggerOrder() == GameEventListener.TriggerOrder.BY_DISTANCE) {
            list.add(new GameEvent.Message(event, emitterPos, emitter, listener, listenerPos));
         } else {
            listener.listen(this.world, event, emitter, emitterPos);
         }

      };
      boolean bl = false;

      for(int p = j; p <= m; ++p) {
         for(int q = l; q <= o; ++q) {
            Chunk lv3 = this.world.getChunkManager().getWorldChunk(p, q);
            if (lv3 != null) {
               for(int r = k; r <= n; ++r) {
                  bl |= lv3.getGameEventDispatcher(r).dispatch(event, emitterPos, emitter, lv2);
               }
            }
         }
      }

      if (!list.isEmpty()) {
         this.dispatchListenersByDistance(list);
      }

      if (bl) {
         DebugInfoSender.sendGameEvent(this.world, event, emitterPos);
      }

   }

   private void dispatchListenersByDistance(List messages) {
      Collections.sort(messages);
      Iterator var2 = messages.iterator();

      while(var2.hasNext()) {
         GameEvent.Message lv = (GameEvent.Message)var2.next();
         GameEventListener lv2 = lv.getListener();
         lv2.listen(this.world, lv.getEvent(), lv.getEmitter(), lv.getEmitterPos());
      }

   }
}
