/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.realms.gui.screen;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsSlotOptionsScreen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

@Environment(value=EnvType.CLIENT)
public class RealmsBackupInfoScreen
extends RealmsScreen {
    private static final Text TITLE = Text.translatable("mco.backup.info.title");
    private static final Text UNKNOWN = Text.translatable("mco.backup.unknown");
    private final Screen parent;
    final Backup backup;
    final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private BackupInfoList backupInfoList;

    public RealmsBackupInfoScreen(Screen parent, Backup backup) {
        super(TITLE);
        this.parent = parent;
        this.backup = backup;
    }

    @Override
    public void init() {
        this.layout.addHeader(TITLE, this.textRenderer);
        this.backupInfoList = this.layout.addBody(new BackupInfoList(this.client));
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).build());
        this.initTabNavigation();
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
    }

    @Override
    protected void initTabNavigation() {
        this.backupInfoList.setDimensions(this.width, this.layout.getContentHeight());
        this.layout.refreshPositions();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    Text checkForSpecificMetadata(String key, String value) {
        String string3 = key.toLowerCase(Locale.ROOT);
        if (string3.contains("game") && string3.contains("mode")) {
            return this.gameModeMetadata(value);
        }
        if (string3.contains("game") && string3.contains("difficulty")) {
            return this.gameDifficultyMetadata(value);
        }
        return Text.literal(value);
    }

    private Text gameDifficultyMetadata(String value) {
        try {
            return RealmsSlotOptionsScreen.DIFFICULTIES.get(Integer.parseInt(value)).getTranslatableName();
        } catch (Exception exception) {
            return UNKNOWN;
        }
    }

    private Text gameModeMetadata(String value) {
        try {
            return RealmsSlotOptionsScreen.GAME_MODES.get(Integer.parseInt(value)).getSimpleTranslatableName();
        } catch (Exception exception) {
            return UNKNOWN;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BackupInfoList
    extends AlwaysSelectedEntryListWidget<BackupInfoListEntry> {
        public BackupInfoList(MinecraftClient client) {
            super(client, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.layout.getContentHeight(), RealmsBackupInfoScreen.this.layout.getHeaderHeight(), 36);
            if (RealmsBackupInfoScreen.this.backup.changeList != null) {
                RealmsBackupInfoScreen.this.backup.changeList.forEach((key, value) -> this.addEntry(new BackupInfoListEntry((String)key, (String)value)));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BackupInfoListEntry
    extends AlwaysSelectedEntryListWidget.Entry<BackupInfoListEntry> {
        private static final Text TEMPLATE_NAME_TEXT = Text.translatable("mco.backup.entry.templateName");
        private static final Text GAME_DIFFICULTY_TEXT = Text.translatable("mco.backup.entry.gameDifficulty");
        private static final Text NAME_TEXT = Text.translatable("mco.backup.entry.name");
        private static final Text GAME_SERVER_VERSION_TEXT = Text.translatable("mco.backup.entry.gameServerVersion");
        private static final Text UPLOADED_TEXT = Text.translatable("mco.backup.entry.uploaded");
        private static final Text ENABLED_PACK_TEXT = Text.translatable("mco.backup.entry.enabledPack");
        private static final Text DESCRIPTION_TEXT = Text.translatable("mco.backup.entry.description");
        private static final Text GAME_MODE_TEXT = Text.translatable("mco.backup.entry.gameMode");
        private static final Text SEED_TEXT = Text.translatable("mco.backup.entry.seed");
        private static final Text WORLD_TYPE_TEXT = Text.translatable("mco.backup.entry.worldType");
        private static final Text UNDEFINED_TEXT = Text.translatable("mco.backup.entry.undefined");
        private final String key;
        private final String value;

        public BackupInfoListEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(RealmsBackupInfoScreen.this.textRenderer, this.getTextFromKey(this.key), x, y, Colors.LIGHT_GRAY);
            context.drawTextWithShadow(RealmsBackupInfoScreen.this.textRenderer, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), x, y + 12, Colors.WHITE);
        }

        private Text getTextFromKey(String key) {
            return switch (key) {
                case "template_name" -> TEMPLATE_NAME_TEXT;
                case "game_difficulty" -> GAME_DIFFICULTY_TEXT;
                case "name" -> NAME_TEXT;
                case "game_server_version" -> GAME_SERVER_VERSION_TEXT;
                case "uploaded" -> UPLOADED_TEXT;
                case "enabled_packs" -> ENABLED_PACK_TEXT;
                case "description" -> DESCRIPTION_TEXT;
                case "game_mode" -> GAME_MODE_TEXT;
                case "seed" -> SEED_TEXT;
                case "world_type" -> WORLD_TYPE_TEXT;
                default -> UNDEFINED_TEXT;
            };
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return true;
        }

        @Override
        public Text getNarration() {
            return Text.translatable("narrator.select", this.key + " " + this.value);
        }
    }
}

