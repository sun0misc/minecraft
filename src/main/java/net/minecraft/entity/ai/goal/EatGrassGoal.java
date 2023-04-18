package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class EatGrassGoal extends Goal {
   private static final int MAX_TIMER = 40;
   private static final Predicate GRASS_PREDICATE;
   private final MobEntity mob;
   private final World world;
   private int timer;

   public EatGrassGoal(MobEntity mob) {
      this.mob = mob;
      this.world = mob.world;
      this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
   }

   public boolean canStart() {
      if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) != 0) {
         return false;
      } else {
         BlockPos lv = this.mob.getBlockPos();
         if (GRASS_PREDICATE.test(this.world.getBlockState(lv))) {
            return true;
         } else {
            return this.world.getBlockState(lv.down()).isOf(Blocks.GRASS_BLOCK);
         }
      }
   }

   public void start() {
      this.timer = this.getTickCount(40);
      this.world.sendEntityStatus(this.mob, EntityStatuses.SET_SHEEP_EAT_GRASS_TIMER_OR_PRIME_TNT_MINECART);
      this.mob.getNavigation().stop();
   }

   public void stop() {
      this.timer = 0;
   }

   public boolean shouldContinue() {
      return this.timer > 0;
   }

   public int getTimer() {
      return this.timer;
   }

   public void tick() {
      this.timer = Math.max(0, this.timer - 1);
      if (this.timer == this.getTickCount(4)) {
         BlockPos lv = this.mob.getBlockPos();
         if (GRASS_PREDICATE.test(this.world.getBlockState(lv))) {
            if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
               this.world.breakBlock(lv, false);
            }

            this.mob.onEatingGrass();
         } else {
            BlockPos lv2 = lv.down();
            if (this.world.getBlockState(lv2).isOf(Blocks.GRASS_BLOCK)) {
               if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                  this.world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, lv2, Block.getRawIdFromState(Blocks.GRASS_BLOCK.getDefaultState()));
                  this.world.setBlockState(lv2, Blocks.DIRT.getDefaultState(), Block.NOTIFY_LISTENERS);
               }

               this.mob.onEatingGrass();
            }
         }

      }
   }

   static {
      GRASS_PREDICATE = BlockStatePredicate.forBlock(Blocks.GRASS);
   }
}
