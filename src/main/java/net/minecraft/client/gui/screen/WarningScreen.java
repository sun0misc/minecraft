package net.minecraft.client.gui.screen;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class WarningScreen extends Screen {
   private final Text message;
   @Nullable
   private final Text checkMessage;
   private final Text narratedText;
   @Nullable
   protected CheckboxWidget checkbox;
   private MultilineText messageText;

   protected WarningScreen(Text header, Text message, Text narratedText) {
      this(header, message, (Text)null, narratedText);
   }

   protected WarningScreen(Text header, Text message, @Nullable Text checkMessage, Text narratedText) {
      super(header);
      this.messageText = MultilineText.EMPTY;
      this.message = message;
      this.checkMessage = checkMessage;
      this.narratedText = narratedText;
   }

   protected abstract void initButtons(int yOffset);

   protected void init() {
      super.init();
      this.messageText = MultilineText.create(this.textRenderer, this.message, this.width - 100);
      int i = (this.messageText.count() + 1) * this.getLineHeight();
      if (this.checkMessage != null) {
         int j = this.textRenderer.getWidth((StringVisitable)this.checkMessage);
         this.checkbox = new CheckboxWidget(this.width / 2 - j / 2 - 8, 76 + i, j + 24, 20, this.checkMessage, false);
         this.addDrawableChild(this.checkbox);
      }

      this.initButtons(i);
   }

   public Text getNarratedTitle() {
      return this.narratedText;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.drawTitle(matrices);
      int k = this.width / 2 - this.messageText.getMaxWidth() / 2;
      this.messageText.drawWithShadow(matrices, k, 70, this.getLineHeight(), 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }

   protected void drawTitle(MatrixStack matrices) {
      drawTextWithShadow(matrices, this.textRenderer, this.title, 25, 30, 16777215);
   }

   protected int getLineHeight() {
      Objects.requireNonNull(this.textRenderer);
      return 9 * 2;
   }
}
