package net.minecraft.client.toast;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class RecipeToast implements Toast {
   private static final long DURATION = 5000L;
   private static final Text TITLE = Text.translatable("recipe.toast.title");
   private static final Text DESCRIPTION = Text.translatable("recipe.toast.description");
   private final List recipes = Lists.newArrayList();
   private long startTime;
   private boolean justUpdated;

   public RecipeToast(Recipe recipes) {
      this.recipes.add(recipes);
   }

   public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
      if (this.justUpdated) {
         this.startTime = startTime;
         this.justUpdated = false;
      }

      if (this.recipes.isEmpty()) {
         return Toast.Visibility.HIDE;
      } else {
         RenderSystem.setShaderTexture(0, TEXTURE);
         DrawableHelper.drawTexture(matrices, 0, 0, 0, 32, this.getWidth(), this.getHeight());
         manager.getClient().textRenderer.draw(matrices, TITLE, 30.0F, 7.0F, -11534256);
         manager.getClient().textRenderer.draw(matrices, DESCRIPTION, 30.0F, 18.0F, -16777216);
         Recipe lv = (Recipe)this.recipes.get((int)((double)startTime / Math.max(1.0, 5000.0 * manager.getNotificationDisplayTimeMultiplier() / (double)this.recipes.size()) % (double)this.recipes.size()));
         ItemStack lv2 = lv.createIcon();
         matrices.push();
         matrices.scale(0.6F, 0.6F, 1.0F);
         manager.getClient().getItemRenderer().renderInGui(matrices, lv2, 3, 3);
         matrices.pop();
         manager.getClient().getItemRenderer().renderInGui(matrices, lv.getOutput(manager.getClient().world.getRegistryManager()), 8, 8);
         return (double)(startTime - this.startTime) >= 5000.0 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
      }
   }

   private void addRecipes(Recipe recipes) {
      this.recipes.add(recipes);
      this.justUpdated = true;
   }

   public static void show(ToastManager manager, Recipe recipes) {
      RecipeToast lv = (RecipeToast)manager.getToast(RecipeToast.class, TYPE);
      if (lv == null) {
         manager.add(new RecipeToast(recipes));
      } else {
         lv.addRecipes(recipes);
      }

   }
}
