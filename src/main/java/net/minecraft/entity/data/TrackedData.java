package net.minecraft.entity.data;

public class TrackedData {
   private final int id;
   private final TrackedDataHandler dataType;

   public TrackedData(int id, TrackedDataHandler dataType) {
      this.id = id;
      this.dataType = dataType;
   }

   public int getId() {
      return this.id;
   }

   public TrackedDataHandler getType() {
      return this.dataType;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TrackedData lv = (TrackedData)o;
         return this.id == lv.id;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.id;
   }

   public String toString() {
      return "<entity data: " + this.id + ">";
   }
}
