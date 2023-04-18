package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.network.PacketByteBuf;

public class LongArgumentSerializer implements ArgumentSerializer {
   public void writePacket(Properties arg, PacketByteBuf arg2) {
      boolean bl = arg.min != Long.MIN_VALUE;
      boolean bl2 = arg.max != Long.MAX_VALUE;
      arg2.writeByte(ArgumentHelper.getMinMaxFlag(bl, bl2));
      if (bl) {
         arg2.writeLong(arg.min);
      }

      if (bl2) {
         arg2.writeLong(arg.max);
      }

   }

   public Properties fromPacket(PacketByteBuf arg) {
      byte b = arg.readByte();
      long l = ArgumentHelper.hasMinFlag(b) ? arg.readLong() : Long.MIN_VALUE;
      long m = ArgumentHelper.hasMaxFlag(b) ? arg.readLong() : Long.MAX_VALUE;
      return new Properties(l, m);
   }

   public void writeJson(Properties arg, JsonObject jsonObject) {
      if (arg.min != Long.MIN_VALUE) {
         jsonObject.addProperty("min", arg.min);
      }

      if (arg.max != Long.MAX_VALUE) {
         jsonObject.addProperty("max", arg.max);
      }

   }

   public Properties getArgumentTypeProperties(LongArgumentType longArgumentType) {
      return new Properties(longArgumentType.getMinimum(), longArgumentType.getMaximum());
   }

   // $FF: synthetic method
   public ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
      return this.fromPacket(buf);
   }

   public final class Properties implements ArgumentSerializer.ArgumentTypeProperties {
      final long min;
      final long max;

      Properties(long min, long max) {
         this.min = min;
         this.max = max;
      }

      public LongArgumentType createType(CommandRegistryAccess arg) {
         return LongArgumentType.longArg(this.min, this.max);
      }

      public ArgumentSerializer getSerializer() {
         return LongArgumentSerializer.this;
      }

      // $FF: synthetic method
      public ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
         return this.createType(commandRegistryAccess);
      }
   }
}
