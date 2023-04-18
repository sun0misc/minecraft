package net.minecraft.world;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.ChunkPos;

public class SimulationDistanceLevelPropagator extends ChunkPosDistanceLevelPropagator {
   private static final int field_34889 = 4;
   protected final Long2ByteMap levels = new Long2ByteOpenHashMap();
   private final Long2ObjectOpenHashMap tickets = new Long2ObjectOpenHashMap();

   public SimulationDistanceLevelPropagator() {
      super(34, 16, 256);
      this.levels.defaultReturnValue((byte)33);
   }

   private SortedArraySet getTickets(long pos) {
      return (SortedArraySet)this.tickets.computeIfAbsent(pos, (p) -> {
         return SortedArraySet.create(4);
      });
   }

   private int getLevel(SortedArraySet ticket) {
      return ticket.isEmpty() ? 34 : ((ChunkTicket)ticket.first()).getLevel();
   }

   public void add(long pos, ChunkTicket ticket) {
      SortedArraySet lv = this.getTickets(pos);
      int i = this.getLevel(lv);
      lv.add(ticket);
      if (ticket.getLevel() < i) {
         this.updateLevel(pos, ticket.getLevel(), true);
      }

   }

   public void remove(long pos, ChunkTicket ticket) {
      SortedArraySet lv = this.getTickets(pos);
      lv.remove(ticket);
      if (lv.isEmpty()) {
         this.tickets.remove(pos);
      }

      this.updateLevel(pos, this.getLevel(lv), false);
   }

   public void add(ChunkTicketType type, ChunkPos pos, int level, Object argument) {
      this.add(pos.toLong(), new ChunkTicket(type, level, argument));
   }

   public void remove(ChunkTicketType type, ChunkPos pos, int level, Object argument) {
      ChunkTicket lv = new ChunkTicket(type, level, argument);
      this.remove(pos.toLong(), lv);
   }

   public void updatePlayerTickets(int level) {
      List list = new ArrayList();
      ObjectIterator var3 = this.tickets.long2ObjectEntrySet().iterator();

      ChunkTicket lv;
      while(var3.hasNext()) {
         Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)var3.next();
         Iterator var5 = ((SortedArraySet)entry.getValue()).iterator();

         while(var5.hasNext()) {
            lv = (ChunkTicket)var5.next();
            if (lv.getType() == ChunkTicketType.PLAYER) {
               list.add(Pair.of(lv, entry.getLongKey()));
            }
         }
      }

      Iterator var9 = list.iterator();

      while(var9.hasNext()) {
         Pair pair = (Pair)var9.next();
         Long long_ = (Long)pair.getSecond();
         lv = (ChunkTicket)pair.getFirst();
         this.remove(long_, lv);
         ChunkPos lv2 = new ChunkPos(long_);
         ChunkTicketType lv3 = lv.getType();
         this.add(lv3, lv2, level, lv2);
      }

   }

   protected int getInitialLevel(long id) {
      SortedArraySet lv = (SortedArraySet)this.tickets.get(id);
      return lv != null && !lv.isEmpty() ? ((ChunkTicket)lv.first()).getLevel() : Integer.MAX_VALUE;
   }

   public int getLevel(ChunkPos pos) {
      return this.getLevel(pos.toLong());
   }

   protected int getLevel(long id) {
      return this.levels.get(id);
   }

   protected void setLevel(long id, int level) {
      if (level > 33) {
         this.levels.remove(id);
      } else {
         this.levels.put(id, (byte)level);
      }

   }

   public void updateLevels() {
      this.applyPendingUpdates(Integer.MAX_VALUE);
   }

   public String getTickingTicket(long pos) {
      SortedArraySet lv = (SortedArraySet)this.tickets.get(pos);
      return lv != null && !lv.isEmpty() ? ((ChunkTicket)lv.first()).toString() : "no_ticket";
   }
}
