package net.minecraft.client.realms;

import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;

@Environment(EnvType.CLIENT)
public abstract class RealmsObjectSelectionList extends AlwaysSelectedEntryListWidget {
   protected RealmsObjectSelectionList(int width, int height, int top, int bottom, int itemHeight) {
      super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
   }

   public void setSelectedItem(int index) {
      if (index == -1) {
         this.setSelected((EntryListWidget.Entry)null);
      } else if (super.getEntryCount() != 0) {
         this.setSelected((AlwaysSelectedEntryListWidget.Entry)this.getEntry(index));
      }

   }

   public void setSelected(int index) {
      this.setSelectedItem(index);
   }

   public void itemClicked(int cursorY, int selectionIndex, double mouseX, double mouseY, int listWidth, int l) {
   }

   public int getMaxPosition() {
      return 0;
   }

   public int getScrollbarPositionX() {
      return this.getRowLeft() + this.getRowWidth();
   }

   public int getRowWidth() {
      return (int)((double)this.width * 0.6);
   }

   public void replaceEntries(Collection newEntries) {
      super.replaceEntries(newEntries);
   }

   public int getEntryCount() {
      return super.getEntryCount();
   }

   public int getRowTop(int index) {
      return super.getRowTop(index);
   }

   public int getRowLeft() {
      return super.getRowLeft();
   }

   public int addEntry(AlwaysSelectedEntryListWidget.Entry arg) {
      return super.addEntry(arg);
   }

   public void clear() {
      this.clearEntries();
   }

   // $FF: synthetic method
   public int addEntry(EntryListWidget.Entry entry) {
      return this.addEntry((AlwaysSelectedEntryListWidget.Entry)entry);
   }
}
