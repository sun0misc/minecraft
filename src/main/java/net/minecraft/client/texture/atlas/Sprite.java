package net.minecraft.client.texture.atlas;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class Sprite {
   private final Identifier id;
   private final Resource resource;
   private final AtomicReference image = new AtomicReference();
   private final AtomicInteger regionCount;

   public Sprite(Identifier id, Resource resource, int regionCount) {
      this.id = id;
      this.resource = resource;
      this.regionCount = new AtomicInteger(regionCount);
   }

   public NativeImage read() throws IOException {
      NativeImage lv = (NativeImage)this.image.get();
      if (lv == null) {
         synchronized(this) {
            lv = (NativeImage)this.image.get();
            if (lv == null) {
               try {
                  InputStream inputStream = this.resource.getInputStream();

                  try {
                     lv = NativeImage.read(inputStream);
                     this.image.set(lv);
                  } catch (Throwable var8) {
                     if (inputStream != null) {
                        try {
                           inputStream.close();
                        } catch (Throwable var7) {
                           var8.addSuppressed(var7);
                        }
                     }

                     throw var8;
                  }

                  if (inputStream != null) {
                     inputStream.close();
                  }
               } catch (IOException var9) {
                  throw new IOException("Failed to load image " + this.id, var9);
               }
            }
         }
      }

      return lv;
   }

   public void close() {
      int i = this.regionCount.decrementAndGet();
      if (i <= 0) {
         NativeImage lv = (NativeImage)this.image.getAndSet((Object)null);
         if (lv != null) {
            lv.close();
         }
      }

   }
}
