package net.minecraft.world.tick;

import it.unimi.dsi.fastutil.Hash;
import java.util.Comparator;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public record OrderedTick(Object type, BlockPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
   public static final Comparator TRIGGER_TICK_COMPARATOR = (first, second) -> {
      int i = Long.compare(first.triggerTick, second.triggerTick);
      if (i != 0) {
         return i;
      } else {
         i = first.priority.compareTo(second.priority);
         return i != 0 ? i : Long.compare(first.subTickOrder, second.subTickOrder);
      }
   };
   public static final Comparator BASIC_COMPARATOR = (first, second) -> {
      int i = first.priority.compareTo(second.priority);
      return i != 0 ? i : Long.compare(first.subTickOrder, second.subTickOrder);
   };
   public static final Hash.Strategy HASH_STRATEGY = new Hash.Strategy() {
      public int hashCode(OrderedTick arg) {
         return 31 * arg.pos().hashCode() + arg.type().hashCode();
      }

      public boolean equals(@Nullable OrderedTick arg, @Nullable OrderedTick arg2) {
         if (arg == arg2) {
            return true;
         } else if (arg != null && arg2 != null) {
            return arg.type() == arg2.type() && arg.pos().equals(arg2.pos());
         } else {
            return false;
         }
      }

      // $FF: synthetic method
      public boolean equals(@Nullable Object first, @Nullable Object second) {
         return this.equals((OrderedTick)first, (OrderedTick)second);
      }

      // $FF: synthetic method
      public int hashCode(Object orderedTick) {
         return this.hashCode((OrderedTick)orderedTick);
      }
   };

   public OrderedTick(Object type, BlockPos pos, long triggerTick, long subTickOrder) {
      this(type, pos, triggerTick, TickPriority.NORMAL, subTickOrder);
   }

   public OrderedTick(Object object, BlockPos arg, long l, TickPriority arg2, long m) {
      arg = arg.toImmutable();
      this.type = object;
      this.pos = arg;
      this.triggerTick = l;
      this.priority = arg2;
      this.subTickOrder = m;
   }

   public static OrderedTick create(Object type, BlockPos pos) {
      return new OrderedTick(type, pos, 0L, TickPriority.NORMAL, 0L);
   }

   public Object type() {
      return this.type;
   }

   public BlockPos pos() {
      return this.pos;
   }

   public long triggerTick() {
      return this.triggerTick;
   }

   public TickPriority priority() {
      return this.priority;
   }

   public long subTickOrder() {
      return this.subTickOrder;
   }
}
