package net.minecraft.client.sound;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Channel {
   private final Set sources = Sets.newIdentityHashSet();
   final SoundEngine soundEngine;
   final Executor executor;

   public Channel(SoundEngine soundEngine, Executor executor) {
      this.soundEngine = soundEngine;
      this.executor = executor;
   }

   public CompletableFuture createSource(SoundEngine.RunMode mode) {
      CompletableFuture completableFuture = new CompletableFuture();
      this.executor.execute(() -> {
         Source lv = this.soundEngine.createSource(mode);
         if (lv != null) {
            SourceManager lv2 = new SourceManager(lv);
            this.sources.add(lv2);
            completableFuture.complete(lv2);
         } else {
            completableFuture.complete((Object)null);
         }

      });
      return completableFuture;
   }

   public void execute(Consumer sourcesConsumer) {
      this.executor.execute(() -> {
         sourcesConsumer.accept(this.sources.stream().map((source) -> {
            return source.source;
         }).filter(Objects::nonNull));
      });
   }

   public void tick() {
      this.executor.execute(() -> {
         Iterator iterator = this.sources.iterator();

         while(iterator.hasNext()) {
            SourceManager lv = (SourceManager)iterator.next();
            lv.source.tick();
            if (lv.source.isStopped()) {
               lv.close();
               iterator.remove();
            }
         }

      });
   }

   public void close() {
      this.sources.forEach(SourceManager::close);
      this.sources.clear();
   }

   @Environment(EnvType.CLIENT)
   public class SourceManager {
      @Nullable
      Source source;
      private boolean stopped;

      public boolean isStopped() {
         return this.stopped;
      }

      public SourceManager(Source source) {
         this.source = source;
      }

      public void run(Consumer action) {
         Channel.this.executor.execute(() -> {
            if (this.source != null) {
               action.accept(this.source);
            }

         });
      }

      public void close() {
         this.stopped = true;
         Channel.this.soundEngine.release(this.source);
         this.source = null;
      }
   }
}
