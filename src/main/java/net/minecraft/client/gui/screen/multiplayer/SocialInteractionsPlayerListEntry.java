package net.minecraft.client.gui.screen.multiplayer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.report.ChatReportScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SocialInteractionsPlayerListEntry extends ElementListWidget.Entry {
   private static final Identifier REPORT_BUTTON_TEXTURE = new Identifier("textures/gui/report_button.png");
   private static final int field_32418 = 10;
   private final MinecraftClient client;
   private final List buttons;
   private final UUID uuid;
   private final String name;
   private final Supplier skinTexture;
   private boolean offline;
   private boolean sentMessage;
   private final boolean canSendReports;
   private final boolean reportable;
   private final boolean hasDraftReport;
   @Nullable
   private ButtonWidget hideButton;
   @Nullable
   private ButtonWidget showButton;
   @Nullable
   private ButtonWidget reportButton;
   private float timeCounter;
   private static final Text HIDDEN_TEXT;
   private static final Text BLOCKED_TEXT;
   private static final Text OFFLINE_TEXT;
   private static final Text HIDDEN_OFFLINE_TEXT;
   private static final Text BLOCKED_OFFLINE_TEXT;
   private static final Text REPORT_DISABLED_TEXT;
   private static final Text NOT_REPORTABLE_TEXT;
   private static final Text hideText;
   private static final Text showText;
   private static final Text reportText;
   private static final int field_32420 = 24;
   private static final int field_32421 = 4;
   private static final int field_32422 = 20;
   private static final int field_32416 = 0;
   private static final int field_32417 = 38;
   public static final int BLACK_COLOR;
   public static final int GRAY_COLOR;
   public static final int DARK_GRAY_COLOR;
   public static final int WHITE_COLOR;
   public static final int LIGHT_GRAY_COLOR;

   public SocialInteractionsPlayerListEntry(MinecraftClient client, SocialInteractionsScreen parent, UUID uuid, String name, Supplier skinTexture, boolean reportable) {
      this.client = client;
      this.uuid = uuid;
      this.name = name;
      this.skinTexture = skinTexture;
      AbuseReportContext lv = client.getAbuseReportContext();
      this.canSendReports = lv.getSender().canSendReports();
      this.reportable = reportable;
      this.hasDraftReport = lv.draftPlayerUuidEquals(uuid);
      Text lv2 = Text.translatable("gui.socialInteractions.narration.hide", name);
      Text lv3 = Text.translatable("gui.socialInteractions.narration.show", name);
      SocialInteractionsManager lv4 = client.getSocialInteractionsManager();
      boolean bl2 = client.getChatRestriction().allowsChat(client.isInSingleplayer());
      boolean bl3 = !client.player.getUuid().equals(uuid);
      if (bl3 && bl2 && !lv4.isPlayerBlocked(uuid)) {
         this.reportButton = new TexturedButtonWidget(0, 0, 20, 20, 0, 0, 20, REPORT_BUTTON_TEXTURE, 64, 64, (button) -> {
            lv.tryShowDraftScreen(client, parent, () -> {
               client.setScreen(new ChatReportScreen(parent, lv, uuid));
            }, false);
         }, Text.translatable("gui.socialInteractions.report")) {
            protected MutableText getNarrationMessage() {
               return SocialInteractionsPlayerListEntry.this.getNarrationMessage(super.getNarrationMessage());
            }
         };
         this.reportButton.setTooltip(this.getReportButtonTooltip());
         this.reportButton.setTooltipDelay(10);
         this.hideButton = new TexturedButtonWidget(0, 0, 20, 20, 0, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_TEXTURE, 256, 256, (button) -> {
            lv4.hidePlayer(uuid);
            this.onButtonClick(true, Text.translatable("gui.socialInteractions.hidden_in_chat", name));
         }, Text.translatable("gui.socialInteractions.hide")) {
            protected MutableText getNarrationMessage() {
               return SocialInteractionsPlayerListEntry.this.getNarrationMessage(super.getNarrationMessage());
            }
         };
         this.hideButton.setTooltip(Tooltip.of(hideText, lv2));
         this.hideButton.setTooltipDelay(10);
         this.showButton = new TexturedButtonWidget(0, 0, 20, 20, 20, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_TEXTURE, 256, 256, (button) -> {
            lv4.showPlayer(uuid);
            this.onButtonClick(false, Text.translatable("gui.socialInteractions.shown_in_chat", name));
         }, Text.translatable("gui.socialInteractions.show")) {
            protected MutableText getNarrationMessage() {
               return SocialInteractionsPlayerListEntry.this.getNarrationMessage(super.getNarrationMessage());
            }
         };
         this.showButton.setTooltip(Tooltip.of(showText, lv3));
         this.showButton.setTooltipDelay(10);
         this.reportButton.active = false;
         this.buttons = new ArrayList();
         this.buttons.add(this.hideButton);
         this.buttons.add(this.reportButton);
         this.setShowButtonVisible(lv4.isPlayerHidden(this.uuid));
      } else {
         this.buttons = ImmutableList.of();
      }

   }

   private Tooltip getReportButtonTooltip() {
      if (!this.reportable) {
         return Tooltip.of(NOT_REPORTABLE_TEXT);
      } else if (!this.canSendReports) {
         return Tooltip.of(REPORT_DISABLED_TEXT);
      } else {
         return !this.sentMessage ? Tooltip.of(Text.translatable("gui.socialInteractions.tooltip.report.no_messages", this.name)) : Tooltip.of(reportText, Text.translatable("gui.socialInteractions.narration.report", this.name));
      }
   }

   public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
      int p = x + 4;
      int q = y + (entryHeight - 24) / 2;
      int r = p + 24 + 4;
      Text lv = this.getStatusText();
      int s;
      if (lv == ScreenTexts.EMPTY) {
         DrawableHelper.fill(matrices, x, y, x + entryWidth, y + entryHeight, GRAY_COLOR);
         Objects.requireNonNull(this.client.textRenderer);
         s = y + (entryHeight - 9) / 2;
      } else {
         DrawableHelper.fill(matrices, x, y, x + entryWidth, y + entryHeight, DARK_GRAY_COLOR);
         Objects.requireNonNull(this.client.textRenderer);
         Objects.requireNonNull(this.client.textRenderer);
         s = y + (entryHeight - (9 + 9)) / 2;
         this.client.textRenderer.draw(matrices, lv, (float)r, (float)(s + 12), LIGHT_GRAY_COLOR);
      }

      RenderSystem.setShaderTexture(0, (Identifier)this.skinTexture.get());
      PlayerSkinDrawer.draw(matrices, p, q, 24);
      this.client.textRenderer.draw(matrices, this.name, (float)r, (float)s, WHITE_COLOR);
      if (this.offline) {
         DrawableHelper.fill(matrices, p, q, p + 24, q + 24, BLACK_COLOR);
      }

      if (this.hideButton != null && this.showButton != null && this.reportButton != null) {
         float g = this.timeCounter;
         this.hideButton.setX(x + (entryWidth - this.hideButton.getWidth() - 4) - 20 - 4);
         this.hideButton.setY(y + (entryHeight - this.hideButton.getHeight()) / 2);
         this.hideButton.render(matrices, mouseX, mouseY, tickDelta);
         this.showButton.setX(x + (entryWidth - this.showButton.getWidth() - 4) - 20 - 4);
         this.showButton.setY(y + (entryHeight - this.showButton.getHeight()) / 2);
         this.showButton.render(matrices, mouseX, mouseY, tickDelta);
         this.reportButton.setX(x + (entryWidth - this.showButton.getWidth() - 4));
         this.reportButton.setY(y + (entryHeight - this.showButton.getHeight()) / 2);
         this.reportButton.render(matrices, mouseX, mouseY, tickDelta);
         if (g == this.timeCounter) {
            this.timeCounter = 0.0F;
         }
      }

      if (this.hasDraftReport && this.reportButton != null) {
         RenderSystem.setShaderTexture(0, ClickableWidget.WIDGETS_TEXTURE);
         DrawableHelper.drawTexture(matrices, this.reportButton.getX() + 5, this.reportButton.getY() + 1, 182.0F, 24.0F, 15, 15, 256, 256);
      }

   }

   public List children() {
      return this.buttons;
   }

   public List selectableChildren() {
      return this.buttons;
   }

   public String getName() {
      return this.name;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public void setOffline(boolean offline) {
      this.offline = offline;
   }

   public boolean isOffline() {
      return this.offline;
   }

   public void setSentMessage(boolean sentMessage) {
      this.sentMessage = sentMessage;
      if (this.reportButton != null) {
         this.reportButton.active = this.canSendReports && this.reportable && sentMessage;
         this.reportButton.setTooltip(this.getReportButtonTooltip());
      }

   }

   public boolean hasSentMessage() {
      return this.sentMessage;
   }

   private void onButtonClick(boolean showButtonVisible, Text chatMessage) {
      this.setShowButtonVisible(showButtonVisible);
      this.client.inGameHud.getChatHud().addMessage(chatMessage);
      this.client.getNarratorManager().narrate(chatMessage);
   }

   private void setShowButtonVisible(boolean showButtonVisible) {
      this.showButton.visible = showButtonVisible;
      this.hideButton.visible = !showButtonVisible;
      this.buttons.set(0, showButtonVisible ? this.showButton : this.hideButton);
   }

   MutableText getNarrationMessage(MutableText text) {
      Text lv = this.getStatusText();
      return lv == ScreenTexts.EMPTY ? Text.literal(this.name).append(", ").append((Text)text) : Text.literal(this.name).append(", ").append(lv).append(", ").append((Text)text);
   }

   private Text getStatusText() {
      boolean bl = this.client.getSocialInteractionsManager().isPlayerHidden(this.uuid);
      boolean bl2 = this.client.getSocialInteractionsManager().isPlayerBlocked(this.uuid);
      if (bl2 && this.offline) {
         return BLOCKED_OFFLINE_TEXT;
      } else if (bl && this.offline) {
         return HIDDEN_OFFLINE_TEXT;
      } else if (bl2) {
         return BLOCKED_TEXT;
      } else if (bl) {
         return HIDDEN_TEXT;
      } else {
         return this.offline ? OFFLINE_TEXT : ScreenTexts.EMPTY;
      }
   }

   static {
      HIDDEN_TEXT = Text.translatable("gui.socialInteractions.status_hidden").formatted(Formatting.ITALIC);
      BLOCKED_TEXT = Text.translatable("gui.socialInteractions.status_blocked").formatted(Formatting.ITALIC);
      OFFLINE_TEXT = Text.translatable("gui.socialInteractions.status_offline").formatted(Formatting.ITALIC);
      HIDDEN_OFFLINE_TEXT = Text.translatable("gui.socialInteractions.status_hidden_offline").formatted(Formatting.ITALIC);
      BLOCKED_OFFLINE_TEXT = Text.translatable("gui.socialInteractions.status_blocked_offline").formatted(Formatting.ITALIC);
      REPORT_DISABLED_TEXT = Text.translatable("gui.socialInteractions.tooltip.report.disabled");
      NOT_REPORTABLE_TEXT = Text.translatable("gui.socialInteractions.tooltip.report.not_reportable");
      hideText = Text.translatable("gui.socialInteractions.tooltip.hide");
      showText = Text.translatable("gui.socialInteractions.tooltip.show");
      reportText = Text.translatable("gui.socialInteractions.tooltip.report");
      BLACK_COLOR = ColorHelper.Argb.getArgb(190, 0, 0, 0);
      GRAY_COLOR = ColorHelper.Argb.getArgb(255, 74, 74, 74);
      DARK_GRAY_COLOR = ColorHelper.Argb.getArgb(255, 48, 48, 48);
      WHITE_COLOR = ColorHelper.Argb.getArgb(255, 255, 255, 255);
      LIGHT_GRAY_COLOR = ColorHelper.Argb.getArgb(140, 255, 255, 255);
   }
}
