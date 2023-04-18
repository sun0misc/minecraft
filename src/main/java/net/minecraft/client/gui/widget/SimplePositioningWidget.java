package net.minecraft.client.gui.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SimplePositioningWidget extends WrapperWidget {
   private final List elements;
   private int minHeight;
   private int minWidth;
   private final Positioner mainPositioner;

   public SimplePositioningWidget() {
      this(0, 0, 0, 0);
   }

   public SimplePositioningWidget(int width, int height) {
      this(0, 0, width, height);
   }

   public SimplePositioningWidget(int i, int j, int k, int l) {
      super(i, j, k, l);
      this.elements = new ArrayList();
      this.mainPositioner = Positioner.create().relative(0.5F, 0.5F);
      this.setDimensions(k, l);
   }

   public SimplePositioningWidget setDimensions(int minWidth, int minHeight) {
      return this.setMinWidth(minWidth).setMinHeight(minHeight);
   }

   public SimplePositioningWidget setMinHeight(int minWidth) {
      this.minWidth = minWidth;
      return this;
   }

   public SimplePositioningWidget setMinWidth(int minHeight) {
      this.minHeight = minHeight;
      return this;
   }

   public Positioner copyPositioner() {
      return this.mainPositioner.copy();
   }

   public Positioner getMainPositioner() {
      return this.mainPositioner;
   }

   public void refreshPositions() {
      super.refreshPositions();
      int i = this.minHeight;
      int j = this.minWidth;

      Iterator var3;
      Element lv;
      for(var3 = this.elements.iterator(); var3.hasNext(); j = Math.max(j, lv.getHeight())) {
         lv = (Element)var3.next();
         i = Math.max(i, lv.getWidth());
      }

      var3 = this.elements.iterator();

      while(var3.hasNext()) {
         lv = (Element)var3.next();
         lv.setX(this.getX(), i);
         lv.setY(this.getY(), j);
      }

      this.width = i;
      this.height = j;
   }

   public Widget add(Widget widget) {
      return this.add(widget, this.copyPositioner());
   }

   public Widget add(Widget widget, Positioner positioner) {
      this.elements.add(new Element(widget, positioner));
      return widget;
   }

   public void forEachElement(Consumer consumer) {
      this.elements.forEach((element) -> {
         consumer.accept(element.widget);
      });
   }

   public static void setPos(Widget widget, int left, int top, int right, int bottom) {
      setPos(widget, left, top, right, bottom, 0.5F, 0.5F);
   }

   public static void setPos(Widget widget, ScreenRect rect) {
      setPos(widget, rect.position().x(), rect.position().y(), rect.width(), rect.height());
   }

   public static void setPos(Widget widget, ScreenRect rect, float relativeX, float relativeY) {
      setPos(widget, rect.getLeft(), rect.getTop(), rect.width(), rect.height(), relativeX, relativeY);
   }

   public static void setPos(Widget widget, int left, int top, int right, int bottom, float relativeX, float relativeY) {
      int var10002 = widget.getWidth();
      Objects.requireNonNull(widget);
      setPos(left, right, var10002, widget::setX, relativeX);
      var10002 = widget.getHeight();
      Objects.requireNonNull(widget);
      setPos(top, bottom, var10002, widget::setY, relativeY);
   }

   public static void setPos(int low, int high, int length, Consumer setter, float relative) {
      int l = (int)MathHelper.lerp(relative, 0.0F, (float)(high - length));
      setter.accept(low + l);
   }

   @Environment(EnvType.CLIENT)
   private static class Element extends WrapperWidget.WrappedElement {
      protected Element(Widget arg, Positioner arg2) {
         super(arg, arg2);
      }
   }
}
