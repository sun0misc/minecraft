/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.math.Fraction;

@Environment(value=EnvType.CLIENT)
public class BundleTooltipComponent
implements TooltipComponent {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.method_60656("container/bundle/background");
    private static final int field_32381 = 4;
    private static final int field_32382 = 1;
    private static final int WIDTH_PER_COLUMN = 18;
    private static final int HEIGHT_PER_ROW = 20;
    private final BundleContentsComponent field_49537;

    public BundleTooltipComponent(BundleContentsComponent data) {
        this.field_49537 = data;
    }

    @Override
    public int getHeight() {
        return this.getRowsHeight() + 4;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.getColumnsWidth();
    }

    private int getColumnsWidth() {
        return this.getColumns() * 18 + 2;
    }

    private int getRowsHeight() {
        return this.getRows() * 20 + 2;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        int k = this.getColumns();
        int l = this.getRows();
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, this.getColumnsWidth(), this.getRowsHeight());
        boolean bl = this.field_49537.getOccupancy().compareTo(Fraction.ONE) >= 0;
        int m = 0;
        for (int n = 0; n < l; ++n) {
            for (int o = 0; o < k; ++o) {
                int p = x + o * 18 + 1;
                int q = y + n * 20 + 1;
                this.drawSlot(p, q, m++, bl, context, textRenderer);
            }
        }
    }

    private void drawSlot(int x, int y, int index, boolean shouldBlock, DrawContext context, TextRenderer textRenderer) {
        if (index >= this.field_49537.size()) {
            this.draw(context, x, y, shouldBlock ? SlotSprite.BLOCKED_SLOT : SlotSprite.SLOT);
            return;
        }
        ItemStack lv = this.field_49537.get(index);
        this.draw(context, x, y, SlotSprite.SLOT);
        context.drawItem(lv, x + 1, y + 1, index);
        context.drawItemInSlot(textRenderer, lv, x + 1, y + 1);
        if (index == 0) {
            HandledScreen.drawSlotHighlight(context, x + 1, y + 1, 0);
        }
    }

    private void draw(DrawContext context, int x, int y, SlotSprite sprite) {
        context.drawGuiTexture(sprite.texture, x, y, 0, sprite.width, sprite.height);
    }

    private int getColumns() {
        return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.field_49537.size() + 1.0)));
    }

    private int getRows() {
        return (int)Math.ceil(((double)this.field_49537.size() + 1.0) / (double)this.getColumns());
    }

    @Environment(value=EnvType.CLIENT)
    static enum SlotSprite {
        BLOCKED_SLOT(Identifier.method_60656("container/bundle/blocked_slot"), 18, 20),
        SLOT(Identifier.method_60656("container/bundle/slot"), 18, 20);

        public final Identifier texture;
        public final int width;
        public final int height;

        private SlotSprite(Identifier texture, int width, int height) {
            this.texture = texture;
            this.width = width;
            this.height = height;
        }
    }
}

