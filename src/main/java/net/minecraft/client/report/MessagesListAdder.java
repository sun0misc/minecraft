package net.minecraft.client.report;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.report.log.ChatLog;
import net.minecraft.client.report.log.ChatLogEntry;
import net.minecraft.client.report.log.ReceivedMessage;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MessagesListAdder {
   private final ChatLog log;
   private final ContextMessageCollector contextMessageCollector;
   private final Predicate reportablePredicate;
   @Nullable
   private MessageLink link = null;
   private int maxLogIndex;
   private int foldedMessageCount;
   @Nullable
   private SignedMessage lastMessage;

   public MessagesListAdder(AbuseReportContext context, Predicate reportablePredicate) {
      this.log = context.getChatLog();
      this.contextMessageCollector = new ContextMessageCollector(context.getSender().getLimits().leadingContextMessageCount());
      this.reportablePredicate = reportablePredicate;
      this.maxLogIndex = this.log.getMaxIndex();
   }

   public void add(int minAmount, MessagesList messages) {
      int j = 0;

      while(j < minAmount) {
         ChatLogEntry lv = this.log.get(this.maxLogIndex);
         if (lv == null) {
            break;
         }

         int k = this.maxLogIndex--;
         if (lv instanceof ReceivedMessage.ChatMessage lv2) {
            if (!lv2.message().equals(this.lastMessage)) {
               if (this.tryAdd(messages, lv2)) {
                  if (this.foldedMessageCount > 0) {
                     messages.addText(Text.translatable("gui.chatSelection.fold", this.foldedMessageCount));
                     this.foldedMessageCount = 0;
                  }

                  messages.addMessage(k, lv2);
                  ++j;
               } else {
                  ++this.foldedMessageCount;
               }

               this.lastMessage = lv2.message();
            }
         }
      }

   }

   private boolean tryAdd(MessagesList messages, ReceivedMessage.ChatMessage message) {
      SignedMessage lv = message.message();
      boolean bl = this.contextMessageCollector.tryLink(lv);
      if (this.reportablePredicate.test(message)) {
         this.contextMessageCollector.add(lv);
         if (this.link != null && !this.link.linksTo(lv.link())) {
            messages.addText(Text.translatable("gui.chatSelection.join", message.profile().getName()).formatted(Formatting.YELLOW));
         }

         this.link = lv.link();
         return true;
      } else {
         return bl;
      }
   }

   @Environment(EnvType.CLIENT)
   public interface MessagesList {
      void addMessage(int index, ReceivedMessage.ChatMessage message);

      void addText(Text text);
   }
}
