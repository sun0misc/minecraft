package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BufferBuilder extends FixedColorVertexConsumer implements BufferVertexConsumer {
   private static final int ROUND_SIZE_CHANGE = 2097152;
   private static final Logger LOGGER = LogUtils.getLogger();
   private ByteBuffer buffer;
   private int builtBufferCount;
   private int batchOffset;
   private int elementOffset;
   private int vertexCount;
   @Nullable
   private VertexFormatElement currentElement;
   private int currentElementId;
   private VertexFormat format;
   private VertexFormat.DrawMode drawMode;
   private boolean canSkipElementChecks;
   private boolean hasOverlay;
   private boolean building;
   @Nullable
   private Vector3f[] sortingPrimitiveCenters;
   @Nullable
   private VertexSorter sorter;
   private boolean hasNoVertexBuffer;

   public BufferBuilder(int initialCapacity) {
      this.buffer = GlAllocationUtils.allocateByteBuffer(initialCapacity * 6);
   }

   private void grow() {
      this.grow(this.format.getVertexSizeByte());
   }

   private void grow(int size) {
      if (this.elementOffset + size > this.buffer.capacity()) {
         int j = this.buffer.capacity();
         int k = j + roundBufferSize(size);
         LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", j, k);
         ByteBuffer byteBuffer = GlAllocationUtils.resizeByteBuffer(this.buffer, k);
         byteBuffer.rewind();
         this.buffer = byteBuffer;
      }
   }

   private static int roundBufferSize(int amount) {
      int j = 2097152;
      if (amount == 0) {
         return j;
      } else {
         if (amount < 0) {
            j *= -1;
         }

         int k = amount % j;
         return k == 0 ? amount : amount + j - k;
      }
   }

   public void setSorter(VertexSorter sorter) {
      if (this.drawMode == VertexFormat.DrawMode.QUADS) {
         this.sorter = sorter;
         if (this.sortingPrimitiveCenters == null) {
            this.sortingPrimitiveCenters = this.buildPrimitiveCenters();
         }

      }
   }

   public TransparentSortingData getSortingData() {
      return new TransparentSortingData(this.drawMode, this.vertexCount, this.sortingPrimitiveCenters, this.sorter);
   }

   public void beginSortedIndexBuffer(TransparentSortingData state) {
      this.buffer.rewind();
      this.drawMode = state.drawMode;
      this.vertexCount = state.vertexCount;
      this.elementOffset = this.batchOffset;
      this.sortingPrimitiveCenters = state.primitiveCenters;
      this.sorter = state.sorter;
      this.hasNoVertexBuffer = true;
   }

   public void begin(VertexFormat.DrawMode drawMode, VertexFormat format) {
      if (this.building) {
         throw new IllegalStateException("Already building!");
      } else {
         this.building = true;
         this.drawMode = drawMode;
         this.setFormat(format);
         this.currentElement = (VertexFormatElement)format.getElements().get(0);
         this.currentElementId = 0;
         this.buffer.rewind();
      }
   }

   private void setFormat(VertexFormat format) {
      if (this.format != format) {
         this.format = format;
         boolean bl = format == VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
         boolean bl2 = format == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
         this.canSkipElementChecks = bl || bl2;
         this.hasOverlay = bl;
      }
   }

   private IntConsumer getIndexConsumer(int offset, VertexFormat.IndexType indexType) {
      MutableInt mutableInt = new MutableInt(offset);
      IntConsumer var10000;
      switch (indexType) {
         case SHORT:
            var10000 = (index) -> {
               this.buffer.putShort(mutableInt.getAndAdd(2), (short)index);
            };
            break;
         case INT:
            var10000 = (index) -> {
               this.buffer.putInt(mutableInt.getAndAdd(4), index);
            };
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   private Vector3f[] buildPrimitiveCenters() {
      FloatBuffer floatBuffer = this.buffer.asFloatBuffer();
      int i = this.batchOffset / 4;
      int j = this.format.getVertexSizeInteger();
      int k = j * this.drawMode.additionalVertexCount;
      int l = this.vertexCount / this.drawMode.additionalVertexCount;
      Vector3f[] vector3fs = new Vector3f[l];

      for(int m = 0; m < l; ++m) {
         float f = floatBuffer.get(i + m * k + 0);
         float g = floatBuffer.get(i + m * k + 1);
         float h = floatBuffer.get(i + m * k + 2);
         float n = floatBuffer.get(i + m * k + j * 2 + 0);
         float o = floatBuffer.get(i + m * k + j * 2 + 1);
         float p = floatBuffer.get(i + m * k + j * 2 + 2);
         float q = (f + n) / 2.0F;
         float r = (g + o) / 2.0F;
         float s = (h + p) / 2.0F;
         vector3fs[m] = new Vector3f(q, r, s);
      }

      return vector3fs;
   }

   private void writeSortedIndices(VertexFormat.IndexType indexType) {
      if (this.sortingPrimitiveCenters != null && this.sorter != null) {
         int[] is = this.sorter.sort(this.sortingPrimitiveCenters);
         IntConsumer intConsumer = this.getIndexConsumer(this.elementOffset, indexType);
         int[] var4 = is;
         int var5 = is.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int i = var4[var6];
            intConsumer.accept(i * this.drawMode.additionalVertexCount + 0);
            intConsumer.accept(i * this.drawMode.additionalVertexCount + 1);
            intConsumer.accept(i * this.drawMode.additionalVertexCount + 2);
            intConsumer.accept(i * this.drawMode.additionalVertexCount + 2);
            intConsumer.accept(i * this.drawMode.additionalVertexCount + 3);
            intConsumer.accept(i * this.drawMode.additionalVertexCount + 0);
         }

      } else {
         throw new IllegalStateException("Sorting state uninitialized");
      }
   }

   public boolean isBatchEmpty() {
      return this.vertexCount == 0;
   }

   @Nullable
   public BuiltBuffer endNullable() {
      this.ensureBuilding();
      if (this.isBatchEmpty()) {
         this.resetBuilding();
         return null;
      } else {
         BuiltBuffer lv = this.build();
         this.resetBuilding();
         return lv;
      }
   }

   public BuiltBuffer end() {
      this.ensureBuilding();
      BuiltBuffer lv = this.build();
      this.resetBuilding();
      return lv;
   }

   private void ensureBuilding() {
      if (!this.building) {
         throw new IllegalStateException("Not building!");
      }
   }

   private BuiltBuffer build() {
      int i = this.drawMode.getIndexCount(this.vertexCount);
      int j = !this.hasNoVertexBuffer ? this.vertexCount * this.format.getVertexSizeByte() : 0;
      VertexFormat.IndexType lv = VertexFormat.IndexType.smallestFor(i);
      boolean bl;
      int l;
      int k;
      if (this.sortingPrimitiveCenters != null) {
         k = MathHelper.roundUpToMultiple(i * lv.size, 4);
         this.grow(k);
         this.writeSortedIndices(lv);
         bl = false;
         this.elementOffset += k;
         l = j + k;
      } else {
         bl = true;
         l = j;
      }

      k = this.batchOffset;
      this.batchOffset += l;
      ++this.builtBufferCount;
      DrawParameters lv2 = new DrawParameters(this.format, this.vertexCount, i, this.drawMode, lv, this.hasNoVertexBuffer, bl);
      return new BuiltBuffer(k, lv2);
   }

   private void resetBuilding() {
      this.building = false;
      this.vertexCount = 0;
      this.currentElement = null;
      this.currentElementId = 0;
      this.sortingPrimitiveCenters = null;
      this.sorter = null;
      this.hasNoVertexBuffer = false;
   }

   public void putByte(int index, byte value) {
      this.buffer.put(this.elementOffset + index, value);
   }

   public void putShort(int index, short value) {
      this.buffer.putShort(this.elementOffset + index, value);
   }

   public void putFloat(int index, float value) {
      this.buffer.putFloat(this.elementOffset + index, value);
   }

   public void next() {
      if (this.currentElementId != 0) {
         throw new IllegalStateException("Not filled all elements of the vertex");
      } else {
         ++this.vertexCount;
         this.grow();
         if (this.drawMode == VertexFormat.DrawMode.LINES || this.drawMode == VertexFormat.DrawMode.LINE_STRIP) {
            int i = this.format.getVertexSizeByte();
            this.buffer.put(this.elementOffset, this.buffer, this.elementOffset - i, i);
            this.elementOffset += i;
            ++this.vertexCount;
            this.grow();
         }

      }
   }

   public void nextElement() {
      ImmutableList immutableList = this.format.getElements();
      this.currentElementId = (this.currentElementId + 1) % immutableList.size();
      this.elementOffset += this.currentElement.getByteLength();
      VertexFormatElement lv = (VertexFormatElement)immutableList.get(this.currentElementId);
      this.currentElement = lv;
      if (lv.getType() == VertexFormatElement.Type.PADDING) {
         this.nextElement();
      }

      if (this.colorFixed && this.currentElement.getType() == VertexFormatElement.Type.COLOR) {
         BufferVertexConsumer.super.color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha);
      }

   }

   public VertexConsumer color(int red, int green, int blue, int alpha) {
      if (this.colorFixed) {
         throw new IllegalStateException();
      } else {
         return BufferVertexConsumer.super.color(red, green, blue, alpha);
      }
   }

   public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      if (this.colorFixed) {
         throw new IllegalStateException();
      } else if (this.canSkipElementChecks) {
         this.putFloat(0, x);
         this.putFloat(4, y);
         this.putFloat(8, z);
         this.putByte(12, (byte)((int)(red * 255.0F)));
         this.putByte(13, (byte)((int)(green * 255.0F)));
         this.putByte(14, (byte)((int)(blue * 255.0F)));
         this.putByte(15, (byte)((int)(alpha * 255.0F)));
         this.putFloat(16, u);
         this.putFloat(20, v);
         byte t;
         if (this.hasOverlay) {
            this.putShort(24, (short)(overlay & '\uffff'));
            this.putShort(26, (short)(overlay >> 16 & '\uffff'));
            t = 28;
         } else {
            t = 24;
         }

         this.putShort(t + 0, (short)(light & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | '／')));
         this.putShort(t + 2, (short)(light >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | '／')));
         this.putByte(t + 4, BufferVertexConsumer.packByte(normalX));
         this.putByte(t + 5, BufferVertexConsumer.packByte(normalY));
         this.putByte(t + 6, BufferVertexConsumer.packByte(normalZ));
         this.elementOffset += t + 8;
         this.next();
      } else {
         super.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
      }
   }

   void releaseBuiltBuffer() {
      if (this.builtBufferCount > 0 && --this.builtBufferCount == 0) {
         this.clear();
      }

   }

   public void clear() {
      if (this.builtBufferCount > 0) {
         LOGGER.warn("Clearing BufferBuilder with unused batches");
      }

      this.reset();
   }

   public void reset() {
      this.builtBufferCount = 0;
      this.batchOffset = 0;
      this.elementOffset = 0;
   }

   public VertexFormatElement getCurrentElement() {
      if (this.currentElement == null) {
         throw new IllegalStateException("BufferBuilder not started");
      } else {
         return this.currentElement;
      }
   }

   public boolean isBuilding() {
      return this.building;
   }

   ByteBuffer slice(int start, int end) {
      return MemoryUtil.memSlice(this.buffer, start, end - start);
   }

   @Environment(EnvType.CLIENT)
   public static class TransparentSortingData {
      final VertexFormat.DrawMode drawMode;
      final int vertexCount;
      @Nullable
      final Vector3f[] primitiveCenters;
      @Nullable
      final VertexSorter sorter;

      TransparentSortingData(VertexFormat.DrawMode drawMode, int vertexCount, @Nullable Vector3f[] primitiveCenters, @Nullable VertexSorter sorter) {
         this.drawMode = drawMode;
         this.vertexCount = vertexCount;
         this.primitiveCenters = primitiveCenters;
         this.sorter = sorter;
      }
   }

   @Environment(EnvType.CLIENT)
   public class BuiltBuffer {
      private final int batchOffset;
      private final DrawParameters parameters;
      private boolean released;

      BuiltBuffer(int batchOffset, DrawParameters parameters) {
         this.batchOffset = batchOffset;
         this.parameters = parameters;
      }

      public ByteBuffer getVertexBuffer() {
         int i = this.batchOffset + this.parameters.getVertexBufferStart();
         int j = this.batchOffset + this.parameters.getVertexBufferEnd();
         return BufferBuilder.this.slice(i, j);
      }

      public ByteBuffer getIndexBuffer() {
         int i = this.batchOffset + this.parameters.getIndexBufferStart();
         int j = this.batchOffset + this.parameters.getIndexBufferEnd();
         return BufferBuilder.this.slice(i, j);
      }

      public DrawParameters getParameters() {
         return this.parameters;
      }

      public boolean isEmpty() {
         return this.parameters.vertexCount == 0;
      }

      public void release() {
         if (this.released) {
            throw new IllegalStateException("Buffer has already been released!");
         } else {
            BufferBuilder.this.releaseBuiltBuffer();
            this.released = true;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static record DrawParameters(VertexFormat format, int vertexCount, int indexCount, VertexFormat.DrawMode mode, VertexFormat.IndexType indexType, boolean indexOnly, boolean sequentialIndex) {
      final int vertexCount;

      public DrawParameters(VertexFormat arg, int i, int j, VertexFormat.DrawMode arg2, VertexFormat.IndexType arg3, boolean bl, boolean bl2) {
         this.format = arg;
         this.vertexCount = i;
         this.indexCount = j;
         this.mode = arg2;
         this.indexType = arg3;
         this.indexOnly = bl;
         this.sequentialIndex = bl2;
      }

      public int getVertexBufferSize() {
         return this.vertexCount * this.format.getVertexSizeByte();
      }

      public int getVertexBufferStart() {
         return 0;
      }

      public int getVertexBufferEnd() {
         return this.getVertexBufferSize();
      }

      public int getIndexBufferStart() {
         return this.indexOnly ? 0 : this.getVertexBufferEnd();
      }

      public int getIndexBufferEnd() {
         return this.getIndexBufferStart() + this.getIndexBufferSize();
      }

      private int getIndexBufferSize() {
         return this.sequentialIndex ? 0 : this.indexCount * this.indexType.size;
      }

      public int getBufferSize() {
         return this.getIndexBufferEnd();
      }

      public VertexFormat format() {
         return this.format;
      }

      public int vertexCount() {
         return this.vertexCount;
      }

      public int indexCount() {
         return this.indexCount;
      }

      public VertexFormat.DrawMode mode() {
         return this.mode;
      }

      public VertexFormat.IndexType indexType() {
         return this.indexType;
      }

      public boolean indexOnly() {
         return this.indexOnly;
      }

      public boolean sequentialIndex() {
         return this.sequentialIndex;
      }
   }
}
