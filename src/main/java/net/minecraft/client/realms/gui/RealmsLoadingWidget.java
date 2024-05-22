/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsLoadingWidget
extends ClickableWidget {
    private final TextRenderer textRenderer;

    public RealmsLoadingWidget(TextRenderer textRenderer, Text message) {
        super(0, 0, textRenderer.getWidth(message), textRenderer.fontHeight * 3, message);
        this.textRenderer = textRenderer;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int k = this.getX() + this.getWidth() / 2;
        int l = this.getY() + this.getHeight() / 2;
        Text lv = this.getMessage();
        context.drawText(this.textRenderer, lv, k - this.textRenderer.getWidth(lv) / 2, l - this.textRenderer.fontHeight, Colors.WHITE, false);
        String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
        context.drawText(this.textRenderer, string, k - this.textRenderer.getWidth(string) / 2, l + this.textRenderer.fontHeight, Colors.GRAY, false);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public boolean isNarratable() {
        return false;
    }

    @Override
    @Nullable
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return null;
    }
}

