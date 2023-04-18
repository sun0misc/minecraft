package net.minecraft.client.gui.hud.spectator;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class TeamTeleportSpectatorMenu implements SpectatorMenuCommandGroup, SpectatorMenuCommand {
   private static final Text TEAM_TELEPORT_TEXT = Text.translatable("spectatorMenu.team_teleport");
   private static final Text PROMPT_TEXT = Text.translatable("spectatorMenu.team_teleport.prompt");
   private final List commands;

   public TeamTeleportSpectatorMenu() {
      MinecraftClient lv = MinecraftClient.getInstance();
      this.commands = getCommands(lv, lv.world.getScoreboard());
   }

   private static List getCommands(MinecraftClient client, Scoreboard scoreboard) {
      return scoreboard.getTeams().stream().flatMap((team) -> {
         return TeamTeleportSpectatorMenu.TeleportToSpecificTeamCommand.create(client, team).stream();
      }).toList();
   }

   public List getCommands() {
      return this.commands;
   }

   public Text getPrompt() {
      return PROMPT_TEXT;
   }

   public void use(SpectatorMenu menu) {
      menu.selectElement(this);
   }

   public Text getName() {
      return TEAM_TELEPORT_TEXT;
   }

   public void renderIcon(MatrixStack matrices, float brightness, int alpha) {
      RenderSystem.setShaderTexture(0, SpectatorHud.SPECTATOR_TEXTURE);
      DrawableHelper.drawTexture(matrices, 0, 0, 16.0F, 0.0F, 16, 16, 256, 256);
   }

   public boolean isEnabled() {
      return !this.commands.isEmpty();
   }

   @Environment(EnvType.CLIENT)
   static class TeleportToSpecificTeamCommand implements SpectatorMenuCommand {
      private final Team team;
      private final Identifier skinId;
      private final List scoreboardEntries;

      private TeleportToSpecificTeamCommand(Team team, List scoreboardEntries, Identifier skinId) {
         this.team = team;
         this.scoreboardEntries = scoreboardEntries;
         this.skinId = skinId;
      }

      public static Optional create(MinecraftClient client, Team team) {
         List list = new ArrayList();
         Iterator var3 = team.getPlayerList().iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            PlayerListEntry lv = client.getNetworkHandler().getPlayerListEntry(string);
            if (lv != null && lv.getGameMode() != GameMode.SPECTATOR) {
               list.add(lv);
            }
         }

         if (list.isEmpty()) {
            return Optional.empty();
         } else {
            GameProfile gameProfile = ((PlayerListEntry)list.get(Random.create().nextInt(list.size()))).getProfile();
            Identifier lv2 = client.getSkinProvider().loadSkin(gameProfile);
            return Optional.of(new TeleportToSpecificTeamCommand(team, list, lv2));
         }
      }

      public void use(SpectatorMenu menu) {
         menu.selectElement(new TeleportSpectatorMenu(this.scoreboardEntries));
      }

      public Text getName() {
         return this.team.getDisplayName();
      }

      public void renderIcon(MatrixStack matrices, float brightness, int alpha) {
         Integer integer = this.team.getColor().getColorValue();
         if (integer != null) {
            float g = (float)(integer >> 16 & 255) / 255.0F;
            float h = (float)(integer >> 8 & 255) / 255.0F;
            float j = (float)(integer & 255) / 255.0F;
            DrawableHelper.fill(matrices, 1, 1, 15, 15, MathHelper.packRgb(g * brightness, h * brightness, j * brightness) | alpha << 24);
         }

         RenderSystem.setShaderTexture(0, this.skinId);
         RenderSystem.setShaderColor(brightness, brightness, brightness, (float)alpha / 255.0F);
         PlayerSkinDrawer.draw(matrices, 2, 2, 12);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      public boolean isEnabled() {
         return true;
      }
   }
}
