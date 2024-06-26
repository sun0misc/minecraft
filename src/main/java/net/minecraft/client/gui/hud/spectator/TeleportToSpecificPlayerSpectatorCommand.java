/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.spectator;

import com.mojang.authlib.GameProfile;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.spectator.SpectatorMenu;
import net.minecraft.client.gui.hud.spectator.SpectatorMenuCommand;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class TeleportToSpecificPlayerSpectatorCommand
implements SpectatorMenuCommand {
    private final GameProfile gameProfile;
    private final Supplier<SkinTextures> skinTexturesSupplier;
    private final Text name;

    public TeleportToSpecificPlayerSpectatorCommand(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
        this.skinTexturesSupplier = MinecraftClient.getInstance().getSkinProvider().getSkinTexturesSupplier(gameProfile);
        this.name = Text.literal(gameProfile.getName());
    }

    @Override
    public void use(SpectatorMenu menu) {
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new SpectatorTeleportC2SPacket(this.gameProfile.getId()));
    }

    @Override
    public Text getName() {
        return this.name;
    }

    @Override
    public void renderIcon(DrawContext context, float brightness, int alpha) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, (float)alpha / 255.0f);
        PlayerSkinDrawer.draw(context, this.skinTexturesSupplier.get(), 2, 2, 12);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

