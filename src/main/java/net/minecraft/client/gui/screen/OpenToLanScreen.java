/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.PublishCommand;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.NetworkUtils;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OpenToLanScreen
extends Screen {
    private static final int MIN_PORT = 1024;
    private static final int MAX_PORT = 65535;
    private static final Text ALLOW_COMMANDS_TEXT = Text.translatable("selectWorld.allowCommands.new");
    private static final Text GAME_MODE_TEXT = Text.translatable("selectWorld.gameMode");
    private static final Text OTHER_PLAYERS_TEXT = Text.translatable("lanServer.otherPlayers");
    private static final Text PORT_TEXT = Text.translatable("lanServer.port");
    private static final Text UNAVAILABLE_PORT_TEXT = Text.translatable("lanServer.port.unavailable.new", 1024, 65535);
    private static final Text INVALID_PORT_TEXT = Text.translatable("lanServer.port.invalid.new", 1024, 65535);
    private static final int ERROR_TEXT_COLOR = 0xFF5555;
    private final Screen parent;
    private GameMode gameMode = GameMode.SURVIVAL;
    private boolean allowCommands;
    private int port = NetworkUtils.findLocalPort();
    @Nullable
    private TextFieldWidget portField;

    public OpenToLanScreen(Screen screen) {
        super(Text.translatable("lanServer.title"));
        this.parent = screen;
    }

    @Override
    protected void init() {
        IntegratedServer lv = this.client.getServer();
        this.gameMode = lv.getDefaultGameMode();
        this.allowCommands = lv.getSaveProperties().areCommandsAllowed();
        this.addDrawableChild(CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName).values((GameMode[])new GameMode[]{GameMode.SURVIVAL, GameMode.SPECTATOR, GameMode.CREATIVE, GameMode.ADVENTURE}).initially(this.gameMode).build(this.width / 2 - 155, 100, 150, 20, GAME_MODE_TEXT, (button, gameMode) -> {
            this.gameMode = gameMode;
        }));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.allowCommands).build(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_TEXT, (button, allowCommands) -> {
            this.allowCommands = allowCommands;
        }));
        ButtonWidget lv2 = ButtonWidget.builder(Text.translatable("lanServer.start"), button -> {
            this.client.setScreen(null);
            MutableText lv = lv.openToLan(this.gameMode, this.allowCommands, this.port) ? PublishCommand.getStartedText(this.port) : Text.translatable("commands.publish.failed");
            this.client.inGameHud.getChatHud().addMessage(lv);
            this.client.updateWindowTitle();
        }).dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build();
        this.portField = new TextFieldWidget(this.textRenderer, this.width / 2 - 75, 160, 150, 20, Text.translatable("lanServer.port"));
        this.portField.setChangedListener(portText -> {
            Text lv = this.updatePort((String)portText);
            this.portField.setPlaceholder(Text.literal("" + this.port).formatted(Formatting.DARK_GRAY));
            if (lv == null) {
                this.portField.setEditableColor(0xE0E0E0);
                this.portField.setTooltip(null);
                arg.active = true;
            } else {
                this.portField.setEditableColor(0xFF5555);
                this.portField.setTooltip(Tooltip.of(lv));
                arg.active = false;
            }
        });
        this.portField.setPlaceholder(Text.literal("" + this.port).formatted(Formatting.DARK_GRAY));
        this.addDrawableChild(this.portField);
        this.addDrawableChild(lv2);
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).dimensions(this.width / 2 + 5, this.height - 28, 150, 20).build());
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Nullable
    private Text updatePort(String portText) {
        if (portText.isBlank()) {
            this.port = NetworkUtils.findLocalPort();
            return null;
        }
        try {
            this.port = Integer.parseInt(portText);
            if (this.port < 1024 || this.port > 65535) {
                return INVALID_PORT_TEXT;
            }
            if (!NetworkUtils.isPortAvailable(this.port)) {
                return UNAVAILABLE_PORT_TEXT;
            }
            return null;
        } catch (NumberFormatException numberFormatException) {
            this.port = NetworkUtils.findLocalPort();
            return INVALID_PORT_TEXT;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 50, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, OTHER_PLAYERS_TEXT, this.width / 2, 82, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, PORT_TEXT, this.width / 2, 142, 0xFFFFFF);
    }
}

