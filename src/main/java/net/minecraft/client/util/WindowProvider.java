/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public final class WindowProvider
implements AutoCloseable {
    private final MinecraftClient client;
    private final MonitorTracker monitorTracker;

    public WindowProvider(MinecraftClient client) {
        this.client = client;
        this.monitorTracker = new MonitorTracker(Monitor::new);
    }

    public Window createWindow(WindowSettings settings, @Nullable String videoMode, String title) {
        return new Window(this.client, this.monitorTracker, settings, videoMode, title);
    }

    @Override
    public void close() {
        this.monitorTracker.stop();
    }
}

