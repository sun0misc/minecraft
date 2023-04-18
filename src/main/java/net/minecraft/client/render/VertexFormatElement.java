package net.minecraft.client.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexFormatElement {
   private final ComponentType componentType;
   private final Type type;
   private final int uvIndex;
   private final int componentCount;
   private final int byteLength;

   public VertexFormatElement(int uvIndex, ComponentType componentType, Type type, int componentCount) {
      if (this.isValidType(uvIndex, type)) {
         this.type = type;
         this.componentType = componentType;
         this.uvIndex = uvIndex;
         this.componentCount = componentCount;
         this.byteLength = componentType.getByteLength() * this.componentCount;
      } else {
         throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
      }
   }

   private boolean isValidType(int uvIndex, Type type) {
      return uvIndex == 0 || type == VertexFormatElement.Type.UV;
   }

   public final ComponentType getComponentType() {
      return this.componentType;
   }

   public final Type getType() {
      return this.type;
   }

   public final int getComponentCount() {
      return this.componentCount;
   }

   public final int getUvIndex() {
      return this.uvIndex;
   }

   public String toString() {
      int var10000 = this.componentCount;
      return "" + var10000 + "," + this.type.getName() + "," + this.componentType.getName();
   }

   public final int getByteLength() {
      return this.byteLength;
   }

   public final boolean isPosition() {
      return this.type == VertexFormatElement.Type.POSITION;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VertexFormatElement lv = (VertexFormatElement)o;
         if (this.componentCount != lv.componentCount) {
            return false;
         } else if (this.uvIndex != lv.uvIndex) {
            return false;
         } else if (this.componentType != lv.componentType) {
            return false;
         } else {
            return this.type == lv.type;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = this.componentType.hashCode();
      i = 31 * i + this.type.hashCode();
      i = 31 * i + this.uvIndex;
      i = 31 * i + this.componentCount;
      return i;
   }

   public void setupState(int elementIndex, long offset, int stride) {
      this.type.setupState(this.componentCount, this.componentType.getGlType(), stride, offset, this.uvIndex, elementIndex);
   }

   public void clearState(int elementIndex) {
      this.type.clearState(this.uvIndex, elementIndex);
   }

   @Environment(EnvType.CLIENT)
   public static enum Type {
      POSITION("Position", (componentCount, componentType, stride, offset, uvIndex, elementIndex) -> {
         GlStateManager._enableVertexAttribArray(elementIndex);
         GlStateManager._vertexAttribPointer(elementIndex, componentCount, componentType, false, stride, offset);
      }, (uvIndex, elementIndex) -> {
         GlStateManager._disableVertexAttribArray(elementIndex);
      }),
      NORMAL("Normal", (componentCount, componentType, stride, offset, uvIndex, elementIndex) -> {
         GlStateManager._enableVertexAttribArray(elementIndex);
         GlStateManager._vertexAttribPointer(elementIndex, componentCount, componentType, true, stride, offset);
      }, (uvIndex, elementIndex) -> {
         GlStateManager._disableVertexAttribArray(elementIndex);
      }),
      COLOR("Vertex Color", (componentCount, componentType, stride, offset, uvIndex, elementIndex) -> {
         GlStateManager._enableVertexAttribArray(elementIndex);
         GlStateManager._vertexAttribPointer(elementIndex, componentCount, componentType, true, stride, offset);
      }, (uvIndex, elementIndex) -> {
         GlStateManager._disableVertexAttribArray(elementIndex);
      }),
      UV("UV", (componentCount, componentType, stride, offset, uvIndex, elementIndex) -> {
         GlStateManager._enableVertexAttribArray(elementIndex);
         if (componentType == 5126) {
            GlStateManager._vertexAttribPointer(elementIndex, componentCount, componentType, false, stride, offset);
         } else {
            GlStateManager._vertexAttribIPointer(elementIndex, componentCount, componentType, stride, offset);
         }

      }, (uvIndex, elementIndex) -> {
         GlStateManager._disableVertexAttribArray(elementIndex);
      }),
      PADDING("Padding", (componentCount, componentType, stride, offset, uvIndex, elementIndex) -> {
      }, (uvIndex, elementIndex) -> {
      }),
      GENERIC("Generic", (componentCount, componentType, stride, offset, uvIndex, elementIndex) -> {
         GlStateManager._enableVertexAttribArray(elementIndex);
         GlStateManager._vertexAttribPointer(elementIndex, componentCount, componentType, false, stride, offset);
      }, (uvIndex, elementIndex) -> {
         GlStateManager._disableVertexAttribArray(elementIndex);
      });

      private final String name;
      private final SetupTask setupTask;
      private final ClearTask clearTask;

      private Type(String name, SetupTask setupTask, ClearTask clearTask) {
         this.name = name;
         this.setupTask = setupTask;
         this.clearTask = clearTask;
      }

      void setupState(int componentCount, int componentType, int stride, long offset, int uvIndex, int elementIndex) {
         this.setupTask.setupBufferState(componentCount, componentType, stride, offset, uvIndex, elementIndex);
      }

      public void clearState(int uvIndex, int elementIndex) {
         this.clearTask.clearBufferState(uvIndex, elementIndex);
      }

      public String getName() {
         return this.name;
      }

      // $FF: synthetic method
      private static Type[] method_36819() {
         return new Type[]{POSITION, NORMAL, COLOR, UV, PADDING, GENERIC};
      }

      @FunctionalInterface
      @Environment(EnvType.CLIENT)
      private interface SetupTask {
         void setupBufferState(int componentCount, int componentType, int stride, long offset, int uvIndex, int elementIndex);
      }

      @FunctionalInterface
      @Environment(EnvType.CLIENT)
      private interface ClearTask {
         void clearBufferState(int uvIndex, int elementIndex);
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum ComponentType {
      FLOAT(4, "Float", GlConst.GL_FLOAT),
      UBYTE(1, "Unsigned Byte", GlConst.GL_UNSIGNED_BYTE),
      BYTE(1, "Byte", GlConst.GL_BYTE),
      USHORT(2, "Unsigned Short", GlConst.GL_UNSIGNED_SHORT),
      SHORT(2, "Short", GlConst.GL_SHORT),
      UINT(4, "Unsigned Int", GlConst.GL_UNSIGNED_INT),
      INT(4, "Int", GlConst.GL_INT);

      private final int byteLength;
      private final String name;
      private final int glType;

      private ComponentType(int byteLength, String name, int glType) {
         this.byteLength = byteLength;
         this.name = name;
         this.glType = glType;
      }

      public int getByteLength() {
         return this.byteLength;
      }

      public String getName() {
         return this.name;
      }

      public int getGlType() {
         return this.glType;
      }

      // $FF: synthetic method
      private static ComponentType[] method_36818() {
         return new ComponentType[]{FLOAT, UBYTE, BYTE, USHORT, SHORT, UINT, INT};
      }
   }
}
