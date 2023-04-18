package net.minecraft.client.report.log;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.message.MessageTrustStatus;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;

@Environment(EnvType.CLIENT)
public interface ReceivedMessage extends ChatLogEntry {
   static ChatMessage of(GameProfile gameProfile, SignedMessage message, MessageTrustStatus trustStatus) {
      return new ChatMessage(gameProfile, message, trustStatus);
   }

   static GameMessage of(Text message, Instant timestamp) {
      return new GameMessage(message, timestamp);
   }

   Text getContent();

   default Text getNarration() {
      return this.getContent();
   }

   boolean isSentFrom(UUID uuid);

   @Environment(EnvType.CLIENT)
   public static record ChatMessage(GameProfile profile, SignedMessage message, MessageTrustStatus trustStatus) implements ReceivedMessage {
      public static final Codec CHAT_MESSAGE_CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codecs.GAME_PROFILE.fieldOf("profile").forGetter(ChatMessage::profile), SignedMessage.CODEC.forGetter(ChatMessage::message), MessageTrustStatus.CODEC.optionalFieldOf("trust_level", MessageTrustStatus.SECURE).forGetter(ChatMessage::trustStatus)).apply(instance, ChatMessage::new);
      });
      private static final DateTimeFormatter DATE_TIME_FORMATTER;

      public ChatMessage(GameProfile gameProfile, SignedMessage arg, MessageTrustStatus arg2) {
         this.profile = gameProfile;
         this.message = arg;
         this.trustStatus = arg2;
      }

      public Text getContent() {
         if (!this.message.filterMask().isPassThrough()) {
            Text lv = this.message.filterMask().getFilteredText(this.message.getSignedContent());
            return (Text)(lv != null ? lv : Text.empty());
         } else {
            return this.message.getContent();
         }
      }

      public Text getNarration() {
         Text lv = this.getContent();
         Text lv2 = this.getFormattedTimestamp();
         return Text.translatable("gui.chatSelection.message.narrate", this.profile.getName(), lv, lv2);
      }

      public Text getHeadingText() {
         Text lv = this.getFormattedTimestamp();
         return Text.translatable("gui.chatSelection.heading", this.profile.getName(), lv);
      }

      private Text getFormattedTimestamp() {
         LocalDateTime localDateTime = LocalDateTime.ofInstant(this.message.getTimestamp(), ZoneOffset.systemDefault());
         return Text.literal(localDateTime.format(DATE_TIME_FORMATTER)).formatted(Formatting.ITALIC, Formatting.GRAY);
      }

      public boolean isSentFrom(UUID uuid) {
         return this.message.canVerifyFrom(uuid);
      }

      public UUID getSenderUuid() {
         return this.profile.getId();
      }

      public ChatLogEntry.Type getType() {
         return ChatLogEntry.Type.PLAYER;
      }

      public GameProfile profile() {
         return this.profile;
      }

      public SignedMessage message() {
         return this.message;
      }

      public MessageTrustStatus trustStatus() {
         return this.trustStatus;
      }

      static {
         DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
      }
   }

   @Environment(EnvType.CLIENT)
   public static record GameMessage(Text message, Instant timestamp) implements ReceivedMessage {
      public static final Codec GAME_MESSAGE_CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codecs.TEXT.fieldOf("message").forGetter(GameMessage::message), Codecs.INSTANT.fieldOf("time_stamp").forGetter(GameMessage::timestamp)).apply(instance, GameMessage::new);
      });

      public GameMessage(Text arg, Instant instant) {
         this.message = arg;
         this.timestamp = instant;
      }

      public Text getContent() {
         return this.message;
      }

      public boolean isSentFrom(UUID uuid) {
         return false;
      }

      public ChatLogEntry.Type getType() {
         return ChatLogEntry.Type.SYSTEM;
      }

      public Text message() {
         return this.message;
      }

      public Instant timestamp() {
         return this.timestamp;
      }
   }
}
