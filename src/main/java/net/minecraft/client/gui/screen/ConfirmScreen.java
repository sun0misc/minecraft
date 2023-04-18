package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ConfirmScreen extends Screen {
   private static final int TITLE_BOTTOM_MARGIN = 20;
   private final Text message;
   private MultilineText messageSplit;
   protected Text yesText;
   protected Text noText;
   private int buttonEnableTimer;
   protected final BooleanConsumer callback;
   private final List buttons;

   public ConfirmScreen(BooleanConsumer callback, Text title, Text message) {
      this(callback, title, message, ScreenTexts.YES, ScreenTexts.NO);
   }

   public ConfirmScreen(BooleanConsumer callback, Text title, Text message, Text yesText, Text noText) {
      super(title);
      this.messageSplit = MultilineText.EMPTY;
      this.buttons = Lists.newArrayList();
      this.callback = callback;
      this.message = message;
      this.yesText = yesText;
      this.noText = noText;
   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinSentences(super.getNarratedTitle(), this.message);
   }

   protected void init() {
      super.init();
      this.messageSplit = MultilineText.create(this.textRenderer, this.message, this.width - 50);
      int i = MathHelper.clamp(this.getMessageY() + this.getMessagesHeight() + 20, this.height / 6 + 96, this.height - 24);
      this.buttons.clear();
      this.addButtons(i);
   }

   protected void addButtons(int y) {
      this.addButton(ButtonWidget.builder(this.yesText, (button) -> {
         this.callback.accept(true);
      }).dimensions(this.width / 2 - 155, y, 150, 20).build());
      this.addButton(ButtonWidget.builder(this.noText, (button) -> {
         this.callback.accept(false);
      }).dimensions(this.width / 2 - 155 + 160, y, 150, 20).build());
   }

   protected void addButton(ButtonWidget button) {
      this.buttons.add((ButtonWidget)this.addDrawableChild(button));
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, this.getTitleY(), 16777215);
      this.messageSplit.drawCenterWithShadow(matrices, this.width / 2, this.getMessageY());
      super.render(matrices, mouseX, mouseY, delta);
   }

   private int getTitleY() {
      int i = (this.height - this.getMessagesHeight()) / 2;
      int var10000 = i - 20;
      Objects.requireNonNull(this.textRenderer);
      return MathHelper.clamp(var10000 - 9, 10, 80);
   }

   private int getMessageY() {
      return this.getTitleY() + 20;
   }

   private int getMessagesHeight() {
      int var10000 = this.messageSplit.count();
      Objects.requireNonNull(this.textRenderer);
      return var10000 * 9;
   }

   public void disableButtons(int ticks) {
      this.buttonEnableTimer = ticks;

      ButtonWidget lv;
      for(Iterator var2 = this.buttons.iterator(); var2.hasNext(); lv.active = false) {
         lv = (ButtonWidget)var2.next();
      }

   }

   public void tick() {
      super.tick();
      ButtonWidget lv;
      if (--this.buttonEnableTimer == 0) {
         for(Iterator var1 = this.buttons.iterator(); var1.hasNext(); lv.active = true) {
            lv = (ButtonWidget)var1.next();
         }
      }

   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.callback.accept(false);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
