package net.minecraft.client.render.chunk;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class ChunkOcclusionData {
   private static final int DIRECTION_COUNT = Direction.values().length;
   private final BitSet visibility;

   public ChunkOcclusionData() {
      this.visibility = new BitSet(DIRECTION_COUNT * DIRECTION_COUNT);
   }

   public void addOpenEdgeFaces(Set faces) {
      Iterator var2 = faces.iterator();

      while(var2.hasNext()) {
         Direction lv = (Direction)var2.next();
         Iterator var4 = faces.iterator();

         while(var4.hasNext()) {
            Direction lv2 = (Direction)var4.next();
            this.setVisibleThrough(lv, lv2, true);
         }
      }

   }

   public void setVisibleThrough(Direction from, Direction to, boolean visible) {
      this.visibility.set(from.ordinal() + to.ordinal() * DIRECTION_COUNT, visible);
      this.visibility.set(to.ordinal() + from.ordinal() * DIRECTION_COUNT, visible);
   }

   public void fill(boolean visible) {
      this.visibility.set(0, this.visibility.size(), visible);
   }

   public boolean isVisibleThrough(Direction from, Direction to) {
      return this.visibility.get(from.ordinal() + to.ordinal() * DIRECTION_COUNT);
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(' ');
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      int var4;
      Direction lv;
      for(var4 = 0; var4 < var3; ++var4) {
         lv = var2[var4];
         stringBuilder.append(' ').append(lv.toString().toUpperCase().charAt(0));
      }

      stringBuilder.append('\n');
      var2 = Direction.values();
      var3 = var2.length;

      for(var4 = 0; var4 < var3; ++var4) {
         lv = var2[var4];
         stringBuilder.append(lv.toString().toUpperCase().charAt(0));
         Direction[] var6 = Direction.values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Direction lv2 = var6[var8];
            if (lv == lv2) {
               stringBuilder.append("  ");
            } else {
               boolean bl = this.isVisibleThrough(lv, lv2);
               stringBuilder.append(' ').append((char)(bl ? 'Y' : 'n'));
            }
         }

         stringBuilder.append('\n');
      }

      return stringBuilder.toString();
   }
}
