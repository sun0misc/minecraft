package net.minecraft.network.message;

import net.minecraft.network.encryption.SignatureVerifier;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MessageVerifier {
   MessageVerifier NO_SIGNATURE = (message) -> {
      return !message.hasSignature();
   };
   MessageVerifier UNVERIFIED = (message) -> {
      return false;
   };

   boolean isVerified(SignedMessage message);

   public static class Impl implements MessageVerifier {
      private final SignatureVerifier signatureVerifier;
      @Nullable
      private SignedMessage lastVerifiedMessage;
      private boolean lastMessageVerified = true;

      public Impl(SignatureVerifier signatureVerifier) {
         this.signatureVerifier = signatureVerifier;
      }

      private boolean verifyPrecedingSignature(SignedMessage message) {
         if (message.equals(this.lastVerifiedMessage)) {
            return true;
         } else {
            return this.lastVerifiedMessage == null || message.link().linksTo(this.lastVerifiedMessage.link());
         }
      }

      public boolean isVerified(SignedMessage message) {
         this.lastMessageVerified = this.lastMessageVerified && message.verify(this.signatureVerifier) && this.verifyPrecedingSignature(message);
         if (!this.lastMessageVerified) {
            return false;
         } else {
            this.lastVerifiedMessage = message;
            return true;
         }
      }
   }
}
