package net.minecraft.client.option;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import net.minecraft.util.thread.TaskExecutor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerList {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final TaskExecutor IO_EXECUTOR = TaskExecutor.create(Util.getMainWorkerExecutor(), "server-list-io");
   private static final int MAX_HIDDEN_ENTRIES = 16;
   private final MinecraftClient client;
   private final List servers = Lists.newArrayList();
   private final List hiddenServers = Lists.newArrayList();

   public ServerList(MinecraftClient client) {
      this.client = client;
   }

   public void loadFile() {
      try {
         this.servers.clear();
         this.hiddenServers.clear();
         NbtCompound lv = NbtIo.read(new File(this.client.runDirectory, "servers.dat"));
         if (lv == null) {
            return;
         }

         NbtList lv2 = lv.getList("servers", NbtElement.COMPOUND_TYPE);

         for(int i = 0; i < lv2.size(); ++i) {
            NbtCompound lv3 = lv2.getCompound(i);
            ServerInfo lv4 = ServerInfo.fromNbt(lv3);
            if (lv3.getBoolean("hidden")) {
               this.hiddenServers.add(lv4);
            } else {
               this.servers.add(lv4);
            }
         }
      } catch (Exception var6) {
         LOGGER.error("Couldn't load server list", var6);
      }

   }

   public void saveFile() {
      try {
         NbtList lv = new NbtList();
         Iterator var2 = this.servers.iterator();

         ServerInfo lv2;
         NbtCompound lv3;
         while(var2.hasNext()) {
            lv2 = (ServerInfo)var2.next();
            lv3 = lv2.toNbt();
            lv3.putBoolean("hidden", false);
            lv.add(lv3);
         }

         var2 = this.hiddenServers.iterator();

         while(var2.hasNext()) {
            lv2 = (ServerInfo)var2.next();
            lv3 = lv2.toNbt();
            lv3.putBoolean("hidden", true);
            lv.add(lv3);
         }

         NbtCompound lv4 = new NbtCompound();
         lv4.put("servers", lv);
         File file = File.createTempFile("servers", ".dat", this.client.runDirectory);
         NbtIo.write(lv4, file);
         File file2 = new File(this.client.runDirectory, "servers.dat_old");
         File file3 = new File(this.client.runDirectory, "servers.dat");
         Util.backupAndReplace(file3, file, file2);
      } catch (Exception var6) {
         LOGGER.error("Couldn't save server list", var6);
      }

   }

   public ServerInfo get(int index) {
      return (ServerInfo)this.servers.get(index);
   }

   @Nullable
   public ServerInfo get(String address) {
      Iterator var2 = this.servers.iterator();

      ServerInfo lv;
      do {
         if (!var2.hasNext()) {
            var2 = this.hiddenServers.iterator();

            do {
               if (!var2.hasNext()) {
                  return null;
               }

               lv = (ServerInfo)var2.next();
            } while(!lv.address.equals(address));

            return lv;
         }

         lv = (ServerInfo)var2.next();
      } while(!lv.address.equals(address));

      return lv;
   }

   @Nullable
   public ServerInfo tryUnhide(String address) {
      for(int i = 0; i < this.hiddenServers.size(); ++i) {
         ServerInfo lv = (ServerInfo)this.hiddenServers.get(i);
         if (lv.address.equals(address)) {
            this.hiddenServers.remove(i);
            this.servers.add(lv);
            return lv;
         }
      }

      return null;
   }

   public void remove(ServerInfo serverInfo) {
      if (!this.servers.remove(serverInfo)) {
         this.hiddenServers.remove(serverInfo);
      }

   }

   public void add(ServerInfo serverInfo, boolean hidden) {
      if (hidden) {
         this.hiddenServers.add(0, serverInfo);

         while(this.hiddenServers.size() > 16) {
            this.hiddenServers.remove(this.hiddenServers.size() - 1);
         }
      } else {
         this.servers.add(serverInfo);
      }

   }

   public int size() {
      return this.servers.size();
   }

   public void swapEntries(int index1, int index2) {
      ServerInfo lv = this.get(index1);
      this.servers.set(index1, this.get(index2));
      this.servers.set(index2, lv);
      this.saveFile();
   }

   public void set(int index, ServerInfo serverInfo) {
      this.servers.set(index, serverInfo);
   }

   private static boolean replace(ServerInfo serverInfo, List serverInfos) {
      for(int i = 0; i < serverInfos.size(); ++i) {
         ServerInfo lv = (ServerInfo)serverInfos.get(i);
         if (lv.name.equals(serverInfo.name) && lv.address.equals(serverInfo.address)) {
            serverInfos.set(i, serverInfo);
            return true;
         }
      }

      return false;
   }

   public static void updateServerListEntry(ServerInfo serverInfo) {
      IO_EXECUTOR.send(() -> {
         ServerList lv = new ServerList(MinecraftClient.getInstance());
         lv.loadFile();
         if (!replace(serverInfo, lv.servers)) {
            replace(serverInfo, lv.hiddenServers);
         }

         lv.saveFile();
      });
   }
}
