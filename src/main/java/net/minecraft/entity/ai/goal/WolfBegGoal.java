package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WolfBegGoal extends Goal {
   private final WolfEntity wolf;
   @Nullable
   private PlayerEntity begFrom;
   private final World world;
   private final float begDistance;
   private int timer;
   private final TargetPredicate validPlayerPredicate;

   public WolfBegGoal(WolfEntity wolf, float begDistance) {
      this.wolf = wolf;
      this.world = wolf.world;
      this.begDistance = begDistance;
      this.validPlayerPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance((double)begDistance);
      this.setControls(EnumSet.of(Goal.Control.LOOK));
   }

   public boolean canStart() {
      this.begFrom = this.world.getClosestPlayer(this.validPlayerPredicate, this.wolf);
      return this.begFrom == null ? false : this.isAttractive(this.begFrom);
   }

   public boolean shouldContinue() {
      if (!this.begFrom.isAlive()) {
         return false;
      } else if (this.wolf.squaredDistanceTo(this.begFrom) > (double)(this.begDistance * this.begDistance)) {
         return false;
      } else {
         return this.timer > 0 && this.isAttractive(this.begFrom);
      }
   }

   public void start() {
      this.wolf.setBegging(true);
      this.timer = this.getTickCount(40 + this.wolf.getRandom().nextInt(40));
   }

   public void stop() {
      this.wolf.setBegging(false);
      this.begFrom = null;
   }

   public void tick() {
      this.wolf.getLookControl().lookAt(this.begFrom.getX(), this.begFrom.getEyeY(), this.begFrom.getZ(), 10.0F, (float)this.wolf.getMaxLookPitchChange());
      --this.timer;
   }

   private boolean isAttractive(PlayerEntity player) {
      Hand[] var2 = Hand.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Hand lv = var2[var4];
         ItemStack lv2 = player.getStackInHand(lv);
         if (this.wolf.isTamed() && lv2.isOf(Items.BONE)) {
            return true;
         }

         if (this.wolf.isBreedingItem(lv2)) {
            return true;
         }
      }

      return false;
   }
}
