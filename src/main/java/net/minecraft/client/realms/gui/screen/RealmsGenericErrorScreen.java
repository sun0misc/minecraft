package net.minecraft.client.realms.gui.screen;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class RealmsGenericErrorScreen extends RealmsScreen {
   private final Screen parent;
   private final Pair errorMessages;
   private MultilineText description;

   public RealmsGenericErrorScreen(RealmsServiceException realmsServiceException, Screen parent) {
      super(NarratorManager.EMPTY);
      this.description = MultilineText.EMPTY;
      this.parent = parent;
      this.errorMessages = getErrorMessages(realmsServiceException);
   }

   public RealmsGenericErrorScreen(Text description, Screen parent) {
      super(NarratorManager.EMPTY);
      this.description = MultilineText.EMPTY;
      this.parent = parent;
      this.errorMessages = getErrorMessages(description);
   }

   public RealmsGenericErrorScreen(Text title, Text description, Screen parent) {
      super(NarratorManager.EMPTY);
      this.description = MultilineText.EMPTY;
      this.parent = parent;
      this.errorMessages = getErrorMessages(title, description);
   }

   private static Pair getErrorMessages(RealmsServiceException exception) {
      if (exception.error == null) {
         return Pair.of(Text.literal("An error occurred (" + exception.httpResultCode + "):"), Text.literal(exception.httpResponseText));
      } else {
         String string = "mco.errorMessage." + exception.error.getErrorCode();
         return Pair.of(Text.literal("Realms (" + exception.error + "):"), I18n.hasTranslation(string) ? Text.translatable(string) : Text.of(exception.error.getErrorMessage()));
      }
   }

   private static Pair getErrorMessages(Text description) {
      return Pair.of(Text.literal("An error occurred: "), description);
   }

   private static Pair getErrorMessages(Text title, Text description) {
      return Pair.of(title, description);
   }

   public void init() {
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Ok"), (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 100, this.height - 52, 200, 20).build());
      this.description = MultilineText.create(this.textRenderer, (StringVisitable)this.errorMessages.getSecond(), this.width * 3 / 4);
   }

   public Text getNarratedTitle() {
      return Text.empty().append((Text)this.errorMessages.getFirst()).append(": ").append((Text)this.errorMessages.getSecond());
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredTextWithShadow(matrices, this.textRenderer, (Text)this.errorMessages.getFirst(), this.width / 2, 80, 16777215);
      MultilineText var10000 = this.description;
      int var10002 = this.width / 2;
      Objects.requireNonNull(this.client.textRenderer);
      var10000.drawCenterWithShadow(matrices, var10002, 100, 9, 16711680);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
