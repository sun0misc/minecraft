package net.minecraft.network.message;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface SentMessage {
   Text getContent();

   void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params);

   static SentMessage of(SignedMessage message) {
      return (SentMessage)(message.isSenderMissing() ? new Profileless(message.getContent()) : new Chat(message));
   }

   public static record Profileless(Text getContent) implements SentMessage {
      public Profileless(Text arg) {
         this.getContent = arg;
      }

      public Text getContent() {
         return this.getContent;
      }

      public void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params) {
         sender.networkHandler.sendProfilelessChatMessage(this.getContent, params);
      }
   }

   public static record Chat(SignedMessage message) implements SentMessage {
      public Chat(SignedMessage message) {
         this.message = message;
      }

      public Text getContent() {
         return this.message.getContent();
      }

      public void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params) {
         SignedMessage lv = this.message.withFilterMaskEnabled(filterMaskEnabled);
         if (!lv.isFullyFiltered()) {
            sender.networkHandler.sendChatMessage(lv, params);
         }

      }

      public SignedMessage message() {
         return this.message;
      }
   }
}
