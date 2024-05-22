/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class IconWidget
extends ClickableWidget {
    IconWidget(int x, int y, int width, int height) {
        super(x, y, width, height, ScreenTexts.EMPTY);
    }

    public static IconWidget create(int width, int height, Identifier texture, int textureWidth, int textureHeight) {
        return new Texture(0, 0, width, height, texture, textureWidth, textureHeight);
    }

    public static IconWidget create(int width, int height, Identifier texture) {
        return new Simple(0, 0, width, height, texture);
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

    @Environment(value=EnvType.CLIENT)
    static class Texture
    extends IconWidget {
        private final Identifier texture;
        private final int textureWidth;
        private final int textureHeight;

        public Texture(int x, int y, int width, int height, Identifier texture, int textureWidth, int textureHeight) {
            super(x, y, width, height);
            this.texture = texture;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawTexture(this.texture, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0.0f, 0.0f, this.getWidth(), this.getHeight(), this.textureWidth, this.textureHeight);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Simple
    extends IconWidget {
        private final Identifier texture;

        public Simple(int x, int y, int width, int height, Identifier texture) {
            super(x, y, width, height);
            this.texture = texture;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawGuiTexture(this.texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
    }
}

