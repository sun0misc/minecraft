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
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class UserCache {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_SAVED_ENTRIES = 1000;
   private static final int field_29789 = 1;
   private static boolean useRemote;
   private final Map byName = Maps.newConcurrentMap();
   private final Map byUuid = Maps.newConcurrentMap();
   private final Map pendingRequests = Maps.newConcurrentMap();
   private final GameProfileRepository profileRepository;
   private final Gson gson = (new GsonBuilder()).create();
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
      String string = gameProfile.getName();
      if (string != null) {
         this.byName.put(string.toLowerCase(Locale.ROOT), entry);
      }

      UUID uUID = gameProfile.getId();
      if (uUID != null) {
         this.byUuid.put(uUID, entry);
      }

   }

   private static Optional findProfileByName(GameProfileRepository repository, String name) {
      final AtomicReference atomicReference = new AtomicReference();
      ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback() {
         public void onProfileLookupSucceeded(GameProfile profile) {
            atomicReference.set(profile);
         }

         public void onProfileLookupFailed(GameProfile profile, Exception exception) {
            atomicReference.set((Object)null);
         }
      };
      repository.findProfilesByNames(new String[]{name}, Agent.MINECRAFT, profileLookupCallback);
      GameProfile gameProfile = (GameProfile)atomicReference.get();
      if (!shouldUseRemote() && gameProfile == null) {
         UUID uUID = Uuids.getUuidFromProfile(new GameProfile((UUID)null, name));
         return Optional.of(new GameProfile(uUID, name));
      } else {
         return Optional.ofNullable(gameProfile);
      }
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

   public Optional findByName(String name) {
      String string2 = name.toLowerCase(Locale.ROOT);
      Entry lv = (Entry)this.byName.get(string2);
      boolean bl = false;
      if (lv != null && (new Date()).getTime() >= lv.expirationDate.getTime()) {
         this.byUuid.remove(lv.getProfile().getId());
         this.byName.remove(lv.getProfile().getName().toLowerCase(Locale.ROOT));
         bl = true;
         lv = null;
      }

      Optional optional;
      if (lv != null) {
         lv.setLastAccessed(this.incrementAndGetAccessCount());
         optional = Optional.of(lv.getProfile());
      } else {
         optional = findProfileByName(this.profileRepository, string2);
         if (optional.isPresent()) {
            this.add((GameProfile)optional.get());
            bl = false;
         }
      }

      if (bl) {
         this.save();
      }

      return optional;
   }

   public void findByNameAsync(String username, Consumer consumer) {
      if (this.executor == null) {
         throw new IllegalStateException("No executor");
      } else {
         CompletableFuture completableFuture = (CompletableFuture)this.pendingRequests.get(username);
         if (completableFuture != null) {
            this.pendingRequests.put(username, completableFuture.whenCompleteAsync((profile, throwable) -> {
               consumer.accept(profile);
            }, this.executor));
         } else {
            this.pendingRequests.put(username, CompletableFuture.supplyAsync(() -> {
               return this.findByName(username);
            }, Util.getMainWorkerExecutor()).whenCompleteAsync((profile, throwable) -> {
               this.pendingRequests.remove(username);
            }, this.executor).whenCompleteAsync((profile, throwable) -> {
               consumer.accept(profile);
            }, this.executor));
         }

      }
   }

   public Optional getByUuid(UUID uuid) {
      Entry lv = (Entry)this.byUuid.get(uuid);
      if (lv == null) {
         return Optional.empty();
      } else {
         lv.setLastAccessed(this.incrementAndGetAccessCount());
         return Optional.of(lv.getProfile());
      }
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

   public List load() {
      List list = Lists.newArrayList();

      try {
         Reader reader = Files.newReader(this.cacheFile, StandardCharsets.UTF_8);

         label54: {
            ArrayList var4;
            try {
               JsonArray jsonArray = (JsonArray)this.gson.fromJson(reader, JsonArray.class);
               if (jsonArray != null) {
                  DateFormat dateFormat = getDateFormat();
                  jsonArray.forEach((json) -> {
                     Optional var10000 = entryFromJson(json, dateFormat);
                     Objects.requireNonNull(list);
                     var10000.ifPresent(list::add);
                  });
                  break label54;
               }

               var4 = list;
            } catch (Throwable var6) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (reader != null) {
               reader.close();
            }

            return var4;
         }

         if (reader != null) {
            reader.close();
         }
      } catch (FileNotFoundException var7) {
      } catch (JsonParseException | IOException var8) {
         LOGGER.warn("Failed to load profile cache {}", this.cacheFile, var8);
      }

      return list;
   }

   public void save() {
      JsonArray jsonArray = new JsonArray();
      DateFormat dateFormat = getDateFormat();
      this.getLastAccessedEntries(1000).forEach((entry) -> {
         jsonArray.add(entryToJson(entry, dateFormat));
      });
      String string = this.gson.toJson(jsonArray);

      try {
         Writer writer = Files.newWriter(this.cacheFile, StandardCharsets.UTF_8);

         try {
            writer.write(string);
         } catch (Throwable var8) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException var9) {
      }

   }

   private Stream getLastAccessedEntries(int limit) {
      return ImmutableList.copyOf(this.byUuid.values()).stream().sorted(Comparator.comparing(Entry::getLastAccessed).reversed()).limit((long)limit);
   }

   private static JsonElement entryToJson(Entry entry, DateFormat dateFormat) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("name", entry.getProfile().getName());
      UUID uUID = entry.getProfile().getId();
      jsonObject.addProperty("uuid", uUID == null ? "" : uUID.toString());
      jsonObject.addProperty("expiresOn", dateFormat.format(entry.getExpirationDate()));
      return jsonObject;
   }

   private static Optional entryFromJson(JsonElement json, DateFormat dateFormat) {
      if (json.isJsonObject()) {
         JsonObject jsonObject = json.getAsJsonObject();
         JsonElement jsonElement2 = jsonObject.get("name");
         JsonElement jsonElement3 = jsonObject.get("uuid");
         JsonElement jsonElement4 = jsonObject.get("expiresOn");
         if (jsonElement2 != null && jsonElement3 != null) {
            String string = jsonElement3.getAsString();
            String string2 = jsonElement2.getAsString();
            Date date = null;
            if (jsonElement4 != null) {
               try {
                  date = dateFormat.parse(jsonElement4.getAsString());
               } catch (ParseException var12) {
               }
            }

            if (string2 != null && string != null && date != null) {
               UUID uUID;
               try {
                  uUID = UUID.fromString(string);
               } catch (Throwable var11) {
                  return Optional.empty();
               }

               return Optional.of(new Entry(new GameProfile(uUID, string2), date));
            } else {
               return Optional.empty();
            }
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
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
