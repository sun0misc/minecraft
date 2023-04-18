package net.minecraft.client.network.message;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.report.log.ChatLog;
import net.minecraft.client.report.log.ReceivedMessage;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MessageHandler {
   private final MinecraftClient client;
   private final Deque delayedMessages = Queues.newArrayDeque();
   private long chatDelay;
   private long lastProcessTime;

   public MessageHandler(MinecraftClient client) {
      this.client = client;
   }

   public void processDelayedMessages() {
      if (this.chatDelay != 0L) {
         if (Util.getMeasuringTimeMs() >= this.lastProcessTime + this.chatDelay) {
            for(ProcessableMessage lv = (ProcessableMessage)this.delayedMessages.poll(); lv != null && !lv.accept(); lv = (ProcessableMessage)this.delayedMessages.poll()) {
            }
         }

      }
   }

   public void setChatDelay(double chatDelay) {
      long l = (long)(chatDelay * 1000.0);
      if (l == 0L && this.chatDelay > 0L) {
         this.delayedMessages.forEach(ProcessableMessage::accept);
         this.delayedMessages.clear();
      }

      this.chatDelay = l;
   }

   public void process() {
      ((ProcessableMessage)this.delayedMessages.remove()).accept();
   }

   public long getUnprocessedMessageCount() {
      return (long)this.delayedMessages.size();
   }

   public void processAll() {
      this.delayedMessages.forEach(ProcessableMessage::accept);
      this.delayedMessages.clear();
   }

   public boolean removeDelayedMessage(MessageSignatureData signature) {
      return this.delayedMessages.removeIf((message) -> {
         return signature.equals(message.signature());
      });
   }

   private boolean shouldDelay() {
      return this.chatDelay > 0L && Util.getMeasuringTimeMs() < this.lastProcessTime + this.chatDelay;
   }

   private void process(@Nullable MessageSignatureData signature, BooleanSupplier processor) {
      if (this.shouldDelay()) {
         this.delayedMessages.add(new ProcessableMessage(signature, processor));
      } else {
         processor.getAsBoolean();
      }

   }

   public void onChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params) {
      boolean bl = (Boolean)this.client.options.getOnlyShowSecureChat().getValue();
      SignedMessage lv = bl ? message.withoutUnsigned() : message;
      Text lv2 = params.applyChatDecoration(lv.getContent());
      Instant instant = Instant.now();
      this.process(message.signature(), () -> {
         boolean bl2 = this.processChatMessageInternal(params, message, lv2, sender, bl, instant);
         ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
         if (lv != null) {
            lv.acknowledge(message, bl2);
         }

         return bl2;
      });
   }

   public void onProfilelessMessage(Text content, MessageType.Parameters params) {
      Instant instant = Instant.now();
      this.process((MessageSignatureData)null, () -> {
         Text lv = params.applyChatDecoration(content);
         this.client.inGameHud.getChatHud().addMessage(lv);
         this.narrate(params, content);
         this.addToChatLog(lv, instant);
         this.lastProcessTime = Util.getMeasuringTimeMs();
         return true;
      });
   }

   private boolean processChatMessageInternal(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp) {
      MessageTrustStatus lv = this.getStatus(message, decorated, receptionTimestamp);
      if (onlyShowSecureChat && lv.isInsecure()) {
         return false;
      } else if (!this.client.shouldBlockMessages(message.getSender()) && !message.isFullyFiltered()) {
         MessageIndicator lv2 = lv.createIndicator(message);
         MessageSignatureData lv3 = message.signature();
         FilterMask lv4 = message.filterMask();
         if (lv4.isPassThrough()) {
            this.client.inGameHud.getChatHud().addMessage(decorated, lv3, lv2);
            this.narrate(params, message.getContent());
         } else {
            Text lv5 = lv4.getFilteredText(message.getSignedContent());
            if (lv5 != null) {
               this.client.inGameHud.getChatHud().addMessage(params.applyChatDecoration(lv5), lv3, lv2);
               this.narrate(params, lv5);
            }
         }

         this.addToChatLog(message, params, sender, lv);
         this.lastProcessTime = Util.getMeasuringTimeMs();
         return true;
      } else {
         return false;
      }
   }

   private void narrate(MessageType.Parameters params, Text message) {
      this.client.getNarratorManager().narrateChatMessage(params.applyNarrationDecoration(message));
   }

   private MessageTrustStatus getStatus(SignedMessage message, Text decorated, Instant receptionTimestamp) {
      return this.isAlwaysTrusted(message.getSender()) ? MessageTrustStatus.SECURE : MessageTrustStatus.getStatus(message, decorated, receptionTimestamp);
   }

   private void addToChatLog(SignedMessage message, MessageType.Parameters params, GameProfile sender, MessageTrustStatus trustStatus) {
      ChatLog lv = this.client.getAbuseReportContext().getChatLog();
      lv.add(ReceivedMessage.of(sender, message, trustStatus));
   }

   private void addToChatLog(Text message, Instant timestamp) {
      ChatLog lv = this.client.getAbuseReportContext().getChatLog();
      lv.add(ReceivedMessage.of(message, timestamp));
   }

   public void onGameMessage(Text message, boolean overlay) {
      if (!(Boolean)this.client.options.getHideMatchedNames().getValue() || !this.client.shouldBlockMessages(this.extractSender(message))) {
         if (overlay) {
            this.client.inGameHud.setOverlayMessage(message, false);
         } else {
            this.client.inGameHud.getChatHud().addMessage(message);
            this.addToChatLog(message, Instant.now());
         }

         this.client.getNarratorManager().narrateSystemMessage(message);
      }
   }

   private UUID extractSender(Text text) {
      String string = TextVisitFactory.removeFormattingCodes(text);
      String string2 = StringUtils.substringBetween(string, "<", ">");
      return string2 == null ? Util.NIL_UUID : this.client.getSocialInteractionsManager().getUuid(string2);
   }

   private boolean isAlwaysTrusted(UUID sender) {
      if (this.client.isInSingleplayer() && this.client.player != null) {
         UUID uUID2 = this.client.player.getGameProfile().getId();
         return uUID2.equals(sender);
      } else {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   private static record ProcessableMessage(@Nullable MessageSignatureData signature, BooleanSupplier handler) {
      ProcessableMessage(@Nullable MessageSignatureData arg, BooleanSupplier booleanSupplier) {
         this.signature = arg;
         this.handler = booleanSupplier;
      }

      public boolean accept() {
         return this.handler.getAsBoolean();
      }

      @Nullable
      public MessageSignatureData signature() {
         return this.signature;
      }

      public BooleanSupplier handler() {
         return this.handler;
      }
   }
}
