/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.report;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.network.message.MessageTrustStatus;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ChatAbuseReport;
import net.minecraft.client.session.report.MessagesListAdder;
import net.minecraft.client.session.report.log.ReceivedMessage;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Nullables;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatSelectionScreen
extends Screen {
    static final Identifier CHECKMARK_ICON_TEXTURE = Identifier.method_60656("icon/checkmark");
    private static final Text TITLE_TEXT = Text.translatable("gui.chatSelection.title");
    private static final Text CONTEXT_TEXT = Text.translatable("gui.chatSelection.context");
    @Nullable
    private final Screen parent;
    private final AbuseReportContext reporter;
    private ButtonWidget doneButton;
    private MultilineText contextMessage;
    @Nullable
    private SelectionListWidget selectionList;
    final ChatAbuseReport.Builder report;
    private final Consumer<ChatAbuseReport.Builder> newReportConsumer;
    private MessagesListAdder listAdder;

    public ChatSelectionScreen(@Nullable Screen parent, AbuseReportContext reporter, ChatAbuseReport.Builder report, Consumer<ChatAbuseReport.Builder> newReportConsumer) {
        super(TITLE_TEXT);
        this.parent = parent;
        this.reporter = reporter;
        this.report = report.copy();
        this.newReportConsumer = newReportConsumer;
    }

    @Override
    protected void init() {
        this.listAdder = new MessagesListAdder(this.reporter, this::isSentByReportedPlayer);
        this.contextMessage = MultilineText.create(this.textRenderer, (StringVisitable)CONTEXT_TEXT, this.width - 16);
        this.selectionList = this.addDrawableChild(new SelectionListWidget(this.client, (this.contextMessage.count() + 1) * this.textRenderer.fontHeight));
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).dimensions(this.width / 2 - 155, this.height - 32, 150, 20).build());
        this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            this.newReportConsumer.accept(this.report);
            this.close();
        }).dimensions(this.width / 2 - 155 + 160, this.height - 32, 150, 20).build());
        this.setDoneButtonActivation();
        this.addMessages();
        this.selectionList.setScrollAmount(this.selectionList.getMaxScroll());
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
        this.doneButton.active = !this.report.getSelectedMessages().isEmpty();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        AbuseReportLimits abuseReportLimits = this.reporter.getSender().getLimits();
        int k = this.report.getSelectedMessages().size();
        int l = abuseReportLimits.maxReportedMessageCount();
        MutableText lv = Text.translatable("gui.chatSelection.selected", k, l);
        context.drawCenteredTextWithShadow(this.textRenderer, lv, this.width / 2, 16 + this.textRenderer.fontHeight * 3 / 2, Colors.WHITE);
        this.contextMessage.drawCenterWithShadow(context, this.width / 2, this.selectionList.getContextMessageY());
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), CONTEXT_TEXT);
    }

    @Environment(value=EnvType.CLIENT)
    public class SelectionListWidget
    extends AlwaysSelectedEntryListWidget<Entry>
    implements MessagesListAdder.MessagesList {
        @Nullable
        private SenderEntryPair lastSenderEntryPair;

        public SelectionListWidget(MinecraftClient client, int contextMessagesHeight) {
            super(client, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height - contextMessagesHeight - 80, 40, 16);
        }

        @Override
        public void setScrollAmount(double amount) {
            double e = this.getScrollAmount();
            super.setScrollAmount(amount);
            if ((float)this.getMaxScroll() > 1.0E-5f && amount <= (double)1.0E-5f && !MathHelper.approximatelyEquals(amount, e)) {
                ChatSelectionScreen.this.addMoreMessages();
            }
        }

        @Override
        public void addMessage(int index, ReceivedMessage.ChatMessage message) {
            boolean bl = message.isSentFrom(ChatSelectionScreen.this.report.getReportedPlayerUuid());
            MessageTrustStatus lv = message.trustStatus();
            MessageIndicator lv2 = lv.createIndicator(message.message());
            MessageEntry lv3 = new MessageEntry(index, message.getContent(), message.getNarration(), lv2, bl, true);
            this.addEntryToTop(lv3);
            this.addSenderEntry(message, bl);
        }

        private void addSenderEntry(ReceivedMessage.ChatMessage message, boolean fromReportedPlayer) {
            SenderEntry lv = new SenderEntry(message.profile(), message.getHeadingText(), fromReportedPlayer);
            this.addEntryToTop(lv);
            SenderEntryPair lv2 = new SenderEntryPair(message.getSenderUuid(), lv);
            if (this.lastSenderEntryPair != null && this.lastSenderEntryPair.senderEquals(lv2)) {
                this.removeEntryWithoutScrolling(this.lastSenderEntryPair.entry());
            }
            this.lastSenderEntryPair = lv2;
        }

        @Override
        public void addText(Text text) {
            this.addEntryToTop(new SeparatorEntry(this));
            this.addEntryToTop(new TextEntry(text));
            this.addEntryToTop(new SeparatorEntry(this));
            this.lastSenderEntryPair = null;
        }

        @Override
        public int getRowWidth() {
            return Math.min(350, this.width - 50);
        }

        public int getDisplayedItemCount() {
            return MathHelper.ceilDiv(this.height, this.itemHeight);
        }

        @Override
        protected void renderEntry(DrawContext context, int mouseX, int mouseY, float delta, int index, int x, int y, int entryWidth, int entryHeight) {
            Entry lv = (Entry)this.getEntry(index);
            if (this.shouldHighlight(lv)) {
                boolean bl = this.getSelectedOrNull() == lv;
                int p = this.isFocused() && bl ? -1 : -8355712;
                this.drawSelectionHighlight(context, y, entryWidth, entryHeight, p, -16777216);
            }
            lv.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, this.getHoveredEntry() == lv, delta);
        }

        private boolean shouldHighlight(Entry entry) {
            if (entry.canSelect()) {
                boolean bl = this.getSelectedOrNull() == entry;
                boolean bl2 = this.getSelectedOrNull() == null;
                boolean bl3 = this.getHoveredEntry() == entry;
                return bl || bl2 && bl3 && entry.isHighlightedOnHover();
            }
            return false;
        }

        @Override
        @Nullable
        protected Entry getNeighboringEntry(NavigationDirection arg) {
            return this.getNeighboringEntry(arg, Entry::canSelect);
        }

        @Override
        public void setSelected(@Nullable Entry arg) {
            super.setSelected(arg);
            Entry lv = this.getNeighboringEntry(NavigationDirection.UP);
            if (lv == null) {
                ChatSelectionScreen.this.addMoreMessages();
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            Entry lv = (Entry)this.getSelectedOrNull();
            if (lv != null && lv.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        public int getContextMessageY() {
            return this.getBottom() + ((ChatSelectionScreen)ChatSelectionScreen.this).textRenderer.fontHeight;
        }

        @Override
        @Nullable
        protected /* synthetic */ EntryListWidget.Entry getNeighboringEntry(NavigationDirection direction) {
            return this.getNeighboringEntry(direction);
        }

        @Environment(value=EnvType.CLIENT)
        public class MessageEntry
        extends Entry {
            private static final int CHECKMARK_WIDTH = 9;
            private static final int CHECKMARK_HEIGHT = 8;
            private static final int CHAT_MESSAGE_LEFT_MARGIN = 11;
            private static final int INDICATOR_LEFT_MARGIN = 4;
            private final int index;
            private final StringVisitable truncatedContent;
            private final Text narration;
            @Nullable
            private final List<OrderedText> fullContent;
            @Nullable
            private final MessageIndicator.Icon indicatorIcon;
            @Nullable
            private final List<OrderedText> originalContent;
            private final boolean fromReportedPlayer;
            private final boolean isChatMessage;

            public MessageEntry(int index, Text message, @Nullable Text narration, MessageIndicator indicator, boolean fromReportedPlayer, boolean isChatMessage) {
                super(SelectionListWidget.this);
                this.index = index;
                this.indicatorIcon = Nullables.map(indicator, MessageIndicator::icon);
                this.originalContent = indicator != null && indicator.text() != null ? ChatSelectionScreen.this.textRenderer.wrapLines(indicator.text(), SelectionListWidget.this.getRowWidth()) : null;
                this.fromReportedPlayer = fromReportedPlayer;
                this.isChatMessage = isChatMessage;
                StringVisitable lv = ChatSelectionScreen.this.textRenderer.trimToWidth(message, this.getTextWidth() - ChatSelectionScreen.this.textRenderer.getWidth(ScreenTexts.ELLIPSIS));
                if (message != lv) {
                    this.truncatedContent = StringVisitable.concat(lv, ScreenTexts.ELLIPSIS);
                    this.fullContent = ChatSelectionScreen.this.textRenderer.wrapLines(message, SelectionListWidget.this.getRowWidth());
                } else {
                    this.truncatedContent = message;
                    this.fullContent = null;
                }
                this.narration = narration;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                if (this.isSelected() && this.fromReportedPlayer) {
                    this.drawCheckmark(context, y, x, entryHeight);
                }
                int p = x + this.getIndent();
                int q = y + 1 + (entryHeight - ((ChatSelectionScreen)ChatSelectionScreen.this).textRenderer.fontHeight) / 2;
                context.drawTextWithShadow(ChatSelectionScreen.this.textRenderer, Language.getInstance().reorder(this.truncatedContent), p, q, this.fromReportedPlayer ? -1 : -1593835521);
                if (this.fullContent != null && hovered) {
                    ChatSelectionScreen.this.setTooltip(this.fullContent);
                }
                int r = ChatSelectionScreen.this.textRenderer.getWidth(this.truncatedContent);
                this.renderIndicator(context, p + r + 4, y, entryHeight, mouseX, mouseY);
            }

            private void renderIndicator(DrawContext context, int x, int y, int entryHeight, int mouseX, int mouseY) {
                if (this.indicatorIcon != null) {
                    int n = y + (entryHeight - this.indicatorIcon.height) / 2;
                    this.indicatorIcon.draw(context, x, n);
                    if (this.originalContent != null && mouseX >= x && mouseX <= x + this.indicatorIcon.width && mouseY >= n && mouseY <= n + this.indicatorIcon.height) {
                        ChatSelectionScreen.this.setTooltip(this.originalContent);
                    }
                }
            }

            private void drawCheckmark(DrawContext context, int y, int x, int entryHeight) {
                int l = x;
                int m = y + (entryHeight - 8) / 2;
                RenderSystem.enableBlend();
                context.drawGuiTexture(CHECKMARK_ICON_TEXTURE, l, m, 9, 8);
                RenderSystem.disableBlend();
            }

            private int getTextWidth() {
                int i = this.indicatorIcon != null ? this.indicatorIcon.width + 4 : 0;
                return SelectionListWidget.this.getRowWidth() - this.getIndent() - 4 - i;
            }

            private int getIndent() {
                return this.isChatMessage ? 11 : 0;
            }

            @Override
            public Text getNarration() {
                return this.isSelected() ? Text.translatable("narrator.select", this.narration) : this.narration;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                SelectionListWidget.this.setSelected((Entry)null);
                return this.toggle();
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (KeyCodes.isToggle(keyCode)) {
                    return this.toggle();
                }
                return false;
            }

            @Override
            public boolean isSelected() {
                return ChatSelectionScreen.this.report.isMessageSelected(this.index);
            }

            @Override
            public boolean canSelect() {
                return true;
            }

            @Override
            public boolean isHighlightedOnHover() {
                return this.fromReportedPlayer;
            }

            private boolean toggle() {
                if (this.fromReportedPlayer) {
                    ChatSelectionScreen.this.report.toggleMessageSelection(this.index);
                    ChatSelectionScreen.this.setDoneButtonActivation();
                    return true;
                }
                return false;
            }
        }

        @Environment(value=EnvType.CLIENT)
        public class SenderEntry
        extends Entry {
            private static final int PLAYER_SKIN_SIZE = 12;
            private static final int field_49545 = 4;
            private final Text headingText;
            private final Supplier<SkinTextures> skinTexturesSupplier;
            private final boolean fromReportedPlayer;

            public SenderEntry(GameProfile gameProfile, Text headingText, boolean fromReportedPlayer) {
                super(SelectionListWidget.this);
                this.headingText = headingText;
                this.fromReportedPlayer = fromReportedPlayer;
                this.skinTexturesSupplier = SelectionListWidget.this.client.getSkinProvider().getSkinTexturesSupplier(gameProfile);
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int p = x - 12 + 4;
                int q = y + (entryHeight - 12) / 2;
                PlayerSkinDrawer.draw(context, this.skinTexturesSupplier.get(), p, q, 12);
                int r = y + 1 + (entryHeight - ((ChatSelectionScreen)ChatSelectionScreen.this).textRenderer.fontHeight) / 2;
                context.drawTextWithShadow(ChatSelectionScreen.this.textRenderer, this.headingText, p + 12 + 4, r, this.fromReportedPlayer ? Colors.WHITE : -1593835521);
            }
        }

        @Environment(value=EnvType.CLIENT)
        record SenderEntryPair(UUID sender, Entry entry) {
            public boolean senderEquals(SenderEntryPair pair) {
                return pair.sender.equals(this.sender);
            }
        }

        @Environment(value=EnvType.CLIENT)
        public abstract class Entry
        extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            public Entry(SelectionListWidget arg) {
            }

            @Override
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

        @Environment(value=EnvType.CLIENT)
        public class SeparatorEntry
        extends Entry {
            public SeparatorEntry(SelectionListWidget arg) {
                super(arg);
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            }
        }

        @Environment(value=EnvType.CLIENT)
        public class TextEntry
        extends Entry {
            private static final int TEXT_COLOR = -6250336;
            private final Text text;

            public TextEntry(Text text) {
                super(SelectionListWidget.this);
                this.text = text;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int p = y + entryHeight / 2;
                int q = x + entryWidth - 8;
                int r = ChatSelectionScreen.this.textRenderer.getWidth(this.text);
                int s = (x + q - r) / 2;
                int t = p - ((ChatSelectionScreen)ChatSelectionScreen.this).textRenderer.fontHeight / 2;
                context.drawTextWithShadow(ChatSelectionScreen.this.textRenderer, this.text, s, t, Colors.LIGHT_GRAY);
            }

            @Override
            public Text getNarration() {
                return this.text;
            }
        }
    }
}

