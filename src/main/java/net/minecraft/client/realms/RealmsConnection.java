package net.minecraft.client.realms;

import com.mojang.logging.LogUtils;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.QuickPlayLogger;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.report.ReporterEnvironment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsConnection {
   static final Logger LOGGER = LogUtils.getLogger();
   final Screen onlineScreen;
   volatile boolean aborted;
   @Nullable
   ClientConnection connection;

   public RealmsConnection(Screen onlineScreen) {
      this.onlineScreen = onlineScreen;
   }

   public void connect(final RealmsServer server, ServerAddress address) {
      final MinecraftClient lv = MinecraftClient.getInstance();
      lv.setConnectedToRealms(true);
      lv.loadBlockList();
      lv.getNarratorManager().narrate((Text)Text.translatable("mco.connect.success"));
      final String string = address.getAddress();
      final int i = address.getPort();
      (new Thread("Realms-connect-task") {
         public void run() {
            InetSocketAddress inetSocketAddress = null;

            try {
               inetSocketAddress = new InetSocketAddress(string, i);
               if (RealmsConnection.this.aborted) {
                  return;
               }

               RealmsConnection.this.connection = ClientConnection.connect(inetSocketAddress, lv.options.shouldUseNativeTransport());
               if (RealmsConnection.this.aborted) {
                  return;
               }

               RealmsConnection.this.connection.setPacketListener(new ClientLoginNetworkHandler(RealmsConnection.this.connection, lv, server.createServerInfo(string), RealmsConnection.this.onlineScreen, false, (Duration)null, (status) -> {
               }));
               if (RealmsConnection.this.aborted) {
                  return;
               }

               RealmsConnection.this.connection.send(new HandshakeC2SPacket(string, i, NetworkState.LOGIN));
               if (RealmsConnection.this.aborted) {
                  return;
               }

               String stringx = lv.getSession().getUsername();
               UUID uUID = lv.getSession().getUuidOrNull();
               RealmsConnection.this.connection.send(new LoginHelloC2SPacket(stringx, Optional.ofNullable(uUID)));
               lv.ensureAbuseReportContext(ReporterEnvironment.ofRealm(server));
               lv.getQuickPlayLogger().setWorld(QuickPlayLogger.WorldType.REALMS, String.valueOf(server.id), server.name);
            } catch (Exception var5) {
               lv.getServerResourcePackProvider().clear();
               if (RealmsConnection.this.aborted) {
                  return;
               }

               RealmsConnection.LOGGER.error("Couldn't connect to world", var5);
               String string2 = var5.toString();
               if (inetSocketAddress != null) {
                  String string3 = "" + inetSocketAddress + ":" + i;
                  string2 = string2.replaceAll(string3, "");
               }

               DisconnectedRealmsScreen lvx = new DisconnectedRealmsScreen(RealmsConnection.this.onlineScreen, ScreenTexts.CONNECT_FAILED, Text.translatable("disconnect.genericReason", string2));
               lv.execute(() -> {
                  lv.setScreen(lvx);
               });
            }

         }
      }).start();
   }

   public void abort() {
      this.aborted = true;
      if (this.connection != null && this.connection.isOpen()) {
         this.connection.disconnect(Text.translatable("disconnect.genericReason"));
         this.connection.handleDisconnection();
      }

   }

   public void tick() {
      if (this.connection != null) {
         if (this.connection.isOpen()) {
            this.connection.tick();
         } else {
            this.connection.handleDisconnection();
         }
      }

   }
}
