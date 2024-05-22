/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen;

import java.net.URI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9812;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class DisconnectedScreen
extends Screen {
    private static final Text TO_MENU_TEXT = Text.translatable("gui.toMenu");
    private static final Text TO_TITLE_TEXT = Text.translatable("gui.toTitle");
    private static final Text field_52129 = Text.translatable("gui.report_to_server");
    private static final Text field_52130 = Text.translatable("gui.open_report_dir");
    private final Screen parent;
    private final class_9812 field_52131;
    private final Text buttonLabel;
    private final DirectionalLayoutWidget grid = DirectionalLayoutWidget.vertical();

    public DisconnectedScreen(Screen arg, Text arg2, Text arg3) {
        this(arg, arg2, new class_9812(arg3));
    }

    public DisconnectedScreen(Screen arg, Text arg2, Text arg3, Text arg4) {
        this(arg, arg2, new class_9812(arg3), arg4);
    }

    public DisconnectedScreen(Screen parent, Text title, class_9812 arg3) {
        this(parent, title, arg3, TO_MENU_TEXT);
    }

    public DisconnectedScreen(Screen parent, Text title, class_9812 arg3, Text buttonLabel) {
        super(title);
        this.parent = parent;
        this.field_52131 = arg3;
        this.buttonLabel = buttonLabel;
    }

    @Override
    protected void init() {
        this.grid.getMainPositioner().alignHorizontalCenter().margin(10);
        this.grid.add(new TextWidget(this.title, this.textRenderer));
        this.grid.add(new MultilineTextWidget(this.field_52131.reason(), this.textRenderer).setMaxWidth(this.width - 50).setCentered(true));
        this.grid.getMainPositioner().margin(2);
        this.field_52131.bugReportLink().ifPresent(string -> this.grid.add(ButtonWidget.builder(field_52129, ConfirmLinkScreen.method_60867(this, string, false)).width(200).build()));
        this.field_52131.report().ifPresent(path -> {
            URI uRI = path.getParent().toUri();
            this.grid.add(ButtonWidget.builder(field_52130, arg -> Util.getOperatingSystem().open(uRI)).width(200).build());
        });
        ButtonWidget lv = this.client.isMultiplayerEnabled() ? ButtonWidget.builder(this.buttonLabel, button -> this.client.setScreen(this.parent)).width(200).build() : ButtonWidget.builder(TO_TITLE_TEXT, button -> this.client.setScreen(new TitleScreen())).width(200).build();
        this.grid.add(lv);
        this.grid.refreshPositions();
        this.grid.forEachChild(this::addDrawableChild);
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        SimplePositioningWidget.setPos(this.grid, this.getNavigationFocus());
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(this.title, this.field_52131.reason());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}

