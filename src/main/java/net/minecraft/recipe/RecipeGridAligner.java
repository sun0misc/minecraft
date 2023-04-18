package net.minecraft.recipe;

import java.util.Iterator;
import net.minecraft.util.math.MathHelper;

public interface RecipeGridAligner {
   default void alignRecipeToGrid(int gridWidth, int gridHeight, int gridOutputSlot, Recipe recipe, Iterator inputs, int amount) {
      int m = gridWidth;
      int n = gridHeight;
      if (recipe instanceof ShapedRecipe lv) {
         m = lv.getWidth();
         n = lv.getHeight();
      }

      int o = 0;

      for(int p = 0; p < gridHeight; ++p) {
         if (o == gridOutputSlot) {
            ++o;
         }

         boolean bl = (float)n < (float)gridHeight / 2.0F;
         int q = MathHelper.floor((float)gridHeight / 2.0F - (float)n / 2.0F);
         if (bl && q > p) {
            o += gridWidth;
            ++p;
         }

         for(int r = 0; r < gridWidth; ++r) {
            if (!inputs.hasNext()) {
               return;
            }

            bl = (float)m < (float)gridWidth / 2.0F;
            q = MathHelper.floor((float)gridWidth / 2.0F - (float)m / 2.0F);
            int s = m;
            boolean bl2 = r < m;
            if (bl) {
               s = q + m;
               bl2 = q <= r && r < q + m;
            }

            if (bl2) {
               this.acceptAlignedInput(inputs, o, amount, p, r);
            } else if (s == r) {
               o += gridWidth - r;
               break;
            }

            ++o;
         }
      }

   }

   void acceptAlignedInput(Iterator inputs, int slot, int amount, int gridX, int gridY);
}
