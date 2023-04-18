package net.minecraft.client.realms.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RealmsUtil {
   static final MinecraftSessionService SESSION_SERVICE = MinecraftClient.getInstance().getSessionService();
   private static final LoadingCache gameProfileCache;
   private static final int SECONDS_PER_MINUTE = 60;
   private static final int SECONDS_PER_HOUR = 3600;
   private static final int SECONDS_PER_DAY = 86400;

   public static String uuidToName(String uuid) {
      return ((GameProfile)gameProfileCache.getUnchecked(uuid)).getName();
   }

   public static GameProfile uuidToProfile(String uuid) {
      return (GameProfile)gameProfileCache.getUnchecked(uuid);
   }

   public static String convertToAgePresentation(long milliseconds) {
      if (milliseconds < 0L) {
         return "right now";
      } else {
         long m = milliseconds / 1000L;
         String var10000;
         if (m < 60L) {
            var10000 = m == 1L ? "1 second" : "" + m + " seconds";
            return var10000 + " ago";
         } else {
            long n;
            if (m < 3600L) {
               n = m / 60L;
               var10000 = n == 1L ? "1 minute" : "" + n + " minutes";
               return var10000 + " ago";
            } else if (m < 86400L) {
               n = m / 3600L;
               var10000 = n == 1L ? "1 hour" : "" + n + " hours";
               return var10000 + " ago";
            } else {
               n = m / 86400L;
               var10000 = n == 1L ? "1 day" : "" + n + " days";
               return var10000 + " ago";
            }
         }
      }
   }

   public static String convertToAgePresentation(Date date) {
      return convertToAgePresentation(System.currentTimeMillis() - date.getTime());
   }

   public static void drawPlayerHead(MatrixStack matrices, int x, int y, int size, String uuid) {
      GameProfile gameProfile = uuidToProfile(uuid);
      Identifier lv = MinecraftClient.getInstance().getSkinProvider().loadSkin(gameProfile);
      RenderSystem.setShaderTexture(0, lv);
      PlayerSkinDrawer.draw(matrices, x, y, size);
   }

   static {
      gameProfileCache = CacheBuilder.newBuilder().expireAfterWrite(60L, TimeUnit.MINUTES).build(new CacheLoader() {
         public GameProfile load(String string) {
            return RealmsUtil.SESSION_SERVICE.fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(string), (String)null), false);
         }

         // $FF: synthetic method
         public Object load(Object uuid) throws Exception {
            return this.load((String)uuid);
         }
      });
   }
}
