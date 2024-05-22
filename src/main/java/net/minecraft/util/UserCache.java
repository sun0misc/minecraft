/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class UserCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SAVED_ENTRIES = 1000;
    private static final int field_29789 = 1;
    private static boolean useRemote;
    private final Map<String, Entry> byName = Maps.newConcurrentMap();
    private final Map<UUID, Entry> byUuid = Maps.newConcurrentMap();
    private final Map<String, CompletableFuture<Optional<GameProfile>>> pendingRequests = Maps.newConcurrentMap();
    private final GameProfileRepository profileRepository;
    private final Gson gson = new GsonBuilder().create();
    private final File cacheFile;
    private final AtomicLong accessCount = new AtomicLong();
    @Nullable
    private Executor executor;

    public UserCache(GameProfileRepository profileRepository, File cacheFile) {
        this.profileRepository = profileRepository;
        this.cacheFile = cacheFile;
        Lists.reverse(this.load()).forEach(this::add);
    }

    private void add(Entry entry) {
        GameProfile gameProfile = entry.getProfile();
        entry.setLastAccessed(this.incrementAndGetAccessCount());
        this.byName.put(gameProfile.getName().toLowerCase(Locale.ROOT), entry);
        this.byUuid.put(gameProfile.getId(), entry);
    }

    private static Optional<GameProfile> findProfileByName(GameProfileRepository repository, String name) {
        if (!StringHelper.isValidPlayerName(name)) {
            return UserCache.getOfflinePlayerProfile(name);
        }
        final AtomicReference atomicReference = new AtomicReference();
        ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){

            @Override
            public void onProfileLookupSucceeded(GameProfile profile) {
                atomicReference.set(profile);
            }

            @Override
            public void onProfileLookupFailed(String string, Exception exception) {
                atomicReference.set(null);
            }
        };
        repository.findProfilesByNames(new String[]{name}, profileLookupCallback);
        GameProfile gameProfile = (GameProfile)atomicReference.get();
        return gameProfile != null ? Optional.of(gameProfile) : UserCache.getOfflinePlayerProfile(name);
    }

    private static Optional<GameProfile> getOfflinePlayerProfile(String name) {
        if (UserCache.shouldUseRemote()) {
            return Optional.empty();
        }
        return Optional.of(Uuids.getOfflinePlayerProfile(name));
    }

    public static void setUseRemote(boolean value) {
        useRemote = value;
    }

    private static boolean shouldUseRemote() {
        return useRemote;
    }

    public void add(GameProfile profile) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(2, 1);
        Date date = calendar.getTime();
        Entry lv = new Entry(profile, date);
        this.add(lv);
        this.save();
    }

    private long incrementAndGetAccessCount() {
        return this.accessCount.incrementAndGet();
    }

    public Optional<GameProfile> findByName(String name) {
        Optional<GameProfile> optional;
        String string2 = name.toLowerCase(Locale.ROOT);
        Entry lv = this.byName.get(string2);
        boolean bl = false;
        if (lv != null && new Date().getTime() >= lv.expirationDate.getTime()) {
            this.byUuid.remove(lv.getProfile().getId());
            this.byName.remove(lv.getProfile().getName().toLowerCase(Locale.ROOT));
            bl = true;
            lv = null;
        }
        if (lv != null) {
            lv.setLastAccessed(this.incrementAndGetAccessCount());
            optional = Optional.of(lv.getProfile());
        } else {
            optional = UserCache.findProfileByName(this.profileRepository, string2);
            if (optional.isPresent()) {
                this.add(optional.get());
                bl = false;
            }
        }
        if (bl) {
            this.save();
        }
        return optional;
    }

    public CompletableFuture<Optional<GameProfile>> findByNameAsync(String username) {
        if (this.executor == null) {
            throw new IllegalStateException("No executor");
        }
        CompletableFuture<Optional<GameProfile>> completableFuture = this.pendingRequests.get(username);
        if (completableFuture != null) {
            return completableFuture;
        }
        CompletionStage completableFuture2 = CompletableFuture.supplyAsync(() -> this.findByName(username), Util.getMainWorkerExecutor()).whenCompleteAsync((profile, throwable) -> this.pendingRequests.remove(username), this.executor);
        this.pendingRequests.put(username, (CompletableFuture<Optional<GameProfile>>)completableFuture2);
        return completableFuture2;
    }

    public Optional<GameProfile> getByUuid(UUID uuid) {
        Entry lv = this.byUuid.get(uuid);
        if (lv == null) {
            return Optional.empty();
        }
        lv.setLastAccessed(this.incrementAndGetAccessCount());
        return Optional.of(lv.getProfile());
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void clearExecutor() {
        this.executor = null;
    }

    private static DateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public List<Entry> load() {
        ArrayList<Entry> list = Lists.newArrayList();
        try (BufferedReader reader2222 = Files.newReader(this.cacheFile, StandardCharsets.UTF_8);){
            JsonArray jsonArray = this.gson.fromJson((Reader)reader2222, JsonArray.class);
            if (jsonArray == null) {
                ArrayList<Entry> arrayList = list;
                return arrayList;
            }
            DateFormat dateFormat = UserCache.getDateFormat();
            jsonArray.forEach(json -> UserCache.entryFromJson(json, dateFormat).ifPresent(list::add));
            return list;
        } catch (FileNotFoundException reader2222) {
            return list;
        } catch (JsonParseException | IOException exception) {
            LOGGER.warn("Failed to load profile cache {}", (Object)this.cacheFile, (Object)exception);
        }
        return list;
    }

    public void save() {
        JsonArray jsonArray = new JsonArray();
        DateFormat dateFormat = UserCache.getDateFormat();
        this.getLastAccessedEntries(1000).forEach(entry -> jsonArray.add(UserCache.entryToJson(entry, dateFormat)));
        String string = this.gson.toJson(jsonArray);
        try (BufferedWriter writer = Files.newWriter(this.cacheFile, StandardCharsets.UTF_8);){
            writer.write(string);
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    private Stream<Entry> getLastAccessedEntries(int limit) {
        return ImmutableList.copyOf(this.byUuid.values()).stream().sorted(Comparator.comparing(Entry::getLastAccessed).reversed()).limit(limit);
    }

    private static JsonElement entryToJson(Entry entry, DateFormat dateFormat) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", entry.getProfile().getName());
        jsonObject.addProperty("uuid", entry.getProfile().getId().toString());
        jsonObject.addProperty("expiresOn", dateFormat.format(entry.getExpirationDate()));
        return jsonObject;
    }

    private static Optional<Entry> entryFromJson(JsonElement json, DateFormat dateFormat) {
        if (json.isJsonObject()) {
            UUID uUID;
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement jsonElement2 = jsonObject.get("name");
            JsonElement jsonElement3 = jsonObject.get("uuid");
            JsonElement jsonElement4 = jsonObject.get("expiresOn");
            if (jsonElement2 == null || jsonElement3 == null) {
                return Optional.empty();
            }
            String string = jsonElement3.getAsString();
            String string2 = jsonElement2.getAsString();
            Date date = null;
            if (jsonElement4 != null) {
                try {
                    date = dateFormat.parse(jsonElement4.getAsString());
                } catch (ParseException parseException) {
                    // empty catch block
                }
            }
            if (string2 == null || string == null || date == null) {
                return Optional.empty();
            }
            try {
                uUID = UUID.fromString(string);
            } catch (Throwable throwable) {
                return Optional.empty();
            }
            return Optional.of(new Entry(new GameProfile(uUID, string2), date));
        }
        return Optional.empty();
    }

    static class Entry {
        private final GameProfile profile;
        final Date expirationDate;
        private volatile long lastAccessed;

        Entry(GameProfile profile, Date expirationDate) {
            this.profile = profile;
            this.expirationDate = expirationDate;
        }

        public GameProfile getProfile() {
            return this.profile;
        }

        public Date getExpirationDate() {
            return this.expirationDate;
        }

        public void setLastAccessed(long lastAccessed) {
            this.lastAccessed = lastAccessed;
        }

        public long getLastAccessed() {
            return this.lastAccessed;
        }
    }
}

