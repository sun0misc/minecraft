/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.multiplayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(value=EnvType.CLIENT)
public class MultiplayerWarningScreen
extends WarningScreen {
    private static final Text HEADER = Text.translatable("multiplayerWarning.header").formatted(Formatting.BOLD);
    private static final Text MESSAGE = Text.translatable("multiplayerWarning.message");
    private static final Text CHECK_MESSAGE = Text.translatable("multiplayerWarning.check");
    private static final Text NARRATED_TEXT = HEADER.copy().append("\n").append(MESSAGE);
    private final Screen parent;

    public MultiplayerWarningScreen(Screen parent) {
        super(HEADER, MESSAGE, CHECK_MESSAGE, NARRATED_TEXT);
        this.parent = parent;
    }

    @Override
    protected LayoutWidget getLayout() {
        DirectionalLayoutWidget lv = DirectionalLayoutWidget.horizontal().spacing(8);
        lv.add(ButtonWidget.builder(ScreenTexts.PROCEED, arg -> {
            if (this.checkbox.isChecked()) {
                this.client.options.skipMultiplayerWarning = true;
                this.client.options.write();
            }
            this.client.setScreen(new MultiplayerScreen(this.parent));
        }).build());
        lv.add(ButtonWidget.builder(ScreenTexts.BACK, arg -> this.close()).build());
        return lv;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}

