package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public interface BufferVertexConsumer extends VertexConsumer {
   VertexFormatElement getCurrentElement();

   void nextElement();

   void putByte(int index, byte value);

   void putShort(int index, short value);

   void putFloat(int index, float value);

   default VertexConsumer vertex(double x, double y, double z) {
      if (this.getCurrentElement().getType() != VertexFormatElement.Type.POSITION) {
         return this;
      } else if (this.getCurrentElement().getComponentType() == VertexFormatElement.ComponentType.FLOAT && this.getCurrentElement().getComponentCount() == 3) {
         this.putFloat(0, (float)x);
         this.putFloat(4, (float)y);
         this.putFloat(8, (float)z);
         this.nextElement();
         return this;
      } else {
         throw new IllegalStateException();
      }
   }

   default VertexConsumer color(int red, int green, int blue, int alpha) {
      VertexFormatElement lv = this.getCurrentElement();
      if (lv.getType() != VertexFormatElement.Type.COLOR) {
         return this;
      } else if (lv.getComponentType() == VertexFormatElement.ComponentType.UBYTE && lv.getComponentCount() == 4) {
         this.putByte(0, (byte)red);
         this.putByte(1, (byte)green);
         this.putByte(2, (byte)blue);
         this.putByte(3, (byte)alpha);
         this.nextElement();
         return this;
      } else {
         throw new IllegalStateException();
      }
   }

   default VertexConsumer texture(float u, float v) {
      VertexFormatElement lv = this.getCurrentElement();
      if (lv.getType() == VertexFormatElement.Type.UV && lv.getUvIndex() == 0) {
         if (lv.getComponentType() == VertexFormatElement.ComponentType.FLOAT && lv.getComponentCount() == 2) {
            this.putFloat(0, u);
            this.putFloat(4, v);
            this.nextElement();
            return this;
         } else {
            throw new IllegalStateException();
         }
      } else {
         return this;
      }
   }

   default VertexConsumer overlay(int u, int v) {
      return this.uv((short)u, (short)v, 1);
   }

   default VertexConsumer light(int u, int v) {
      return this.uv((short)u, (short)v, 2);
   }

   default VertexConsumer uv(short u, short v, int index) {
      VertexFormatElement lv = this.getCurrentElement();
      if (lv.getType() == VertexFormatElement.Type.UV && lv.getUvIndex() == index) {
         if (lv.getComponentType() == VertexFormatElement.ComponentType.SHORT && lv.getComponentCount() == 2) {
            this.putShort(0, u);
            this.putShort(2, v);
            this.nextElement();
            return this;
         } else {
            throw new IllegalStateException();
         }
      } else {
         return this;
      }
   }

   default VertexConsumer normal(float x, float y, float z) {
      VertexFormatElement lv = this.getCurrentElement();
      if (lv.getType() != VertexFormatElement.Type.NORMAL) {
         return this;
      } else if (lv.getComponentType() == VertexFormatElement.ComponentType.BYTE && lv.getComponentCount() == 3) {
         this.putByte(0, packByte(x));
         this.putByte(1, packByte(y));
         this.putByte(2, packByte(z));
         this.nextElement();
         return this;
      } else {
         throw new IllegalStateException();
      }
   }

   static byte packByte(float f) {
      return (byte)((int)(MathHelper.clamp(f, -1.0F, 1.0F) * 127.0F) & 255);
   }
}
