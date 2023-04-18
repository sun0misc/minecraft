package net.minecraft.client.util;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Session {
   private final String username;
   private final String uuid;
   private final String accessToken;
   private final Optional xuid;
   private final Optional clientId;
   private final AccountType accountType;

   public Session(String username, String uuid, String accessToken, Optional xuid, Optional clientId, AccountType accountType) {
      this.username = username;
      this.uuid = uuid;
      this.accessToken = accessToken;
      this.xuid = xuid;
      this.clientId = clientId;
      this.accountType = accountType;
   }

   public String getSessionId() {
      return "token:" + this.accessToken + ":" + this.uuid;
   }

   public String getUuid() {
      return this.uuid;
   }

   public String getUsername() {
      return this.username;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public Optional getClientId() {
      return this.clientId;
   }

   public Optional getXuid() {
      return this.xuid;
   }

   @Nullable
   public UUID getUuidOrNull() {
      try {
         return UUIDTypeAdapter.fromString(this.getUuid());
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public GameProfile getProfile() {
      return new GameProfile(this.getUuidOrNull(), this.getUsername());
   }

   public AccountType getAccountType() {
      return this.accountType;
   }

   @Environment(EnvType.CLIENT)
   public static enum AccountType {
      LEGACY("legacy"),
      MOJANG("mojang"),
      MSA("msa");

      private static final Map BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap((type) -> {
         return type.name;
      }, Function.identity()));
      private final String name;

      private AccountType(String name) {
         this.name = name;
      }

      @Nullable
      public static AccountType byName(String name) {
         return (AccountType)BY_NAME.get(name.toLowerCase(Locale.ROOT));
      }

      public String getName() {
         return this.name;
      }

      // $FF: synthetic method
      private static AccountType[] method_36868() {
         return new AccountType[]{LEGACY, MOJANG, MSA};
      }
   }
}
