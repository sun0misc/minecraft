package net.minecraft.client.gui.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Divider;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class GridWidget extends WrapperWidget {
   private final List children;
   private final List grids;
   private final Positioner mainPositioner;
   private int rowSpacing;
   private int columnSpacing;

   public GridWidget() {
      this(0, 0);
   }

   public GridWidget(int x, int y) {
      super(x, y, 0, 0);
      this.children = new ArrayList();
      this.grids = new ArrayList();
      this.mainPositioner = Positioner.create();
      this.rowSpacing = 0;
      this.columnSpacing = 0;
   }

   public void refreshPositions() {
      super.refreshPositions();
      int i = 0;
      int j = 0;

      Element lv;
      for(Iterator var3 = this.grids.iterator(); var3.hasNext(); j = Math.max(lv.getColumnEnd(), j)) {
         lv = (Element)var3.next();
         i = Math.max(lv.getRowEnd(), i);
      }

      int[] is = new int[j + 1];
      int[] js = new int[i + 1];
      Iterator var5 = this.grids.iterator();

      int k;
      int l;
      int m;
      while(var5.hasNext()) {
         Element lv2 = (Element)var5.next();
         k = lv2.getHeight() - (lv2.occupiedRows - 1) * this.rowSpacing;
         Divider lv3 = new Divider(k, lv2.occupiedRows);

         for(l = lv2.row; l <= lv2.getRowEnd(); ++l) {
            js[l] = Math.max(js[l], lv3.nextInt());
         }

         l = lv2.getWidth() - (lv2.occupiedColumns - 1) * this.columnSpacing;
         Divider lv4 = new Divider(l, lv2.occupiedColumns);

         for(m = lv2.column; m <= lv2.getColumnEnd(); ++m) {
            is[m] = Math.max(is[m], lv4.nextInt());
         }
      }

      int[] ks = new int[j + 1];
      int[] ls = new int[i + 1];
      ks[0] = 0;

      for(k = 1; k <= j; ++k) {
         ks[k] = ks[k - 1] + is[k - 1] + this.columnSpacing;
      }

      ls[0] = 0;

      for(k = 1; k <= i; ++k) {
         ls[k] = ls[k - 1] + js[k - 1] + this.rowSpacing;
      }

      Iterator var17 = this.grids.iterator();

      while(var17.hasNext()) {
         Element lv5 = (Element)var17.next();
         l = 0;

         int n;
         for(n = lv5.column; n <= lv5.getColumnEnd(); ++n) {
            l += is[n];
         }

         l += this.columnSpacing * (lv5.occupiedColumns - 1);
         lv5.setX(this.getX() + ks[lv5.column], l);
         n = 0;

         for(m = lv5.row; m <= lv5.getRowEnd(); ++m) {
            n += js[m];
         }

         n += this.rowSpacing * (lv5.occupiedRows - 1);
         lv5.setY(this.getY() + ls[lv5.row], n);
      }

      this.width = ks[j] + is[j];
      this.height = ls[i] + js[i];
   }

   public Widget add(Widget widget, int row, int column) {
      return this.add(widget, row, column, this.copyPositioner());
   }

   public Widget add(Widget widget, int row, int column, Positioner positioner) {
      return this.add(widget, row, column, 1, 1, positioner);
   }

   public Widget add(Widget widget, int row, int column, int occupiedRows, int occupiedColumns) {
      return this.add(widget, row, column, occupiedRows, occupiedColumns, this.copyPositioner());
   }

   public Widget add(Widget widget, int row, int column, int occupiedRows, int occupiedColumns, Positioner positioner) {
      if (occupiedRows < 1) {
         throw new IllegalArgumentException("Occupied rows must be at least 1");
      } else if (occupiedColumns < 1) {
         throw new IllegalArgumentException("Occupied columns must be at least 1");
      } else {
         this.grids.add(new Element(widget, row, column, occupiedRows, occupiedColumns, positioner));
         this.children.add(widget);
         return widget;
      }
   }

   public GridWidget setColumnSpacing(int columnSpacing) {
      this.columnSpacing = columnSpacing;
      return this;
   }

   public GridWidget setRowSpacing(int rowSpacing) {
      this.rowSpacing = rowSpacing;
      return this;
   }

   public GridWidget setSpacing(int spacing) {
      return this.setColumnSpacing(spacing).setRowSpacing(spacing);
   }

   public void forEachElement(Consumer consumer) {
      this.children.forEach(consumer);
   }

   public Positioner copyPositioner() {
      return this.mainPositioner.copy();
   }

   public Positioner getMainPositioner() {
      return this.mainPositioner;
   }

   public Adder createAdder(int columns) {
      return new Adder(columns);
   }

   @Environment(EnvType.CLIENT)
   static class Element extends WrapperWidget.WrappedElement {
      final int row;
      final int column;
      final int occupiedRows;
      final int occupiedColumns;

      Element(Widget widget, int row, int column, int occupiedRows, int occupiedColumns, Positioner positioner) {
         super(widget, positioner.toImpl());
         this.row = row;
         this.column = column;
         this.occupiedRows = occupiedRows;
         this.occupiedColumns = occupiedColumns;
      }

      public int getRowEnd() {
         return this.row + this.occupiedRows - 1;
      }

      public int getColumnEnd() {
         return this.column + this.occupiedColumns - 1;
      }
   }

   @Environment(EnvType.CLIENT)
   public final class Adder {
      private final int columns;
      private int totalOccupiedColumns;

      Adder(int columns) {
         this.columns = columns;
      }

      public Widget add(Widget widget) {
         return this.add(widget, 1);
      }

      public Widget add(Widget widget, int occupiedColumns) {
         return this.add(widget, occupiedColumns, this.getMainPositioner());
      }

      public Widget add(Widget widget, Positioner positioner) {
         return this.add(widget, 1, positioner);
      }

      public Widget add(Widget widget, int occupiedColumns, Positioner positioner) {
         int j = this.totalOccupiedColumns / this.columns;
         int k = this.totalOccupiedColumns % this.columns;
         if (k + occupiedColumns > this.columns) {
            ++j;
            k = 0;
            this.totalOccupiedColumns = MathHelper.roundUpToMultiple(this.totalOccupiedColumns, this.columns);
         }

         this.totalOccupiedColumns += occupiedColumns;
         return GridWidget.this.add(widget, j, k, 1, occupiedColumns, positioner);
      }

      public GridWidget getGridWidget() {
         return GridWidget.this;
      }

      public Positioner copyPositioner() {
         return GridWidget.this.copyPositioner();
      }

      public Positioner getMainPositioner() {
         return GridWidget.this.getMainPositioner();
      }
   }
}
