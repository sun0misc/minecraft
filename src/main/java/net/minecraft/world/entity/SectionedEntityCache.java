package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Nullable;

public class SectionedEntityCache {
   private final Class entityClass;
   private final Long2ObjectFunction posToStatus;
   private final Long2ObjectMap trackingSections = new Long2ObjectOpenHashMap();
   private final LongSortedSet trackedPositions = new LongAVLTreeSet();

   public SectionedEntityCache(Class entityClass, Long2ObjectFunction chunkStatusDiscriminator) {
      this.entityClass = entityClass;
      this.posToStatus = chunkStatusDiscriminator;
   }

   public void forEachInBox(Box box, LazyIterationConsumer consumer) {
      int i = true;
      int j = ChunkSectionPos.getSectionCoord(box.minX - 2.0);
      int k = ChunkSectionPos.getSectionCoord(box.minY - 4.0);
      int l = ChunkSectionPos.getSectionCoord(box.minZ - 2.0);
      int m = ChunkSectionPos.getSectionCoord(box.maxX + 2.0);
      int n = ChunkSectionPos.getSectionCoord(box.maxY + 0.0);
      int o = ChunkSectionPos.getSectionCoord(box.maxZ + 2.0);

      for(int p = j; p <= m; ++p) {
         long q = ChunkSectionPos.asLong(p, 0, 0);
         long r = ChunkSectionPos.asLong(p, -1, -1);
         LongIterator longIterator = this.trackedPositions.subSet(q, r + 1L).iterator();

         while(longIterator.hasNext()) {
            long s = longIterator.nextLong();
            int t = ChunkSectionPos.unpackY(s);
            int u = ChunkSectionPos.unpackZ(s);
            if (t >= k && t <= n && u >= l && u <= o) {
               EntityTrackingSection lv = (EntityTrackingSection)this.trackingSections.get(s);
               if (lv != null && !lv.isEmpty() && lv.getStatus().shouldTrack() && consumer.accept(lv).shouldAbort()) {
                  return;
               }
            }
         }
      }

   }

   public LongStream getSections(long chunkPos) {
      int i = ChunkPos.getPackedX(chunkPos);
      int j = ChunkPos.getPackedZ(chunkPos);
      LongSortedSet longSortedSet = this.getSections(i, j);
      if (longSortedSet.isEmpty()) {
         return LongStream.empty();
      } else {
         PrimitiveIterator.OfLong ofLong = longSortedSet.iterator();
         return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(ofLong, 1301), false);
      }
   }

   private LongSortedSet getSections(int chunkX, int chunkZ) {
      long l = ChunkSectionPos.asLong(chunkX, 0, chunkZ);
      long m = ChunkSectionPos.asLong(chunkX, -1, chunkZ);
      return this.trackedPositions.subSet(l, m + 1L);
   }

   public Stream getTrackingSections(long chunkPos) {
      LongStream var10000 = this.getSections(chunkPos);
      Long2ObjectMap var10001 = this.trackingSections;
      Objects.requireNonNull(var10001);
      return var10000.mapToObj(var10001::get).filter(Objects::nonNull);
   }

   private static long chunkPosFromSectionPos(long sectionPos) {
      return ChunkPos.toLong(ChunkSectionPos.unpackX(sectionPos), ChunkSectionPos.unpackZ(sectionPos));
   }

   public EntityTrackingSection getTrackingSection(long sectionPos) {
      return (EntityTrackingSection)this.trackingSections.computeIfAbsent(sectionPos, this::addSection);
   }

   @Nullable
   public EntityTrackingSection findTrackingSection(long sectionPos) {
      return (EntityTrackingSection)this.trackingSections.get(sectionPos);
   }

   private EntityTrackingSection addSection(long sectionPos) {
      long m = chunkPosFromSectionPos(sectionPos);
      EntityTrackingStatus lv = (EntityTrackingStatus)this.posToStatus.get(m);
      this.trackedPositions.add(sectionPos);
      return new EntityTrackingSection(this.entityClass, lv);
   }

   public LongSet getChunkPositions() {
      LongSet longSet = new LongOpenHashSet();
      this.trackingSections.keySet().forEach((sectionPos) -> {
         longSet.add(chunkPosFromSectionPos(sectionPos));
      });
      return longSet;
   }

   public void forEachIntersects(Box box, LazyIterationConsumer consumer) {
      this.forEachInBox(box, (section) -> {
         return section.forEach(box, consumer);
      });
   }

   public void forEachIntersects(TypeFilter filter, Box box, LazyIterationConsumer consumer) {
      this.forEachInBox(box, (section) -> {
         return section.forEach(filter, box, consumer);
      });
   }

   public void removeSection(long sectionPos) {
      this.trackingSections.remove(sectionPos);
      this.trackedPositions.remove(sectionPos);
   }

   @Debug
   public int sectionCount() {
      return this.trackedPositions.size();
   }
}
