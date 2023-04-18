package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.network.PacketByteBuf;

public class IntegerArgumentSerializer implements ArgumentSerializer {
   public void writePacket(Properties arg, PacketByteBuf arg2) {
      boolean bl = arg.min != Integer.MIN_VALUE;
      boolean bl2 = arg.max != Integer.MAX_VALUE;
      arg2.writeByte(ArgumentHelper.getMinMaxFlag(bl, bl2));
      if (bl) {
         arg2.writeInt(arg.min);
      }

      if (bl2) {
         arg2.writeInt(arg.max);
      }

   }

   public Properties fromPacket(PacketByteBuf arg) {
      byte b = arg.readByte();
      int i = ArgumentHelper.hasMinFlag(b) ? arg.readInt() : Integer.MIN_VALUE;
      int j = ArgumentHelper.hasMaxFlag(b) ? arg.readInt() : Integer.MAX_VALUE;
      return new Properties(i, j);
   }

   public void writeJson(Properties arg, JsonObject jsonObject) {
      if (arg.min != Integer.MIN_VALUE) {
         jsonObject.addProperty("min", arg.min);
      }

      if (arg.max != Integer.MAX_VALUE) {
         jsonObject.addProperty("max", arg.max);
      }

   }

   public Properties getArgumentTypeProperties(IntegerArgumentType integerArgumentType) {
      return new Properties(integerArgumentType.getMinimum(), integerArgumentType.getMaximum());
   }

   // $FF: synthetic method
   public ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
      return this.fromPacket(buf);
   }

   public final class Properties implements ArgumentSerializer.ArgumentTypeProperties {
      final int min;
      final int max;

      Properties(int min, int max) {
         this.min = min;
         this.max = max;
      }

      public IntegerArgumentType createType(CommandRegistryAccess arg) {
         return IntegerArgumentType.integer(this.min, this.max);
      }

      public ArgumentSerializer getSerializer() {
         return IntegerArgumentSerializer.this;
      }

      // $FF: synthetic method
      public ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
         return this.createType(commandRegistryAccess);
      }
   }
}
