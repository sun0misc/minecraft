package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.network.PacketByteBuf;

public class FloatArgumentSerializer implements ArgumentSerializer {
   public void writePacket(Properties arg, PacketByteBuf arg2) {
      boolean bl = arg.min != -3.4028235E38F;
      boolean bl2 = arg.max != Float.MAX_VALUE;
      arg2.writeByte(ArgumentHelper.getMinMaxFlag(bl, bl2));
      if (bl) {
         arg2.writeFloat(arg.min);
      }

      if (bl2) {
         arg2.writeFloat(arg.max);
      }

   }

   public Properties fromPacket(PacketByteBuf arg) {
      byte b = arg.readByte();
      float f = ArgumentHelper.hasMinFlag(b) ? arg.readFloat() : -3.4028235E38F;
      float g = ArgumentHelper.hasMaxFlag(b) ? arg.readFloat() : Float.MAX_VALUE;
      return new Properties(f, g);
   }

   public void writeJson(Properties arg, JsonObject jsonObject) {
      if (arg.min != -3.4028235E38F) {
         jsonObject.addProperty("min", arg.min);
      }

      if (arg.max != Float.MAX_VALUE) {
         jsonObject.addProperty("max", arg.max);
      }

   }

   public Properties getArgumentTypeProperties(FloatArgumentType floatArgumentType) {
      return new Properties(floatArgumentType.getMinimum(), floatArgumentType.getMaximum());
   }

   // $FF: synthetic method
   public ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
      return this.fromPacket(buf);
   }

   public final class Properties implements ArgumentSerializer.ArgumentTypeProperties {
      final float min;
      final float max;

      Properties(float min, float max) {
         this.min = min;
         this.max = max;
      }

      public FloatArgumentType createType(CommandRegistryAccess arg) {
         return FloatArgumentType.floatArg(this.min, this.max);
      }

      public ArgumentSerializer getSerializer() {
         return FloatArgumentSerializer.this;
      }

      // $FF: synthetic method
      public ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
         return this.createType(commandRegistryAccess);
      }
   }
}
