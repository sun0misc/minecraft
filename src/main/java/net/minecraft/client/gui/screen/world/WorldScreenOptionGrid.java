package net.minecraft.client.gui.screen.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
class WorldScreenOptionGrid {
   private static final int BUTTON_WIDTH = 44;
   private final List options;

   WorldScreenOptionGrid(List options) {
      this.options = options;
   }

   public void refresh() {
      this.options.forEach(Option::refresh);
   }

   public static Builder builder(int width) {
      return new Builder(width);
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      final int width;
      private final List options = new ArrayList();
      int marginLeft;
      int rowSpacing = 4;
      int rows;
      Optional tooltipBoxDisplay = Optional.empty();

      public Builder(int width) {
         this.width = width;
      }

      void incrementRows() {
         ++this.rows;
      }

      public OptionBuilder add(Text text, BooleanSupplier getter, Consumer setter) {
         OptionBuilder lv = new OptionBuilder(text, getter, setter, 44);
         this.options.add(lv);
         return lv;
      }

      public Builder marginLeft(int marginLeft) {
         this.marginLeft = marginLeft;
         return this;
      }

      public Builder setRowSpacing(int rowSpacing) {
         this.rowSpacing = rowSpacing;
         return this;
      }

      public WorldScreenOptionGrid build(Consumer widgetConsumer) {
         GridWidget lv = (new GridWidget()).setRowSpacing(this.rowSpacing);
         lv.add(EmptyWidget.ofWidth(this.width - 44), 0, 0);
         lv.add(EmptyWidget.ofWidth(44), 0, 1);
         List list = new ArrayList();
         this.rows = 0;
         Iterator var4 = this.options.iterator();

         while(var4.hasNext()) {
            OptionBuilder lv2 = (OptionBuilder)var4.next();
            list.add(lv2.build(this, lv, 0));
         }

         lv.refreshPositions();
         widgetConsumer.accept(lv);
         WorldScreenOptionGrid lv3 = new WorldScreenOptionGrid(list);
         lv3.refresh();
         return lv3;
      }

      public Builder withTooltipBox(int maxInfoRows, boolean alwaysMaxHeight) {
         this.tooltipBoxDisplay = Optional.of(new TooltipBoxDisplay(maxInfoRows, alwaysMaxHeight));
         return this;
      }
   }

   @Environment(EnvType.CLIENT)
   static record TooltipBoxDisplay(int maxInfoRows, boolean alwaysMaxHeight) {
      final int maxInfoRows;
      final boolean alwaysMaxHeight;

      TooltipBoxDisplay(int i, boolean bl) {
         this.maxInfoRows = i;
         this.alwaysMaxHeight = bl;
      }

      public int maxInfoRows() {
         return this.maxInfoRows;
      }

      public boolean alwaysMaxHeight() {
         return this.alwaysMaxHeight;
      }
   }

   @Environment(EnvType.CLIENT)
   static record Option(CyclingButtonWidget button, BooleanSupplier getter, @Nullable BooleanSupplier toggleable) {
      Option(CyclingButtonWidget button, BooleanSupplier getter, @Nullable BooleanSupplier toggleable) {
         this.button = button;
         this.getter = getter;
         this.toggleable = toggleable;
      }

      public void refresh() {
         this.button.setValue(this.getter.getAsBoolean());
         if (this.toggleable != null) {
            this.button.active = this.toggleable.getAsBoolean();
         }

      }

      public CyclingButtonWidget button() {
         return this.button;
      }

      public BooleanSupplier getter() {
         return this.getter;
      }

      @Nullable
      public BooleanSupplier toggleable() {
         return this.toggleable;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class OptionBuilder {
      private final Text text;
      private final BooleanSupplier getter;
      private final Consumer setter;
      @Nullable
      private Text tooltip;
      @Nullable
      private BooleanSupplier toggleable;
      private final int buttonWidth;

      OptionBuilder(Text text, BooleanSupplier getter, Consumer setter, int buttonWidth) {
         this.text = text;
         this.getter = getter;
         this.setter = setter;
         this.buttonWidth = buttonWidth;
      }

      public OptionBuilder toggleable(BooleanSupplier toggleable) {
         this.toggleable = toggleable;
         return this;
      }

      public OptionBuilder tooltip(Text tooltip) {
         this.tooltip = tooltip;
         return this;
      }

      Option build(Builder gridBuilder, GridWidget gridWidget, int row) {
         gridBuilder.incrementRows();
         TextWidget lv = (new TextWidget(this.text, MinecraftClient.getInstance().textRenderer)).alignLeft();
         gridWidget.add(lv, gridBuilder.rows, row, gridWidget.copyPositioner().relative(0.0F, 0.5F).marginLeft(gridBuilder.marginLeft));
         Optional optional = gridBuilder.tooltipBoxDisplay;
         CyclingButtonWidget.Builder lv2 = CyclingButtonWidget.onOffBuilder(this.getter.getAsBoolean());
         lv2.omitKeyText();
         boolean bl = this.tooltip != null && !optional.isPresent();
         if (bl) {
            Tooltip lv3 = Tooltip.of(this.tooltip);
            lv2.tooltip((value) -> {
               return lv3;
            });
         }

         if (this.tooltip != null && !bl) {
            lv2.narration((button) -> {
               return ScreenTexts.joinSentences(this.text, button.getGenericNarrationMessage(), this.tooltip);
            });
         } else {
            lv2.narration((button) -> {
               return ScreenTexts.joinSentences(this.text, button.getGenericNarrationMessage());
            });
         }

         CyclingButtonWidget lv4 = lv2.build(0, 0, this.buttonWidth, 20, Text.empty(), (button, value) -> {
            this.setter.accept(value);
         });
         if (this.toggleable != null) {
            lv4.active = this.toggleable.getAsBoolean();
         }

         gridWidget.add(lv4, gridBuilder.rows, row + 1, gridWidget.copyPositioner().alignRight());
         if (this.tooltip != null) {
            optional.ifPresent((tooltipBoxDisplay) -> {
               Text lv = this.tooltip.copy().formatted(Formatting.GRAY);
               TextRenderer lv2 = MinecraftClient.getInstance().textRenderer;
               MultilineTextWidget lv3 = new MultilineTextWidget(lv, lv2);
               lv3.setMaxWidth(gridBuilder.width - gridBuilder.marginLeft - this.buttonWidth);
               lv3.setMaxRows(tooltipBoxDisplay.maxInfoRows());
               gridBuilder.incrementRows();
               int var10000;
               if (tooltipBoxDisplay.alwaysMaxHeight) {
                  Objects.requireNonNull(lv2);
                  var10000 = 9 * tooltipBoxDisplay.maxInfoRows - lv3.getHeight();
               } else {
                  var10000 = 0;
               }

               int j = var10000;
               gridWidget.add(lv3, gridBuilder.rows, row, gridWidget.copyPositioner().marginTop(-gridBuilder.rowSpacing).marginBottom(j));
            });
         }

         return new Option(lv4, this.getter, this.toggleable);
      }
   }
}
