package net.minecraft.network.message;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.encryption.Signer;
import net.minecraft.text.Text;
import net.minecraft.util.TextifiedException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MessageChain {
   private static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   private MessageLink link;

   public MessageChain(UUID sender, UUID sessionId) {
      this.link = MessageLink.of(sender, sessionId);
   }

   public Packer getPacker(Signer signer) {
      return (body) -> {
         MessageLink lv = this.nextLink();
         return lv == null ? null : new MessageSignatureData(signer.sign((updatable) -> {
            SignedMessage.update(updatable, lv, body);
         }));
      };
   }

   public Unpacker getUnpacker(PlayerPublicKey playerPublicKey) {
      SignatureVerifier lv = playerPublicKey.createSignatureInstance();
      return (signature, body) -> {
         MessageLink lvx = this.nextLink();
         if (lvx == null) {
            throw new MessageChainException(Text.translatable("chat.disabled.chain_broken"), false);
         } else if (playerPublicKey.data().isExpired()) {
            throw new MessageChainException(Text.translatable("chat.disabled.expiredProfileKey"), false);
         } else {
            SignedMessage lv2 = new SignedMessage(lvx, signature, body, (Text)null, FilterMask.PASS_THROUGH);
            if (!lv2.verify(lv)) {
               throw new MessageChainException(Text.translatable("multiplayer.disconnect.unsigned_chat"), true);
            } else {
               if (lv2.isExpiredOnServer(Instant.now())) {
                  LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", body.content());
               }

               return lv2;
            }
         }
      };
   }

   @Nullable
   private MessageLink nextLink() {
      MessageLink lv = this.link;
      if (lv != null) {
         this.link = lv.next();
      }

      return lv;
   }

   @FunctionalInterface
   public interface Packer {
      Packer NONE = (body) -> {
         return null;
      };

      @Nullable
      MessageSignatureData pack(MessageBody body);
   }

   @FunctionalInterface
   public interface Unpacker {
      Unpacker NOT_INITIALIZED = (signature, body) -> {
         throw new MessageChainException(Text.translatable("chat.disabled.missingProfileKey"), false);
      };

      static Unpacker unsigned(UUID uuid) {
         return (signature, body) -> {
            return SignedMessage.ofUnsigned(uuid, body.content());
         };
      }

      SignedMessage unpack(@Nullable MessageSignatureData signature, MessageBody body) throws MessageChainException;
   }

   public static class MessageChainException extends TextifiedException {
      private final boolean shouldDisconnect;

      public MessageChainException(Text message, boolean shouldDisconnect) {
         super(message);
         this.shouldDisconnect = shouldDisconnect;
      }

      public boolean shouldDisconnect() {
         return this.shouldDisconnect;
      }
   }
}
