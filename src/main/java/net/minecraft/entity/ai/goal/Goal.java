package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.math.MathHelper;

public abstract class Goal {
   private final EnumSet controls = EnumSet.noneOf(Control.class);

   public abstract boolean canStart();

   public boolean shouldContinue() {
      return this.canStart();
   }

   public boolean canStop() {
      return true;
   }

   public void start() {
   }

   public void stop() {
   }

   public boolean shouldRunEveryTick() {
      return false;
   }

   public void tick() {
   }

   public void setControls(EnumSet controls) {
      this.controls.clear();
      this.controls.addAll(controls);
   }

   public String toString() {
      return this.getClass().getSimpleName();
   }

   public EnumSet getControls() {
      return this.controls;
   }

   protected int getTickCount(int ticks) {
      return this.shouldRunEveryTick() ? ticks : toGoalTicks(ticks);
   }

   protected static int toGoalTicks(int serverTicks) {
      return MathHelper.ceilDiv(serverTicks, 2);
   }

   public static enum Control {
      MOVE,
      LOOK,
      JUMP,
      TARGET;

      // $FF: synthetic method
      private static Control[] method_36621() {
         return new Control[]{MOVE, LOOK, JUMP, TARGET};
      }
   }
}
