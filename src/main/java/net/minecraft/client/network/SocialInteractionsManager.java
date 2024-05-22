/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.UserApiService;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class SocialInteractionsManager {
    private final MinecraftClient client;
    private final Set<UUID> hiddenPlayers = Sets.newHashSet();
    private final UserApiService userApiService;
    private final Map<String, UUID> playerNameByUuid = Maps.newHashMap();
    private boolean blockListLoaded;
    private CompletableFuture<?> blockListLoader = CompletableFuture.completedFuture(null);

    public SocialInteractionsManager(MinecraftClient client, UserApiService userApiService) {
        this.client = client;
        this.userApiService = userApiService;
    }

    public void hidePlayer(UUID uuid) {
        this.hiddenPlayers.add(uuid);
    }

    public void showPlayer(UUID uuid) {
        this.hiddenPlayers.remove(uuid);
    }

    public boolean isPlayerMuted(UUID uuid) {
        return this.isPlayerHidden(uuid) || this.isPlayerBlocked(uuid);
    }

    public boolean isPlayerHidden(UUID uuid) {
        return this.hiddenPlayers.contains(uuid);
    }

    public void loadBlockList() {
        this.blockListLoaded = true;
        this.blockListLoader = this.blockListLoader.thenRunAsync(this.userApiService::refreshBlockList, Util.getIoWorkerExecutor());
    }

    public void unloadBlockList() {
        this.blockListLoaded = false;
    }

    public boolean isPlayerBlocked(UUID uuid) {
        if (!this.blockListLoaded) {
            return false;
        }
        this.blockListLoader.join();
        return this.userApiService.isBlockedPlayer(uuid);
    }

    public Set<UUID> getHiddenPlayers() {
        return this.hiddenPlayers;
    }

    public UUID getUuid(String playerName) {
        return this.playerNameByUuid.getOrDefault(playerName, Util.NIL_UUID);
    }

    public void setPlayerOnline(PlayerListEntry player) {
        GameProfile gameProfile = player.getProfile();
        this.playerNameByUuid.put(gameProfile.getName(), gameProfile.getId());
        Screen screen = this.client.currentScreen;
        if (screen instanceof SocialInteractionsScreen) {
            SocialInteractionsScreen lv = (SocialInteractionsScreen)screen;
            lv.setPlayerOnline(player);
        }
    }

    public void setPlayerOffline(UUID uuid) {
        Screen screen = this.client.currentScreen;
        if (screen instanceof SocialInteractionsScreen) {
            SocialInteractionsScreen lv = (SocialInteractionsScreen)screen;
            lv.setPlayerOffline(uuid);
        }
    }
}

