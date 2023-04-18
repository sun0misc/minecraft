package net.minecraft.client.gui.screen.report;

import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TaskScreen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.report.AbuseReportReason;
import net.minecraft.client.report.ChatAbuseReport;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Nullables;
import net.minecraft.util.TextifiedException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ChatReportScreen extends Screen {
   private static final int BOTTOM_BUTTON_WIDTH = 120;
   private static final int BUTTON_HEIGHT = 20;
   private static final int BUTTON_TOP_MARGIN = 20;
   private static final int BUTTON_BOTTOM_MARGIN = 10;
   private static final int REASON_DESCRIPTION_TOP_MARGIN = 25;
   private static final int REASON_DESCRIPTION_TEXT_WIDTH = 280;
   private static final int SCREEN_HEIGHT = 300;
   private static final Text OBSERVED_WHAT_TEXT = Text.translatable("gui.chatReport.observed_what");
   private static final Text SELECT_REASON_TEXT = Text.translatable("gui.chatReport.select_reason");
   private static final Text MORE_COMMENTS_TEXT = Text.translatable("gui.chatReport.more_comments");
   private static final Text DESCRIBE_TEXT = Text.translatable("gui.chatReport.describe");
   private static final Text REPORT_SENT_MESSAGE_TEXT = Text.translatable("gui.chatReport.report_sent_msg");
   private static final Text SELECT_CHAT_TEXT = Text.translatable("gui.chatReport.select_chat");
   private static final Text SENDING_TEXT;
   private static final Text REPORT_SENT_TITLE;
   private static final Text REPORT_ERROR_TITLE;
   private static final Text GENERIC_ERROR_TEXT;
   private static final Logger LOGGER;
   @Nullable
   final Screen parent;
   private final AbuseReportContext context;
   @Nullable
   private MultilineText reasonDescription;
   @Nullable
   private EditBoxWidget editBox;
   private ButtonWidget sendButton;
   private ChatAbuseReport report;
   @Nullable
   private ChatAbuseReport.ValidationError validationError;

   private ChatReportScreen(@Nullable Screen parent, AbuseReportContext context, ChatAbuseReport report) {
      super(Text.translatable("gui.chatReport.title"));
      this.parent = parent;
      this.context = context;
      this.report = report;
   }

   public ChatReportScreen(@Nullable Screen parent, AbuseReportContext reporter, UUID reportedPlayerUuid) {
      this(parent, reporter, new ChatAbuseReport(reportedPlayerUuid, reporter.getSender().getLimits()));
   }

   public ChatReportScreen(@Nullable Screen parent, AbuseReportContext context, ChatAbuseReport.Draft draft) {
      this(parent, context, new ChatAbuseReport(draft, context.getSender().getLimits()));
   }

   protected void init() {
      AbuseReportLimits abuseReportLimits = this.context.getSender().getLimits();
      int i = this.width / 2;
      AbuseReportReason lv = this.report.getReason();
      if (lv != null) {
         this.reasonDescription = MultilineText.create(this.textRenderer, lv.getDescription(), 280);
      } else {
         this.reasonDescription = null;
      }

      IntSet intSet = this.report.getSelections();
      Object lv2;
      if (intSet.isEmpty()) {
         lv2 = SELECT_CHAT_TEXT;
      } else {
         lv2 = Text.translatable("gui.chatReport.selected_chat", intSet.size());
      }

      this.addDrawableChild(ButtonWidget.builder((Text)lv2, (button) -> {
         this.client.setScreen(new ChatSelectionScreen(this, this.context, this.report, (report) -> {
            this.report = report;
            this.onChange();
         }));
      }).dimensions(this.getWidgetsLeft(), this.getSelectionButtonY(), 280, 20).build());
      Text lv3 = (Text)Nullables.mapOrElse(lv, AbuseReportReason::getText, SELECT_REASON_TEXT);
      this.addDrawableChild(ButtonWidget.builder(lv3, (button) -> {
         this.client.setScreen(new AbuseReportReasonScreen(this, this.report.getReason(), (reason) -> {
            this.report.setReason(reason);
            this.onChange();
         }));
      }).dimensions(this.getWidgetsLeft(), this.getReasonButtonY(), 280, 20).build());
      this.editBox = (EditBoxWidget)this.addDrawableChild(new EditBoxWidget(this.client.textRenderer, this.getWidgetsLeft(), this.getEditBoxTop(), 280, this.getEditBoxBottom() - this.getEditBoxTop(), DESCRIBE_TEXT, Text.translatable("gui.chatReport.comments")));
      this.editBox.setText(this.report.getOpinionComments());
      this.editBox.setMaxLength(abuseReportLimits.maxOpinionCommentsLength());
      this.editBox.setChangeListener((opinionComments) -> {
         this.report.setOpinionComments(opinionComments);
         this.onChange();
      });
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.close();
      }).dimensions(i - 120, this.getBottomButtonsY(), 120, 20).build());
      this.sendButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.chatReport.send"), (button) -> {
         this.send();
      }).dimensions(i + 10, this.getBottomButtonsY(), 120, 20).build());
      this.onChange();
   }

   private void onChange() {
      this.validationError = this.report.validate();
      this.sendButton.active = this.validationError == null;
      this.sendButton.setTooltip((Tooltip)Nullables.map(this.validationError, (error) -> {
         return Tooltip.of(error.message());
      }));
   }

   private void send() {
      this.report.finalizeReport(this.context).ifLeft((report) -> {
         CompletableFuture completableFuture = this.context.getSender().send(report.id(), report.report());
         this.client.setScreen(TaskScreen.createRunningScreen(SENDING_TEXT, ScreenTexts.CANCEL, () -> {
            this.client.setScreen(this);
            completableFuture.cancel(true);
         }));
         completableFuture.handleAsync((unit, throwable) -> {
            if (throwable == null) {
               this.onSubmissionFinished();
            } else {
               if (throwable instanceof CancellationException) {
                  return null;
               }

               this.onSubmissionError(throwable);
            }

            return null;
         }, this.client);
      }).ifRight((validationError) -> {
         this.showErrorScreen(validationError.message());
      });
   }

   private void onSubmissionFinished() {
      this.clearDraft();
      this.client.setScreen(TaskScreen.createResultScreen(REPORT_SENT_TITLE, REPORT_SENT_MESSAGE_TEXT, ScreenTexts.DONE, () -> {
         this.client.setScreen((Screen)null);
      }));
   }

   private void onSubmissionError(Throwable throwable) {
      LOGGER.error("Encountered error while sending abuse report", throwable);
      Throwable var4 = throwable.getCause();
      Text lv2;
      if (var4 instanceof TextifiedException lv) {
         lv2 = lv.getMessageText();
      } else {
         lv2 = GENERIC_ERROR_TEXT;
      }

      this.showErrorScreen(lv2);
   }

   private void showErrorScreen(Text message) {
      Text lv = message.copy().formatted(Formatting.RED);
      this.client.setScreen(TaskScreen.createResultScreen(REPORT_ERROR_TITLE, lv, ScreenTexts.BACK, () -> {
         this.client.setScreen(this);
      }));
   }

   void setDraft() {
      if (this.report.hasContents()) {
         this.context.setDraft(this.report.getDraft().copy());
      }

   }

   void clearDraft() {
      this.context.setDraft((ChatAbuseReport.Draft)null);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      int k = this.width / 2;
      this.renderBackground(matrices);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, k, 10, 16777215);
      TextRenderer var10001 = this.textRenderer;
      Text var10002 = OBSERVED_WHAT_TEXT;
      int var10004 = this.getSelectionButtonY();
      Objects.requireNonNull(this.textRenderer);
      drawCenteredTextWithShadow(matrices, var10001, var10002, k, var10004 - 9 - 6, 16777215);
      int var10003;
      if (this.reasonDescription != null) {
         MultilineText var10000 = this.reasonDescription;
         int var6 = this.getWidgetsLeft();
         var10003 = this.getReasonButtonY() + 20 + 5;
         Objects.requireNonNull(this.textRenderer);
         var10000.drawWithShadow(matrices, var6, var10003, 9, 16777215);
      }

      var10001 = this.textRenderer;
      var10002 = MORE_COMMENTS_TEXT;
      var10003 = this.getWidgetsLeft();
      var10004 = this.getEditBoxTop();
      Objects.requireNonNull(this.textRenderer);
      drawTextWithShadow(matrices, var10001, var10002, var10003, var10004 - 9 - 6, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public void tick() {
      this.editBox.tick();
      super.tick();
   }

   public void close() {
      if (this.report.hasContents()) {
         this.client.setScreen(new DiscardWarningScreen());
      } else {
         this.client.setScreen(this.parent);
      }

   }

   public void removed() {
      this.setDraft();
      super.removed();
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(mouseX, mouseY, button) ? true : this.editBox.mouseReleased(mouseX, mouseY, button);
   }

   private int getWidgetsLeft() {
      return this.width / 2 - 140;
   }

   private int getWidgetsRight() {
      return this.width / 2 + 140;
   }

   private int getTop() {
      return Math.max((this.height - 300) / 2, 0);
   }

   private int getBottom() {
      return Math.min((this.height + 300) / 2, this.height);
   }

   private int getSelectionButtonY() {
      return this.getTop() + 40;
   }

   private int getReasonButtonY() {
      return this.getSelectionButtonY() + 10 + 20;
   }

   private int getEditBoxTop() {
      int i = this.getReasonButtonY() + 20 + 25;
      if (this.reasonDescription != null) {
         int var10001 = this.reasonDescription.count() + 1;
         Objects.requireNonNull(this.textRenderer);
         i += var10001 * 9;
      }

      return i;
   }

   private int getEditBoxBottom() {
      return this.getBottomButtonsY() - 20;
   }

   private int getBottomButtonsY() {
      return this.getBottom() - 20 - 10;
   }

   static {
      SENDING_TEXT = Text.translatable("gui.abuseReport.sending.title").formatted(Formatting.BOLD);
      REPORT_SENT_TITLE = Text.translatable("gui.abuseReport.sent.title").formatted(Formatting.BOLD);
      REPORT_ERROR_TITLE = Text.translatable("gui.abuseReport.error.title").formatted(Formatting.BOLD);
      GENERIC_ERROR_TEXT = Text.translatable("gui.abuseReport.send.generic_error");
      LOGGER = LogUtils.getLogger();
   }

   @Environment(EnvType.CLIENT)
   class DiscardWarningScreen extends WarningScreen {
      private static final Text TITLE;
      private static final Text MESSAGE;
      private static final Text RETURN_BUTTON_TEXT;
      private static final Text DRAFT_BUTTON_TEXT;
      private static final Text DISCARD_BUTTON_TEXT;

      protected DiscardWarningScreen() {
         super(TITLE, MESSAGE, MESSAGE);
      }

      protected void initButtons(int yOffset) {
         int j = true;
         this.addDrawableChild(ButtonWidget.builder(RETURN_BUTTON_TEXT, (button) -> {
            this.close();
         }).dimensions(this.width / 2 - 155, 100 + yOffset, 150, 20).build());
         this.addDrawableChild(ButtonWidget.builder(DRAFT_BUTTON_TEXT, (button) -> {
            ChatReportScreen.this.setDraft();
            this.client.setScreen(ChatReportScreen.this.parent);
         }).dimensions(this.width / 2 + 5, 100 + yOffset, 150, 20).build());
         this.addDrawableChild(ButtonWidget.builder(DISCARD_BUTTON_TEXT, (button) -> {
            ChatReportScreen.this.clearDraft();
            this.client.setScreen(ChatReportScreen.this.parent);
         }).dimensions(this.width / 2 - 75, 130 + yOffset, 150, 20).build());
      }

      public void close() {
         this.client.setScreen(ChatReportScreen.this);
      }

      public boolean shouldCloseOnEsc() {
         return false;
      }

      protected void drawTitle(MatrixStack matrices) {
         drawTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2 - 155, 30, 16777215);
      }

      static {
         TITLE = Text.translatable("gui.chatReport.discard.title").formatted(Formatting.BOLD);
         MESSAGE = Text.translatable("gui.chatReport.discard.content");
         RETURN_BUTTON_TEXT = Text.translatable("gui.chatReport.discard.return");
         DRAFT_BUTTON_TEXT = Text.translatable("gui.chatReport.discard.draft");
         DISCARD_BUTTON_TEXT = Text.translatable("gui.chatReport.discard.discard");
      }
   }
}
