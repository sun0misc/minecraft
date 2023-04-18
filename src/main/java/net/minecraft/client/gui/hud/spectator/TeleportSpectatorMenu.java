package net.minecraft.client.gui.hud.spectator;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class TeleportSpectatorMenu implements SpectatorMenuCommandGroup, SpectatorMenuCommand {
   private static final Comparator ORDERING = Comparator.comparing((a) -> {
      return a.getProfile().getId();
   });
   private static final Text TELEPORT_TEXT = Text.translatable("spectatorMenu.teleport");
   private static final Text PROMPT_TEXT = Text.translatable("spectatorMenu.teleport.prompt");
   private final List elements;

   public TeleportSpectatorMenu() {
      this(MinecraftClient.getInstance().getNetworkHandler().getListedPlayerListEntries());
   }

   public TeleportSpectatorMenu(Collection entries) {
      this.elements = entries.stream().filter((entry) -> {
         return entry.getGameMode() != GameMode.SPECTATOR;
      }).sorted(ORDERING).map((entry) -> {
         return new TeleportToSpecificPlayerSpectatorCommand(entry.getProfile());
      }).toList();
   }

   public List getCommands() {
      return this.elements;
   }

   public Text getPrompt() {
      return PROMPT_TEXT;
   }

   public void use(SpectatorMenu menu) {
      menu.selectElement(this);
   }

   public Text getName() {
      return TELEPORT_TEXT;
   }

   public void renderIcon(MatrixStack matrices, float brightness, int alpha) {
      RenderSystem.setShaderTexture(0, SpectatorHud.SPECTATOR_TEXTURE);
      DrawableHelper.drawTexture(matrices, 0, 0, 0.0F, 0.0F, 16, 16, 256, 256);
   }

   public boolean isEnabled() {
      return !this.elements.isEmpty();
   }
}
