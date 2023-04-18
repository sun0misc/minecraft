package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.PublishCommand;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class OpenToLanScreen extends Screen {
   private static final int MIN_PORT = 1024;
   private static final int MAX_PORT = 65535;
   private static final Text ALLOW_COMMANDS_TEXT = Text.translatable("selectWorld.allowCommands");
   private static final Text GAME_MODE_TEXT = Text.translatable("selectWorld.gameMode");
   private static final Text OTHER_PLAYERS_TEXT = Text.translatable("lanServer.otherPlayers");
   private static final Text PORT_TEXT = Text.translatable("lanServer.port");
   private static final Text UNAVAILABLE_PORT_TEXT = Text.translatable("lanServer.port.unavailable.new", 1024, 65535);
   private static final Text INVALID_PORT_TEXT = Text.translatable("lanServer.port.invalid.new", 1024, 65535);
   private static final int ERROR_TEXT_COLOR = 16733525;
   private final Screen parent;
   private GameMode gameMode;
   private boolean allowCommands;
   private int port;
   @Nullable
   private TextFieldWidget portField;

   public OpenToLanScreen(Screen screen) {
      super(Text.translatable("lanServer.title"));
      this.gameMode = GameMode.SURVIVAL;
      this.port = NetworkUtils.findLocalPort();
      this.parent = screen;
   }

   protected void init() {
      IntegratedServer lv = this.client.getServer();
      this.gameMode = lv.getDefaultGameMode();
      this.allowCommands = lv.getSaveProperties().areCommandsAllowed();
      this.addDrawableChild(CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName).values((Object[])(GameMode.SURVIVAL, GameMode.SPECTATOR, GameMode.CREATIVE, GameMode.ADVENTURE)).initially(this.gameMode).build(this.width / 2 - 155, 100, 150, 20, GAME_MODE_TEXT, (button, gameMode) -> {
         this.gameMode = gameMode;
      }));
      this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.allowCommands).build(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_TEXT, (button, allowCommands) -> {
         this.allowCommands = allowCommands;
      }));
      ButtonWidget lv2 = ButtonWidget.builder(Text.translatable("lanServer.start"), (button) -> {
         this.client.setScreen((Screen)null);
         MutableText lvx;
         if (lv.openToLan(this.gameMode, this.allowCommands, this.port)) {
            lvx = PublishCommand.getStartedText(this.port);
         } else {
            lvx = Text.translatable("commands.publish.failed");
         }

         this.client.inGameHud.getChatHud().addMessage(lvx);
         this.client.updateWindowTitle();
      }).dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build();
      this.portField = new TextFieldWidget(this.textRenderer, this.width / 2 - 75, 160, 150, 20, Text.translatable("lanServer.port"));
      this.portField.setChangedListener((portText) -> {
         Text lv = this.updatePort(portText);
         this.portField.setPlaceholder(Text.literal("" + this.port).formatted(Formatting.DARK_GRAY));
         if (lv == null) {
            this.portField.setEditableColor(14737632);
            this.portField.setTooltip((Tooltip)null);
            lv2.active = true;
         } else {
            this.portField.setEditableColor(16733525);
            this.portField.setTooltip(Tooltip.of(lv));
            lv2.active = false;
         }

      });
      this.portField.setPlaceholder(Text.literal("" + this.port).formatted(Formatting.DARK_GRAY));
      this.addDrawableChild(this.portField);
      this.addDrawableChild(lv2);
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 + 5, this.height - 28, 150, 20).build());
   }

   public void tick() {
      super.tick();
      if (this.portField != null) {
         this.portField.tick();
      }

   }

   @Nullable
   private Text updatePort(String portText) {
      if (portText.isBlank()) {
         this.port = NetworkUtils.findLocalPort();
         return null;
      } else {
         try {
            this.port = Integer.parseInt(portText);
            if (this.port >= 1024 && this.port <= 65535) {
               return !NetworkUtils.isPortAvailable(this.port) ? UNAVAILABLE_PORT_TEXT : null;
            } else {
               return INVALID_PORT_TEXT;
            }
         } catch (NumberFormatException var3) {
            this.port = NetworkUtils.findLocalPort();
            return INVALID_PORT_TEXT;
         }
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 50, 16777215);
      drawCenteredTextWithShadow(matrices, this.textRenderer, OTHER_PLAYERS_TEXT, this.width / 2, 82, 16777215);
      drawCenteredTextWithShadow(matrices, this.textRenderer, PORT_TEXT, this.width / 2, 142, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
