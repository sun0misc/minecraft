package net.minecraft.world.timer;

import java.util.Collection;
import java.util.Iterator;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;

public class FunctionTagTimerCallback implements TimerCallback {
   final Identifier name;

   public FunctionTagTimerCallback(Identifier name) {
      this.name = name;
   }

   public void call(MinecraftServer minecraftServer, Timer arg, long l) {
      CommandFunctionManager lv = minecraftServer.getCommandFunctionManager();
      Collection collection = lv.getTag(this.name);
      Iterator var7 = collection.iterator();

      while(var7.hasNext()) {
         CommandFunction lv2 = (CommandFunction)var7.next();
         lv.execute(lv2, lv.getScheduledCommandSource());
      }

   }

   // $FF: synthetic method
   public void call(Object server, Timer events, long time) {
      this.call((MinecraftServer)server, events, time);
   }

   public static class Serializer extends TimerCallback.Serializer {
      public Serializer() {
         super(new Identifier("function_tag"), FunctionTagTimerCallback.class);
      }

      public void serialize(NbtCompound arg, FunctionTagTimerCallback arg2) {
         arg.putString("Name", arg2.name.toString());
      }

      public FunctionTagTimerCallback deserialize(NbtCompound arg) {
         Identifier lv = new Identifier(arg.getString("Name"));
         return new FunctionTagTimerCallback(lv);
      }

      // $FF: synthetic method
      public TimerCallback deserialize(NbtCompound nbt) {
         return this.deserialize(nbt);
      }
   }
}
