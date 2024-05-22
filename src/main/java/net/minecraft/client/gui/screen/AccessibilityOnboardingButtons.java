/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class AccessibilityOnboardingButtons {
    public static TextIconButtonWidget createLanguageButton(int width, ButtonWidget.PressAction onPress, boolean hideText) {
        return TextIconButtonWidget.builder(Text.translatable("options.language"), onPress, hideText).width(width).texture(Identifier.method_60656("icon/language"), 15, 15).build();
    }

    public static TextIconButtonWidget createAccessibilityButton(int width, ButtonWidget.PressAction onPress, boolean hideText) {
        MutableText lv = hideText ? Text.translatable("options.accessibility") : Text.translatable("accessibility.onboarding.accessibility.button");
        return TextIconButtonWidget.builder(lv, onPress, hideText).width(width).texture(Identifier.method_60656("icon/accessibility"), 15, 15).build();
    }
}

