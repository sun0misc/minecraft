package net.minecraft.entity.player;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;

public class ItemCooldownManager {
   private final Map entries = Maps.newHashMap();
   private int tick;

   public boolean isCoolingDown(Item item) {
      return this.getCooldownProgress(item, 0.0F) > 0.0F;
   }

   public float getCooldownProgress(Item item, float tickDelta) {
      Entry lv = (Entry)this.entries.get(item);
      if (lv != null) {
         float g = (float)(lv.endTick - lv.startTick);
         float h = (float)lv.endTick - ((float)this.tick + tickDelta);
         return MathHelper.clamp(h / g, 0.0F, 1.0F);
      } else {
         return 0.0F;
      }
   }

   public void update() {
      ++this.tick;
      if (!this.entries.isEmpty()) {
         Iterator iterator = this.entries.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            if (((Entry)entry.getValue()).endTick <= this.tick) {
               iterator.remove();
               this.onCooldownUpdate((Item)entry.getKey());
            }
         }
      }

   }

   public void set(Item item, int duration) {
      this.entries.put(item, new Entry(this.tick, this.tick + duration));
      this.onCooldownUpdate(item, duration);
   }

   public void remove(Item item) {
      this.entries.remove(item);
      this.onCooldownUpdate(item);
   }

   protected void onCooldownUpdate(Item item, int duration) {
   }

   protected void onCooldownUpdate(Item item) {
   }

   private static class Entry {
      final int startTick;
      final int endTick;

      Entry(int startTick, int endTick) {
         this.startTick = startTick;
         this.endTick = endTick;
      }
   }
}
