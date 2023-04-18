package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ServerHandshakeNetworkHandler implements ServerHandshakePacketListener {
   private static final Text IGNORING_STATUS_REQUEST_MESSAGE = Text.literal("Ignoring status request");
   private final MinecraftServer server;
   private final ClientConnection connection;

   public ServerHandshakeNetworkHandler(MinecraftServer server, ClientConnection connection) {
      this.server = server;
      this.connection = connection;
   }

   public void onHandshake(HandshakeC2SPacket packet) {
      switch (packet.getIntendedState()) {
         case LOGIN:
            this.connection.setState(NetworkState.LOGIN);
            if (packet.getProtocolVersion() != SharedConstants.getGameVersion().getProtocolVersion()) {
               MutableText lv;
               if (packet.getProtocolVersion() < 754) {
                  lv = Text.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getGameVersion().getName());
               } else {
                  lv = Text.translatable("multiplayer.disconnect.incompatible", SharedConstants.getGameVersion().getName());
               }

               this.connection.send(new LoginDisconnectS2CPacket(lv));
               this.connection.disconnect(lv);
            } else {
               this.connection.setPacketListener(new ServerLoginNetworkHandler(this.server, this.connection));
            }
            break;
         case STATUS:
            ServerMetadata lv2 = this.server.getServerMetadata();
            if (this.server.acceptsStatusQuery() && lv2 != null) {
               this.connection.setState(NetworkState.STATUS);
               this.connection.setPacketListener(new ServerQueryNetworkHandler(lv2, this.connection));
            } else {
               this.connection.disconnect(IGNORING_STATUS_REQUEST_MESSAGE);
            }
            break;
         default:
            throw new UnsupportedOperationException("Invalid intention " + packet.getIntendedState());
      }

   }

   public void onDisconnected(Text reason) {
   }

   public boolean isConnectionOpen() {
      return this.connection.isOpen();
   }
}
