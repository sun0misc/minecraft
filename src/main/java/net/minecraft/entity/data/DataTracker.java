package net.minecraft.entity.data;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class DataTracker {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Object2IntMap TRACKED_ENTITIES = new Object2IntOpenHashMap();
   private static final int MAX_DATA_VALUE_ID = 254;
   private final Entity trackedEntity;
   private final Int2ObjectMap entries = new Int2ObjectOpenHashMap();
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private boolean dirty;

   public DataTracker(Entity trackedEntity) {
      this.trackedEntity = trackedEntity;
   }

   public static TrackedData registerData(Class entityClass, TrackedDataHandler dataHandler) {
      if (LOGGER.isDebugEnabled()) {
         try {
            Class class2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
            if (!class2.equals(entityClass)) {
               LOGGER.debug("defineId called for: {} from {}", new Object[]{entityClass, class2, new RuntimeException()});
            }
         } catch (ClassNotFoundException var5) {
         }
      }

      int i;
      if (TRACKED_ENTITIES.containsKey(entityClass)) {
         i = TRACKED_ENTITIES.getInt(entityClass) + 1;
      } else {
         int j = 0;
         Class class3 = entityClass;

         while(class3 != Entity.class) {
            class3 = class3.getSuperclass();
            if (TRACKED_ENTITIES.containsKey(class3)) {
               j = TRACKED_ENTITIES.getInt(class3) + 1;
               break;
            }
         }

         i = j;
      }

      if (i > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
      } else {
         TRACKED_ENTITIES.put(entityClass, i);
         return dataHandler.create(i);
      }
   }

   public void startTracking(TrackedData key, Object initialValue) {
      int i = key.getId();
      if (i > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
      } else if (this.entries.containsKey(i)) {
         throw new IllegalArgumentException("Duplicate id value for " + i + "!");
      } else if (TrackedDataHandlerRegistry.getId(key.getType()) < 0) {
         TrackedDataHandler var10002 = key.getType();
         throw new IllegalArgumentException("Unregistered serializer " + var10002 + " for " + i + "!");
      } else {
         this.addTrackedData(key, initialValue);
      }
   }

   private void addTrackedData(TrackedData key, Object value) {
      Entry lv = new Entry(key, value);
      this.lock.writeLock().lock();
      this.entries.put(key.getId(), lv);
      this.lock.writeLock().unlock();
   }

   private Entry getEntry(TrackedData key) {
      this.lock.readLock().lock();

      Entry lv;
      try {
         lv = (Entry)this.entries.get(key.getId());
      } catch (Throwable var9) {
         CrashReport lv2 = CrashReport.create(var9, "Getting synched entity data");
         CrashReportSection lv3 = lv2.addElement("Synched entity data");
         lv3.add("Data ID", (Object)key);
         throw new CrashException(lv2);
      } finally {
         this.lock.readLock().unlock();
      }

      return lv;
   }

   public Object get(TrackedData data) {
      return this.getEntry(data).get();
   }

   public void set(TrackedData key, Object value) {
      this.set(key, value, false);
   }

   public void set(TrackedData key, Object value, boolean force) {
      Entry lv = this.getEntry(key);
      if (force || ObjectUtils.notEqual(value, lv.get())) {
         lv.set(value);
         this.trackedEntity.onTrackedDataSet(key);
         lv.setDirty(true);
         this.dirty = true;
      }

   }

   public boolean isDirty() {
      return this.dirty;
   }

   @Nullable
   public List getDirtyEntries() {
      List list = null;
      if (this.dirty) {
         this.lock.readLock().lock();
         ObjectIterator var2 = this.entries.values().iterator();

         while(var2.hasNext()) {
            Entry lv = (Entry)var2.next();
            if (lv.isDirty()) {
               lv.setDirty(false);
               if (list == null) {
                  list = new ArrayList();
               }

               list.add(lv.toSerialized());
            }
         }

         this.lock.readLock().unlock();
      }

      this.dirty = false;
      return list;
   }

   @Nullable
   public List getChangedEntries() {
      List list = null;
      this.lock.readLock().lock();
      ObjectIterator var2 = this.entries.values().iterator();

      while(var2.hasNext()) {
         Entry lv = (Entry)var2.next();
         if (!lv.isUnchanged()) {
            if (list == null) {
               list = new ArrayList();
            }

            list.add(lv.toSerialized());
         }
      }

      this.lock.readLock().unlock();
      return list;
   }

   public void writeUpdatedEntries(List entries) {
      this.lock.writeLock().lock();

      try {
         Iterator var2 = entries.iterator();

         while(var2.hasNext()) {
            SerializedEntry lv = (SerializedEntry)var2.next();
            Entry lv2 = (Entry)this.entries.get(lv.id);
            if (lv2 != null) {
               this.copyToFrom(lv2, lv);
               this.trackedEntity.onTrackedDataSet(lv2.getData());
            }
         }
      } finally {
         this.lock.writeLock().unlock();
      }

      this.trackedEntity.onDataTrackerUpdate(entries);
   }

   private void copyToFrom(Entry to, SerializedEntry from) {
      if (!Objects.equals(from.handler(), to.data.getType())) {
         throw new IllegalStateException(String.format(Locale.ROOT, "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", to.data.getId(), this.trackedEntity, to.value, to.value.getClass(), from.value, from.value.getClass()));
      } else {
         to.set(from.value);
      }
   }

   public boolean isEmpty() {
      return this.entries.isEmpty();
   }

   public static class Entry {
      final TrackedData data;
      Object value;
      private final Object initialValue;
      private boolean dirty;

      public Entry(TrackedData data, Object value) {
         this.data = data;
         this.initialValue = value;
         this.value = value;
      }

      public TrackedData getData() {
         return this.data;
      }

      public void set(Object value) {
         this.value = value;
      }

      public Object get() {
         return this.value;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public void setDirty(boolean dirty) {
         this.dirty = dirty;
      }

      public boolean isUnchanged() {
         return this.initialValue.equals(this.value);
      }

      public SerializedEntry toSerialized() {
         return DataTracker.SerializedEntry.of(this.data, this.value);
      }
   }

   public static record SerializedEntry(int id, TrackedDataHandler handler, Object value) {
      final int id;
      final Object value;

      public SerializedEntry(int i, TrackedDataHandler arg, Object object) {
         this.id = i;
         this.handler = arg;
         this.value = object;
      }

      public static SerializedEntry of(TrackedData data, Object value) {
         TrackedDataHandler lv = data.getType();
         return new SerializedEntry(data.getId(), lv, lv.copy(value));
      }

      public void write(PacketByteBuf buf) {
         int i = TrackedDataHandlerRegistry.getId(this.handler);
         if (i < 0) {
            throw new EncoderException("Unknown serializer type " + this.handler);
         } else {
            buf.writeByte(this.id);
            buf.writeVarInt(i);
            this.handler.write(buf, this.value);
         }
      }

      public static SerializedEntry fromBuf(PacketByteBuf buf, int id) {
         int j = buf.readVarInt();
         TrackedDataHandler lv = TrackedDataHandlerRegistry.get(j);
         if (lv == null) {
            throw new DecoderException("Unknown serializer type " + j);
         } else {
            return fromBuf(buf, id, lv);
         }
      }

      private static SerializedEntry fromBuf(PacketByteBuf buf, int id, TrackedDataHandler handler) {
         return new SerializedEntry(id, handler, handler.read(buf));
      }

      public int id() {
         return this.id;
      }

      public TrackedDataHandler handler() {
         return this.handler;
      }

      public Object value() {
         return this.value;
      }
   }
}
