package net.minecraft.client.gui.screen.option;

import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class TelemetryInfoScreen extends Screen {
   private static final int MARGIN = 8;
   private static final Text TITLE_TEXT = Text.translatable("telemetry_info.screen.title");
   private static final Text DESCRIPTION_TEXT;
   private static final Text GIVE_FEEDBACK_TEXT;
   private static final Text SHOW_DATA_TEXT;
   private final Screen parent;
   private final GameOptions options;
   private TelemetryEventWidget telemetryEventWidget;
   private double scroll;

   public TelemetryInfoScreen(Screen parent, GameOptions options) {
      super(TITLE_TEXT);
      this.parent = parent;
      this.options = options;
   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinSentences(super.getNarratedTitle(), DESCRIPTION_TEXT);
   }

   protected void init() {
      SimplePositioningWidget lv = new SimplePositioningWidget();
      lv.getMainPositioner().margin(8);
      lv.setMinHeight(this.height);
      GridWidget lv2 = (GridWidget)lv.add(new GridWidget(), lv.copyPositioner().relative(0.5F, 0.0F));
      lv2.getMainPositioner().alignHorizontalCenter().marginBottom(8);
      GridWidget.Adder lv3 = lv2.createAdder(1);
      lv3.add(new TextWidget(this.getTitle(), this.textRenderer));
      lv3.add((new MultilineTextWidget(DESCRIPTION_TEXT, this.textRenderer)).setMaxWidth(this.width - 16).setCentered(true));
      GridWidget lv4 = this.createButtonRow(ButtonWidget.builder(GIVE_FEEDBACK_TEXT, this::openFeedbackPage).build(), ButtonWidget.builder(SHOW_DATA_TEXT, this::openLogDirectory).build());
      lv3.add(lv4);
      GridWidget lv5 = this.createButtonRow(this.createOptInButton(), ButtonWidget.builder(ScreenTexts.DONE, this::goBack).build());
      lv.add(lv5, lv.copyPositioner().relative(0.5F, 1.0F));
      lv.refreshPositions();
      this.telemetryEventWidget = new TelemetryEventWidget(0, 0, this.width - 40, lv5.getY() - (lv4.getY() + lv4.getHeight()) - 16, this.client.textRenderer);
      this.telemetryEventWidget.setScrollY(this.scroll);
      this.telemetryEventWidget.setScrollConsumer((scroll) -> {
         this.scroll = scroll;
      });
      this.setInitialFocus(this.telemetryEventWidget);
      lv3.add(this.telemetryEventWidget);
      lv.refreshPositions();
      SimplePositioningWidget.setPos(lv, 0, 0, this.width, this.height, 0.5F, 0.0F);
      lv.forEachChild((child) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(child);
      });
   }

   private ClickableWidget createOptInButton() {
      ClickableWidget lv = this.options.getTelemetryOptInExtra().createWidget(this.options, 0, 0, 150, (value) -> {
         this.telemetryEventWidget.refresh(value);
      });
      lv.active = this.client.isOptionalTelemetryEnabledByApi();
      return lv;
   }

   private void goBack(ButtonWidget button) {
      this.client.setScreen(this.parent);
   }

   private void openFeedbackPage(ButtonWidget button) {
      this.client.setScreen(new ConfirmLinkScreen((confirmed) -> {
         if (confirmed) {
            Util.getOperatingSystem().open("https://aka.ms/javafeedback?ref=game");
         }

         this.client.setScreen(this);
      }, "https://aka.ms/javafeedback?ref=game", true));
   }

   private void openLogDirectory(ButtonWidget button) {
      Path path = this.client.getTelemetryManager().getLogManager();
      Util.getOperatingSystem().open(path.toUri());
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackgroundTexture(matrices);
      super.render(matrices, mouseX, mouseY, delta);
   }

   private GridWidget createButtonRow(ClickableWidget left, ClickableWidget right) {
      GridWidget lv = new GridWidget();
      lv.getMainPositioner().alignHorizontalCenter().marginX(4);
      lv.add(left, 0, 0);
      lv.add(right, 0, 1);
      return lv;
   }

   static {
      DESCRIPTION_TEXT = Text.translatable("telemetry_info.screen.description").formatted(Formatting.GRAY);
      GIVE_FEEDBACK_TEXT = Text.translatable("telemetry_info.button.give_feedback");
      SHOW_DATA_TEXT = Text.translatable("telemetry_info.button.show_data");
   }
}
