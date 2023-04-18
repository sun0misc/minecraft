package net.minecraft.client.gui.tab;

import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TabManager {
   private final Consumer tabLoadConsumer;
   private final Consumer tabUnloadConsumer;
   @Nullable
   private Tab currentTab;
   @Nullable
   private ScreenRect tabArea;

   public TabManager(Consumer tabLoadConsumer, Consumer tabUnloadConsumer) {
      this.tabLoadConsumer = tabLoadConsumer;
      this.tabUnloadConsumer = tabUnloadConsumer;
   }

   public void setTabArea(ScreenRect tabArea) {
      this.tabArea = tabArea;
      Tab lv = this.getCurrentTab();
      if (lv != null) {
         lv.refreshGrid(tabArea);
      }

   }

   public void setCurrentTab(Tab tab, boolean clickSound) {
      if (!Objects.equals(this.currentTab, tab)) {
         if (this.currentTab != null) {
            this.currentTab.forEachChild(this.tabUnloadConsumer);
         }

         this.currentTab = tab;
         tab.forEachChild(this.tabLoadConsumer);
         if (this.tabArea != null) {
            tab.refreshGrid(this.tabArea);
         }

         if (clickSound) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }
      }

   }

   @Nullable
   public Tab getCurrentTab() {
      return this.currentTab;
   }

   public void tick() {
      Tab lv = this.getCurrentTab();
      if (lv != null) {
         lv.tick();
      }

   }
}
