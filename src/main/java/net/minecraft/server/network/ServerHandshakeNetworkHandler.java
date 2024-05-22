/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.class_9812;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.state.LoginStates;
import net.minecraft.network.state.QueryStates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ServerHandshakeNetworkHandler
implements ServerHandshakePacketListener {
    private static final Text IGNORING_STATUS_REQUEST_MESSAGE = Text.translatable("disconnect.ignoring_status_request");
    private final MinecraftServer server;
    private final ClientConnection connection;

    public ServerHandshakeNetworkHandler(MinecraftServer server, ClientConnection connection) {
        this.server = server;
        this.connection = connection;
    }

    @Override
    public void onHandshake(HandshakeC2SPacket packet) {
        switch (packet.intendedState()) {
            case LOGIN: {
                this.login(packet, false);
                break;
            }
            case STATUS: {
                ServerMetadata lv = this.server.getServerMetadata();
                this.connection.transitionOutbound(QueryStates.S2C);
                if (this.server.acceptsStatusQuery() && lv != null) {
                    this.connection.transitionInbound(QueryStates.C2S, new ServerQueryNetworkHandler(lv, this.connection));
                    break;
                }
                this.connection.disconnect(IGNORING_STATUS_REQUEST_MESSAGE);
                break;
            }
            case TRANSFER: {
                if (!this.server.acceptsTransfers()) {
                    this.connection.transitionOutbound(LoginStates.S2C);
                    MutableText lv2 = Text.translatable("multiplayer.disconnect.transfers_disabled");
                    this.connection.send(new LoginDisconnectS2CPacket(lv2));
                    this.connection.disconnect(lv2);
                    break;
                }
                this.login(packet, true);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Invalid intention " + String.valueOf((Object)packet.intendedState()));
            }
        }
    }

    private void login(HandshakeC2SPacket packet, boolean transfer) {
        this.connection.transitionOutbound(LoginStates.S2C);
        if (packet.protocolVersion() != SharedConstants.getGameVersion().getProtocolVersion()) {
            MutableText lv = packet.protocolVersion() < 754 ? Text.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getGameVersion().getName()) : Text.translatable("multiplayer.disconnect.incompatible", SharedConstants.getGameVersion().getName());
            this.connection.send(new LoginDisconnectS2CPacket(lv));
            this.connection.disconnect(lv);
        } else {
            this.connection.transitionInbound(LoginStates.C2S, new ServerLoginNetworkHandler(this.server, this.connection, transfer));
        }
    }

    @Override
    public void onDisconnected(class_9812 arg) {
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen();
    }
}

