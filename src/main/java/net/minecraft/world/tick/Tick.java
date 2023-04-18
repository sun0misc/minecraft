package net.minecraft.world.tick;

import it.unimi.dsi.fastutil.Hash;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

public record Tick(Object type, BlockPos pos, int delay, TickPriority priority) {
   private static final String TYPE_NBT_KEY = "i";
   private static final String X_NBT_KEY = "x";
   private static final String Y_NBT_KEY = "y";
   private static final String Z_NBT_KEY = "z";
   private static final String DELAY_NBT_KEY = "t";
   private static final String PRIORITY_NBT_KEY = "p";
   public static final Hash.Strategy HASH_STRATEGY = new Hash.Strategy() {
      public int hashCode(Tick arg) {
         return 31 * arg.pos().hashCode() + arg.type().hashCode();
      }

      public boolean equals(@Nullable Tick arg, @Nullable Tick arg2) {
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
         return this.equals((Tick)first, (Tick)second);
      }

      // $FF: synthetic method
      public int hashCode(Object tick) {
         return this.hashCode((Tick)tick);
      }
   };

   public Tick(Object object, BlockPos arg, int i, TickPriority arg2) {
      this.type = object;
      this.pos = arg;
      this.delay = i;
      this.priority = arg2;
   }

   public static void tick(NbtList tickList, Function nameToTypeFunction, ChunkPos pos, Consumer tickConsumer) {
      long l = pos.toLong();

      for(int i = 0; i < tickList.size(); ++i) {
         NbtCompound lv = tickList.getCompound(i);
         fromNbt(lv, nameToTypeFunction).ifPresent((tick) -> {
            if (ChunkPos.toLong(tick.pos()) == l) {
               tickConsumer.accept(tick);
            }

         });
      }

   }

   public static Optional fromNbt(NbtCompound nbt, Function nameToType) {
      return ((Optional)nameToType.apply(nbt.getString("i"))).map((type) -> {
         BlockPos lv = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
         return new Tick(type, lv, nbt.getInt("t"), TickPriority.byIndex(nbt.getInt("p")));
      });
   }

   private static NbtCompound toNbt(String type, BlockPos pos, int delay, TickPriority priority) {
      NbtCompound lv = new NbtCompound();
      lv.putString("i", type);
      lv.putInt("x", pos.getX());
      lv.putInt("y", pos.getY());
      lv.putInt("z", pos.getZ());
      lv.putInt("t", delay);
      lv.putInt("p", priority.getIndex());
      return lv;
   }

   public static NbtCompound orderedTickToNbt(OrderedTick orderedTick, Function typeToNameFunction, long delay) {
      return toNbt((String)typeToNameFunction.apply(orderedTick.type()), orderedTick.pos(), (int)(orderedTick.triggerTick() - delay), orderedTick.priority());
   }

   public NbtCompound toNbt(Function typeToNameFunction) {
      return toNbt((String)typeToNameFunction.apply(this.type), this.pos, this.delay, this.priority);
   }

   public OrderedTick createOrderedTick(long time, long subTickOrder) {
      return new OrderedTick(this.type, this.pos, time + (long)this.delay, this.priority, subTickOrder);
   }

   public static Tick create(Object type, BlockPos pos) {
      return new Tick(type, pos, 0, TickPriority.NORMAL);
   }

   public Object type() {
      return this.type;
   }

   public BlockPos pos() {
      return this.pos;
   }

   public int delay() {
      return this.delay;
   }

   public TickPriority priority() {
      return this.priority;
   }
}
