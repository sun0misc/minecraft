package net.minecraft.server.integrated;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.LanServerPinger;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ApiServices;
import net.minecraft.util.ModStatus;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameMode;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class IntegratedServer extends MinecraftServer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_34964 = 2;
   private final MinecraftClient client;
   private boolean paused = true;
   private int lanPort = -1;
   @Nullable
   private GameMode forcedGameMode;
   @Nullable
   private LanServerPinger lanPinger;
   @Nullable
   private UUID localPlayerUuid;
   private int simulationDistance = 0;

   public IntegratedServer(Thread serverThread, MinecraftClient client, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
      super(serverThread, session, dataPackManager, saveLoader, client.getNetworkProxy(), client.getDataFixer(), apiServices, worldGenerationProgressListenerFactory);
      this.setHostProfile(client.getSession().getProfile());
      this.setDemo(client.isDemo());
      this.setPlayerManager(new IntegratedPlayerManager(this, this.getCombinedDynamicRegistries(), this.saveHandler));
      this.client = client;
   }

   public boolean setupServer() {
      LOGGER.info("Starting integrated minecraft server version {}", SharedConstants.getGameVersion().getName());
      this.setOnlineMode(true);
      this.setPvpEnabled(true);
      this.setFlightEnabled(true);
      this.generateKeyPair();
      this.loadWorld();
      GameProfile gameProfile = this.getHostProfile();
      String string = this.getSaveProperties().getLevelName();
      this.setMotd(gameProfile != null ? gameProfile.getName() + " - " + string : string);
      return true;
   }

   public void tick(BooleanSupplier shouldKeepTicking) {
      boolean bl = this.paused;
      this.paused = MinecraftClient.getInstance().isPaused();
      Profiler lv = this.getProfiler();
      if (!bl && this.paused) {
         lv.push("autoSave");
         LOGGER.info("Saving and pausing game...");
         this.saveAll(false, false, false);
         lv.pop();
      }

      boolean bl2 = MinecraftClient.getInstance().getNetworkHandler() != null;
      if (bl2 && this.paused) {
         this.incrementTotalWorldTimeStat();
      } else {
         if (bl && !this.paused) {
            this.sendTimeUpdatePackets();
         }

         super.tick(shouldKeepTicking);
         int i = Math.max(2, (Integer)this.client.options.getViewDistance().getValue());
         if (i != this.getPlayerManager().getViewDistance()) {
            LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerManager().getViewDistance());
            this.getPlayerManager().setViewDistance(i);
         }

         int j = Math.max(2, (Integer)this.client.options.getSimulationDistance().getValue());
         if (j != this.simulationDistance) {
            LOGGER.info("Changing simulation distance to {}, from {}", j, this.simulationDistance);
            this.getPlayerManager().setSimulationDistance(j);
            this.simulationDistance = j;
         }

      }
   }

   private void incrementTotalWorldTimeStat() {
      Iterator var1 = this.getPlayerManager().getPlayerList().iterator();

      while(var1.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var1.next();
         lv.incrementStat(Stats.TOTAL_WORLD_TIME);
      }

   }

   public boolean shouldBroadcastRconToOps() {
      return true;
   }

   public boolean shouldBroadcastConsoleToOps() {
      return true;
   }

   public File getRunDirectory() {
      return this.client.runDirectory;
   }

   public boolean isDedicated() {
      return false;
   }

   public int getRateLimit() {
      return 0;
   }

   public boolean isUsingNativeTransport() {
      return false;
   }

   public void setCrashReport(CrashReport report) {
      this.client.setCrashReportSupplier(report);
   }

   public SystemDetails addExtraSystemDetails(SystemDetails details) {
      details.addSection("Type", "Integrated Server (map_client.txt)");
      details.addSection("Is Modded", () -> {
         return this.getModStatus().getMessage();
      });
      MinecraftClient var10002 = this.client;
      Objects.requireNonNull(var10002);
      details.addSection("Launched Version", var10002::getGameVersion);
      return details;
   }

   public ModStatus getModStatus() {
      return MinecraftClient.getModStatus().combine(super.getModStatus());
   }

   public boolean openToLan(@Nullable GameMode gameMode, boolean cheatsAllowed, int port) {
      try {
         this.client.loadBlockList();
         this.client.getProfileKeys().fetchKeyPair().thenAcceptAsync((keyPair) -> {
            keyPair.ifPresent((keys) -> {
               ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
               if (lv != null) {
                  lv.updateKeyPair(keys);
               }

            });
         }, this.client);
         this.getNetworkIo().bind((InetAddress)null, port);
         LOGGER.info("Started serving on {}", port);
         this.lanPort = port;
         this.lanPinger = new LanServerPinger(this.getServerMotd(), "" + port);
         this.lanPinger.start();
         this.forcedGameMode = gameMode;
         this.getPlayerManager().setCheatsAllowed(cheatsAllowed);
         int j = this.getPermissionLevel(this.client.player.getGameProfile());
         this.client.player.setClientPermissionLevel(j);
         Iterator var5 = this.getPlayerManager().getPlayerList().iterator();

         while(var5.hasNext()) {
            ServerPlayerEntity lv = (ServerPlayerEntity)var5.next();
            this.getCommandManager().sendCommandTree(lv);
         }

         return true;
      } catch (IOException var7) {
         return false;
      }
   }

   public void shutdown() {
      super.shutdown();
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public void stop(boolean waitForShutdown) {
      this.submitAndJoin(() -> {
         List list = Lists.newArrayList(this.getPlayerManager().getPlayerList());
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            ServerPlayerEntity lv = (ServerPlayerEntity)var2.next();
            if (!lv.getUuid().equals(this.localPlayerUuid)) {
               this.getPlayerManager().remove(lv);
            }
         }

      });
      super.stop(waitForShutdown);
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public boolean isRemote() {
      return this.lanPort > -1;
   }

   public int getServerPort() {
      return this.lanPort;
   }

   public void setDefaultGameMode(GameMode gameMode) {
      super.setDefaultGameMode(gameMode);
      this.forcedGameMode = null;
   }

   public boolean areCommandBlocksEnabled() {
      return true;
   }

   public int getOpPermissionLevel() {
      return 2;
   }

   public int getFunctionPermissionLevel() {
      return 2;
   }

   public void setLocalPlayerUuid(UUID localPlayerUuid) {
      this.localPlayerUuid = localPlayerUuid;
   }

   public boolean isHost(GameProfile profile) {
      return this.getHostProfile() != null && profile.getName().equalsIgnoreCase(this.getHostProfile().getName());
   }

   public int adjustTrackingDistance(int initialDistance) {
      return (int)((Double)this.client.options.getEntityDistanceScaling().getValue() * (double)initialDistance);
   }

   public boolean syncChunkWrites() {
      return this.client.options.syncChunkWrites;
   }

   @Nullable
   public GameMode getForcedGameMode() {
      return this.isRemote() ? (GameMode)MoreObjects.firstNonNull(this.forcedGameMode, this.saveProperties.getGameMode()) : null;
   }
}
