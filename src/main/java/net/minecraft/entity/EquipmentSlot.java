package net.minecraft.entity;

public enum EquipmentSlot {
   MAINHAND(EquipmentSlot.Type.HAND, 0, 0, "mainhand"),
   OFFHAND(EquipmentSlot.Type.HAND, 1, 5, "offhand"),
   FEET(EquipmentSlot.Type.ARMOR, 0, 1, "feet"),
   LEGS(EquipmentSlot.Type.ARMOR, 1, 2, "legs"),
   CHEST(EquipmentSlot.Type.ARMOR, 2, 3, "chest"),
   HEAD(EquipmentSlot.Type.ARMOR, 3, 4, "head");

   private final Type type;
   private final int entityId;
   private final int armorStandId;
   private final String name;

   private EquipmentSlot(Type type, int entityId, int armorStandId, String name) {
      this.type = type;
      this.entityId = entityId;
      this.armorStandId = armorStandId;
      this.name = name;
   }

   public Type getType() {
      return this.type;
   }

   public int getEntitySlotId() {
      return this.entityId;
   }

   public int getOffsetEntitySlotId(int offset) {
      return offset + this.entityId;
   }

   public int getArmorStandSlotId() {
      return this.armorStandId;
   }

   public String getName() {
      return this.name;
   }

   public boolean isArmorSlot() {
      return this.type == EquipmentSlot.Type.ARMOR;
   }

   public static EquipmentSlot byName(String name) {
      EquipmentSlot[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         EquipmentSlot lv = var1[var3];
         if (lv.getName().equals(name)) {
            return lv;
         }
      }

      throw new IllegalArgumentException("Invalid slot '" + name + "'");
   }

   public static EquipmentSlot fromTypeIndex(Type type, int index) {
      EquipmentSlot[] var2 = values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EquipmentSlot lv = var2[var4];
         if (lv.getType() == type && lv.getEntitySlotId() == index) {
            return lv;
         }
      }

      throw new IllegalArgumentException("Invalid slot '" + type + "': " + index);
   }

   // $FF: synthetic method
   private static EquipmentSlot[] method_36604() {
      return new EquipmentSlot[]{MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD};
   }

   public static enum Type {
      HAND,
      ARMOR;

      // $FF: synthetic method
      private static Type[] method_36605() {
         return new Type[]{HAND, ARMOR};
      }
   }
}
