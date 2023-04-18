package net.minecraft.client.realms.gui.screen;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public abstract class RealmsAcceptRejectButton {
   public final int width;
   public final int height;
   public final int x;
   public final int y;

   public RealmsAcceptRejectButton(int width, int height, int x, int y) {
      this.width = width;
      this.height = height;
      this.x = x;
      this.y = y;
   }

   public void render(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
      int m = x + this.x;
      int n = y + this.y;
      boolean bl = mouseX >= m && mouseX <= m + this.width && mouseY >= n && mouseY <= n + this.height;
      this.render(matrices, m, n, bl);
   }

   protected abstract void render(MatrixStack matrices, int x, int y, boolean showTooltip);

   public int getRight() {
      return this.x + this.width;
   }

   public int getBottom() {
      return this.y + this.height;
   }

   public abstract void handleClick(int index);

   public static void render(MatrixStack matrices, List buttons, RealmsObjectSelectionList selectionList, int x, int y, int mouseX, int mouseY) {
      Iterator var7 = buttons.iterator();

      while(var7.hasNext()) {
         RealmsAcceptRejectButton lv = (RealmsAcceptRejectButton)var7.next();
         if (selectionList.getRowWidth() > lv.getRight()) {
            lv.render(matrices, x, y, mouseX, mouseY);
         }
      }

   }

   public static void handleClick(RealmsObjectSelectionList selectionList, AlwaysSelectedEntryListWidget.Entry entry, List buttons, int button, double mouseX, double mouseY) {
      if (button == 0) {
         int j = selectionList.children().indexOf(entry);
         if (j > -1) {
            selectionList.setSelected(j);
            int k = selectionList.getRowLeft();
            int l = selectionList.getRowTop(j);
            int m = (int)(mouseX - (double)k);
            int n = (int)(mouseY - (double)l);
            Iterator var13 = buttons.iterator();

            while(var13.hasNext()) {
               RealmsAcceptRejectButton lv = (RealmsAcceptRejectButton)var13.next();
               if (m >= lv.x && m <= lv.getRight() && n >= lv.y && n <= lv.getBottom()) {
                  lv.handleClick(j);
               }
            }
         }
      }

   }
}
