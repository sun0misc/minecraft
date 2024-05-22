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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.FontOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class LanguageOptionsScreen
extends GameOptionsScreen {
    private static final Text LANGUAGE_WARNING_TEXT = Text.translatable("options.languageAccuracyWarning").formatted(Formatting.GRAY);
    private static final int field_49497 = 53;
    private LanguageSelectionListWidget languageSelectionList;
    final LanguageManager languageManager;

    public LanguageOptionsScreen(Screen parent, GameOptions options, LanguageManager languageManager) {
        super(parent, options, Text.translatable("options.language.title"));
        this.languageManager = languageManager;
        this.layout.setFooterHeight(53);
    }

    @Override
    protected void initBody() {
        this.languageSelectionList = this.layout.addBody(new LanguageSelectionListWidget(this.client));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void initFooter() {
        DirectionalLayoutWidget lv = this.layout.addFooter(DirectionalLayoutWidget.vertical()).spacing(8);
        lv.getMainPositioner().alignHorizontalCenter();
        lv.add(new TextWidget(LANGUAGE_WARNING_TEXT, this.textRenderer));
        DirectionalLayoutWidget lv2 = lv.add(DirectionalLayoutWidget.horizontal().spacing(8));
        lv2.add(ButtonWidget.builder(Text.translatable("options.font"), button -> this.client.setScreen(new FontOptionsScreen(this, this.gameOptions))).build());
        lv2.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.onDone()).build());
    }

    @Override
    protected void initTabNavigation() {
        super.initTabNavigation();
        this.languageSelectionList.position(this.width, this.layout);
    }

    void onDone() {
        LanguageSelectionListWidget.LanguageEntry lv = (LanguageSelectionListWidget.LanguageEntry)this.languageSelectionList.getSelectedOrNull();
        if (lv != null && !lv.languageCode.equals(this.languageManager.getLanguage())) {
            this.languageManager.setLanguage(lv.languageCode);
            this.gameOptions.language = lv.languageCode;
            this.client.reloadResources();
        }
        this.client.setScreen(this.parent);
    }

    @Environment(value=EnvType.CLIENT)
    class LanguageSelectionListWidget
    extends AlwaysSelectedEntryListWidget<LanguageEntry> {
        public LanguageSelectionListWidget(MinecraftClient client) {
            super(client, LanguageOptionsScreen.this.width, LanguageOptionsScreen.this.height - 33 - 53, 33, 18);
            String string = LanguageOptionsScreen.this.languageManager.getLanguage();
            LanguageOptionsScreen.this.languageManager.getAllLanguages().forEach((languageCode, languageDefinition) -> {
                LanguageEntry lv = new LanguageEntry((String)languageCode, (LanguageDefinition)languageDefinition);
                this.addEntry(lv);
                if (string.equals(languageCode)) {
                    this.setSelected(lv);
                }
            });
            if (this.getSelectedOrNull() != null) {
                this.centerScrollOn((LanguageEntry)this.getSelectedOrNull());
            }
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @Environment(value=EnvType.CLIENT)
        public class LanguageEntry
        extends AlwaysSelectedEntryListWidget.Entry<LanguageEntry> {
            final String languageCode;
            private final Text languageDefinition;
            private long clickTime;

            public LanguageEntry(String languageCode, LanguageDefinition languageDefinition) {
                this.languageCode = languageCode;
                this.languageDefinition = languageDefinition.getDisplayText();
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawCenteredTextWithShadow(LanguageOptionsScreen.this.textRenderer, this.languageDefinition, LanguageSelectionListWidget.this.width / 2, y + 1, Colors.WHITE);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (KeyCodes.isToggle(keyCode)) {
                    this.onPressed();
                    LanguageOptionsScreen.this.onDone();
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.onPressed();
                if (Util.getMeasuringTimeMs() - this.clickTime < 250L) {
                    LanguageOptionsScreen.this.onDone();
                }
                this.clickTime = Util.getMeasuringTimeMs();
                return super.mouseClicked(mouseX, mouseY, button);
            }

            private void onPressed() {
                LanguageSelectionListWidget.this.setSelected(this);
            }

            @Override
            public Text getNarration() {
                return Text.translatable("narrator.select", this.languageDefinition);
            }
        }
    }
}

