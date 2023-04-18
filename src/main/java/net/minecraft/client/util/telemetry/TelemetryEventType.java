package net.minecraft.client.util.telemetry;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TelemetryEventType {
   static final Map TYPES = new Object2ObjectLinkedOpenHashMap();
   public static final Codec CODEC;
   private static final List BASIC_PROPERTIES;
   private static final List REQUIRED_PROPERTIES;
   public static final TelemetryEventType WORLD_LOADED;
   public static final TelemetryEventType PERFORMANCE_METRICS;
   public static final TelemetryEventType WORLD_LOAD_TIMES;
   public static final TelemetryEventType WORLD_UNLOADED;
   private final String id;
   private final String exportKey;
   private final List properties;
   private final boolean optional;
   private final Codec codec;

   TelemetryEventType(String id, String exportKey, List properties, boolean optional) {
      this.id = id;
      this.exportKey = exportKey;
      this.properties = properties;
      this.optional = optional;
      this.codec = PropertyMap.createCodec(properties).xmap((map) -> {
         return new SentTelemetryEvent(this, map);
      }, SentTelemetryEvent::properties);
   }

   public static Builder builder(String id, String sentEventId) {
      return new Builder(id, sentEventId);
   }

   public String getId() {
      return this.id;
   }

   public List getProperties() {
      return this.properties;
   }

   public Codec getCodec() {
      return this.codec;
   }

   public boolean isOptional() {
      return this.optional;
   }

   public TelemetryEvent createEvent(TelemetrySession session, PropertyMap properties) {
      TelemetryEvent telemetryEvent = session.createNewEvent(this.exportKey);
      Iterator var4 = this.properties.iterator();

      while(var4.hasNext()) {
         TelemetryEventProperty lv = (TelemetryEventProperty)var4.next();
         lv.addTo(properties, telemetryEvent);
      }

      return telemetryEvent;
   }

   public boolean hasProperty(TelemetryEventProperty property) {
      return this.properties.contains(property);
   }

   public String toString() {
      return "TelemetryEventType[" + this.id + "]";
   }

   public MutableText getTitle() {
      return this.getText("title");
   }

   public MutableText getDescription() {
      return this.getText("description");
   }

   private MutableText getText(String key) {
      return Text.translatable("telemetry.event." + this.id + "." + key);
   }

   public static List getTypes() {
      return List.copyOf(TYPES.values());
   }

   static {
      CODEC = Codec.STRING.comapFlatMap((id) -> {
         TelemetryEventType lv = (TelemetryEventType)TYPES.get(id);
         return lv != null ? DataResult.success(lv) : DataResult.error(() -> {
            return "No TelemetryEventType with key: '" + id + "'";
         });
      }, TelemetryEventType::getId);
      BASIC_PROPERTIES = List.of(TelemetryEventProperty.USER_ID, TelemetryEventProperty.CLIENT_ID, TelemetryEventProperty.MINECRAFT_SESSION_ID, TelemetryEventProperty.GAME_VERSION, TelemetryEventProperty.OPERATING_SYSTEM, TelemetryEventProperty.PLATFORM, TelemetryEventProperty.CLIENT_MODDED, TelemetryEventProperty.EVENT_TIMESTAMP_UTC, TelemetryEventProperty.OPT_IN);
      REQUIRED_PROPERTIES = Stream.concat(BASIC_PROPERTIES.stream(), Stream.of(TelemetryEventProperty.WORLD_SESSION_ID, TelemetryEventProperty.SERVER_MODDED, TelemetryEventProperty.SERVER_TYPE)).toList();
      WORLD_LOADED = builder("world_loaded", "WorldLoaded").properties(REQUIRED_PROPERTIES).properties(TelemetryEventProperty.GAME_MODE).build();
      PERFORMANCE_METRICS = builder("performance_metrics", "PerformanceMetrics").properties(REQUIRED_PROPERTIES).properties(TelemetryEventProperty.FRAME_RATE_SAMPLES).properties(TelemetryEventProperty.RENDER_TIME_SAMPLES).properties(TelemetryEventProperty.USED_MEMORY_SAMPLES).properties(TelemetryEventProperty.NUMBER_OF_SAMPLES).properties(TelemetryEventProperty.RENDER_DISTANCE).properties(TelemetryEventProperty.DEDICATED_MEMORY_KB).optional().build();
      WORLD_LOAD_TIMES = builder("world_load_times", "WorldLoadTimes").properties(REQUIRED_PROPERTIES).properties(TelemetryEventProperty.WORLD_LOAD_TIME_MS).properties(TelemetryEventProperty.NEW_WORLD).optional().build();
      WORLD_UNLOADED = builder("world_unloaded", "WorldUnloaded").properties(REQUIRED_PROPERTIES).properties(TelemetryEventProperty.SECONDS_SINCE_LOAD).properties(TelemetryEventProperty.TICKS_SINCE_LOAD).build();
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      private final String id;
      private final String exportKey;
      private final List properties = new ArrayList();
      private boolean optional;

      Builder(String id, String exportKey) {
         this.id = id;
         this.exportKey = exportKey;
      }

      public Builder properties(List properties) {
         this.properties.addAll(properties);
         return this;
      }

      public Builder properties(TelemetryEventProperty property) {
         this.properties.add(property);
         return this;
      }

      public Builder optional() {
         this.optional = true;
         return this;
      }

      public TelemetryEventType build() {
         TelemetryEventType lv = new TelemetryEventType(this.id, this.exportKey, List.copyOf(this.properties), this.optional);
         if (TelemetryEventType.TYPES.putIfAbsent(this.id, lv) != null) {
            throw new IllegalStateException("Duplicate TelemetryEventType with key: '" + this.id + "'");
         } else {
            return lv;
         }
      }
   }
}
