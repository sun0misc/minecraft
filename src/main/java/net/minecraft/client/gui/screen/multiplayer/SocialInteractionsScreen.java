/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.multiplayer;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SocialInteractionsScreen
extends Screen {
    private static final Text TITLE = Text.translatable("gui.socialInteractions.title");
    private static final Identifier BACKGROUND_TEXTURE = Identifier.method_60656("social_interactions/background");
    private static final Identifier SEARCH_ICON_TEXTURE = Identifier.method_60656("icon/search");
    private static final Text ALL_TAB_TITLE = Text.translatable("gui.socialInteractions.tab_all");
    private static final Text HIDDEN_TAB_TITLE = Text.translatable("gui.socialInteractions.tab_hidden");
    private static final Text BLOCKED_TAB_TITLE = Text.translatable("gui.socialInteractions.tab_blocked");
    private static final Text SELECTED_ALL_TAB_TITLE = ALL_TAB_TITLE.copyContentOnly().formatted(Formatting.UNDERLINE);
    private static final Text SELECTED_HIDDEN_TAB_TITLE = HIDDEN_TAB_TITLE.copyContentOnly().formatted(Formatting.UNDERLINE);
    private static final Text SELECTED_BLOCKED_TAB_TITLE = BLOCKED_TAB_TITLE.copyContentOnly().formatted(Formatting.UNDERLINE);
    private static final Text SEARCH_TEXT = Text.translatable("gui.socialInteractions.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
    static final Text EMPTY_SEARCH_TEXT = Text.translatable("gui.socialInteractions.search_empty").formatted(Formatting.GRAY);
    private static final Text EMPTY_HIDDEN_TEXT = Text.translatable("gui.socialInteractions.empty_hidden").formatted(Formatting.GRAY);
    private static final Text EMPTY_BLOCKED_TEXT = Text.translatable("gui.socialInteractions.empty_blocked").formatted(Formatting.GRAY);
    private static final Text BLOCKING_TEXT = Text.translatable("gui.socialInteractions.blocking_hint");
    private static final int field_32424 = 8;
    private static final int field_32426 = 236;
    private static final int field_32427 = 16;
    private static final int field_32428 = 64;
    public static final int field_32433 = 72;
    public static final int field_32432 = 88;
    private static final int field_32429 = 238;
    private static final int field_32430 = 20;
    private static final int field_32431 = 36;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    @Nullable
    private final Screen parent;
    SocialInteractionsPlayerListWidget playerList;
    TextFieldWidget searchBox;
    private String currentSearch = "";
    private Tab currentTab = Tab.ALL;
    private ButtonWidget allTabButton;
    private ButtonWidget hiddenTabButton;
    private ButtonWidget blockedTabButton;
    private ButtonWidget blockingButton;
    @Nullable
    private Text serverLabel;
    private int playerCount;

    public SocialInteractionsScreen() {
        this((Screen)null);
    }

    public SocialInteractionsScreen(@Nullable Screen parent) {
        super(TITLE);
        this.parent = parent;
        this.updateServerLabel(MinecraftClient.getInstance());
    }

    private int getScreenHeight() {
        return Math.max(52, this.height - 128 - 16);
    }

    private int getPlayerListBottom() {
        return 80 + this.getScreenHeight() - 8;
    }

    private int getSearchBoxX() {
        return (this.width - 238) / 2;
    }

    @Override
    public Text getNarratedTitle() {
        if (this.serverLabel != null) {
            return ScreenTexts.joinSentences(super.getNarratedTitle(), this.serverLabel);
        }
        return super.getNarratedTitle();
    }

    @Override
    protected void init() {
        this.layout.addHeader(TITLE, this.textRenderer);
        this.playerList = new SocialInteractionsPlayerListWidget(this, this.client, this.width, this.getPlayerListBottom() - 88, 88, 36);
        int i = this.playerList.getRowWidth() / 3;
        int j = this.playerList.getRowLeft();
        int k = this.playerList.getRowRight();
        this.allTabButton = this.addDrawableChild(ButtonWidget.builder(ALL_TAB_TITLE, button -> this.setCurrentTab(Tab.ALL)).dimensions(j, 45, i, 20).build());
        this.hiddenTabButton = this.addDrawableChild(ButtonWidget.builder(HIDDEN_TAB_TITLE, button -> this.setCurrentTab(Tab.HIDDEN)).dimensions((j + k - i) / 2 + 1, 45, i, 20).build());
        this.blockedTabButton = this.addDrawableChild(ButtonWidget.builder(BLOCKED_TAB_TITLE, button -> this.setCurrentTab(Tab.BLOCKED)).dimensions(k - i + 1, 45, i, 20).build());
        String string = this.searchBox != null ? this.searchBox.getText() : "";
        this.searchBox = new TextFieldWidget(this.textRenderer, this.getSearchBoxX() + 28, 74, 200, 15, SEARCH_TEXT){

            @Override
            protected MutableText getNarrationMessage() {
                if (!SocialInteractionsScreen.this.searchBox.getText().isEmpty() && SocialInteractionsScreen.this.playerList.isEmpty()) {
                    return super.getNarrationMessage().append(", ").append(EMPTY_SEARCH_TEXT);
                }
                return super.getNarrationMessage();
            }
        };
        this.searchBox.setMaxLength(16);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(-1);
        this.searchBox.setText(string);
        this.searchBox.setPlaceholder(SEARCH_TEXT);
        this.searchBox.setChangedListener(this::onSearchChange);
        this.addDrawableChild(this.searchBox);
        this.addSelectableChild(this.playerList);
        this.blockingButton = this.addDrawableChild(ButtonWidget.builder(BLOCKING_TEXT, ConfirmLinkScreen.opening(this, "https://aka.ms/javablocking")).dimensions(this.width / 2 - 100, 64 + this.getScreenHeight(), 200, 20).build());
        this.setCurrentTab(this.currentTab);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, arg -> this.close()).width(200).build());
        this.layout.forEachChild(arg2 -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(arg2);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
        this.playerList.position(this.width, this.getPlayerListBottom() - 88, 88);
        this.searchBox.setPosition(this.getSearchBoxX() + 28, 74);
        int i = this.playerList.getRowLeft();
        int j = this.playerList.getRowRight();
        int k = this.playerList.getRowWidth() / 3;
        this.allTabButton.setPosition(i, 45);
        this.hiddenTabButton.setPosition((i + j - k) / 2 + 1, 45);
        this.blockedTabButton.setPosition(j - k + 1, 45);
        this.blockingButton.setPosition(this.width / 2 - 100, 64 + this.getScreenHeight());
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void setCurrentTab(Tab currentTab) {
        this.currentTab = currentTab;
        this.allTabButton.setMessage(ALL_TAB_TITLE);
        this.hiddenTabButton.setMessage(HIDDEN_TAB_TITLE);
        this.blockedTabButton.setMessage(BLOCKED_TAB_TITLE);
        boolean bl = false;
        switch (currentTab.ordinal()) {
            case 0: {
                this.allTabButton.setMessage(SELECTED_ALL_TAB_TITLE);
                Collection<UUID> collection = this.client.player.networkHandler.getPlayerUuids();
                this.playerList.update(collection, this.playerList.getScrollAmount(), true);
                break;
            }
            case 1: {
                this.hiddenTabButton.setMessage(SELECTED_HIDDEN_TAB_TITLE);
                Set<UUID> set = this.client.getSocialInteractionsManager().getHiddenPlayers();
                bl = set.isEmpty();
                this.playerList.update(set, this.playerList.getScrollAmount(), false);
                break;
            }
            case 2: {
                this.blockedTabButton.setMessage(SELECTED_BLOCKED_TAB_TITLE);
                SocialInteractionsManager lv = this.client.getSocialInteractionsManager();
                Set<UUID> set2 = this.client.player.networkHandler.getPlayerUuids().stream().filter(lv::isPlayerBlocked).collect(Collectors.toSet());
                bl = set2.isEmpty();
                this.playerList.update(set2, this.playerList.getScrollAmount(), false);
            }
        }
        NarratorManager lv2 = this.client.getNarratorManager();
        if (!this.searchBox.getText().isEmpty() && this.playerList.isEmpty() && !this.searchBox.isFocused()) {
            lv2.narrate(EMPTY_SEARCH_TEXT);
        } else if (bl) {
            if (currentTab == Tab.HIDDEN) {
                lv2.narrate(EMPTY_HIDDEN_TEXT);
            } else if (currentTab == Tab.BLOCKED) {
                lv2.narrate(EMPTY_BLOCKED_TEXT);
            }
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int k = this.getSearchBoxX() + 3;
        context.drawGuiTexture(BACKGROUND_TEXTURE, k, 64, 236, this.getScreenHeight() + 16);
        context.drawGuiTexture(SEARCH_ICON_TEXTURE, k + 10, 76, 12, 12);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.updateServerLabel(this.client);
        if (this.serverLabel != null) {
            context.drawTextWithShadow(this.client.textRenderer, this.serverLabel, this.getSearchBoxX() + 8, 35, Colors.WHITE);
        }
        if (!this.playerList.isEmpty()) {
            this.playerList.render(context, mouseX, mouseY, delta);
        } else if (!this.searchBox.getText().isEmpty()) {
            context.drawCenteredTextWithShadow(this.client.textRenderer, EMPTY_SEARCH_TEXT, this.width / 2, (72 + this.getPlayerListBottom()) / 2, Colors.WHITE);
        } else if (this.currentTab == Tab.HIDDEN) {
            context.drawCenteredTextWithShadow(this.client.textRenderer, EMPTY_HIDDEN_TEXT, this.width / 2, (72 + this.getPlayerListBottom()) / 2, Colors.WHITE);
        } else if (this.currentTab == Tab.BLOCKED) {
            context.drawCenteredTextWithShadow(this.client.textRenderer, EMPTY_BLOCKED_TEXT, this.width / 2, (72 + this.getPlayerListBottom()) / 2, Colors.WHITE);
        }
        this.blockingButton.visible = this.currentTab == Tab.BLOCKED;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.searchBox.isFocused() && this.client.options.socialInteractionsKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void onSearchChange(String currentSearch) {
        if (!(currentSearch = currentSearch.toLowerCase(Locale.ROOT)).equals(this.currentSearch)) {
            this.playerList.setCurrentSearch(currentSearch);
            this.currentSearch = currentSearch;
            this.setCurrentTab(this.currentTab);
        }
    }

    private void updateServerLabel(MinecraftClient client) {
        int i = client.getNetworkHandler().getPlayerList().size();
        if (this.playerCount != i) {
            String string = "";
            ServerInfo lv = client.getCurrentServerEntry();
            if (client.isInSingleplayer()) {
                string = client.getServer().getServerMotd();
            } else if (lv != null) {
                string = lv.name;
            }
            this.serverLabel = i > 1 ? Text.translatable("gui.socialInteractions.server_label.multiple", string, i) : Text.translatable("gui.socialInteractions.server_label.single", string, i);
            this.playerCount = i;
        }
    }

    public void setPlayerOnline(PlayerListEntry player) {
        this.playerList.setPlayerOnline(player, this.currentTab);
    }

    public void setPlayerOffline(UUID uuid) {
        this.playerList.setPlayerOffline(uuid);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Tab {
        ALL,
        HIDDEN,
        BLOCKED;

    }
}

