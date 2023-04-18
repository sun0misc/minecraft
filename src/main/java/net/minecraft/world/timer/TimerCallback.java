package net.minecraft.world.timer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface TimerCallback {
   void call(Object server, Timer events, long time);

   public abstract static class Serializer {
      private final Identifier id;
      private final Class callbackClass;

      public Serializer(Identifier id, Class callbackClass) {
         this.id = id;
         this.callbackClass = callbackClass;
      }

      public Identifier getId() {
         return this.id;
      }

      public Class getCallbackClass() {
         return this.callbackClass;
      }

      public abstract void serialize(NbtCompound nbt, TimerCallback callback);

      public abstract TimerCallback deserialize(NbtCompound nbt);
   }
}
