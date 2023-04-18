package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TextFieldWidget extends ClickableWidget implements Drawable {
   public static final int field_32194 = -1;
   public static final int field_32195 = 1;
   private static final int field_32197 = 1;
   private static final int VERTICAL_CURSOR_COLOR = -3092272;
   private static final String HORIZONTAL_CURSOR = "_";
   public static final int DEFAULT_EDITABLE_COLOR = 14737632;
   private static final int field_32201 = -1;
   private static final int BORDER_COLOR = -6250336;
   private static final int BACKGROUND_COLOR = -16777216;
   private final TextRenderer textRenderer;
   private String text;
   private int maxLength;
   private int focusedTicks;
   private boolean drawsBackground;
   private boolean focusUnlocked;
   private boolean editable;
   private boolean selecting;
   private int firstCharacterIndex;
   private int selectionStart;
   private int selectionEnd;
   private int editableColor;
   private int uneditableColor;
   @Nullable
   private String suggestion;
   @Nullable
   private Consumer changedListener;
   private Predicate textPredicate;
   private BiFunction renderTextProvider;
   @Nullable
   private Text placeholder;

   public TextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
      this(textRenderer, x, y, width, height, (TextFieldWidget)null, text);
   }

   public TextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
      super(x, y, width, height, text);
      this.text = "";
      this.maxLength = 32;
      this.drawsBackground = true;
      this.focusUnlocked = true;
      this.editable = true;
      this.editableColor = 14737632;
      this.uneditableColor = 7368816;
      this.textPredicate = Objects::nonNull;
      this.renderTextProvider = (string, firstCharacterIndex) -> {
         return OrderedText.styledForwardsVisitedString(string, Style.EMPTY);
      };
      this.textRenderer = textRenderer;
      if (copyFrom != null) {
         this.setText(copyFrom.getText());
      }

   }

   public void setChangedListener(Consumer changedListener) {
      this.changedListener = changedListener;
   }

   public void setRenderTextProvider(BiFunction renderTextProvider) {
      this.renderTextProvider = renderTextProvider;
   }

   public void tick() {
      ++this.focusedTicks;
   }

   protected MutableText getNarrationMessage() {
      Text lv = this.getMessage();
      return Text.translatable("gui.narrate.editBox", lv, this.text);
   }

   public void setText(String text) {
      if (this.textPredicate.test(text)) {
         if (text.length() > this.maxLength) {
            this.text = text.substring(0, this.maxLength);
         } else {
            this.text = text;
         }

         this.setCursorToEnd();
         this.setSelectionEnd(this.selectionStart);
         this.onChanged(text);
      }
   }

   public String getText() {
      return this.text;
   }

   public String getSelectedText() {
      int i = Math.min(this.selectionStart, this.selectionEnd);
      int j = Math.max(this.selectionStart, this.selectionEnd);
      return this.text.substring(i, j);
   }

   public void setTextPredicate(Predicate textPredicate) {
      this.textPredicate = textPredicate;
   }

   public void write(String text) {
      int i = Math.min(this.selectionStart, this.selectionEnd);
      int j = Math.max(this.selectionStart, this.selectionEnd);
      int k = this.maxLength - this.text.length() - (i - j);
      String string2 = SharedConstants.stripInvalidChars(text);
      int l = string2.length();
      if (k < l) {
         string2 = string2.substring(0, k);
         l = k;
      }

      String string3 = (new StringBuilder(this.text)).replace(i, j, string2).toString();
      if (this.textPredicate.test(string3)) {
         this.text = string3;
         this.setSelectionStart(i + l);
         this.setSelectionEnd(this.selectionStart);
         this.onChanged(this.text);
      }
   }

   private void onChanged(String newText) {
      if (this.changedListener != null) {
         this.changedListener.accept(newText);
      }

   }

   private void erase(int offset) {
      if (Screen.hasControlDown()) {
         this.eraseWords(offset);
      } else {
         this.eraseCharacters(offset);
      }

   }

   public void eraseWords(int wordOffset) {
      if (!this.text.isEmpty()) {
         if (this.selectionEnd != this.selectionStart) {
            this.write("");
         } else {
            this.eraseCharacters(this.getWordSkipPosition(wordOffset) - this.selectionStart);
         }
      }
   }

   public void eraseCharacters(int characterOffset) {
      if (!this.text.isEmpty()) {
         if (this.selectionEnd != this.selectionStart) {
            this.write("");
         } else {
            int j = this.getCursorPosWithOffset(characterOffset);
            int k = Math.min(j, this.selectionStart);
            int l = Math.max(j, this.selectionStart);
            if (k != l) {
               String string = (new StringBuilder(this.text)).delete(k, l).toString();
               if (this.textPredicate.test(string)) {
                  this.text = string;
                  this.setCursor(k);
               }
            }
         }
      }
   }

   public int getWordSkipPosition(int wordOffset) {
      return this.getWordSkipPosition(wordOffset, this.getCursor());
   }

   private int getWordSkipPosition(int wordOffset, int cursorPosition) {
      return this.getWordSkipPosition(wordOffset, cursorPosition, true);
   }

   private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
      int k = cursorPosition;
      boolean bl2 = wordOffset < 0;
      int l = Math.abs(wordOffset);

      for(int m = 0; m < l; ++m) {
         if (!bl2) {
            int n = this.text.length();
            k = this.text.indexOf(32, k);
            if (k == -1) {
               k = n;
            } else {
               while(skipOverSpaces && k < n && this.text.charAt(k) == ' ') {
                  ++k;
               }
            }
         } else {
            while(skipOverSpaces && k > 0 && this.text.charAt(k - 1) == ' ') {
               --k;
            }

            while(k > 0 && this.text.charAt(k - 1) != ' ') {
               --k;
            }
         }
      }

      return k;
   }

   public void moveCursor(int offset) {
      this.setCursor(this.getCursorPosWithOffset(offset));
   }

   private int getCursorPosWithOffset(int offset) {
      return Util.moveCursor(this.text, this.selectionStart, offset);
   }

   public void setCursor(int cursor) {
      this.setSelectionStart(cursor);
      if (!this.selecting) {
         this.setSelectionEnd(this.selectionStart);
      }

      this.onChanged(this.text);
   }

   public void setSelectionStart(int cursor) {
      this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
   }

   public void setCursorToStart() {
      this.setCursor(0);
   }

   public void setCursorToEnd() {
      this.setCursor(this.text.length());
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.isActive()) {
         return false;
      } else {
         this.selecting = Screen.hasShiftDown();
         if (Screen.isSelectAll(keyCode)) {
            this.setCursorToEnd();
            this.setSelectionEnd(0);
            return true;
         } else if (Screen.isCopy(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            return true;
         } else if (Screen.isPaste(keyCode)) {
            if (this.editable) {
               this.write(MinecraftClient.getInstance().keyboard.getClipboard());
            }

            return true;
         } else if (Screen.isCut(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            if (this.editable) {
               this.write("");
            }

            return true;
         } else {
            switch (keyCode) {
               case 259:
                  if (this.editable) {
                     this.selecting = false;
                     this.erase(-1);
                     this.selecting = Screen.hasShiftDown();
                  }

                  return true;
               case 260:
               case 264:
               case 265:
               case 266:
               case 267:
               default:
                  return false;
               case 261:
                  if (this.editable) {
                     this.selecting = false;
                     this.erase(1);
                     this.selecting = Screen.hasShiftDown();
                  }

                  return true;
               case 262:
                  if (Screen.hasControlDown()) {
                     this.setCursor(this.getWordSkipPosition(1));
                  } else {
                     this.moveCursor(1);
                  }

                  return true;
               case 263:
                  if (Screen.hasControlDown()) {
                     this.setCursor(this.getWordSkipPosition(-1));
                  } else {
                     this.moveCursor(-1);
                  }

                  return true;
               case 268:
                  this.setCursorToStart();
                  return true;
               case 269:
                  this.setCursorToEnd();
                  return true;
            }
         }
      }
   }

   public boolean isActive() {
      return this.isVisible() && this.isFocused() && this.isEditable();
   }

   public boolean charTyped(char chr, int modifiers) {
      if (!this.isActive()) {
         return false;
      } else if (SharedConstants.isValidChar(chr)) {
         if (this.editable) {
            this.write(Character.toString(chr));
         }

         return true;
      } else {
         return false;
      }
   }

   public void onClick(double mouseX, double mouseY) {
      int i = MathHelper.floor(mouseX) - this.getX();
      if (this.drawsBackground) {
         i -= 4;
      }

      String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
      this.setCursor(this.textRenderer.trimToWidth(string, i).length() + this.firstCharacterIndex);
   }

   public void playDownSound(SoundManager soundManager) {
   }

   public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         int k;
         if (this.drawsBackground()) {
            k = this.isFocused() ? -1 : -6250336;
            fill(matrices, this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, k);
            fill(matrices, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
         }

         k = this.editable ? this.editableColor : this.uneditableColor;
         int l = this.selectionStart - this.firstCharacterIndex;
         int m = this.selectionEnd - this.firstCharacterIndex;
         String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
         boolean bl = l >= 0 && l <= string.length();
         boolean bl2 = this.isFocused() && this.focusedTicks / 6 % 2 == 0 && bl;
         int n = this.drawsBackground ? this.getX() + 4 : this.getX();
         int o = this.drawsBackground ? this.getY() + (this.height - 8) / 2 : this.getY();
         int p = n;
         if (m > string.length()) {
            m = string.length();
         }

         if (!string.isEmpty()) {
            String string2 = bl ? string.substring(0, l) : string;
            p = this.textRenderer.drawWithShadow(matrices, (OrderedText)this.renderTextProvider.apply(string2, this.firstCharacterIndex), (float)n, (float)o, k);
         }

         boolean bl3 = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
         int q = p;
         if (!bl) {
            q = l > 0 ? n + this.width : n;
         } else if (bl3) {
            q = p - 1;
            --p;
         }

         if (!string.isEmpty() && bl && l < string.length()) {
            this.textRenderer.drawWithShadow(matrices, (OrderedText)this.renderTextProvider.apply(string.substring(l), this.selectionStart), (float)p, (float)o, k);
         }

         if (this.placeholder != null && string.isEmpty() && !this.isFocused()) {
            this.textRenderer.drawWithShadow(matrices, this.placeholder, (float)p, (float)o, k);
         }

         if (!bl3 && this.suggestion != null) {
            this.textRenderer.drawWithShadow(matrices, this.suggestion, (float)(q - 1), (float)o, -8355712);
         }

         int var10003;
         int var10004;
         if (bl2) {
            if (bl3) {
               int var10002 = o - 1;
               var10003 = q + 1;
               var10004 = o + 1;
               Objects.requireNonNull(this.textRenderer);
               DrawableHelper.fill(matrices, q, var10002, var10003, var10004 + 9, -3092272);
            } else {
               this.textRenderer.drawWithShadow(matrices, "_", (float)q, (float)o, k);
            }
         }

         if (m != l) {
            int r = n + this.textRenderer.getWidth(string.substring(0, m));
            var10003 = o - 1;
            var10004 = r - 1;
            int var10005 = o + 1;
            Objects.requireNonNull(this.textRenderer);
            this.drawSelectionHighlight(matrices, q, var10003, var10004, var10005 + 9);
         }

      }
   }

   private void drawSelectionHighlight(MatrixStack matrices, int x1, int y1, int x2, int y2) {
      int m;
      if (x1 < x2) {
         m = x1;
         x1 = x2;
         x2 = m;
      }

      if (y1 < y2) {
         m = y1;
         y1 = y2;
         y2 = m;
      }

      if (x2 > this.getX() + this.width) {
         x2 = this.getX() + this.width;
      }

      if (x1 > this.getX() + this.width) {
         x1 = this.getX() + this.width;
      }

      RenderSystem.enableColorLogicOp();
      RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
      fill(matrices, x1, y1, x2, y2, -16776961);
      RenderSystem.disableColorLogicOp();
   }

   public void setMaxLength(int maxLength) {
      this.maxLength = maxLength;
      if (this.text.length() > maxLength) {
         this.text = this.text.substring(0, maxLength);
         this.onChanged(this.text);
      }

   }

   private int getMaxLength() {
      return this.maxLength;
   }

   public int getCursor() {
      return this.selectionStart;
   }

   private boolean drawsBackground() {
      return this.drawsBackground;
   }

   public void setDrawsBackground(boolean drawsBackground) {
      this.drawsBackground = drawsBackground;
   }

   public void setEditableColor(int editableColor) {
      this.editableColor = editableColor;
   }

   public void setUneditableColor(int uneditableColor) {
      this.uneditableColor = uneditableColor;
   }

   @Nullable
   public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
      return this.visible && this.editable ? super.getNavigationPath(navigation) : null;
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return this.visible && mouseX >= (double)this.getX() && mouseX < (double)(this.getX() + this.width) && mouseY >= (double)this.getY() && mouseY < (double)(this.getY() + this.height);
   }

   public void setFocused(boolean focused) {
      if (this.focusUnlocked || focused) {
         super.setFocused(focused);
         if (focused) {
            this.focusedTicks = 0;
         }

      }
   }

   private boolean isEditable() {
      return this.editable;
   }

   public void setEditable(boolean editable) {
      this.editable = editable;
   }

   public int getInnerWidth() {
      return this.drawsBackground() ? this.width - 8 : this.width;
   }

   public void setSelectionEnd(int index) {
      int j = this.text.length();
      this.selectionEnd = MathHelper.clamp(index, 0, j);
      if (this.textRenderer != null) {
         if (this.firstCharacterIndex > j) {
            this.firstCharacterIndex = j;
         }

         int k = this.getInnerWidth();
         String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), k);
         int l = string.length() + this.firstCharacterIndex;
         if (this.selectionEnd == this.firstCharacterIndex) {
            this.firstCharacterIndex -= this.textRenderer.trimToWidth(this.text, k, true).length();
         }

         if (this.selectionEnd > l) {
            this.firstCharacterIndex += this.selectionEnd - l;
         } else if (this.selectionEnd <= this.firstCharacterIndex) {
            this.firstCharacterIndex -= this.firstCharacterIndex - this.selectionEnd;
         }

         this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, j);
      }

   }

   public void setFocusUnlocked(boolean focusUnlocked) {
      this.focusUnlocked = focusUnlocked;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public void setSuggestion(@Nullable String suggestion) {
      this.suggestion = suggestion;
   }

   public int getCharacterX(int index) {
      return index > this.text.length() ? this.getX() : this.getX() + this.textRenderer.getWidth(this.text.substring(0, index));
   }

   public void appendClickableNarrations(NarrationMessageBuilder builder) {
      builder.put(NarrationPart.TITLE, (Text)this.getNarrationMessage());
   }

   public void setPlaceholder(Text placeholder) {
      this.placeholder = placeholder;
   }
}
