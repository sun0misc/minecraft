package net.minecraft.entity.ai.brain.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class BiasedLongJumpTask extends LongJumpTask {
   private final TagKey favoredBlocks;
   private final float biasChance;
   private final List unfavoredTargets = new ArrayList();
   private boolean useBias;

   public BiasedLongJumpTask(UniformIntProvider cooldownRange, int verticalRange, int horizontalRange, float maxRange, Function entityToSound, TagKey favoredBlocks, float biasChance, BiPredicate jumpToPredicate) {
      super(cooldownRange, verticalRange, horizontalRange, maxRange, entityToSound, jumpToPredicate);
      this.favoredBlocks = favoredBlocks;
      this.biasChance = biasChance;
   }

   protected void run(ServerWorld arg, MobEntity arg2, long l) {
      super.run(arg, arg2, l);
      this.unfavoredTargets.clear();
      this.useBias = arg2.getRandom().nextFloat() < this.biasChance;
   }

   protected Optional getTarget(ServerWorld world) {
      if (!this.useBias) {
         return super.getTarget(world);
      } else {
         BlockPos.Mutable lv = new BlockPos.Mutable();

         while(!this.targets.isEmpty()) {
            Optional optional = super.getTarget(world);
            if (optional.isPresent()) {
               LongJumpTask.Target lv2 = (LongJumpTask.Target)optional.get();
               if (world.getBlockState(lv.set(lv2.getPos(), (Direction)Direction.DOWN)).isIn(this.favoredBlocks)) {
                  return optional;
               }

               this.unfavoredTargets.add(lv2);
            }
         }

         if (!this.unfavoredTargets.isEmpty()) {
            return Optional.of((LongJumpTask.Target)this.unfavoredTargets.remove(0));
         } else {
            return Optional.empty();
         }
      }
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (MobEntity)entity, time);
   }
}
