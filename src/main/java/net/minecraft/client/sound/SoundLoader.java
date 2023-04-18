package net.minecraft.client.sound;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class SoundLoader {
   private final ResourceFactory resourceFactory;
   private final Map loadedSounds = Maps.newHashMap();

   public SoundLoader(ResourceFactory resourceFactory) {
      this.resourceFactory = resourceFactory;
   }

   public CompletableFuture loadStatic(Identifier id) {
      return (CompletableFuture)this.loadedSounds.computeIfAbsent(id, (id2) -> {
         return CompletableFuture.supplyAsync(() -> {
            try {
               InputStream inputStream = this.resourceFactory.open(id2);

               StaticSound var5;
               try {
                  OggAudioStream lv = new OggAudioStream(inputStream);

                  try {
                     ByteBuffer byteBuffer = lv.getBuffer();
                     var5 = new StaticSound(byteBuffer, lv.getFormat());
                  } catch (Throwable var8) {
                     try {
                        lv.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }

                     throw var8;
                  }

                  lv.close();
               } catch (Throwable var9) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                     }
                  }

                  throw var9;
               }

               if (inputStream != null) {
                  inputStream.close();
               }

               return var5;
            } catch (IOException var10) {
               throw new CompletionException(var10);
            }
         }, Util.getMainWorkerExecutor());
      });
   }

   public CompletableFuture loadStreamed(Identifier id, boolean repeatInstantly) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            InputStream inputStream = this.resourceFactory.open(id);
            return (AudioStream)(repeatInstantly ? new RepeatingAudioStream(OggAudioStream::new, inputStream) : new OggAudioStream(inputStream));
         } catch (IOException var4) {
            throw new CompletionException(var4);
         }
      }, Util.getMainWorkerExecutor());
   }

   public void close() {
      this.loadedSounds.values().forEach((soundFuture) -> {
         soundFuture.thenAccept(StaticSound::close);
      });
      this.loadedSounds.clear();
   }

   public CompletableFuture loadStatic(Collection sounds) {
      return CompletableFuture.allOf((CompletableFuture[])sounds.stream().map((sound) -> {
         return this.loadStatic(sound.getLocation());
      }).toArray((i) -> {
         return new CompletableFuture[i];
      }));
   }
}
