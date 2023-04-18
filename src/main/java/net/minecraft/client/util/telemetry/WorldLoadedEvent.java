package net.minecraft.client.util.telemetry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WorldLoadedEvent {
   private boolean sent;
   @Nullable
   private TelemetryEventProperty.GameMode gameMode = null;
   @Nullable
   private String brand;

   public void putServerType(PropertyMap.Builder builder) {
      if (this.brand != null) {
         builder.put(TelemetryEventProperty.SERVER_MODDED, !this.brand.equals("vanilla"));
      }

      builder.put(TelemetryEventProperty.SERVER_TYPE, this.getServerType());
   }

   private TelemetryEventProperty.ServerType getServerType() {
      if (MinecraftClient.getInstance().isConnectedToRealms()) {
         return TelemetryEventProperty.ServerType.REALM;
      } else {
         return MinecraftClient.getInstance().isIntegratedServerRunning() ? TelemetryEventProperty.ServerType.LOCAL : TelemetryEventProperty.ServerType.OTHER;
      }
   }

   public boolean send(TelemetrySender sender) {
      if (!this.sent && this.gameMode != null && this.brand != null) {
         this.sent = true;
         sender.send(TelemetryEventType.WORLD_LOADED, (builder) -> {
            builder.put(TelemetryEventProperty.GAME_MODE, this.gameMode);
         });
         return true;
      } else {
         return false;
      }
   }

   public void setGameMode(GameMode gameMode, boolean hardcore) {
      TelemetryEventProperty.GameMode var10001;
      switch (gameMode) {
         case SURVIVAL:
            var10001 = hardcore ? TelemetryEventProperty.GameMode.HARDCORE : TelemetryEventProperty.GameMode.SURVIVAL;
            break;
         case CREATIVE:
            var10001 = TelemetryEventProperty.GameMode.CREATIVE;
            break;
         case ADVENTURE:
            var10001 = TelemetryEventProperty.GameMode.ADVENTURE;
            break;
         case SPECTATOR:
            var10001 = TelemetryEventProperty.GameMode.SPECTATOR;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      this.gameMode = var10001;
   }

   public void setBrand(String brand) {
      this.brand = brand;
   }
}
