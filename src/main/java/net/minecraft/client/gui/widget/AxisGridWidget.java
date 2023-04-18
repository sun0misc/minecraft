package net.minecraft.client.gui.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Divider;

@Environment(EnvType.CLIENT)
public class AxisGridWidget extends WrapperWidget {
   private final DisplayAxis axis;
   private final List elements;
   private final Positioner mainPositioner;

   public AxisGridWidget(int width, int height, DisplayAxis axis) {
      this(0, 0, width, height, axis);
   }

   public AxisGridWidget(int x, int y, int width, int height, DisplayAxis axis) {
      super(x, y, width, height);
      this.elements = new ArrayList();
      this.mainPositioner = Positioner.create();
      this.axis = axis;
   }

   public void refreshPositions() {
      super.refreshPositions();
      if (!this.elements.isEmpty()) {
         int i = 0;
         int j = this.axis.getOtherAxisLength((Widget)this);

         Element lv;
         for(Iterator var3 = this.elements.iterator(); var3.hasNext(); j = Math.max(j, this.axis.getOtherAxisLength(lv))) {
            lv = (Element)var3.next();
            i += this.axis.getSameAxisLength(lv);
         }

         int k = this.axis.getSameAxisLength((Widget)this) - i;
         int l = this.axis.getSameAxisCoordinate(this);
         Iterator iterator = this.elements.iterator();
         Element lv2 = (Element)iterator.next();
         this.axis.setSameAxisCoordinate(lv2, l);
         l += this.axis.getSameAxisLength(lv2);
         Element lv4;
         if (this.elements.size() >= 2) {
            for(Divider lv3 = new Divider(k, this.elements.size() - 1); lv3.hasNext(); l += this.axis.getSameAxisLength(lv4)) {
               l += lv3.nextInt();
               lv4 = (Element)iterator.next();
               this.axis.setSameAxisCoordinate(lv4, l);
            }
         }

         int m = this.axis.getOtherAxisCoordinate(this);
         Iterator var13 = this.elements.iterator();

         while(var13.hasNext()) {
            Element lv5 = (Element)var13.next();
            this.axis.setOtherAxisCoordinate(lv5, m, j);
         }

         switch (this.axis) {
            case HORIZONTAL:
               this.height = j;
               break;
            case VERTICAL:
               this.width = j;
         }

      }
   }

   public void forEachElement(Consumer consumer) {
      this.elements.forEach((element) -> {
         consumer.accept(element.widget);
      });
   }

   public Positioner copyPositioner() {
      return this.mainPositioner.copy();
   }

   public Positioner getMainPositioner() {
      return this.mainPositioner;
   }

   public Widget add(Widget widget) {
      return this.add(widget, this.copyPositioner());
   }

   public Widget add(Widget widget, Positioner positioner) {
      this.elements.add(new Element(widget, positioner));
      return widget;
   }

   @Environment(EnvType.CLIENT)
   public static enum DisplayAxis {
      HORIZONTAL,
      VERTICAL;

      int getSameAxisLength(Widget widget) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = widget.getWidth();
               break;
            case VERTICAL:
               var10000 = widget.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      int getSameAxisLength(Element element) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = element.getWidth();
               break;
            case VERTICAL:
               var10000 = element.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      int getOtherAxisLength(Widget widget) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = widget.getHeight();
               break;
            case VERTICAL:
               var10000 = widget.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      int getOtherAxisLength(Element element) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = element.getHeight();
               break;
            case VERTICAL:
               var10000 = element.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      void setSameAxisCoordinate(Element element, int low) {
         switch (this) {
            case HORIZONTAL:
               element.setX(low, element.getWidth());
               break;
            case VERTICAL:
               element.setY(low, element.getHeight());
         }

      }

      void setOtherAxisCoordinate(Element element, int low, int high) {
         switch (this) {
            case HORIZONTAL:
               element.setY(low, high);
               break;
            case VERTICAL:
               element.setX(low, high);
         }

      }

      int getSameAxisCoordinate(Widget widget) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = widget.getX();
               break;
            case VERTICAL:
               var10000 = widget.getY();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      int getOtherAxisCoordinate(Widget widget) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = widget.getY();
               break;
            case VERTICAL:
               var10000 = widget.getX();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      // $FF: synthetic method
      private static DisplayAxis[] method_46501() {
         return new DisplayAxis[]{HORIZONTAL, VERTICAL};
      }
   }

   @Environment(EnvType.CLIENT)
   private static class Element extends WrapperWidget.WrappedElement {
      protected Element(Widget arg, Positioner arg2) {
         super(arg, arg2);
      }
   }
}
