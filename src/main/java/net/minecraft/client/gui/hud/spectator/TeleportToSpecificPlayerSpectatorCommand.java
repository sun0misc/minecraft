package net.minecraft.client.gui.hud.spectator;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TeleportToSpecificPlayerSpectatorCommand implements SpectatorMenuCommand {
   private final GameProfile gameProfile;
   private final Identifier skinId;
   private final Text name;

   public TeleportToSpecificPlayerSpectatorCommand(GameProfile gameProfile) {
      this.gameProfile = gameProfile;
      MinecraftClient lv = MinecraftClient.getInstance();
      this.skinId = lv.getSkinProvider().loadSkin(gameProfile);
      this.name = Text.literal(gameProfile.getName());
   }

   public void use(SpectatorMenu menu) {
      MinecraftClient.getInstance().getNetworkHandler().sendPacket(new SpectatorTeleportC2SPacket(this.gameProfile.getId()));
   }

   public Text getName() {
      return this.name;
   }

   public void renderIcon(MatrixStack matrices, float brightness, int alpha) {
      RenderSystem.setShaderTexture(0, this.skinId);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)alpha / 255.0F);
      PlayerSkinDrawer.draw(matrices, 2, 2, 12);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public boolean isEnabled() {
      return true;
   }
}
