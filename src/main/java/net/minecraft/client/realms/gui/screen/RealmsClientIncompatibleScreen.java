/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

@Environment(value=EnvType.CLIENT)
public class RealmsClientIncompatibleScreen
extends RealmsScreen {
    private static final Text INCOMPATIBLE_TITLE = Text.translatable("mco.client.incompatible.title").withColor(Colors.RED);
    private static final Text GAME_VERSION = Text.literal(SharedConstants.getGameVersion().getName()).withColor(Colors.RED);
    private static final Text UNSUPPORTED_SNAPSHOT_VERSION = Text.translatable("mco.client.unsupported.snapshot.version", GAME_VERSION);
    private static final Text OUTDATED_STABLE_VERSION = Text.translatable("mco.client.outdated.stable.version", GAME_VERSION);
    private final Screen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    public RealmsClientIncompatibleScreen(Screen parent) {
        super(INCOMPATIBLE_TITLE);
        this.parent = parent;
    }

    @Override
    public void init() {
        this.layout.addHeader(INCOMPATIBLE_TITLE, this.textRenderer);
        this.layout.addBody(new MultilineTextWidget(this.getErrorText(), this.textRenderer).setCentered(true));
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.BACK, arg -> this.close()).width(200).build());
        this.layout.forEachChild(arg2 -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(arg2);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private Text getErrorText() {
        if (SharedConstants.getGameVersion().isStable()) {
            return OUTDATED_STABLE_VERSION;
        }
        return UNSUPPORTED_SNAPSHOT_VERSION;
    }
}

