package net.minecraft.world.event.listener;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;

public interface GameEventListener {
   PositionSource getPositionSource();

   int getRange();

   boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos);

   default TriggerOrder getTriggerOrder() {
      return GameEventListener.TriggerOrder.UNSPECIFIED;
   }

   public static enum TriggerOrder {
      UNSPECIFIED,
      BY_DISTANCE;

      // $FF: synthetic method
      private static TriggerOrder[] method_45493() {
         return new TriggerOrder[]{UNSPECIFIED, BY_DISTANCE};
      }
   }
}
