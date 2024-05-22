/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.texture;

import java.util.Collection;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.TextureStitcher;

@Environment(value=EnvType.CLIENT)
public class TextureStitcherCannotFitException
extends RuntimeException {
    private final Collection<TextureStitcher.Stitchable> sprites;

    public TextureStitcherCannotFitException(TextureStitcher.Stitchable sprite, Collection<TextureStitcher.Stitchable> sprites) {
        super(String.format(Locale.ROOT, "Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", sprite.getId(), sprite.getWidth(), sprite.getHeight()));
        this.sprites = sprites;
    }

    public Collection<TextureStitcher.Stitchable> getSprites() {
        return this.sprites;
    }
}

