package net.minecraft.client.report.log;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringIdentifiable;

@Environment(EnvType.CLIENT)
public interface ChatLogEntry {
   Codec CODEC = StringIdentifiable.createCodec(Type::values).dispatch(ChatLogEntry::getType, Type::getCodec);

   Type getType();

   @Environment(EnvType.CLIENT)
   public static enum Type implements StringIdentifiable {
      PLAYER("player", () -> {
         return ReceivedMessage.ChatMessage.CHAT_MESSAGE_CODEC;
      }),
      SYSTEM("system", () -> {
         return ReceivedMessage.GameMessage.GAME_MESSAGE_CODEC;
      });

      private final String id;
      private final Supplier codecSupplier;

      private Type(String id, Supplier codecSupplier) {
         this.id = id;
         this.codecSupplier = codecSupplier;
      }

      private Codec getCodec() {
         return (Codec)this.codecSupplier.get();
      }

      public String asString() {
         return this.id;
      }

      // $FF: synthetic method
      private static Type[] method_46542() {
         return new Type[]{PLAYER, SYSTEM};
      }
   }
}
