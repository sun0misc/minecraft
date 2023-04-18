package net.minecraft.command.argument.serialize;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.PacketByteBuf;

public class StringArgumentSerializer implements ArgumentSerializer {
   public void writePacket(Properties arg, PacketByteBuf arg2) {
      arg2.writeEnumConstant(arg.type);
   }

   public Properties fromPacket(PacketByteBuf arg) {
      StringArgumentType.StringType stringType = (StringArgumentType.StringType)arg.readEnumConstant(StringArgumentType.StringType.class);
      return new Properties(stringType);
   }

   public void writeJson(Properties arg, JsonObject jsonObject) {
      String var10002;
      switch (arg.type) {
         case SINGLE_WORD:
            var10002 = "word";
            break;
         case QUOTABLE_PHRASE:
            var10002 = "phrase";
            break;
         case GREEDY_PHRASE:
            var10002 = "greedy";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      jsonObject.addProperty("type", var10002);
   }

   public Properties getArgumentTypeProperties(StringArgumentType stringArgumentType) {
      return new Properties(stringArgumentType.getType());
   }

   // $FF: synthetic method
   public ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
      return this.fromPacket(buf);
   }

   public final class Properties implements ArgumentSerializer.ArgumentTypeProperties {
      final StringArgumentType.StringType type;

      public Properties(StringArgumentType.StringType type) {
         this.type = type;
      }

      public StringArgumentType createType(CommandRegistryAccess arg) {
         StringArgumentType var10000;
         switch (this.type) {
            case SINGLE_WORD:
               var10000 = StringArgumentType.word();
               break;
            case QUOTABLE_PHRASE:
               var10000 = StringArgumentType.string();
               break;
            case GREEDY_PHRASE:
               var10000 = StringArgumentType.greedyString();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public ArgumentSerializer getSerializer() {
         return StringArgumentSerializer.this;
      }

      // $FF: synthetic method
      public ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
         return this.createType(commandRegistryAccess);
      }
   }
}
