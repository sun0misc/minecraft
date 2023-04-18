package net.minecraft.client.network;

import com.mojang.logging.LogUtils;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerInfo {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String name;
   public String address;
   public Text playerCountLabel;
   public Text label;
   @Nullable
   public ServerMetadata.Players players;
   public long ping;
   public int protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
   public Text version = Text.literal(SharedConstants.getGameVersion().getName());
   public boolean online;
   public List playerListSummary = Collections.emptyList();
   private ResourcePackPolicy resourcePackPolicy;
   @Nullable
   private byte[] favicon;
   private boolean local;
   private boolean secureChatEnforced;

   public ServerInfo(String name, String address, boolean local) {
      this.resourcePackPolicy = ServerInfo.ResourcePackPolicy.PROMPT;
      this.name = name;
      this.address = address;
      this.local = local;
   }

   public NbtCompound toNbt() {
      NbtCompound lv = new NbtCompound();
      lv.putString("name", this.name);
      lv.putString("ip", this.address);
      if (this.favicon != null) {
         lv.putString("icon", Base64.getEncoder().encodeToString(this.favicon));
      }

      if (this.resourcePackPolicy == ServerInfo.ResourcePackPolicy.ENABLED) {
         lv.putBoolean("acceptTextures", true);
      } else if (this.resourcePackPolicy == ServerInfo.ResourcePackPolicy.DISABLED) {
         lv.putBoolean("acceptTextures", false);
      }

      return lv;
   }

   public ResourcePackPolicy getResourcePackPolicy() {
      return this.resourcePackPolicy;
   }

   public void setResourcePackPolicy(ResourcePackPolicy resourcePackPolicy) {
      this.resourcePackPolicy = resourcePackPolicy;
   }

   public static ServerInfo fromNbt(NbtCompound root) {
      ServerInfo lv = new ServerInfo(root.getString("name"), root.getString("ip"), false);
      if (root.contains("icon", NbtElement.STRING_TYPE)) {
         try {
            lv.setFavicon(Base64.getDecoder().decode(root.getString("icon")));
         } catch (IllegalArgumentException var3) {
            LOGGER.warn("Malformed base64 server icon", var3);
         }
      }

      if (root.contains("acceptTextures", NbtElement.BYTE_TYPE)) {
         if (root.getBoolean("acceptTextures")) {
            lv.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.ENABLED);
         } else {
            lv.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.DISABLED);
         }
      } else {
         lv.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.PROMPT);
      }

      return lv;
   }

   @Nullable
   public byte[] getFavicon() {
      return this.favicon;
   }

   public void setFavicon(@Nullable byte[] favicon) {
      this.favicon = favicon;
   }

   public boolean isLocal() {
      return this.local;
   }

   public void setSecureChatEnforced(boolean secureChatEnforced) {
      this.secureChatEnforced = secureChatEnforced;
   }

   public boolean isSecureChatEnforced() {
      return this.secureChatEnforced;
   }

   public void copyFrom(ServerInfo serverInfo) {
      this.address = serverInfo.address;
      this.name = serverInfo.name;
      this.favicon = serverInfo.favicon;
   }

   public void copyWithSettingsFrom(ServerInfo serverInfo) {
      this.copyFrom(serverInfo);
      this.setResourcePackPolicy(serverInfo.getResourcePackPolicy());
      this.local = serverInfo.local;
      this.secureChatEnforced = serverInfo.secureChatEnforced;
   }

   @Environment(EnvType.CLIENT)
   public static enum ResourcePackPolicy {
      ENABLED("enabled"),
      DISABLED("disabled"),
      PROMPT("prompt");

      private final Text name;

      private ResourcePackPolicy(String name) {
         this.name = Text.translatable("addServer.resourcePack." + name);
      }

      public Text getName() {
         return this.name;
      }

      // $FF: synthetic method
      private static ResourcePackPolicy[] method_36896() {
         return new ResourcePackPolicy[]{ENABLED, DISABLED, PROMPT};
      }
   }
}
