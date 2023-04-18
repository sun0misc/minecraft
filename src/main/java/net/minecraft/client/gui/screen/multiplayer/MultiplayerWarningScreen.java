package net.minecraft.client.gui.screen.multiplayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class MultiplayerWarningScreen extends WarningScreen {
   private static final Text HEADER;
   private static final Text MESSAGE;
   private static final Text CHECK_MESSAGE;
   private static final Text NARRATED_TEXT;
   private final Screen parent;

   public MultiplayerWarningScreen(Screen parent) {
      super(HEADER, MESSAGE, CHECK_MESSAGE, NARRATED_TEXT);
      this.parent = parent;
   }

   protected void initButtons(int yOffset) {
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.PROCEED, (arg) -> {
         if (this.checkbox.isChecked()) {
            this.client.options.skipMultiplayerWarning = true;
            this.client.options.write();
         }

         this.client.setScreen(new MultiplayerScreen(this.parent));
      }).dimensions(this.width / 2 - 155, 100 + yOffset, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (arg) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 155 + 160, 100 + yOffset, 150, 20).build());
   }

   static {
      HEADER = Text.translatable("multiplayerWarning.header").formatted(Formatting.BOLD);
      MESSAGE = Text.translatable("multiplayerWarning.message");
      CHECK_MESSAGE = Text.translatable("multiplayerWarning.check");
      NARRATED_TEXT = HEADER.copy().append("\n").append(MESSAGE);
   }
}
