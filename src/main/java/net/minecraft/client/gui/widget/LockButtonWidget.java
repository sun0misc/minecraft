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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class LockButtonWidget
extends ButtonWidget {
    private boolean locked;

    public LockButtonWidget(int x, int y, ButtonWidget.PressAction action) {
        super(x, y, 20, 20, Text.translatable("narrator.button.difficulty_lock"), action, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected MutableText getNarrationMessage() {
        return ScreenTexts.joinSentences(super.getNarrationMessage(), this.isLocked() ? Text.translatable("narrator.button.difficulty_lock.locked") : Text.translatable("narrator.button.difficulty_lock.unlocked"));
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Icon lv = !this.active ? (this.locked ? Icon.LOCKED_DISABLED : Icon.UNLOCKED_DISABLED) : (this.isSelected() ? (this.locked ? Icon.LOCKED_HOVER : Icon.UNLOCKED_HOVER) : (this.locked ? Icon.LOCKED : Icon.UNLOCKED));
        context.drawGuiTexture(lv.texture, this.getX(), this.getY(), this.width, this.height);
    }

    @Environment(value=EnvType.CLIENT)
    static enum Icon {
        LOCKED(Identifier.method_60656("widget/locked_button")),
        LOCKED_HOVER(Identifier.method_60656("widget/locked_button_highlighted")),
        LOCKED_DISABLED(Identifier.method_60656("widget/locked_button_disabled")),
        UNLOCKED(Identifier.method_60656("widget/unlocked_button")),
        UNLOCKED_HOVER(Identifier.method_60656("widget/unlocked_button_highlighted")),
        UNLOCKED_DISABLED(Identifier.method_60656("widget/unlocked_button_disabled"));

        final Identifier texture;

        private Icon(Identifier texture) {
            this.texture = texture;
        }
    }
}

