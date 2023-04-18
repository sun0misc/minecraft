package net.minecraft.client.report;

import com.google.common.collect.Lists;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.report.log.ChatLog;
import net.minecraft.client.report.log.ReceivedMessage;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChatAbuseReport {
   private final Draft draft;
   private final AbuseReportLimits limits;

   public ChatAbuseReport(Draft draft, AbuseReportLimits limits) {
      this.draft = draft;
      this.limits = limits;
   }

   public ChatAbuseReport(UUID reportedPlayerUuid, AbuseReportLimits limits) {
      this.draft = new Draft(UUID.randomUUID(), Instant.now(), reportedPlayerUuid);
      this.limits = limits;
   }

   public Draft getDraft() {
      return this.draft;
   }

   public UUID getReportedPlayerUuid() {
      return this.draft.reportedPlayerUuid;
   }

   public IntSet getSelections() {
      return this.draft.selections;
   }

   public String getOpinionComments() {
      return this.draft.opinionComments;
   }

   public void setOpinionComments(String opinionComments) {
      this.draft.opinionComments = opinionComments;
   }

   @Nullable
   public AbuseReportReason getReason() {
      return this.draft.reason;
   }

   public void setReason(AbuseReportReason reason) {
      this.draft.reason = reason;
   }

   public void toggleMessageSelection(int index) {
      this.draft.toggleMessageSelection(index, this.limits);
   }

   public boolean hasSelectedMessage(int index) {
      return this.draft.selections.contains(index);
   }

   public boolean hasContents() {
      return StringUtils.isNotEmpty(this.getOpinionComments()) || !this.getSelections().isEmpty() || this.getReason() != null;
   }

   @Nullable
   public ValidationError validate() {
      if (this.draft.selections.isEmpty()) {
         return ChatAbuseReport.ValidationError.NO_REPORTED_MESSAGES;
      } else if (this.draft.selections.size() > this.limits.maxReportedMessageCount()) {
         return ChatAbuseReport.ValidationError.TOO_MANY_MESSAGES;
      } else if (this.draft.reason == null) {
         return ChatAbuseReport.ValidationError.NO_REASON;
      } else {
         return this.draft.opinionComments.length() > this.limits.maxOpinionCommentsLength() ? ChatAbuseReport.ValidationError.COMMENTS_TOO_LONG : null;
      }
   }

   public Either finalizeReport(AbuseReportContext reporter) {
      ValidationError lv = this.validate();
      if (lv != null) {
         return Either.right(lv);
      } else {
         String string = ((AbuseReportReason)Objects.requireNonNull(this.draft.reason)).getId();
         ReportEvidence reportEvidence = this.collectEvidence(reporter.getChatLog());
         ReportedEntity reportedEntity = new ReportedEntity(this.draft.reportedPlayerUuid);
         AbuseReport abuseReport = new AbuseReport(this.draft.opinionComments, string, reportEvidence, reportedEntity, this.draft.currentTime);
         return Either.left(new ReportWithId(this.draft.reportId, abuseReport));
      }
   }

   private ReportEvidence collectEvidence(ChatLog log) {
      List list = new ArrayList();
      ContextMessageCollector lv = new ContextMessageCollector(this.limits.leadingContextMessageCount());
      lv.add(log, this.draft.selections, (index, message) -> {
         list.add(this.toReportChatMessage(message, this.hasSelectedMessage(index)));
      });
      return new ReportEvidence(Lists.reverse(list));
   }

   private ReportChatMessage toReportChatMessage(ReceivedMessage.ChatMessage message, boolean selected) {
      MessageLink lv = message.message().link();
      MessageBody lv2 = message.message().signedBody();
      List list = lv2.lastSeenMessages().entries().stream().map(MessageSignatureData::toByteBuffer).toList();
      ByteBuffer byteBuffer = (ByteBuffer)Nullables.map(message.message().signature(), MessageSignatureData::toByteBuffer);
      return new ReportChatMessage(lv.index(), lv.sender(), lv.sessionId(), lv2.timestamp(), lv2.salt(), list, lv2.content(), byteBuffer, selected);
   }

   public ChatAbuseReport copy() {
      return new ChatAbuseReport(this.draft.copy(), this.limits);
   }

   @Environment(EnvType.CLIENT)
   public class Draft {
      final UUID reportId;
      final Instant currentTime;
      final UUID reportedPlayerUuid;
      final IntSet selections = new IntOpenHashSet();
      String opinionComments = "";
      @Nullable
      AbuseReportReason reason;

      Draft(UUID reportId, Instant currentTime, UUID reportedPlayerUuid) {
         this.reportId = reportId;
         this.currentTime = currentTime;
         this.reportedPlayerUuid = reportedPlayerUuid;
      }

      public void toggleMessageSelection(int index, AbuseReportLimits limits) {
         if (this.selections.contains(index)) {
            this.selections.remove(index);
         } else if (this.selections.size() < limits.maxReportedMessageCount()) {
            this.selections.add(index);
         }

      }

      public Draft copy() {
         Draft lv = ChatAbuseReport.this.new Draft(this.reportId, this.currentTime, this.reportedPlayerUuid);
         lv.selections.addAll(this.selections);
         lv.opinionComments = this.opinionComments;
         lv.reason = this.reason;
         return lv;
      }

      public boolean playerUuidEquals(UUID uuid) {
         return uuid.equals(this.reportedPlayerUuid);
      }
   }

   @Environment(EnvType.CLIENT)
   public static record ValidationError(Text message) {
      public static final ValidationError NO_REASON = new ValidationError(Text.translatable("gui.chatReport.send.no_reason"));
      public static final ValidationError NO_REPORTED_MESSAGES = new ValidationError(Text.translatable("gui.chatReport.send.no_reported_messages"));
      public static final ValidationError TOO_MANY_MESSAGES = new ValidationError(Text.translatable("gui.chatReport.send.too_many_messages"));
      public static final ValidationError COMMENTS_TOO_LONG = new ValidationError(Text.translatable("gui.chatReport.send.comments_too_long"));

      public ValidationError(Text arg) {
         this.message = arg;
      }

      public Text message() {
         return this.message;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record ReportWithId(UUID id, AbuseReport report) {
      public ReportWithId(UUID uUID, AbuseReport abuseReport) {
         this.id = uUID;
         this.report = abuseReport;
      }

      public UUID id() {
         return this.id;
      }

      public AbuseReport report() {
         return this.report;
      }
   }
}
