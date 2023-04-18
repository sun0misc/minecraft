package net.minecraft.client.gui.screen.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.AbstractRecipeScreenHandler;

@Environment(EnvType.CLIENT)
public class RecipeGroupButtonWidget extends ToggleButtonWidget {
   private final RecipeBookGroup category;
   private static final float field_32412 = 15.0F;
   private float bounce;

   public RecipeGroupButtonWidget(RecipeBookGroup category) {
      super(0, 0, 35, 27, false);
      this.category = category;
      this.setTextureUV(153, 2, 35, 0, RecipeBookWidget.TEXTURE);
   }

   public void checkForNewRecipes(MinecraftClient client) {
      ClientRecipeBook lv = client.player.getRecipeBook();
      List list = lv.getResultsForGroup(this.category);
      if (client.player.currentScreenHandler instanceof AbstractRecipeScreenHandler) {
         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            RecipeResultCollection lv2 = (RecipeResultCollection)var4.next();
            Iterator var6 = lv2.getResults(lv.isFilteringCraftable((AbstractRecipeScreenHandler)client.player.currentScreenHandler)).iterator();

            while(var6.hasNext()) {
               Recipe lv3 = (Recipe)var6.next();
               if (lv.shouldDisplay(lv3)) {
                  this.bounce = 15.0F;
                  return;
               }
            }
         }

      }
   }

   public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.bounce > 0.0F) {
         float g = 1.0F + 0.1F * (float)Math.sin((double)(this.bounce / 15.0F * 3.1415927F));
         matrices.push();
         matrices.translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
         matrices.scale(1.0F, g, 1.0F);
         matrices.translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
      }

      MinecraftClient lv = MinecraftClient.getInstance();
      RenderSystem.setShaderTexture(0, this.texture);
      RenderSystem.disableDepthTest();
      int k = this.u;
      int l = this.v;
      if (this.toggled) {
         k += this.pressedUOffset;
      }

      if (this.isSelected()) {
         l += this.hoverVOffset;
      }

      int m = this.getX();
      if (this.toggled) {
         m -= 2;
      }

      drawTexture(matrices, m, this.getY(), k, l, this.width, this.height);
      RenderSystem.enableDepthTest();
      this.renderIcons(matrices, lv.getItemRenderer());
      if (this.bounce > 0.0F) {
         matrices.pop();
         this.bounce -= delta;
      }

   }

   private void renderIcons(MatrixStack matrices, ItemRenderer itemRenderer) {
      List list = this.category.getIcons();
      int i = this.toggled ? -2 : 0;
      if (list.size() == 1) {
         itemRenderer.renderInGui(matrices, (ItemStack)list.get(0), this.getX() + 9 + i, this.getY() + 5);
      } else if (list.size() == 2) {
         itemRenderer.renderInGui(matrices, (ItemStack)list.get(0), this.getX() + 3 + i, this.getY() + 5);
         itemRenderer.renderInGui(matrices, (ItemStack)list.get(1), this.getX() + 14 + i, this.getY() + 5);
      }

   }

   public RecipeBookGroup getCategory() {
      return this.category;
   }

   public boolean hasKnownRecipes(ClientRecipeBook recipeBook) {
      List list = recipeBook.getResultsForGroup(this.category);
      this.visible = false;
      if (list != null) {
         Iterator var3 = list.iterator();

         while(var3.hasNext()) {
            RecipeResultCollection lv = (RecipeResultCollection)var3.next();
            if (lv.isInitialized() && lv.hasFittingRecipes()) {
               this.visible = true;
               break;
            }
         }
      }

      return this.visible;
   }
}
