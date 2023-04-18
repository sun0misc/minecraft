package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.scanner.ExclusiveNbtCollector;
import net.minecraft.nbt.scanner.NbtScanQuery;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.crash.CrashMemoryReserve;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.WorldGenSettings;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LevelStorage {
   static final Logger LOGGER = LogUtils.getLogger();
   static final DateTimeFormatter TIME_FORMATTER;
   private static final ImmutableList GENERATOR_OPTION_KEYS;
   private static final String DATA_KEY = "Data";
   final Path savesDirectory;
   private final Path backupsDirectory;
   final DataFixer dataFixer;

   public LevelStorage(Path savesDirectory, Path backupsDirectory, DataFixer dataFixer) {
      this.dataFixer = dataFixer;

      try {
         PathUtil.createDirectories(savesDirectory);
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      }

      this.savesDirectory = savesDirectory;
      this.backupsDirectory = backupsDirectory;
   }

   public static LevelStorage create(Path path) {
      return new LevelStorage(path, path.resolve("../backups"), Schemas.getFixer());
   }

   private static DataResult readGeneratorProperties(Dynamic levelData, DataFixer dataFixer, int version) {
      Dynamic dynamic2 = levelData.get("WorldGenSettings").orElseEmptyMap();
      UnmodifiableIterator var4 = GENERATOR_OPTION_KEYS.iterator();

      while(var4.hasNext()) {
         String string = (String)var4.next();
         Optional optional = levelData.get(string).result();
         if (optional.isPresent()) {
            dynamic2 = dynamic2.set(string, (Dynamic)optional.get());
         }
      }

      Dynamic dynamic3 = DataFixTypes.WORLD_GEN_SETTINGS.update(dataFixer, dynamic2, version);
      return WorldGenSettings.CODEC.parse(dynamic3);
   }

   private static DataConfiguration parseDataPackSettings(Dynamic dynamic) {
      DataResult var10000 = DataConfiguration.CODEC.parse(dynamic);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      return (DataConfiguration)var10000.resultOrPartial(var10001::error).orElse(DataConfiguration.SAFE_MODE);
   }

   public String getFormatName() {
      return "Anvil";
   }

   public LevelList getLevelList() throws LevelStorageException {
      if (!Files.isDirectory(this.savesDirectory, new LinkOption[0])) {
         throw new LevelStorageException(Text.translatable("selectWorld.load_folder_access"));
      } else {
         try {
            List list = Files.list(this.savesDirectory).filter((path) -> {
               return Files.isDirectory(path, new LinkOption[0]);
            }).map(LevelSave::new).filter((levelSave) -> {
               return Files.isRegularFile(levelSave.getLevelDatPath(), new LinkOption[0]) || Files.isRegularFile(levelSave.getLevelDatOldPath(), new LinkOption[0]);
            }).toList();
            return new LevelList(list);
         } catch (IOException var2) {
            throw new LevelStorageException(Text.translatable("selectWorld.load_folder_access"));
         }
      }
   }

   public CompletableFuture loadSummaries(LevelList levels) {
      List list = new ArrayList(levels.levels.size());
      Iterator var3 = levels.levels.iterator();

      while(var3.hasNext()) {
         LevelSave lv = (LevelSave)var3.next();
         list.add(CompletableFuture.supplyAsync(() -> {
            boolean bl;
            try {
               bl = SessionLock.isLocked(lv.path());
            } catch (Exception var6) {
               LOGGER.warn("Failed to read {} lock", lv.path(), var6);
               return null;
            }

            try {
               LevelSummary lvx = (LevelSummary)this.readLevelProperties(lv, this.createLevelDataParser(lv, bl));
               return lvx != null ? lvx : null;
            } catch (OutOfMemoryError var4) {
               CrashMemoryReserve.releaseMemory();
               System.gc();
               LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of memory trying to read summary of {}", lv.getRootPath());
               throw var4;
            } catch (StackOverflowError var5) {
               LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of stack trying to read summary of {}. Assuming corruption; attempting to restore from from level.dat_old.", lv.getRootPath());
               Util.backupAndReplace(lv.getLevelDatPath(), lv.getLevelDatOldPath(), lv.getCorruptedLevelDatPath(LocalDateTime.now()), true);
               throw var5;
            }
         }, Util.getMainWorkerExecutor()));
      }

      return Util.combineCancellable(list).thenApply((summaries) -> {
         return summaries.stream().filter(Objects::nonNull).sorted().toList();
      });
   }

   private int getCurrentVersion() {
      return 19133;
   }

   @Nullable
   Object readLevelProperties(LevelSave levelSave, BiFunction levelDataParser) {
      if (!Files.exists(levelSave.path(), new LinkOption[0])) {
         return null;
      } else {
         Path path = levelSave.getLevelDatPath();
         if (Files.exists(path, new LinkOption[0])) {
            Object object = levelDataParser.apply(path, this.dataFixer);
            if (object != null) {
               return object;
            }
         }

         path = levelSave.getLevelDatOldPath();
         return Files.exists(path, new LinkOption[0]) ? levelDataParser.apply(path, this.dataFixer) : null;
      }
   }

   @Nullable
   private static DataConfiguration readDataPackSettings(Path path, DataFixer dataFixer) {
      try {
         NbtElement lv = loadCompactLevelData(path);
         if (lv instanceof NbtCompound lv2) {
            NbtCompound lv3 = lv2.getCompound("Data");
            int i = NbtHelper.getDataVersion(lv3, -1);
            Dynamic dynamic = DataFixTypes.LEVEL.update(dataFixer, new Dynamic(NbtOps.INSTANCE, lv3), i);
            return parseDataPackSettings(dynamic);
         }
      } catch (Exception var7) {
         LOGGER.error("Exception reading {}", path, var7);
      }

      return null;
   }

   static BiFunction createLevelDataParser(DynamicOps ops, DataConfiguration dataConfiguration, Registry dimensionOptionsRegistry, Lifecycle lifecycle) {
      return (path, dataFixer) -> {
         NbtCompound lv;
         try {
            lv = NbtIo.readCompressed(path.toFile());
         } catch (IOException var17) {
            throw new UncheckedIOException(var17);
         }

         NbtCompound lv2 = lv.getCompound("Data");
         NbtCompound lv3 = lv2.contains("Player", NbtElement.COMPOUND_TYPE) ? lv2.getCompound("Player") : null;
         lv2.remove("Player");
         int i = NbtHelper.getDataVersion(lv2, -1);
         Dynamic dynamic = DataFixTypes.LEVEL.update(dataFixer, new Dynamic(ops, lv2), i);
         DataResult var10000 = readGeneratorProperties(dynamic, dataFixer, i);
         Logger var10003 = LOGGER;
         Objects.requireNonNull(var10003);
         WorldGenSettings lv4 = (WorldGenSettings)var10000.getOrThrow(false, Util.addPrefix("WorldGenSettings: ", var10003::error));
         SaveVersionInfo lv5 = SaveVersionInfo.fromDynamic(dynamic);
         LevelInfo lv6 = LevelInfo.fromDynamic(dynamic, dataConfiguration);
         DimensionOptionsRegistryHolder.DimensionsConfig lv7 = lv4.dimensionOptionsRegistryHolder().toConfig(dimensionOptionsRegistry);
         Lifecycle lifecycle2 = lv7.getLifecycle().add(lifecycle);
         LevelProperties lv8 = LevelProperties.readProperties(dynamic, dataFixer, i, lv3, lv6, lv5, lv7.specialWorldProperty(), lv4.generatorOptions(), lifecycle2);
         return Pair.of(lv8, lv7);
      };
   }

   BiFunction createLevelDataParser(LevelSave levelSave, boolean locked) {
      return (path, dataFixer) -> {
         try {
            NbtElement lv = loadCompactLevelData(path);
            if (lv instanceof NbtCompound lv2) {
               NbtCompound lv3 = lv2.getCompound("Data");
               int i = NbtHelper.getDataVersion(lv3, -1);
               Dynamic dynamic = DataFixTypes.LEVEL.update(dataFixer, new Dynamic(NbtOps.INSTANCE, lv3), i);
               SaveVersionInfo lv4 = SaveVersionInfo.fromDynamic(dynamic);
               int j = lv4.getLevelFormatVersion();
               if (j == 19132 || j == 19133) {
                  boolean bl2 = j != this.getCurrentVersion();
                  Path path2 = levelSave.getIconPath();
                  DataConfiguration lv5 = parseDataPackSettings(dynamic);
                  LevelInfo lv6 = LevelInfo.fromDynamic(dynamic, lv5);
                  FeatureSet lv7 = parseEnabledFeatures(dynamic);
                  boolean bl3 = FeatureFlags.isNotVanilla(lv7);
                  return new LevelSummary(lv6, lv4, levelSave.getRootPath(), bl2, locked, bl3, path2);
               }
            } else {
               LOGGER.warn("Invalid root tag in {}", path);
            }

            return null;
         } catch (Exception var18) {
            LOGGER.error("Exception reading {}", path, var18);
            return null;
         }
      };
   }

   private static FeatureSet parseEnabledFeatures(Dynamic levelData) {
      Set set = (Set)levelData.get("enabled_features").asStream().flatMap((featureFlag) -> {
         return featureFlag.asString().result().map(Identifier::tryParse).stream();
      }).collect(Collectors.toSet());
      return FeatureFlags.FEATURE_MANAGER.featureSetOf(set, (id) -> {
      });
   }

   @Nullable
   private static NbtElement loadCompactLevelData(Path path) throws IOException {
      ExclusiveNbtCollector lv = new ExclusiveNbtCollector(new NbtScanQuery[]{new NbtScanQuery("Data", NbtCompound.TYPE, "Player"), new NbtScanQuery("Data", NbtCompound.TYPE, "WorldGenSettings")});
      NbtIo.scanCompressed((File)path.toFile(), lv);
      return lv.getRoot();
   }

   public boolean isLevelNameValid(String name) {
      try {
         Path path = this.savesDirectory.resolve(name);
         Files.createDirectory(path);
         Files.deleteIfExists(path);
         return true;
      } catch (IOException var3) {
         return false;
      }
   }

   public boolean levelExists(String name) {
      return Files.isDirectory(this.savesDirectory.resolve(name), new LinkOption[0]);
   }

   public Path getSavesDirectory() {
      return this.savesDirectory;
   }

   public Path getBackupsDirectory() {
      return this.backupsDirectory;
   }

   public Session createSession(String directoryName) throws IOException {
      return new Session(directoryName);
   }

   static {
      TIME_FORMATTER = (new DateTimeFormatterBuilder()).appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();
      GENERATOR_OPTION_KEYS = ImmutableList.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");
   }

   public static record LevelList(List levels) implements Iterable {
      final List levels;

      public LevelList(List list) {
         this.levels = list;
      }

      public boolean isEmpty() {
         return this.levels.isEmpty();
      }

      public Iterator iterator() {
         return this.levels.iterator();
      }

      public List levels() {
         return this.levels;
      }
   }

   public static record LevelSave(Path path) {
      public LevelSave(Path path) {
         this.path = path;
      }

      public String getRootPath() {
         return this.path.getFileName().toString();
      }

      public Path getLevelDatPath() {
         return this.getPath(WorldSavePath.LEVEL_DAT);
      }

      public Path getLevelDatOldPath() {
         return this.getPath(WorldSavePath.LEVEL_DAT_OLD);
      }

      public Path getCorruptedLevelDatPath(LocalDateTime dateTime) {
         Path var10000 = this.path;
         String var10001 = WorldSavePath.LEVEL_DAT.getRelativePath();
         return var10000.resolve(var10001 + "_corrupted_" + dateTime.format(LevelStorage.TIME_FORMATTER));
      }

      public Path getIconPath() {
         return this.getPath(WorldSavePath.ICON_PNG);
      }

      public Path getSessionLockPath() {
         return this.getPath(WorldSavePath.SESSION_LOCK);
      }

      public Path getPath(WorldSavePath savePath) {
         return this.path.resolve(savePath.getRelativePath());
      }

      public Path path() {
         return this.path;
      }
   }

   public class Session implements AutoCloseable {
      final SessionLock lock;
      final LevelSave directory;
      private final String directoryName;
      private final Map paths = Maps.newHashMap();

      public Session(String directoryName) throws IOException {
         this.directoryName = directoryName;
         this.directory = new LevelSave(LevelStorage.this.savesDirectory.resolve(directoryName));
         this.lock = SessionLock.create(this.directory.path());
      }

      public String getDirectoryName() {
         return this.directoryName;
      }

      public Path getDirectory(WorldSavePath savePath) {
         Map var10000 = this.paths;
         LevelSave var10002 = this.directory;
         Objects.requireNonNull(var10002);
         return (Path)var10000.computeIfAbsent(savePath, var10002::getPath);
      }

      public Path getWorldDirectory(RegistryKey key) {
         return DimensionType.getSaveDirectory(key, this.directory.path());
      }

      private void checkValid() {
         if (!this.lock.isValid()) {
            throw new IllegalStateException("Lock is no longer valid");
         }
      }

      public WorldSaveHandler createSaveHandler() {
         this.checkValid();
         return new WorldSaveHandler(this, LevelStorage.this.dataFixer);
      }

      @Nullable
      public LevelSummary getLevelSummary() {
         this.checkValid();
         return (LevelSummary)LevelStorage.this.readLevelProperties(this.directory, LevelStorage.this.createLevelDataParser(this.directory, false));
      }

      @Nullable
      public Pair readLevelProperties(DynamicOps ops, DataConfiguration dataConfiguration, Registry dimensionOptionsRegistry, Lifecycle lifecycle) {
         this.checkValid();
         return (Pair)LevelStorage.this.readLevelProperties(this.directory, LevelStorage.createLevelDataParser(ops, dataConfiguration, dimensionOptionsRegistry, lifecycle));
      }

      @Nullable
      public DataConfiguration getDataPackSettings() {
         this.checkValid();
         return (DataConfiguration)LevelStorage.this.readLevelProperties(this.directory, LevelStorage::readDataPackSettings);
      }

      public void backupLevelDataFile(DynamicRegistryManager registryManager, SaveProperties saveProperties) {
         this.backupLevelDataFile(registryManager, saveProperties, (NbtCompound)null);
      }

      public void backupLevelDataFile(DynamicRegistryManager registryManager, SaveProperties saveProperties, @Nullable NbtCompound nbt) {
         File file = this.directory.path().toFile();
         NbtCompound lv = saveProperties.cloneWorldNbt(registryManager, nbt);
         NbtCompound lv2 = new NbtCompound();
         lv2.put("Data", lv);

         try {
            File file2 = File.createTempFile("level", ".dat", file);
            NbtIo.writeCompressed(lv2, file2);
            File file3 = this.directory.getLevelDatOldPath().toFile();
            File file4 = this.directory.getLevelDatPath().toFile();
            Util.backupAndReplace(file4, file2, file3);
         } catch (Exception var10) {
            LevelStorage.LOGGER.error("Failed to save level {}", file, var10);
         }

      }

      public Optional getIconFile() {
         return !this.lock.isValid() ? Optional.empty() : Optional.of(this.directory.getIconPath());
      }

      public void deleteSessionLock() throws IOException {
         this.checkValid();
         final Path path = this.directory.getSessionLockPath();
         LevelStorage.LOGGER.info("Deleting level {}", this.directoryName);
         int i = 1;

         while(i <= 5) {
            LevelStorage.LOGGER.info("Attempt {}...", i);

            try {
               Files.walkFileTree(this.directory.path(), new SimpleFileVisitor() {
                  public FileVisitResult visitFile(Path pathx, BasicFileAttributes basicFileAttributes) throws IOException {
                     if (!pathx.equals(path)) {
                        LevelStorage.LOGGER.debug("Deleting {}", pathx);
                        Files.delete(pathx);
                     }

                     return FileVisitResult.CONTINUE;
                  }

                  public FileVisitResult postVisitDirectory(Path pathx, IOException iOException) throws IOException {
                     if (iOException != null) {
                        throw iOException;
                     } else {
                        if (pathx.equals(Session.this.directory.path())) {
                           Session.this.lock.close();
                           Files.deleteIfExists(path);
                        }

                        Files.delete(pathx);
                        return FileVisitResult.CONTINUE;
                     }
                  }

                  // $FF: synthetic method
                  public FileVisitResult postVisitDirectory(Object pathx, IOException exception) throws IOException {
                     return this.postVisitDirectory((Path)pathx, exception);
                  }

                  // $FF: synthetic method
                  public FileVisitResult visitFile(Object pathx, BasicFileAttributes attributes) throws IOException {
                     return this.visitFile((Path)pathx, attributes);
                  }
               });
               break;
            } catch (IOException var6) {
               if (i >= 5) {
                  throw var6;
               }

               LevelStorage.LOGGER.warn("Failed to delete {}", this.directory.path(), var6);

               try {
                  Thread.sleep(500L);
               } catch (InterruptedException var5) {
               }

               ++i;
            }
         }

      }

      public void save(String name) throws IOException {
         this.checkValid();
         Path path = this.directory.getLevelDatPath();
         if (Files.exists(path, new LinkOption[0])) {
            NbtCompound lv = NbtIo.readCompressed(path.toFile());
            NbtCompound lv2 = lv.getCompound("Data");
            lv2.putString("LevelName", name);
            NbtIo.writeCompressed(lv, path.toFile());
         }

      }

      public long createBackup() throws IOException {
         this.checkValid();
         String var10000 = LocalDateTime.now().format(LevelStorage.TIME_FORMATTER);
         String string = var10000 + "_" + this.directoryName;
         Path path = LevelStorage.this.getBackupsDirectory();

         try {
            PathUtil.createDirectories(path);
         } catch (IOException var9) {
            throw new RuntimeException(var9);
         }

         Path path2 = path.resolve(PathUtil.getNextUniqueName(path, string, ".zip"));
         final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path2)));

         try {
            final Path path3 = Paths.get(this.directoryName);
            Files.walkFileTree(this.directory.path(), new SimpleFileVisitor() {
               public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                  if (path.endsWith("session.lock")) {
                     return FileVisitResult.CONTINUE;
                  } else {
                     String string = path3.resolve(Session.this.directory.path().relativize(path)).toString().replace('\\', '/');
                     ZipEntry zipEntry = new ZipEntry(string);
                     zipOutputStream.putNextEntry(zipEntry);
                     com.google.common.io.Files.asByteSource(path.toFile()).copyTo(zipOutputStream);
                     zipOutputStream.closeEntry();
                     return FileVisitResult.CONTINUE;
                  }
               }

               // $FF: synthetic method
               public FileVisitResult visitFile(Object path, BasicFileAttributes attributes) throws IOException {
                  return this.visitFile((Path)path, attributes);
               }
            });
         } catch (Throwable var8) {
            try {
               zipOutputStream.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         zipOutputStream.close();
         return Files.size(path2);
      }

      public void close() throws IOException {
         this.lock.close();
      }
   }
}
