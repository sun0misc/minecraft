package net.minecraft.util;

import com.google.common.base.Ticker;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.state.property.Property;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.function.CharPredicate;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Util {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_PARALLELISM = 255;
   private static final String MAX_BG_THREADS_PROPERTY = "max.bg.threads";
   private static final AtomicInteger NEXT_WORKER_ID = new AtomicInteger(1);
   private static final ExecutorService MAIN_WORKER_EXECUTOR = createWorker("Main");
   private static final ExecutorService IO_WORKER_EXECUTOR = createIoWorker();
   private static final DateTimeFormatter DATE_TIME_FORMATTER;
   public static TimeSupplier.Nanoseconds nanoTimeSupplier;
   public static final Ticker TICKER;
   public static final UUID NIL_UUID;
   public static final FileSystemProvider JAR_FILE_SYSTEM_PROVIDER;
   private static Consumer missingBreakpointHandler;

   public static Collector toMap() {
      return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
   }

   public static String getValueAsString(Property property, Object value) {
      return property.name((Comparable)value);
   }

   public static String createTranslationKey(String type, @Nullable Identifier id) {
      return id == null ? type + ".unregistered_sadface" : type + "." + id.getNamespace() + "." + id.getPath().replace('/', '.');
   }

   public static long getMeasuringTimeMs() {
      return getMeasuringTimeNano() / 1000000L;
   }

   public static long getMeasuringTimeNano() {
      return nanoTimeSupplier.getAsLong();
   }

   public static long getEpochTimeMs() {
      return Instant.now().toEpochMilli();
   }

   public static String getFormattedCurrentTime() {
      return DATE_TIME_FORMATTER.format(ZonedDateTime.now());
   }

   private static ExecutorService createWorker(String name) {
      int i = MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, getMaxBackgroundThreads());
      Object executorService;
      if (i <= 0) {
         executorService = MoreExecutors.newDirectExecutorService();
      } else {
         executorService = new ForkJoinPool(i, (forkJoinPool) -> {
            ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool) {
               protected void onTermination(Throwable throwable) {
                  if (throwable != null) {
                     Util.LOGGER.warn("{} died", this.getName(), throwable);
                  } else {
                     Util.LOGGER.debug("{} shutdown", this.getName());
                  }

                  super.onTermination(throwable);
               }
            };
            forkJoinWorkerThread.setName("Worker-" + name + "-" + NEXT_WORKER_ID.getAndIncrement());
            return forkJoinWorkerThread;
         }, Util::uncaughtExceptionHandler, true);
      }

      return (ExecutorService)executorService;
   }

   private static int getMaxBackgroundThreads() {
      String string = System.getProperty("max.bg.threads");
      if (string != null) {
         try {
            int i = Integer.parseInt(string);
            if (i >= 1 && i <= 255) {
               return i;
            }

            LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{"max.bg.threads", string, 255});
         } catch (NumberFormatException var2) {
            LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{"max.bg.threads", string, 255});
         }
      }

      return 255;
   }

   public static ExecutorService getMainWorkerExecutor() {
      return MAIN_WORKER_EXECUTOR;
   }

   public static ExecutorService getIoWorkerExecutor() {
      return IO_WORKER_EXECUTOR;
   }

   public static void shutdownExecutors() {
      attemptShutdown(MAIN_WORKER_EXECUTOR);
      attemptShutdown(IO_WORKER_EXECUTOR);
   }

   private static void attemptShutdown(ExecutorService service) {
      service.shutdown();

      boolean bl;
      try {
         bl = service.awaitTermination(3L, TimeUnit.SECONDS);
      } catch (InterruptedException var3) {
         bl = false;
      }

      if (!bl) {
         service.shutdownNow();
      }

   }

   private static ExecutorService createIoWorker() {
      return Executors.newCachedThreadPool((runnable) -> {
         Thread thread = new Thread(runnable);
         thread.setName("IO-Worker-" + NEXT_WORKER_ID.getAndIncrement());
         thread.setUncaughtExceptionHandler(Util::uncaughtExceptionHandler);
         return thread;
      });
   }

   public static void throwUnchecked(Throwable t) {
      throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(t);
   }

   private static void uncaughtExceptionHandler(Thread thread, Throwable t) {
      throwOrPause(t);
      if (t instanceof CompletionException) {
         t = t.getCause();
      }

      if (t instanceof CrashException) {
         Bootstrap.println(((CrashException)t).getReport().asString());
         System.exit(-1);
      }

      LOGGER.error(String.format(Locale.ROOT, "Caught exception in thread %s", thread), t);
   }

   @Nullable
   public static Type getChoiceType(DSL.TypeReference typeReference, String id) {
      return !SharedConstants.useChoiceTypeRegistrations ? null : getChoiceTypeInternal(typeReference, id);
   }

   @Nullable
   private static Type getChoiceTypeInternal(DSL.TypeReference typeReference, String id) {
      Type type = null;

      try {
         type = Schemas.getFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getSaveVersion().getId())).getChoiceType(typeReference, id);
      } catch (IllegalArgumentException var4) {
         LOGGER.error("No data fixer registered for {}", id);
         if (SharedConstants.isDevelopment) {
            throw var4;
         }
      }

      return type;
   }

   public static Runnable debugRunnable(String activeThreadName, Runnable task) {
      return SharedConstants.isDevelopment ? () -> {
         Thread thread = Thread.currentThread();
         String string2 = thread.getName();
         thread.setName(activeThreadName);

         try {
            task.run();
         } finally {
            thread.setName(string2);
         }

      } : task;
   }

   public static Supplier debugSupplier(String activeThreadName, Supplier supplier) {
      return SharedConstants.isDevelopment ? () -> {
         Thread thread = Thread.currentThread();
         String string2 = thread.getName();
         thread.setName(activeThreadName);

         Object var4;
         try {
            var4 = supplier.get();
         } finally {
            thread.setName(string2);
         }

         return var4;
      } : supplier;
   }

   public static OperatingSystem getOperatingSystem() {
      String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
      if (string.contains("win")) {
         return Util.OperatingSystem.WINDOWS;
      } else if (string.contains("mac")) {
         return Util.OperatingSystem.OSX;
      } else if (string.contains("solaris")) {
         return Util.OperatingSystem.SOLARIS;
      } else if (string.contains("sunos")) {
         return Util.OperatingSystem.SOLARIS;
      } else if (string.contains("linux")) {
         return Util.OperatingSystem.LINUX;
      } else {
         return string.contains("unix") ? Util.OperatingSystem.LINUX : Util.OperatingSystem.UNKNOWN;
      }
   }

   public static Stream getJVMFlags() {
      RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
      return runtimeMXBean.getInputArguments().stream().filter((runtimeArg) -> {
         return runtimeArg.startsWith("-X");
      });
   }

   public static Object getLast(List list) {
      return list.get(list.size() - 1);
   }

   public static Object next(Iterable iterable, @Nullable Object object) {
      Iterator iterator = iterable.iterator();
      Object object2 = iterator.next();
      if (object != null) {
         Object object3 = object2;

         while(object3 != object) {
            if (iterator.hasNext()) {
               object3 = iterator.next();
            }
         }

         if (iterator.hasNext()) {
            return iterator.next();
         }
      }

      return object2;
   }

   public static Object previous(Iterable iterable, @Nullable Object object) {
      Iterator iterator = iterable.iterator();

      Object object2;
      Object object3;
      for(object2 = null; iterator.hasNext(); object2 = object3) {
         object3 = iterator.next();
         if (object3 == object) {
            if (object2 == null) {
               object2 = iterator.hasNext() ? Iterators.getLast(iterator) : object;
            }
            break;
         }
      }

      return object2;
   }

   public static Object make(Supplier factory) {
      return factory.get();
   }

   public static Object make(Object object, Consumer initializer) {
      initializer.accept(object);
      return object;
   }

   public static Hash.Strategy identityHashStrategy() {
      return Util.IdentityHashStrategy.INSTANCE;
   }

   public static CompletableFuture combineSafe(List futures) {
      if (futures.isEmpty()) {
         return CompletableFuture.completedFuture(List.of());
      } else if (futures.size() == 1) {
         return ((CompletableFuture)futures.get(0)).thenApply(List::of);
      } else {
         CompletableFuture completableFuture = CompletableFuture.allOf((CompletableFuture[])futures.toArray(new CompletableFuture[0]));
         return completableFuture.thenApply((void_) -> {
            return futures.stream().map(CompletableFuture::join).toList();
         });
      }
   }

   public static CompletableFuture combine(List futures) {
      CompletableFuture completableFuture = new CompletableFuture();
      Objects.requireNonNull(completableFuture);
      return combine(futures, completableFuture::completeExceptionally).applyToEither(completableFuture, Function.identity());
   }

   public static CompletableFuture combineCancellable(List futures) {
      CompletableFuture completableFuture = new CompletableFuture();
      return combine(futures, (throwable) -> {
         if (completableFuture.completeExceptionally(throwable)) {
            Iterator var3 = futures.iterator();

            while(var3.hasNext()) {
               CompletableFuture completableFuture2 = (CompletableFuture)var3.next();
               completableFuture2.cancel(true);
            }
         }

      }).applyToEither(completableFuture, Function.identity());
   }

   private static CompletableFuture combine(List futures, Consumer exceptionHandler) {
      List list2 = Lists.newArrayListWithCapacity(futures.size());
      CompletableFuture[] completableFutures = new CompletableFuture[futures.size()];
      futures.forEach((future) -> {
         int i = list2.size();
         list2.add((Object)null);
         completableFutures[i] = future.whenComplete((value, throwable) -> {
            if (throwable != null) {
               exceptionHandler.accept(throwable);
            } else {
               list2.set(i, value);
            }

         });
      });
      return CompletableFuture.allOf(completableFutures).thenApply((void_) -> {
         return list2;
      });
   }

   public static Optional ifPresentOrElse(Optional optional, Consumer presentAction, Runnable elseAction) {
      if (optional.isPresent()) {
         presentAction.accept(optional.get());
      } else {
         elseAction.run();
      }

      return optional;
   }

   public static Supplier debugSupplier(Supplier supplier, Supplier messageSupplier) {
      return supplier;
   }

   public static Runnable debugRunnable(Runnable runnable, Supplier messageSupplier) {
      return runnable;
   }

   public static void error(String message) {
      LOGGER.error(message);
      if (SharedConstants.isDevelopment) {
         pause(message);
      }

   }

   public static void error(String message, Throwable throwable) {
      LOGGER.error(message, throwable);
      if (SharedConstants.isDevelopment) {
         pause(message);
      }

   }

   public static Throwable throwOrPause(Throwable t) {
      if (SharedConstants.isDevelopment) {
         LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t);
         pause(t.getMessage());
      }

      return t;
   }

   public static void setMissingBreakpointHandler(Consumer missingBreakpointHandler) {
      Util.missingBreakpointHandler = missingBreakpointHandler;
   }

   private static void pause(String message) {
      Instant instant = Instant.now();
      LOGGER.warn("Did you remember to set a breakpoint here?");
      boolean bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
      if (!bl) {
         missingBreakpointHandler.accept(message);
      }

   }

   public static String getInnermostMessage(Throwable t) {
      if (t.getCause() != null) {
         return getInnermostMessage(t.getCause());
      } else {
         return t.getMessage() != null ? t.getMessage() : t.toString();
      }
   }

   public static Object getRandom(Object[] array, Random random) {
      return array[random.nextInt(array.length)];
   }

   public static int getRandom(int[] array, Random random) {
      return array[random.nextInt(array.length)];
   }

   public static Object getRandom(List list, Random random) {
      return list.get(random.nextInt(list.size()));
   }

   public static Optional getRandomOrEmpty(List list, Random random) {
      return list.isEmpty() ? Optional.empty() : Optional.of(getRandom(list, random));
   }

   private static BooleanSupplier renameTask(final Path src, final Path dest) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.move(src, dest);
               return true;
            } catch (IOException var2) {
               Util.LOGGER.error("Failed to rename", var2);
               return false;
            }
         }

         public String toString() {
            return "rename " + src + " to " + dest;
         }
      };
   }

   private static BooleanSupplier deleteTask(final Path path) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.deleteIfExists(path);
               return true;
            } catch (IOException var2) {
               Util.LOGGER.warn("Failed to delete", var2);
               return false;
            }
         }

         public String toString() {
            return "delete old " + path;
         }
      };
   }

   private static BooleanSupplier deletionVerifyTask(final Path path) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return !Files.exists(path, new LinkOption[0]);
         }

         public String toString() {
            return "verify that " + path + " is deleted";
         }
      };
   }

   private static BooleanSupplier existenceCheckTask(final Path path) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return Files.isRegularFile(path, new LinkOption[0]);
         }

         public String toString() {
            return "verify that " + path + " is present";
         }
      };
   }

   private static boolean attemptTasks(BooleanSupplier... tasks) {
      BooleanSupplier[] var1 = tasks;
      int var2 = tasks.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         BooleanSupplier booleanSupplier = var1[var3];
         if (!booleanSupplier.getAsBoolean()) {
            LOGGER.warn("Failed to execute {}", booleanSupplier);
            return false;
         }
      }

      return true;
   }

   private static boolean attemptTasks(int retries, String taskName, BooleanSupplier... tasks) {
      for(int j = 0; j < retries; ++j) {
         if (attemptTasks(tasks)) {
            return true;
         }

         LOGGER.error("Failed to {}, retrying {}/{}", new Object[]{taskName, j, retries});
      }

      LOGGER.error("Failed to {}, aborting, progress might be lost", taskName);
      return false;
   }

   public static void backupAndReplace(File current, File newFile, File backup) {
      backupAndReplace(current.toPath(), newFile.toPath(), backup.toPath());
   }

   public static void backupAndReplace(Path current, Path newPath, Path backup) {
      backupAndReplace(current, newPath, backup, false);
   }

   public static void backupAndReplace(File current, File newPath, File backup, boolean noRestoreOnFail) {
      backupAndReplace(current.toPath(), newPath.toPath(), backup.toPath(), noRestoreOnFail);
   }

   public static void backupAndReplace(Path current, Path newPath, Path backup, boolean noRestoreOnFail) {
      int i = true;
      if (!Files.exists(current, new LinkOption[0]) || attemptTasks(10, "create backup " + backup, deleteTask(backup), renameTask(current, backup), existenceCheckTask(backup))) {
         if (attemptTasks(10, "remove old " + current, deleteTask(current), deletionVerifyTask(current))) {
            if (!attemptTasks(10, "replace " + current + " with " + newPath, renameTask(newPath, current), existenceCheckTask(current)) && !noRestoreOnFail) {
               attemptTasks(10, "restore " + current + " from " + backup, renameTask(backup, current), existenceCheckTask(current));
            }

         }
      }
   }

   public static int moveCursor(String string, int cursor, int delta) {
      int k = string.length();
      int l;
      if (delta >= 0) {
         for(l = 0; cursor < k && l < delta; ++l) {
            if (Character.isHighSurrogate(string.charAt(cursor++)) && cursor < k && Character.isLowSurrogate(string.charAt(cursor))) {
               ++cursor;
            }
         }
      } else {
         for(l = delta; cursor > 0 && l < 0; ++l) {
            --cursor;
            if (Character.isLowSurrogate(string.charAt(cursor)) && cursor > 0 && Character.isHighSurrogate(string.charAt(cursor - 1))) {
               --cursor;
            }
         }
      }

      return cursor;
   }

   public static Consumer addPrefix(String prefix, Consumer consumer) {
      return (string) -> {
         consumer.accept(prefix + string);
      };
   }

   public static DataResult toArray(IntStream stream, int length) {
      int[] is = stream.limit((long)(length + 1)).toArray();
      if (is.length != length) {
         Supplier supplier = () -> {
            return "Input is not a list of " + length + " ints";
         };
         return is.length >= length ? DataResult.error(supplier, Arrays.copyOf(is, length)) : DataResult.error(supplier);
      } else {
         return DataResult.success(is);
      }
   }

   public static DataResult toArray(List list, int length) {
      if (list.size() != length) {
         Supplier supplier = () -> {
            return "Input is not a list of " + length + " elements";
         };
         return list.size() >= length ? DataResult.error(supplier, list.subList(0, length)) : DataResult.error(supplier);
      } else {
         return DataResult.success(list);
      }
   }

   public static void startTimerHack() {
      Thread thread = new Thread("Timer hack thread") {
         public void run() {
            while(true) {
               try {
                  Thread.sleep(2147483647L);
               } catch (InterruptedException var2) {
                  Util.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                  return;
               }
            }
         }
      };
      thread.setDaemon(true);
      thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
      thread.start();
   }

   public static void relativeCopy(Path src, Path dest, Path toCopy) throws IOException {
      Path path4 = src.relativize(toCopy);
      Path path5 = dest.resolve(path4);
      Files.copy(toCopy, path5);
   }

   public static String replaceInvalidChars(String string, CharPredicate predicate) {
      return (String)string.toLowerCase(Locale.ROOT).chars().mapToObj((charCode) -> {
         return predicate.test((char)charCode) ? Character.toString((char)charCode) : "_";
      }).collect(Collectors.joining());
   }

   public static CachedMapper cachedMapper(Function mapper) {
      return new CachedMapper(mapper);
   }

   public static Function memoize(final Function function) {
      return new Function() {
         private final Map cache = new ConcurrentHashMap();

         public Object apply(Object object) {
            return this.cache.computeIfAbsent(object, function);
         }

         public String toString() {
            Function var10000 = function;
            return "memoize/1[function=" + var10000 + ", size=" + this.cache.size() + "]";
         }
      };
   }

   public static BiFunction memoize(final BiFunction biFunction) {
      return new BiFunction() {
         private final Map cache = new ConcurrentHashMap();

         public Object apply(Object a, Object b) {
            return this.cache.computeIfAbsent(com.mojang.datafixers.util.Pair.of(a, b), (pair) -> {
               return biFunction.apply(pair.getFirst(), pair.getSecond());
            });
         }

         public String toString() {
            BiFunction var10000 = biFunction;
            return "memoize/2[function=" + var10000 + ", size=" + this.cache.size() + "]";
         }
      };
   }

   public static List copyShuffled(Stream stream, Random random) {
      ObjectArrayList objectArrayList = (ObjectArrayList)stream.collect(ObjectArrayList.toList());
      shuffle(objectArrayList, random);
      return objectArrayList;
   }

   public static IntArrayList shuffle(IntStream stream, Random random) {
      IntArrayList intArrayList = IntArrayList.wrap(stream.toArray());
      int i = intArrayList.size();

      for(int j = i; j > 1; --j) {
         int k = random.nextInt(j);
         intArrayList.set(j - 1, intArrayList.set(k, intArrayList.getInt(j - 1)));
      }

      return intArrayList;
   }

   public static List copyShuffled(Object[] array, Random random) {
      ObjectArrayList objectArrayList = new ObjectArrayList(array);
      shuffle(objectArrayList, random);
      return objectArrayList;
   }

   public static List copyShuffled(ObjectArrayList list, Random random) {
      ObjectArrayList objectArrayList2 = new ObjectArrayList(list);
      shuffle(objectArrayList2, random);
      return objectArrayList2;
   }

   public static void shuffle(ObjectArrayList list, Random random) {
      int i = list.size();

      for(int j = i; j > 1; --j) {
         int k = random.nextInt(j);
         list.set(j - 1, list.set(k, list.get(j - 1)));
      }

   }

   public static CompletableFuture waitAndApply(Function resultFactory) {
      return (CompletableFuture)waitAndApply(resultFactory, CompletableFuture::isDone);
   }

   public static Object waitAndApply(Function resultFactory, Predicate donePredicate) {
      BlockingQueue blockingQueue = new LinkedBlockingQueue();
      Objects.requireNonNull(blockingQueue);
      Object object = resultFactory.apply(blockingQueue::add);

      while(!donePredicate.test(object)) {
         try {
            Runnable runnable = (Runnable)blockingQueue.poll(100L, TimeUnit.MILLISECONDS);
            if (runnable != null) {
               runnable.run();
            }
         } catch (InterruptedException var5) {
            LOGGER.warn("Interrupted wait");
            break;
         }
      }

      int i = blockingQueue.size();
      if (i > 0) {
         LOGGER.warn("Tasks left in queue: {}", i);
      }

      return object;
   }

   public static ToIntFunction lastIndexGetter(List values) {
      return lastIndexGetter(values, Object2IntOpenHashMap::new);
   }

   public static ToIntFunction lastIndexGetter(List values, IntFunction mapCreator) {
      Object2IntMap object2IntMap = (Object2IntMap)mapCreator.apply(values.size());

      for(int i = 0; i < values.size(); ++i) {
         object2IntMap.put(values.get(i), i);
      }

      return object2IntMap;
   }

   public static Object getResult(DataResult result, Function exceptionGetter) throws Exception {
      Optional optional = result.error();
      if (optional.isPresent()) {
         throw (Exception)exceptionGetter.apply(((DataResult.PartialResult)optional.get()).message());
      } else {
         return result.result().orElseThrow();
      }
   }

   static {
      DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
      nanoTimeSupplier = System::nanoTime;
      TICKER = new Ticker() {
         public long read() {
            return Util.nanoTimeSupplier.getAsLong();
         }
      };
      NIL_UUID = new UUID(0L, 0L);
      JAR_FILE_SYSTEM_PROVIDER = (FileSystemProvider)FileSystemProvider.installedProviders().stream().filter((fileSystemProvider) -> {
         return fileSystemProvider.getScheme().equalsIgnoreCase("jar");
      }).findFirst().orElseThrow(() -> {
         return new IllegalStateException("No jar file system provider found");
      });
      missingBreakpointHandler = (message) -> {
      };
   }

   public static enum OperatingSystem {
      LINUX("linux"),
      SOLARIS("solaris"),
      WINDOWS("windows") {
         protected String[] getURLOpenCommand(URL url) {
            return new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()};
         }
      },
      OSX("mac") {
         protected String[] getURLOpenCommand(URL url) {
            return new String[]{"open", url.toString()};
         }
      },
      UNKNOWN("unknown");

      private final String name;

      OperatingSystem(String name) {
         this.name = name;
      }

      public void open(URL url) {
         try {
            Process process = (Process)AccessController.doPrivileged(() -> {
               return Runtime.getRuntime().exec(this.getURLOpenCommand(url));
            });
            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
         } catch (IOException | PrivilegedActionException var3) {
            Util.LOGGER.error("Couldn't open url '{}'", url, var3);
         }

      }

      public void open(URI uri) {
         try {
            this.open(uri.toURL());
         } catch (MalformedURLException var3) {
            Util.LOGGER.error("Couldn't open uri '{}'", uri, var3);
         }

      }

      public void open(File file) {
         try {
            this.open(file.toURI().toURL());
         } catch (MalformedURLException var3) {
            Util.LOGGER.error("Couldn't open file '{}'", file, var3);
         }

      }

      protected String[] getURLOpenCommand(URL url) {
         String string = url.toString();
         if ("file".equals(url.getProtocol())) {
            string = string.replace("file:", "file://");
         }

         return new String[]{"xdg-open", string};
      }

      public void open(String uri) {
         try {
            this.open((new URI(uri)).toURL());
         } catch (MalformedURLException | IllegalArgumentException | URISyntaxException var3) {
            Util.LOGGER.error("Couldn't open uri '{}'", uri, var3);
         }

      }

      public String getName() {
         return this.name;
      }

      // $FF: synthetic method
      private static OperatingSystem[] method_36579() {
         return new OperatingSystem[]{LINUX, SOLARIS, WINDOWS, OSX, UNKNOWN};
      }
   }

   static enum IdentityHashStrategy implements Hash.Strategy {
      INSTANCE;

      public int hashCode(Object o) {
         return System.identityHashCode(o);
      }

      public boolean equals(Object o, Object o2) {
         return o == o2;
      }

      // $FF: synthetic method
      private static IdentityHashStrategy[] method_36578() {
         return new IdentityHashStrategy[]{INSTANCE};
      }
   }
}
