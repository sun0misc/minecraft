package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.util.GlException;
import net.minecraft.client.util.Session;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.WinNativeModuleUtil;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.util.profiling.jfr.InstanceType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Main {
   static final Logger LOGGER = LogUtils.getLogger();

   @DontObfuscate
   public static void main(String[] args) {
      SharedConstants.createGameVersion();
      SharedConstants.enableDataFixerOptimization();
      OptionParser optionParser = new OptionParser();
      optionParser.allowsUnrecognizedOptions();
      optionParser.accepts("demo");
      optionParser.accepts("disableMultiplayer");
      optionParser.accepts("disableChat");
      optionParser.accepts("fullscreen");
      optionParser.accepts("checkGlErrors");
      OptionSpec optionSpec = optionParser.accepts("jfrProfile");
      OptionSpec optionSpec2 = optionParser.accepts("quickPlayPath").withRequiredArg();
      OptionSpec optionSpec3 = optionParser.accepts("quickPlaySingleplayer").withRequiredArg();
      OptionSpec optionSpec4 = optionParser.accepts("quickPlayMultiplayer").withRequiredArg();
      OptionSpec optionSpec5 = optionParser.accepts("quickPlayRealms").withRequiredArg();
      OptionSpec optionSpec6 = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."), new File[0]);
      OptionSpec optionSpec7 = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
      OptionSpec optionSpec8 = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
      OptionSpec optionSpec9 = optionParser.accepts("proxyHost").withRequiredArg();
      OptionSpec optionSpec10 = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo("8080", new String[0]).ofType(Integer.class);
      OptionSpec optionSpec11 = optionParser.accepts("proxyUser").withRequiredArg();
      OptionSpec optionSpec12 = optionParser.accepts("proxyPass").withRequiredArg();
      OptionSpec optionSpec13 = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMeasuringTimeMs() % 1000L, new String[0]);
      OptionSpec optionSpec14 = optionParser.accepts("uuid").withRequiredArg();
      OptionSpec optionSpec15 = optionParser.accepts("xuid").withOptionalArg().defaultsTo("", new String[0]);
      OptionSpec optionSpec16 = optionParser.accepts("clientId").withOptionalArg().defaultsTo("", new String[0]);
      OptionSpec optionSpec17 = optionParser.accepts("accessToken").withRequiredArg().required();
      OptionSpec optionSpec18 = optionParser.accepts("version").withRequiredArg().required();
      OptionSpec optionSpec19 = optionParser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854, new Integer[0]);
      OptionSpec optionSpec20 = optionParser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480, new Integer[0]);
      OptionSpec optionSpec21 = optionParser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
      OptionSpec optionSpec22 = optionParser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
      OptionSpec optionSpec23 = optionParser.accepts("userProperties").withRequiredArg().defaultsTo("{}", new String[0]);
      OptionSpec optionSpec24 = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo("{}", new String[0]);
      OptionSpec optionSpec25 = optionParser.accepts("assetIndex").withRequiredArg();
      OptionSpec optionSpec26 = optionParser.accepts("userType").withRequiredArg().defaultsTo(Session.AccountType.LEGACY.getName(), new String[0]);
      OptionSpec optionSpec27 = optionParser.accepts("versionType").withRequiredArg().defaultsTo("release", new String[0]);
      OptionSpec optionSpec28 = optionParser.nonOptions();
      OptionSet optionSet = optionParser.parse(args);
      List list = optionSet.valuesOf(optionSpec28);
      if (!list.isEmpty()) {
         System.out.println("Completely ignored arguments: " + list);
      }

      String string = (String)getOption(optionSet, optionSpec9);
      Proxy proxy = Proxy.NO_PROXY;
      if (string != null) {
         try {
            proxy = new Proxy(Type.SOCKS, new InetSocketAddress(string, (Integer)getOption(optionSet, optionSpec10)));
         } catch (Exception var81) {
         }
      }

      final String string2 = (String)getOption(optionSet, optionSpec11);
      final String string3 = (String)getOption(optionSet, optionSpec12);
      if (!proxy.equals(Proxy.NO_PROXY) && isNotNullOrEmpty(string2) && isNotNullOrEmpty(string3)) {
         Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(string2, string3.toCharArray());
            }
         });
      }

      int i = (Integer)getOption(optionSet, optionSpec19);
      int j = (Integer)getOption(optionSet, optionSpec20);
      OptionalInt optionalInt = toOptional((Integer)getOption(optionSet, optionSpec21));
      OptionalInt optionalInt2 = toOptional((Integer)getOption(optionSet, optionSpec22));
      boolean bl = optionSet.has("fullscreen");
      boolean bl2 = optionSet.has("demo");
      boolean bl3 = optionSet.has("disableMultiplayer");
      boolean bl4 = optionSet.has("disableChat");
      String string4 = (String)getOption(optionSet, optionSpec18);
      Gson gson = (new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create();
      PropertyMap propertyMap = (PropertyMap)JsonHelper.deserialize(gson, (String)getOption(optionSet, optionSpec23), PropertyMap.class);
      PropertyMap propertyMap2 = (PropertyMap)JsonHelper.deserialize(gson, (String)getOption(optionSet, optionSpec24), PropertyMap.class);
      String string5 = (String)getOption(optionSet, optionSpec27);
      File file = (File)getOption(optionSet, optionSpec6);
      File file2 = optionSet.has(optionSpec7) ? (File)getOption(optionSet, optionSpec7) : new File(file, "assets/");
      File file3 = optionSet.has(optionSpec8) ? (File)getOption(optionSet, optionSpec8) : new File(file, "resourcepacks/");
      String string6 = optionSet.has(optionSpec14) ? (String)optionSpec14.value(optionSet) : Uuids.getOfflinePlayerUuid((String)optionSpec13.value(optionSet)).toString();
      String string7 = optionSet.has(optionSpec25) ? (String)optionSpec25.value(optionSet) : null;
      String string8 = (String)optionSet.valueOf(optionSpec15);
      String string9 = (String)optionSet.valueOf(optionSpec16);
      String string10 = (String)getOption(optionSet, optionSpec2);
      String string11 = (String)getOption(optionSet, optionSpec3);
      String string12 = (String)getOption(optionSet, optionSpec4);
      String string13 = (String)getOption(optionSet, optionSpec5);
      if (optionSet.has(optionSpec)) {
         FlightProfiler.INSTANCE.start(InstanceType.CLIENT);
      }

      CrashReport.initCrashReport();
      Bootstrap.initialize();
      Bootstrap.logMissing();
      Util.startTimerHack();
      String string14 = (String)optionSpec26.value(optionSet);
      Session.AccountType lv = Session.AccountType.byName(string14);
      if (lv == null) {
         LOGGER.warn("Unrecognized user type: {}", string14);
      }

      Session lv2 = new Session((String)optionSpec13.value(optionSet), string6, (String)optionSpec17.value(optionSet), toOptional(string8), toOptional(string9), lv);
      RunArgs lv3 = new RunArgs(new RunArgs.Network(lv2, propertyMap, propertyMap2, proxy), new WindowSettings(i, j, optionalInt, optionalInt2, bl), new RunArgs.Directories(file, file3, file2, string7), new RunArgs.Game(bl2, string4, string5, bl3, bl4), new RunArgs.QuickPlay(string10, string11, string12, string13));
      Thread thread = new Thread("Client Shutdown Thread") {
         public void run() {
            MinecraftClient lv = MinecraftClient.getInstance();
            if (lv != null) {
               IntegratedServer lv2 = lv.getServer();
               if (lv2 != null) {
                  lv2.stop(true);
               }

            }
         }
      };
      thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
      Runtime.getRuntime().addShutdownHook(thread);

      final MinecraftClient lv4;
      try {
         Thread.currentThread().setName("Render thread");
         RenderSystem.initRenderThread();
         RenderSystem.beginInitialization();
         lv4 = new MinecraftClient(lv3);
         RenderSystem.finishInitialization();
      } catch (GlException var79) {
         LOGGER.warn("Failed to create window: ", var79);
         return;
      } catch (Throwable var80) {
         CrashReport lv6 = CrashReport.create(var80, "Initializing game");
         CrashReportSection lv7 = lv6.addElement("Initialization");
         WinNativeModuleUtil.addDetailTo(lv7);
         MinecraftClient.addSystemDetailsToCrashReport((MinecraftClient)null, (LanguageManager)null, (String)lv3.game.version, (GameOptions)null, (CrashReport)lv6);
         MinecraftClient.printCrashReport(lv6);
         return;
      }

      Thread thread2;
      if (lv4.shouldRenderAsync()) {
         thread2 = new Thread("Game thread") {
            public void run() {
               try {
                  RenderSystem.initGameThread(true);
                  lv4.run();
               } catch (Throwable var2) {
                  Main.LOGGER.error("Exception in client thread", var2);
               }

            }
         };
         thread2.start();

         while(true) {
            if (lv4.isRunning()) {
               continue;
            }
         }
      } else {
         thread2 = null;

         try {
            RenderSystem.initGameThread(false);
            lv4.run();
         } catch (Throwable var78) {
            LOGGER.error("Unhandled game exception", var78);
         }
      }

      BufferRenderer.reset();

      try {
         lv4.scheduleStop();
         if (thread2 != null) {
            thread2.join();
         }
      } catch (InterruptedException var76) {
         LOGGER.error("Exception during client thread shutdown", var76);
      } finally {
         lv4.stop();
      }

   }

   private static Optional toOptional(String string) {
      return string.isEmpty() ? Optional.empty() : Optional.of(string);
   }

   private static OptionalInt toOptional(@Nullable Integer i) {
      return i != null ? OptionalInt.of(i) : OptionalInt.empty();
   }

   @Nullable
   private static Object getOption(OptionSet optionSet, OptionSpec optionSpec) {
      try {
         return optionSet.valueOf(optionSpec);
      } catch (Throwable var5) {
         if (optionSpec instanceof ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec) {
            List list = argumentAcceptingOptionSpec.defaultValues();
            if (!list.isEmpty()) {
               return list.get(0);
            }
         }

         throw var5;
      }
   }

   private static boolean isNotNullOrEmpty(@Nullable String s) {
      return s != null && !s.isEmpty();
   }

   static {
      System.setProperty("java.awt.headless", "true");
   }
}
