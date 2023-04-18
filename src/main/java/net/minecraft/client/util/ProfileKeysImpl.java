package net.minecraft.client.util;

import com.google.common.base.Strings;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.PublicKey;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ProfileKeysImpl implements ProfileKeys {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Duration TIME_UNTIL_FIRST_EXPIRY_CHECK = Duration.ofHours(1L);
   private static final Path PROFILE_KEYS_PATH = Path.of("profilekeys");
   private final UserApiService userApiService;
   private final Path jsonPath;
   private CompletableFuture keyFuture;
   private Instant expiryCheckTime;

   public ProfileKeysImpl(UserApiService userApiService, UUID uuid, Path root) {
      this.expiryCheckTime = Instant.EPOCH;
      this.userApiService = userApiService;
      this.jsonPath = root.resolve(PROFILE_KEYS_PATH).resolve("" + uuid + ".json");
      this.keyFuture = CompletableFuture.supplyAsync(() -> {
         return this.loadKeyPairFromFile().filter((key) -> {
            return !key.publicKey().data().isExpired();
         });
      }, Util.getMainWorkerExecutor()).thenCompose(this::getKeyPair);
   }

   public CompletableFuture fetchKeyPair() {
      this.expiryCheckTime = Instant.now().plus(TIME_UNTIL_FIRST_EXPIRY_CHECK);
      this.keyFuture = this.keyFuture.thenCompose(this::getKeyPair);
      return this.keyFuture;
   }

   public boolean isExpired() {
      return this.keyFuture.isDone() && Instant.now().isAfter(this.expiryCheckTime) ? (Boolean)((Optional)this.keyFuture.join()).map(PlayerKeyPair::isExpired).orElse(true) : false;
   }

   private CompletableFuture getKeyPair(Optional currentKey) {
      return CompletableFuture.supplyAsync(() -> {
         if (currentKey.isPresent() && !((PlayerKeyPair)currentKey.get()).isExpired()) {
            if (!SharedConstants.isDevelopment) {
               this.saveKeyPairToFile((PlayerKeyPair)null);
            }

            return currentKey;
         } else {
            try {
               PlayerKeyPair lv = this.fetchKeyPair(this.userApiService);
               this.saveKeyPairToFile(lv);
               return Optional.of(lv);
            } catch (NetworkEncryptionException | MinecraftClientException | IOException var3) {
               LOGGER.error("Failed to retrieve profile key pair", var3);
               this.saveKeyPairToFile((PlayerKeyPair)null);
               return currentKey;
            }
         }
      }, Util.getMainWorkerExecutor());
   }

   private Optional loadKeyPairFromFile() {
      if (Files.notExists(this.jsonPath, new LinkOption[0])) {
         return Optional.empty();
      } else {
         try {
            BufferedReader bufferedReader = Files.newBufferedReader(this.jsonPath);

            Optional var2;
            try {
               var2 = PlayerKeyPair.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(bufferedReader)).result();
            } catch (Throwable var5) {
               if (bufferedReader != null) {
                  try {
                     bufferedReader.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (bufferedReader != null) {
               bufferedReader.close();
            }

            return var2;
         } catch (Exception var6) {
            LOGGER.error("Failed to read profile key pair file {}", this.jsonPath, var6);
            return Optional.empty();
         }
      }
   }

   private void saveKeyPairToFile(@Nullable PlayerKeyPair keyPair) {
      try {
         Files.deleteIfExists(this.jsonPath);
      } catch (IOException var3) {
         LOGGER.error("Failed to delete profile key pair file {}", this.jsonPath, var3);
      }

      if (keyPair != null) {
         if (SharedConstants.isDevelopment) {
            PlayerKeyPair.CODEC.encodeStart(JsonOps.INSTANCE, keyPair).result().ifPresent((json) -> {
               try {
                  Files.createDirectories(this.jsonPath.getParent());
                  Files.writeString(this.jsonPath, json.toString());
               } catch (Exception var3) {
                  LOGGER.error("Failed to write profile key pair file {}", this.jsonPath, var3);
               }

            });
         }
      }
   }

   private PlayerKeyPair fetchKeyPair(UserApiService userApiService) throws NetworkEncryptionException, IOException {
      KeyPairResponse keyPairResponse = userApiService.getKeyPair();
      if (keyPairResponse != null) {
         PlayerPublicKey.PublicKeyData lv = decodeKeyPairResponse(keyPairResponse);
         return new PlayerKeyPair(NetworkEncryptionUtils.decodeRsaPrivateKeyPem(keyPairResponse.getPrivateKey()), new PlayerPublicKey(lv), Instant.parse(keyPairResponse.getRefreshedAfter()));
      } else {
         throw new IOException("Could not retrieve profile key pair");
      }
   }

   private static PlayerPublicKey.PublicKeyData decodeKeyPairResponse(KeyPairResponse keyPairResponse) throws NetworkEncryptionException {
      if (!Strings.isNullOrEmpty(keyPairResponse.getPublicKey()) && keyPairResponse.getPublicKeySignature() != null && keyPairResponse.getPublicKeySignature().array().length != 0) {
         try {
            Instant instant = Instant.parse(keyPairResponse.getExpiresAt());
            PublicKey publicKey = NetworkEncryptionUtils.decodeRsaPublicKeyPem(keyPairResponse.getPublicKey());
            ByteBuffer byteBuffer = keyPairResponse.getPublicKeySignature();
            return new PlayerPublicKey.PublicKeyData(instant, publicKey, byteBuffer.array());
         } catch (IllegalArgumentException | DateTimeException var4) {
            throw new NetworkEncryptionException(var4);
         }
      } else {
         throw new NetworkEncryptionException(new InsecurePublicKeyException.MissingException());
      }
   }
}
