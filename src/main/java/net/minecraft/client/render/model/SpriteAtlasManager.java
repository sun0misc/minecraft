/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.model;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpriteAtlasManager
implements AutoCloseable {
    private final Map<Identifier, Atlas> atlases;

    public SpriteAtlasManager(Map<Identifier, Identifier> loaders, TextureManager textureManager) {
        this.atlases = loaders.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            SpriteAtlasTexture lv = new SpriteAtlasTexture((Identifier)entry.getKey());
            textureManager.registerTexture((Identifier)entry.getKey(), lv);
            return new Atlas(lv, (Identifier)entry.getValue());
        }));
    }

    public SpriteAtlasTexture getAtlas(Identifier id) {
        return this.atlases.get(id).atlas();
    }

    @Override
    public void close() {
        this.atlases.values().forEach(Atlas::close);
        this.atlases.clear();
    }

    public Map<Identifier, CompletableFuture<AtlasPreparation>> reload(ResourceManager resourceManager, int mipmapLevels, Executor executor) {
        return this.atlases.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            Atlas lv = (Atlas)entry.getValue();
            return SpriteLoader.fromAtlas(lv.atlas).load(resourceManager, lv.atlasInfoLocation, mipmapLevels, executor).thenApply(stitchResult -> new AtlasPreparation(arg.atlas, (SpriteLoader.StitchResult)stitchResult));
        }));
    }

    @Environment(value=EnvType.CLIENT)
    record Atlas(SpriteAtlasTexture atlas, Identifier atlasInfoLocation) implements AutoCloseable
    {
        @Override
        public void close() {
            this.atlas.clear();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class AtlasPreparation {
        private final SpriteAtlasTexture atlasTexture;
        private final SpriteLoader.StitchResult stitchResult;

        public AtlasPreparation(SpriteAtlasTexture atlasTexture, SpriteLoader.StitchResult stitchResult) {
            this.atlasTexture = atlasTexture;
            this.stitchResult = stitchResult;
        }

        @Nullable
        public Sprite getSprite(Identifier id) {
            return this.stitchResult.regions().get(id);
        }

        public Sprite getMissingSprite() {
            return this.stitchResult.missing();
        }

        public CompletableFuture<Void> whenComplete() {
            return this.stitchResult.readyForUpload();
        }

        public void upload() {
            this.atlasTexture.upload(this.stitchResult);
        }
    }
}

