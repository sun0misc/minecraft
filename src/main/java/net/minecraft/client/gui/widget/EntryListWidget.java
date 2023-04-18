package net.minecraft.client.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class EntryListWidget extends AbstractParentElement implements Drawable, Selectable {
   protected final MinecraftClient client;
   protected final int itemHeight;
   private final List children = new Entries();
   protected int width;
   protected int height;
   protected int top;
   protected int bottom;
   protected int right;
   protected int left;
   protected boolean centerListVertically = true;
   private double scrollAmount;
   private boolean renderSelection = true;
   private boolean renderHeader;
   protected int headerHeight;
   private boolean scrolling;
   @Nullable
   private Entry selected;
   private boolean renderBackground = true;
   private boolean renderHorizontalShadows = true;
   @Nullable
   private Entry hoveredEntry;

   public EntryListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
      this.client = client;
      this.width = width;
      this.height = height;
      this.top = top;
      this.bottom = bottom;
      this.itemHeight = itemHeight;
      this.left = 0;
      this.right = width;
   }

   public void setRenderSelection(boolean renderSelection) {
      this.renderSelection = renderSelection;
   }

   protected void setRenderHeader(boolean renderHeader, int headerHeight) {
      this.renderHeader = renderHeader;
      this.headerHeight = headerHeight;
      if (!renderHeader) {
         this.headerHeight = 0;
      }

   }

   public int getRowWidth() {
      return 220;
   }

   @Nullable
   public Entry getSelectedOrNull() {
      return this.selected;
   }

   public void setSelected(@Nullable Entry entry) {
      this.selected = entry;
   }

   public Entry getFirst() {
      return (Entry)this.children.get(0);
   }

   public void setRenderBackground(boolean renderBackground) {
      this.renderBackground = renderBackground;
   }

   public void setRenderHorizontalShadows(boolean renderHorizontalShadows) {
      this.renderHorizontalShadows = renderHorizontalShadows;
   }

   @Nullable
   public Entry getFocused() {
      return (Entry)super.getFocused();
   }

   public final List children() {
      return this.children;
   }

   protected final void clearEntries() {
      this.children.clear();
      this.selected = null;
   }

   protected void replaceEntries(Collection newEntries) {
      this.clearEntries();
      this.children.addAll(newEntries);
   }

   protected Entry getEntry(int index) {
      return (Entry)this.children().get(index);
   }

   protected int addEntry(Entry entry) {
      this.children.add(entry);
      return this.children.size() - 1;
   }

   protected void addEntryToTop(Entry entry) {
      double d = (double)this.getMaxScroll() - this.getScrollAmount();
      this.children.add(0, entry);
      this.setScrollAmount((double)this.getMaxScroll() - d);
   }

   protected boolean removeEntryWithoutScrolling(Entry entry) {
      double d = (double)this.getMaxScroll() - this.getScrollAmount();
      boolean bl = this.removeEntry(entry);
      this.setScrollAmount((double)this.getMaxScroll() - d);
      return bl;
   }

   protected int getEntryCount() {
      return this.children().size();
   }

   protected boolean isSelectedEntry(int index) {
      return Objects.equals(this.getSelectedOrNull(), this.children().get(index));
   }

   @Nullable
   protected final Entry getEntryAtPosition(double x, double y) {
      int i = this.getRowWidth() / 2;
      int j = this.left + this.width / 2;
      int k = j - i;
      int l = j + i;
      int m = MathHelper.floor(y - (double)this.top) - this.headerHeight + (int)this.getScrollAmount() - 4;
      int n = m / this.itemHeight;
      return x < (double)this.getScrollbarPositionX() && x >= (double)k && x <= (double)l && n >= 0 && m >= 0 && n < this.getEntryCount() ? (Entry)this.children().get(n) : null;
   }

   public void updateSize(int width, int height, int top, int bottom) {
      this.width = width;
      this.height = height;
      this.top = top;
      this.bottom = bottom;
      this.left = 0;
      this.right = width;
   }

   public void setLeftPos(int left) {
      this.left = left;
      this.right = left + this.width;
   }

   protected int getMaxPosition() {
      return this.getEntryCount() * this.itemHeight + this.headerHeight;
   }

   protected void clickedHeader(int x, int y) {
   }

   protected void renderHeader(MatrixStack matrices, int x, int y) {
   }

   protected void renderBackground(MatrixStack matrices) {
   }

   protected void renderDecorations(MatrixStack matrices, int mouseX, int mouseY) {
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      int k = this.getScrollbarPositionX();
      int l = k + 6;
      this.hoveredEntry = this.isMouseOver((double)mouseX, (double)mouseY) ? this.getEntryAtPosition((double)mouseX, (double)mouseY) : null;
      if (this.renderBackground) {
         RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
         RenderSystem.setShaderColor(0.125F, 0.125F, 0.125F, 1.0F);
         int m = true;
         drawTexture(matrices, this.left, this.top, (float)this.right, (float)(this.bottom + (int)this.getScrollAmount()), this.right - this.left, this.bottom - this.top, 32, 32);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      int m = this.getRowLeft();
      int n = this.top + 4 - (int)this.getScrollAmount();
      this.enableScissor();
      if (this.renderHeader) {
         this.renderHeader(matrices, m, n);
      }

      this.renderList(matrices, mouseX, mouseY, delta);
      disableScissor();
      if (this.renderHorizontalShadows) {
         RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
         int o = true;
         RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
         drawTexture(matrices, this.left, 0, 0.0F, 0.0F, this.width, this.top, 32, 32);
         drawTexture(matrices, this.left, this.bottom, 0.0F, (float)this.bottom, this.width, this.height - this.bottom, 32, 32);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         int p = true;
         fillGradient(matrices, this.left, this.top, this.right, this.top + 4, -16777216, 0);
         fillGradient(matrices, this.left, this.bottom - 4, this.right, this.bottom, 0, -16777216);
      }

      int o = this.getMaxScroll();
      if (o > 0) {
         int p = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
         p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
         int q = (int)this.getScrollAmount() * (this.bottom - this.top - p) / o + this.top;
         if (q < this.top) {
            q = this.top;
         }

         fill(matrices, k, this.top, l, this.bottom, -16777216);
         fill(matrices, k, q, l, q + p, -8355712);
         fill(matrices, k, q, l - 1, q + p - 1, -4144960);
      }

      this.renderDecorations(matrices, mouseX, mouseY);
      RenderSystem.disableBlend();
   }

   protected void enableScissor() {
      enableScissor(this.left, this.top, this.right, this.bottom);
   }

   protected void centerScrollOn(Entry entry) {
      this.setScrollAmount((double)(this.children().indexOf(entry) * this.itemHeight + this.itemHeight / 2 - (this.bottom - this.top) / 2));
   }

   protected void ensureVisible(Entry entry) {
      int i = this.getRowTop(this.children().indexOf(entry));
      int j = i - this.top - 4 - this.itemHeight;
      if (j < 0) {
         this.scroll(j);
      }

      int k = this.bottom - i - this.itemHeight - this.itemHeight;
      if (k < 0) {
         this.scroll(-k);
      }

   }

   private void scroll(int amount) {
      this.setScrollAmount(this.getScrollAmount() + (double)amount);
   }

   public double getScrollAmount() {
      return this.scrollAmount;
   }

   public void setScrollAmount(double amount) {
      this.scrollAmount = MathHelper.clamp(amount, 0.0, (double)this.getMaxScroll());
   }

   public int getMaxScroll() {
      return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
   }

   public int method_35721() {
      return (int)this.getScrollAmount() - this.height - this.headerHeight;
   }

   protected void updateScrollingState(double mouseX, double mouseY, int button) {
      this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarPositionX() && mouseX < (double)(this.getScrollbarPositionX() + 6);
   }

   protected int getScrollbarPositionX() {
      return this.width / 2 + 124;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.updateScrollingState(mouseX, mouseY, button);
      if (!this.isMouseOver(mouseX, mouseY)) {
         return false;
      } else {
         Entry lv = this.getEntryAtPosition(mouseX, mouseY);
         if (lv != null) {
            if (lv.mouseClicked(mouseX, mouseY, button)) {
               Entry lv2 = this.getFocused();
               if (lv2 != lv && lv2 instanceof ParentElement) {
                  ParentElement lv3 = (ParentElement)lv2;
                  lv3.setFocused((Element)null);
               }

               this.setFocused(lv);
               this.setDragging(true);
               return true;
            }
         } else if (button == 0) {
            this.clickedHeader((int)(mouseX - (double)(this.left + this.width / 2 - this.getRowWidth() / 2)), (int)(mouseY - (double)this.top) + (int)this.getScrollAmount() - 4);
            return true;
         }

         return this.scrolling;
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.getFocused() != null) {
         this.getFocused().mouseReleased(mouseX, mouseY, button);
      }

      return false;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      } else if (button == 0 && this.scrolling) {
         if (mouseY < (double)this.top) {
            this.setScrollAmount(0.0);
         } else if (mouseY > (double)this.bottom) {
            this.setScrollAmount((double)this.getMaxScroll());
         } else {
            double h = (double)Math.max(1, this.getMaxScroll());
            int j = this.bottom - this.top;
            int k = MathHelper.clamp((int)((float)(j * j) / (float)this.getMaxPosition()), 32, j - 8);
            double l = Math.max(1.0, h / (double)(j - k));
            this.setScrollAmount(this.getScrollAmount() + deltaY * l);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      this.setScrollAmount(this.getScrollAmount() - amount * (double)this.itemHeight / 2.0);
      return true;
   }

   public void setFocused(@Nullable Element focused) {
      super.setFocused(focused);
      int i = this.children.indexOf(focused);
      if (i >= 0) {
         Entry lv = (Entry)this.children.get(i);
         this.setSelected(lv);
         if (this.client.getNavigationType().isKeyboard()) {
            this.ensureVisible(lv);
         }
      }

   }

   @Nullable
   protected Entry getNeighboringEntry(NavigationDirection direction) {
      return this.getNeighboringEntry(direction, (entry) -> {
         return true;
      });
   }

   @Nullable
   protected Entry getNeighboringEntry(NavigationDirection direction, Predicate predicate) {
      return this.getNeighboringEntry(direction, predicate, this.getSelectedOrNull());
   }

   @Nullable
   protected Entry getNeighboringEntry(NavigationDirection direction, Predicate predicate, @Nullable Entry selected) {
      byte var10000;
      switch (direction) {
         case RIGHT:
         case LEFT:
            var10000 = 0;
            break;
         case UP:
            var10000 = -1;
            break;
         case DOWN:
            var10000 = 1;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      int i = var10000;
      if (!this.children().isEmpty() && i != 0) {
         int j;
         if (selected == null) {
            j = i > 0 ? 0 : this.children().size() - 1;
         } else {
            j = this.children().indexOf(selected) + i;
         }

         for(int k = j; k >= 0 && k < this.children.size(); k += i) {
            Entry lv = (Entry)this.children().get(k);
            if (predicate.test(lv)) {
               return lv;
            }
         }
      }

      return null;
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return mouseY >= (double)this.top && mouseY <= (double)this.bottom && mouseX >= (double)this.left && mouseX <= (double)this.right;
   }

   protected void renderList(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      int k = this.getRowLeft();
      int l = this.getRowWidth();
      int m = this.itemHeight - 4;
      int n = this.getEntryCount();

      for(int o = 0; o < n; ++o) {
         int p = this.getRowTop(o);
         int q = this.getRowBottom(o);
         if (q >= this.top && p <= this.bottom) {
            this.renderEntry(matrices, mouseX, mouseY, delta, o, k, p, l, m);
         }
      }

   }

   protected void renderEntry(MatrixStack matrices, int mouseX, int mouseY, float delta, int index, int x, int y, int entryWidth, int entryHeight) {
      Entry lv = this.getEntry(index);
      lv.drawBorder(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, Objects.equals(this.hoveredEntry, lv), delta);
      if (this.renderSelection && this.isSelectedEntry(index)) {
         int p = this.isFocused() ? -1 : -8355712;
         this.drawSelectionHighlight(matrices, y, entryWidth, entryHeight, p, -16777216);
      }

      lv.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, Objects.equals(this.hoveredEntry, lv), delta);
   }

   protected void drawSelectionHighlight(MatrixStack matrices, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
      int n = this.left + (this.width - entryWidth) / 2;
      int o = this.left + (this.width + entryWidth) / 2;
      fill(matrices, n, y - 2, o, y + entryHeight + 2, borderColor);
      fill(matrices, n + 1, y - 1, o - 1, y + entryHeight + 1, fillColor);
   }

   public int getRowLeft() {
      return this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
   }

   public int getRowRight() {
      return this.getRowLeft() + this.getRowWidth();
   }

   protected int getRowTop(int index) {
      return this.top + 4 - (int)this.getScrollAmount() + index * this.itemHeight + this.headerHeight;
   }

   protected int getRowBottom(int index) {
      return this.getRowTop(index) + this.itemHeight;
   }

   public Selectable.SelectionType getType() {
      if (this.isFocused()) {
         return Selectable.SelectionType.FOCUSED;
      } else {
         return this.hoveredEntry != null ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
      }
   }

   @Nullable
   protected Entry remove(int index) {
      Entry lv = (Entry)this.children.get(index);
      return this.removeEntry((Entry)this.children.get(index)) ? lv : null;
   }

   protected boolean removeEntry(Entry entry) {
      boolean bl = this.children.remove(entry);
      if (bl && entry == this.getSelectedOrNull()) {
         this.setSelected((Entry)null);
      }

      return bl;
   }

   @Nullable
   protected Entry getHoveredEntry() {
      return this.hoveredEntry;
   }

   void setEntryParentList(Entry entry) {
      entry.parentList = this;
   }

   protected void appendNarrations(NarrationMessageBuilder builder, Entry entry) {
      List list = this.children();
      if (list.size() > 1) {
         int i = list.indexOf(entry);
         if (i != -1) {
            builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.list", i + 1, list.size()));
         }
      }

   }

   public ScreenRect getNavigationFocus() {
      return new ScreenRect(this.left, this.top, this.right - this.left, this.bottom - this.top);
   }

   // $FF: synthetic method
   @Nullable
   public Element getFocused() {
      return this.getFocused();
   }

   @Environment(EnvType.CLIENT)
   private class Entries extends AbstractList {
      private final List entries = Lists.newArrayList();

      Entries() {
      }

      public Entry get(int i) {
         return (Entry)this.entries.get(i);
      }

      public int size() {
         return this.entries.size();
      }

      public Entry set(int i, Entry arg) {
         Entry lv = (Entry)this.entries.set(i, arg);
         EntryListWidget.this.setEntryParentList(arg);
         return lv;
      }

      public void add(int i, Entry arg) {
         this.entries.add(i, arg);
         EntryListWidget.this.setEntryParentList(arg);
      }

      public Entry remove(int i) {
         return (Entry)this.entries.remove(i);
      }

      // $FF: synthetic method
      public Object remove(int index) {
         return this.remove(index);
      }

      // $FF: synthetic method
      public void add(int index, Object entry) {
         this.add(index, (Entry)entry);
      }

      // $FF: synthetic method
      public Object set(int index, Object entry) {
         return this.set(index, (Entry)entry);
      }

      // $FF: synthetic method
      public Object get(int index) {
         return this.get(index);
      }
   }

   @Environment(EnvType.CLIENT)
   protected abstract static class Entry implements Element {
      /** @deprecated */
      @Deprecated
      EntryListWidget parentList;

      public void setFocused(boolean focused) {
      }

      public boolean isFocused() {
         return this.parentList.getFocused() == this;
      }

      public abstract void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);

      public void drawBorder(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
      }

      public boolean isMouseOver(double mouseX, double mouseY) {
         return Objects.equals(this.parentList.getEntryAtPosition(mouseX, mouseY), this);
      }
   }
}
