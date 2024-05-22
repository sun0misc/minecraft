/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.screen.recipebook.RecipeAlternativesWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeDisplayListener;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RecipeBookResults {
    public static final int field_32411 = 20;
    private static final ButtonTextures PAGE_FORWARD_TEXTURES = new ButtonTextures(Identifier.method_60656("recipe_book/page_forward"), Identifier.method_60656("recipe_book/page_forward_highlighted"));
    private static final ButtonTextures PAGE_BACKWARD_TEXTURES = new ButtonTextures(Identifier.method_60656("recipe_book/page_backward"), Identifier.method_60656("recipe_book/page_backward_highlighted"));
    private final List<AnimatedResultButton> resultButtons = Lists.newArrayListWithCapacity(20);
    @Nullable
    private AnimatedResultButton hoveredResultButton;
    private final RecipeAlternativesWidget alternatesWidget = new RecipeAlternativesWidget();
    private MinecraftClient client;
    private final List<RecipeDisplayListener> recipeDisplayListeners = Lists.newArrayList();
    private List<RecipeResultCollection> resultCollections = ImmutableList.of();
    private ToggleButtonWidget nextPageButton;
    private ToggleButtonWidget prevPageButton;
    private int pageCount;
    private int currentPage;
    private RecipeBook recipeBook;
    @Nullable
    private RecipeEntry<?> lastClickedRecipe;
    @Nullable
    private RecipeResultCollection resultCollection;

    public RecipeBookResults() {
        for (int i = 0; i < 20; ++i) {
            this.resultButtons.add(new AnimatedResultButton());
        }
    }

    public void initialize(MinecraftClient client, int parentLeft, int parentTop) {
        this.client = client;
        this.recipeBook = client.player.getRecipeBook();
        for (int k = 0; k < this.resultButtons.size(); ++k) {
            this.resultButtons.get(k).setPosition(parentLeft + 11 + 25 * (k % 5), parentTop + 31 + 25 * (k / 5));
        }
        this.nextPageButton = new ToggleButtonWidget(parentLeft + 93, parentTop + 137, 12, 17, false);
        this.nextPageButton.setTextures(PAGE_FORWARD_TEXTURES);
        this.prevPageButton = new ToggleButtonWidget(parentLeft + 38, parentTop + 137, 12, 17, true);
        this.prevPageButton.setTextures(PAGE_BACKWARD_TEXTURES);
    }

    public void setGui(RecipeBookWidget widget) {
        this.recipeDisplayListeners.remove(widget);
        this.recipeDisplayListeners.add(widget);
    }

    public void setResults(List<RecipeResultCollection> resultCollections, boolean resetCurrentPage) {
        this.resultCollections = resultCollections;
        this.pageCount = (int)Math.ceil((double)resultCollections.size() / 20.0);
        if (this.pageCount <= this.currentPage || resetCurrentPage) {
            this.currentPage = 0;
        }
        this.refreshResultButtons();
    }

    private void refreshResultButtons() {
        int i = 20 * this.currentPage;
        for (int j = 0; j < this.resultButtons.size(); ++j) {
            AnimatedResultButton lv = this.resultButtons.get(j);
            if (i + j < this.resultCollections.size()) {
                RecipeResultCollection lv2 = this.resultCollections.get(i + j);
                lv.showResultCollection(lv2, this);
                lv.visible = true;
                continue;
            }
            lv.visible = false;
        }
        this.hideShowPageButtons();
    }

    private void hideShowPageButtons() {
        this.nextPageButton.visible = this.pageCount > 1 && this.currentPage < this.pageCount - 1;
        this.prevPageButton.visible = this.pageCount > 1 && this.currentPage > 0;
    }

    public void draw(DrawContext context, int x, int y, int mouseX, int mouseY, float delta) {
        if (this.pageCount > 1) {
            MutableText lv = Text.translatable("gui.recipebook.page", this.currentPage + 1, this.pageCount);
            int m = this.client.textRenderer.getWidth(lv);
            context.drawText(this.client.textRenderer, lv, x - m / 2 + 73, y + 141, Colors.WHITE, false);
        }
        this.hoveredResultButton = null;
        for (AnimatedResultButton lv2 : this.resultButtons) {
            lv2.render(context, mouseX, mouseY, delta);
            if (!lv2.visible || !lv2.isSelected()) continue;
            this.hoveredResultButton = lv2;
        }
        this.prevPageButton.render(context, mouseX, mouseY, delta);
        this.nextPageButton.render(context, mouseX, mouseY, delta);
        this.alternatesWidget.render(context, mouseX, mouseY, delta);
    }

    public void drawTooltip(DrawContext context, int x, int y) {
        if (this.client.currentScreen != null && this.hoveredResultButton != null && !this.alternatesWidget.isVisible()) {
            context.drawTooltip(this.client.textRenderer, this.hoveredResultButton.getTooltip(), x, y);
        }
    }

    @Nullable
    public RecipeEntry<?> getLastClickedRecipe() {
        return this.lastClickedRecipe;
    }

    @Nullable
    public RecipeResultCollection getLastClickedResults() {
        return this.resultCollection;
    }

    public void hideAlternates() {
        this.alternatesWidget.setVisible(false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int areaLeft, int areaTop, int areaWidth, int areaHeight) {
        this.lastClickedRecipe = null;
        this.resultCollection = null;
        if (this.alternatesWidget.isVisible()) {
            if (this.alternatesWidget.mouseClicked(mouseX, mouseY, button)) {
                this.lastClickedRecipe = this.alternatesWidget.getLastClickedRecipe();
                this.resultCollection = this.alternatesWidget.getResults();
            } else {
                this.alternatesWidget.setVisible(false);
            }
            return true;
        }
        if (this.nextPageButton.mouseClicked(mouseX, mouseY, button)) {
            ++this.currentPage;
            this.refreshResultButtons();
            return true;
        }
        if (this.prevPageButton.mouseClicked(mouseX, mouseY, button)) {
            --this.currentPage;
            this.refreshResultButtons();
            return true;
        }
        for (AnimatedResultButton lv : this.resultButtons) {
            if (!lv.mouseClicked(mouseX, mouseY, button)) continue;
            if (button == 0) {
                this.lastClickedRecipe = lv.currentRecipe();
                this.resultCollection = lv.getResultCollection();
            } else if (button == 1 && !this.alternatesWidget.isVisible() && !lv.hasResults()) {
                this.alternatesWidget.showAlternativesForResult(this.client, lv.getResultCollection(), lv.getX(), lv.getY(), areaLeft + areaWidth / 2, areaTop + 13 + areaHeight / 2, lv.getWidth());
            }
            return true;
        }
        return false;
    }

    public void onRecipesDisplayed(List<RecipeEntry<?>> recipes) {
        for (RecipeDisplayListener lv : this.recipeDisplayListeners) {
            lv.onRecipesDisplayed(recipes);
        }
    }

    public MinecraftClient getClient() {
        return this.client;
    }

    public RecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    protected void forEachButton(Consumer<ClickableWidget> consumer) {
        consumer.accept(this.nextPageButton);
        consumer.accept(this.prevPageButton);
        this.resultButtons.forEach(consumer);
    }
}

