package net.minecraft.client.gui.screen.report;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.network.message.MessageTrustStatus;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.report.ChatAbuseReport;
import net.minecraft.client.report.MessagesListAdder;
import net.minecraft.client.report.log.ReceivedMessage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Nullables;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChatSelectionScreen extends Screen {
   private static final Text TITLE = Text.translatable("gui.chatSelection.title");
   private static final Text CONTEXT_MESSAGE;
   @Nullable
   private final Screen parent;
   private final AbuseReportContext reporter;
   private ButtonWidget doneButton;
   private MultilineText contextMessage;
   @Nullable
   private SelectionListWidget selectionList;
   final ChatAbuseReport report;
   private final Consumer newReportConsumer;
   private MessagesListAdder listAdder;

   public ChatSelectionScreen(@Nullable Screen parent, AbuseReportContext reporter, ChatAbuseReport report, Consumer newReportConsumer) {
      super(TITLE);
      this.parent = parent;
      this.reporter = reporter;
      this.report = report.copy();
      this.newReportConsumer = newReportConsumer;
   }

   protected void init() {
      this.listAdder = new MessagesListAdder(this.reporter, this::isSentByReportedPlayer);
      this.contextMessage = MultilineText.create(this.textRenderer, CONTEXT_MESSAGE, this.width - 16);
      MinecraftClient var10004 = this.client;
      int var10005 = this.contextMessage.count() + 1;
      Objects.requireNonNull(this.textRenderer);
      this.selectionList = new SelectionListWidget(var10004, var10005 * 9);
      this.selectionList.setRenderBackground(false);
      this.addSelectableChild(this.selectionList);
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.close();
      }).dimensions(this.width / 2 - 155, this.height - 32, 150, 20).build());
      this.doneButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.newReportConsumer.accept(this.report);
         this.close();
      }).dimensions(this.width / 2 - 155 + 160, this.height - 32, 150, 20).build());
      this.setDoneButtonActivation();
      this.addMessages();
      this.selectionList.setScrollAmount((double)this.selectionList.getMaxScroll());
   }

   private boolean isSentByReportedPlayer(ReceivedMessage message) {
      return message.isSentFrom(this.report.getReportedPlayerUuid());
   }

   private void addMessages() {
      int i = this.selectionList.getDisplayedItemCount();
      this.listAdder.add(i, this.selectionList);
   }

   void addMoreMessages() {
      this.addMessages();
   }

   void setDoneButtonActivation() {
      this.doneButton.active = !this.report.getSelections().isEmpty();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.selectionList.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 16, 16777215);
      AbuseReportLimits abuseReportLimits = this.reporter.getSender().getLimits();
      int k = this.report.getSelections().size();
      int l = abuseReportLimits.maxReportedMessageCount();
      Text lv = Text.translatable("gui.chatSelection.selected", k, l);
      TextRenderer var10001 = this.textRenderer;
      int var10003 = this.width / 2;
      Objects.requireNonNull(this.textRenderer);
      drawCenteredTextWithShadow(matrices, var10001, lv, var10003, 16 + 9 * 3 / 2, 10526880);
      this.contextMessage.drawCenterWithShadow(matrices, this.width / 2, this.selectionList.getContextMessageY());
      super.render(matrices, mouseX, mouseY, delta);
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinSentences(super.getNarratedTitle(), CONTEXT_MESSAGE);
   }

   static {
      CONTEXT_MESSAGE = Text.translatable("gui.chatSelection.context").formatted(Formatting.GRAY);
   }

   @Environment(EnvType.CLIENT)
   public class SelectionListWidget extends AlwaysSelectedEntryListWidget implements MessagesListAdder.MessagesList {
      @Nullable
      private SenderEntryPair lastSenderEntryPair;

      public SelectionListWidget(MinecraftClient client, int contextMessagesHeight) {
         super(client, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height, 40, ChatSelectionScreen.this.height - 40 - contextMessagesHeight, 16);
      }

      public void setScrollAmount(double amount) {
         double e = this.getScrollAmount();
         super.setScrollAmount(amount);
         if ((float)this.getMaxScroll() > 1.0E-5F && amount <= 9.999999747378752E-6 && !MathHelper.approximatelyEquals(amount, e)) {
            ChatSelectionScreen.this.addMoreMessages();
         }

      }

      public void addMessage(int index, ReceivedMessage.ChatMessage message) {
         boolean bl = message.isSentFrom(ChatSelectionScreen.this.report.getReportedPlayerUuid());
         MessageTrustStatus lv = message.trustStatus();
         MessageIndicator lv2 = lv.createIndicator(message.message());
         Entry lv3 = new MessageEntry(index, message.getContent(), message.getNarration(), lv2, bl, true);
         this.addEntryToTop(lv3);
         this.addSenderEntry(message, bl);
      }

      private void addSenderEntry(ReceivedMessage.ChatMessage message, boolean fromReportedPlayer) {
         Entry lv = new SenderEntry(message.profile(), message.getHeadingText(), fromReportedPlayer);
         this.addEntryToTop(lv);
         SenderEntryPair lv2 = new SenderEntryPair(message.getSenderUuid(), lv);
         if (this.lastSenderEntryPair != null && this.lastSenderEntryPair.senderEquals(lv2)) {
            this.removeEntryWithoutScrolling(this.lastSenderEntryPair.entry());
         }

         this.lastSenderEntryPair = lv2;
      }

      public void addText(Text text) {
         this.addEntryToTop(new SeparatorEntry());
         this.addEntryToTop(new TextEntry(text));
         this.addEntryToTop(new SeparatorEntry());
         this.lastSenderEntryPair = null;
      }

      protected int getScrollbarPositionX() {
         return (this.width + this.getRowWidth()) / 2;
      }

      public int getRowWidth() {
         return Math.min(350, this.width - 50);
      }

      public int getDisplayedItemCount() {
         return MathHelper.ceilDiv(this.bottom - this.top, this.itemHeight);
      }

      protected void renderEntry(MatrixStack matrices, int mouseX, int mouseY, float delta, int index, int x, int y, int entryWidth, int entryHeight) {
         Entry lv = (Entry)this.getEntry(index);
         if (this.shouldHighlight(lv)) {
            boolean bl = this.getSelectedOrNull() == lv;
            int p = this.isFocused() && bl ? -1 : -8355712;
            this.drawSelectionHighlight(matrices, y, entryWidth, entryHeight, p, -16777216);
         }

         lv.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, this.getHoveredEntry() == lv, delta);
      }

      private boolean shouldHighlight(Entry entry) {
         if (entry.canSelect()) {
            boolean bl = this.getSelectedOrNull() == entry;
            boolean bl2 = this.getSelectedOrNull() == null;
            boolean bl3 = this.getHoveredEntry() == entry;
            return bl || bl2 && bl3 && entry.isHighlightedOnHover();
         } else {
            return false;
         }
      }

      @Nullable
      protected Entry getNeighboringEntry(NavigationDirection arg) {
         return (Entry)this.getNeighboringEntry(arg, Entry::canSelect);
      }

      public void setSelected(@Nullable Entry arg) {
         super.setSelected(arg);
         Entry lv = this.getNeighboringEntry(NavigationDirection.UP);
         if (lv == null) {
            ChatSelectionScreen.this.addMoreMessages();
         }

      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         Entry lv = (Entry)this.getSelectedOrNull();
         return lv != null && lv.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
      }

      public int getContextMessageY() {
         int var10000 = this.bottom;
         Objects.requireNonNull(ChatSelectionScreen.this.textRenderer);
         return var10000 + 9;
      }

      // $FF: synthetic method
      @Nullable
      protected EntryListWidget.Entry getNeighboringEntry(NavigationDirection direction) {
         return this.getNeighboringEntry(direction);
      }

      @Environment(EnvType.CLIENT)
      public class MessageEntry extends Entry {
         private static final Identifier CHECKMARK = new Identifier("minecraft", "textures/gui/checkmark.png");
         private static final int CHECKMARK_WIDTH = 9;
         private static final int CHECKMARK_HEIGHT = 8;
         private static final int CHAT_MESSAGE_LEFT_MARGIN = 11;
         private static final int INDICATOR_LEFT_MARGIN = 4;
         private final int index;
         private final StringVisitable truncatedContent;
         private final Text narration;
         @Nullable
         private final List fullContent;
         @Nullable
         private final MessageIndicator.Icon indicatorIcon;
         @Nullable
         private final List originalContent;
         private final boolean fromReportedPlayer;
         private final boolean isChatMessage;

         public MessageEntry(int index, Text message, Text narration, @Nullable MessageIndicator indicator, boolean fromReportedPlayer, boolean isChatMessage) {
            super();
            this.index = index;
            this.indicatorIcon = (MessageIndicator.Icon)Nullables.map(indicator, MessageIndicator::icon);
            this.originalContent = indicator != null && indicator.text() != null ? ChatSelectionScreen.this.textRenderer.wrapLines(indicator.text(), SelectionListWidget.this.getRowWidth()) : null;
            this.fromReportedPlayer = fromReportedPlayer;
            this.isChatMessage = isChatMessage;
            StringVisitable lv = ChatSelectionScreen.this.textRenderer.trimToWidth((StringVisitable)message, this.getTextWidth() - ChatSelectionScreen.this.textRenderer.getWidth((StringVisitable)ScreenTexts.ELLIPSIS));
            if (message != lv) {
               this.truncatedContent = StringVisitable.concat(lv, ScreenTexts.ELLIPSIS);
               this.fullContent = ChatSelectionScreen.this.textRenderer.wrapLines(message, SelectionListWidget.this.getRowWidth());
            } else {
               this.truncatedContent = message;
               this.fullContent = null;
            }

            this.narration = narration;
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.isSelected() && this.fromReportedPlayer) {
               this.drawCheckmark(matrices, y, x, entryHeight);
            }

            int p = x + this.getIndent();
            int var10000 = y + 1;
            Objects.requireNonNull(ChatSelectionScreen.this.textRenderer);
            int q = var10000 + (entryHeight - 9) / 2;
            DrawableHelper.drawTextWithShadow(matrices, ChatSelectionScreen.this.textRenderer, Language.getInstance().reorder(this.truncatedContent), p, q, this.fromReportedPlayer ? -1 : -1593835521);
            if (this.fullContent != null && hovered) {
               ChatSelectionScreen.this.setTooltip(this.fullContent);
            }

            int r = ChatSelectionScreen.this.textRenderer.getWidth(this.truncatedContent);
            this.renderIndicator(matrices, p + r + 4, y, entryHeight, mouseX, mouseY);
         }

         private void renderIndicator(MatrixStack matrices, int x, int y, int entryHeight, int mouseX, int mouseY) {
            if (this.indicatorIcon != null) {
               int n = y + (entryHeight - this.indicatorIcon.height) / 2;
               this.indicatorIcon.draw(matrices, x, n);
               if (this.originalContent != null && mouseX >= x && mouseX <= x + this.indicatorIcon.width && mouseY >= n && mouseY <= n + this.indicatorIcon.height) {
                  ChatSelectionScreen.this.setTooltip(this.originalContent);
               }
            }

         }

         private void drawCheckmark(MatrixStack matrices, int y, int x, int entryHeight) {
            int m = y + (entryHeight - 8) / 2;
            RenderSystem.setShaderTexture(0, CHECKMARK);
            RenderSystem.enableBlend();
            DrawableHelper.drawTexture(matrices, x, m, 0.0F, 0.0F, 9, 8, 9, 8);
            RenderSystem.disableBlend();
         }

         private int getTextWidth() {
            int i = this.indicatorIcon != null ? this.indicatorIcon.width + 4 : 0;
            return SelectionListWidget.this.getRowWidth() - this.getIndent() - 4 - i;
         }

         private int getIndent() {
            return this.isChatMessage ? 11 : 0;
         }

         public Text getNarration() {
            return (Text)(this.isSelected() ? Text.translatable("narrator.select", this.narration) : this.narration);
         }

         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
               SelectionListWidget.this.setSelected((Entry)null);
               return this.toggle();
            } else {
               return false;
            }
         }

         public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return KeyCodes.isToggle(keyCode) ? this.toggle() : false;
         }

         public boolean isSelected() {
            return ChatSelectionScreen.this.report.hasSelectedMessage(this.index);
         }

         public boolean canSelect() {
            return true;
         }

         public boolean isHighlightedOnHover() {
            return this.fromReportedPlayer;
         }

         private boolean toggle() {
            if (this.fromReportedPlayer) {
               ChatSelectionScreen.this.report.toggleMessageSelection(this.index);
               ChatSelectionScreen.this.setDoneButtonActivation();
               return true;
            } else {
               return false;
            }
         }
      }

      @Environment(EnvType.CLIENT)
      public class SenderEntry extends Entry {
         private static final int PLAYER_SKIN_SIZE = 12;
         private final Text headingText;
         private final Identifier skinTextureId;
         private final boolean fromReportedPlayer;

         public SenderEntry(GameProfile gameProfile, Text headingText, boolean fromReportedPlayer) {
            super();
            this.headingText = headingText;
            this.fromReportedPlayer = fromReportedPlayer;
            this.skinTextureId = SelectionListWidget.this.client.getSkinProvider().loadSkin(gameProfile);
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int p = x - 12 - 4;
            int q = y + (entryHeight - 12) / 2;
            this.drawSkin(matrices, p, q, this.skinTextureId);
            int var10000 = y + 1;
            Objects.requireNonNull(ChatSelectionScreen.this.textRenderer);
            int r = var10000 + (entryHeight - 9) / 2;
            DrawableHelper.drawTextWithShadow(matrices, ChatSelectionScreen.this.textRenderer, this.headingText, x, r, this.fromReportedPlayer ? -1 : -1593835521);
         }

         private void drawSkin(MatrixStack matrices, int x, int y, Identifier skinTextureId) {
            RenderSystem.setShaderTexture(0, skinTextureId);
            PlayerSkinDrawer.draw(matrices, x, y, 12);
         }
      }

      @Environment(EnvType.CLIENT)
      private static record SenderEntryPair(UUID sender, Entry entry) {
         SenderEntryPair(UUID uUID, Entry arg) {
            this.sender = uUID;
            this.entry = arg;
         }

         public boolean senderEquals(SenderEntryPair pair) {
            return pair.sender.equals(this.sender);
         }

         public UUID sender() {
            return this.sender;
         }

         public Entry entry() {
            return this.entry;
         }
      }

      @Environment(EnvType.CLIENT)
      public abstract class Entry extends AlwaysSelectedEntryListWidget.Entry {
         public Text getNarration() {
            return ScreenTexts.EMPTY;
         }

         public boolean isSelected() {
            return false;
         }

         public boolean canSelect() {
            return false;
         }

         public boolean isHighlightedOnHover() {
            return this.canSelect();
         }
      }

      @Environment(EnvType.CLIENT)
      public class SeparatorEntry extends Entry {
         public SeparatorEntry() {
            super();
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         }
      }

      @Environment(EnvType.CLIENT)
      public class TextEntry extends Entry {
         private static final int TEXT_COLOR = -6250336;
         private final Text text;

         public TextEntry(Text text) {
            super();
            this.text = text;
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int p = y + entryHeight / 2;
            int q = x + entryWidth - 8;
            int r = ChatSelectionScreen.this.textRenderer.getWidth((StringVisitable)this.text);
            int s = (x + q - r) / 2;
            Objects.requireNonNull(ChatSelectionScreen.this.textRenderer);
            int t = p - 9 / 2;
            DrawableHelper.drawTextWithShadow(matrices, ChatSelectionScreen.this.textRenderer, this.text, s, t, -6250336);
         }

         public Text getNarration() {
            return this.text;
         }
      }
   }
}
