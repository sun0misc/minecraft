/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class PressableTextWidget
extends ButtonWidget {
    private final TextRenderer textRenderer;
    private final Text text;
    private final Text hoverText;

    public PressableTextWidget(int x, int y, int width, int height, Text text, ButtonWidget.PressAction onPress, TextRenderer textRenderer) {
        super(x, y, width, height, text, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.textRenderer = textRenderer;
        this.text = text;
        this.hoverText = Texts.setStyleIfAbsent(text.copy(), Style.EMPTY.withUnderline(true));
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Text lv = this.isSelected() ? this.hoverText : this.text;
        context.drawTextWithShadow(this.textRenderer, lv, this.getX(), this.getY(), 0xFFFFFF | MathHelper.ceil(this.alpha * 255.0f) << 24);
    }
}

