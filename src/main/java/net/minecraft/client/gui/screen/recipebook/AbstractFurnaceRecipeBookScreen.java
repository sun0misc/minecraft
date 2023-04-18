package net.minecraft.client.gui.screen.recipebook;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class AbstractFurnaceRecipeBookScreen extends RecipeBookWidget {
   @Nullable
   private Ingredient fuels;

   protected void setBookButtonTexture() {
      this.toggleCraftableButton.setTextureUV(152, 182, 28, 18, TEXTURE);
   }

   public void slotClicked(@Nullable Slot slot) {
      super.slotClicked(slot);
      if (slot != null && slot.id < this.craftingScreenHandler.getCraftingSlotCount()) {
         this.ghostSlots.reset();
      }

   }

   public void showGhostRecipe(Recipe recipe, List slots) {
      ItemStack lv = recipe.getOutput(this.client.world.getRegistryManager());
      this.ghostSlots.setRecipe(recipe);
      this.ghostSlots.addSlot(Ingredient.ofStacks(lv), ((Slot)slots.get(2)).x, ((Slot)slots.get(2)).y);
      DefaultedList lv2 = recipe.getIngredients();
      Slot lv3 = (Slot)slots.get(1);
      if (lv3.getStack().isEmpty()) {
         if (this.fuels == null) {
            this.fuels = Ingredient.ofStacks(this.getAllowedFuels().stream().filter((arg) -> {
               return arg.isEnabled(this.client.world.getEnabledFeatures());
            }).map(ItemStack::new));
         }

         this.ghostSlots.addSlot(this.fuels, lv3.x, lv3.y);
      }

      Iterator iterator = lv2.iterator();

      for(int i = 0; i < 2; ++i) {
         if (!iterator.hasNext()) {
            return;
         }

         Ingredient lv4 = (Ingredient)iterator.next();
         if (!lv4.isEmpty()) {
            Slot lv5 = (Slot)slots.get(i);
            this.ghostSlots.addSlot(lv4, lv5.x, lv5.y);
         }
      }

   }

   protected abstract Set getAllowedFuels();
}
