/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class TabButtonWidget
extends ClickableWidget {
    private static final ButtonTextures TAB_BUTTON_TEXTURES = new ButtonTextures(Identifier.method_60656("widget/tab_selected"), Identifier.method_60656("widget/tab"), Identifier.method_60656("widget/tab_selected_highlighted"), Identifier.method_60656("widget/tab_highlighted"));
    private static final int field_43063 = 3;
    private static final int field_43064 = 1;
    private static final int field_43065 = 1;
    private static final int field_43066 = 4;
    private static final int field_43067 = 2;
    private final TabManager tabManager;
    private final Tab tab;

    public TabButtonWidget(TabManager tabManager, Tab tab, int width, int height) {
        super(0, 0, width, height, tab.getTitle());
        this.tabManager = tabManager;
        this.tab = tab;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.enableBlend();
        context.drawGuiTexture(TAB_BUTTON_TEXTURES.get(this.isCurrentTab(), this.isSelected()), this.getX(), this.getY(), this.width, this.height);
        RenderSystem.disableBlend();
        TextRenderer lv = MinecraftClient.getInstance().textRenderer;
        int k = this.active ? -1 : -6250336;
        this.drawMessage(context, lv, k);
        if (this.isCurrentTab()) {
            this.renderBackgroundTexture(context, this.getX() + 2, this.getY() + 2, this.getRight() - 2, this.getBottom());
            this.drawCurrentTabLine(context, lv, k);
        }
    }

    protected void renderBackgroundTexture(DrawContext context, int left, int top, int right, int bottom) {
        Screen.renderBackgroundTexture(context, Screen.MENU_BACKGROUND_TEXTURE, left, top, 0.0f, 0.0f, right - left, bottom - top);
    }

    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        int j = this.getX() + 1;
        int k = this.getY() + (this.isCurrentTab() ? 0 : 3);
        int l = this.getX() + this.getWidth() - 1;
        int m = this.getY() + this.getHeight();
        TabButtonWidget.drawScrollableText(context, textRenderer, this.getMessage(), j, k, l, m, color);
    }

    private void drawCurrentTabLine(DrawContext context, TextRenderer textRenderer, int color) {
        int j = Math.min(textRenderer.getWidth(this.getMessage()), this.getWidth() - 4);
        int k = this.getX() + (this.getWidth() - j) / 2;
        int l = this.getY() + this.getHeight() - 2;
        context.fill(k, l, k + j, l + 1, color);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, (Text)Text.translatable("gui.narrate.tab", this.tab.getTitle()));
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    public Tab getTab() {
        return this.tab;
    }

    public boolean isCurrentTab() {
        return this.tabManager.getCurrentTab() == this.tab;
    }
}

