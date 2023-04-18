package net.minecraft.world.event.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;

public class SimpleGameEventDispatcher implements GameEventDispatcher {
   private final List listeners = Lists.newArrayList();
   private final Set toRemove = Sets.newHashSet();
   private final List toAdd = Lists.newArrayList();
   private boolean dispatching;
   private final ServerWorld world;

   public SimpleGameEventDispatcher(ServerWorld world) {
      this.world = world;
   }

   public boolean isEmpty() {
      return this.listeners.isEmpty();
   }

   public void addListener(GameEventListener listener) {
      if (this.dispatching) {
         this.toAdd.add(listener);
      } else {
         this.listeners.add(listener);
      }

      DebugInfoSender.sendGameEventListener(this.world, listener);
   }

   public void removeListener(GameEventListener listener) {
      if (this.dispatching) {
         this.toRemove.add(listener);
      } else {
         this.listeners.remove(listener);
      }

   }

   public boolean dispatch(GameEvent event, Vec3d pos, GameEvent.Emitter emitter, GameEventDispatcher.DispatchCallback callback) {
      this.dispatching = true;
      boolean bl = false;

      try {
         Iterator iterator = this.listeners.iterator();

         while(iterator.hasNext()) {
            GameEventListener lv = (GameEventListener)iterator.next();
            if (this.toRemove.remove(lv)) {
               iterator.remove();
            } else {
               Optional optional = dispatchTo(this.world, pos, lv);
               if (optional.isPresent()) {
                  callback.visit(lv, (Vec3d)optional.get());
                  bl = true;
               }
            }
         }
      } finally {
         this.dispatching = false;
      }

      if (!this.toAdd.isEmpty()) {
         this.listeners.addAll(this.toAdd);
         this.toAdd.clear();
      }

      if (!this.toRemove.isEmpty()) {
         this.listeners.removeAll(this.toRemove);
         this.toRemove.clear();
      }

      return bl;
   }

   private static Optional dispatchTo(ServerWorld world, Vec3d listenerPos, GameEventListener listener) {
      Optional optional = listener.getPositionSource().getPos(world);
      if (optional.isEmpty()) {
         return Optional.empty();
      } else {
         double d = ((Vec3d)optional.get()).squaredDistanceTo(listenerPos);
         int i = listener.getRange() * listener.getRange();
         return d > (double)i ? Optional.empty() : optional;
      }
   }
}
