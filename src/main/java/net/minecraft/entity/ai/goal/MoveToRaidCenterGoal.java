package net.minecraft.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;

public class MoveToRaidCenterGoal extends Goal {
   private static final int FREE_RAIDER_CHECK_INTERVAL = 20;
   private static final float WALK_SPEED = 1.0F;
   private final RaiderEntity actor;
   private int nextFreeRaiderCheckAge;

   public MoveToRaidCenterGoal(RaiderEntity actor) {
      this.actor = actor;
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      return this.actor.getTarget() == null && !this.actor.hasPassengers() && this.actor.hasActiveRaid() && !this.actor.getRaid().isFinished() && !((ServerWorld)this.actor.world).isNearOccupiedPointOfInterest(this.actor.getBlockPos());
   }

   public boolean shouldContinue() {
      return this.actor.hasActiveRaid() && !this.actor.getRaid().isFinished() && this.actor.world instanceof ServerWorld && !((ServerWorld)this.actor.world).isNearOccupiedPointOfInterest(this.actor.getBlockPos());
   }

   public void tick() {
      if (this.actor.hasActiveRaid()) {
         Raid lv = this.actor.getRaid();
         if (this.actor.age > this.nextFreeRaiderCheckAge) {
            this.nextFreeRaiderCheckAge = this.actor.age + 20;
            this.includeFreeRaiders(lv);
         }

         if (!this.actor.isNavigating()) {
            Vec3d lv2 = NoPenaltyTargeting.findTo(this.actor, 15, 4, Vec3d.ofBottomCenter(lv.getCenter()), 1.5707963705062866);
            if (lv2 != null) {
               this.actor.getNavigation().startMovingTo(lv2.x, lv2.y, lv2.z, 1.0);
            }
         }
      }

   }

   private void includeFreeRaiders(Raid raid) {
      if (raid.isActive()) {
         Set set = Sets.newHashSet();
         List list = this.actor.world.getEntitiesByClass(RaiderEntity.class, this.actor.getBoundingBox().expand(16.0), (raider) -> {
            return !raider.hasActiveRaid() && RaidManager.isValidRaiderFor(raider, raid);
         });
         set.addAll(list);
         Iterator var4 = set.iterator();

         while(var4.hasNext()) {
            RaiderEntity lv = (RaiderEntity)var4.next();
            raid.addRaider(raid.getGroupsSpawned(), lv, (BlockPos)null, true);
         }
      }

   }
}
