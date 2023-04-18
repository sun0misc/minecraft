package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AnimalMateGoal extends Goal {
   private static final TargetPredicate VALID_MATE_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(8.0).ignoreVisibility();
   protected final AnimalEntity animal;
   private final Class entityClass;
   protected final World world;
   @Nullable
   protected AnimalEntity mate;
   private int timer;
   private final double speed;

   public AnimalMateGoal(AnimalEntity animal, double speed) {
      this(animal, speed, animal.getClass());
   }

   public AnimalMateGoal(AnimalEntity animal, double speed, Class entityClass) {
      this.animal = animal;
      this.world = animal.world;
      this.entityClass = entityClass;
      this.speed = speed;
      this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
   }

   public boolean canStart() {
      if (!this.animal.isInLove()) {
         return false;
      } else {
         this.mate = this.findMate();
         return this.mate != null;
      }
   }

   public boolean shouldContinue() {
      return this.mate.isAlive() && this.mate.isInLove() && this.timer < 60;
   }

   public void stop() {
      this.mate = null;
      this.timer = 0;
   }

   public void tick() {
      this.animal.getLookControl().lookAt(this.mate, 10.0F, (float)this.animal.getMaxLookPitchChange());
      this.animal.getNavigation().startMovingTo(this.mate, this.speed);
      ++this.timer;
      if (this.timer >= this.getTickCount(60) && this.animal.squaredDistanceTo(this.mate) < 9.0) {
         this.breed();
      }

   }

   @Nullable
   private AnimalEntity findMate() {
      List list = this.world.getTargets(this.entityClass, VALID_MATE_PREDICATE, this.animal, this.animal.getBoundingBox().expand(8.0));
      double d = Double.MAX_VALUE;
      AnimalEntity lv = null;
      Iterator var5 = list.iterator();

      while(var5.hasNext()) {
         AnimalEntity lv2 = (AnimalEntity)var5.next();
         if (this.animal.canBreedWith(lv2) && this.animal.squaredDistanceTo(lv2) < d) {
            lv = lv2;
            d = this.animal.squaredDistanceTo(lv2);
         }
      }

      return lv;
   }

   protected void breed() {
      this.animal.breed((ServerWorld)this.world, this.mate);
   }
}
