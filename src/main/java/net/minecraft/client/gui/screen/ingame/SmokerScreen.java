/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.recipebook.SmokerRecipeBookScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SmokerScreen
extends AbstractFurnaceScreen<SmokerScreenHandler> {
    private static final Identifier LIT_PROGRESS_TEXTURE = Identifier.method_60656("container/smoker/lit_progress");
    private static final Identifier BURN_PROGRESS_TEXTURE = Identifier.method_60656("container/smoker/burn_progress");
    private static final Identifier TEXTURE = Identifier.method_60656("textures/gui/container/smoker.png");

    public SmokerScreen(SmokerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, new SmokerRecipeBookScreen(), inventory, title, TEXTURE, LIT_PROGRESS_TEXTURE, BURN_PROGRESS_TEXTURE);
    }
}

