/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.font;

import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.texture.NativeImage;

@Environment(value=EnvType.CLIENT)
public enum BuiltinEmptyGlyph implements Glyph
{
    WHITE(() -> BuiltinEmptyGlyph.createRectImage(5, 8, (x, y) -> -1)),
    MISSING(() -> {
        int i = 5;
        int j = 8;
        return BuiltinEmptyGlyph.createRectImage(5, 8, (x, y) -> {
            boolean bl = x == 0 || x + 1 == 5 || y == 0 || y + 1 == 8;
            return bl ? -1 : 0;
        });
    });

    final NativeImage image;

    private static NativeImage createRectImage(int width, int height, ColorSupplier colorSupplier) {
        NativeImage lv = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        for (int k = 0; k < height; ++k) {
            for (int l = 0; l < width; ++l) {
                lv.setColor(l, k, colorSupplier.getColor(l, k));
            }
        }
        lv.untrack();
        return lv;
    }

    private BuiltinEmptyGlyph(Supplier<NativeImage> imageSupplier) {
        this.image = imageSupplier.get();
    }

    @Override
    public float getAdvance() {
        return this.image.getWidth() + 1;
    }

    @Override
    public GlyphRenderer bake(Function<RenderableGlyph, GlyphRenderer> function) {
        return function.apply(new RenderableGlyph(){

            @Override
            public int getWidth() {
                return BuiltinEmptyGlyph.this.image.getWidth();
            }

            @Override
            public int getHeight() {
                return BuiltinEmptyGlyph.this.image.getHeight();
            }

            @Override
            public float getOversample() {
                return 1.0f;
            }

            @Override
            public void upload(int x, int y) {
                BuiltinEmptyGlyph.this.image.upload(0, x, y, false);
            }

            @Override
            public boolean hasColor() {
                return true;
            }
        });
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface ColorSupplier {
        public int getColor(int var1, int var2);
    }
}

