/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.hud.spectator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.spectator.SpectatorMenu;

@Environment(value=EnvType.CLIENT)
public interface SpectatorMenuCloseCallback {
    public void close(SpectatorMenu var1);
}

