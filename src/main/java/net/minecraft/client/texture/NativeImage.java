/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.texture;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.FreeTypeUtil;
import net.minecraft.client.util.Untracker;
import net.minecraft.util.PngMetadata;
import net.minecraft.util.math.ColorHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public final class NativeImage
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<StandardOpenOption> WRITE_TO_FILE_OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    private final Format format;
    private final int width;
    private final int height;
    private final boolean isStbImage;
    private long pointer;
    private final long sizeBytes;

    public NativeImage(int width, int height, boolean useStb) {
        this(Format.RGBA, width, height, useStb);
    }

    public NativeImage(Format format, int width, int height, boolean useStb) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + width + "x" + height);
        }
        this.format = format;
        this.width = width;
        this.height = height;
        this.sizeBytes = (long)width * (long)height * (long)format.getChannelCount();
        this.isStbImage = false;
        this.pointer = useStb ? MemoryUtil.nmemCalloc(1L, this.sizeBytes) : MemoryUtil.nmemAlloc(this.sizeBytes);
        if (this.pointer == 0L) {
            throw new IllegalStateException("Unable to allocate texture of size " + width + "x" + height + " (" + format.getChannelCount() + " channels)");
        }
    }

    private NativeImage(Format format, int width, int height, boolean useStb, long pointer) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + width + "x" + height);
        }
        this.format = format;
        this.width = width;
        this.height = height;
        this.isStbImage = useStb;
        this.pointer = pointer;
        this.sizeBytes = (long)width * (long)height * (long)format.getChannelCount();
    }

    public String toString() {
        return "NativeImage[" + String.valueOf((Object)this.format) + " " + this.width + "x" + this.height + "@" + this.pointer + (this.isStbImage ? "S" : "N") + "]";
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= this.width || y < 0 || y >= this.height;
    }

    public static NativeImage read(InputStream stream) throws IOException {
        return NativeImage.read(Format.RGBA, stream);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeImage read(@Nullable Format format, InputStream stream) throws IOException {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = TextureUtil.readResource(stream);
            byteBuffer.rewind();
            NativeImage nativeImage = NativeImage.read(format, byteBuffer);
            return nativeImage;
        } finally {
            MemoryUtil.memFree(byteBuffer);
            IOUtils.closeQuietly(stream);
        }
    }

    public static NativeImage read(ByteBuffer buffer) throws IOException {
        return NativeImage.read(Format.RGBA, buffer);
    }

    public static NativeImage read(byte[] bytes) throws IOException {
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(bytes.length);
            byteBuffer.put(bytes);
            byteBuffer.rewind();
            NativeImage nativeImage = NativeImage.read(byteBuffer);
            return nativeImage;
        }
    }

    public static NativeImage read(@Nullable Format format, ByteBuffer buffer) throws IOException {
        if (format != null && !format.isWriteable()) {
            throw new UnsupportedOperationException("Don't know how to read format " + String.valueOf((Object)format));
        }
        if (MemoryUtil.memAddress(buffer) == 0L) {
            throw new IllegalArgumentException("Invalid buffer");
        }
        PngMetadata.validate(buffer);
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            ByteBuffer byteBuffer2 = STBImage.stbi_load_from_memory(buffer, intBuffer, intBuffer2, intBuffer3, format == null ? 0 : format.channelCount);
            if (byteBuffer2 == null) {
                throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }
            NativeImage nativeImage = new NativeImage(format == null ? Format.fromChannelCount(intBuffer3.get(0)) : format, intBuffer.get(0), intBuffer2.get(0), true, MemoryUtil.memAddress(byteBuffer2));
            return nativeImage;
        }
    }

    private static void setTextureFilter(boolean blur, boolean mipmap) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (blur) {
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, mipmap ? GlConst.GL_LINEAR_MIPMAP_LINEAR : GlConst.GL_LINEAR);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
        } else {
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, mipmap ? GlConst.GL_NEAREST_MIPMAP_LINEAR : GlConst.GL_NEAREST);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
        }
    }

    private void checkAllocated() {
        if (this.pointer == 0L) {
            throw new IllegalStateException("Image is not allocated.");
        }
    }

    @Override
    public void close() {
        if (this.pointer != 0L) {
            if (this.isStbImage) {
                STBImage.nstbi_image_free(this.pointer);
            } else {
                MemoryUtil.nmemFree(this.pointer);
            }
        }
        this.pointer = 0L;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Format getFormat() {
        return this.format;
    }

    public int getColor(int x, int y) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)x + (long)y * (long)this.width) * 4L;
        return MemoryUtil.memGetInt(this.pointer + l);
    }

    public void setColor(int x, int y, int color) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)x + (long)y * (long)this.width) * 4L;
        MemoryUtil.memPutInt(this.pointer + l, color);
    }

    public NativeImage applyToCopy(IntUnaryOperator operator) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", new Object[]{this.format}));
        }
        this.checkAllocated();
        NativeImage lv = new NativeImage(this.width, this.height, false);
        int i = this.width * this.height;
        IntBuffer intBuffer = MemoryUtil.memIntBuffer(this.pointer, i);
        IntBuffer intBuffer2 = MemoryUtil.memIntBuffer(lv.pointer, i);
        for (int j = 0; j < i; ++j) {
            intBuffer2.put(j, operator.applyAsInt(intBuffer.get(j)));
        }
        return lv;
    }

    public void apply(IntUnaryOperator operator) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", new Object[]{this.format}));
        }
        this.checkAllocated();
        int i = this.width * this.height;
        IntBuffer intBuffer = MemoryUtil.memIntBuffer(this.pointer, i);
        for (int j = 0; j < i; ++j) {
            intBuffer.put(j, operator.applyAsInt(intBuffer.get(j)));
        }
    }

    public int[] copyPixelsRgba() {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelsRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        this.checkAllocated();
        int[] is = new int[this.width * this.height];
        MemoryUtil.memIntBuffer(this.pointer, this.width * this.height).get(is);
        return is;
    }

    public void setLuminance(int x, int y, byte luminance) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminance()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelLuminance only works on image with luminance; have %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)x + (long)y * (long)this.width) * (long)this.format.getChannelCount() + (long)(this.format.getLuminanceOffset() / 8);
        MemoryUtil.memPutByte(this.pointer + l, luminance);
    }

    public byte getRed(int x, int y) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasRedChannel()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no red or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        int k = (x + y * this.width) * this.format.getChannelCount() + this.format.getRedChannelOffset() / 8;
        return MemoryUtil.memGetByte(this.pointer + (long)k);
    }

    public byte getGreen(int x, int y) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasGreenChannel()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no green or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        int k = (x + y * this.width) * this.format.getChannelCount() + this.format.getGreenChannelOffset() / 8;
        return MemoryUtil.memGetByte(this.pointer + (long)k);
    }

    public byte getBlue(int x, int y) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasBlueChannel()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no blue or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        int k = (x + y * this.width) * this.format.getChannelCount() + this.format.getBlueChannelOffset() / 8;
        return MemoryUtil.memGetByte(this.pointer + (long)k);
    }

    public byte getOpacity(int x, int y) {
        if (!this.format.hasOpacityChannel()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no luminance or alpha in %s", new Object[]{this.format}));
        }
        if (this.isOutOfBounds(x, y)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
        }
        int k = (x + y * this.width) * this.format.getChannelCount() + this.format.getOpacityChannelOffset() / 8;
        return MemoryUtil.memGetByte(this.pointer + (long)k);
    }

    public void blend(int x, int y, int color) {
        if (this.format != Format.RGBA) {
            throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
        }
        int l = this.getColor(x, y);
        float f = (float)ColorHelper.Abgr.getAlpha(color) / 255.0f;
        float g = (float)ColorHelper.Abgr.getBlue(color) / 255.0f;
        float h = (float)ColorHelper.Abgr.getGreen(color) / 255.0f;
        float m = (float)ColorHelper.Abgr.getRed(color) / 255.0f;
        float n = (float)ColorHelper.Abgr.getAlpha(l) / 255.0f;
        float o = (float)ColorHelper.Abgr.getBlue(l) / 255.0f;
        float p = (float)ColorHelper.Abgr.getGreen(l) / 255.0f;
        float q = (float)ColorHelper.Abgr.getRed(l) / 255.0f;
        float r = f;
        float s = 1.0f - f;
        float t = f * r + n * s;
        float u = g * r + o * s;
        float v = h * r + p * s;
        float w = m * r + q * s;
        if (t > 1.0f) {
            t = 1.0f;
        }
        if (u > 1.0f) {
            u = 1.0f;
        }
        if (v > 1.0f) {
            v = 1.0f;
        }
        if (w > 1.0f) {
            w = 1.0f;
        }
        int x2 = (int)(t * 255.0f);
        int y2 = (int)(u * 255.0f);
        int z = (int)(v * 255.0f);
        int aa = (int)(w * 255.0f);
        this.setColor(x, y, ColorHelper.Abgr.getAbgr(x2, y2, z, aa));
    }

    @Deprecated
    public int[] makePixelArray() {
        if (this.format != Format.RGBA) {
            throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
        }
        this.checkAllocated();
        int[] is = new int[this.getWidth() * this.getHeight()];
        for (int i = 0; i < this.getHeight(); ++i) {
            for (int j = 0; j < this.getWidth(); ++j) {
                int k = this.getColor(j, i);
                is[j + i * this.getWidth()] = ColorHelper.Argb.getArgb(ColorHelper.Abgr.getAlpha(k), ColorHelper.Abgr.getRed(k), ColorHelper.Abgr.getGreen(k), ColorHelper.Abgr.getBlue(k));
            }
        }
        return is;
    }

    public void upload(int level, int offsetX, int offsetY, boolean close) {
        this.upload(level, offsetX, offsetY, 0, 0, this.width, this.height, false, close);
    }

    public void upload(int level, int offsetX, int offsetY, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean mipmap, boolean close) {
        this.upload(level, offsetX, offsetY, unpackSkipPixels, unpackSkipRows, width, height, false, false, mipmap, close);
    }

    public void upload(int level, int offsetX, int offsetY, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean blur, boolean clamp, boolean mipmap, boolean close) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.uploadInternal(level, offsetX, offsetY, unpackSkipPixels, unpackSkipRows, width, height, blur, clamp, mipmap, close));
        } else {
            this.uploadInternal(level, offsetX, offsetY, unpackSkipPixels, unpackSkipRows, width, height, blur, clamp, mipmap, close);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void uploadInternal(int level, int offsetX, int offsetY, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean blur, boolean clamp, boolean mipmap, boolean close) {
        try {
            RenderSystem.assertOnRenderThreadOrInit();
            this.checkAllocated();
            NativeImage.setTextureFilter(blur, mipmap);
            if (width == this.getWidth()) {
                GlStateManager._pixelStore(GlConst.GL_UNPACK_ROW_LENGTH, 0);
            } else {
                GlStateManager._pixelStore(GlConst.GL_UNPACK_ROW_LENGTH, this.getWidth());
            }
            GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_PIXELS, unpackSkipPixels);
            GlStateManager._pixelStore(GlConst.GL_UNPACK_SKIP_ROWS, unpackSkipRows);
            this.format.setUnpackAlignment();
            GlStateManager._texSubImage2D(GlConst.GL_TEXTURE_2D, level, offsetX, offsetY, width, height, this.format.toGl(), GlConst.GL_UNSIGNED_BYTE, this.pointer);
            if (clamp) {
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
            }
        } finally {
            if (close) {
                this.close();
            }
        }
    }

    public void loadFromTextureImage(int level, boolean removeAlpha) {
        RenderSystem.assertOnRenderThread();
        this.checkAllocated();
        this.format.setPackAlignment();
        GlStateManager._getTexImage(GlConst.GL_TEXTURE_2D, level, this.format.toGl(), 5121, this.pointer);
        if (removeAlpha && this.format.hasAlpha()) {
            for (int j = 0; j < this.getHeight(); ++j) {
                for (int k = 0; k < this.getWidth(); ++k) {
                    this.setColor(k, j, this.getColor(k, j) | 255 << this.format.getAlphaOffset());
                }
            }
        }
    }

    public void readDepthComponent(float unused) {
        RenderSystem.assertOnRenderThread();
        if (this.format.getChannelCount() != 1) {
            throw new IllegalStateException("Depth buffer must be stored in NativeImage with 1 component.");
        }
        this.checkAllocated();
        this.format.setPackAlignment();
        GlStateManager._readPixels(0, 0, this.width, this.height, GlConst.GL_DEPTH_COMPONENT, GlConst.GL_UNSIGNED_BYTE, this.pointer);
    }

    public void drawPixels() {
        RenderSystem.assertOnRenderThread();
        this.format.setUnpackAlignment();
        GlStateManager._glDrawPixels(this.width, this.height, this.format.toGl(), GlConst.GL_UNSIGNED_BYTE, this.pointer);
    }

    public void writeTo(File path) throws IOException {
        this.writeTo(path.toPath());
    }

    public boolean makeGlyphBitmapSubpixel(FT_Face face, int glyphIndex) {
        if (this.format.getChannelCount() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
        }
        if (FreeTypeUtil.checkError(FreeType.FT_Load_Glyph(face, glyphIndex, 4), "Loading glyph")) {
            return false;
        }
        FT_GlyphSlot fT_GlyphSlot = Objects.requireNonNull(face.glyph(), "Glyph not initialized");
        FT_Bitmap fT_Bitmap = fT_GlyphSlot.bitmap();
        if (fT_Bitmap.pixel_mode() != 2) {
            throw new IllegalStateException("Rendered glyph was not 8-bit grayscale");
        }
        if (fT_Bitmap.width() != this.getWidth() || fT_Bitmap.rows() != this.getHeight()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Glyph bitmap of size %sx%s does not match image of size: %sx%s", fT_Bitmap.width(), fT_Bitmap.rows(), this.getWidth(), this.getHeight()));
        }
        int j = fT_Bitmap.width() * fT_Bitmap.rows();
        ByteBuffer byteBuffer = Objects.requireNonNull(fT_Bitmap.buffer(j), "Glyph has no bitmap");
        MemoryUtil.memCopy(MemoryUtil.memAddress(byteBuffer), this.pointer, j);
        return true;
    }

    public void writeTo(Path path) throws IOException {
        if (!this.format.isWriteable()) {
            throw new UnsupportedOperationException("Don't know how to write format " + String.valueOf((Object)this.format));
        }
        this.checkAllocated();
        try (SeekableByteChannel writableByteChannel = Files.newByteChannel(path, WRITE_TO_FILE_OPEN_OPTIONS, new FileAttribute[0]);){
            if (!this.write(writableByteChannel)) {
                throw new IOException("Could not write image to the PNG file \"" + String.valueOf(path.toAbsolutePath()) + "\": " + STBImage.stbi_failure_reason());
            }
        }
    }

    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            byte[] byArray;
            block12: {
                WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
                try {
                    if (!this.write(writableByteChannel)) {
                        throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
                    }
                    byArray = byteArrayOutputStream.toByteArray();
                    if (writableByteChannel == null) break block12;
                } catch (Throwable throwable) {
                    if (writableByteChannel != null) {
                        try {
                            writableByteChannel.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                writableByteChannel.close();
            }
            return byArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean write(WritableByteChannel channel) throws IOException {
        WriteCallback lv = new WriteCallback(channel);
        try {
            int i = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.getChannelCount());
            if (i < this.getHeight()) {
                LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", (Object)this.getHeight(), (Object)i);
            }
            if (STBImageWrite.nstbi_write_png_to_func(lv.address(), 0L, this.getWidth(), i, this.format.getChannelCount(), this.pointer, 0) == 0) {
                boolean bl = false;
                return bl;
            }
            lv.throwStoredException();
            boolean bl = true;
            return bl;
        } finally {
            lv.free();
        }
    }

    public void copyFrom(NativeImage image) {
        if (image.getFormat() != this.format) {
            throw new UnsupportedOperationException("Image formats don't match.");
        }
        int i = this.format.getChannelCount();
        this.checkAllocated();
        image.checkAllocated();
        if (this.width == image.width) {
            MemoryUtil.memCopy(image.pointer, this.pointer, Math.min(this.sizeBytes, image.sizeBytes));
        } else {
            int j = Math.min(this.getWidth(), image.getWidth());
            int k = Math.min(this.getHeight(), image.getHeight());
            for (int l = 0; l < k; ++l) {
                int m = l * image.getWidth() * i;
                int n = l * this.getWidth() * i;
                MemoryUtil.memCopy(image.pointer + (long)m, this.pointer + (long)n, j);
            }
        }
    }

    public void fillRect(int x, int y, int width, int height, int color) {
        for (int n = y; n < y + height; ++n) {
            for (int o = x; o < x + width; ++o) {
                this.setColor(o, n, color);
            }
        }
    }

    public void copyRect(int x, int y, int translateX, int translateY, int width, int height, boolean flipX, boolean flipY) {
        this.copyRect(this, x, y, x + translateX, y + translateY, width, height, flipX, flipY);
    }

    public void copyRect(NativeImage image, int x, int y, int destX, int destY, int width, int height, boolean flipX, boolean flipY) {
        for (int o = 0; o < height; ++o) {
            for (int p = 0; p < width; ++p) {
                int q = flipX ? width - 1 - p : p;
                int r = flipY ? height - 1 - o : o;
                int s = this.getColor(x + p, y + o);
                image.setColor(destX + q, destY + r, s);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void mirrorVertically() {
        this.checkAllocated();
        int i = this.format.getChannelCount();
        int j = this.getWidth() * i;
        long l = MemoryUtil.nmemAlloc(j);
        try {
            for (int k = 0; k < this.getHeight() / 2; ++k) {
                int m = k * this.getWidth() * i;
                int n = (this.getHeight() - 1 - k) * this.getWidth() * i;
                MemoryUtil.memCopy(this.pointer + (long)m, l, j);
                MemoryUtil.memCopy(this.pointer + (long)n, this.pointer + (long)m, j);
                MemoryUtil.memCopy(l, this.pointer + (long)n, j);
            }
        } finally {
            MemoryUtil.nmemFree(l);
        }
    }

    public void resizeSubRectTo(int x, int y, int width, int height, NativeImage targetImage) {
        this.checkAllocated();
        if (targetImage.getFormat() != this.format) {
            throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
        }
        int m = this.format.getChannelCount();
        STBImageResize.nstbir_resize_uint8(this.pointer + (long)((x + y * this.getWidth()) * m), width, height, this.getWidth() * m, targetImage.pointer, targetImage.getWidth(), targetImage.getHeight(), 0, m);
    }

    public void untrack() {
        Untracker.untrack(this.pointer);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Format {
        RGBA(4, GlConst.GL_RGBA, true, true, true, false, true, 0, 8, 16, 255, 24, true),
        RGB(3, GlConst.GL_RGB, true, true, true, false, false, 0, 8, 16, 255, 255, true),
        LUMINANCE_ALPHA(2, GlConst.GL_RG, false, false, false, true, true, 255, 255, 255, 0, 8, true),
        LUMINANCE(1, GlConst.GL_RED, false, false, false, true, false, 0, 0, 0, 0, 255, true);

        final int channelCount;
        private final int glFormat;
        private final boolean hasRed;
        private final boolean hasGreen;
        private final boolean hasBlue;
        private final boolean hasLuminance;
        private final boolean hasAlpha;
        private final int redOffset;
        private final int greenOffset;
        private final int blueOffset;
        private final int luminanceOffset;
        private final int alphaOffset;
        private final boolean writeable;

        private Format(int channelCount, int glFormat, boolean hasRed, boolean hasGreen, boolean hasBlue, boolean hasLuminance, boolean hasAlpha, int redOffset, int greenOffset, int blueOffset, int luminanceOffset, int alphaOffset, boolean writeable) {
            this.channelCount = channelCount;
            this.glFormat = glFormat;
            this.hasRed = hasRed;
            this.hasGreen = hasGreen;
            this.hasBlue = hasBlue;
            this.hasLuminance = hasLuminance;
            this.hasAlpha = hasAlpha;
            this.redOffset = redOffset;
            this.greenOffset = greenOffset;
            this.blueOffset = blueOffset;
            this.luminanceOffset = luminanceOffset;
            this.alphaOffset = alphaOffset;
            this.writeable = writeable;
        }

        public int getChannelCount() {
            return this.channelCount;
        }

        public void setPackAlignment() {
            RenderSystem.assertOnRenderThread();
            GlStateManager._pixelStore(GlConst.GL_PACK_ALIGNMENT, this.getChannelCount());
        }

        public void setUnpackAlignment() {
            RenderSystem.assertOnRenderThreadOrInit();
            GlStateManager._pixelStore(GlConst.GL_UNPACK_ALIGNMENT, this.getChannelCount());
        }

        public int toGl() {
            return this.glFormat;
        }

        public boolean hasRed() {
            return this.hasRed;
        }

        public boolean hasGreen() {
            return this.hasGreen;
        }

        public boolean hasBlue() {
            return this.hasBlue;
        }

        public boolean hasLuminance() {
            return this.hasLuminance;
        }

        public boolean hasAlpha() {
            return this.hasAlpha;
        }

        public int getRedOffset() {
            return this.redOffset;
        }

        public int getGreenOffset() {
            return this.greenOffset;
        }

        public int getBlueOffset() {
            return this.blueOffset;
        }

        public int getLuminanceOffset() {
            return this.luminanceOffset;
        }

        public int getAlphaOffset() {
            return this.alphaOffset;
        }

        public boolean hasRedChannel() {
            return this.hasLuminance || this.hasRed;
        }

        public boolean hasGreenChannel() {
            return this.hasLuminance || this.hasGreen;
        }

        public boolean hasBlueChannel() {
            return this.hasLuminance || this.hasBlue;
        }

        public boolean hasOpacityChannel() {
            return this.hasLuminance || this.hasAlpha;
        }

        public int getRedChannelOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.redOffset;
        }

        public int getGreenChannelOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.greenOffset;
        }

        public int getBlueChannelOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.blueOffset;
        }

        public int getOpacityChannelOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
        }

        public boolean isWriteable() {
            return this.writeable;
        }

        static Format fromChannelCount(int glFormat) {
            switch (glFormat) {
                case 1: {
                    return LUMINANCE;
                }
                case 2: {
                    return LUMINANCE_ALPHA;
                }
                case 3: {
                    return RGB;
                }
            }
            return RGBA;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class WriteCallback
    extends STBIWriteCallback {
        private final WritableByteChannel channel;
        @Nullable
        private IOException exception;

        WriteCallback(WritableByteChannel channel) {
            this.channel = channel;
        }

        @Override
        public void invoke(long context, long data, int size) {
            ByteBuffer byteBuffer = WriteCallback.getData(data, size);
            try {
                this.channel.write(byteBuffer);
            } catch (IOException iOException) {
                this.exception = iOException;
            }
        }

        public void throwStoredException() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum InternalFormat {
        RGBA(6408),
        RGB(6407),
        RG(33319),
        RED(6403);

        private final int value;

        private InternalFormat(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}

