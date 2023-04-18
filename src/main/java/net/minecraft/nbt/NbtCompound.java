package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;

public class NbtCompound implements NbtElement {
   public static final Codec CODEC;
   private static final int SIZE = 48;
   private static final int field_41719 = 32;
   public static final NbtType TYPE;
   private final Map entries;

   protected NbtCompound(Map entries) {
      this.entries = entries;
   }

   public NbtCompound() {
      this(Maps.newHashMap());
   }

   public void write(DataOutput output) throws IOException {
      Iterator var2 = this.entries.keySet().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         NbtElement lv = (NbtElement)this.entries.get(string);
         write(string, lv, output);
      }

      output.writeByte(0);
   }

   public int getSizeInBytes() {
      int i = 48;

      Map.Entry entry;
      for(Iterator var2 = this.entries.entrySet().iterator(); var2.hasNext(); i += ((NbtElement)entry.getValue()).getSizeInBytes()) {
         entry = (Map.Entry)var2.next();
         i += 28 + 2 * ((String)entry.getKey()).length();
         i += 36;
      }

      return i;
   }

   public Set getKeys() {
      return this.entries.keySet();
   }

   public byte getType() {
      return NbtElement.COMPOUND_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public int getSize() {
      return this.entries.size();
   }

   @Nullable
   public NbtElement put(String key, NbtElement element) {
      return (NbtElement)this.entries.put(key, element);
   }

   public void putByte(String key, byte value) {
      this.entries.put(key, NbtByte.of(value));
   }

   public void putShort(String key, short value) {
      this.entries.put(key, NbtShort.of(value));
   }

   public void putInt(String key, int value) {
      this.entries.put(key, NbtInt.of(value));
   }

   public void putLong(String key, long value) {
      this.entries.put(key, NbtLong.of(value));
   }

   public void putUuid(String key, UUID value) {
      this.entries.put(key, NbtHelper.fromUuid(value));
   }

   public UUID getUuid(String key) {
      return NbtHelper.toUuid(this.get(key));
   }

   public boolean containsUuid(String key) {
      NbtElement lv = this.get(key);
      return lv != null && lv.getNbtType() == NbtIntArray.TYPE && ((NbtIntArray)lv).getIntArray().length == 4;
   }

   public void putFloat(String key, float value) {
      this.entries.put(key, NbtFloat.of(value));
   }

   public void putDouble(String key, double value) {
      this.entries.put(key, NbtDouble.of(value));
   }

   public void putString(String key, String value) {
      this.entries.put(key, NbtString.of(value));
   }

   public void putByteArray(String key, byte[] value) {
      this.entries.put(key, new NbtByteArray(value));
   }

   public void putByteArray(String key, List value) {
      this.entries.put(key, new NbtByteArray(value));
   }

   public void putIntArray(String key, int[] value) {
      this.entries.put(key, new NbtIntArray(value));
   }

   public void putIntArray(String key, List value) {
      this.entries.put(key, new NbtIntArray(value));
   }

   public void putLongArray(String key, long[] value) {
      this.entries.put(key, new NbtLongArray(value));
   }

   public void putLongArray(String key, List value) {
      this.entries.put(key, new NbtLongArray(value));
   }

   public void putBoolean(String key, boolean value) {
      this.entries.put(key, NbtByte.of(value));
   }

   @Nullable
   public NbtElement get(String key) {
      return (NbtElement)this.entries.get(key);
   }

   public byte getType(String key) {
      NbtElement lv = (NbtElement)this.entries.get(key);
      return lv == null ? NbtElement.END_TYPE : lv.getType();
   }

   public boolean contains(String key) {
      return this.entries.containsKey(key);
   }

   public boolean contains(String key, int type) {
      int j = this.getType(key);
      if (j == type) {
         return true;
      } else if (type != NbtElement.NUMBER_TYPE) {
         return false;
      } else {
         return j == NbtElement.BYTE_TYPE || j == NbtElement.SHORT_TYPE || j == NbtElement.INT_TYPE || j == NbtElement.LONG_TYPE || j == NbtElement.FLOAT_TYPE || j == NbtElement.DOUBLE_TYPE;
      }
   }

   public byte getByte(String key) {
      try {
         if (this.contains(key, NbtElement.NUMBER_TYPE)) {
            return ((AbstractNbtNumber)this.entries.get(key)).byteValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public short getShort(String key) {
      try {
         if (this.contains(key, NbtElement.NUMBER_TYPE)) {
            return ((AbstractNbtNumber)this.entries.get(key)).shortValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public int getInt(String key) {
      try {
         if (this.contains(key, NbtElement.NUMBER_TYPE)) {
            return ((AbstractNbtNumber)this.entries.get(key)).intValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public long getLong(String key) {
      try {
         if (this.contains(key, NbtElement.NUMBER_TYPE)) {
            return ((AbstractNbtNumber)this.entries.get(key)).longValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0L;
   }

   public float getFloat(String key) {
      try {
         if (this.contains(key, NbtElement.NUMBER_TYPE)) {
            return ((AbstractNbtNumber)this.entries.get(key)).floatValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0.0F;
   }

   public double getDouble(String key) {
      try {
         if (this.contains(key, NbtElement.NUMBER_TYPE)) {
            return ((AbstractNbtNumber)this.entries.get(key)).doubleValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0.0;
   }

   public String getString(String key) {
      try {
         if (this.contains(key, NbtElement.STRING_TYPE)) {
            return ((NbtElement)this.entries.get(key)).asString();
         }
      } catch (ClassCastException var3) {
      }

      return "";
   }

   public byte[] getByteArray(String key) {
      try {
         if (this.contains(key, NbtElement.BYTE_ARRAY_TYPE)) {
            return ((NbtByteArray)this.entries.get(key)).getByteArray();
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, NbtByteArray.TYPE, var3));
      }

      return new byte[0];
   }

   public int[] getIntArray(String key) {
      try {
         if (this.contains(key, NbtElement.INT_ARRAY_TYPE)) {
            return ((NbtIntArray)this.entries.get(key)).getIntArray();
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, NbtIntArray.TYPE, var3));
      }

      return new int[0];
   }

   public long[] getLongArray(String key) {
      try {
         if (this.contains(key, NbtElement.LONG_ARRAY_TYPE)) {
            return ((NbtLongArray)this.entries.get(key)).getLongArray();
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, NbtLongArray.TYPE, var3));
      }

      return new long[0];
   }

   public NbtCompound getCompound(String key) {
      try {
         if (this.contains(key, NbtElement.COMPOUND_TYPE)) {
            return (NbtCompound)this.entries.get(key);
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, TYPE, var3));
      }

      return new NbtCompound();
   }

   public NbtList getList(String key, int type) {
      try {
         if (this.getType(key) == NbtElement.LIST_TYPE) {
            NbtList lv = (NbtList)this.entries.get(key);
            if (!lv.isEmpty() && lv.getHeldType() != type) {
               return new NbtList();
            }

            return lv;
         }
      } catch (ClassCastException var4) {
         throw new CrashException(this.createCrashReport(key, NbtList.TYPE, var4));
      }

      return new NbtList();
   }

   public boolean getBoolean(String key) {
      return this.getByte(key) != 0;
   }

   public void remove(String key) {
      this.entries.remove(key);
   }

   public String toString() {
      return this.asString();
   }

   public boolean isEmpty() {
      return this.entries.isEmpty();
   }

   private CrashReport createCrashReport(String key, NbtType reader, ClassCastException exception) {
      CrashReport lv = CrashReport.create(exception, "Reading NBT data");
      CrashReportSection lv2 = lv.addElement("Corrupt NBT tag", 1);
      lv2.add("Tag type found", () -> {
         return ((NbtElement)this.entries.get(key)).getNbtType().getCrashReportName();
      });
      Objects.requireNonNull(reader);
      lv2.add("Tag type expected", reader::getCrashReportName);
      lv2.add("Tag name", (Object)key);
      return lv;
   }

   public NbtCompound copy() {
      Map map = Maps.newHashMap(Maps.transformValues(this.entries, NbtElement::copy));
      return new NbtCompound(map);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtCompound && Objects.equals(this.entries, ((NbtCompound)o).entries);
      }
   }

   public int hashCode() {
      return this.entries.hashCode();
   }

   private static void write(String key, NbtElement element, DataOutput output) throws IOException {
      output.writeByte(element.getType());
      if (element.getType() != 0) {
         output.writeUTF(key);
         element.write(output);
      }
   }

   static byte readByte(DataInput input, NbtTagSizeTracker tracker) throws IOException {
      return input.readByte();
   }

   static String readString(DataInput input, NbtTagSizeTracker tracker) throws IOException {
      return input.readUTF();
   }

   static NbtElement read(NbtType reader, String key, DataInput input, int depth, NbtTagSizeTracker tracker) {
      try {
         return reader.read(input, depth, tracker);
      } catch (IOException var8) {
         CrashReport lv = CrashReport.create(var8, "Loading NBT data");
         CrashReportSection lv2 = lv.addElement("NBT Tag");
         lv2.add("Tag name", (Object)key);
         lv2.add("Tag type", (Object)reader.getCrashReportName());
         throw new CrashException(lv);
      }
   }

   public NbtCompound copyFrom(NbtCompound source) {
      Iterator var2 = source.entries.keySet().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         NbtElement lv = (NbtElement)source.entries.get(string);
         if (lv.getType() == NbtElement.COMPOUND_TYPE) {
            if (this.contains(string, NbtElement.COMPOUND_TYPE)) {
               NbtCompound lv2 = this.getCompound(string);
               lv2.copyFrom((NbtCompound)lv);
            } else {
               this.put(string, lv.copy());
            }
         } else {
            this.put(string, lv.copy());
         }
      }

      return this;
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitCompound(this);
   }

   protected Map toMap() {
      return Collections.unmodifiableMap(this.entries);
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      Iterator var2 = this.entries.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         NbtElement lv = (NbtElement)entry.getValue();
         NbtType lv2 = lv.getNbtType();
         NbtScanner.NestedResult lv3 = visitor.visitSubNbtType(lv2);
         switch (lv3) {
            case HALT:
               return NbtScanner.Result.HALT;
            case BREAK:
               return visitor.endNested();
            case SKIP:
               break;
            default:
               lv3 = visitor.startSubNbt(lv2, (String)entry.getKey());
               switch (lv3) {
                  case HALT:
                     return NbtScanner.Result.HALT;
                  case BREAK:
                     return visitor.endNested();
                  case SKIP:
                     break;
                  default:
                     NbtScanner.Result lv4 = lv.doAccept(visitor);
                     switch (lv4) {
                        case HALT:
                           return NbtScanner.Result.HALT;
                        case BREAK:
                           return visitor.endNested();
                     }
               }
         }
      }

      return visitor.endNested();
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }

   static {
      CODEC = Codec.PASSTHROUGH.comapFlatMap((dynamic) -> {
         NbtElement lv = (NbtElement)dynamic.convert(NbtOps.INSTANCE).getValue();
         return lv instanceof NbtCompound ? DataResult.success((NbtCompound)lv) : DataResult.error(() -> {
            return "Not a compound tag: " + lv;
         });
      }, (nbt) -> {
         return new Dynamic(NbtOps.INSTANCE, nbt);
      });
      TYPE = new NbtType.OfVariableSize() {
         public NbtCompound read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
            arg.add(48L);
            if (i > 512) {
               throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            } else {
               Map map = Maps.newHashMap();

               byte b;
               while((b = NbtCompound.readByte(dataInput, arg)) != 0) {
                  String string = NbtCompound.readString(dataInput, arg);
                  arg.add((long)(28 + 2 * string.length()));
                  NbtElement lv = NbtCompound.read(NbtTypes.byId(b), string, dataInput, i + 1, arg);
                  if (map.put(string, lv) == null) {
                     arg.add(36L);
                  }
               }

               return new NbtCompound(map);
            }
         }

         public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
            while(true) {
               byte b;
               if ((b = input.readByte()) != 0) {
                  NbtType lv = NbtTypes.byId(b);
                  switch (visitor.visitSubNbtType(lv)) {
                     case HALT:
                        return NbtScanner.Result.HALT;
                     case BREAK:
                        NbtString.skip(input);
                        lv.skip(input);
                        break;
                     case SKIP:
                        NbtString.skip(input);
                        lv.skip(input);
                        continue;
                     default:
                        String string = input.readUTF();
                        switch (visitor.startSubNbt(lv, string)) {
                           case HALT:
                              return NbtScanner.Result.HALT;
                           case BREAK:
                              lv.skip(input);
                              break;
                           case SKIP:
                              lv.skip(input);
                              continue;
                           default:
                              switch (lv.doAccept(input, visitor)) {
                                 case HALT:
                                    return NbtScanner.Result.HALT;
                                 case BREAK:
                                 default:
                                    continue;
                              }
                        }
                  }
               }

               if (b != 0) {
                  while((b = input.readByte()) != 0) {
                     NbtString.skip(input);
                     NbtTypes.byId(b).skip(input);
                  }
               }

               return visitor.endNested();
            }
         }

         public void skip(DataInput input) throws IOException {
            byte b;
            while((b = input.readByte()) != 0) {
               NbtString.skip(input);
               NbtTypes.byId(b).skip(input);
            }

         }

         public String getCrashReportName() {
            return "COMPOUND";
         }

         public String getCommandFeedbackName() {
            return "TAG_Compound";
         }

         // $FF: synthetic method
         public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
            return this.read(input, depth, tracker);
         }
      };
   }
}
