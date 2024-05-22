/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import net.minecraft.class_9812;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.state.LoginStates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;

public class LocalServerHandshakeNetworkHandler
implements ServerHandshakePacketListener {
    private final MinecraftServer server;
    private final ClientConnection connection;

    public LocalServerHandshakeNetworkHandler(MinecraftServer server, ClientConnection connection) {
        this.server = server;
        this.connection = connection;
    }

    @Override
    public void onHandshake(HandshakeC2SPacket packet) {
        if (packet.intendedState() != ConnectionIntent.LOGIN) {
            throw new UnsupportedOperationException("Invalid intention " + String.valueOf((Object)packet.intendedState()));
        }
        this.connection.transitionInbound(LoginStates.C2S, new ServerLoginNetworkHandler(this.server, this.connection, false));
        this.connection.transitionOutbound(LoginStates.S2C);
    }

    @Override
    public void onDisconnected(class_9812 arg) {
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen();
    }
}

