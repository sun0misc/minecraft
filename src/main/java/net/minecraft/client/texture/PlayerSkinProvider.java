package net.minecraft.client.texture;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PlayerSkinProvider {
   public static final String TEXTURES = "textures";
   private final TextureManager textureManager;
   private final File skinCacheDir;
   private final MinecraftSessionService sessionService;
   private final LoadingCache skinCache;

   public PlayerSkinProvider(TextureManager textureManager, File skinCacheDir, final MinecraftSessionService sessionService) {
      this.textureManager = textureManager;
      this.skinCacheDir = skinCacheDir;
      this.sessionService = sessionService;
      this.skinCache = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader() {
         public Map load(String string) {
            GameProfile gameProfile = new GameProfile((UUID)null, "dummy_mcdummyface");
            gameProfile.getProperties().put("textures", new Property("textures", string, ""));

            try {
               return sessionService.getTextures(gameProfile, false);
            } catch (Throwable var4) {
               return ImmutableMap.of();
            }
         }

         // $FF: synthetic method
         public Object load(Object value) throws Exception {
            return this.load((String)value);
         }
      });
   }

   public Identifier loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type type) {
      return this.loadSkin(profileTexture, type, (SkinTextureAvailableCallback)null);
   }

   private Identifier loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type type, @Nullable SkinTextureAvailableCallback callback) {
      String string = Hashing.sha1().hashUnencodedChars(profileTexture.getHash()).toString();
      Identifier lv = getSkinId(type, string);
      AbstractTexture lv2 = this.textureManager.getOrDefault(lv, MissingSprite.getMissingSpriteTexture());
      if (lv2 == MissingSprite.getMissingSpriteTexture()) {
         File file = new File(this.skinCacheDir, string.length() > 2 ? string.substring(0, 2) : "xx");
         File file2 = new File(file, string);
         PlayerSkinTexture lv3 = new PlayerSkinTexture(file2, profileTexture.getUrl(), DefaultSkinHelper.getTexture(), type == Type.SKIN, () -> {
            if (callback != null) {
               callback.onSkinTextureAvailable(type, lv, profileTexture);
            }

         });
         this.textureManager.registerTexture(lv, lv3);
      } else if (callback != null) {
         callback.onSkinTextureAvailable(type, lv, profileTexture);
      }

      return lv;
   }

   private static Identifier getSkinId(MinecraftProfileTexture.Type skinType, String hash) {
      String var10000;
      switch (skinType) {
         case SKIN:
            var10000 = "skins";
            break;
         case CAPE:
            var10000 = "capes";
            break;
         case ELYTRA:
            var10000 = "elytra";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      String string2 = var10000;
      return new Identifier(string2 + "/" + hash);
   }

   public void loadSkin(GameProfile profile, SkinTextureAvailableCallback callback, boolean requireSecure) {
      Runnable runnable = () -> {
         Map map = Maps.newHashMap();

         try {
            map.putAll(this.sessionService.getTextures(profile, requireSecure));
         } catch (InsecureTextureException var7) {
         }

         if (map.isEmpty()) {
            profile.getProperties().clear();
            if (profile.getId().equals(MinecraftClient.getInstance().getSession().getProfile().getId())) {
               profile.getProperties().putAll(MinecraftClient.getInstance().getSessionProperties());
               map.putAll(this.sessionService.getTextures(profile, false));
            } else {
               this.sessionService.fillProfileProperties(profile, requireSecure);

               try {
                  map.putAll(this.sessionService.getTextures(profile, requireSecure));
               } catch (InsecureTextureException var6) {
               }
            }
         }

         MinecraftClient.getInstance().execute(() -> {
            RenderSystem.recordRenderCall(() -> {
               ImmutableList.of(Type.SKIN, Type.CAPE).forEach((textureType) -> {
                  if (map.containsKey(textureType)) {
                     this.loadSkin((MinecraftProfileTexture)map.get(textureType), textureType, callback);
                  }

               });
            });
         });
      };
      Util.getMainWorkerExecutor().execute(runnable);
   }

   public Map getTextures(GameProfile profile) {
      Property property = (Property)Iterables.getFirst(profile.getProperties().get("textures"), (Object)null);
      return (Map)(property == null ? ImmutableMap.of() : (Map)this.skinCache.getUnchecked(property.getValue()));
   }

   public Identifier loadSkin(GameProfile profile) {
      MinecraftProfileTexture minecraftProfileTexture = (MinecraftProfileTexture)this.getTextures(profile).get(Type.SKIN);
      return minecraftProfileTexture != null ? this.loadSkin(minecraftProfileTexture, Type.SKIN) : DefaultSkinHelper.getTexture(Uuids.getUuidFromProfile(profile));
   }

   @Environment(EnvType.CLIENT)
   public interface SkinTextureAvailableCallback {
      void onSkinTextureAvailable(MinecraftProfileTexture.Type type, Identifier id, MinecraftProfileTexture texture);
   }
}
