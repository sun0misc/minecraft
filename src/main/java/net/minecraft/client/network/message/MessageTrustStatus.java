package net.minecraft.client.network.message;

import java.time.Instant;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public enum MessageTrustStatus implements StringIdentifiable {
   SECURE("secure"),
   MODIFIED("modified"),
   NOT_SECURE("not_secure");

   public static final com.mojang.serialization.Codec CODEC = StringIdentifiable.createCodec(MessageTrustStatus::values);
   private final String id;

   private MessageTrustStatus(String id) {
      this.id = id;
   }

   public static MessageTrustStatus getStatus(SignedMessage message, Text decorated, Instant receptionTimestamp) {
      if (message.hasSignature() && !message.isExpiredOnClient(receptionTimestamp)) {
         return isModified(message, decorated) ? MODIFIED : SECURE;
      } else {
         return NOT_SECURE;
      }
   }

   private static boolean isModified(SignedMessage message, Text decorated) {
      if (!decorated.getString().contains(message.getSignedContent())) {
         return true;
      } else {
         Text lv = message.unsignedContent();
         return lv == null ? false : isNotInDefaultFont(lv);
      }
   }

   private static boolean isNotInDefaultFont(Text content) {
      return (Boolean)content.visit((style, part) -> {
         return isNotInDefaultFont(style) ? Optional.of(true) : Optional.empty();
      }, Style.EMPTY).orElse(false);
   }

   private static boolean isNotInDefaultFont(Style style) {
      return !style.getFont().equals(Style.DEFAULT_FONT_ID);
   }

   public boolean isInsecure() {
      return this == NOT_SECURE;
   }

   @Nullable
   public MessageIndicator createIndicator(SignedMessage message) {
      MessageIndicator var10000;
      switch (this) {
         case MODIFIED:
            var10000 = MessageIndicator.modified(message.getSignedContent());
            break;
         case NOT_SECURE:
            var10000 = MessageIndicator.notSecure();
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   public String asString() {
      return this.id;
   }

   // $FF: synthetic method
   private static MessageTrustStatus[] method_44743() {
      return new MessageTrustStatus[]{SECURE, MODIFIED, NOT_SECURE};
   }
}
