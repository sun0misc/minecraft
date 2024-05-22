/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class ChatScreen
extends Screen {
    public static final double SHIFT_SCROLL_AMOUNT = 7.0;
    private static final Text USAGE_TEXT = Text.translatable("chat_screen.usage");
    private static final int MAX_INDICATOR_TOOLTIP_WIDTH = 210;
    private String chatLastMessage = "";
    private int messageHistoryIndex = -1;
    protected TextFieldWidget chatField;
    private String originalChatText;
    ChatInputSuggestor chatInputSuggestor;

    public ChatScreen(String originalChatText) {
        super(Text.translatable("chat_screen.title"));
        this.originalChatText = originalChatText;
    }

    @Override
    protected void init() {
        this.messageHistoryIndex = this.client.inGameHud.getChatHud().getMessageHistory().size();
        this.chatField = new TextFieldWidget(this.client.advanceValidatingTextRenderer, 4, this.height - 12, this.width - 4, 12, (Text)Text.translatable("chat.editBox")){

            @Override
            protected MutableText getNarrationMessage() {
                return super.getNarrationMessage().append(ChatScreen.this.chatInputSuggestor.getNarration());
            }
        };
        this.chatField.setMaxLength(256);
        this.chatField.setDrawsBackground(false);
        this.chatField.setText(this.originalChatText);
        this.chatField.setChangedListener(this::onChatFieldUpdate);
        this.chatField.setFocusUnlocked(false);
        this.addSelectableChild(this.chatField);
        this.chatInputSuggestor = new ChatInputSuggestor(this.client, this, this.chatField, this.textRenderer, false, false, 1, 10, true, -805306368);
        this.chatInputSuggestor.setCanLeave(false);
        this.chatInputSuggestor.refresh();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.chatField);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.chatField.getText();
        this.init(client, width, height);
        this.setText(string);
        this.chatInputSuggestor.refresh();
    }

    @Override
    public void removed() {
        this.client.inGameHud.getChatHud().resetScroll();
    }

    private void onChatFieldUpdate(String chatText) {
        String string2 = this.chatField.getText();
        this.chatInputSuggestor.setWindowActive(!string2.equals(this.originalChatText));
        this.chatInputSuggestor.refresh();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.chatInputSuggestor.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.setScreen(null);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.sendMessage(this.chatField.getText(), true);
            this.client.setScreen(null);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP) {
            this.setChatFromHistory(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            this.setChatFromHistory(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            this.client.inGameHud.getChatHud().scroll(this.client.inGameHud.getChatHud().getVisibleLineCount() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.client.inGameHud.getChatHud().scroll(-this.client.inGameHud.getChatHud().getVisibleLineCount() + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.chatInputSuggestor.mouseScrolled(verticalAmount = MathHelper.clamp(verticalAmount, -1.0, 1.0))) {
            return true;
        }
        if (!ChatScreen.hasShiftDown()) {
            verticalAmount *= 7.0;
        }
        this.client.inGameHud.getChatHud().scroll((int)verticalAmount);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.chatInputSuggestor.mouseClicked((int)mouseX, (int)mouseY, button)) {
            return true;
        }
        if (button == 0) {
            ChatHud lv = this.client.inGameHud.getChatHud();
            if (lv.mouseClicked(mouseX, mouseY)) {
                return true;
            }
            Style lv2 = this.getTextStyleAt(mouseX, mouseY);
            if (lv2 != null && this.handleTextClick(lv2)) {
                this.originalChatText = this.chatField.getText();
                return true;
            }
        }
        if (this.chatField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void insertText(String text, boolean override) {
        if (override) {
            this.chatField.setText(text);
        } else {
            this.chatField.write(text);
        }
    }

    public void setChatFromHistory(int offset) {
        int j = this.messageHistoryIndex + offset;
        int k = this.client.inGameHud.getChatHud().getMessageHistory().size();
        if ((j = MathHelper.clamp(j, 0, k)) == this.messageHistoryIndex) {
            return;
        }
        if (j == k) {
            this.messageHistoryIndex = k;
            this.chatField.setText(this.chatLastMessage);
            return;
        }
        if (this.messageHistoryIndex == k) {
            this.chatLastMessage = this.chatField.getText();
        }
        this.chatField.setText(this.client.inGameHud.getChatHud().getMessageHistory().get(j));
        this.chatInputSuggestor.setWindowActive(false);
        this.messageHistoryIndex = j;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.client.inGameHud.getChatHud().render(context, this.client.inGameHud.getTicks(), mouseX, mouseY, true);
        context.fill(2, this.height - 14, this.width - 2, this.height - 2, this.client.options.getTextBackgroundColor(Integer.MIN_VALUE));
        this.chatField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 200.0f);
        this.chatInputSuggestor.render(context, mouseX, mouseY);
        context.getMatrices().pop();
        MessageIndicator lv = this.client.inGameHud.getChatHud().getIndicatorAt(mouseX, mouseY);
        if (lv != null && lv.text() != null) {
            context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(lv.text(), 210), mouseX, mouseY);
        } else {
            Style lv2 = this.getTextStyleAt(mouseX, mouseY);
            if (lv2 != null && lv2.getHoverEvent() != null) {
                context.drawHoverEvent(this.textRenderer, lv2, mouseX, mouseY);
            }
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void setText(String text) {
        this.chatField.setText(text);
    }

    @Override
    protected void addScreenNarrations(NarrationMessageBuilder messageBuilder) {
        messageBuilder.put(NarrationPart.TITLE, this.getTitle());
        messageBuilder.put(NarrationPart.USAGE, USAGE_TEXT);
        String string = this.chatField.getText();
        if (!string.isEmpty()) {
            messageBuilder.nextMessage().put(NarrationPart.TITLE, (Text)Text.translatable("chat_screen.message", string));
        }
    }

    @Nullable
    private Style getTextStyleAt(double x, double y) {
        return this.client.inGameHud.getChatHud().getTextStyleAt(x, y);
    }

    public void sendMessage(String chatText, boolean addToHistory) {
        if ((chatText = this.normalize(chatText)).isEmpty()) {
            return;
        }
        if (addToHistory) {
            this.client.inGameHud.getChatHud().addToMessageHistory(chatText);
        }
        if (chatText.startsWith("/")) {
            this.client.player.networkHandler.sendChatCommand(chatText.substring(1));
        } else {
            this.client.player.networkHandler.sendChatMessage(chatText);
        }
    }

    public String normalize(String chatText) {
        return StringHelper.truncateChat(StringUtils.normalizeSpace(chatText.trim()));
    }
}

