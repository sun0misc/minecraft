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
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Untracker;
import net.minecraft.util.math.ColorHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public final class NativeImage implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Set WRITE_TO_FILE_OPEN_OPTIONS;
   private final Format format;
   private final int width;
   private final int height;
   private final boolean isStbImage;
   private long pointer;
   private final long sizeBytes;

   public NativeImage(int width, int height, boolean useStb) {
      this(NativeImage.Format.RGBA, width, height, useStb);
   }

   public NativeImage(Format format, int width, int height, boolean useStb) {
      if (width > 0 && height > 0) {
         this.format = format;
         this.width = width;
         this.height = height;
         this.sizeBytes = (long)width * (long)height * (long)format.getChannelCount();
         this.isStbImage = false;
         if (useStb) {
            this.pointer = MemoryUtil.nmemCalloc(1L, this.sizeBytes);
         } else {
            this.pointer = MemoryUtil.nmemAlloc(this.sizeBytes);
         }

      } else {
         throw new IllegalArgumentException("Invalid texture size: " + width + "x" + height);
      }
   }

   private NativeImage(Format format, int width, int height, boolean useStb, long pointer) {
      if (width > 0 && height > 0) {
         this.format = format;
         this.width = width;
         this.height = height;
         this.isStbImage = useStb;
         this.pointer = pointer;
         this.sizeBytes = (long)width * (long)height * (long)format.getChannelCount();
      } else {
         throw new IllegalArgumentException("Invalid texture size: " + width + "x" + height);
      }
   }

   public String toString() {
      return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pointer + (this.isStbImage ? "S" : "N") + "]";
   }

   private boolean isOutOfBounds(int x, int y) {
      return x < 0 || x >= this.width || y < 0 || y >= this.height;
   }

   public static NativeImage read(InputStream stream) throws IOException {
      return read(NativeImage.Format.RGBA, stream);
   }

   public static NativeImage read(@Nullable Format format, InputStream stream) throws IOException {
      ByteBuffer byteBuffer = null;

      NativeImage var3;
      try {
         byteBuffer = TextureUtil.readResource(stream);
         byteBuffer.rewind();
         var3 = read(format, byteBuffer);
      } finally {
         MemoryUtil.memFree(byteBuffer);
         IOUtils.closeQuietly(stream);
      }

      return var3;
   }

   public static NativeImage read(ByteBuffer buffer) throws IOException {
      return read(NativeImage.Format.RGBA, buffer);
   }

   public static NativeImage read(byte[] bytes) throws IOException {
      MemoryStack memoryStack = MemoryStack.stackPush();

      NativeImage var3;
      try {
         ByteBuffer byteBuffer = memoryStack.malloc(bytes.length);
         byteBuffer.put(bytes);
         byteBuffer.rewind();
         var3 = read(byteBuffer);
      } catch (Throwable var5) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }

      return var3;
   }

   public static NativeImage read(@Nullable Format format, ByteBuffer buffer) throws IOException {
      if (format != null && !format.isWriteable()) {
         throw new UnsupportedOperationException("Don't know how to read format " + format);
      } else if (MemoryUtil.memAddress(buffer) == 0L) {
         throw new IllegalArgumentException("Invalid buffer");
      } else {
         MemoryStack memoryStack = MemoryStack.stackPush();

         NativeImage var7;
         try {
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            ByteBuffer byteBuffer2 = STBImage.stbi_load_from_memory(buffer, intBuffer, intBuffer2, intBuffer3, format == null ? 0 : format.channelCount);
            if (byteBuffer2 == null) {
               throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }

            var7 = new NativeImage(format == null ? NativeImage.Format.fromGl(intBuffer3.get(0)) : format, intBuffer.get(0), intBuffer2.get(0), true, MemoryUtil.memAddress(byteBuffer2));
         } catch (Throwable var9) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }

         return var7;
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
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelRGBA only works on RGBA images; have %s", this.format));
      } else if (this.isOutOfBounds(x, y)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
      } else {
         this.checkAllocated();
         long l = ((long)x + (long)y * (long)this.width) * 4L;
         return MemoryUtil.memGetInt(this.pointer + l);
      }
   }

   public void setColor(int x, int y, int color) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelRGBA only works on RGBA images; have %s", this.format));
      } else if (this.isOutOfBounds(x, y)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
      } else {
         this.checkAllocated();
         long l = ((long)x + (long)y * (long)this.width) * 4L;
         MemoryUtil.memPutInt(this.pointer + l, color);
      }
   }

   public NativeImage apply(IntUnaryOperator operator) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", this.format));
      } else {
         this.checkAllocated();
         NativeImage lv = new NativeImage(this.width, this.height, false);
         int i = this.width * this.height;
         IntBuffer intBuffer = MemoryUtil.memIntBuffer(this.pointer, i);
         IntBuffer intBuffer2 = MemoryUtil.memIntBuffer(lv.pointer, i);

         for(int j = 0; j < i; ++j) {
            intBuffer2.put(j, operator.applyAsInt(intBuffer.get(j)));
         }

         return lv;
      }
   }

   public int[] copyPixelsRgba() {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelsRGBA only works on RGBA images; have %s", this.format));
      } else {
         this.checkAllocated();
         int[] is = new int[this.width * this.height];
         MemoryUtil.memIntBuffer(this.pointer, this.width * this.height).get(is);
         return is;
      }
   }

   public void setLuminance(int x, int y, byte luminance) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasLuminance()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelLuminance only works on image with luminance; have %s", this.format));
      } else if (this.isOutOfBounds(x, y)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
      } else {
         this.checkAllocated();
         long l = ((long)x + (long)y * (long)this.width) * (long)this.format.getChannelCount() + (long)(this.format.getLuminanceOffset() / 8);
         MemoryUtil.memPutByte(this.pointer + l, luminance);
      }
   }

   public byte getRed(int x, int y) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasRedChannel()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no red or luminance in %s", this.format));
      } else if (this.isOutOfBounds(x, y)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
      } else {
         int k = (x + y * this.width) * this.format.getChannelCount() + this.format.getRedChannelOffset() / 8;
         return MemoryUtil.memGetByte(this.pointer + (long)k);
      }
   }

   public byte getGreen(int x, int y) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasGreenChannel()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no green or luminance in %s", this.format));
      } else if (this.isOutOfBounds(x, y)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
      } else {
         int k = (x + y * this.width) * this.format.getChannelCount() + this.format.getGreenChannelOffset() / 8;
         return MemoryUtil.memGetByte(this.pointer + (long)k);
      }
   }

   public byte getBlue(int x, int y) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasBlueChannel()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no blue or luminance in %s", this.format));
      } else if (this.isOutOfBounds(x, y)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
      } else {
         int k = (x + y * this.width) * this.format.getChannelCount() + this.format.getBlueChannelOffset() / 8;
         return MemoryUtil.memGetByte(this.pointer + (long)k);
      }
   }

   public byte getOpacity(int x, int y) {
      if (!this.format.hasOpacityChannel()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no luminance or alpha in %s", this.format));
      } else if (this.isOutOfBounds(x, y)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", x, y, this.width, this.height));
      } else {
         int k = (x + y * this.width) * this.format.getChannelCount() + this.format.getOpacityChannelOffset() / 8;
         return MemoryUtil.memGetByte(this.pointer + (long)k);
      }
   }

   public void blend(int x, int y, int color) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
      } else {
         int l = this.getColor(x, y);
         float f = (float)ColorHelper.Abgr.getAlpha(color) / 255.0F;
         float g = (float)ColorHelper.Abgr.getBlue(color) / 255.0F;
         float h = (float)ColorHelper.Abgr.getGreen(color) / 255.0F;
         float m = (float)ColorHelper.Abgr.getRed(color) / 255.0F;
         float n = (float)ColorHelper.Abgr.getAlpha(l) / 255.0F;
         float o = (float)ColorHelper.Abgr.getBlue(l) / 255.0F;
         float p = (float)ColorHelper.Abgr.getGreen(l) / 255.0F;
         float q = (float)ColorHelper.Abgr.getRed(l) / 255.0F;
         float s = 1.0F - f;
         float t = f * f + n * s;
         float u = g * f + o * s;
         float v = h * f + p * s;
         float w = m * f + q * s;
         if (t > 1.0F) {
            t = 1.0F;
         }

         if (u > 1.0F) {
            u = 1.0F;
         }

         if (v > 1.0F) {
            v = 1.0F;
         }

         if (w > 1.0F) {
            w = 1.0F;
         }

         int x = (int)(t * 255.0F);
         int y = (int)(u * 255.0F);
         int z = (int)(v * 255.0F);
         int aa = (int)(w * 255.0F);
         this.setColor(x, y, ColorHelper.Abgr.getAbgr(x, y, z, aa));
      }
   }

   /** @deprecated */
   @Deprecated
   public int[] makePixelArray() {
      if (this.format != NativeImage.Format.RGBA) {
         throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
      } else {
         this.checkAllocated();
         int[] is = new int[this.getWidth() * this.getHeight()];

         for(int i = 0; i < this.getHeight(); ++i) {
            for(int j = 0; j < this.getWidth(); ++j) {
               int k = this.getColor(j, i);
               is[j + i * this.getWidth()] = ColorHelper.Argb.getArgb(ColorHelper.Abgr.getAlpha(k), ColorHelper.Abgr.getRed(k), ColorHelper.Abgr.getGreen(k), ColorHelper.Abgr.getBlue(k));
            }
         }

         return is;
      }
   }

   public void upload(int level, int offsetX, int offsetY, boolean close) {
      this.upload(level, offsetX, offsetY, 0, 0, this.width, this.height, false, close);
   }

   public void upload(int level, int offsetX, int offsetY, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean mipmap, boolean close) {
      this.upload(level, offsetX, offsetY, unpackSkipPixels, unpackSkipRows, width, height, false, false, mipmap, close);
   }

   public void upload(int level, int offsetX, int offsetY, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean blur, boolean clamp, boolean mipmap, boolean close) {
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> {
            this.uploadInternal(level, offsetX, offsetY, unpackSkipPixels, unpackSkipRows, width, height, blur, clamp, mipmap, close);
         });
      } else {
         this.uploadInternal(level, offsetX, offsetY, unpackSkipPixels, unpackSkipRows, width, height, blur, clamp, mipmap, close);
      }

   }

   private void uploadInternal(int level, int offsetX, int offsetY, int unpackSkipPixels, int unpackSkipRows, int width, int height, boolean blur, boolean clamp, boolean mipmap, boolean close) {
      RenderSystem.assertOnRenderThreadOrInit();
      this.checkAllocated();
      setTextureFilter(blur, mipmap);
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

      if (close) {
         this.close();
      }

   }

   public void loadFromTextureImage(int level, boolean removeAlpha) {
      RenderSystem.assertOnRenderThread();
      this.checkAllocated();
      this.format.setPackAlignment();
      GlStateManager._getTexImage(GlConst.GL_TEXTURE_2D, level, this.format.toGl(), 5121, this.pointer);
      if (removeAlpha && this.format.hasAlpha()) {
         for(int j = 0; j < this.getHeight(); ++j) {
            for(int k = 0; k < this.getWidth(); ++k) {
               this.setColor(k, j, this.getColor(k, j) | 255 << this.format.getAlphaOffset());
            }
         }
      }

   }

   public void readDepthComponent(float unused) {
      RenderSystem.assertOnRenderThread();
      if (this.format.getChannelCount() != 1) {
         throw new IllegalStateException("Depth buffer must be stored in NativeImage with 1 component.");
      } else {
         this.checkAllocated();
         this.format.setPackAlignment();
         GlStateManager._readPixels(0, 0, this.width, this.height, GlConst.GL_DEPTH_COMPONENT, GlConst.GL_UNSIGNED_BYTE, this.pointer);
      }
   }

   public void drawPixels() {
      RenderSystem.assertOnRenderThread();
      this.format.setUnpackAlignment();
      GlStateManager._glDrawPixels(this.width, this.height, this.format.toGl(), GlConst.GL_UNSIGNED_BYTE, this.pointer);
   }

   public void writeTo(File path) throws IOException {
      this.writeTo(path.toPath());
   }

   public void makeGlyphBitmapSubpixel(STBTTFontinfo fontInfo, int glyphIndex, int width, int height, float scaleX, float scaleY, float shiftX, float shiftY, int startX, int startY) {
      if (startX >= 0 && startX + width <= this.getWidth() && startY >= 0 && startY + height <= this.getHeight()) {
         if (this.format.getChannelCount() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
         } else {
            STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(fontInfo.address(), this.pointer + (long)startX + (long)(startY * this.getWidth()), width, height, this.getWidth(), scaleX, scaleY, shiftX, shiftY, glyphIndex);
         }
      } else {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", startX, startY, width, height, this.getWidth(), this.getHeight()));
      }
   }

   public void writeTo(Path path) throws IOException {
      if (!this.format.isWriteable()) {
         throw new UnsupportedOperationException("Don't know how to write format " + this.format);
      } else {
         this.checkAllocated();
         WritableByteChannel writableByteChannel = Files.newByteChannel(path, WRITE_TO_FILE_OPEN_OPTIONS);

         try {
            if (!this.write(writableByteChannel)) {
               Path var10002 = path.toAbsolutePath();
               throw new IOException("Could not write image to the PNG file \"" + var10002 + "\": " + STBImage.stbi_failure_reason());
            }
         } catch (Throwable var6) {
            if (writableByteChannel != null) {
               try {
                  writableByteChannel.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (writableByteChannel != null) {
            writableByteChannel.close();
         }

      }
   }

   public byte[] getBytes() throws IOException {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      byte[] var3;
      try {
         WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);

         try {
            if (!this.write(writableByteChannel)) {
               throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
            }

            var3 = byteArrayOutputStream.toByteArray();
         } catch (Throwable var7) {
            if (writableByteChannel != null) {
               try {
                  writableByteChannel.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (writableByteChannel != null) {
            writableByteChannel.close();
         }
      } catch (Throwable var8) {
         try {
            byteArrayOutputStream.close();
         } catch (Throwable var5) {
            var8.addSuppressed(var5);
         }

         throw var8;
      }

      byteArrayOutputStream.close();
      return var3;
   }

   private boolean write(WritableByteChannel channel) throws IOException {
      WriteCallback lv = new WriteCallback(channel);

      boolean var4;
      try {
         int i = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.getChannelCount());
         if (i < this.getHeight()) {
            LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", this.getHeight(), i);
         }

         if (STBImageWrite.nstbi_write_png_to_func(lv.address(), 0L, this.getWidth(), i, this.format.getChannelCount(), this.pointer, 0) == 0) {
            var4 = false;
            return var4;
         }

         lv.throwStoredException();
         var4 = true;
      } finally {
         lv.free();
      }

      return var4;
   }

   public void copyFrom(NativeImage image) {
      if (image.getFormat() != this.format) {
         throw new UnsupportedOperationException("Image formats don't match.");
      } else {
         int i = this.format.getChannelCount();
         this.checkAllocated();
         image.checkAllocated();
         if (this.width == image.width) {
            MemoryUtil.memCopy(image.pointer, this.pointer, Math.min(this.sizeBytes, image.sizeBytes));
         } else {
            int j = Math.min(this.getWidth(), image.getWidth());
            int k = Math.min(this.getHeight(), image.getHeight());

            for(int l = 0; l < k; ++l) {
               int m = l * image.getWidth() * i;
               int n = l * this.getWidth() * i;
               MemoryUtil.memCopy(image.pointer + (long)m, this.pointer + (long)n, (long)j);
            }
         }

      }
   }

   public void fillRect(int x, int y, int width, int height, int color) {
      for(int n = y; n < y + height; ++n) {
         for(int o = x; o < x + width; ++o) {
            this.setColor(o, n, color);
         }
      }

   }

   public void copyRect(int x, int y, int translateX, int translateY, int width, int height, boolean flipX, boolean flipY) {
      this.copyRect(this, x, y, x + translateX, y + translateY, width, height, flipX, flipY);
   }

   public void copyRect(NativeImage image, int x, int y, int destX, int destY, int width, int height, boolean flipX, boolean flipY) {
      for(int o = 0; o < height; ++o) {
         for(int p = 0; p < width; ++p) {
            int q = flipX ? width - 1 - p : p;
            int r = flipY ? height - 1 - o : o;
            int s = this.getColor(x + p, y + o);
            image.setColor(destX + q, destY + r, s);
         }
      }

   }

   public void mirrorVertically() {
      this.checkAllocated();
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         int i = this.format.getChannelCount();
         int j = this.getWidth() * i;
         long l = memoryStack.nmalloc(j);

         for(int k = 0; k < this.getHeight() / 2; ++k) {
            int m = k * this.getWidth() * i;
            int n = (this.getHeight() - 1 - k) * this.getWidth() * i;
            MemoryUtil.memCopy(this.pointer + (long)m, l, (long)j);
            MemoryUtil.memCopy(this.pointer + (long)n, this.pointer + (long)m, (long)j);
            MemoryUtil.memCopy(l, this.pointer + (long)n, (long)j);
         }
      } catch (Throwable var10) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }

   }

   public void resizeSubRectTo(int x, int y, int width, int height, NativeImage targetImage) {
      this.checkAllocated();
      if (targetImage.getFormat() != this.format) {
         throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
      } else {
         int m = this.format.getChannelCount();
         STBImageResize.nstbir_resize_uint8(this.pointer + (long)((x + y * this.getWidth()) * m), width, height, this.getWidth() * m, targetImage.pointer, targetImage.getWidth(), targetImage.getHeight(), 0, m);
      }
   }

   public void untrack() {
      Untracker.untrack(this.pointer);
   }

   static {
      WRITE_TO_FILE_OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
   }

   @Environment(EnvType.CLIENT)
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

      static Format fromGl(int glFormat) {
         switch (glFormat) {
            case 1:
               return LUMINANCE;
            case 2:
               return LUMINANCE_ALPHA;
            case 3:
               return RGB;
            case 4:
            default:
               return RGBA;
         }
      }

      // $FF: synthetic method
      private static Format[] method_36811() {
         return new Format[]{RGBA, RGB, LUMINANCE_ALPHA, LUMINANCE};
      }
   }

   @Environment(EnvType.CLIENT)
   private static class WriteCallback extends STBIWriteCallback {
      private final WritableByteChannel channel;
      @Nullable
      private IOException exception;

      WriteCallback(WritableByteChannel channel) {
         this.channel = channel;
      }

      public void invoke(long context, long data, int size) {
         ByteBuffer byteBuffer = getData(data, size);

         try {
            this.channel.write(byteBuffer);
         } catch (IOException var8) {
            this.exception = var8;
         }

      }

      public void throwStoredException() throws IOException {
         if (this.exception != null) {
            throw this.exception;
         }
      }
   }

   @Environment(EnvType.CLIENT)
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

      // $FF: synthetic method
      private static InternalFormat[] method_36812() {
         return new InternalFormat[]{RGBA, RGB, RG, RED};
      }
   }
}
