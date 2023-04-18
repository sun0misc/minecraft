package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;

public class NbtList extends AbstractNbtList {
   private static final int SIZE = 37;
   public static final NbtType TYPE = new NbtType.OfVariableSize() {
      public NbtList read(DataInput dataInput, int i, NbtTagSizeTracker arg) throws IOException {
         arg.add(37L);
         if (i > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
         } else {
            byte b = dataInput.readByte();
            int j = dataInput.readInt();
            if (b == 0 && j > 0) {
               throw new RuntimeException("Missing type on ListTag");
            } else {
               arg.add(4L * (long)j);
               NbtType lv = NbtTypes.byId(b);
               List list = Lists.newArrayListWithCapacity(j);

               for(int k = 0; k < j; ++k) {
                  list.add(lv.read(dataInput, i + 1, arg));
               }

               return new NbtList(list, b);
            }
         }
      }

      public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor) throws IOException {
         NbtType lv = NbtTypes.byId(input.readByte());
         int i = input.readInt();
         switch (visitor.visitListMeta(lv, i)) {
            case HALT:
               return NbtScanner.Result.HALT;
            case BREAK:
               lv.skip(input, i);
               return visitor.endNested();
            default:
               int j = 0;

               label34:
               for(; j < i; ++j) {
                  switch (visitor.startListItem(lv, j)) {
                     case HALT:
                        return NbtScanner.Result.HALT;
                     case BREAK:
                        lv.skip(input);
                        break label34;
                     case SKIP:
                        lv.skip(input);
                        break;
                     default:
                        switch (lv.doAccept(input, visitor)) {
                           case HALT:
                              return NbtScanner.Result.HALT;
                           case BREAK:
                              break label34;
                        }
                  }
               }

               int k = i - 1 - j;
               if (k > 0) {
                  lv.skip(input, k);
               }

               return visitor.endNested();
         }
      }

      public void skip(DataInput input) throws IOException {
         NbtType lv = NbtTypes.byId(input.readByte());
         int i = input.readInt();
         lv.skip(input, i);
      }

      public String getCrashReportName() {
         return "LIST";
      }

      public String getCommandFeedbackName() {
         return "TAG_List";
      }

      // $FF: synthetic method
      public NbtElement read(DataInput input, int depth, NbtTagSizeTracker tracker) throws IOException {
         return this.read(input, depth, tracker);
      }
   };
   private final List value;
   private byte type;

   NbtList(List list, byte type) {
      this.value = list;
      this.type = type;
   }

   public NbtList() {
      this(Lists.newArrayList(), NbtElement.END_TYPE);
   }

   public void write(DataOutput output) throws IOException {
      if (this.value.isEmpty()) {
         this.type = 0;
      } else {
         this.type = ((NbtElement)this.value.get(0)).getType();
      }

      output.writeByte(this.type);
      output.writeInt(this.value.size());
      Iterator var2 = this.value.iterator();

      while(var2.hasNext()) {
         NbtElement lv = (NbtElement)var2.next();
         lv.write(output);
      }

   }

   public int getSizeInBytes() {
      int i = 37;
      i += 4 * this.value.size();

      NbtElement lv;
      for(Iterator var2 = this.value.iterator(); var2.hasNext(); i += lv.getSizeInBytes()) {
         lv = (NbtElement)var2.next();
      }

      return i;
   }

   public byte getType() {
      return NbtElement.LIST_TYPE;
   }

   public NbtType getNbtType() {
      return TYPE;
   }

   public String toString() {
      return this.asString();
   }

   private void forgetTypeIfEmpty() {
      if (this.value.isEmpty()) {
         this.type = 0;
      }

   }

   public NbtElement remove(int i) {
      NbtElement lv = (NbtElement)this.value.remove(i);
      this.forgetTypeIfEmpty();
      return lv;
   }

   public boolean isEmpty() {
      return this.value.isEmpty();
   }

   public NbtCompound getCompound(int index) {
      if (index >= 0 && index < this.value.size()) {
         NbtElement lv = (NbtElement)this.value.get(index);
         if (lv.getType() == NbtElement.COMPOUND_TYPE) {
            return (NbtCompound)lv;
         }
      }

      return new NbtCompound();
   }

   public NbtList getList(int index) {
      if (index >= 0 && index < this.value.size()) {
         NbtElement lv = (NbtElement)this.value.get(index);
         if (lv.getType() == NbtElement.LIST_TYPE) {
            return (NbtList)lv;
         }
      }

      return new NbtList();
   }

   public short getShort(int index) {
      if (index >= 0 && index < this.value.size()) {
         NbtElement lv = (NbtElement)this.value.get(index);
         if (lv.getType() == NbtElement.SHORT_TYPE) {
            return ((NbtShort)lv).shortValue();
         }
      }

      return 0;
   }

   public int getInt(int index) {
      if (index >= 0 && index < this.value.size()) {
         NbtElement lv = (NbtElement)this.value.get(index);
         if (lv.getType() == NbtElement.INT_TYPE) {
            return ((NbtInt)lv).intValue();
         }
      }

      return 0;
   }

   public int[] getIntArray(int index) {
      if (index >= 0 && index < this.value.size()) {
         NbtElement lv = (NbtElement)this.value.get(index);
         if (lv.getType() == NbtElement.INT_ARRAY_TYPE) {
            return ((NbtIntArray)lv).getIntArray();
         }
      }

      return new int[0];
   }

   public long[] getLongArray(int index) {
      if (index >= 0 && index < this.value.size()) {
         NbtElement lv = (NbtElement)this.value.get(index);
         if (lv.getType() == NbtElement.LONG_ARRAY_TYPE) {
            return ((NbtLongArray)lv).getLongArray();
         }
      }

      return new long[0];
   }

   public double getDouble(int index) {
      if (index >= 0 && index < this.value.size()) {
         NbtElement lv = (NbtElement)this.value.get(index);
         if (lv.getType() == NbtElement.DOUBLE_TYPE) {
            return ((NbtDouble)lv).doubleValue();
         }
      }

      return 0.0;
   }

   public float getFloat(int index) {
      if (index >= 0 && index < this.value.size()) {
         NbtElement lv = (NbtElement)this.value.get(index);
         if (lv.getType() == NbtElement.FLOAT_TYPE) {
            return ((NbtFloat)lv).floatValue();
         }
      }

      return 0.0F;
   }

   public String getString(int index) {
      if (index >= 0 && index < this.value.size()) {
         NbtElement lv = (NbtElement)this.value.get(index);
         return lv.getType() == NbtElement.STRING_TYPE ? lv.asString() : lv.toString();
      } else {
         return "";
      }
   }

   public int size() {
      return this.value.size();
   }

   public NbtElement get(int i) {
      return (NbtElement)this.value.get(i);
   }

   public NbtElement set(int i, NbtElement arg) {
      NbtElement lv = this.get(i);
      if (!this.setElement(i, arg)) {
         throw new UnsupportedOperationException(String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", arg.getType(), this.type));
      } else {
         return lv;
      }
   }

   public void add(int i, NbtElement arg) {
      if (!this.addElement(i, arg)) {
         throw new UnsupportedOperationException(String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", arg.getType(), this.type));
      }
   }

   public boolean setElement(int index, NbtElement element) {
      if (this.canAdd(element)) {
         this.value.set(index, element);
         return true;
      } else {
         return false;
      }
   }

   public boolean addElement(int index, NbtElement element) {
      if (this.canAdd(element)) {
         this.value.add(index, element);
         return true;
      } else {
         return false;
      }
   }

   private boolean canAdd(NbtElement element) {
      if (element.getType() == 0) {
         return false;
      } else if (this.type == 0) {
         this.type = element.getType();
         return true;
      } else {
         return this.type == element.getType();
      }
   }

   public NbtList copy() {
      Iterable iterable = NbtTypes.byId(this.type).isImmutable() ? this.value : Iterables.transform(this.value, NbtElement::copy);
      List list = Lists.newArrayList((Iterable)iterable);
      return new NbtList(list, this.type);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtList && Objects.equals(this.value, ((NbtList)o).value);
      }
   }

   public int hashCode() {
      return this.value.hashCode();
   }

   public void accept(NbtElementVisitor visitor) {
      visitor.visitList(this);
   }

   public byte getHeldType() {
      return this.type;
   }

   public void clear() {
      this.value.clear();
      this.type = 0;
   }

   public NbtScanner.Result doAccept(NbtScanner visitor) {
      switch (visitor.visitListMeta(NbtTypes.byId(this.type), this.value.size())) {
         case HALT:
            return NbtScanner.Result.HALT;
         case BREAK:
            return visitor.endNested();
         default:
            int i = 0;

            while(i < this.value.size()) {
               NbtElement lv = (NbtElement)this.value.get(i);
               switch (visitor.startListItem(lv.getNbtType(), i)) {
                  case HALT:
                     return NbtScanner.Result.HALT;
                  case BREAK:
                     return visitor.endNested();
                  default:
                     switch (lv.doAccept(visitor)) {
                        case HALT:
                           return NbtScanner.Result.HALT;
                        case BREAK:
                           return visitor.endNested();
                     }
                  case SKIP:
                     ++i;
               }
            }

            return visitor.endNested();
      }
   }

   // $FF: synthetic method
   public NbtElement copy() {
      return this.copy();
   }

   // $FF: synthetic method
   public Object remove(int i) {
      return this.remove(i);
   }

   // $FF: synthetic method
   public void add(int i, Object object) {
      this.add(i, (NbtElement)object);
   }

   // $FF: synthetic method
   public Object set(int i, Object object) {
      return this.set(i, (NbtElement)object);
   }

   // $FF: synthetic method
   public Object get(int index) {
      return this.get(index);
   }
}
