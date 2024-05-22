/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9782;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.telemetry.WorldSession;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ClientConnectionState(GameProfile localGameProfile, WorldSession worldSession, DynamicRegistryManager.Immutable receivedRegistries, FeatureSet enabledFeatures, @Nullable String serverBrand, @Nullable ServerInfo serverInfo, @Nullable Screen postDisconnectScreen, Map<Identifier, byte[]> serverCookies, @Nullable ChatHud.ChatState chatState, @Deprecated(forRemoval=true) boolean strictErrorHandling, Map<String, String> customReportDetails, class_9782 serverLinks) {
    @Nullable
    public String serverBrand() {
        return this.serverBrand;
    }

    @Nullable
    public ServerInfo serverInfo() {
        return this.serverInfo;
    }

    @Nullable
    public Screen postDisconnectScreen() {
        return this.postDisconnectScreen;
    }

    @Nullable
    public ChatHud.ChatState chatState() {
        return this.chatState;
    }

    @Deprecated(forRemoval=true)
    public boolean strictErrorHandling() {
        return this.strictErrorHandling;
    }
}

