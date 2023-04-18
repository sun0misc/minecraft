package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class StonecutterScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/stonecutter.png");
   private static final int SCROLLBAR_WIDTH = 12;
   private static final int SCROLLBAR_HEIGHT = 15;
   private static final int RECIPE_LIST_COLUMNS = 4;
   private static final int RECIPE_LIST_ROWS = 3;
   private static final int RECIPE_ENTRY_WIDTH = 16;
   private static final int RECIPE_ENTRY_HEIGHT = 18;
   private static final int SCROLLBAR_AREA_HEIGHT = 54;
   private static final int RECIPE_LIST_OFFSET_X = 52;
   private static final int RECIPE_LIST_OFFSET_Y = 14;
   private float scrollAmount;
   private boolean mouseClicked;
   private int scrollOffset;
   private boolean canCraft;

   public StonecutterScreen(StonecutterScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      handler.setContentsChangedListener(this::onInventoryChange);
      --this.titleY;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      super.render(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      this.renderBackground(matrices);
      RenderSystem.setShaderTexture(0, TEXTURE);
      int k = this.x;
      int l = this.y;
      drawTexture(matrices, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      int m = (int)(41.0F * this.scrollAmount);
      drawTexture(matrices, k + 119, l + 15 + m, 176 + (this.shouldScroll() ? 0 : 12), 0, 12, 15);
      int n = this.x + 52;
      int o = this.y + 14;
      int p = this.scrollOffset + 12;
      this.renderRecipeBackground(matrices, mouseX, mouseY, n, o, p);
      this.renderRecipeIcons(matrices, n, o, p);
   }

   protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
      super.drawMouseoverTooltip(matrices, x, y);
      if (this.canCraft) {
         int k = this.x + 52;
         int l = this.y + 14;
         int m = this.scrollOffset + 12;
         List list = ((StonecutterScreenHandler)this.handler).getAvailableRecipes();

         for(int n = this.scrollOffset; n < m && n < ((StonecutterScreenHandler)this.handler).getAvailableRecipeCount(); ++n) {
            int o = n - this.scrollOffset;
            int p = k + o % 4 * 16;
            int q = l + o / 4 * 18 + 2;
            if (x >= p && x < p + 16 && y >= q && y < q + 18) {
               this.renderTooltip(matrices, ((StonecuttingRecipe)list.get(n)).getOutput(this.client.world.getRegistryManager()), x, y);
            }
         }
      }

   }

   private void renderRecipeBackground(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int scrollOffset) {
      for(int n = this.scrollOffset; n < scrollOffset && n < ((StonecutterScreenHandler)this.handler).getAvailableRecipeCount(); ++n) {
         int o = n - this.scrollOffset;
         int p = x + o % 4 * 16;
         int q = o / 4;
         int r = y + q * 18 + 2;
         int s = this.backgroundHeight;
         if (n == ((StonecutterScreenHandler)this.handler).getSelectedRecipe()) {
            s += 18;
         } else if (mouseX >= p && mouseY >= r && mouseX < p + 16 && mouseY < r + 18) {
            s += 36;
         }

         drawTexture(matrices, p, r - 1, 0, s, 16, 18);
      }

   }

   private void renderRecipeIcons(MatrixStack matrices, int x, int y, int scrollOffset) {
      List list = ((StonecutterScreenHandler)this.handler).getAvailableRecipes();

      for(int l = this.scrollOffset; l < scrollOffset && l < ((StonecutterScreenHandler)this.handler).getAvailableRecipeCount(); ++l) {
         int m = l - this.scrollOffset;
         int n = x + m % 4 * 16;
         int o = m / 4;
         int p = y + o * 18 + 2;
         this.client.getItemRenderer().renderInGuiWithOverrides(matrices, ((StonecuttingRecipe)list.get(l)).getOutput(this.client.world.getRegistryManager()), n, p);
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.mouseClicked = false;
      if (this.canCraft) {
         int j = this.x + 52;
         int k = this.y + 14;
         int l = this.scrollOffset + 12;

         for(int m = this.scrollOffset; m < l; ++m) {
            int n = m - this.scrollOffset;
            double f = mouseX - (double)(j + n % 4 * 16);
            double g = mouseY - (double)(k + n / 4 * 18);
            if (f >= 0.0 && g >= 0.0 && f < 16.0 && g < 18.0 && ((StonecutterScreenHandler)this.handler).onButtonClick(this.client.player, m)) {
               MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
               this.client.interactionManager.clickButton(((StonecutterScreenHandler)this.handler).syncId, m);
               return true;
            }
         }

         j = this.x + 119;
         k = this.y + 9;
         if (mouseX >= (double)j && mouseX < (double)(j + 12) && mouseY >= (double)k && mouseY < (double)(k + 54)) {
            this.mouseClicked = true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.mouseClicked && this.shouldScroll()) {
         int j = this.y + 14;
         int k = j + 54;
         this.scrollAmount = ((float)mouseY - (float)j - 7.5F) / ((float)(k - j) - 15.0F);
         this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
         this.scrollOffset = (int)((double)(this.scrollAmount * (float)this.getMaxScroll()) + 0.5) * 4;
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.shouldScroll()) {
         int i = this.getMaxScroll();
         float g = (float)amount / (float)i;
         this.scrollAmount = MathHelper.clamp(this.scrollAmount - g, 0.0F, 1.0F);
         this.scrollOffset = (int)((double)(this.scrollAmount * (float)i) + 0.5) * 4;
      }

      return true;
   }

   private boolean shouldScroll() {
      return this.canCraft && ((StonecutterScreenHandler)this.handler).getAvailableRecipeCount() > 12;
   }

   protected int getMaxScroll() {
      return (((StonecutterScreenHandler)this.handler).getAvailableRecipeCount() + 4 - 1) / 4 - 3;
   }

   private void onInventoryChange() {
      this.canCraft = ((StonecutterScreenHandler)this.handler).canCraft();
      if (!this.canCraft) {
         this.scrollAmount = 0.0F;
         this.scrollOffset = 0;
      }

   }
}
