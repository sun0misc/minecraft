package net.minecraft.world.timer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;

public class FunctionTimerCallback implements TimerCallback {
   final Identifier name;

   public FunctionTimerCallback(Identifier name) {
      this.name = name;
   }

   public void call(MinecraftServer minecraftServer, Timer arg, long l) {
      CommandFunctionManager lv = minecraftServer.getCommandFunctionManager();
      lv.getFunction(this.name).ifPresent((function) -> {
         lv.execute(function, lv.getScheduledCommandSource());
      });
   }

   // $FF: synthetic method
   public void call(Object server, Timer events, long time) {
      this.call((MinecraftServer)server, events, time);
   }

   public static class Serializer extends TimerCallback.Serializer {
      public Serializer() {
         super(new Identifier("function"), FunctionTimerCallback.class);
      }

      public void serialize(NbtCompound arg, FunctionTimerCallback arg2) {
         arg.putString("Name", arg2.name.toString());
      }

      public FunctionTimerCallback deserialize(NbtCompound arg) {
         Identifier lv = new Identifier(arg.getString("Name"));
         return new FunctionTimerCallback(lv);
      }

      // $FF: synthetic method
      public TimerCallback deserialize(NbtCompound nbt) {
         return this.deserialize(nbt);
      }
   }
}
