package net.minecraft.client.gui.widget;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class ElementListWidget extends EntryListWidget {
   public ElementListWidget(MinecraftClient arg, int i, int j, int k, int l, int m) {
      super(arg, i, j, k, l, m);
   }

   @Nullable
   public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
      if (this.getEntryCount() == 0) {
         return null;
      } else if (!(navigation instanceof GuiNavigation.Arrow)) {
         return super.getNavigationPath(navigation);
      } else {
         GuiNavigation.Arrow lv = (GuiNavigation.Arrow)navigation;
         Entry lv2 = (Entry)this.getFocused();
         if (lv.direction().getAxis() == NavigationAxis.HORIZONTAL && lv2 != null) {
            return GuiNavigationPath.of((ParentElement)this, (GuiNavigationPath)lv2.getNavigationPath(navigation));
         } else {
            int i = -1;
            NavigationDirection lv3 = lv.direction();
            if (lv2 != null) {
               i = lv2.children().indexOf(lv2.getFocused());
            }

            if (i == -1) {
               switch (lv3) {
                  case LEFT:
                     i = Integer.MAX_VALUE;
                     lv3 = NavigationDirection.DOWN;
                     break;
                  case RIGHT:
                     i = 0;
                     lv3 = NavigationDirection.DOWN;
                     break;
                  default:
                     i = 0;
               }
            }

            Entry lv4 = lv2;

            GuiNavigationPath lv5;
            do {
               lv4 = (Entry)this.getNeighboringEntry(lv3, (element) -> {
                  return !element.children().isEmpty();
               }, lv4);
               if (lv4 == null) {
                  return null;
               }

               lv5 = lv4.getNavigationPath(lv, i);
            } while(lv5 == null);

            return GuiNavigationPath.of((ParentElement)this, (GuiNavigationPath)lv5);
         }
      }
   }

   public void setFocused(@Nullable Element focused) {
      super.setFocused(focused);
      if (focused == null) {
         this.setSelected((EntryListWidget.Entry)null);
      }

   }

   public Selectable.SelectionType getType() {
      return this.isFocused() ? Selectable.SelectionType.FOCUSED : super.getType();
   }

   protected boolean isSelectedEntry(int index) {
      return false;
   }

   public void appendNarrations(NarrationMessageBuilder builder) {
      Entry lv = (Entry)this.getHoveredEntry();
      if (lv != null) {
         lv.appendNarrations(builder.nextMessage());
         this.appendNarrations(builder, lv);
      } else {
         Entry lv2 = (Entry)this.getFocused();
         if (lv2 != null) {
            lv2.appendNarrations(builder.nextMessage());
            this.appendNarrations(builder, lv2);
         }
      }

      builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.component_list.usage"));
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends EntryListWidget.Entry implements ParentElement {
      @Nullable
      private Element focused;
      @Nullable
      private Selectable focusedSelectable;
      private boolean dragging;

      public boolean isDragging() {
         return this.dragging;
      }

      public void setDragging(boolean dragging) {
         this.dragging = dragging;
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         return ParentElement.super.mouseClicked(mouseX, mouseY, button);
      }

      public void setFocused(@Nullable Element focused) {
         if (this.focused != null) {
            this.focused.setFocused(false);
         }

         if (focused != null) {
            focused.setFocused(true);
         }

         this.focused = focused;
      }

      @Nullable
      public Element getFocused() {
         return this.focused;
      }

      @Nullable
      public GuiNavigationPath getNavigationPath(GuiNavigation navigation, int index) {
         if (this.children().isEmpty()) {
            return null;
         } else {
            GuiNavigationPath lv = ((Element)this.children().get(Math.min(index, this.children().size() - 1))).getNavigationPath(navigation);
            return GuiNavigationPath.of((ParentElement)this, (GuiNavigationPath)lv);
         }
      }

      @Nullable
      public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
         if (navigation instanceof GuiNavigation.Arrow) {
            GuiNavigation.Arrow lv = (GuiNavigation.Arrow)navigation;
            byte var10000;
            switch (lv.direction()) {
               case LEFT:
                  var10000 = -1;
                  break;
               case RIGHT:
                  var10000 = 1;
                  break;
               case UP:
               case DOWN:
                  var10000 = 0;
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            int i = var10000;
            if (i == 0) {
               return null;
            }

            int j = MathHelper.clamp(i + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1);

            for(int k = j; k >= 0 && k < this.children().size(); k += i) {
               Element lv2 = (Element)this.children().get(k);
               GuiNavigationPath lv3 = lv2.getNavigationPath(navigation);
               if (lv3 != null) {
                  return GuiNavigationPath.of((ParentElement)this, (GuiNavigationPath)lv3);
               }
            }
         }

         return ParentElement.super.getNavigationPath(navigation);
      }

      public abstract List selectableChildren();

      void appendNarrations(NarrationMessageBuilder builder) {
         List list = this.selectableChildren();
         Screen.SelectedElementNarrationData lv = Screen.findSelectedElementData(list, this.focusedSelectable);
         if (lv != null) {
            if (lv.selectType.isFocused()) {
               this.focusedSelectable = lv.selectable;
            }

            if (list.size() > 1) {
               builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.object_list", lv.index + 1, list.size()));
               if (lv.selectType == Selectable.SelectionType.FOCUSED) {
                  builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.component_list.usage"));
               }
            }

            lv.selectable.appendNarrations(builder.nextMessage());
         }

      }
   }
}
