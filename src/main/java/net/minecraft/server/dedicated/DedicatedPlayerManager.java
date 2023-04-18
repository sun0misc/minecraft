package net.minecraft.server.dedicated;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.world.WorldSaveHandler;
import org.slf4j.Logger;

public class DedicatedPlayerManager extends PlayerManager {
   private static final Logger LOGGER = LogUtils.getLogger();

   public DedicatedPlayerManager(MinecraftDedicatedServer server, CombinedDynamicRegistries tracker, WorldSaveHandler saveHandler) {
      super(server, tracker, saveHandler, server.getProperties().maxPlayers);
      ServerPropertiesHandler lv = server.getProperties();
      this.setViewDistance(lv.viewDistance);
      this.setSimulationDistance(lv.simulationDistance);
      super.setWhitelistEnabled((Boolean)lv.whiteList.get());
      this.loadUserBanList();
      this.saveUserBanList();
      this.loadIpBanList();
      this.saveIpBanList();
      this.loadOpList();
      this.loadWhitelist();
      this.saveOpList();
      if (!this.getWhitelist().getFile().exists()) {
         this.saveWhitelist();
      }

   }

   public void setWhitelistEnabled(boolean whitelistEnabled) {
      super.setWhitelistEnabled(whitelistEnabled);
      this.getServer().setUseWhitelist(whitelistEnabled);
   }

   public void addToOperators(GameProfile profile) {
      super.addToOperators(profile);
      this.saveOpList();
   }

   public void removeFromOperators(GameProfile profile) {
      super.removeFromOperators(profile);
      this.saveOpList();
   }

   public void reloadWhitelist() {
      this.loadWhitelist();
   }

   private void saveIpBanList() {
      try {
         this.getIpBanList().save();
      } catch (IOException var2) {
         LOGGER.warn("Failed to save ip banlist: ", var2);
      }

   }

   private void saveUserBanList() {
      try {
         this.getUserBanList().save();
      } catch (IOException var2) {
         LOGGER.warn("Failed to save user banlist: ", var2);
      }

   }

   private void loadIpBanList() {
      try {
         this.getIpBanList().load();
      } catch (IOException var2) {
         LOGGER.warn("Failed to load ip banlist: ", var2);
      }

   }

   private void loadUserBanList() {
      try {
         this.getUserBanList().load();
      } catch (IOException var2) {
         LOGGER.warn("Failed to load user banlist: ", var2);
      }

   }

   private void loadOpList() {
      try {
         this.getOpList().load();
      } catch (Exception var2) {
         LOGGER.warn("Failed to load operators list: ", var2);
      }

   }

   private void saveOpList() {
      try {
         this.getOpList().save();
      } catch (Exception var2) {
         LOGGER.warn("Failed to save operators list: ", var2);
      }

   }

   private void loadWhitelist() {
      try {
         this.getWhitelist().load();
      } catch (Exception var2) {
         LOGGER.warn("Failed to load white-list: ", var2);
      }

   }

   private void saveWhitelist() {
      try {
         this.getWhitelist().save();
      } catch (Exception var2) {
         LOGGER.warn("Failed to save white-list: ", var2);
      }

   }

   public boolean isWhitelisted(GameProfile profile) {
      return !this.isWhitelistEnabled() || this.isOperator(profile) || this.getWhitelist().isAllowed(profile);
   }

   public MinecraftDedicatedServer getServer() {
      return (MinecraftDedicatedServer)super.getServer();
   }

   public boolean canBypassPlayerLimit(GameProfile profile) {
      return this.getOpList().canBypassPlayerLimit(profile);
   }

   // $FF: synthetic method
   public MinecraftServer getServer() {
      return this.getServer();
   }
}
