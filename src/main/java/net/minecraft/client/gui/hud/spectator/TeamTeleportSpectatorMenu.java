/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.spectator;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.spectator.SpectatorMenu;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommandGroup;
import net.minecraft.client.gui.hud.spectator.TeleportSpectatorMenu;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;

@Environment(value=EnvType.CLIENT)
public class TeamTeleportSpectatorMenu
implements SpectatorMenuCommandGroup,
SpectatorMenuCommand {
    private static final Identifier TEXTURE = Identifier.method_60656("spectator/teleport_to_team");
    private static final Text TEAM_TELEPORT_TEXT = Text.translatable("spectatorMenu.team_teleport");
    private static final Text PROMPT_TEXT = Text.translatable("spectatorMenu.team_teleport.prompt");
    private final List<SpectatorMenuCommand> commands;

    public TeamTeleportSpectatorMenu() {
        MinecraftClient lv = MinecraftClient.getInstance();
        this.commands = TeamTeleportSpectatorMenu.getCommands(lv, lv.world.getScoreboard());
    }

    private static List<SpectatorMenuCommand> getCommands(MinecraftClient client, Scoreboard scoreboard) {
        return scoreboard.getTeams().stream().flatMap(team -> TeleportToSpecificTeamCommand.create(client, team).stream()).toList();
    }

    @Override
    public List<SpectatorMenuCommand> getCommands() {
        return this.commands;
    }

    @Override
    public Text getPrompt() {
        return PROMPT_TEXT;
    }

    @Override
    public void use(SpectatorMenu menu) {
        menu.selectElement(this);
    }

    @Override
    public Text getName() {
        return TEAM_TELEPORT_TEXT;
    }

    @Override
    public void renderIcon(DrawContext context, float brightness, int alpha) {
        context.drawGuiTexture(TEXTURE, 0, 0, 16, 16);
    }

    @Override
    public boolean isEnabled() {
        return !this.commands.isEmpty();
    }

    @Environment(value=EnvType.CLIENT)
    static class TeleportToSpecificTeamCommand
    implements SpectatorMenuCommand {
        private final Team team;
        private final Supplier<SkinTextures> skinTexturesSupplier;
        private final List<PlayerListEntry> scoreboardEntries;

        private TeleportToSpecificTeamCommand(Team team, List<PlayerListEntry> scoreboardEntries, Supplier<SkinTextures> skinTexturesSupplier) {
            this.team = team;
            this.scoreboardEntries = scoreboardEntries;
            this.skinTexturesSupplier = skinTexturesSupplier;
        }

        public static Optional<SpectatorMenuCommand> create(MinecraftClient client, Team team) {
            ArrayList<PlayerListEntry> list = new ArrayList<PlayerListEntry>();
            for (String string : team.getPlayerList()) {
                PlayerListEntry lv = client.getNetworkHandler().getPlayerListEntry(string);
                if (lv == null || lv.getGameMode() == GameMode.SPECTATOR) continue;
                list.add(lv);
            }
            if (list.isEmpty()) {
                return Optional.empty();
            }
            GameProfile gameProfile = ((PlayerListEntry)list.get(Random.create().nextInt(list.size()))).getProfile();
            Supplier<SkinTextures> supplier = client.getSkinProvider().getSkinTexturesSupplier(gameProfile);
            return Optional.of(new TeleportToSpecificTeamCommand(team, list, supplier));
        }

        @Override
        public void use(SpectatorMenu menu) {
            menu.selectElement(new TeleportSpectatorMenu(this.scoreboardEntries));
        }

        @Override
        public Text getName() {
            return this.team.getDisplayName();
        }

        @Override
        public void renderIcon(DrawContext context, float brightness, int alpha) {
            Integer integer = this.team.getColor().getColorValue();
            if (integer != null) {
                float g = (float)(integer >> 16 & 0xFF) / 255.0f;
                float h = (float)(integer >> 8 & 0xFF) / 255.0f;
                float j = (float)(integer & 0xFF) / 255.0f;
                context.fill(1, 1, 15, 15, MathHelper.packRgb(g * brightness, h * brightness, j * brightness) | alpha << 24);
            }
            context.setShaderColor(brightness, brightness, brightness, (float)alpha / 255.0f);
            PlayerSkinDrawer.draw(context, this.skinTexturesSupplier.get(), 2, 2, 12);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}

