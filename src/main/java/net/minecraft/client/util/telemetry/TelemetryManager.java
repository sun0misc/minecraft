package net.minecraft.client.util.telemetry;

import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.minecraft.UserApiService;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TelemetryManager implements AutoCloseable {
   private static final AtomicInteger NEXT_WORKER_ID = new AtomicInteger(1);
   private static final Executor EXECUTOR = Executors.newSingleThreadExecutor((runnable) -> {
      Thread thread = new Thread(runnable);
      thread.setName("Telemetry-Sender-#" + NEXT_WORKER_ID.getAndIncrement());
      return thread;
   });
   private final UserApiService userApiService;
   private final PropertyMap propertyMap;
   private final Path logDirectory;
   private final CompletableFuture logManager;

   public TelemetryManager(MinecraftClient client, UserApiService userApiService, Session session) {
      this.userApiService = userApiService;
      PropertyMap.Builder lv = PropertyMap.builder();
      session.getXuid().ifPresent((xuid) -> {
         lv.put(TelemetryEventProperty.USER_ID, xuid);
      });
      session.getClientId().ifPresent((clientId) -> {
         lv.put(TelemetryEventProperty.CLIENT_ID, clientId);
      });
      lv.put(TelemetryEventProperty.MINECRAFT_SESSION_ID, UUID.randomUUID());
      lv.put(TelemetryEventProperty.GAME_VERSION, SharedConstants.getGameVersion().getId());
      lv.put(TelemetryEventProperty.OPERATING_SYSTEM, Util.getOperatingSystem().getName());
      lv.put(TelemetryEventProperty.PLATFORM, System.getProperty("os.name"));
      lv.put(TelemetryEventProperty.CLIENT_MODDED, MinecraftClient.getModStatus().isModded());
      this.propertyMap = lv.build();
      this.logDirectory = client.runDirectory.toPath().resolve("logs/telemetry");
      this.logManager = TelemetryLogManager.create(this.logDirectory);
   }

   public WorldSession createWorldSession(boolean newWorld, @Nullable Duration worldLoadTime) {
      return new WorldSession(this.getSender(), newWorld, worldLoadTime);
   }

   private TelemetrySender getSender() {
      if (SharedConstants.isDevelopment) {
         return TelemetrySender.NOOP;
      } else {
         TelemetrySession telemetrySession = this.userApiService.newTelemetrySession(EXECUTOR);
         if (!telemetrySession.isEnabled()) {
            return TelemetrySender.NOOP;
         } else {
            CompletableFuture completableFuture = this.logManager.thenCompose((manager) -> {
               return (CompletionStage)manager.map(TelemetryLogManager::getLogger).orElseGet(() -> {
                  return CompletableFuture.completedFuture(Optional.empty());
               });
            });
            return (arg, consumer) -> {
               if (!arg.isOptional() || MinecraftClient.getInstance().isOptionalTelemetryEnabled()) {
                  PropertyMap.Builder lv = PropertyMap.builder();
                  lv.putAll(this.propertyMap);
                  lv.put(TelemetryEventProperty.EVENT_TIMESTAMP_UTC, Instant.now());
                  lv.put(TelemetryEventProperty.OPT_IN, arg.isOptional());
                  consumer.accept(lv);
                  SentTelemetryEvent lv2 = new SentTelemetryEvent(arg, lv.build());
                  completableFuture.thenAccept((logger) -> {
                     if (!logger.isEmpty()) {
                        ((TelemetryLogger)logger.get()).log(lv2);
                        lv2.createEvent(telemetrySession).send();
                     }
                  });
               }
            };
         }
      }
   }

   public Path getLogManager() {
      return this.logDirectory;
   }

   public void close() {
      this.logManager.thenAccept((manager) -> {
         manager.ifPresent(TelemetryLogManager::close);
      });
   }
}
