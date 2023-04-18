package net.minecraft.server.world;

import java.util.Objects;

public final class ChunkTicket implements Comparable {
   private final ChunkTicketType type;
   private final int level;
   private final Object argument;
   private long tickCreated;

   protected ChunkTicket(ChunkTicketType type, int level, Object argument) {
      this.type = type;
      this.level = level;
      this.argument = argument;
   }

   public int compareTo(ChunkTicket arg) {
      int i = Integer.compare(this.level, arg.level);
      if (i != 0) {
         return i;
      } else {
         int j = Integer.compare(System.identityHashCode(this.type), System.identityHashCode(arg.type));
         return j != 0 ? j : this.type.getArgumentComparator().compare(this.argument, arg.argument);
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ChunkTicket)) {
         return false;
      } else {
         ChunkTicket lv = (ChunkTicket)o;
         return this.level == lv.level && Objects.equals(this.type, lv.type) && Objects.equals(this.argument, lv.argument);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type, this.level, this.argument});
   }

   public String toString() {
      return "Ticket[" + this.type + " " + this.level + " (" + this.argument + ")] at " + this.tickCreated;
   }

   public ChunkTicketType getType() {
      return this.type;
   }

   public int getLevel() {
      return this.level;
   }

   protected void setTickCreated(long tickCreated) {
      this.tickCreated = tickCreated;
   }

   protected boolean isExpired(long currentTick) {
      long m = this.type.getExpiryTicks();
      return m != 0L && currentTick - this.tickCreated > m;
   }

   // $FF: synthetic method
   public int compareTo(Object that) {
      return this.compareTo((ChunkTicket)that);
   }
}
