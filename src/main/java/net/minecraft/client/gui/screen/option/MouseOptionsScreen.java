package net.minecraft.client.gui.screen.option;

import java.util.Arrays;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class MouseOptionsScreen extends GameOptionsScreen {
   private OptionListWidget buttonList;

   private static SimpleOption[] getOptions(GameOptions gameOptions) {
      return new SimpleOption[]{gameOptions.getMouseSensitivity(), gameOptions.getInvertYMouse(), gameOptions.getMouseWheelSensitivity(), gameOptions.getDiscreteMouseScroll(), gameOptions.getTouchscreen()};
   }

   public MouseOptionsScreen(Screen parent, GameOptions gameOptions) {
      super(parent, gameOptions, Text.translatable("options.mouse_settings.title"));
   }

   protected void init() {
      this.buttonList = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
      if (InputUtil.isRawMouseMotionSupported()) {
         this.buttonList.addAll((SimpleOption[])Stream.concat(Arrays.stream(getOptions(this.gameOptions)), Stream.of(this.gameOptions.getRawMouseInput())).toArray((i) -> {
            return new SimpleOption[i];
         }));
      } else {
         this.buttonList.addAll(getOptions(this.gameOptions));
      }

      this.addSelectableChild(this.buttonList);
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.gameOptions.write();
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      this.buttonList.render(matrices, mouseX, mouseY, delta);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 5, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
