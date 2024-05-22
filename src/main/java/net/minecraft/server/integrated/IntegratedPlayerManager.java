/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.integrated;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.WorldSaveHandler;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class IntegratedPlayerManager
extends PlayerManager {
    @Nullable
    private NbtCompound userData;

    public IntegratedPlayerManager(IntegratedServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, WorldSaveHandler saveHandler) {
        super(server, registryManager, saveHandler, 8);
        this.setViewDistance(10);
    }

    @Override
    protected void savePlayerData(ServerPlayerEntity player) {
        if (this.getServer().isHost(player.getGameProfile())) {
            this.userData = player.writeNbt(new NbtCompound());
        }
        super.savePlayerData(player);
    }

    @Override
    public Text checkCanJoin(SocketAddress address, GameProfile profile) {
        if (this.getServer().isHost(profile) && this.getPlayer(profile.getName()) != null) {
            return Text.translatable("multiplayer.disconnect.name_taken");
        }
        return super.checkCanJoin(address, profile);
    }

    @Override
    public IntegratedServer getServer() {
        return (IntegratedServer)super.getServer();
    }

    @Override
    @Nullable
    public NbtCompound getUserData() {
        return this.userData;
    }

    @Override
    public /* synthetic */ MinecraftServer getServer() {
        return this.getServer();
    }
}

