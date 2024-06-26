/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ToggleButtonWidget
extends ClickableWidget {
    @Nullable
    protected ButtonTextures textures;
    protected boolean toggled;

    public ToggleButtonWidget(int x, int y, int width, int height, boolean toggled) {
        super(x, y, width, height, ScreenTexts.EMPTY);
        this.toggled = toggled;
    }

    public void setTextures(ButtonTextures textures) {
        this.textures = textures;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return this.toggled;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.textures == null) {
            return;
        }
        RenderSystem.disableDepthTest();
        context.drawGuiTexture(this.textures.get(this.toggled, this.isSelected()), this.getX(), this.getY(), this.width, this.height);
        RenderSystem.enableDepthTest();
    }
}

