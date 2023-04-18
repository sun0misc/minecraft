package net.minecraft.network.packet.s2c.play;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public enum PositionFlag {
   X(0),
   Y(1),
   Z(2),
   Y_ROT(3),
   X_ROT(4);

   public static final Set VALUES = Set.of(values());
   public static final Set ROT = Set.of(X_ROT, Y_ROT);
   private final int shift;

   private PositionFlag(int shift) {
      this.shift = shift;
   }

   private int getMask() {
      return 1 << this.shift;
   }

   private boolean isSet(int mask) {
      return (mask & this.getMask()) == this.getMask();
   }

   public static Set getFlags(int mask) {
      Set set = EnumSet.noneOf(PositionFlag.class);
      PositionFlag[] var2 = values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         PositionFlag lv = var2[var4];
         if (lv.isSet(mask)) {
            set.add(lv);
         }
      }

      return set;
   }

   public static int getBitfield(Set flags) {
      int i = 0;

      PositionFlag lv;
      for(Iterator var2 = flags.iterator(); var2.hasNext(); i |= lv.getMask()) {
         lv = (PositionFlag)var2.next();
      }

      return i;
   }

   // $FF: synthetic method
   private static PositionFlag[] method_36952() {
      return new PositionFlag[]{X, Y, Z, Y_ROT, X_ROT};
   }
}
