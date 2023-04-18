package net.minecraft.server.world;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class SleepManager {
   private int total;
   private int sleeping;

   public boolean canSkipNight(int percentage) {
      return this.sleeping >= this.getNightSkippingRequirement(percentage);
   }

   public boolean canResetTime(int percentage, List players) {
      int j = (int)players.stream().filter(PlayerEntity::canResetTimeBySleeping).count();
      return j >= this.getNightSkippingRequirement(percentage);
   }

   public int getNightSkippingRequirement(int percentage) {
      return Math.max(1, MathHelper.ceil((float)(this.total * percentage) / 100.0F));
   }

   public void clearSleeping() {
      this.sleeping = 0;
   }

   public int getSleeping() {
      return this.sleeping;
   }

   public boolean update(List players) {
      int i = this.total;
      int j = this.sleeping;
      this.total = 0;
      this.sleeping = 0;
      Iterator var4 = players.iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var4.next();
         if (!lv.isSpectator()) {
            ++this.total;
            if (lv.isSleeping()) {
               ++this.sleeping;
            }
         }
      }

      return (j > 0 || this.sleeping > 0) && (i != this.total || j != this.sleeping);
   }
}
