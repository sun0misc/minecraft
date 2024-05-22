/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.hud;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Nullables;
import net.minecraft.util.collection.ArrayListDeque;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ChatHud {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_MESSAGES = 100;
    private static final int MISSING_MESSAGE_INDEX = -1;
    private static final int field_39772 = 4;
    private static final int field_39773 = 4;
    private static final int OFFSET_FROM_BOTTOM = 40;
    private static final int REMOVAL_QUEUE_TICKS = 60;
    private static final Text DELETED_MARKER_TEXT = Text.translatable("chat.deleted_marker").formatted(Formatting.GRAY, Formatting.ITALIC);
    private final MinecraftClient client;
    private final ArrayListDeque<String> messageHistory = new ArrayListDeque(100);
    private final List<ChatHudLine> messages = Lists.newArrayList();
    private final List<ChatHudLine.Visible> visibleMessages = Lists.newArrayList();
    private int scrolledLines;
    private boolean hasUnreadNewMessages;
    private final List<RemovalQueuedMessage> removalQueue = new ArrayList<RemovalQueuedMessage>();

    public ChatHud(MinecraftClient client) {
        this.client = client;
        this.messageHistory.addAll(client.getCommandHistoryManager().getHistory());
    }

    public void tickRemovalQueueIfExists() {
        if (!this.removalQueue.isEmpty()) {
            this.tickRemovalQueue();
        }
    }

    public void render(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused) {
        int aa;
        int z;
        int y;
        int x;
        int w;
        if (this.isChatHidden()) {
            return;
        }
        int l = this.getVisibleLineCount();
        int m = this.visibleMessages.size();
        if (m <= 0) {
            return;
        }
        this.client.getProfiler().push("chat");
        float f = (float)this.getChatScale();
        int n = MathHelper.ceil((float)this.getWidth() / f);
        int o = context.getScaledWindowHeight();
        context.getMatrices().push();
        context.getMatrices().scale(f, f, 1.0f);
        context.getMatrices().translate(4.0f, 0.0f, 0.0f);
        int p = MathHelper.floor((float)(o - 40) / f);
        int q = this.getMessageIndex(this.toChatLineX(mouseX), this.toChatLineY(mouseY));
        double d = this.client.options.getChatOpacity().getValue() * (double)0.9f + (double)0.1f;
        double e = this.client.options.getTextBackgroundOpacity().getValue();
        double g = this.client.options.getChatLineSpacing().getValue();
        int r = this.getLineHeight();
        int s = (int)Math.round(-8.0 * (g + 1.0) + 4.0 * g);
        int t = 0;
        for (int u = 0; u + this.scrolledLines < this.visibleMessages.size() && u < l; ++u) {
            int v = u + this.scrolledLines;
            ChatHudLine.Visible lv = this.visibleMessages.get(v);
            if (lv == null || (w = currentTick - lv.addedTime()) >= 200 && !focused) continue;
            double h = focused ? 1.0 : ChatHud.getMessageOpacityMultiplier(w);
            x = (int)(255.0 * h * d);
            y = (int)(255.0 * h * e);
            ++t;
            if (x <= 3) continue;
            z = 0;
            aa = p - u * r;
            int ab = aa + s;
            context.fill(-4, aa - r, 0 + n + 4 + 4, aa, y << 24);
            MessageIndicator lv2 = lv.indicator();
            if (lv2 != null) {
                int ac = lv2.indicatorColor() | x << 24;
                context.fill(-4, aa - r, -2, aa, ac);
                if (v == q && lv2.icon() != null) {
                    int ad = this.getIndicatorX(lv);
                    int ae = ab + this.client.textRenderer.fontHeight;
                    this.drawIndicatorIcon(context, ad, ae, lv2.icon());
                }
            }
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, 0.0f, 50.0f);
            context.drawTextWithShadow(this.client.textRenderer, lv.content(), 0, ab, 0xFFFFFF + (x << 24));
            context.getMatrices().pop();
        }
        long af = this.client.getMessageHandler().getUnprocessedMessageCount();
        if (af > 0L) {
            int ag = (int)(128.0 * d);
            w = (int)(255.0 * e);
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, p, 0.0f);
            context.fill(-2, 0, n + 4, 9, w << 24);
            context.getMatrices().translate(0.0f, 0.0f, 50.0f);
            context.drawTextWithShadow(this.client.textRenderer, Text.translatable("chat.queue", af), 0, 1, 0xFFFFFF + (ag << 24));
            context.getMatrices().pop();
        }
        if (focused) {
            int ag = this.getLineHeight();
            w = m * ag;
            int ah = t * ag;
            int ai = this.scrolledLines * ah / m - p;
            x = ah * ah / w;
            if (w != ah) {
                y = ai > 0 ? 170 : 96;
                z = this.hasUnreadNewMessages ? 0xCC3333 : 0x3333AA;
                aa = n + 4;
                context.fill(aa, -ai, aa + 2, -ai - x, 100, z + (y << 24));
                context.fill(aa + 2, -ai, aa + 1, -ai - x, 100, 0xCCCCCC + (y << 24));
            }
        }
        context.getMatrices().pop();
        this.client.getProfiler().pop();
    }

    private void drawIndicatorIcon(DrawContext context, int x, int y, MessageIndicator.Icon icon) {
        int k = y - icon.height - 1;
        icon.draw(context, x, k);
    }

    private int getIndicatorX(ChatHudLine.Visible line) {
        return this.client.textRenderer.getWidth(line.content()) + 4;
    }

    private boolean isChatHidden() {
        return this.client.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN;
    }

    private static double getMessageOpacityMultiplier(int age) {
        double d = (double)age / 200.0;
        d = 1.0 - d;
        d *= 10.0;
        d = MathHelper.clamp(d, 0.0, 1.0);
        d *= d;
        return d;
    }

    public void clear(boolean clearHistory) {
        this.client.getMessageHandler().processAll();
        this.removalQueue.clear();
        this.visibleMessages.clear();
        this.messages.clear();
        if (clearHistory) {
            this.messageHistory.clear();
            this.messageHistory.addAll(this.client.getCommandHistoryManager().getHistory());
        }
    }

    public void addMessage(Text message) {
        this.addMessage(message, null, this.client.isConnectedToLocalServer() ? MessageIndicator.singlePlayer() : MessageIndicator.system());
    }

    public void addMessage(Text message, @Nullable MessageSignatureData signatureData, @Nullable MessageIndicator indicator) {
        ChatHudLine lv = new ChatHudLine(this.client.inGameHud.getTicks(), message, signatureData, indicator);
        this.logChatMessage(lv);
        this.addVisibleMessage(lv);
        this.addMessage(lv);
    }

    private void logChatMessage(ChatHudLine message) {
        String string = message.content().getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String string2 = Nullables.map(message.indicator(), MessageIndicator::loggedName);
        if (string2 != null) {
            LOGGER.info("[{}] [CHAT] {}", (Object)string2, (Object)string);
        } else {
            LOGGER.info("[CHAT] {}", (Object)string);
        }
    }

    private void addVisibleMessage(ChatHudLine message) {
        int i = MathHelper.floor((double)this.getWidth() / this.getChatScale());
        MessageIndicator.Icon lv = message.getIcon();
        if (lv != null) {
            i -= lv.width + 4 + 2;
        }
        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(message.content(), i, this.client.textRenderer);
        boolean bl = this.isChatFocused();
        for (int j = 0; j < list.size(); ++j) {
            OrderedText lv2 = list.get(j);
            if (bl && this.scrolledLines > 0) {
                this.hasUnreadNewMessages = true;
                this.scroll(1);
            }
            boolean bl2 = j == list.size() - 1;
            this.visibleMessages.add(0, new ChatHudLine.Visible(message.creationTick(), lv2, message.indicator(), bl2));
        }
        while (this.visibleMessages.size() > 100) {
            this.visibleMessages.remove(this.visibleMessages.size() - 1);
        }
    }

    private void addMessage(ChatHudLine message) {
        this.messages.add(0, message);
        while (this.messages.size() > 100) {
            this.messages.remove(this.messages.size() - 1);
        }
    }

    private void tickRemovalQueue() {
        int i = this.client.inGameHud.getTicks();
        this.removalQueue.removeIf(message -> {
            if (i >= message.deletableAfter()) {
                return this.queueForRemoval(message.signature()) == null;
            }
            return false;
        });
    }

    public void removeMessage(MessageSignatureData signature) {
        RemovalQueuedMessage lv = this.queueForRemoval(signature);
        if (lv != null) {
            this.removalQueue.add(lv);
        }
    }

    @Nullable
    private RemovalQueuedMessage queueForRemoval(MessageSignatureData signature) {
        int i = this.client.inGameHud.getTicks();
        ListIterator<ChatHudLine> listIterator = this.messages.listIterator();
        while (listIterator.hasNext()) {
            ChatHudLine lv = listIterator.next();
            if (!signature.equals(lv.signature())) continue;
            int j = lv.creationTick() + 60;
            if (i >= j) {
                listIterator.set(this.createRemovalMarker(lv));
                this.refresh();
                return null;
            }
            return new RemovalQueuedMessage(signature, j);
        }
        return null;
    }

    private ChatHudLine createRemovalMarker(ChatHudLine original) {
        return new ChatHudLine(original.creationTick(), DELETED_MARKER_TEXT, null, MessageIndicator.system());
    }

    public void reset() {
        this.resetScroll();
        this.refresh();
    }

    private void refresh() {
        this.visibleMessages.clear();
        for (ChatHudLine lv : Lists.reverse(this.messages)) {
            this.addVisibleMessage(lv);
        }
    }

    public ArrayListDeque<String> getMessageHistory() {
        return this.messageHistory;
    }

    public void addToMessageHistory(String message) {
        if (!message.equals(this.messageHistory.peekLast())) {
            if (this.messageHistory.size() >= 100) {
                this.messageHistory.removeFirst();
            }
            this.messageHistory.addLast(message);
        }
        if (message.startsWith("/")) {
            this.client.getCommandHistoryManager().add(message);
        }
    }

    public void resetScroll() {
        this.scrolledLines = 0;
        this.hasUnreadNewMessages = false;
    }

    public void scroll(int scroll) {
        this.scrolledLines += scroll;
        int j = this.visibleMessages.size();
        if (this.scrolledLines > j - this.getVisibleLineCount()) {
            this.scrolledLines = j - this.getVisibleLineCount();
        }
        if (this.scrolledLines <= 0) {
            this.scrolledLines = 0;
            this.hasUnreadNewMessages = false;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (!this.isChatFocused() || this.client.options.hudHidden || this.isChatHidden()) {
            return false;
        }
        MessageHandler lv = this.client.getMessageHandler();
        if (lv.getUnprocessedMessageCount() == 0L) {
            return false;
        }
        double f = mouseX - 2.0;
        double g = (double)this.client.getWindow().getScaledHeight() - mouseY - 40.0;
        if (f <= (double)MathHelper.floor((double)this.getWidth() / this.getChatScale()) && g < 0.0 && g > (double)MathHelper.floor(-9.0 * this.getChatScale())) {
            lv.process();
            return true;
        }
        return false;
    }

    @Nullable
    public Style getTextStyleAt(double x, double y) {
        double g;
        double f = this.toChatLineX(x);
        int i = this.getMessageLineIndex(f, g = this.toChatLineY(y));
        if (i >= 0 && i < this.visibleMessages.size()) {
            ChatHudLine.Visible lv = this.visibleMessages.get(i);
            return this.client.textRenderer.getTextHandler().getStyleAt(lv.content(), MathHelper.floor(f));
        }
        return null;
    }

    @Nullable
    public MessageIndicator getIndicatorAt(double mouseX, double mouseY) {
        ChatHudLine.Visible lv;
        MessageIndicator lv2;
        double g;
        double f = this.toChatLineX(mouseX);
        int i = this.getMessageIndex(f, g = this.toChatLineY(mouseY));
        if (i >= 0 && i < this.visibleMessages.size() && (lv2 = (lv = this.visibleMessages.get(i)).indicator()) != null && this.isXInsideIndicatorIcon(f, lv, lv2)) {
            return lv2;
        }
        return null;
    }

    private boolean isXInsideIndicatorIcon(double x, ChatHudLine.Visible line, MessageIndicator indicator) {
        if (x < 0.0) {
            return true;
        }
        MessageIndicator.Icon lv = indicator.icon();
        if (lv != null) {
            int i = this.getIndicatorX(line);
            int j = i + lv.width;
            return x >= (double)i && x <= (double)j;
        }
        return false;
    }

    private double toChatLineX(double x) {
        return x / this.getChatScale() - 4.0;
    }

    private double toChatLineY(double y) {
        double e = (double)this.client.getWindow().getScaledHeight() - y - 40.0;
        return e / (this.getChatScale() * (double)this.getLineHeight());
    }

    private int getMessageIndex(double chatLineX, double chatLineY) {
        int i = this.getMessageLineIndex(chatLineX, chatLineY);
        if (i == -1) {
            return -1;
        }
        while (i >= 0) {
            if (this.visibleMessages.get(i).endOfEntry()) {
                return i;
            }
            --i;
        }
        return i;
    }

    private int getMessageLineIndex(double chatLineX, double chatLineY) {
        int j;
        if (!this.isChatFocused() || this.isChatHidden()) {
            return -1;
        }
        if (chatLineX < -4.0 || chatLineX > (double)MathHelper.floor((double)this.getWidth() / this.getChatScale())) {
            return -1;
        }
        int i = Math.min(this.getVisibleLineCount(), this.visibleMessages.size());
        if (chatLineY >= 0.0 && chatLineY < (double)i && (j = MathHelper.floor(chatLineY + (double)this.scrolledLines)) >= 0 && j < this.visibleMessages.size()) {
            return j;
        }
        return -1;
    }

    public boolean isChatFocused() {
        return this.client.currentScreen instanceof ChatScreen;
    }

    public int getWidth() {
        return ChatHud.getWidth(this.client.options.getChatWidth().getValue());
    }

    public int getHeight() {
        return ChatHud.getHeight(this.isChatFocused() ? this.client.options.getChatHeightFocused().getValue() : this.client.options.getChatHeightUnfocused().getValue());
    }

    public double getChatScale() {
        return this.client.options.getChatScale().getValue();
    }

    public static int getWidth(double widthOption) {
        int i = 320;
        int j = 40;
        return MathHelper.floor(widthOption * 280.0 + 40.0);
    }

    public static int getHeight(double heightOption) {
        int i = 180;
        int j = 20;
        return MathHelper.floor(heightOption * 160.0 + 20.0);
    }

    public static double getDefaultUnfocusedHeight() {
        int i = 180;
        int j = 20;
        return 70.0 / (double)(ChatHud.getHeight(1.0) - 20);
    }

    public int getVisibleLineCount() {
        return this.getHeight() / this.getLineHeight();
    }

    private int getLineHeight() {
        return (int)((double)this.client.textRenderer.fontHeight * (this.client.options.getChatLineSpacing().getValue() + 1.0));
    }

    public ChatState toChatState() {
        return new ChatState(List.copyOf(this.messages), List.copyOf(this.messageHistory), List.copyOf(this.removalQueue));
    }

    public void restoreChatState(ChatState state) {
        this.messageHistory.clear();
        this.messageHistory.addAll(state.messageHistory);
        this.removalQueue.clear();
        this.removalQueue.addAll(state.removalQueue);
        this.messages.clear();
        this.messages.addAll(state.messages);
        this.refresh();
    }

    @Environment(value=EnvType.CLIENT)
    record RemovalQueuedMessage(MessageSignatureData signature, int deletableAfter) {
    }

    @Environment(value=EnvType.CLIENT)
    public static class ChatState {
        final List<ChatHudLine> messages;
        final List<String> messageHistory;
        final List<RemovalQueuedMessage> removalQueue;

        public ChatState(List<ChatHudLine> messages, List<String> messageHistory, List<RemovalQueuedMessage> removalQueue) {
            this.messages = messages;
            this.messageHistory = messageHistory;
            this.removalQueue = removalQueue;
        }
    }
}

