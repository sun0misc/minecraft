package net.minecraft.server.rcon;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class RconListener extends RconBase {
   private static final Logger SERVER_LOGGER = LogUtils.getLogger();
   private final ServerSocket listener;
   private final String password;
   private final List clients = Lists.newArrayList();
   private final DedicatedServer server;

   private RconListener(DedicatedServer server, ServerSocket listener, String password) {
      super("RCON Listener");
      this.server = server;
      this.listener = listener;
      this.password = password;
   }

   private void removeStoppedClients() {
      this.clients.removeIf((client) -> {
         return !client.isRunning();
      });
   }

   public void run() {
      try {
         while(this.running) {
            try {
               Socket socket = this.listener.accept();
               RconClient lv = new RconClient(this.server, this.password, socket);
               lv.start();
               this.clients.add(lv);
               this.removeStoppedClients();
            } catch (SocketTimeoutException var7) {
               this.removeStoppedClients();
            } catch (IOException var8) {
               if (this.running) {
                  SERVER_LOGGER.info("IO exception: ", var8);
               }
            }
         }
      } finally {
         this.closeSocket(this.listener);
      }

   }

   @Nullable
   public static RconListener create(DedicatedServer server) {
      ServerPropertiesHandler lv = server.getProperties();
      String string = server.getHostname();
      if (string.isEmpty()) {
         string = "0.0.0.0";
      }

      int i = lv.rconPort;
      if (0 < i && 65535 >= i) {
         String string2 = lv.rconPassword;
         if (string2.isEmpty()) {
            SERVER_LOGGER.warn("No rcon password set in server.properties, rcon disabled!");
            return null;
         } else {
            try {
               ServerSocket serverSocket = new ServerSocket(i, 0, InetAddress.getByName(string));
               serverSocket.setSoTimeout(500);
               RconListener lv2 = new RconListener(server, serverSocket, string2);
               if (!lv2.start()) {
                  return null;
               } else {
                  SERVER_LOGGER.info("RCON running on {}:{}", string, i);
                  return lv2;
               }
            } catch (IOException var7) {
               SERVER_LOGGER.warn("Unable to initialise RCON on {}:{}", new Object[]{string, i, var7});
               return null;
            }
         }
      } else {
         SERVER_LOGGER.warn("Invalid rcon port {} found in server.properties, rcon disabled!", i);
         return null;
      }
   }

   public void stop() {
      this.running = false;
      this.closeSocket(this.listener);
      super.stop();
      Iterator var1 = this.clients.iterator();

      while(var1.hasNext()) {
         RconClient lv = (RconClient)var1.next();
         if (lv.isRunning()) {
            lv.stop();
         }
      }

      this.clients.clear();
   }

   private void closeSocket(ServerSocket socket) {
      SERVER_LOGGER.debug("closeSocket: {}", socket);

      try {
         socket.close();
      } catch (IOException var3) {
         SERVER_LOGGER.warn("Failed to close socket", var3);
      }

   }
}
