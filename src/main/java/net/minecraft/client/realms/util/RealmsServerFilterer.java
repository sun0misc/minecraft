package net.minecraft.client.realms.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.dto.RealmsServer;

@Environment(EnvType.CLIENT)
public class RealmsServerFilterer {
   private final MinecraftClient client;
   private final Set removedServers = Sets.newHashSet();
   private List sortedServers = Lists.newArrayList();

   public RealmsServerFilterer(MinecraftClient client) {
      this.client = client;
   }

   public List filterAndSort(List servers) {
      List list2 = new ArrayList(servers);
      list2.sort(new RealmsServer.McoServerComparator(this.client.getSession().getUsername()));
      boolean bl = list2.removeAll(this.removedServers);
      if (!bl) {
         this.removedServers.clear();
      }

      this.sortedServers = list2;
      return List.copyOf(this.sortedServers);
   }

   public synchronized List remove(RealmsServer server) {
      this.sortedServers.remove(server);
      this.removedServers.add(server);
      return List.copyOf(this.sortedServers);
   }
}
