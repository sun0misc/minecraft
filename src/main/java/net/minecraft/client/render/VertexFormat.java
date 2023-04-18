package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.VertexBuffer;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class VertexFormat {
   private final ImmutableList elements;
   private final ImmutableMap elementMap;
   private final IntList offsets = new IntArrayList();
   private final int vertexSizeByte;
   @Nullable
   private VertexBuffer buffer;

   public VertexFormat(ImmutableMap elementMap) {
      this.elementMap = elementMap;
      this.elements = elementMap.values().asList();
      int i = 0;

      VertexFormatElement lv;
      for(UnmodifiableIterator var3 = elementMap.values().iterator(); var3.hasNext(); i += lv.getByteLength()) {
         lv = (VertexFormatElement)var3.next();
         this.offsets.add(i);
      }

      this.vertexSizeByte = i;
   }

   public String toString() {
      int var10000 = this.elementMap.size();
      return "format: " + var10000 + " elements: " + (String)this.elementMap.entrySet().stream().map(Object::toString).collect(Collectors.joining(" "));
   }

   public int getVertexSizeInteger() {
      return this.getVertexSizeByte() / 4;
   }

   public int getVertexSizeByte() {
      return this.vertexSizeByte;
   }

   public ImmutableList getElements() {
      return this.elements;
   }

   public ImmutableList getAttributeNames() {
      return this.elementMap.keySet().asList();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VertexFormat lv = (VertexFormat)o;
         return this.vertexSizeByte != lv.vertexSizeByte ? false : this.elementMap.equals(lv.elementMap);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.elementMap.hashCode();
   }

   public void setupState() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::setupStateInternal);
      } else {
         this.setupStateInternal();
      }
   }

   private void setupStateInternal() {
      int i = this.getVertexSizeByte();
      List list = this.getElements();

      for(int j = 0; j < list.size(); ++j) {
         ((VertexFormatElement)list.get(j)).setupState(j, (long)this.offsets.getInt(j), i);
      }

   }

   public void clearState() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::clearStateInternal);
      } else {
         this.clearStateInternal();
      }
   }

   private void clearStateInternal() {
      ImmutableList immutableList = this.getElements();

      for(int i = 0; i < immutableList.size(); ++i) {
         VertexFormatElement lv = (VertexFormatElement)immutableList.get(i);
         lv.clearState(i);
      }

   }

   public VertexBuffer getBuffer() {
      VertexBuffer lv = this.buffer;
      if (lv == null) {
         this.buffer = lv = new VertexBuffer();
      }

      return lv;
   }

   @Environment(EnvType.CLIENT)
   public static enum DrawMode {
      LINES(4, 2, 2, false),
      LINE_STRIP(5, 2, 1, true),
      DEBUG_LINES(1, 2, 2, false),
      DEBUG_LINE_STRIP(3, 2, 1, true),
      TRIANGLES(4, 3, 3, false),
      TRIANGLE_STRIP(5, 3, 1, true),
      TRIANGLE_FAN(6, 3, 1, true),
      QUADS(4, 4, 4, false);

      public final int glMode;
      public final int firstVertexCount;
      public final int additionalVertexCount;
      public final boolean shareVertices;

      private DrawMode(int glMode, int firstVertexCount, int additionalVertexCount, boolean shareVertices) {
         this.glMode = glMode;
         this.firstVertexCount = firstVertexCount;
         this.additionalVertexCount = additionalVertexCount;
         this.shareVertices = shareVertices;
      }

      public int getIndexCount(int vertexCount) {
         int j;
         switch (this) {
            case LINE_STRIP:
            case DEBUG_LINES:
            case DEBUG_LINE_STRIP:
            case TRIANGLES:
            case TRIANGLE_STRIP:
            case TRIANGLE_FAN:
               j = vertexCount;
               break;
            case LINES:
            case QUADS:
               j = vertexCount / 4 * 6;
               break;
            default:
               j = 0;
         }

         return j;
      }

      // $FF: synthetic method
      private static DrawMode[] method_36817() {
         return new DrawMode[]{LINES, LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN, QUADS};
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum IndexType {
      SHORT(GlConst.GL_UNSIGNED_SHORT, 2),
      INT(GlConst.GL_UNSIGNED_INT, 4);

      public final int glType;
      public final int size;

      private IndexType(int glType, int size) {
         this.glType = glType;
         this.size = size;
      }

      public static IndexType smallestFor(int indexCount) {
         return (indexCount & -65536) != 0 ? INT : SHORT;
      }

      // $FF: synthetic method
      private static IndexType[] method_36816() {
         return new IndexType[]{SHORT, INT};
      }
   }
}
