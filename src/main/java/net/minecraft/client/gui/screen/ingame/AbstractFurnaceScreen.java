package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.AbstractFurnaceRecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class AbstractFurnaceScreen extends HandledScreen implements RecipeBookProvider {
   private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");
   public final AbstractFurnaceRecipeBookScreen recipeBook;
   private boolean narrow;
   private final Identifier background;

   public AbstractFurnaceScreen(AbstractFurnaceScreenHandler handler, AbstractFurnaceRecipeBookScreen recipeBook, PlayerInventory inventory, Text title, Identifier background) {
      super(handler, inventory, title);
      this.recipeBook = recipeBook;
      this.background = background;
   }

   public void init() {
      super.init();
      this.narrow = this.width < 379;
      this.recipeBook.initialize(this.width, this.height, this.client, this.narrow, (AbstractRecipeScreenHandler)this.handler);
      this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
      this.addDrawableChild(new TexturedButtonWidget(this.x + 20, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (button) -> {
         this.recipeBook.toggleOpen();
         this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
         ((TexturedButtonWidget)button).setPosition(this.x + 20, this.height / 2 - 49);
      }));
      this.titleX = (this.backgroundWidth - this.textRenderer.getWidth((StringVisitable)this.title)) / 2;
   }

   public void handledScreenTick() {
      super.handledScreenTick();
      this.recipeBook.update();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      if (this.recipeBook.isOpen() && this.narrow) {
         this.drawBackground(matrices, delta, mouseX, mouseY);
         this.recipeBook.render(matrices, mouseX, mouseY, delta);
      } else {
         this.recipeBook.render(matrices, mouseX, mouseY, delta);
         super.render(matrices, mouseX, mouseY, delta);
         this.recipeBook.drawGhostSlots(matrices, this.x, this.y, true, delta);
      }

      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
      this.recipeBook.drawTooltip(matrices, this.x, this.y, mouseX, mouseY);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      RenderSystem.setShaderTexture(0, this.background);
      int k = this.x;
      int l = this.y;
      drawTexture(matrices, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      int m;
      if (((AbstractFurnaceScreenHandler)this.handler).isBurning()) {
         m = ((AbstractFurnaceScreenHandler)this.handler).getFuelProgress();
         drawTexture(matrices, k + 56, l + 36 + 12 - m, 176, 12 - m, 14, m + 1);
      }

      m = ((AbstractFurnaceScreenHandler)this.handler).getCookProgress();
      drawTexture(matrices, k + 79, l + 34, 176, 14, m + 1, 16);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         return this.narrow && this.recipeBook.isOpen() ? true : super.mouseClicked(mouseX, mouseY, button);
      }
   }

   protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
      super.onMouseClick(slot, slotId, button, actionType);
      this.recipeBook.slotClicked(slot);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.recipeBook.keyPressed(keyCode, scanCode, modifiers) ? false : super.keyPressed(keyCode, scanCode, modifiers);
   }

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
      boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
      return this.recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth, this.backgroundHeight, button) && bl;
   }

   public boolean charTyped(char chr, int modifiers) {
      return this.recipeBook.charTyped(chr, modifiers) ? true : super.charTyped(chr, modifiers);
   }

   public void refreshRecipeBook() {
      this.recipeBook.refresh();
   }

   public RecipeBookWidget getRecipeBookWidget() {
      return this.recipeBook;
   }
}
