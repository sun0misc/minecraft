/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatOptionsScreen
extends GameOptionsScreen {
    private static final Text TITLE_TEXT = Text.translatable("options.chat.title");
    @Nullable
    private ClickableWidget narratorButton;

    private static SimpleOption<?>[] getOptions(GameOptions gameOptions) {
        return new SimpleOption[]{gameOptions.getChatVisibility(), gameOptions.getChatColors(), gameOptions.getChatLinks(), gameOptions.getChatLinksPrompt(), gameOptions.getChatOpacity(), gameOptions.getTextBackgroundOpacity(), gameOptions.getChatScale(), gameOptions.getChatLineSpacing(), gameOptions.getChatDelay(), gameOptions.getChatWidth(), gameOptions.getChatHeightFocused(), gameOptions.getChatHeightUnfocused(), gameOptions.getNarrator(), gameOptions.getAutoSuggestions(), gameOptions.getHideMatchedNames(), gameOptions.getReducedDebugInfo(), gameOptions.getOnlyShowSecureChat()};
    }

    public ChatOptionsScreen(Screen parent, GameOptions options) {
        super(parent, options, TITLE_TEXT);
    }

    @Override
    public void init() {
        super.init();
        this.narratorButton = this.body.getWidgetFor(this.gameOptions.getNarrator());
        if (this.narratorButton != null) {
            this.narratorButton.active = this.client.getNarratorManager().isActive();
        }
    }

    @Override
    protected void addOptions() {
        this.body.addAll(ChatOptionsScreen.getOptions(this.gameOptions));
    }

    public void refreshNarratorOption() {
        if (this.narratorButton instanceof CyclingButtonWidget) {
            ((CyclingButtonWidget)this.narratorButton).setValue(this.gameOptions.getNarrator().getValue());
        }
    }
}

