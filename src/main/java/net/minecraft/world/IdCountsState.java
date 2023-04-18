package net.minecraft.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class IdCountsState extends PersistentState {
   public static final String IDCOUNTS_KEY = "idcounts";
   private final Object2IntMap idCounts = new Object2IntOpenHashMap();

   public IdCountsState() {
      this.idCounts.defaultReturnValue(-1);
   }

   public static IdCountsState fromNbt(NbtCompound nbt) {
      IdCountsState lv = new IdCountsState();
      Iterator var2 = nbt.getKeys().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         if (nbt.contains(string, NbtElement.NUMBER_TYPE)) {
            lv.idCounts.put(string, nbt.getInt(string));
         }
      }

      return lv;
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      ObjectIterator var2 = this.idCounts.object2IntEntrySet().iterator();

      while(var2.hasNext()) {
         Object2IntMap.Entry entry = (Object2IntMap.Entry)var2.next();
         nbt.putInt((String)entry.getKey(), entry.getIntValue());
      }

      return nbt;
   }

   public int getNextMapId() {
      int i = this.idCounts.getInt("map") + 1;
      this.idCounts.put("map", i);
      this.markDirty();
      return i;
   }
}
