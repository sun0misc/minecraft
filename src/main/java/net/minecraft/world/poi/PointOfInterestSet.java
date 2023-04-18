package net.minecraft.world.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.slf4j.Logger;

public class PointOfInterestSet {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Short2ObjectMap pointsOfInterestByPos;
   private final Map pointsOfInterestByType;
   private final Runnable updateListener;
   private boolean valid;

   public static Codec createCodec(Runnable updateListener) {
      Codec var10000 = RecordCodecBuilder.create((instance) -> {
         return instance.group(RecordCodecBuilder.point(updateListener), Codec.BOOL.optionalFieldOf("Valid", false).forGetter((poiSet) -> {
            return poiSet.valid;
         }), PointOfInterest.createCodec(updateListener).listOf().fieldOf("Records").forGetter((poiSet) -> {
            return ImmutableList.copyOf(poiSet.pointsOfInterestByPos.values());
         })).apply(instance, PointOfInterestSet::new);
      });
      Logger var10002 = LOGGER;
      Objects.requireNonNull(var10002);
      return var10000.orElseGet(Util.addPrefix("Failed to read POI section: ", var10002::error), () -> {
         return new PointOfInterestSet(updateListener, false, ImmutableList.of());
      });
   }

   public PointOfInterestSet(Runnable updateListener) {
      this(updateListener, true, ImmutableList.of());
   }

   private PointOfInterestSet(Runnable updateListener, boolean valid, List pois) {
      this.pointsOfInterestByPos = new Short2ObjectOpenHashMap();
      this.pointsOfInterestByType = Maps.newHashMap();
      this.updateListener = updateListener;
      this.valid = valid;
      pois.forEach(this::add);
   }

   public Stream get(Predicate predicate, PointOfInterestStorage.OccupationStatus occupationStatus) {
      return this.pointsOfInterestByType.entrySet().stream().filter((entry) -> {
         return predicate.test((RegistryEntry)entry.getKey());
      }).flatMap((entry) -> {
         return ((Set)entry.getValue()).stream();
      }).filter(occupationStatus.getPredicate());
   }

   public void add(BlockPos pos, RegistryEntry type) {
      if (this.add(new PointOfInterest(pos, type, this.updateListener))) {
         LOGGER.debug("Added POI of type {} @ {}", type.getKey().map((key) -> {
            return key.getValue().toString();
         }).orElse("[unregistered]"), pos);
         this.updateListener.run();
      }

   }

   private boolean add(PointOfInterest poi) {
      BlockPos lv = poi.getPos();
      RegistryEntry lv2 = poi.getType();
      short s = ChunkSectionPos.packLocal(lv);
      PointOfInterest lv3 = (PointOfInterest)this.pointsOfInterestByPos.get(s);
      if (lv3 != null) {
         if (lv2.equals(lv3.getType())) {
            return false;
         }

         Util.error("POI data mismatch: already registered at " + lv);
      }

      this.pointsOfInterestByPos.put(s, poi);
      ((Set)this.pointsOfInterestByType.computeIfAbsent(lv2, (type) -> {
         return Sets.newHashSet();
      })).add(poi);
      return true;
   }

   public void remove(BlockPos pos) {
      PointOfInterest lv = (PointOfInterest)this.pointsOfInterestByPos.remove(ChunkSectionPos.packLocal(pos));
      if (lv == null) {
         LOGGER.error("POI data mismatch: never registered at {}", pos);
      } else {
         ((Set)this.pointsOfInterestByType.get(lv.getType())).remove(lv);
         Logger var10000 = LOGGER;
         Objects.requireNonNull(lv);
         Object var10002 = LogUtils.defer(lv::getType);
         Objects.requireNonNull(lv);
         var10000.debug("Removed POI of type {} @ {}", var10002, LogUtils.defer(lv::getPos));
         this.updateListener.run();
      }
   }

   /** @deprecated */
   @Deprecated
   @Debug
   public int getFreeTickets(BlockPos pos) {
      return (Integer)this.get(pos).map(PointOfInterest::getFreeTickets).orElse(0);
   }

   public boolean releaseTicket(BlockPos pos) {
      PointOfInterest lv = (PointOfInterest)this.pointsOfInterestByPos.get(ChunkSectionPos.packLocal(pos));
      if (lv == null) {
         throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("POI never registered at " + pos));
      } else {
         boolean bl = lv.releaseTicket();
         this.updateListener.run();
         return bl;
      }
   }

   public boolean test(BlockPos pos, Predicate predicate) {
      return this.getType(pos).filter(predicate).isPresent();
   }

   public Optional getType(BlockPos pos) {
      return this.get(pos).map(PointOfInterest::getType);
   }

   private Optional get(BlockPos pos) {
      return Optional.ofNullable((PointOfInterest)this.pointsOfInterestByPos.get(ChunkSectionPos.packLocal(pos)));
   }

   public void updatePointsOfInterest(Consumer updater) {
      if (!this.valid) {
         Short2ObjectMap short2ObjectMap = new Short2ObjectOpenHashMap(this.pointsOfInterestByPos);
         this.clear();
         updater.accept((pos, arg2) -> {
            short s = ChunkSectionPos.packLocal(pos);
            PointOfInterest lv = (PointOfInterest)short2ObjectMap.computeIfAbsent(s, (sx) -> {
               return new PointOfInterest(pos, arg2, this.updateListener);
            });
            this.add(lv);
         });
         this.valid = true;
         this.updateListener.run();
      }

   }

   private void clear() {
      this.pointsOfInterestByPos.clear();
      this.pointsOfInterestByType.clear();
   }

   boolean isValid() {
      return this.valid;
   }
}
