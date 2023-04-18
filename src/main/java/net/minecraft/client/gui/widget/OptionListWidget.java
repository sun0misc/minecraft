package net.minecraft.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class OptionListWidget extends ElementListWidget {
   public OptionListWidget(MinecraftClient arg, int i, int j, int k, int l, int m) {
      super(arg, i, j, k, l, m);
      this.centerListVertically = false;
   }

   public int addSingleOptionEntry(SimpleOption option) {
      return this.addEntry(OptionListWidget.WidgetEntry.create(this.client.options, this.width, option));
   }

   public void addOptionEntry(SimpleOption firstOption, @Nullable SimpleOption secondOption) {
      this.addEntry(OptionListWidget.WidgetEntry.create(this.client.options, this.width, firstOption, secondOption));
   }

   public void addAll(SimpleOption[] options) {
      for(int i = 0; i < options.length; i += 2) {
         this.addOptionEntry(options[i], i < options.length - 1 ? options[i + 1] : null);
      }

   }

   public int getRowWidth() {
      return 400;
   }

   protected int getScrollbarPositionX() {
      return super.getScrollbarPositionX() + 32;
   }

   @Nullable
   public ClickableWidget getWidgetFor(SimpleOption option) {
      Iterator var2 = this.children().iterator();

      ClickableWidget lv2;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         WidgetEntry lv = (WidgetEntry)var2.next();
         lv2 = (ClickableWidget)lv.optionsToWidgets.get(option);
      } while(lv2 == null);

      return lv2;
   }

   public Optional getHoveredWidget(double mouseX, double mouseY) {
      Iterator var5 = this.children().iterator();

      while(var5.hasNext()) {
         WidgetEntry lv = (WidgetEntry)var5.next();
         Iterator var7 = lv.widgets.iterator();

         while(var7.hasNext()) {
            ClickableWidget lv2 = (ClickableWidget)var7.next();
            if (lv2.isMouseOver(mouseX, mouseY)) {
               return Optional.of(lv2);
            }
         }
      }

      return Optional.empty();
   }

   @Environment(EnvType.CLIENT)
   protected static class WidgetEntry extends ElementListWidget.Entry {
      final Map optionsToWidgets;
      final List widgets;

      private WidgetEntry(Map optionsToWidgets) {
         this.optionsToWidgets = optionsToWidgets;
         this.widgets = ImmutableList.copyOf(optionsToWidgets.values());
      }

      public static WidgetEntry create(GameOptions options, int width, SimpleOption option) {
         return new WidgetEntry(ImmutableMap.of(option, option.createWidget(options, width / 2 - 155, 0, 310)));
      }

      public static WidgetEntry create(GameOptions options, int width, SimpleOption firstOption, @Nullable SimpleOption secondOption) {
         ClickableWidget lv = firstOption.createWidget(options, width / 2 - 155, 0, 150);
         return secondOption == null ? new WidgetEntry(ImmutableMap.of(firstOption, lv)) : new WidgetEntry(ImmutableMap.of(firstOption, lv, secondOption, secondOption.createWidget(options, width / 2 - 155 + 160, 0, 150)));
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.widgets.forEach((widget) -> {
            widget.setY(y);
            widget.render(matrices, mouseX, mouseY, tickDelta);
         });
      }

      public List children() {
         return this.widgets;
      }

      public List selectableChildren() {
         return this.widgets;
      }
   }
}
