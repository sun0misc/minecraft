/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PlayerSkinTexture
extends ResourceTexture {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WIDTH = 64;
    private static final int HEIGHT = 64;
    private static final int OLD_HEIGHT = 32;
    @Nullable
    private final File cacheFile;
    private final String url;
    private final boolean convertLegacy;
    @Nullable
    private final Runnable loadedCallback;
    @Nullable
    private CompletableFuture<?> loader;
    private boolean loaded;

    public PlayerSkinTexture(@Nullable File cacheFile, String url, Identifier fallbackSkin, boolean convertLegacy, @Nullable Runnable callback) {
        super(fallbackSkin);
        this.cacheFile = cacheFile;
        this.url = url;
        this.convertLegacy = convertLegacy;
        this.loadedCallback = callback;
    }

    private void onTextureLoaded(NativeImage image) {
        if (this.loadedCallback != null) {
            this.loadedCallback.run();
        }
        MinecraftClient.getInstance().execute(() -> {
            this.loaded = true;
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> this.uploadTexture(image));
            } else {
                this.uploadTexture(image);
            }
        });
    }

    private void uploadTexture(NativeImage image) {
        TextureUtil.prepareImage(this.getGlId(), image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        NativeImage lv;
        MinecraftClient.getInstance().execute(() -> {
            if (!this.loaded) {
                try {
                    super.load(manager);
                } catch (IOException iOException) {
                    LOGGER.warn("Failed to load texture: {}", (Object)this.location, (Object)iOException);
                }
                this.loaded = true;
            }
        });
        if (this.loader != null) {
            return;
        }
        if (this.cacheFile != null && this.cacheFile.isFile()) {
            LOGGER.debug("Loading http texture from local cache ({})", (Object)this.cacheFile);
            FileInputStream fileInputStream = new FileInputStream(this.cacheFile);
            lv = this.loadTexture(fileInputStream);
        } else {
            lv = null;
        }
        if (lv != null) {
            this.onTextureLoaded(lv);
            return;
        }
        this.loader = CompletableFuture.runAsync(() -> {
            HttpURLConnection httpURLConnection = null;
            LOGGER.debug("Downloading http texture from {} to {}", (Object)this.url, (Object)this.cacheFile);
            try {
                InputStream inputStream;
                httpURLConnection = (HttpURLConnection)new URL(this.url).openConnection(MinecraftClient.getInstance().getNetworkProxy());
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(false);
                httpURLConnection.connect();
                if (httpURLConnection.getResponseCode() / 100 != 2) {
                    return;
                }
                if (this.cacheFile != null) {
                    FileUtils.copyInputStreamToFile(httpURLConnection.getInputStream(), this.cacheFile);
                    inputStream = new FileInputStream(this.cacheFile);
                } else {
                    inputStream = httpURLConnection.getInputStream();
                }
                MinecraftClient.getInstance().execute(() -> {
                    NativeImage lv = this.loadTexture(inputStream);
                    if (lv != null) {
                        this.onTextureLoaded(lv);
                    }
                });
            } catch (Exception exception) {
                LOGGER.error("Couldn't download http texture", exception);
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
        }, Util.getMainWorkerExecutor());
    }

    @Nullable
    private NativeImage loadTexture(InputStream stream) {
        NativeImage lv = null;
        try {
            lv = NativeImage.read(stream);
            if (this.convertLegacy) {
                lv = this.remapTexture(lv);
            }
        } catch (Exception exception) {
            LOGGER.warn("Error while loading the skin texture", exception);
        }
        return lv;
    }

    @Nullable
    private NativeImage remapTexture(NativeImage image) {
        boolean bl;
        int i = image.getHeight();
        int j = image.getWidth();
        if (j != 64 || i != 32 && i != 64) {
            image.close();
            LOGGER.warn("Discarding incorrectly sized ({}x{}) skin texture from {}", j, i, this.url);
            return null;
        }
        boolean bl2 = bl = i == 32;
        if (bl) {
            NativeImage lv = new NativeImage(64, 64, true);
            lv.copyFrom(image);
            image.close();
            image = lv;
            image.fillRect(0, 32, 64, 32, 0);
            image.copyRect(4, 16, 16, 32, 4, 4, true, false);
            image.copyRect(8, 16, 16, 32, 4, 4, true, false);
            image.copyRect(0, 20, 24, 32, 4, 12, true, false);
            image.copyRect(4, 20, 16, 32, 4, 12, true, false);
            image.copyRect(8, 20, 8, 32, 4, 12, true, false);
            image.copyRect(12, 20, 16, 32, 4, 12, true, false);
            image.copyRect(44, 16, -8, 32, 4, 4, true, false);
            image.copyRect(48, 16, -8, 32, 4, 4, true, false);
            image.copyRect(40, 20, 0, 32, 4, 12, true, false);
            image.copyRect(44, 20, -8, 32, 4, 12, true, false);
            image.copyRect(48, 20, -16, 32, 4, 12, true, false);
            image.copyRect(52, 20, -8, 32, 4, 12, true, false);
        }
        PlayerSkinTexture.stripAlpha(image, 0, 0, 32, 16);
        if (bl) {
            PlayerSkinTexture.stripColor(image, 32, 0, 64, 32);
        }
        PlayerSkinTexture.stripAlpha(image, 0, 16, 64, 32);
        PlayerSkinTexture.stripAlpha(image, 16, 48, 48, 64);
        return image;
    }

    private static void stripColor(NativeImage image, int x1, int y1, int x2, int y2) {
        int n;
        int m;
        for (m = x1; m < x2; ++m) {
            for (n = y1; n < y2; ++n) {
                int o = image.getColor(m, n);
                if ((o >> 24 & 0xFF) >= 128) continue;
                return;
            }
        }
        for (m = x1; m < x2; ++m) {
            for (n = y1; n < y2; ++n) {
                image.setColor(m, n, image.getColor(m, n) & 0xFFFFFF);
            }
        }
    }

    private static void stripAlpha(NativeImage image, int x1, int y1, int x2, int y2) {
        for (int m = x1; m < x2; ++m) {
            for (int n = y1; n < y2; ++n) {
                image.setColor(m, n, image.getColor(m, n) | 0xFF000000);
            }
        }
    }
}

