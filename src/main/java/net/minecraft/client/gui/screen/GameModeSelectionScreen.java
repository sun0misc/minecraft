package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class GameModeSelectionScreen extends Screen {
   static final Identifier TEXTURE = new Identifier("textures/gui/container/gamemode_switcher.png");
   private static final int TEXTURE_WIDTH = 128;
   private static final int TEXTURE_HEIGHT = 128;
   private static final int BUTTON_SIZE = 26;
   private static final int ICON_OFFSET = 5;
   private static final int field_32314 = 31;
   private static final int field_32315 = 5;
   private static final int UI_WIDTH = GameModeSelectionScreen.GameModeSelection.values().length * 31 - 5;
   private static final Text SELECT_NEXT_TEXT;
   private final Optional currentGameMode = GameModeSelectionScreen.GameModeSelection.of(this.getPreviousGameMode());
   private Optional gameMode = Optional.empty();
   private int lastMouseX;
   private int lastMouseY;
   private boolean mouseUsedForSelection;
   private final List gameModeButtons = Lists.newArrayList();

   public GameModeSelectionScreen() {
      super(NarratorManager.EMPTY);
   }

   private GameMode getPreviousGameMode() {
      ClientPlayerInteractionManager lv = MinecraftClient.getInstance().interactionManager;
      GameMode lv2 = lv.getPreviousGameMode();
      if (lv2 != null) {
         return lv2;
      } else {
         return lv.getCurrentGameMode() == GameMode.CREATIVE ? GameMode.SURVIVAL : GameMode.CREATIVE;
      }
   }

   protected void init() {
      super.init();
      this.gameMode = this.currentGameMode.isPresent() ? this.currentGameMode : GameModeSelectionScreen.GameModeSelection.of(this.client.interactionManager.getCurrentGameMode());

      for(int i = 0; i < GameModeSelectionScreen.GameModeSelection.VALUES.length; ++i) {
         GameModeSelection lv = GameModeSelectionScreen.GameModeSelection.VALUES[i];
         this.gameModeButtons.add(new ButtonWidget(lv, this.width / 2 - UI_WIDTH / 2 + i * 31, this.height / 2 - 31));
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (!this.checkForClose()) {
         matrices.push();
         RenderSystem.enableBlend();
         RenderSystem.setShaderTexture(0, TEXTURE);
         int k = this.width / 2 - 62;
         int l = this.height / 2 - 31 - 27;
         drawTexture(matrices, k, l, 0.0F, 0.0F, 125, 75, 128, 128);
         matrices.pop();
         super.render(matrices, mouseX, mouseY, delta);
         this.gameMode.ifPresent((gameMode) -> {
            drawCenteredTextWithShadow(matrices, this.textRenderer, gameMode.getText(), this.width / 2, this.height / 2 - 31 - 20, -1);
         });
         drawCenteredTextWithShadow(matrices, this.textRenderer, SELECT_NEXT_TEXT, this.width / 2, this.height / 2 + 5, 16777215);
         if (!this.mouseUsedForSelection) {
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            this.mouseUsedForSelection = true;
         }

         boolean bl = this.lastMouseX == mouseX && this.lastMouseY == mouseY;
         Iterator var8 = this.gameModeButtons.iterator();

         while(var8.hasNext()) {
            ButtonWidget lv = (ButtonWidget)var8.next();
            lv.render(matrices, mouseX, mouseY, delta);
            this.gameMode.ifPresent((gameMode) -> {
               lv.setSelected(gameMode == lv.gameMode);
            });
            if (!bl && lv.isSelected()) {
               this.gameMode = Optional.of(lv.gameMode);
            }
         }

      }
   }

   private void apply() {
      apply(this.client, this.gameMode);
   }

   private static void apply(MinecraftClient client, Optional gameMode) {
      if (client.interactionManager != null && client.player != null && gameMode.isPresent()) {
         Optional optional2 = GameModeSelectionScreen.GameModeSelection.of(client.interactionManager.getCurrentGameMode());
         GameModeSelection lv = (GameModeSelection)gameMode.get();
         if (optional2.isPresent() && client.player.hasPermissionLevel(2) && lv != optional2.get()) {
            client.player.networkHandler.sendCommand(lv.getCommand());
         }

      }
   }

   private boolean checkForClose() {
      if (!InputUtil.isKeyPressed(this.client.getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
         this.apply();
         this.client.setScreen((Screen)null);
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_F4 && this.gameMode.isPresent()) {
         this.mouseUsedForSelection = false;
         this.gameMode = ((GameModeSelection)this.gameMode.get()).next();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean shouldPause() {
      return false;
   }

   static {
      SELECT_NEXT_TEXT = Text.translatable("debug.gamemodes.select_next", Text.translatable("debug.gamemodes.press_f4").formatted(Formatting.AQUA));
   }

   @Environment(EnvType.CLIENT)
   private static enum GameModeSelection {
      CREATIVE(Text.translatable("gameMode.creative"), "gamemode creative", new ItemStack(Blocks.GRASS_BLOCK)),
      SURVIVAL(Text.translatable("gameMode.survival"), "gamemode survival", new ItemStack(Items.IRON_SWORD)),
      ADVENTURE(Text.translatable("gameMode.adventure"), "gamemode adventure", new ItemStack(Items.MAP)),
      SPECTATOR(Text.translatable("gameMode.spectator"), "gamemode spectator", new ItemStack(Items.ENDER_EYE));

      protected static final GameModeSelection[] VALUES = values();
      private static final int field_32317 = 16;
      protected static final int field_32316 = 5;
      final Text text;
      final String command;
      final ItemStack icon;

      private GameModeSelection(Text text, String command, ItemStack icon) {
         this.text = text;
         this.command = command;
         this.icon = icon;
      }

      void renderIcon(MatrixStack matrices, ItemRenderer itemRenderer, int x, int y) {
         itemRenderer.renderInGuiWithOverrides(matrices, this.icon, x, y);
      }

      Text getText() {
         return this.text;
      }

      String getCommand() {
         return this.command;
      }

      Optional next() {
         switch (this) {
            case CREATIVE:
               return Optional.of(SURVIVAL);
            case SURVIVAL:
               return Optional.of(ADVENTURE);
            case ADVENTURE:
               return Optional.of(SPECTATOR);
            default:
               return Optional.of(CREATIVE);
         }
      }

      static Optional of(GameMode gameMode) {
         switch (gameMode) {
            case SPECTATOR:
               return Optional.of(SPECTATOR);
            case SURVIVAL:
               return Optional.of(SURVIVAL);
            case CREATIVE:
               return Optional.of(CREATIVE);
            case ADVENTURE:
               return Optional.of(ADVENTURE);
            default:
               return Optional.empty();
         }
      }

      // $FF: synthetic method
      private static GameModeSelection[] method_36886() {
         return new GameModeSelection[]{CREATIVE, SURVIVAL, ADVENTURE, SPECTATOR};
      }
   }

   @Environment(EnvType.CLIENT)
   public class ButtonWidget extends ClickableWidget {
      final GameModeSelection gameMode;
      private boolean selected;

      public ButtonWidget(GameModeSelection gameMode, int x, int y) {
         super(x, y, 26, 26, gameMode.getText());
         this.gameMode = gameMode;
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         MinecraftClient lv = MinecraftClient.getInstance();
         this.drawBackground(matrices, lv.getTextureManager());
         this.gameMode.renderIcon(matrices, GameModeSelectionScreen.this.itemRenderer, this.getX() + 5, this.getY() + 5);
         if (this.selected) {
            this.drawSelectionBox(matrices, lv.getTextureManager());
         }

      }

      public void appendClickableNarrations(NarrationMessageBuilder builder) {
         this.appendDefaultNarrations(builder);
      }

      public boolean isSelected() {
         return super.isSelected() || this.selected;
      }

      public void setSelected(boolean selected) {
         this.selected = selected;
      }

      private void drawBackground(MatrixStack matrices, TextureManager textureManager) {
         RenderSystem.setShaderTexture(0, GameModeSelectionScreen.TEXTURE);
         matrices.push();
         matrices.translate((float)this.getX(), (float)this.getY(), 0.0F);
         drawTexture(matrices, 0, 0, 0.0F, 75.0F, 26, 26, 128, 128);
         matrices.pop();
      }

      private void drawSelectionBox(MatrixStack matrices, TextureManager textureManager) {
         RenderSystem.setShaderTexture(0, GameModeSelectionScreen.TEXTURE);
         matrices.push();
         matrices.translate((float)this.getX(), (float)this.getY(), 0.0F);
         drawTexture(matrices, 0, 0, 26.0F, 75.0F, 26, 26, 128, 128);
         matrices.pop();
      }
   }
}
