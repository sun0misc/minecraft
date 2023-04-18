package net.minecraft.client.gui.screen;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class BackupPromptScreen extends Screen {
   private final Screen parent;
   protected final Callback callback;
   private final Text subtitle;
   private final boolean showEraseCacheCheckbox;
   private MultilineText wrappedText;
   protected int field_32236;
   private CheckboxWidget eraseCacheCheckbox;

   public BackupPromptScreen(Screen parent, Callback callback, Text title, Text subtitle, boolean showEraseCacheCheckBox) {
      super(title);
      this.wrappedText = MultilineText.EMPTY;
      this.parent = parent;
      this.callback = callback;
      this.subtitle = subtitle;
      this.showEraseCacheCheckbox = showEraseCacheCheckBox;
   }

   protected void init() {
      super.init();
      this.wrappedText = MultilineText.create(this.textRenderer, this.subtitle, this.width - 50);
      int var10000 = this.wrappedText.count() + 1;
      Objects.requireNonNull(this.textRenderer);
      int i = var10000 * 9;
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.backupJoinConfirmButton"), (button) -> {
         this.callback.proceed(true, this.eraseCacheCheckbox.isChecked());
      }).dimensions(this.width / 2 - 155, 100 + i, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.backupJoinSkipButton"), (button) -> {
         this.callback.proceed(false, this.eraseCacheCheckbox.isChecked());
      }).dimensions(this.width / 2 - 155 + 160, 100 + i, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 155 + 80, 124 + i, 150, 20).build());
      this.eraseCacheCheckbox = new CheckboxWidget(this.width / 2 - 155 + 80, 76 + i, 150, 20, Text.translatable("selectWorld.backupEraseCache"), false);
      if (this.showEraseCacheCheckbox) {
         this.addDrawableChild(this.eraseCacheCheckbox);
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 50, 16777215);
      this.wrappedText.drawCenterWithShadow(matrices, this.width / 2, 70);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   @Environment(EnvType.CLIENT)
   public interface Callback {
      void proceed(boolean backup, boolean eraseCache);
   }
}
