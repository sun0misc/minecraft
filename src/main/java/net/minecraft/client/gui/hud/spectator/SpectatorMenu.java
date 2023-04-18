package net.minecraft.client.gui.hud.spectator;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SpectatorMenu {
   private static final SpectatorMenuCommand CLOSE_COMMAND = new CloseSpectatorMenuCommand();
   private static final SpectatorMenuCommand PREVIOUS_PAGE_COMMAND = new ChangePageSpectatorMenuCommand(-1, true);
   private static final SpectatorMenuCommand NEXT_PAGE_COMMAND = new ChangePageSpectatorMenuCommand(1, true);
   private static final SpectatorMenuCommand DISABLED_NEXT_PAGE_COMMAND = new ChangePageSpectatorMenuCommand(1, false);
   private static final int field_32443 = 8;
   static final Text CLOSE_TEXT = Text.translatable("spectatorMenu.close");
   static final Text PREVIOUS_PAGE_TEXT = Text.translatable("spectatorMenu.previous_page");
   static final Text NEXT_PAGE_TEXT = Text.translatable("spectatorMenu.next_page");
   public static final SpectatorMenuCommand BLANK_COMMAND = new SpectatorMenuCommand() {
      public void use(SpectatorMenu menu) {
      }

      public Text getName() {
         return ScreenTexts.EMPTY;
      }

      public void renderIcon(MatrixStack matrices, float brightness, int alpha) {
      }

      public boolean isEnabled() {
         return false;
      }
   };
   private final SpectatorMenuCloseCallback closeCallback;
   private SpectatorMenuCommandGroup currentGroup = new RootSpectatorCommandGroup();
   private int selectedSlot = -1;
   int page;

   public SpectatorMenu(SpectatorMenuCloseCallback closeCallback) {
      this.closeCallback = closeCallback;
   }

   public SpectatorMenuCommand getCommand(int slot) {
      int j = slot + this.page * 6;
      if (this.page > 0 && slot == 0) {
         return PREVIOUS_PAGE_COMMAND;
      } else if (slot == 7) {
         return j < this.currentGroup.getCommands().size() ? NEXT_PAGE_COMMAND : DISABLED_NEXT_PAGE_COMMAND;
      } else if (slot == 8) {
         return CLOSE_COMMAND;
      } else {
         return j >= 0 && j < this.currentGroup.getCommands().size() ? (SpectatorMenuCommand)MoreObjects.firstNonNull((SpectatorMenuCommand)this.currentGroup.getCommands().get(j), BLANK_COMMAND) : BLANK_COMMAND;
      }
   }

   public List getCommands() {
      List list = Lists.newArrayList();

      for(int i = 0; i <= 8; ++i) {
         list.add(this.getCommand(i));
      }

      return list;
   }

   public SpectatorMenuCommand getSelectedCommand() {
      return this.getCommand(this.selectedSlot);
   }

   public SpectatorMenuCommandGroup getCurrentGroup() {
      return this.currentGroup;
   }

   public void useCommand(int slot) {
      SpectatorMenuCommand lv = this.getCommand(slot);
      if (lv != BLANK_COMMAND) {
         if (this.selectedSlot == slot && lv.isEnabled()) {
            lv.use(this);
         } else {
            this.selectedSlot = slot;
         }
      }

   }

   public void close() {
      this.closeCallback.close(this);
   }

   public int getSelectedSlot() {
      return this.selectedSlot;
   }

   public void selectElement(SpectatorMenuCommandGroup group) {
      this.currentGroup = group;
      this.selectedSlot = -1;
      this.page = 0;
   }

   public SpectatorMenuState getCurrentState() {
      return new SpectatorMenuState(this.getCommands(), this.selectedSlot);
   }

   @Environment(EnvType.CLIENT)
   static class CloseSpectatorMenuCommand implements SpectatorMenuCommand {
      public void use(SpectatorMenu menu) {
         menu.close();
      }

      public Text getName() {
         return SpectatorMenu.CLOSE_TEXT;
      }

      public void renderIcon(MatrixStack matrices, float brightness, int alpha) {
         RenderSystem.setShaderTexture(0, SpectatorHud.SPECTATOR_TEXTURE);
         DrawableHelper.drawTexture(matrices, 0, 0, 128.0F, 0.0F, 16, 16, 256, 256);
      }

      public boolean isEnabled() {
         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class ChangePageSpectatorMenuCommand implements SpectatorMenuCommand {
      private final int direction;
      private final boolean enabled;

      public ChangePageSpectatorMenuCommand(int direction, boolean enabled) {
         this.direction = direction;
         this.enabled = enabled;
      }

      public void use(SpectatorMenu menu) {
         menu.page += this.direction;
      }

      public Text getName() {
         return this.direction < 0 ? SpectatorMenu.PREVIOUS_PAGE_TEXT : SpectatorMenu.NEXT_PAGE_TEXT;
      }

      public void renderIcon(MatrixStack matrices, float brightness, int alpha) {
         RenderSystem.setShaderTexture(0, SpectatorHud.SPECTATOR_TEXTURE);
         if (this.direction < 0) {
            DrawableHelper.drawTexture(matrices, 0, 0, 144.0F, 0.0F, 16, 16, 256, 256);
         } else {
            DrawableHelper.drawTexture(matrices, 0, 0, 160.0F, 0.0F, 16, 16, 256, 256);
         }

      }

      public boolean isEnabled() {
         return this.enabled;
      }
   }
}
