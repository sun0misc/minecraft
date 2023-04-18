package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class RecipeBookWidget extends DrawableHelper implements RecipeGridAligner, Drawable, Element, Selectable, RecipeDisplayListener {
   protected static final Identifier TEXTURE = new Identifier("textures/gui/recipe_book.png");
   private static final Text SEARCH_HINT_TEXT;
   public static final int field_32408 = 147;
   public static final int field_32409 = 166;
   private static final int field_32410 = 86;
   private static final Text TOGGLE_CRAFTABLE_RECIPES_TEXT;
   private static final Text TOGGLE_ALL_RECIPES_TEXT;
   private int leftOffset;
   private int parentWidth;
   private int parentHeight;
   protected final RecipeBookGhostSlots ghostSlots = new RecipeBookGhostSlots();
   private final List tabButtons = Lists.newArrayList();
   @Nullable
   private RecipeGroupButtonWidget currentTab;
   protected ToggleButtonWidget toggleCraftableButton;
   protected AbstractRecipeScreenHandler craftingScreenHandler;
   protected MinecraftClient client;
   @Nullable
   private TextFieldWidget searchField;
   private String searchText = "";
   private ClientRecipeBook recipeBook;
   private final RecipeBookResults recipesArea = new RecipeBookResults();
   private final RecipeMatcher recipeFinder = new RecipeMatcher();
   private int cachedInvChangeCount;
   private boolean searching;
   private boolean open;
   private boolean narrow;

   public void initialize(int parentWidth, int parentHeight, MinecraftClient client, boolean narrow, AbstractRecipeScreenHandler craftingScreenHandler) {
      this.client = client;
      this.parentWidth = parentWidth;
      this.parentHeight = parentHeight;
      this.craftingScreenHandler = craftingScreenHandler;
      this.narrow = narrow;
      client.player.currentScreenHandler = craftingScreenHandler;
      this.recipeBook = client.player.getRecipeBook();
      this.cachedInvChangeCount = client.player.getInventory().getChangeCount();
      this.open = this.isGuiOpen();
      if (this.open) {
         this.reset();
      }

   }

   public void reset() {
      this.leftOffset = this.narrow ? 0 : 86;
      int i = (this.parentWidth - 147) / 2 - this.leftOffset;
      int j = (this.parentHeight - 166) / 2;
      this.recipeFinder.clear();
      this.client.player.getInventory().populateRecipeFinder(this.recipeFinder);
      this.craftingScreenHandler.populateRecipeFinder(this.recipeFinder);
      String string = this.searchField != null ? this.searchField.getText() : "";
      TextRenderer var10003 = this.client.textRenderer;
      int var10004 = i + 26;
      int var10005 = j + 14;
      Objects.requireNonNull(this.client.textRenderer);
      this.searchField = new TextFieldWidget(var10003, var10004, var10005, 79, 9 + 3, Text.translatable("itemGroup.search"));
      this.searchField.setMaxLength(50);
      this.searchField.setVisible(true);
      this.searchField.setEditableColor(16777215);
      this.searchField.setText(string);
      this.searchField.setPlaceholder(SEARCH_HINT_TEXT);
      this.recipesArea.initialize(this.client, i, j);
      this.recipesArea.setGui(this);
      this.toggleCraftableButton = new ToggleButtonWidget(i + 110, j + 12, 26, 16, this.recipeBook.isFilteringCraftable(this.craftingScreenHandler));
      this.updateTooltip();
      this.setBookButtonTexture();
      this.tabButtons.clear();
      Iterator var4 = RecipeBookGroup.getGroups(this.craftingScreenHandler.getCategory()).iterator();

      while(var4.hasNext()) {
         RecipeBookGroup lv = (RecipeBookGroup)var4.next();
         this.tabButtons.add(new RecipeGroupButtonWidget(lv));
      }

      if (this.currentTab != null) {
         this.currentTab = (RecipeGroupButtonWidget)this.tabButtons.stream().filter((button) -> {
            return button.getCategory().equals(this.currentTab.getCategory());
         }).findFirst().orElse((Object)null);
      }

      if (this.currentTab == null) {
         this.currentTab = (RecipeGroupButtonWidget)this.tabButtons.get(0);
      }

      this.currentTab.setToggled(true);
      this.refreshResults(false);
      this.refreshTabButtons();
   }

   private void updateTooltip() {
      this.toggleCraftableButton.setTooltip(this.toggleCraftableButton.isToggled() ? Tooltip.of(this.getToggleCraftableButtonText()) : Tooltip.of(TOGGLE_ALL_RECIPES_TEXT));
   }

   protected void setBookButtonTexture() {
      this.toggleCraftableButton.setTextureUV(152, 41, 28, 18, TEXTURE);
   }

   public int findLeftEdge(int width, int backgroundWidth) {
      int k;
      if (this.isOpen() && !this.narrow) {
         k = 177 + (width - backgroundWidth - 200) / 2;
      } else {
         k = (width - backgroundWidth) / 2;
      }

      return k;
   }

   public void toggleOpen() {
      this.setOpen(!this.isOpen());
   }

   public boolean isOpen() {
      return this.open;
   }

   private boolean isGuiOpen() {
      return this.recipeBook.isGuiOpen(this.craftingScreenHandler.getCategory());
   }

   protected void setOpen(boolean opened) {
      if (opened) {
         this.reset();
      }

      this.open = opened;
      this.recipeBook.setGuiOpen(this.craftingScreenHandler.getCategory(), opened);
      if (!opened) {
         this.recipesArea.hideAlternates();
      }

      this.sendBookDataPacket();
   }

   public void slotClicked(@Nullable Slot slot) {
      if (slot != null && slot.id < this.craftingScreenHandler.getCraftingSlotCount()) {
         this.ghostSlots.reset();
         if (this.isOpen()) {
            this.refreshInputs();
         }
      }

   }

   private void refreshResults(boolean resetCurrentPage) {
      List list = this.recipeBook.getResultsForGroup(this.currentTab.getCategory());
      list.forEach((resultCollection) -> {
         resultCollection.computeCraftables(this.recipeFinder, this.craftingScreenHandler.getCraftingWidth(), this.craftingScreenHandler.getCraftingHeight(), this.recipeBook);
      });
      List list2 = Lists.newArrayList(list);
      list2.removeIf((resultCollection) -> {
         return !resultCollection.isInitialized();
      });
      list2.removeIf((resultCollection) -> {
         return !resultCollection.hasFittingRecipes();
      });
      String string = this.searchField.getText();
      if (!string.isEmpty()) {
         ObjectSet objectSet = new ObjectLinkedOpenHashSet(this.client.getSearchProvider(SearchManager.RECIPE_OUTPUT).findAll(string.toLowerCase(Locale.ROOT)));
         list2.removeIf((arg) -> {
            return !objectSet.contains(arg);
         });
      }

      if (this.recipeBook.isFilteringCraftable(this.craftingScreenHandler)) {
         list2.removeIf((resultCollection) -> {
            return !resultCollection.hasCraftableRecipes();
         });
      }

      this.recipesArea.setResults(list2, resetCurrentPage);
   }

   private void refreshTabButtons() {
      int i = (this.parentWidth - 147) / 2 - this.leftOffset - 30;
      int j = (this.parentHeight - 166) / 2 + 3;
      int k = true;
      int l = 0;
      Iterator var5 = this.tabButtons.iterator();

      while(true) {
         while(var5.hasNext()) {
            RecipeGroupButtonWidget lv = (RecipeGroupButtonWidget)var5.next();
            RecipeBookGroup lv2 = lv.getCategory();
            if (lv2 != RecipeBookGroup.CRAFTING_SEARCH && lv2 != RecipeBookGroup.FURNACE_SEARCH) {
               if (lv.hasKnownRecipes(this.recipeBook)) {
                  lv.setPosition(i, j + 27 * l++);
                  lv.checkForNewRecipes(this.client);
               }
            } else {
               lv.visible = true;
               lv.setPosition(i, j + 27 * l++);
            }
         }

         return;
      }
   }

   public void update() {
      boolean bl = this.isGuiOpen();
      if (this.isOpen() != bl) {
         this.setOpen(bl);
      }

      if (this.isOpen()) {
         if (this.cachedInvChangeCount != this.client.player.getInventory().getChangeCount()) {
            this.refreshInputs();
            this.cachedInvChangeCount = this.client.player.getInventory().getChangeCount();
         }

         this.searchField.tick();
      }
   }

   private void refreshInputs() {
      this.recipeFinder.clear();
      this.client.player.getInventory().populateRecipeFinder(this.recipeFinder);
      this.craftingScreenHandler.populateRecipeFinder(this.recipeFinder);
      this.refreshResults(false);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.isOpen()) {
         matrices.push();
         matrices.translate(0.0F, 0.0F, 100.0F);
         RenderSystem.setShaderTexture(0, TEXTURE);
         int k = (this.parentWidth - 147) / 2 - this.leftOffset;
         int l = (this.parentHeight - 166) / 2;
         drawTexture(matrices, k, l, 1, 1, 147, 166);
         this.searchField.render(matrices, mouseX, mouseY, delta);
         Iterator var7 = this.tabButtons.iterator();

         while(var7.hasNext()) {
            RecipeGroupButtonWidget lv = (RecipeGroupButtonWidget)var7.next();
            lv.render(matrices, mouseX, mouseY, delta);
         }

         this.toggleCraftableButton.render(matrices, mouseX, mouseY, delta);
         this.recipesArea.draw(matrices, k, l, mouseX, mouseY, delta);
         matrices.pop();
      }
   }

   public void drawTooltip(MatrixStack matrices, int i, int j, int k, int l) {
      if (this.isOpen()) {
         this.recipesArea.drawTooltip(matrices, k, l);
         this.drawGhostSlotTooltip(matrices, i, j, k, l);
      }
   }

   protected Text getToggleCraftableButtonText() {
      return TOGGLE_CRAFTABLE_RECIPES_TEXT;
   }

   private void drawGhostSlotTooltip(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
      ItemStack lv = null;

      for(int m = 0; m < this.ghostSlots.getSlotCount(); ++m) {
         RecipeBookGhostSlots.GhostInputSlot lv2 = this.ghostSlots.getSlot(m);
         int n = lv2.getX() + x;
         int o = lv2.getY() + y;
         if (mouseX >= n && mouseY >= o && mouseX < n + 16 && mouseY < o + 16) {
            lv = lv2.getCurrentItemStack();
         }
      }

      if (lv != null && this.client.currentScreen != null) {
         this.client.currentScreen.renderTooltip(matrices, this.client.currentScreen.getTooltipFromItem(lv), mouseX, mouseY);
      }

   }

   public void drawGhostSlots(MatrixStack matrices, int x, int y, boolean notInventory, float delta) {
      this.ghostSlots.draw(matrices, this.client, x, y, notInventory, delta);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isOpen() && !this.client.player.isSpectator()) {
         if (this.recipesArea.mouseClicked(mouseX, mouseY, button, (this.parentWidth - 147) / 2 - this.leftOffset, (this.parentHeight - 166) / 2, 147, 166)) {
            Recipe lv = this.recipesArea.getLastClickedRecipe();
            RecipeResultCollection lv2 = this.recipesArea.getLastClickedResults();
            if (lv != null && lv2 != null) {
               if (!lv2.isCraftable(lv) && this.ghostSlots.getRecipe() == lv) {
                  return false;
               }

               this.ghostSlots.reset();
               this.client.interactionManager.clickRecipe(this.client.player.currentScreenHandler.syncId, lv, Screen.hasShiftDown());
               if (!this.isWide()) {
                  this.setOpen(false);
               }
            }

            return true;
         } else if (this.searchField.mouseClicked(mouseX, mouseY, button)) {
            this.searchField.setFocused(true);
            return true;
         } else {
            this.searchField.setFocused(false);
            if (this.toggleCraftableButton.mouseClicked(mouseX, mouseY, button)) {
               boolean bl = this.toggleFilteringCraftable();
               this.toggleCraftableButton.setToggled(bl);
               this.updateTooltip();
               this.sendBookDataPacket();
               this.refreshResults(false);
               return true;
            } else {
               Iterator var6 = this.tabButtons.iterator();

               RecipeGroupButtonWidget lv3;
               do {
                  if (!var6.hasNext()) {
                     return false;
                  }

                  lv3 = (RecipeGroupButtonWidget)var6.next();
               } while(!lv3.mouseClicked(mouseX, mouseY, button));

               if (this.currentTab != lv3) {
                  if (this.currentTab != null) {
                     this.currentTab.setToggled(false);
                  }

                  this.currentTab = lv3;
                  this.currentTab.setToggled(true);
                  this.refreshResults(true);
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private boolean toggleFilteringCraftable() {
      RecipeBookCategory lv = this.craftingScreenHandler.getCategory();
      boolean bl = !this.recipeBook.isFilteringCraftable(lv);
      this.recipeBook.setFilteringCraftable(lv, bl);
      return bl;
   }

   public boolean isClickOutsideBounds(double mouseX, double mouseY, int x, int y, int backgroundWidth, int backgroundHeight, int button) {
      if (!this.isOpen()) {
         return true;
      } else {
         boolean bl = mouseX < (double)x || mouseY < (double)y || mouseX >= (double)(x + backgroundWidth) || mouseY >= (double)(y + backgroundHeight);
         boolean bl2 = (double)(x - 147) < mouseX && mouseX < (double)x && (double)y < mouseY && mouseY < (double)(y + backgroundHeight);
         return bl && !bl2 && !this.currentTab.isSelected();
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.searching = false;
      if (this.isOpen() && !this.client.player.isSpectator()) {
         if (keyCode == GLFW.GLFW_KEY_ESCAPE && !this.isWide()) {
            this.setOpen(false);
            return true;
         } else if (this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
            this.refreshSearchResults();
            return true;
         } else if (this.searchField.isFocused() && this.searchField.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
         } else if (this.client.options.chatKey.matchesKey(keyCode, scanCode) && !this.searchField.isFocused()) {
            this.searching = true;
            this.searchField.setFocused(true);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      this.searching = false;
      return Element.super.keyReleased(keyCode, scanCode, modifiers);
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.searching) {
         return false;
      } else if (this.isOpen() && !this.client.player.isSpectator()) {
         if (this.searchField.charTyped(chr, modifiers)) {
            this.refreshSearchResults();
            return true;
         } else {
            return Element.super.charTyped(chr, modifiers);
         }
      } else {
         return false;
      }
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return false;
   }

   public void setFocused(boolean focused) {
   }

   public boolean isFocused() {
      return false;
   }

   private void refreshSearchResults() {
      String string = this.searchField.getText().toLowerCase(Locale.ROOT);
      this.triggerPirateSpeakEasterEgg(string);
      if (!string.equals(this.searchText)) {
         this.refreshResults(false);
         this.searchText = string;
      }

   }

   private void triggerPirateSpeakEasterEgg(String search) {
      if ("excitedze".equals(search)) {
         LanguageManager lv = this.client.getLanguageManager();
         String string2 = "en_pt";
         LanguageDefinition lv2 = lv.getLanguage("en_pt");
         if (lv2 == null || lv.getLanguage().equals("en_pt")) {
            return;
         }

         lv.setLanguage("en_pt");
         this.client.options.language = "en_pt";
         this.client.reloadResources();
         this.client.options.write();
      }

   }

   private boolean isWide() {
      return this.leftOffset == 86;
   }

   public void refresh() {
      this.refreshTabButtons();
      if (this.isOpen()) {
         this.refreshResults(false);
      }

   }

   public void onRecipesDisplayed(List recipes) {
      Iterator var2 = recipes.iterator();

      while(var2.hasNext()) {
         Recipe lv = (Recipe)var2.next();
         this.client.player.onRecipeDisplayed(lv);
      }

   }

   public void showGhostRecipe(Recipe recipe, List slots) {
      ItemStack lv = recipe.getOutput(this.client.world.getRegistryManager());
      this.ghostSlots.setRecipe(recipe);
      this.ghostSlots.addSlot(Ingredient.ofStacks(lv), ((Slot)slots.get(0)).x, ((Slot)slots.get(0)).y);
      this.alignRecipeToGrid(this.craftingScreenHandler.getCraftingWidth(), this.craftingScreenHandler.getCraftingHeight(), this.craftingScreenHandler.getCraftingResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
   }

   public void acceptAlignedInput(Iterator inputs, int slot, int amount, int gridX, int gridY) {
      Ingredient lv = (Ingredient)inputs.next();
      if (!lv.isEmpty()) {
         Slot lv2 = (Slot)this.craftingScreenHandler.slots.get(slot);
         this.ghostSlots.addSlot(lv, lv2.x, lv2.y);
      }

   }

   protected void sendBookDataPacket() {
      if (this.client.getNetworkHandler() != null) {
         RecipeBookCategory lv = this.craftingScreenHandler.getCategory();
         boolean bl = this.recipeBook.getOptions().isGuiOpen(lv);
         boolean bl2 = this.recipeBook.getOptions().isFilteringCraftable(lv);
         this.client.getNetworkHandler().sendPacket(new RecipeCategoryOptionsC2SPacket(lv, bl, bl2));
      }

   }

   public Selectable.SelectionType getType() {
      return this.open ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
   }

   public void appendNarrations(NarrationMessageBuilder builder) {
      List list = Lists.newArrayList();
      this.recipesArea.forEachButton((button) -> {
         if (button.isNarratable()) {
            list.add(button);
         }

      });
      list.add(this.searchField);
      list.add(this.toggleCraftableButton);
      list.addAll(this.tabButtons);
      Screen.SelectedElementNarrationData lv = Screen.findSelectedElementData(list, (Selectable)null);
      if (lv != null) {
         lv.selectable.appendNarrations(builder.nextMessage());
      }

   }

   static {
      SEARCH_HINT_TEXT = Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
      TOGGLE_CRAFTABLE_RECIPES_TEXT = Text.translatable("gui.recipebook.toggleRecipes.craftable");
      TOGGLE_ALL_RECIPES_TEXT = Text.translatable("gui.recipebook.toggleRecipes.all");
   }
}
