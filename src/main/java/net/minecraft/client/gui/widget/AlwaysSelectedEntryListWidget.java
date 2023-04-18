package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Narratable;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class AlwaysSelectedEntryListWidget extends EntryListWidget {
   private static final Text SELECTION_USAGE_TEXT = Text.translatable("narration.selection.usage");

   public AlwaysSelectedEntryListWidget(MinecraftClient arg, int i, int j, int k, int l, int m) {
      super(arg, i, j, k, l, m);
   }

   @Nullable
   public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
      if (this.getEntryCount() == 0) {
         return null;
      } else if (this.isFocused() && navigation instanceof GuiNavigation.Arrow) {
         GuiNavigation.Arrow lv = (GuiNavigation.Arrow)navigation;
         Entry lv2 = (Entry)this.getNeighboringEntry(lv.direction());
         return lv2 != null ? GuiNavigationPath.of((ParentElement)this, (GuiNavigationPath)GuiNavigationPath.of(lv2)) : null;
      } else if (!this.isFocused()) {
         Entry lv3 = (Entry)this.getSelectedOrNull();
         if (lv3 == null) {
            lv3 = (Entry)this.getNeighboringEntry(navigation.getDirection());
         }

         return lv3 == null ? null : GuiNavigationPath.of((ParentElement)this, (GuiNavigationPath)GuiNavigationPath.of(lv3));
      } else {
         return null;
      }
   }

   public void appendNarrations(NarrationMessageBuilder builder) {
      Entry lv = (Entry)this.getHoveredEntry();
      if (lv != null) {
         this.appendNarrations(builder.nextMessage(), lv);
         lv.appendNarrations(builder);
      } else {
         Entry lv2 = (Entry)this.getSelectedOrNull();
         if (lv2 != null) {
            this.appendNarrations(builder.nextMessage(), lv2);
            lv2.appendNarrations(builder);
         }
      }

      if (this.isFocused()) {
         builder.put(NarrationPart.USAGE, SELECTION_USAGE_TEXT);
      }

   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends EntryListWidget.Entry implements Narratable {
      public abstract Text getNarration();

      public void appendNarrations(NarrationMessageBuilder builder) {
         builder.put(NarrationPart.TITLE, this.getNarration());
      }
   }
}
