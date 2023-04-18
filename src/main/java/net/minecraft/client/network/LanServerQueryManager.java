package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class LanServerQueryManager {
   static final AtomicInteger THREAD_ID = new AtomicInteger(0);
   static final Logger LOGGER = LogUtils.getLogger();

   @Environment(EnvType.CLIENT)
   public static class LanServerDetector extends Thread {
      private final LanServerEntryList entryList;
      private final InetAddress multicastAddress;
      private final MulticastSocket socket;

      public LanServerDetector(LanServerEntryList entryList) throws IOException {
         super("LanServerDetector #" + LanServerQueryManager.THREAD_ID.incrementAndGet());
         this.entryList = entryList;
         this.setDaemon(true);
         this.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LanServerQueryManager.LOGGER));
         this.socket = new MulticastSocket(4445);
         this.multicastAddress = InetAddress.getByName("224.0.2.60");
         this.socket.setSoTimeout(5000);
         this.socket.joinGroup(this.multicastAddress);
      }

      public void run() {
         byte[] bs = new byte[1024];

         while(!this.isInterrupted()) {
            DatagramPacket datagramPacket = new DatagramPacket(bs, bs.length);

            try {
               this.socket.receive(datagramPacket);
            } catch (SocketTimeoutException var5) {
               continue;
            } catch (IOException var6) {
               LanServerQueryManager.LOGGER.error("Couldn't ping server", var6);
               break;
            }

            String string = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), StandardCharsets.UTF_8);
            LanServerQueryManager.LOGGER.debug("{}: {}", datagramPacket.getAddress(), string);
            this.entryList.addServer(string, datagramPacket.getAddress());
         }

         try {
            this.socket.leaveGroup(this.multicastAddress);
         } catch (IOException var4) {
         }

         this.socket.close();
      }
   }

   @Environment(EnvType.CLIENT)
   public static class LanServerEntryList {
      private final List serverEntries = Lists.newArrayList();
      private boolean dirty;

      @Nullable
      public synchronized List getEntriesIfUpdated() {
         if (this.dirty) {
            List list = List.copyOf(this.serverEntries);
            this.dirty = false;
            return list;
         } else {
            return null;
         }
      }

      public synchronized void addServer(String announcement, InetAddress address) {
         String string2 = LanServerPinger.parseAnnouncementMotd(announcement);
         String string3 = LanServerPinger.parseAnnouncementAddressPort(announcement);
         if (string3 != null) {
            String var10000 = address.getHostAddress();
            string3 = var10000 + ":" + string3;
            boolean bl = false;
            Iterator var6 = this.serverEntries.iterator();

            while(var6.hasNext()) {
               LanServerInfo lv = (LanServerInfo)var6.next();
               if (lv.getAddressPort().equals(string3)) {
                  lv.updateLastTime();
                  bl = true;
                  break;
               }
            }

            if (!bl) {
               this.serverEntries.add(new LanServerInfo(string2, string3));
               this.dirty = true;
            }

         }
      }
   }
}
