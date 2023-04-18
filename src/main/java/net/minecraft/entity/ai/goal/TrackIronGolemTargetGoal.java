package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public class TrackIronGolemTargetGoal extends TrackTargetGoal {
   private final IronGolemEntity golem;
   @Nullable
   private LivingEntity target;
   private final TargetPredicate targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(64.0);

   public TrackIronGolemTargetGoal(IronGolemEntity golem) {
      super(golem, false, true);
      this.golem = golem;
      this.setControls(EnumSet.of(Goal.Control.TARGET));
   }

   public boolean canStart() {
      Box lv = this.golem.getBoundingBox().expand(10.0, 8.0, 10.0);
      List list = this.golem.world.getTargets(VillagerEntity.class, this.targetPredicate, this.golem, lv);
      List list2 = this.golem.world.getPlayers(this.targetPredicate, this.golem, lv);
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         LivingEntity lv2 = (LivingEntity)var4.next();
         VillagerEntity lv3 = (VillagerEntity)lv2;
         Iterator var7 = list2.iterator();

         while(var7.hasNext()) {
            PlayerEntity lv4 = (PlayerEntity)var7.next();
            int i = lv3.getReputation(lv4);
            if (i <= -100) {
               this.target = lv4;
            }
         }
      }

      if (this.target == null) {
         return false;
      } else if (!(this.target instanceof PlayerEntity) || !this.target.isSpectator() && !((PlayerEntity)this.target).isCreative()) {
         return true;
      } else {
         return false;
      }
   }

   public void start() {
      this.golem.setTarget(this.target);
      super.start();
   }
}
