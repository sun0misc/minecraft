package net.minecraft.nbt;

import java.util.AbstractList;

public abstract class AbstractNbtList extends AbstractList implements NbtElement {
   public abstract NbtElement set(int i, NbtElement arg);

   public abstract void add(int i, NbtElement arg);

   public abstract NbtElement remove(int i);

   public abstract boolean setElement(int index, NbtElement element);

   public abstract boolean addElement(int index, NbtElement element);

   public abstract byte getHeldType();

   // $FF: synthetic method
   public Object remove(int index) {
      return this.remove(index);
   }

   // $FF: synthetic method
   public void add(int index, Object value) {
      this.add(index, (NbtElement)value);
   }

   // $FF: synthetic method
   public Object set(int index, Object value) {
      return this.set(index, (NbtElement)value);
   }
}
