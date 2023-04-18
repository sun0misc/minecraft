package net.minecraft.world.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.EmptyPaletteStorage;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.thread.LockHelper;
import org.jetbrains.annotations.Nullable;

public class PalettedContainer implements PaletteResizeListener, ReadableContainer {
   private static final int field_34557 = 0;
   private final PaletteResizeListener dummyListener = (newSize, added) -> {
      return 0;
   };
   private final IndexedIterable idList;
   private volatile Data data;
   private final PaletteProvider paletteProvider;
   private final LockHelper lockHelper = new LockHelper("PalettedContainer");

   public void lock() {
      this.lockHelper.lock();
   }

   public void unlock() {
      this.lockHelper.unlock();
   }

   public static Codec createPalettedContainerCodec(IndexedIterable idList, Codec entryCodec, PaletteProvider paletteProvider, Object defaultValue) {
      ReadableContainer.Reader lv = PalettedContainer::read;
      return createCodec(idList, entryCodec, paletteProvider, defaultValue, lv);
   }

   public static Codec createReadableContainerCodec(IndexedIterable idList, Codec entryCodec, PaletteProvider paletteProvider, Object defaultValue) {
      ReadableContainer.Reader lv = (idListx, paletteProviderx, serialized) -> {
         return read(idListx, paletteProviderx, serialized).map((result) -> {
            return result;
         });
      };
      return createCodec(idList, entryCodec, paletteProvider, defaultValue, lv);
   }

   private static Codec createCodec(IndexedIterable idList, Codec entryCodec, PaletteProvider provider, Object defaultValue, ReadableContainer.Reader reader) {
      return RecordCodecBuilder.create((instance) -> {
         return instance.group(entryCodec.mapResult(Codecs.orElsePartial(defaultValue)).listOf().fieldOf("palette").forGetter(ReadableContainer.Serialized::paletteEntries), Codec.LONG_STREAM.optionalFieldOf("data").forGetter(ReadableContainer.Serialized::storage)).apply(instance, ReadableContainer.Serialized::new);
      }).comapFlatMap((serialized) -> {
         return reader.read(idList, provider, serialized);
      }, (container) -> {
         return container.serialize(idList, provider);
      });
   }

   public PalettedContainer(IndexedIterable idList, PaletteProvider paletteProvider, DataProvider dataProvider, PaletteStorage storage, List paletteEntries) {
      this.idList = idList;
      this.paletteProvider = paletteProvider;
      this.data = new Data(dataProvider, storage, dataProvider.factory().create(dataProvider.bits(), idList, this, paletteEntries));
   }

   private PalettedContainer(IndexedIterable idList, PaletteProvider paletteProvider, Data data) {
      this.idList = idList;
      this.paletteProvider = paletteProvider;
      this.data = data;
   }

   public PalettedContainer(IndexedIterable idList, Object object, PaletteProvider paletteProvider) {
      this.paletteProvider = paletteProvider;
      this.idList = idList;
      this.data = this.getCompatibleData((Data)null, 0);
      this.data.palette.index(object);
   }

   private Data getCompatibleData(@Nullable Data previousData, int bits) {
      DataProvider lv = this.paletteProvider.createDataProvider(this.idList, bits);
      return previousData != null && lv.equals(previousData.configuration()) ? previousData : lv.createData(this.idList, this, this.paletteProvider.getContainerSize());
   }

   public int onResize(int i, Object object) {
      Data lv = this.data;
      Data lv2 = this.getCompatibleData(lv, i);
      lv2.importFrom(lv.palette, lv.storage);
      this.data = lv2;
      return lv2.palette.index(object);
   }

   public Object swap(int x, int y, int z, Object value) {
      this.lock();

      Object var5;
      try {
         var5 = this.swap(this.paletteProvider.computeIndex(x, y, z), value);
      } finally {
         this.unlock();
      }

      return var5;
   }

   public Object swapUnsafe(int x, int y, int z, Object value) {
      return this.swap(this.paletteProvider.computeIndex(x, y, z), value);
   }

   private Object swap(int index, Object value) {
      int j = this.data.palette.index(value);
      int k = this.data.storage.swap(index, j);
      return this.data.palette.get(k);
   }

   public void set(int x, int y, int z, Object value) {
      this.lock();

      try {
         this.set(this.paletteProvider.computeIndex(x, y, z), value);
      } finally {
         this.unlock();
      }

   }

   private void set(int index, Object value) {
      int j = this.data.palette.index(value);
      this.data.storage.set(index, j);
   }

   public Object get(int x, int y, int z) {
      return this.get(this.paletteProvider.computeIndex(x, y, z));
   }

   protected Object get(int index) {
      Data lv = this.data;
      return lv.palette.get(lv.storage.get(index));
   }

   public void forEachValue(Consumer action) {
      Palette lv = this.data.palette();
      IntSet intSet = new IntArraySet();
      PaletteStorage var10000 = this.data.storage;
      Objects.requireNonNull(intSet);
      var10000.forEach(intSet::add);
      intSet.forEach((id) -> {
         action.accept(lv.get(id));
      });
   }

   public void readPacket(PacketByteBuf buf) {
      this.lock();

      try {
         int i = buf.readByte();
         Data lv = this.getCompatibleData(this.data, i);
         lv.palette.readPacket(buf);
         buf.readLongArray(lv.storage.getData());
         this.data = lv;
      } finally {
         this.unlock();
      }

   }

   public void writePacket(PacketByteBuf buf) {
      this.lock();

      try {
         this.data.writePacket(buf);
      } finally {
         this.unlock();
      }

   }

   private static DataResult read(IndexedIterable idList, PaletteProvider paletteProvider, ReadableContainer.Serialized serialized) {
      List list = serialized.paletteEntries();
      int i = paletteProvider.getContainerSize();
      int j = paletteProvider.getBits(idList, list.size());
      DataProvider lv = paletteProvider.createDataProvider(idList, j);
      Object lv2;
      if (j == 0) {
         lv2 = new EmptyPaletteStorage(i);
      } else {
         Optional optional = serialized.storage();
         if (optional.isEmpty()) {
            return DataResult.error(() -> {
               return "Missing values for non-zero storage";
            });
         }

         long[] ls = ((LongStream)optional.get()).toArray();

         try {
            if (lv.factory() == PalettedContainer.PaletteProvider.ID_LIST) {
               Palette lv3 = new BiMapPalette(idList, j, (id, value) -> {
                  return 0;
               }, list);
               PackedIntegerArray lv4 = new PackedIntegerArray(j, i, ls);
               int[] is = new int[i];
               lv4.method_39892(is);
               applyEach(is, (id) -> {
                  return idList.getRawId(lv3.get(id));
               });
               lv2 = new PackedIntegerArray(lv.bits(), i, is);
            } else {
               lv2 = new PackedIntegerArray(lv.bits(), i, ls);
            }
         } catch (PackedIntegerArray.InvalidLengthException var13) {
            return DataResult.error(() -> {
               return "Failed to read PalettedContainer: " + var13.getMessage();
            });
         }
      }

      return DataResult.success(new PalettedContainer(idList, paletteProvider, lv, (PaletteStorage)lv2, list));
   }

   public ReadableContainer.Serialized serialize(IndexedIterable idList, PaletteProvider paletteProvider) {
      this.lock();

      ReadableContainer.Serialized var12;
      try {
         BiMapPalette lv = new BiMapPalette(idList, this.data.storage.getElementBits(), this.dummyListener);
         int i = paletteProvider.getContainerSize();
         int[] is = new int[i];
         this.data.storage.method_39892(is);
         applyEach(is, (id) -> {
            return lv.index(this.data.palette.get(id));
         });
         int j = paletteProvider.getBits(idList, lv.getSize());
         Optional optional;
         if (j != 0) {
            PackedIntegerArray lv2 = new PackedIntegerArray(j, i, is);
            optional = Optional.of(Arrays.stream(lv2.getData()));
         } else {
            optional = Optional.empty();
         }

         var12 = new ReadableContainer.Serialized(lv.getElements(), optional);
      } finally {
         this.unlock();
      }

      return var12;
   }

   private static void applyEach(int[] is, IntUnaryOperator applier) {
      int i = -1;
      int j = -1;

      for(int k = 0; k < is.length; ++k) {
         int l = is[k];
         if (l != i) {
            i = l;
            j = applier.applyAsInt(l);
         }

         is[k] = j;
      }

   }

   public int getPacketSize() {
      return this.data.getPacketSize();
   }

   public boolean hasAny(Predicate predicate) {
      return this.data.palette.hasAny(predicate);
   }

   public PalettedContainer copy() {
      return new PalettedContainer(this.idList, this.paletteProvider, this.data.copy());
   }

   public PalettedContainer slice() {
      return new PalettedContainer(this.idList, this.data.palette.get(0), this.paletteProvider);
   }

   public void count(Counter counter) {
      if (this.data.palette.getSize() == 1) {
         counter.accept(this.data.palette.get(0), this.data.storage.getSize());
      } else {
         Int2IntOpenHashMap int2IntOpenHashMap = new Int2IntOpenHashMap();
         this.data.storage.forEach((key) -> {
            int2IntOpenHashMap.addTo(key, 1);
         });
         int2IntOpenHashMap.int2IntEntrySet().forEach((entry) -> {
            counter.accept(this.data.palette.get(entry.getIntKey()), entry.getIntValue());
         });
      }
   }

   public abstract static class PaletteProvider {
      public static final Palette.Factory SINGULAR = SingularPalette::create;
      public static final Palette.Factory ARRAY = ArrayPalette::create;
      public static final Palette.Factory BI_MAP = BiMapPalette::create;
      static final Palette.Factory ID_LIST = IdListPalette::create;
      public static final PaletteProvider BLOCK_STATE = new PaletteProvider(4) {
         public DataProvider createDataProvider(IndexedIterable idList, int bits) {
            DataProvider var10000;
            switch (bits) {
               case 0:
                  var10000 = new DataProvider(SINGULAR, bits);
                  break;
               case 1:
               case 2:
               case 3:
               case 4:
                  var10000 = new DataProvider(ARRAY, 4);
                  break;
               case 5:
               case 6:
               case 7:
               case 8:
                  var10000 = new DataProvider(BI_MAP, bits);
                  break;
               default:
                  var10000 = new DataProvider(PalettedContainer.PaletteProvider.ID_LIST, MathHelper.ceilLog2(idList.size()));
            }

            return var10000;
         }
      };
      public static final PaletteProvider BIOME = new PaletteProvider(2) {
         public DataProvider createDataProvider(IndexedIterable idList, int bits) {
            DataProvider var10000;
            switch (bits) {
               case 0:
                  var10000 = new DataProvider(SINGULAR, bits);
                  break;
               case 1:
               case 2:
               case 3:
                  var10000 = new DataProvider(ARRAY, bits);
                  break;
               default:
                  var10000 = new DataProvider(PalettedContainer.PaletteProvider.ID_LIST, MathHelper.ceilLog2(idList.size()));
            }

            return var10000;
         }
      };
      private final int edgeBits;

      PaletteProvider(int edgeBits) {
         this.edgeBits = edgeBits;
      }

      public int getContainerSize() {
         return 1 << this.edgeBits * 3;
      }

      public int computeIndex(int x, int y, int z) {
         return (y << this.edgeBits | z) << this.edgeBits | x;
      }

      public abstract DataProvider createDataProvider(IndexedIterable idList, int bits);

      int getBits(IndexedIterable idList, int size) {
         int j = MathHelper.ceilLog2(size);
         DataProvider lv = this.createDataProvider(idList, j);
         return lv.factory() == ID_LIST ? j : lv.bits();
      }
   }

   static record Data(DataProvider configuration, PaletteStorage storage, Palette palette) {
      final PaletteStorage storage;
      final Palette palette;

      Data(DataProvider configuration, PaletteStorage storage, Palette palette) {
         this.configuration = configuration;
         this.storage = storage;
         this.palette = palette;
      }

      public void importFrom(Palette palette, PaletteStorage storage) {
         for(int i = 0; i < storage.getSize(); ++i) {
            Object object = palette.get(storage.get(i));
            this.storage.set(i, this.palette.index(object));
         }

      }

      public int getPacketSize() {
         return 1 + this.palette.getPacketSize() + PacketByteBuf.getVarIntLength(this.storage.getSize()) + this.storage.getData().length * 8;
      }

      public void writePacket(PacketByteBuf buf) {
         buf.writeByte(this.storage.getElementBits());
         this.palette.writePacket(buf);
         buf.writeLongArray(this.storage.getData());
      }

      public Data copy() {
         return new Data(this.configuration, this.storage.copy(), this.palette.copy());
      }

      public DataProvider configuration() {
         return this.configuration;
      }

      public PaletteStorage storage() {
         return this.storage;
      }

      public Palette palette() {
         return this.palette;
      }
   }

   private static record DataProvider(Palette.Factory factory, int bits) {
      DataProvider(Palette.Factory arg, int i) {
         this.factory = arg;
         this.bits = i;
      }

      public Data createData(IndexedIterable idList, PaletteResizeListener listener, int size) {
         PaletteStorage lv = this.bits == 0 ? new EmptyPaletteStorage(size) : new PackedIntegerArray(this.bits, size);
         Palette lv2 = this.factory.create(this.bits, idList, listener, List.of());
         return new Data(this, (PaletteStorage)lv, lv2);
      }

      public Palette.Factory factory() {
         return this.factory;
      }

      public int bits() {
         return this.bits;
      }
   }

   @FunctionalInterface
   public interface Counter {
      void accept(Object object, int count);
   }
}
