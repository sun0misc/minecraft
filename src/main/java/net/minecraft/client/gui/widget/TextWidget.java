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
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

@Environment(value=EnvType.CLIENT)
public class TextWidget
extends AbstractTextWidget {
    private float horizontalAlignment = 0.5f;

    public TextWidget(Text message, TextRenderer textRenderer) {
        this(0, 0, textRenderer.getWidth(message.asOrderedText()), textRenderer.fontHeight, message, textRenderer);
    }

    public TextWidget(int width, int height, Text message, TextRenderer textRenderer) {
        this(0, 0, width, height, message, textRenderer);
    }

    public TextWidget(int x, int y, int width, int height, Text message, TextRenderer textRenderer) {
        super(x, y, width, height, message, textRenderer);
        this.active = false;
    }

    @Override
    public TextWidget setTextColor(int textColor) {
        super.setTextColor(textColor);
        return this;
    }

    private TextWidget align(float horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    public TextWidget alignLeft() {
        return this.align(0.0f);
    }

    public TextWidget alignCenter() {
        return this.align(0.5f);
    }

    public TextWidget alignRight() {
        return this.align(1.0f);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Text lv = this.getMessage();
        TextRenderer lv2 = this.getTextRenderer();
        int k = this.getWidth();
        int l = lv2.getWidth(lv);
        int m = this.getX() + Math.round(this.horizontalAlignment * (float)(k - l));
        int n = this.getY() + (this.getHeight() - lv2.fontHeight) / 2;
        OrderedText lv3 = l > k ? this.trim(lv, k) : lv.asOrderedText();
        context.drawTextWithShadow(lv2, lv3, m, n, this.getTextColor());
    }

    private OrderedText trim(Text text, int width) {
        TextRenderer lv = this.getTextRenderer();
        StringVisitable lv2 = lv.trimToWidth(text, width - lv.getWidth(ScreenTexts.ELLIPSIS));
        return Language.getInstance().reorder(StringVisitable.concat(lv2, ScreenTexts.ELLIPSIS));
    }

    @Override
    public /* synthetic */ AbstractTextWidget setTextColor(int textColor) {
        return this.setTextColor(textColor);
    }
}

