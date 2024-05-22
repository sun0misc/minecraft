/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class PaintingManager
extends SpriteAtlasHolder {
    private static final Identifier PAINTING_BACK_ID = Identifier.method_60656("back");

    public PaintingManager(TextureManager manager) {
        super(manager, Identifier.method_60656("textures/atlas/paintings.png"), Identifier.method_60656("paintings"));
    }

    public Sprite getPaintingSprite(PaintingVariant variant) {
        return this.getSprite(variant.assetId());
    }

    public Sprite getBackSprite() {
        return this.getSprite(PAINTING_BACK_ID);
    }
}

