package net.minecraft.client.realms;

import com.google.common.collect.Lists;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.RegionPingResult;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;

@Environment(EnvType.CLIENT)
public class Ping {
   public static List ping(Region... regions) {
      Region[] var1 = regions;
      int var2 = regions.length;

      int var3;
      for(var3 = 0; var3 < var2; ++var3) {
         Region lv = var1[var3];
         ping(lv.endpoint);
      }

      List list = Lists.newArrayList();
      Region[] var7 = regions;
      var3 = regions.length;

      for(int var8 = 0; var8 < var3; ++var8) {
         Region lv2 = var7[var8];
         list.add(new RegionPingResult(lv2.name, ping(lv2.endpoint)));
      }

      list.sort(Comparator.comparingInt(RegionPingResult::getPing));
      return list;
   }

   private static int ping(String host) {
      int i = true;
      long l = 0L;
      Socket socket = null;

      for(int j = 0; j < 5; ++j) {
         try {
            SocketAddress socketAddress = new InetSocketAddress(host, 80);
            socket = new Socket();
            long m = now();
            socket.connect(socketAddress, 700);
            l += now() - m;
         } catch (Exception var12) {
            l += 700L;
         } finally {
            IOUtils.closeQuietly(socket);
         }
      }

      return (int)((double)l / 5.0);
   }

   private static long now() {
      return Util.getMeasuringTimeMs();
   }

   public static List pingAllRegions() {
      return ping(Ping.Region.values());
   }

   @Environment(EnvType.CLIENT)
   static enum Region {
      US_EAST_1("us-east-1", "ec2.us-east-1.amazonaws.com"),
      US_WEST_2("us-west-2", "ec2.us-west-2.amazonaws.com"),
      US_WEST_1("us-west-1", "ec2.us-west-1.amazonaws.com"),
      EU_WEST_1("eu-west-1", "ec2.eu-west-1.amazonaws.com"),
      AP_SOUTHEAST_1("ap-southeast-1", "ec2.ap-southeast-1.amazonaws.com"),
      AP_SOUTHEAST_2("ap-southeast-2", "ec2.ap-southeast-2.amazonaws.com"),
      AP_NORTHEAST_1("ap-northeast-1", "ec2.ap-northeast-1.amazonaws.com"),
      SA_EAST_1("sa-east-1", "ec2.sa-east-1.amazonaws.com");

      final String name;
      final String endpoint;

      private Region(String name, String endpoint) {
         this.name = name;
         this.endpoint = endpoint;
      }

      // $FF: synthetic method
      private static Region[] method_36845() {
         return new Region[]{US_EAST_1, US_WEST_2, US_WEST_1, EU_WEST_1, AP_SOUTHEAST_1, AP_SOUTHEAST_2, AP_NORTHEAST_1, SA_EAST_1};
      }
   }
}
