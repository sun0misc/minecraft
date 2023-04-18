package net.minecraft.client.gui.screen.report;

import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.report.AbuseReportReason;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AbuseReportReasonScreen extends Screen {
   private static final Text TITLE_TEXT = Text.translatable("gui.abuseReport.reason.title");
   private static final Text DESCRIPTION_TEXT = Text.translatable("gui.abuseReport.reason.description");
   private static final Text READ_INFO_TEXT = Text.translatable("gui.chatReport.read_info");
   private static final int REASON_LIST_BOTTOM_MARGIN = 95;
   private static final int DONE_BUTTON_WIDTH = 150;
   private static final int DONE_BUTTON_HEIGHT = 20;
   private static final int SCREEN_WIDTH = 320;
   private static final int TOP_MARGIN = 4;
   @Nullable
   private final Screen parent;
   @Nullable
   private ReasonListWidget reasonList;
   @Nullable
   AbuseReportReason reason;
   private final Consumer reasonConsumer;

   public AbuseReportReasonScreen(@Nullable Screen parent, @Nullable AbuseReportReason reason, Consumer reasonConsumer) {
      super(TITLE_TEXT);
      this.parent = parent;
      this.reason = reason;
      this.reasonConsumer = reasonConsumer;
   }

   protected void init() {
      this.reasonList = new ReasonListWidget(this.client);
      this.reasonList.setRenderBackground(false);
      this.addSelectableChild(this.reasonList);
      AbuseReportReason var10000 = this.reason;
      ReasonListWidget var10001 = this.reasonList;
      Objects.requireNonNull(var10001);
      ReasonListWidget.ReasonEntry lv = (ReasonListWidget.ReasonEntry)Nullables.map(var10000, var10001::getEntry);
      this.reasonList.setSelected(lv);
      int i = this.width / 2 - 150 - 5;
      this.addDrawableChild(ButtonWidget.builder(READ_INFO_TEXT, (button) -> {
         this.client.setScreen(new ConfirmLinkScreen((confirmed) -> {
            if (confirmed) {
               Util.getOperatingSystem().open("https://aka.ms/aboutjavareporting");
            }

            this.client.setScreen(this);
         }, "https://aka.ms/aboutjavareporting", true));
      }).dimensions(i, this.getDoneButtonY(), 150, 20).build());
      int j = this.width / 2 + 5;
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         ReasonListWidget.ReasonEntry lv = (ReasonListWidget.ReasonEntry)this.reasonList.getSelectedOrNull();
         if (lv != null) {
            this.reasonConsumer.accept(lv.getReason());
         }

         this.client.setScreen(this.parent);
      }).dimensions(j, this.getDoneButtonY(), 150, 20).build());
      super.init();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.reasonList.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 16, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
      fill(matrices, this.getLeft(), this.getTop(), this.getRight(), this.getBottom(), 2130706432);
      drawTextWithShadow(matrices, this.textRenderer, DESCRIPTION_TEXT, this.getLeft() + 4, this.getTop() + 4, -8421505);
      ReasonListWidget.ReasonEntry lv = (ReasonListWidget.ReasonEntry)this.reasonList.getSelectedOrNull();
      if (lv != null) {
         int k = this.getLeft() + 4 + 16;
         int l = this.getRight() - 4;
         int var10000 = this.getTop() + 4;
         Objects.requireNonNull(this.textRenderer);
         int m = var10000 + 9 + 2;
         int n = this.getBottom() - 4;
         int o = l - k;
         int p = n - m;
         int q = this.textRenderer.getWrappedLinesHeight((StringVisitable)lv.reason.getDescription(), o);
         this.textRenderer.drawTrimmed(matrices, lv.reason.getDescription(), k, m + (p - q) / 2, o, -1);
      }

   }

   private int getDoneButtonY() {
      return this.height - 20 - 4;
   }

   private int getLeft() {
      return (this.width - 320) / 2;
   }

   private int getRight() {
      return (this.width + 320) / 2;
   }

   private int getTop() {
      return this.height - 95 + 4;
   }

   private int getBottom() {
      return this.getDoneButtonY() - 4;
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   @Environment(EnvType.CLIENT)
   public class ReasonListWidget extends AlwaysSelectedEntryListWidget {
      public ReasonListWidget(MinecraftClient client) {
         super(client, AbuseReportReasonScreen.this.width, AbuseReportReasonScreen.this.height, 40, AbuseReportReasonScreen.this.height - 95, 18);
         AbuseReportReason[] var3 = AbuseReportReason.values();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            AbuseReportReason lv = var3[var5];
            this.addEntry(new ReasonEntry(lv));
         }

      }

      @Nullable
      public ReasonEntry getEntry(AbuseReportReason reason) {
         return (ReasonEntry)this.children().stream().filter((entry) -> {
            return entry.reason == reason;
         }).findFirst().orElse((Object)null);
      }

      public int getRowWidth() {
         return 320;
      }

      protected int getScrollbarPositionX() {
         return this.getRowRight() - 2;
      }

      public void setSelected(@Nullable ReasonEntry arg) {
         super.setSelected(arg);
         AbuseReportReasonScreen.this.reason = arg != null ? arg.getReason() : null;
      }

      @Environment(EnvType.CLIENT)
      public class ReasonEntry extends AlwaysSelectedEntryListWidget.Entry {
         final AbuseReportReason reason;

         public ReasonEntry(AbuseReportReason reason) {
            this.reason = reason;
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int p = x + 1;
            Objects.requireNonNull(AbuseReportReasonScreen.this.textRenderer);
            int q = y + (entryHeight - 9) / 2 + 1;
            DrawableHelper.drawTextWithShadow(matrices, AbuseReportReasonScreen.this.textRenderer, (Text)this.reason.getText(), p, q, -1);
         }

         public Text getNarration() {
            return Text.translatable("gui.abuseReport.reason.narration", this.reason.getText(), this.reason.getDescription());
         }

         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
               ReasonListWidget.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }

         public AbuseReportReason getReason() {
            return this.reason;
         }
      }
   }
}
