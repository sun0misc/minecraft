package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@Environment(EnvType.CLIENT)
public class WidgetTooltipPositioner implements TooltipPositioner {
   private static final int field_42159 = 5;
   private static final int field_42160 = 12;
   public static final int field_42157 = 3;
   public static final int field_42158 = 5;
   private final ClickableWidget widget;

   public WidgetTooltipPositioner(ClickableWidget widget) {
      this.widget = widget;
   }

   public Vector2ic getPosition(Screen screen, int x, int y, int width, int height) {
      Vector2i vector2i = new Vector2i(x + 12, y);
      if (vector2i.x + width > screen.width - 5) {
         vector2i.x = Math.max(x - 12 - width, 9);
      }

      vector2i.y += 3;
      int m = height + 3 + 3;
      int n = this.widget.getY() + this.widget.getHeight() + 3 + getOffsetY(0, 0, this.widget.getHeight());
      int o = screen.height - 5;
      if (n + m <= o) {
         vector2i.y += getOffsetY(vector2i.y, this.widget.getY(), this.widget.getHeight());
      } else {
         vector2i.y -= m + getOffsetY(vector2i.y, this.widget.getY() + this.widget.getHeight(), this.widget.getHeight());
      }

      return vector2i;
   }

   private static int getOffsetY(int tooltipY, int widgetY, int widgetHeight) {
      int l = Math.min(Math.abs(tooltipY - widgetY), widgetHeight);
      return Math.round(MathHelper.lerp((float)l / (float)widgetHeight, (float)(widgetHeight - 3), 5.0F));
   }
}
