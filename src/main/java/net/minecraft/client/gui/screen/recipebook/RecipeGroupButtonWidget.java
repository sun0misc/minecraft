/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class RecipeGroupButtonWidget
extends ToggleButtonWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.method_60656("recipe_book/tab"), Identifier.method_60656("recipe_book/tab_selected"));
    private final RecipeBookGroup category;
    private static final float field_32412 = 15.0f;
    private float bounce;

    public RecipeGroupButtonWidget(RecipeBookGroup category) {
        super(0, 0, 35, 27, false);
        this.category = category;
        this.setTextures(TEXTURES);
    }

    public void checkForNewRecipes(MinecraftClient client) {
        ClientRecipeBook lv = client.player.getRecipeBook();
        List<RecipeResultCollection> list = lv.getResultsForGroup(this.category);
        if (!(client.player.currentScreenHandler instanceof AbstractRecipeScreenHandler)) {
            return;
        }
        for (RecipeResultCollection lv2 : list) {
            for (RecipeEntry<?> lv3 : lv2.getResults(lv.isFilteringCraftable((AbstractRecipeScreenHandler)client.player.currentScreenHandler))) {
                if (!lv.shouldDisplay(lv3)) continue;
                this.bounce = 15.0f;
                return;
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.textures == null) {
            return;
        }
        if (this.bounce > 0.0f) {
            float g = 1.0f + 0.1f * (float)Math.sin(this.bounce / 15.0f * (float)Math.PI);
            context.getMatrices().push();
            context.getMatrices().translate(this.getX() + 8, this.getY() + 12, 0.0f);
            context.getMatrices().scale(1.0f, g, 1.0f);
            context.getMatrices().translate(-(this.getX() + 8), -(this.getY() + 12), 0.0f);
        }
        MinecraftClient lv = MinecraftClient.getInstance();
        RenderSystem.disableDepthTest();
        Identifier lv2 = this.textures.get(true, this.toggled);
        int k = this.getX();
        if (this.toggled) {
            k -= 2;
        }
        context.drawGuiTexture(lv2, k, this.getY(), this.width, this.height);
        RenderSystem.enableDepthTest();
        this.renderIcons(context, lv.getItemRenderer());
        if (this.bounce > 0.0f) {
            context.getMatrices().pop();
            this.bounce -= delta;
        }
    }

    private void renderIcons(DrawContext context, ItemRenderer itemRenderer) {
        int i;
        List<ItemStack> list = this.category.getIcons();
        int n = i = this.toggled ? -2 : 0;
        if (list.size() == 1) {
            context.drawItemWithoutEntity(list.get(0), this.getX() + 9 + i, this.getY() + 5);
        } else if (list.size() == 2) {
            context.drawItemWithoutEntity(list.get(0), this.getX() + 3 + i, this.getY() + 5);
            context.drawItemWithoutEntity(list.get(1), this.getX() + 14 + i, this.getY() + 5);
        }
    }

    public RecipeBookGroup getCategory() {
        return this.category;
    }

    public boolean hasKnownRecipes(ClientRecipeBook recipeBook) {
        List<RecipeResultCollection> list = recipeBook.getResultsForGroup(this.category);
        this.visible = false;
        if (list != null) {
            for (RecipeResultCollection lv : list) {
                if (!lv.isInitialized() || !lv.hasFittingRecipes()) continue;
                this.visible = true;
                break;
            }
        }
        return this.visible;
    }
}

