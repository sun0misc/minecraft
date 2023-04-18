package net.minecraft.nbt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

public class NbtOps implements DynamicOps {
   public static final NbtOps INSTANCE = new NbtOps();
   private static final String MARKER_KEY = "";

   protected NbtOps() {
   }

   public NbtElement empty() {
      return NbtEnd.INSTANCE;
   }

   public Object convertTo(DynamicOps dynamicOps, NbtElement arg) {
      switch (arg.getType()) {
         case 0:
            return dynamicOps.empty();
         case 1:
            return dynamicOps.createByte(((AbstractNbtNumber)arg).byteValue());
         case 2:
            return dynamicOps.createShort(((AbstractNbtNumber)arg).shortValue());
         case 3:
            return dynamicOps.createInt(((AbstractNbtNumber)arg).intValue());
         case 4:
            return dynamicOps.createLong(((AbstractNbtNumber)arg).longValue());
         case 5:
            return dynamicOps.createFloat(((AbstractNbtNumber)arg).floatValue());
         case 6:
            return dynamicOps.createDouble(((AbstractNbtNumber)arg).doubleValue());
         case 7:
            return dynamicOps.createByteList(ByteBuffer.wrap(((NbtByteArray)arg).getByteArray()));
         case 8:
            return dynamicOps.createString(arg.asString());
         case 9:
            return this.convertList(dynamicOps, arg);
         case 10:
            return this.convertMap(dynamicOps, arg);
         case 11:
            return dynamicOps.createIntList(Arrays.stream(((NbtIntArray)arg).getIntArray()));
         case 12:
            return dynamicOps.createLongList(Arrays.stream(((NbtLongArray)arg).getLongArray()));
         default:
            throw new IllegalStateException("Unknown tag type: " + arg);
      }
   }

   public DataResult getNumberValue(NbtElement arg) {
      if (arg instanceof AbstractNbtNumber lv) {
         return DataResult.success(lv.numberValue());
      } else {
         return DataResult.error(() -> {
            return "Not a number";
         });
      }
   }

   public NbtElement createNumeric(Number number) {
      return NbtDouble.of(number.doubleValue());
   }

   public NbtElement createByte(byte b) {
      return NbtByte.of(b);
   }

   public NbtElement createShort(short s) {
      return NbtShort.of(s);
   }

   public NbtElement createInt(int i) {
      return NbtInt.of(i);
   }

   public NbtElement createLong(long l) {
      return NbtLong.of(l);
   }

   public NbtElement createFloat(float f) {
      return NbtFloat.of(f);
   }

   public NbtElement createDouble(double d) {
      return NbtDouble.of(d);
   }

   public NbtElement createBoolean(boolean bl) {
      return NbtByte.of(bl);
   }

   public DataResult getStringValue(NbtElement arg) {
      if (arg instanceof NbtString lv) {
         return DataResult.success(lv.asString());
      } else {
         return DataResult.error(() -> {
            return "Not a string";
         });
      }
   }

   public NbtElement createString(String string) {
      return NbtString.of(string);
   }

   public DataResult mergeToList(NbtElement arg, NbtElement arg2) {
      return (DataResult)createMerger(arg).map((merger) -> {
         return DataResult.success(merger.merge(arg2).getResult());
      }).orElseGet(() -> {
         return DataResult.error(() -> {
            return "mergeToList called with not a list: " + arg;
         }, arg);
      });
   }

   public DataResult mergeToList(NbtElement arg, List list) {
      return (DataResult)createMerger(arg).map((merger) -> {
         return DataResult.success(merger.merge((Iterable)list).getResult());
      }).orElseGet(() -> {
         return DataResult.error(() -> {
            return "mergeToList called with not a list: " + arg;
         }, arg);
      });
   }

   public DataResult mergeToMap(NbtElement arg, NbtElement arg2, NbtElement arg3) {
      if (!(arg instanceof NbtCompound) && !(arg instanceof NbtEnd)) {
         return DataResult.error(() -> {
            return "mergeToMap called with not a map: " + arg;
         }, arg);
      } else if (!(arg2 instanceof NbtString)) {
         return DataResult.error(() -> {
            return "key is not a string: " + arg2;
         }, arg);
      } else {
         NbtCompound lv = new NbtCompound();
         if (arg instanceof NbtCompound) {
            NbtCompound lv2 = (NbtCompound)arg;
            lv2.getKeys().forEach((key) -> {
               lv.put(key, lv2.get(key));
            });
         }

         lv.put(arg2.asString(), arg3);
         return DataResult.success(lv);
      }
   }

   public DataResult mergeToMap(NbtElement arg, MapLike mapLike) {
      if (!(arg instanceof NbtCompound) && !(arg instanceof NbtEnd)) {
         return DataResult.error(() -> {
            return "mergeToMap called with not a map: " + arg;
         }, arg);
      } else {
         NbtCompound lv = new NbtCompound();
         if (arg instanceof NbtCompound) {
            NbtCompound lv2 = (NbtCompound)arg;
            lv2.getKeys().forEach((key) -> {
               lv.put(key, lv2.get(key));
            });
         }

         List list = Lists.newArrayList();
         mapLike.entries().forEach((pair) -> {
            NbtElement lvx = (NbtElement)pair.getFirst();
            if (!(lvx instanceof NbtString)) {
               list.add(lvx);
            } else {
               lv.put(lvx.asString(), (NbtElement)pair.getSecond());
            }
         });
         return !list.isEmpty() ? DataResult.error(() -> {
            return "some keys are not strings: " + list;
         }, lv) : DataResult.success(lv);
      }
   }

   public DataResult getMapValues(NbtElement arg) {
      if (arg instanceof NbtCompound lv) {
         return DataResult.success(lv.getKeys().stream().map((key) -> {
            return Pair.of(this.createString(key), lv.get(key));
         }));
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + arg;
         });
      }
   }

   public DataResult getMapEntries(NbtElement arg) {
      if (arg instanceof NbtCompound lv) {
         return DataResult.success((entryConsumer) -> {
            lv.getKeys().forEach((key) -> {
               entryConsumer.accept(this.createString(key), lv.get(key));
            });
         });
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + arg;
         });
      }
   }

   public DataResult getMap(NbtElement arg) {
      if (arg instanceof final NbtCompound lv) {
         return DataResult.success(new MapLike() {
            @Nullable
            public NbtElement get(NbtElement arg) {
               return lv.get(arg.asString());
            }

            @Nullable
            public NbtElement get(String string) {
               return lv.get(string);
            }

            public Stream entries() {
               return lv.getKeys().stream().map((key) -> {
                  return Pair.of(NbtOps.this.createString(key), lv.get(key));
               });
            }

            public String toString() {
               return "MapLike[" + lv + "]";
            }

            // $FF: synthetic method
            @Nullable
            public Object get(String key) {
               return this.get(key);
            }

            // $FF: synthetic method
            @Nullable
            public Object get(Object nbt) {
               return this.get((NbtElement)nbt);
            }
         });
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + arg;
         });
      }
   }

   public NbtElement createMap(Stream stream) {
      NbtCompound lv = new NbtCompound();
      stream.forEach((entry) -> {
         lv.put(((NbtElement)entry.getFirst()).asString(), (NbtElement)entry.getSecond());
      });
      return lv;
   }

   private static NbtElement unpackMarker(NbtCompound nbt) {
      if (nbt.getSize() == 1) {
         NbtElement lv = nbt.get("");
         if (lv != null) {
            return lv;
         }
      }

      return nbt;
   }

   public DataResult getStream(NbtElement arg) {
      if (arg instanceof NbtList lv) {
         return lv.getHeldType() == NbtElement.COMPOUND_TYPE ? DataResult.success(lv.stream().map((nbt) -> {
            return unpackMarker((NbtCompound)nbt);
         })) : DataResult.success(lv.stream());
      } else if (arg instanceof AbstractNbtList lv2) {
         return DataResult.success(lv2.stream().map((nbt) -> {
            return nbt;
         }));
      } else {
         return DataResult.error(() -> {
            return "Not a list";
         });
      }
   }

   public DataResult getList(NbtElement arg) {
      if (arg instanceof NbtList lv) {
         if (lv.getHeldType() == NbtElement.COMPOUND_TYPE) {
            return DataResult.success((consumer) -> {
               lv.forEach((nbt) -> {
                  consumer.accept(unpackMarker((NbtCompound)nbt));
               });
            });
         } else {
            Objects.requireNonNull(lv);
            return DataResult.success(lv::forEach);
         }
      } else if (arg instanceof AbstractNbtList lv2) {
         Objects.requireNonNull(lv2);
         return DataResult.success(lv2::forEach);
      } else {
         return DataResult.error(() -> {
            return "Not a list: " + arg;
         });
      }
   }

   public DataResult getByteBuffer(NbtElement arg) {
      if (arg instanceof NbtByteArray lv) {
         return DataResult.success(ByteBuffer.wrap(lv.getByteArray()));
      } else {
         return super.getByteBuffer(arg);
      }
   }

   public NbtElement createByteList(ByteBuffer byteBuffer) {
      return new NbtByteArray(DataFixUtils.toArray(byteBuffer));
   }

   public DataResult getIntStream(NbtElement arg) {
      if (arg instanceof NbtIntArray lv) {
         return DataResult.success(Arrays.stream(lv.getIntArray()));
      } else {
         return super.getIntStream(arg);
      }
   }

   public NbtElement createIntList(IntStream intStream) {
      return new NbtIntArray(intStream.toArray());
   }

   public DataResult getLongStream(NbtElement arg) {
      if (arg instanceof NbtLongArray lv) {
         return DataResult.success(Arrays.stream(lv.getLongArray()));
      } else {
         return super.getLongStream(arg);
      }
   }

   public NbtElement createLongList(LongStream longStream) {
      return new NbtLongArray(longStream.toArray());
   }

   public NbtElement createList(Stream stream) {
      return NbtOps.BasicMerger.EMPTY.merge(stream).getResult();
   }

   public NbtElement remove(NbtElement arg, String string) {
      if (arg instanceof NbtCompound lv) {
         NbtCompound lv2 = new NbtCompound();
         lv.getKeys().stream().filter((k) -> {
            return !Objects.equals(k, string);
         }).forEach((k) -> {
            lv2.put(k, lv.get(k));
         });
         return lv2;
      } else {
         return arg;
      }
   }

   public String toString() {
      return "NBT";
   }

   public RecordBuilder mapBuilder() {
      return new MapBuilder();
   }

   private static Optional createMerger(NbtElement nbt) {
      if (nbt instanceof NbtEnd) {
         return Optional.of(NbtOps.BasicMerger.EMPTY);
      } else {
         if (nbt instanceof AbstractNbtList) {
            AbstractNbtList lv = (AbstractNbtList)nbt;
            if (lv.isEmpty()) {
               return Optional.of(NbtOps.BasicMerger.EMPTY);
            }

            if (lv instanceof NbtList) {
               NbtList lv2 = (NbtList)lv;
               Optional var10000;
               switch (lv2.getHeldType()) {
                  case 0:
                     var10000 = Optional.of(NbtOps.BasicMerger.EMPTY);
                     break;
                  case 10:
                     var10000 = Optional.of(new CompoundListMerger(lv2));
                     break;
                  default:
                     var10000 = Optional.of(new ListMerger(lv2));
               }

               return var10000;
            }

            if (lv instanceof NbtByteArray) {
               NbtByteArray lv3 = (NbtByteArray)lv;
               return Optional.of(new ByteArrayMerger(lv3.getByteArray()));
            }

            if (lv instanceof NbtIntArray) {
               NbtIntArray lv4 = (NbtIntArray)lv;
               return Optional.of(new IntArrayMerger(lv4.getIntArray()));
            }

            if (lv instanceof NbtLongArray) {
               NbtLongArray lv5 = (NbtLongArray)lv;
               return Optional.of(new LongArrayMerger(lv5.getLongArray()));
            }
         }

         return Optional.empty();
      }
   }

   // $FF: synthetic method
   public Object remove(Object element, String key) {
      return this.remove((NbtElement)element, key);
   }

   // $FF: synthetic method
   public Object createLongList(LongStream stream) {
      return this.createLongList(stream);
   }

   // $FF: synthetic method
   public DataResult getLongStream(Object element) {
      return this.getLongStream((NbtElement)element);
   }

   // $FF: synthetic method
   public Object createIntList(IntStream stream) {
      return this.createIntList(stream);
   }

   // $FF: synthetic method
   public DataResult getIntStream(Object element) {
      return this.getIntStream((NbtElement)element);
   }

   // $FF: synthetic method
   public Object createByteList(ByteBuffer buf) {
      return this.createByteList(buf);
   }

   // $FF: synthetic method
   public DataResult getByteBuffer(Object element) {
      return this.getByteBuffer((NbtElement)element);
   }

   // $FF: synthetic method
   public Object createList(Stream stream) {
      return this.createList(stream);
   }

   // $FF: synthetic method
   public DataResult getList(Object element) {
      return this.getList((NbtElement)element);
   }

   // $FF: synthetic method
   public DataResult getStream(Object element) {
      return this.getStream((NbtElement)element);
   }

   // $FF: synthetic method
   public DataResult getMap(Object element) {
      return this.getMap((NbtElement)element);
   }

   // $FF: synthetic method
   public Object createMap(Stream entries) {
      return this.createMap(entries);
   }

   // $FF: synthetic method
   public DataResult getMapEntries(Object element) {
      return this.getMapEntries((NbtElement)element);
   }

   // $FF: synthetic method
   public DataResult getMapValues(Object element) {
      return this.getMapValues((NbtElement)element);
   }

   // $FF: synthetic method
   public DataResult mergeToMap(Object element, MapLike map) {
      return this.mergeToMap((NbtElement)element, map);
   }

   // $FF: synthetic method
   public DataResult mergeToMap(Object map, Object key, Object value) {
      return this.mergeToMap((NbtElement)map, (NbtElement)key, (NbtElement)value);
   }

   // $FF: synthetic method
   public DataResult mergeToList(Object list, List values) {
      return this.mergeToList((NbtElement)list, values);
   }

   // $FF: synthetic method
   public DataResult mergeToList(Object list, Object value) {
      return this.mergeToList((NbtElement)list, (NbtElement)value);
   }

   // $FF: synthetic method
   public Object createString(String string) {
      return this.createString(string);
   }

   // $FF: synthetic method
   public DataResult getStringValue(Object element) {
      return this.getStringValue((NbtElement)element);
   }

   // $FF: synthetic method
   public Object createBoolean(boolean value) {
      return this.createBoolean(value);
   }

   // $FF: synthetic method
   public Object createDouble(double value) {
      return this.createDouble(value);
   }

   // $FF: synthetic method
   public Object createFloat(float value) {
      return this.createFloat(value);
   }

   // $FF: synthetic method
   public Object createLong(long value) {
      return this.createLong(value);
   }

   // $FF: synthetic method
   public Object createInt(int value) {
      return this.createInt(value);
   }

   // $FF: synthetic method
   public Object createShort(short value) {
      return this.createShort(value);
   }

   // $FF: synthetic method
   public Object createByte(byte value) {
      return this.createByte(value);
   }

   // $FF: synthetic method
   public Object createNumeric(Number value) {
      return this.createNumeric(value);
   }

   // $FF: synthetic method
   public DataResult getNumberValue(Object element) {
      return this.getNumberValue((NbtElement)element);
   }

   // $FF: synthetic method
   public Object convertTo(DynamicOps ops, Object element) {
      return this.convertTo(ops, (NbtElement)element);
   }

   // $FF: synthetic method
   public Object empty() {
      return this.empty();
   }

   private static class BasicMerger implements Merger {
      public static final BasicMerger EMPTY = new BasicMerger();

      public Merger merge(NbtElement nbt) {
         if (nbt instanceof NbtCompound lv) {
            return (new CompoundListMerger()).merge(lv);
         } else if (nbt instanceof NbtByte lv2) {
            return new ByteArrayMerger(lv2.byteValue());
         } else if (nbt instanceof NbtInt lv3) {
            return new IntArrayMerger(lv3.intValue());
         } else if (nbt instanceof NbtLong lv4) {
            return new LongArrayMerger(lv4.longValue());
         } else {
            return new ListMerger(nbt);
         }
      }

      public NbtElement getResult() {
         return new NbtList();
      }
   }

   private interface Merger {
      Merger merge(NbtElement nbt);

      default Merger merge(Iterable nbts) {
         Merger lv = this;

         NbtElement lv2;
         for(Iterator var3 = nbts.iterator(); var3.hasNext(); lv = lv.merge(lv2)) {
            lv2 = (NbtElement)var3.next();
         }

         return lv;
      }

      default Merger merge(Stream nbts) {
         Objects.requireNonNull(nbts);
         return this.merge(nbts::iterator);
      }

      NbtElement getResult();
   }

   class MapBuilder extends RecordBuilder.AbstractStringBuilder {
      protected MapBuilder() {
         super(NbtOps.this);
      }

      protected NbtCompound initBuilder() {
         return new NbtCompound();
      }

      protected NbtCompound append(String string, NbtElement arg, NbtCompound arg2) {
         arg2.put(string, arg);
         return arg2;
      }

      protected DataResult build(NbtCompound arg, NbtElement arg2) {
         if (arg2 != null && arg2 != NbtEnd.INSTANCE) {
            if (!(arg2 instanceof NbtCompound)) {
               return DataResult.error(() -> {
                  return "mergeToMap called with not a map: " + arg2;
               }, arg2);
            } else {
               NbtCompound lv = (NbtCompound)arg2;
               NbtCompound lv2 = new NbtCompound(Maps.newHashMap(lv.toMap()));
               Iterator var5 = arg.toMap().entrySet().iterator();

               while(var5.hasNext()) {
                  Map.Entry entry = (Map.Entry)var5.next();
                  lv2.put((String)entry.getKey(), (NbtElement)entry.getValue());
               }

               return DataResult.success(lv2);
            }
         } else {
            return DataResult.success(arg);
         }
      }

      // $FF: synthetic method
      protected Object append(String key, Object value, Object nbt) {
         return this.append(key, (NbtElement)value, (NbtCompound)nbt);
      }

      // $FF: synthetic method
      protected DataResult build(Object nbt, Object mergedValue) {
         return this.build((NbtCompound)nbt, (NbtElement)mergedValue);
      }

      // $FF: synthetic method
      protected Object initBuilder() {
         return this.initBuilder();
      }
   }

   private static class CompoundListMerger implements Merger {
      private final NbtList list = new NbtList();

      public CompoundListMerger() {
      }

      public CompoundListMerger(Collection nbts) {
         this.list.addAll(nbts);
      }

      public CompoundListMerger(IntArrayList list) {
         list.forEach((value) -> {
            this.list.add(createMarkerNbt(NbtInt.of(value)));
         });
      }

      public CompoundListMerger(ByteArrayList list) {
         list.forEach((value) -> {
            this.list.add(createMarkerNbt(NbtByte.of(value)));
         });
      }

      public CompoundListMerger(LongArrayList list) {
         list.forEach((value) -> {
            this.list.add(createMarkerNbt(NbtLong.of(value)));
         });
      }

      private static boolean isMarker(NbtCompound nbt) {
         return nbt.getSize() == 1 && nbt.contains("");
      }

      private static NbtElement makeMarker(NbtElement value) {
         if (value instanceof NbtCompound lv) {
            if (!isMarker(lv)) {
               return lv;
            }
         }

         return createMarkerNbt(value);
      }

      private static NbtCompound createMarkerNbt(NbtElement value) {
         NbtCompound lv = new NbtCompound();
         lv.put("", value);
         return lv;
      }

      public Merger merge(NbtElement nbt) {
         this.list.add(makeMarker(nbt));
         return this;
      }

      public NbtElement getResult() {
         return this.list;
      }
   }

   private static class ListMerger implements Merger {
      private final NbtList list = new NbtList();

      ListMerger(NbtElement nbt) {
         this.list.add(nbt);
      }

      ListMerger(NbtList nbt) {
         this.list.addAll(nbt);
      }

      public Merger merge(NbtElement nbt) {
         if (nbt.getType() != this.list.getHeldType()) {
            return (new CompoundListMerger()).merge(this.list).merge(nbt);
         } else {
            this.list.add(nbt);
            return this;
         }
      }

      public NbtElement getResult() {
         return this.list;
      }
   }

   static class ByteArrayMerger implements Merger {
      private final ByteArrayList list = new ByteArrayList();

      public ByteArrayMerger(byte value) {
         this.list.add(value);
      }

      public ByteArrayMerger(byte[] values) {
         this.list.addElements(0, values);
      }

      public Merger merge(NbtElement nbt) {
         if (nbt instanceof NbtByte lv) {
            this.list.add(lv.byteValue());
            return this;
         } else {
            return (new CompoundListMerger(this.list)).merge(nbt);
         }
      }

      public NbtElement getResult() {
         return new NbtByteArray(this.list.toByteArray());
      }
   }

   private static class IntArrayMerger implements Merger {
      private final IntArrayList list = new IntArrayList();

      public IntArrayMerger(int value) {
         this.list.add(value);
      }

      public IntArrayMerger(int[] values) {
         this.list.addElements(0, values);
      }

      public Merger merge(NbtElement nbt) {
         if (nbt instanceof NbtInt lv) {
            this.list.add(lv.intValue());
            return this;
         } else {
            return (new CompoundListMerger(this.list)).merge(nbt);
         }
      }

      public NbtElement getResult() {
         return new NbtIntArray(this.list.toIntArray());
      }
   }

   static class LongArrayMerger implements Merger {
      private final LongArrayList list = new LongArrayList();

      public LongArrayMerger(long value) {
         this.list.add(value);
      }

      public LongArrayMerger(long[] values) {
         this.list.addElements(0, values);
      }

      public Merger merge(NbtElement nbt) {
         if (nbt instanceof NbtLong lv) {
            this.list.add(lv.longValue());
            return this;
         } else {
            return (new CompoundListMerger(this.list)).merge(nbt);
         }
      }

      public NbtElement getResult() {
         return new NbtLongArray(this.list.toLongArray());
      }
   }
}
