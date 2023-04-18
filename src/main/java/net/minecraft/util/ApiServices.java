package net.minecraft.util;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import net.minecraft.network.encryption.SignatureVerifier;

public record ApiServices(MinecraftSessionService sessionService, SignatureVerifier serviceSignatureVerifier, GameProfileRepository profileRepository, UserCache userCache) {
   private static final String USER_CACHE_FILE_NAME = "usercache.json";

   public ApiServices(MinecraftSessionService minecraftSessionService, SignatureVerifier arg, GameProfileRepository gameProfileRepository, UserCache arg2) {
      this.sessionService = minecraftSessionService;
      this.serviceSignatureVerifier = arg;
      this.profileRepository = gameProfileRepository;
      this.userCache = arg2;
   }

   public static ApiServices create(YggdrasilAuthenticationService authenticationService, File rootDirectory) {
      MinecraftSessionService minecraftSessionService = authenticationService.createMinecraftSessionService();
      GameProfileRepository gameProfileRepository = authenticationService.createProfileRepository();
      UserCache lv = new UserCache(gameProfileRepository, new File(rootDirectory, "usercache.json"));
      SignatureVerifier lv2 = SignatureVerifier.create(authenticationService.getServicesKey());
      return new ApiServices(minecraftSessionService, lv2, gameProfileRepository, lv);
   }

   public MinecraftSessionService sessionService() {
      return this.sessionService;
   }

   public SignatureVerifier serviceSignatureVerifier() {
      return this.serviceSignatureVerifier;
   }

   public GameProfileRepository profileRepository() {
      return this.profileRepository;
   }

   public UserCache userCache() {
      return this.userCache;
   }
}
