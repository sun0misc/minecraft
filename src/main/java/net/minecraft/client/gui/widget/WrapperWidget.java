package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class WrapperWidget implements LayoutWidget {
   private int x;
   private int y;
   protected int width;
   protected int height;

   public WrapperWidget(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public void setX(int x) {
      this.forEachElement((element) -> {
         int j = element.getX() + (x - this.getX());
         element.setX(j);
      });
      this.x = x;
   }

   public void setY(int y) {
      this.forEachElement((element) -> {
         int j = element.getY() + (y - this.getY());
         element.setY(j);
      });
      this.y = y;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   @Environment(EnvType.CLIENT)
   protected abstract static class WrappedElement {
      public final Widget widget;
      public final Positioner.Impl positioner;

      protected WrappedElement(Widget widget, Positioner positioner) {
         this.widget = widget;
         this.positioner = positioner.toImpl();
      }

      public int getHeight() {
         return this.widget.getHeight() + this.positioner.marginTop + this.positioner.marginBottom;
      }

      public int getWidth() {
         return this.widget.getWidth() + this.positioner.marginLeft + this.positioner.marginRight;
      }

      public void setX(int left, int right) {
         float f = (float)this.positioner.marginLeft;
         float g = (float)(right - this.widget.getWidth() - this.positioner.marginRight);
         int k = (int)MathHelper.lerp(this.positioner.relativeX, f, g);
         this.widget.setX(k + left);
      }

      public void setY(int top, int bottom) {
         float f = (float)this.positioner.marginTop;
         float g = (float)(bottom - this.widget.getHeight() - this.positioner.marginBottom);
         int k = Math.round(MathHelper.lerp(this.positioner.relativeY, f, g));
         this.widget.setY(k + top);
      }
   }
}
