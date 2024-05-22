/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.option;

import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.TelemetryEventWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TelemetryInfoScreen
extends Screen {
    private static final Text TITLE_TEXT = Text.translatable("telemetry_info.screen.title");
    private static final Text DESCRIPTION_TEXT = Text.translatable("telemetry_info.screen.description").withColor(Colors.ALTERNATE_WHITE);
    private static final Text PRIVACY_STATEMENT_TEXT = Text.translatable("telemetry_info.button.privacy_statement");
    private static final Text GIVE_FEEDBACK_TEXT = Text.translatable("telemetry_info.button.give_feedback");
    private static final Text SHOW_DATA_TEXT = Text.translatable("telemetry_info.button.show_data");
    private static final Text OPT_IN_DESCRIPTION_TEXT = Text.translatable("telemetry_info.opt_in.description");
    private static final int MARGIN = 8;
    private static final boolean OPTIONAL_TELEMETRY_ENABLED_BY_API = MinecraftClient.getInstance().isOptionalTelemetryEnabledByApi();
    private final Screen parent;
    private final GameOptions options;
    private final ThreePartsLayoutWidget layout;
    @Nullable
    private TelemetryEventWidget telemetryEventWidget;
    @Nullable
    private MultilineTextWidget textWidget;
    private double scroll;

    public TelemetryInfoScreen(Screen parent, GameOptions options) {
        super(TITLE_TEXT);
        this.layout = new ThreePartsLayoutWidget(this, 16 + MinecraftClient.getInstance().textRenderer.fontHeight * 5 + 20, OPTIONAL_TELEMETRY_ENABLED_BY_API ? 33 + CheckboxWidget.getSize(MinecraftClient.getInstance().textRenderer) : 33);
        this.parent = parent;
        this.options = options;
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), DESCRIPTION_TEXT);
    }

    @Override
    protected void init() {
        DirectionalLayoutWidget lv = this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(4));
        lv.getMainPositioner().alignHorizontalCenter();
        lv.add(new TextWidget(TITLE_TEXT, this.textRenderer));
        this.textWidget = lv.add(new MultilineTextWidget(DESCRIPTION_TEXT, this.textRenderer).setCentered(true));
        DirectionalLayoutWidget lv2 = lv.add(DirectionalLayoutWidget.horizontal().spacing(8));
        lv2.add(ButtonWidget.builder(PRIVACY_STATEMENT_TEXT, this::openPrivacyStatementPage).build());
        lv2.add(ButtonWidget.builder(GIVE_FEEDBACK_TEXT, this::openFeedbackPage).build());
        DirectionalLayoutWidget lv3 = this.layout.addFooter(DirectionalLayoutWidget.vertical().spacing(4));
        if (OPTIONAL_TELEMETRY_ENABLED_BY_API) {
            lv3.add(this.createOptInCheckbox());
        }
        DirectionalLayoutWidget lv4 = lv3.add(DirectionalLayoutWidget.horizontal().spacing(8));
        lv4.add(ButtonWidget.builder(SHOW_DATA_TEXT, this::openLogDirectory).build());
        lv4.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build());
        DirectionalLayoutWidget lv5 = this.layout.addBody(DirectionalLayoutWidget.vertical().spacing(8));
        this.telemetryEventWidget = lv5.add(new TelemetryEventWidget(0, 0, this.width - 40, this.layout.getContentHeight(), this.textRenderer));
        this.telemetryEventWidget.setScrollConsumer(scroll -> {
            this.scroll = scroll;
        });
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        if (this.telemetryEventWidget != null) {
            this.telemetryEventWidget.setScrollY(this.scroll);
            this.telemetryEventWidget.setWidth(this.width - 40);
            this.telemetryEventWidget.setHeight(this.layout.getContentHeight());
            this.telemetryEventWidget.initContents();
        }
        if (this.textWidget != null) {
            this.textWidget.setMaxWidth(this.width - 16);
        }
        this.layout.refreshPositions();
    }

    @Override
    protected void setInitialFocus() {
        if (this.telemetryEventWidget != null) {
            this.setInitialFocus(this.telemetryEventWidget);
        }
    }

    private ClickableWidget createOptInCheckbox() {
        SimpleOption<Boolean> lv = this.options.getTelemetryOptInExtra();
        return CheckboxWidget.builder(OPT_IN_DESCRIPTION_TEXT, this.textRenderer).option(lv).callback(this::updateOptIn).build();
    }

    private void updateOptIn(ClickableWidget checkbox, boolean checked) {
        if (this.telemetryEventWidget != null) {
            this.telemetryEventWidget.refresh(checked);
        }
    }

    private void openPrivacyStatementPage(ButtonWidget button) {
        ConfirmLinkScreen.open(this, "http://go.microsoft.com/fwlink/?LinkId=521839");
    }

    private void openFeedbackPage(ButtonWidget button) {
        ConfirmLinkScreen.open(this, "https://aka.ms/javafeedback?ref=game");
    }

    private void openLogDirectory(ButtonWidget button) {
        Path path = this.client.getTelemetryManager().getLogManager();
        Util.getOperatingSystem().open(path.toUri());
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}

