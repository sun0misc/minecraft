package net.minecraft.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TabNavigationWidget extends AbstractParentElement implements Drawable, Element, Selectable {
   private static final int field_42489 = -1;
   private static final int field_43076 = 400;
   private static final int field_43077 = 24;
   private static final int field_43078 = 14;
   private static final Text USAGE_NARRATION_TEXT = Text.translatable("narration.tab_navigation.usage");
   private final GridWidget grid;
   private int tabNavWidth;
   private final TabManager tabManager;
   private final ImmutableList tabs;
   private final ImmutableList tabButtons;

   TabNavigationWidget(int x, TabManager tabManager, Iterable tabs) {
      this.tabNavWidth = x;
      this.tabManager = tabManager;
      this.tabs = ImmutableList.copyOf(tabs);
      this.grid = new GridWidget(0, 0);
      this.grid.getMainPositioner().alignHorizontalCenter();
      ImmutableList.Builder builder = ImmutableList.builder();
      int j = 0;
      Iterator var6 = tabs.iterator();

      while(var6.hasNext()) {
         Tab lv = (Tab)var6.next();
         builder.add((TabButtonWidget)this.grid.add(new TabButtonWidget(tabManager, lv, 0, 24), 0, j++));
      }

      this.tabButtons = builder.build();
   }

   public static Builder builder(TabManager tabManager, int width) {
      return new Builder(tabManager, width);
   }

   public void setWidth(int width) {
      this.tabNavWidth = width;
   }

   public void setFocused(boolean focused) {
      super.setFocused(focused);
      if (this.getFocused() != null) {
         this.getFocused().setFocused(focused);
      }

   }

   public void setFocused(@Nullable Element focused) {
      super.setFocused(focused);
      if (focused instanceof TabButtonWidget lv) {
         this.tabManager.setCurrentTab(lv.getTab(), true);
      }

   }

   public @Nullable GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
      if (!this.isFocused()) {
         TabButtonWidget lv = this.getCurrentTabButton();
         if (lv != null) {
            return GuiNavigationPath.of((ParentElement)this, (GuiNavigationPath)GuiNavigationPath.of(lv));
         }
      }

      return navigation instanceof GuiNavigation.Tab ? null : super.getNavigationPath(navigation);
   }

   public List children() {
      return this.tabButtons;
   }

   public Selectable.SelectionType getType() {
      return (Selectable.SelectionType)this.tabButtons.stream().map(ClickableWidget::getType).max(Comparator.naturalOrder()).orElse(Selectable.SelectionType.NONE);
   }

   public void appendNarrations(NarrationMessageBuilder builder) {
      Optional optional = this.tabButtons.stream().filter(ClickableWidget::isHovered).findFirst().or(() -> {
         return Optional.ofNullable(this.getCurrentTabButton());
      });
      optional.ifPresent((button) -> {
         this.appendNarrations(builder.nextMessage(), button);
         button.appendNarrations(builder);
      });
      if (this.isFocused()) {
         builder.put(NarrationPart.USAGE, USAGE_NARRATION_TEXT);
      }

   }

   protected void appendNarrations(NarrationMessageBuilder builder, TabButtonWidget button) {
      if (this.tabs.size() > 1) {
         int i = this.tabButtons.indexOf(button);
         if (i != -1) {
            builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.tab", i + 1, this.tabs.size()));
         }
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      fill(matrices, 0, 0, this.tabNavWidth, 24, -16777216);
      RenderSystem.setShaderTexture(0, CreateWorldScreen.HEADER_SEPARATOR_TEXTURE);
      drawTexture(matrices, 0, this.grid.getY() + this.grid.getHeight() - 2, 0.0F, 0.0F, this.tabNavWidth, 2, 32, 2);
      UnmodifiableIterator var5 = this.tabButtons.iterator();

      while(var5.hasNext()) {
         TabButtonWidget lv = (TabButtonWidget)var5.next();
         lv.render(matrices, mouseX, mouseY, delta);
      }

   }

   public ScreenRect getNavigationFocus() {
      return this.grid.getNavigationFocus();
   }

   public void init() {
      int i = Math.min(400, this.tabNavWidth) - 28;
      int j = MathHelper.roundUpToMultiple(i / this.tabs.size(), 2);
      UnmodifiableIterator var3 = this.tabButtons.iterator();

      while(var3.hasNext()) {
         TabButtonWidget lv = (TabButtonWidget)var3.next();
         lv.setWidth(j);
      }

      this.grid.refreshPositions();
      this.grid.setX(MathHelper.roundUpToMultiple((this.tabNavWidth - i) / 2, 2));
      this.grid.setY(0);
   }

   public void selectTab(int index, boolean clickSound) {
      if (this.isFocused()) {
         this.setFocused((Element)this.tabButtons.get(index));
      } else {
         this.tabManager.setCurrentTab((Tab)this.tabs.get(index), clickSound);
      }

   }

   public boolean trySwitchTabsWithKey(int keyCode) {
      if (Screen.hasControlDown()) {
         int j = this.getTabForKey(keyCode);
         if (j != -1) {
            this.selectTab(MathHelper.clamp(j, 0, this.tabs.size() - 1), true);
            return true;
         }
      }

      return false;
   }

   private int getTabForKey(int keyCode) {
      if (keyCode >= 49 && keyCode <= 57) {
         return keyCode - 49;
      } else {
         if (keyCode == 258) {
            int j = this.getCurrentTabIndex();
            if (j != -1) {
               int k = Screen.hasShiftDown() ? j - 1 : j + 1;
               return Math.floorMod(k, this.tabs.size());
            }
         }

         return -1;
      }
   }

   private int getCurrentTabIndex() {
      Tab lv = this.tabManager.getCurrentTab();
      int i = this.tabs.indexOf(lv);
      return i != -1 ? i : -1;
   }

   private @Nullable TabButtonWidget getCurrentTabButton() {
      int i = this.getCurrentTabIndex();
      return i != -1 ? (TabButtonWidget)this.tabButtons.get(i) : null;
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      private final int width;
      private final TabManager tabManager;
      private final List tabs = new ArrayList();

      Builder(TabManager tabManager, int width) {
         this.tabManager = tabManager;
         this.width = width;
      }

      public Builder tabs(Tab... tabs) {
         Collections.addAll(this.tabs, tabs);
         return this;
      }

      public TabNavigationWidget build() {
         return new TabNavigationWidget(this.width, this.tabManager, this.tabs);
      }
   }
}
