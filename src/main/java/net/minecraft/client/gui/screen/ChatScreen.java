package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ChatScreen extends Screen {
   public static final double SHIFT_SCROLL_AMOUNT = 7.0;
   private static final Text USAGE_TEXT = Text.translatable("chat_screen.usage");
   private static final int MAX_INDICATOR_TOOLTIP_WIDTH = 210;
   private String chatLastMessage = "";
   private int messageHistorySize = -1;
   protected TextFieldWidget chatField;
   private String originalChatText;
   ChatInputSuggestor chatInputSuggestor;

   public ChatScreen(String originalChatText) {
      super(Text.translatable("chat_screen.title"));
      this.originalChatText = originalChatText;
   }

   protected void init() {
      this.messageHistorySize = this.client.inGameHud.getChatHud().getMessageHistory().size();
      this.chatField = new TextFieldWidget(this.client.advanceValidatingTextRenderer, 4, this.height - 12, this.width - 4, 12, Text.translatable("chat.editBox")) {
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
      this.chatInputSuggestor.refresh();
      this.setInitialFocus(this.chatField);
   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.chatField.getText();
      this.init(client, width, height);
      this.setText(string);
      this.chatInputSuggestor.refresh();
   }

   public void removed() {
      this.client.inGameHud.getChatHud().resetScroll();
   }

   public void tick() {
      this.chatField.tick();
   }

   private void onChatFieldUpdate(String chatText) {
      String string2 = this.chatField.getText();
      this.chatInputSuggestor.setWindowActive(!string2.equals(this.originalChatText));
      this.chatInputSuggestor.refresh();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.chatInputSuggestor.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen((Screen)null);
         return true;
      } else if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
         if (keyCode == GLFW.GLFW_KEY_UP) {
            this.setChatFromHistory(-1);
            return true;
         } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            this.setChatFromHistory(1);
            return true;
         } else if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            this.client.inGameHud.getChatHud().scroll(this.client.inGameHud.getChatHud().getVisibleLineCount() - 1);
            return true;
         } else if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.client.inGameHud.getChatHud().scroll(-this.client.inGameHud.getChatHud().getVisibleLineCount() + 1);
            return true;
         } else {
            return false;
         }
      } else {
         if (this.sendMessage(this.chatField.getText(), true)) {
            this.client.setScreen((Screen)null);
         }

         return true;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      amount = MathHelper.clamp(amount, -1.0, 1.0);
      if (this.chatInputSuggestor.mouseScrolled(amount)) {
         return true;
      } else {
         if (!hasShiftDown()) {
            amount *= 7.0;
         }

         this.client.inGameHud.getChatHud().scroll((int)amount);
         return true;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.chatInputSuggestor.mouseClicked((double)((int)mouseX), (double)((int)mouseY), button)) {
         return true;
      } else {
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

         return this.chatField.mouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(mouseX, mouseY, button);
      }
   }

   protected void insertText(String text, boolean override) {
      if (override) {
         this.chatField.setText(text);
      } else {
         this.chatField.write(text);
      }

   }

   public void setChatFromHistory(int offset) {
      int j = this.messageHistorySize + offset;
      int k = this.client.inGameHud.getChatHud().getMessageHistory().size();
      j = MathHelper.clamp(j, 0, k);
      if (j != this.messageHistorySize) {
         if (j == k) {
            this.messageHistorySize = k;
            this.chatField.setText(this.chatLastMessage);
         } else {
            if (this.messageHistorySize == k) {
               this.chatLastMessage = this.chatField.getText();
            }

            this.chatField.setText((String)this.client.inGameHud.getChatHud().getMessageHistory().get(j));
            this.chatInputSuggestor.setWindowActive(false);
            this.messageHistorySize = j;
         }
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      fill(matrices, 2, this.height - 14, this.width - 2, this.height - 2, this.client.options.getTextBackgroundColor(Integer.MIN_VALUE));
      this.chatField.render(matrices, mouseX, mouseY, delta);
      super.render(matrices, mouseX, mouseY, delta);
      this.chatInputSuggestor.render(matrices, mouseX, mouseY);
      MessageIndicator lv = this.client.inGameHud.getChatHud().getIndicatorAt((double)mouseX, (double)mouseY);
      if (lv != null && lv.text() != null) {
         this.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(lv.text(), 210), mouseX, mouseY);
      } else {
         Style lv2 = this.getTextStyleAt((double)mouseX, (double)mouseY);
         if (lv2 != null && lv2.getHoverEvent() != null) {
            this.renderTextHoverEffect(matrices, lv2, mouseX, mouseY);
         }
      }

   }

   public boolean shouldPause() {
      return false;
   }

   private void setText(String text) {
      this.chatField.setText(text);
   }

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

   public boolean sendMessage(String chatText, boolean addToHistory) {
      chatText = this.normalize(chatText);
      if (chatText.isEmpty()) {
         return true;
      } else {
         if (addToHistory) {
            this.client.inGameHud.getChatHud().addToMessageHistory(chatText);
         }

         if (chatText.startsWith("/")) {
            this.client.player.networkHandler.sendChatCommand(chatText.substring(1));
         } else {
            this.client.player.networkHandler.sendChatMessage(chatText);
         }

         return true;
      }
   }

   public String normalize(String chatText) {
      return StringHelper.truncateChat(StringUtils.normalizeSpace(chatText.trim()));
   }
}
