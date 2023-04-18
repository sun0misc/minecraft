package net.minecraft.client.gui.screen.option;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.telemetry.TelemetryEventProperty;
import net.minecraft.client.util.telemetry.TelemetryEventType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TelemetryEventWidget extends ScrollableWidget {
   private static final int MARGIN_X = 32;
   private static final String REQUIRED_TRANSLATION_KEY = "telemetry.event.required";
   private static final String OPTIONAL_TRANSLATION_KEY = "telemetry.event.optional";
   private static final Text PROPERTY_TITLE_TEXT;
   private final TextRenderer textRenderer;
   private Contents contents;
   @Nullable
   private DoubleConsumer scrollConsumer;

   public TelemetryEventWidget(int x, int y, int width, int height, TextRenderer textRenderer) {
      super(x, y, width, height, Text.empty());
      this.textRenderer = textRenderer;
      this.contents = this.collectContents(MinecraftClient.getInstance().isOptionalTelemetryEnabled());
   }

   public void refresh(boolean optionalTelemetryEnabled) {
      this.contents = this.collectContents(optionalTelemetryEnabled);
      this.setScrollY(this.getScrollY());
   }

   private Contents collectContents(boolean optionalTelemetryEnabled) {
      ContentsBuilder lv = new ContentsBuilder(this.getGridWidth());
      List list = new ArrayList(TelemetryEventType.getTypes());
      list.sort(Comparator.comparing(TelemetryEventType::isOptional));
      if (!optionalTelemetryEnabled) {
         list.removeIf(TelemetryEventType::isOptional);
      }

      for(int i = 0; i < list.size(); ++i) {
         TelemetryEventType lv2 = (TelemetryEventType)list.get(i);
         this.appendEventInfo(lv, lv2);
         if (i < list.size() - 1) {
            Objects.requireNonNull(this.textRenderer);
            lv.appendSpace(9);
         }
      }

      return lv.build();
   }

   public void setScrollConsumer(@Nullable DoubleConsumer scrollConsumer) {
      this.scrollConsumer = scrollConsumer;
   }

   protected void setScrollY(double scrollY) {
      super.setScrollY(scrollY);
      if (this.scrollConsumer != null) {
         this.scrollConsumer.accept(this.getScrollY());
      }

   }

   protected int getContentsHeight() {
      return this.contents.grid().getHeight();
   }

   protected boolean overflows() {
      return this.getContentsHeight() > this.height;
   }

   protected double getDeltaYPerScroll() {
      Objects.requireNonNull(this.textRenderer);
      return 9.0;
   }

   protected void renderContents(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      int k = this.getY() + this.getPadding();
      int l = this.getX() + this.getPadding();
      matrices.push();
      matrices.translate((double)l, (double)k, 0.0);
      this.contents.grid().forEachChild((widget) -> {
         widget.render(matrices, mouseX, mouseY, delta);
      });
      matrices.pop();
   }

   protected void appendClickableNarrations(NarrationMessageBuilder builder) {
      builder.put(NarrationPart.TITLE, this.contents.narration());
   }

   private void appendEventInfo(ContentsBuilder builder, TelemetryEventType eventType) {
      String string = eventType.isOptional() ? "telemetry.event.optional" : "telemetry.event.required";
      builder.appendText(this.textRenderer, Text.translatable(string, eventType.getTitle()));
      builder.appendText(this.textRenderer, eventType.getDescription().formatted(Formatting.GRAY));
      Objects.requireNonNull(this.textRenderer);
      builder.appendSpace(9 / 2);
      builder.appendTitle(this.textRenderer, PROPERTY_TITLE_TEXT, 2);
      this.appendProperties(eventType, builder);
   }

   private void appendProperties(TelemetryEventType eventType, ContentsBuilder builder) {
      Iterator var3 = eventType.getProperties().iterator();

      while(var3.hasNext()) {
         TelemetryEventProperty lv = (TelemetryEventProperty)var3.next();
         builder.appendTitle(this.textRenderer, lv.getTitle());
      }

   }

   private int getGridWidth() {
      return this.width - this.getPaddingDoubled();
   }

   static {
      PROPERTY_TITLE_TEXT = Text.translatable("telemetry_info.property_title").formatted(Formatting.UNDERLINE);
   }

   @Environment(EnvType.CLIENT)
   static record Contents(GridWidget grid, Text narration) {
      Contents(GridWidget arg, Text arg2) {
         this.grid = arg;
         this.narration = arg2;
      }

      public GridWidget grid() {
         return this.grid;
      }

      public Text narration() {
         return this.narration;
      }
   }

   @Environment(EnvType.CLIENT)
   static class ContentsBuilder {
      private final int gridWidth;
      private final GridWidget grid;
      private final GridWidget.Adder widgetAdder;
      private final Positioner positioner;
      private final MutableText narration = Text.empty();

      public ContentsBuilder(int gridWidth) {
         this.gridWidth = gridWidth;
         this.grid = new GridWidget();
         this.grid.getMainPositioner().alignLeft();
         this.widgetAdder = this.grid.createAdder(1);
         this.widgetAdder.add(EmptyWidget.ofWidth(gridWidth));
         this.positioner = this.widgetAdder.copyPositioner().alignHorizontalCenter().marginX(32);
      }

      public void appendTitle(TextRenderer textRenderer, Text title) {
         this.appendTitle(textRenderer, title, 0);
      }

      public void appendTitle(TextRenderer textRenderer, Text title, int marginBottom) {
         this.widgetAdder.add((new MultilineTextWidget(title, textRenderer)).setMaxWidth(this.gridWidth), this.widgetAdder.copyPositioner().marginBottom(marginBottom));
         this.narration.append(title).append("\n");
      }

      public void appendText(TextRenderer textRenderer, Text text) {
         this.widgetAdder.add((new MultilineTextWidget(text, textRenderer)).setMaxWidth(this.gridWidth - 64).setCentered(true), this.positioner);
         this.narration.append(text).append("\n");
      }

      public void appendSpace(int height) {
         this.widgetAdder.add(EmptyWidget.ofHeight(height));
      }

      public Contents build() {
         this.grid.refreshPositions();
         return new Contents(this.grid, this.narration);
      }
   }
}
