package net.minecraft.world.tick;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class SimpleTickScheduler implements SerializableTickScheduler, BasicTickScheduler {
   private final List scheduledTicks = Lists.newArrayList();
   private final Set scheduledTicksSet;

   public SimpleTickScheduler() {
      this.scheduledTicksSet = new ObjectOpenCustomHashSet(Tick.HASH_STRATEGY);
   }

   public void scheduleTick(OrderedTick orderedTick) {
      Tick lv = new Tick(orderedTick.type(), orderedTick.pos(), 0, orderedTick.priority());
      this.scheduleTick(lv);
   }

   private void scheduleTick(Tick tick) {
      if (this.scheduledTicksSet.add(tick)) {
         this.scheduledTicks.add(tick);
      }

   }

   public boolean isQueued(BlockPos pos, Object type) {
      return this.scheduledTicksSet.contains(Tick.create(type, pos));
   }

   public int getTickCount() {
      return this.scheduledTicks.size();
   }

   public NbtElement toNbt(long time, Function typeToNameFunction) {
      NbtList lv = new NbtList();
      Iterator var5 = this.scheduledTicks.iterator();

      while(var5.hasNext()) {
         Tick lv2 = (Tick)var5.next();
         lv.add(lv2.toNbt(typeToNameFunction));
      }

      return lv;
   }

   public List getTicks() {
      return List.copyOf(this.scheduledTicks);
   }

   public static SimpleTickScheduler tick(NbtList tickList, Function typeToNameFunction, ChunkPos pos) {
      SimpleTickScheduler lv = new SimpleTickScheduler();
      Objects.requireNonNull(lv);
      Tick.tick(tickList, typeToNameFunction, pos, lv::scheduleTick);
      return lv;
   }
}
