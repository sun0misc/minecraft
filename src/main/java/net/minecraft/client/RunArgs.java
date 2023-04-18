package net.minecraft.client;

import com.mojang.authlib.properties.PropertyMap;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.ResourceIndex;
import net.minecraft.client.util.Session;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RunArgs {
   public final Network network;
   public final WindowSettings windowSettings;
   public final Directories directories;
   public final Game game;
   public final QuickPlay quickPlay;

   public RunArgs(Network network, WindowSettings windowSettings, Directories dirs, Game game, QuickPlay quickPlay) {
      this.network = network;
      this.windowSettings = windowSettings;
      this.directories = dirs;
      this.game = game;
      this.quickPlay = quickPlay;
   }

   @Environment(EnvType.CLIENT)
   public static class Network {
      public final Session session;
      public final PropertyMap userProperties;
      public final PropertyMap profileProperties;
      public final Proxy netProxy;

      public Network(Session session, PropertyMap userProperties, PropertyMap profileProperties, Proxy proxy) {
         this.session = session;
         this.userProperties = userProperties;
         this.profileProperties = profileProperties;
         this.netProxy = proxy;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Directories {
      public final File runDir;
      public final File resourcePackDir;
      public final File assetDir;
      @Nullable
      public final String assetIndex;

      public Directories(File runDir, File resPackDir, File assetDir, @Nullable String assetIndex) {
         this.runDir = runDir;
         this.resourcePackDir = resPackDir;
         this.assetDir = assetDir;
         this.assetIndex = assetIndex;
      }

      public Path getAssetDir() {
         return this.assetIndex == null ? this.assetDir.toPath() : ResourceIndex.buildFileSystem(this.assetDir.toPath(), this.assetIndex);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Game {
      public final boolean demo;
      public final String version;
      public final String versionType;
      public final boolean multiplayerDisabled;
      public final boolean onlineChatDisabled;

      public Game(boolean demo, String version, String versionType, boolean multiplayerDisabled, boolean onlineChatDisabled) {
         this.demo = demo;
         this.version = version;
         this.versionType = versionType;
         this.multiplayerDisabled = multiplayerDisabled;
         this.onlineChatDisabled = onlineChatDisabled;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record QuickPlay(@Nullable String path, @Nullable String singleplayer, @Nullable String multiplayer, @Nullable String realms) {
      public QuickPlay(@Nullable String string, @Nullable String string2, @Nullable String string3, @Nullable String string4) {
         this.path = string;
         this.singleplayer = string2;
         this.multiplayer = string3;
         this.realms = string4;
      }

      public boolean isEnabled() {
         return this.singleplayer != null || this.multiplayer != null || this.realms != null;
      }

      @Nullable
      public String path() {
         return this.path;
      }

      @Nullable
      public String singleplayer() {
         return this.singleplayer;
      }

      @Nullable
      public String multiplayer() {
         return this.multiplayer;
      }

      @Nullable
      public String realms() {
         return this.realms;
      }
   }
}
