package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

public class RevengeGoal extends TrackTargetGoal {
   private static final TargetPredicate VALID_AVOIDABLES_PREDICATE = TargetPredicate.createAttackable().ignoreVisibility().ignoreDistanceScalingFactor();
   private static final int BOX_VERTICAL_EXPANSION = 10;
   private boolean groupRevenge;
   private int lastAttackedTime;
   private final Class[] noRevengeTypes;
   @Nullable
   private Class[] noHelpTypes;

   public RevengeGoal(PathAwareEntity mob, Class... noRevengeTypes) {
      super(mob, true);
      this.noRevengeTypes = noRevengeTypes;
      this.setControls(EnumSet.of(Goal.Control.TARGET));
   }

   public boolean canStart() {
      int i = this.mob.getLastAttackedTime();
      LivingEntity lv = this.mob.getAttacker();
      if (i != this.lastAttackedTime && lv != null) {
         if (lv.getType() == EntityType.PLAYER && this.mob.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
            return false;
         } else {
            Class[] var3 = this.noRevengeTypes;
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               Class class_ = var3[var5];
               if (class_.isAssignableFrom(lv.getClass())) {
                  return false;
               }
            }

            return this.canTrack(lv, VALID_AVOIDABLES_PREDICATE);
         }
      } else {
         return false;
      }
   }

   public RevengeGoal setGroupRevenge(Class... noHelpTypes) {
      this.groupRevenge = true;
      this.noHelpTypes = noHelpTypes;
      return this;
   }

   public void start() {
      this.mob.setTarget(this.mob.getAttacker());
      this.target = this.mob.getTarget();
      this.lastAttackedTime = this.mob.getLastAttackedTime();
      this.maxTimeWithoutVisibility = 300;
      if (this.groupRevenge) {
         this.callSameTypeForRevenge();
      }

      super.start();
   }

   protected void callSameTypeForRevenge() {
      double d = this.getFollowRange();
      Box lv = Box.from(this.mob.getPos()).expand(d, 10.0, d);
      List list = this.mob.world.getEntitiesByClass(this.mob.getClass(), lv, EntityPredicates.EXCEPT_SPECTATOR);
      Iterator var5 = list.iterator();

      while(true) {
         MobEntity lv2;
         boolean bl;
         do {
            do {
               do {
                  do {
                     do {
                        if (!var5.hasNext()) {
                           return;
                        }

                        lv2 = (MobEntity)var5.next();
                     } while(this.mob == lv2);
                  } while(lv2.getTarget() != null);
               } while(this.mob instanceof TameableEntity && ((TameableEntity)this.mob).getOwner() != ((TameableEntity)lv2).getOwner());
            } while(lv2.isTeammate(this.mob.getAttacker()));

            if (this.noHelpTypes == null) {
               break;
            }

            bl = false;
            Class[] var8 = this.noHelpTypes;
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
               Class class_ = var8[var10];
               if (lv2.getClass() == class_) {
                  bl = true;
                  break;
               }
            }
         } while(bl);

         this.setMobEntityTarget(lv2, this.mob.getAttacker());
      }
   }

   protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
      mob.setTarget(target);
   }
}
