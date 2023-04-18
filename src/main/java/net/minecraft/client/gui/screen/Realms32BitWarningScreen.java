package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class Realms32BitWarningScreen extends WarningScreen {
   private static final Text HEADER;
   private static final Text MESSAGE;
   private static final Text CHECK_MESSAGE;
   private static final Text NARRATED_TEXT;
   private final Screen parent;

   public Realms32BitWarningScreen(Screen parent) {
      super(HEADER, MESSAGE, CHECK_MESSAGE, NARRATED_TEXT);
      this.parent = parent;
   }

   protected void initButtons(int yOffset) {
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         if (this.checkbox.isChecked()) {
            this.client.options.skipRealms32BitWarning = true;
            this.client.options.write();
         }

         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 75, 100 + yOffset, 150, 20).build());
   }

   static {
      HEADER = Text.translatable("title.32bit.deprecation.realms.header").formatted(Formatting.BOLD);
      MESSAGE = Text.translatable("title.32bit.deprecation.realms");
      CHECK_MESSAGE = Text.translatable("title.32bit.deprecation.realms.check");
      NARRATED_TEXT = HEADER.copy().append("\n").append(MESSAGE);
   }
}
