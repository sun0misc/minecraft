/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class CreditsAndAttributionScreen
extends Screen {
    private static final int SPACING = 8;
    private static final int BUTTON_WIDTH = 210;
    private static final Text TITLE = Text.translatable("credits_and_attribution.screen.title");
    private static final Text CREDITS_TEXT = Text.translatable("credits_and_attribution.button.credits");
    private static final Text ATTRIBUTION_TEXT = Text.translatable("credits_and_attribution.button.attribution");
    private static final Text LICENSE_TEXT = Text.translatable("credits_and_attribution.button.licenses");
    private final Screen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    public CreditsAndAttributionScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.layout.addHeader(TITLE, this.textRenderer);
        DirectionalLayoutWidget lv = this.layout.addBody(DirectionalLayoutWidget.vertical()).spacing(8);
        lv.getMainPositioner().alignHorizontalCenter();
        lv.add(ButtonWidget.builder(CREDITS_TEXT, button -> this.openCredits()).width(210).build());
        lv.add(ButtonWidget.builder(ATTRIBUTION_TEXT, ConfirmLinkScreen.opening(this, "https://aka.ms/MinecraftJavaAttribution")).width(210).build());
        lv.add(ButtonWidget.builder(LICENSE_TEXT, ConfirmLinkScreen.opening(this, "https://aka.ms/MinecraftJavaLicenses")).width(210).build());
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(200).build());
        this.layout.refreshPositions();
        this.layout.forEachChild(this::addDrawableChild);
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
    }

    private void openCredits() {
        this.client.setScreen(new CreditsScreen(false, () -> this.client.setScreen(this)));
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}

