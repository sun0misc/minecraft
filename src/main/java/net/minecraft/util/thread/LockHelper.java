package net.minecraft.util.thread;

import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LockHelper {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String name;
   private final Semaphore semaphore = new Semaphore(1);
   private final Lock lock = new ReentrantLock();
   @Nullable
   private volatile Thread thread;
   @Nullable
   private volatile CrashException crashException;

   public LockHelper(String name) {
      this.name = name;
   }

   public void lock() {
      boolean bl = false;

      try {
         this.lock.lock();
         if (!this.semaphore.tryAcquire()) {
            this.thread = Thread.currentThread();
            bl = true;
            this.lock.unlock();

            try {
               this.semaphore.acquire();
            } catch (InterruptedException var6) {
               Thread.currentThread().interrupt();
            }

            throw this.crashException;
         }
      } finally {
         if (!bl) {
            this.lock.unlock();
         }

      }

   }

   public void unlock() {
      try {
         this.lock.lock();
         Thread thread = this.thread;
         if (thread != null) {
            CrashException lv = crash(this.name, thread);
            this.crashException = lv;
            this.semaphore.release();
            throw lv;
         }

         this.semaphore.release();
      } finally {
         this.lock.unlock();
      }

   }

   public static CrashException crash(String message, @Nullable Thread thread) {
      String string2 = (String)Stream.of(Thread.currentThread(), thread).filter(Objects::nonNull).map(LockHelper::formatStackTraceForThread).collect(Collectors.joining("\n"));
      String string3 = "Accessing " + message + " from multiple threads";
      CrashReport lv = new CrashReport(string3, new IllegalStateException(string3));
      CrashReportSection lv2 = lv.addElement("Thread dumps");
      lv2.add("Thread dumps", (Object)string2);
      LOGGER.error("Thread dumps: \n" + string2);
      return new CrashException(lv);
   }

   private static String formatStackTraceForThread(Thread thread) {
      String var10000 = thread.getName();
      return var10000 + ": \n\tat " + (String)Arrays.stream(thread.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat "));
   }
}
