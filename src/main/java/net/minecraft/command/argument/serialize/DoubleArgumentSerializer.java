package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.network.PacketByteBuf;

public class DoubleArgumentSerializer implements ArgumentSerializer {
   public void writePacket(Properties arg, PacketByteBuf arg2) {
      boolean bl = arg.min != -1.7976931348623157E308;
      boolean bl2 = arg.max != Double.MAX_VALUE;
      arg2.writeByte(ArgumentHelper.getMinMaxFlag(bl, bl2));
      if (bl) {
         arg2.writeDouble(arg.min);
      }

      if (bl2) {
         arg2.writeDouble(arg.max);
      }

   }

   public Properties fromPacket(PacketByteBuf arg) {
      byte b = arg.readByte();
      double d = ArgumentHelper.hasMinFlag(b) ? arg.readDouble() : -1.7976931348623157E308;
      double e = ArgumentHelper.hasMaxFlag(b) ? arg.readDouble() : Double.MAX_VALUE;
      return new Properties(d, e);
   }

   public void writeJson(Properties arg, JsonObject jsonObject) {
      if (arg.min != -1.7976931348623157E308) {
         jsonObject.addProperty("min", arg.min);
      }

      if (arg.max != Double.MAX_VALUE) {
         jsonObject.addProperty("max", arg.max);
      }

   }

   public Properties getArgumentTypeProperties(DoubleArgumentType doubleArgumentType) {
      return new Properties(doubleArgumentType.getMinimum(), doubleArgumentType.getMaximum());
   }

   // $FF: synthetic method
   public ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
      return this.fromPacket(buf);
   }

   public final class Properties implements ArgumentSerializer.ArgumentTypeProperties {
      final double min;
      final double max;

      Properties(double min, double max) {
         this.min = min;
         this.max = max;
      }

      public DoubleArgumentType createType(CommandRegistryAccess arg) {
         return DoubleArgumentType.doubleArg(this.min, this.max);
      }

      public ArgumentSerializer getSerializer() {
         return DoubleArgumentSerializer.this;
      }

      // $FF: synthetic method
      public ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
         return this.createType(commandRegistryAccess);
      }
   }
}
