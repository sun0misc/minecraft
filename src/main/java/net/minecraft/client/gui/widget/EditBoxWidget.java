package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class EditBoxWidget extends ScrollableWidget {
   private static final int CURSOR_PADDING = 1;
   private static final int CURSOR_COLOR = -3092272;
   private static final String UNDERSCORE = "_";
   private static final int FOCUSED_BOX_TEXT_COLOR = -2039584;
   private static final int UNFOCUSED_BOX_TEXT_COLOR = -857677600;
   private final TextRenderer textRenderer;
   private final Text placeholder;
   private final EditBox editBox;
   private int tick;

   public EditBoxWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message) {
      super(x, y, width, height, message);
      this.textRenderer = textRenderer;
      this.placeholder = placeholder;
      this.editBox = new EditBox(textRenderer, width - this.getPaddingDoubled());
      this.editBox.setCursorChangeListener(this::onCursorChange);
   }

   public void setMaxLength(int maxLength) {
      this.editBox.setMaxLength(maxLength);
   }

   public void setChangeListener(Consumer changeListener) {
      this.editBox.setChangeListener(changeListener);
   }

   public void setText(String text) {
      this.editBox.setText(text);
   }

   public String getText() {
      return this.editBox.getText();
   }

   public void tick() {
      ++this.tick;
   }

   public void appendClickableNarrations(NarrationMessageBuilder builder) {
      builder.put(NarrationPart.TITLE, (Text)Text.translatable("gui.narrate.editBox", this.getMessage(), this.getText()));
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else if (this.isWithinBounds(mouseX, mouseY) && button == 0) {
         this.editBox.setSelecting(Screen.hasShiftDown());
         this.moveCursor(mouseX, mouseY);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      } else if (this.isWithinBounds(mouseX, mouseY) && button == 0) {
         this.editBox.setSelecting(true);
         this.moveCursor(mouseX, mouseY);
         this.editBox.setSelecting(Screen.hasShiftDown());
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.editBox.handleSpecialKey(keyCode);
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.visible && this.isFocused() && SharedConstants.isValidChar(chr)) {
         this.editBox.replaceSelection(Character.toString(chr));
         return true;
      } else {
         return false;
      }
   }

   protected void renderContents(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      String string = this.editBox.getText();
      if (string.isEmpty() && !this.isFocused()) {
         this.textRenderer.drawTrimmed(matrices, this.placeholder, this.getX() + this.getPadding(), this.getY() + this.getPadding(), this.width - this.getPaddingDoubled(), -857677600);
      } else {
         int k = this.editBox.getCursor();
         boolean bl = this.isFocused() && this.tick / 6 % 2 == 0;
         boolean bl2 = k < string.length();
         int l = 0;
         int m = 0;
         int n = this.getY() + this.getPadding();

         int var10002;
         int var10004;
         for(Iterator var12 = this.editBox.getLines().iterator(); var12.hasNext(); n += 9) {
            EditBox.Substring lv = (EditBox.Substring)var12.next();
            Objects.requireNonNull(this.textRenderer);
            boolean bl3 = this.isVisible(n, n + 9);
            if (bl && bl2 && k >= lv.beginIndex() && k <= lv.endIndex()) {
               if (bl3) {
                  l = this.textRenderer.drawWithShadow(matrices, string.substring(lv.beginIndex(), k), (float)(this.getX() + this.getPadding()), (float)n, -2039584) - 1;
                  var10002 = n - 1;
                  int var10003 = l + 1;
                  var10004 = n + 1;
                  Objects.requireNonNull(this.textRenderer);
                  DrawableHelper.fill(matrices, l, var10002, var10003, var10004 + 9, -3092272);
                  this.textRenderer.drawWithShadow(matrices, string.substring(k, lv.endIndex()), (float)l, (float)n, -2039584);
               }
            } else {
               if (bl3) {
                  l = this.textRenderer.drawWithShadow(matrices, string.substring(lv.beginIndex(), lv.endIndex()), (float)(this.getX() + this.getPadding()), (float)n, -2039584) - 1;
               }

               m = n;
            }

            Objects.requireNonNull(this.textRenderer);
         }

         if (bl && !bl2) {
            Objects.requireNonNull(this.textRenderer);
            if (this.isVisible(m, m + 9)) {
               this.textRenderer.drawWithShadow(matrices, "_", (float)l, (float)m, -3092272);
            }
         }

         if (this.editBox.hasSelection()) {
            EditBox.Substring lv2 = this.editBox.getSelection();
            int o = this.getX() + this.getPadding();
            n = this.getY() + this.getPadding();
            Iterator var20 = this.editBox.getLines().iterator();

            while(var20.hasNext()) {
               EditBox.Substring lv3 = (EditBox.Substring)var20.next();
               if (lv2.beginIndex() > lv3.endIndex()) {
                  Objects.requireNonNull(this.textRenderer);
                  n += 9;
               } else {
                  if (lv3.beginIndex() > lv2.endIndex()) {
                     break;
                  }

                  Objects.requireNonNull(this.textRenderer);
                  if (this.isVisible(n, n + 9)) {
                     int p = this.textRenderer.getWidth(string.substring(lv3.beginIndex(), Math.max(lv2.beginIndex(), lv3.beginIndex())));
                     int q;
                     if (lv2.endIndex() > lv3.endIndex()) {
                        q = this.width - this.getPadding();
                     } else {
                        q = this.textRenderer.getWidth(string.substring(lv3.beginIndex(), lv2.endIndex()));
                     }

                     var10002 = o + p;
                     var10004 = o + q;
                     Objects.requireNonNull(this.textRenderer);
                     this.drawSelection(matrices, var10002, n, var10004, n + 9);
                  }

                  Objects.requireNonNull(this.textRenderer);
                  n += 9;
               }
            }
         }

      }
   }

   protected void renderOverlay(MatrixStack matrices) {
      super.renderOverlay(matrices);
      if (this.editBox.hasMaxLength()) {
         int i = this.editBox.getMaxLength();
         Text lv = Text.translatable("gui.multiLineEditBox.character_limit", this.editBox.getText().length(), i);
         drawTextWithShadow(matrices, this.textRenderer, lv, this.getX() + this.width - this.textRenderer.getWidth((StringVisitable)lv), this.getY() + this.height + 4, 10526880);
      }

   }

   public int getContentsHeight() {
      Objects.requireNonNull(this.textRenderer);
      return 9 * this.editBox.getLineCount();
   }

   protected boolean overflows() {
      return (double)this.editBox.getLineCount() > this.getMaxLinesWithoutOverflow();
   }

   protected double getDeltaYPerScroll() {
      Objects.requireNonNull(this.textRenderer);
      return 9.0 / 2.0;
   }

   private void drawSelection(MatrixStack matrices, int left, int top, int right, int bottom) {
      RenderSystem.enableColorLogicOp();
      RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
      fill(matrices, left, top, right, bottom, -16776961);
      RenderSystem.disableColorLogicOp();
   }

   private void onCursorChange() {
      double d = this.getScrollY();
      EditBox var10000 = this.editBox;
      Objects.requireNonNull(this.textRenderer);
      EditBox.Substring lv = var10000.getLine((int)(d / 9.0));
      int var5;
      if (this.editBox.getCursor() <= lv.beginIndex()) {
         var5 = this.editBox.getCurrentLineIndex();
         Objects.requireNonNull(this.textRenderer);
         d = (double)(var5 * 9);
      } else {
         var10000 = this.editBox;
         double var10001 = d + (double)this.height;
         Objects.requireNonNull(this.textRenderer);
         EditBox.Substring lv2 = var10000.getLine((int)(var10001 / 9.0) - 1);
         if (this.editBox.getCursor() > lv2.endIndex()) {
            var5 = this.editBox.getCurrentLineIndex();
            Objects.requireNonNull(this.textRenderer);
            var5 = var5 * 9 - this.height;
            Objects.requireNonNull(this.textRenderer);
            d = (double)(var5 + 9 + this.getPaddingDoubled());
         }
      }

      this.setScrollY(d);
   }

   private double getMaxLinesWithoutOverflow() {
      double var10000 = (double)(this.height - this.getPaddingDoubled());
      Objects.requireNonNull(this.textRenderer);
      return var10000 / 9.0;
   }

   private void moveCursor(double mouseX, double mouseY) {
      double f = mouseX - (double)this.getX() - (double)this.getPadding();
      double g = mouseY - (double)this.getY() - (double)this.getPadding() + this.getScrollY();
      this.editBox.moveCursor(f, g);
   }
}
