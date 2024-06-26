/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.world.CommandBlockExecutor;

@Environment(value=EnvType.CLIENT)
public class MinecartCommandBlockScreen
extends AbstractCommandBlockScreen {
    private final CommandBlockExecutor commandExecutor;

    public MinecartCommandBlockScreen(CommandBlockExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public CommandBlockExecutor getCommandExecutor() {
        return this.commandExecutor;
    }

    @Override
    int getTrackOutputButtonHeight() {
        return 150;
    }

    @Override
    protected void init() {
        super.init();
        this.consoleCommandTextField.setText(this.getCommandExecutor().getCommand());
    }

    @Override
    protected void syncSettingsToServer(CommandBlockExecutor commandExecutor) {
        if (commandExecutor instanceof CommandBlockMinecartEntity.CommandExecutor) {
            CommandBlockMinecartEntity.CommandExecutor lv = (CommandBlockMinecartEntity.CommandExecutor)commandExecutor;
            this.client.getNetworkHandler().sendPacket(new UpdateCommandBlockMinecartC2SPacket(lv.getMinecart().getId(), this.consoleCommandTextField.getText(), commandExecutor.isTrackingOutput()));
        }
    }
}

