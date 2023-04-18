package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RecipeAlternativesWidget extends DrawableHelper implements Drawable, Element {
   static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/recipe_book.png");
   private static final int field_32406 = 4;
   private static final int field_32407 = 5;
   private static final float field_33739 = 0.375F;
   public static final int field_42162 = 25;
   private final List alternativeButtons = Lists.newArrayList();
   private boolean visible;
   private int buttonX;
   private int buttonY;
   MinecraftClient client;
   private RecipeResultCollection resultCollection;
   @Nullable
   private Recipe lastClickedRecipe;
   float time;
   boolean furnace;

   public void showAlternativesForResult(MinecraftClient client, RecipeResultCollection results, int buttonX, int buttonY, int areaCenterX, int areaCenterY, float delta) {
      this.client = client;
      this.resultCollection = results;
      if (client.player.currentScreenHandler instanceof AbstractFurnaceScreenHandler) {
         this.furnace = true;
      }

      boolean bl = client.player.getRecipeBook().isFilteringCraftable((AbstractRecipeScreenHandler)client.player.currentScreenHandler);
      List list = results.getRecipes(true);
      List list2 = bl ? Collections.emptyList() : results.getRecipes(false);
      int m = list.size();
      int n = m + list2.size();
      int o = n <= 16 ? 4 : 5;
      int p = (int)Math.ceil((double)((float)n / (float)o));
      this.buttonX = buttonX;
      this.buttonY = buttonY;
      float g = (float)(this.buttonX + Math.min(n, o) * 25);
      float h = (float)(areaCenterX + 50);
      if (g > h) {
         this.buttonX = (int)((float)this.buttonX - delta * (float)((int)((g - h) / delta)));
      }

      float q = (float)(this.buttonY + p * 25);
      float r = (float)(areaCenterY + 50);
      if (q > r) {
         this.buttonY = (int)((float)this.buttonY - delta * (float)MathHelper.ceil((q - r) / delta));
      }

      float s = (float)this.buttonY;
      float t = (float)(areaCenterY - 100);
      if (s < t) {
         this.buttonY = (int)((float)this.buttonY - delta * (float)MathHelper.ceil((s - t) / delta));
      }

      this.visible = true;
      this.alternativeButtons.clear();

      for(int u = 0; u < n; ++u) {
         boolean bl2 = u < m;
         Recipe lv = bl2 ? (Recipe)list.get(u) : (Recipe)list2.get(u - m);
         int v = this.buttonX + 4 + 25 * (u % o);
         int w = this.buttonY + 5 + 25 * (u / o);
         if (this.furnace) {
            this.alternativeButtons.add(new FurnaceAlternativeButtonWidget(v, w, lv, bl2));
         } else {
            this.alternativeButtons.add(new AlternativeButtonWidget(v, w, lv, bl2));
         }
      }

      this.lastClickedRecipe = null;
   }

   public RecipeResultCollection getResults() {
      return this.resultCollection;
   }

   @Nullable
   public Recipe getLastClickedRecipe() {
      return this.lastClickedRecipe;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      } else {
         Iterator var6 = this.alternativeButtons.iterator();

         AlternativeButtonWidget lv;
         do {
            if (!var6.hasNext()) {
               return false;
            }

            lv = (AlternativeButtonWidget)var6.next();
         } while(!lv.mouseClicked(mouseX, mouseY, button));

         this.lastClickedRecipe = lv.recipe;
         return true;
      }
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return false;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.visible) {
         this.time += delta;
         RenderSystem.enableBlend();
         RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
         matrices.push();
         matrices.translate(0.0F, 0.0F, 170.0F);
         int k = this.alternativeButtons.size() <= 16 ? 4 : 5;
         int l = Math.min(this.alternativeButtons.size(), k);
         int m = MathHelper.ceil((float)this.alternativeButtons.size() / (float)k);
         int n = true;
         drawNineSlicedTexture(matrices, this.buttonX, this.buttonY, l * 25 + 8, m * 25 + 8, 4, 32, 32, 82, 208);
         RenderSystem.disableBlend();
         Iterator var9 = this.alternativeButtons.iterator();

         while(var9.hasNext()) {
            AlternativeButtonWidget lv = (AlternativeButtonWidget)var9.next();
            lv.render(matrices, mouseX, mouseY, delta);
         }

         matrices.pop();
      }
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setFocused(boolean focused) {
   }

   public boolean isFocused() {
      return false;
   }

   @Environment(EnvType.CLIENT)
   private class FurnaceAlternativeButtonWidget extends AlternativeButtonWidget {
      public FurnaceAlternativeButtonWidget(int i, int j, Recipe arg2, boolean bl) {
         super(i, j, arg2, bl);
      }

      protected void alignRecipe(Recipe recipe) {
         ItemStack[] lvs = ((Ingredient)recipe.getIngredients().get(0)).getMatchingStacks();
         this.slots.add(new AlternativeButtonWidget.InputSlot(10, 10, lvs));
      }
   }

   @Environment(EnvType.CLIENT)
   private class AlternativeButtonWidget extends ClickableWidget implements RecipeGridAligner {
      final Recipe recipe;
      private final boolean craftable;
      protected final List slots = Lists.newArrayList();

      public AlternativeButtonWidget(int x, int y, Recipe recipe, boolean craftable) {
         super(x, y, 200, 20, ScreenTexts.EMPTY);
         this.width = 24;
         this.height = 24;
         this.recipe = recipe;
         this.craftable = craftable;
         this.alignRecipe(recipe);
      }

      protected void alignRecipe(Recipe recipe) {
         this.alignRecipeToGrid(3, 3, -1, recipe, recipe.getIngredients().iterator(), 0);
      }

      public void appendClickableNarrations(NarrationMessageBuilder builder) {
         this.appendDefaultNarrations(builder);
      }

      public void acceptAlignedInput(Iterator inputs, int slot, int amount, int gridX, int gridY) {
         ItemStack[] lvs = ((Ingredient)inputs.next()).getMatchingStacks();
         if (lvs.length != 0) {
            this.slots.add(new InputSlot(3 + gridY * 7, 3 + gridX * 7, lvs));
         }

      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         RenderSystem.setShaderTexture(0, RecipeAlternativesWidget.BACKGROUND_TEXTURE);
         int k = 152;
         if (!this.craftable) {
            k += 26;
         }

         int l = RecipeAlternativesWidget.this.furnace ? 130 : 78;
         if (this.isSelected()) {
            l += 26;
         }

         drawTexture(matrices, this.getX(), this.getY(), k, l, this.width, this.height);
         matrices.push();
         matrices.translate((double)(this.getX() + 2), (double)(this.getY() + 2), 150.0);
         Iterator var7 = this.slots.iterator();

         while(var7.hasNext()) {
            InputSlot lv = (InputSlot)var7.next();
            matrices.push();
            matrices.translate((double)lv.y, (double)lv.x, 0.0);
            matrices.scale(0.375F, 0.375F, 1.0F);
            matrices.translate(-8.0, -8.0, 0.0);
            RecipeAlternativesWidget.this.client.getItemRenderer().renderInGuiWithOverrides(matrices, lv.stacks[MathHelper.floor(RecipeAlternativesWidget.this.time / 30.0F) % lv.stacks.length], 0, 0);
            matrices.pop();
         }

         matrices.pop();
      }

      @Environment(EnvType.CLIENT)
      protected class InputSlot {
         public final ItemStack[] stacks;
         public final int y;
         public final int x;

         public InputSlot(int y, int x, ItemStack[] stacks) {
            this.y = y;
            this.x = x;
            this.stacks = stacks;
         }
      }
   }
}
