/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.session.telemetry;

import com.mojang.authlib.minecraft.TelemetryPropertyContainer;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.telemetry.GameLoadTimeEvent;
import net.minecraft.client.session.telemetry.PropertyMap;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;

@Environment(value=EnvType.CLIENT)
public record TelemetryEventProperty<T>(String id, String exportKey, Codec<T> codec, PropertyExporter<T> exporter) {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
    public static final TelemetryEventProperty<String> USER_ID = TelemetryEventProperty.ofString("user_id", "userId");
    public static final TelemetryEventProperty<String> CLIENT_ID = TelemetryEventProperty.ofString("client_id", "clientId");
    public static final TelemetryEventProperty<UUID> MINECRAFT_SESSION_ID = TelemetryEventProperty.ofUuid("minecraft_session_id", "deviceSessionId");
    public static final TelemetryEventProperty<String> GAME_VERSION = TelemetryEventProperty.ofString("game_version", "buildDisplayName");
    public static final TelemetryEventProperty<String> OPERATING_SYSTEM = TelemetryEventProperty.ofString("operating_system", "buildPlatform");
    public static final TelemetryEventProperty<String> PLATFORM = TelemetryEventProperty.ofString("platform", "platform");
    public static final TelemetryEventProperty<Boolean> CLIENT_MODDED = TelemetryEventProperty.ofBoolean("client_modded", "clientModded");
    public static final TelemetryEventProperty<String> LAUNCHER_NAME = TelemetryEventProperty.ofString("launcher_name", "launcherName");
    public static final TelemetryEventProperty<UUID> WORLD_SESSION_ID = TelemetryEventProperty.ofUuid("world_session_id", "worldSessionId");
    public static final TelemetryEventProperty<Boolean> SERVER_MODDED = TelemetryEventProperty.ofBoolean("server_modded", "serverModded");
    public static final TelemetryEventProperty<ServerType> SERVER_TYPE = TelemetryEventProperty.of("server_type", "serverType", ServerType.CODEC, (container, exportKey, value) -> container.addProperty(exportKey, value.asString()));
    public static final TelemetryEventProperty<Boolean> OPT_IN = TelemetryEventProperty.ofBoolean("opt_in", "isOptional");
    public static final TelemetryEventProperty<Instant> EVENT_TIMESTAMP_UTC = TelemetryEventProperty.of("event_timestamp_utc", "eventTimestampUtc", Codecs.INSTANT, (container, exportKey, value) -> container.addProperty(exportKey, DATE_TIME_FORMATTER.format((TemporalAccessor)value)));
    public static final TelemetryEventProperty<GameMode> GAME_MODE = TelemetryEventProperty.of("game_mode", "playerGameMode", GameMode.CODEC, (container, exportKey, value) -> container.addProperty(exportKey, value.getRawId()));
    public static final TelemetryEventProperty<String> REALMS_MAP_CONTENT = TelemetryEventProperty.ofString("realms_map_content", "realmsMapContent");
    public static final TelemetryEventProperty<Integer> SECONDS_SINCE_LOAD = TelemetryEventProperty.ofInteger("seconds_since_load", "secondsSinceLoad");
    public static final TelemetryEventProperty<Integer> TICKS_SINCE_LOAD = TelemetryEventProperty.ofInteger("ticks_since_load", "ticksSinceLoad");
    public static final TelemetryEventProperty<LongList> FRAME_RATE_SAMPLES = TelemetryEventProperty.ofLongList("frame_rate_samples", "serializedFpsSamples");
    public static final TelemetryEventProperty<LongList> RENDER_TIME_SAMPLES = TelemetryEventProperty.ofLongList("render_time_samples", "serializedRenderTimeSamples");
    public static final TelemetryEventProperty<LongList> USED_MEMORY_SAMPLES = TelemetryEventProperty.ofLongList("used_memory_samples", "serializedUsedMemoryKbSamples");
    public static final TelemetryEventProperty<Integer> NUMBER_OF_SAMPLES = TelemetryEventProperty.ofInteger("number_of_samples", "numSamples");
    public static final TelemetryEventProperty<Integer> RENDER_DISTANCE = TelemetryEventProperty.ofInteger("render_distance", "renderDistance");
    public static final TelemetryEventProperty<Integer> DEDICATED_MEMORY_KB = TelemetryEventProperty.ofInteger("dedicated_memory_kb", "dedicatedMemoryKb");
    public static final TelemetryEventProperty<Integer> WORLD_LOAD_TIME_MS = TelemetryEventProperty.ofInteger("world_load_time_ms", "worldLoadTimeMs");
    public static final TelemetryEventProperty<Boolean> NEW_WORLD = TelemetryEventProperty.ofBoolean("new_world", "newWorld");
    public static final TelemetryEventProperty<GameLoadTimeEvent.Measurement> LOAD_TIME_TOTAL_TIME_MS = TelemetryEventProperty.ofTimeMeasurement("load_time_total_time_ms", "loadTimeTotalTimeMs");
    public static final TelemetryEventProperty<GameLoadTimeEvent.Measurement> LOAD_TIME_PRE_WINDOW_MS = TelemetryEventProperty.ofTimeMeasurement("load_time_pre_window_ms", "loadTimePreWindowMs");
    public static final TelemetryEventProperty<GameLoadTimeEvent.Measurement> LOAD_TIME_BOOTSTRAP_MS = TelemetryEventProperty.ofTimeMeasurement("load_time_bootstrap_ms", "loadTimeBootstrapMs");
    public static final TelemetryEventProperty<GameLoadTimeEvent.Measurement> LOAD_TIME_LOADING_OVERLAY_MS = TelemetryEventProperty.ofTimeMeasurement("load_time_loading_overlay_ms", "loadTimeLoadingOverlayMs");
    public static final TelemetryEventProperty<String> ADVANCEMENT_ID = TelemetryEventProperty.ofString("advancement_id", "advancementId");
    public static final TelemetryEventProperty<Long> ADVANCEMENT_GAME_TIME = TelemetryEventProperty.ofLong("advancement_game_time", "advancementGameTime");

    public static <T> TelemetryEventProperty<T> of(String id, String exportKey, Codec<T> codec, PropertyExporter<T> exporter) {
        return new TelemetryEventProperty<T>(id, exportKey, codec, exporter);
    }

    public static TelemetryEventProperty<Boolean> ofBoolean(String id, String exportKey) {
        return TelemetryEventProperty.of(id, exportKey, Codec.BOOL, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryEventProperty<String> ofString(String id, String exportKey) {
        return TelemetryEventProperty.of(id, exportKey, Codec.STRING, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryEventProperty<Integer> ofInteger(String id, String exportKey) {
        return TelemetryEventProperty.of(id, exportKey, Codec.INT, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryEventProperty<Long> ofLong(String id, String exportKey) {
        return TelemetryEventProperty.of(id, exportKey, Codec.LONG, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryEventProperty<UUID> ofUuid(String id, String exportKey) {
        return TelemetryEventProperty.of(id, exportKey, Uuids.STRING_CODEC, (container, key, value) -> container.addProperty(key, value.toString()));
    }

    public static TelemetryEventProperty<GameLoadTimeEvent.Measurement> ofTimeMeasurement(String id, String exportKey) {
        return TelemetryEventProperty.of(id, exportKey, GameLoadTimeEvent.Measurement.CODEC, (container, key, value) -> container.addProperty(key, value.millis()));
    }

    public static TelemetryEventProperty<LongList> ofLongList(String id, String exportKey) {
        return TelemetryEventProperty.of(id, exportKey, Codec.LONG.listOf().xmap(LongArrayList::new, Function.identity()), (container, key, value) -> container.addProperty(key, value.longStream().mapToObj(String::valueOf).collect(Collectors.joining(";"))));
    }

    public void addTo(PropertyMap map, TelemetryPropertyContainer container) {
        Object object = map.get(this);
        if (object != null) {
            this.exporter.apply(container, this.exportKey, object);
        } else {
            container.addNullProperty(this.exportKey);
        }
    }

    public MutableText getTitle() {
        return Text.translatable("telemetry.property." + this.id + ".title");
    }

    @Override
    public String toString() {
        return "TelemetryProperty[" + this.id + "]";
    }

    @Environment(value=EnvType.CLIENT)
    public static interface PropertyExporter<T> {
        public void apply(TelemetryPropertyContainer var1, String var2, T var3);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum GameMode implements StringIdentifiable
    {
        SURVIVAL("survival", 0),
        CREATIVE("creative", 1),
        ADVENTURE("adventure", 2),
        SPECTATOR("spectator", 6),
        HARDCORE("hardcore", 99);

        public static final Codec<GameMode> CODEC;
        private final String id;
        private final int rawId;

        private GameMode(String id, int rawId) {
            this.id = id;
            this.rawId = rawId;
        }

        public int getRawId() {
            return this.rawId;
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(GameMode::values);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ServerType implements StringIdentifiable
    {
        REALM("realm"),
        LOCAL("local"),
        OTHER("server");

        public static final Codec<ServerType> CODEC;
        private final String id;

        private ServerType(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(ServerType::values);
        }
    }
}

