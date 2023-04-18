package net.minecraft.client.report;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.dto.RealmsServer;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record ReporterEnvironment(String clientVersion, @Nullable Server server) {
   public ReporterEnvironment(String string, @Nullable Server arg) {
      this.clientVersion = string;
      this.server = arg;
   }

   public static ReporterEnvironment ofIntegratedServer() {
      return ofServer((Server)null);
   }

   public static ReporterEnvironment ofThirdPartyServer(String ip) {
      return ofServer(new Server.ThirdParty(ip));
   }

   public static ReporterEnvironment ofRealm(RealmsServer server) {
      return ofServer(new Server.Realm(server));
   }

   public static ReporterEnvironment ofServer(@Nullable Server server) {
      return new ReporterEnvironment(getVersion(), server);
   }

   public AbuseReportRequest.ClientInfo toClientInfo() {
      return new AbuseReportRequest.ClientInfo(this.clientVersion, Locale.getDefault().toLanguageTag());
   }

   @Nullable
   public AbuseReportRequest.ThirdPartyServerInfo toThirdPartyServerInfo() {
      Server var2 = this.server;
      if (var2 instanceof Server.ThirdParty lv) {
         return new AbuseReportRequest.ThirdPartyServerInfo(lv.ip);
      } else {
         return null;
      }
   }

   @Nullable
   public AbuseReportRequest.RealmInfo toRealmInfo() {
      Server var2 = this.server;
      if (var2 instanceof Server.Realm lv) {
         return new AbuseReportRequest.RealmInfo(String.valueOf(lv.realmId()), lv.slotId());
      } else {
         return null;
      }
   }

   private static String getVersion() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("23w14a");
      if (MinecraftClient.getModStatus().isModded()) {
         stringBuilder.append(" (modded)");
      }

      return stringBuilder.toString();
   }

   public String clientVersion() {
      return this.clientVersion;
   }

   @Nullable
   public Server server() {
      return this.server;
   }

   @Environment(EnvType.CLIENT)
   public interface Server {
      @Environment(EnvType.CLIENT)
      public static record Realm(long realmId, int slotId) implements Server {
         public Realm(RealmsServer server) {
            this(server.id, server.activeSlot);
         }

         public Realm(long l, int i) {
            this.realmId = l;
            this.slotId = i;
         }

         public long realmId() {
            return this.realmId;
         }

         public int slotId() {
            return this.slotId;
         }
      }

      @Environment(EnvType.CLIENT)
      public static record ThirdParty(String ip) implements Server {
         final String ip;

         public ThirdParty(String string) {
            this.ip = string;
         }

         public String ip() {
            return this.ip;
         }
      }
   }
}
