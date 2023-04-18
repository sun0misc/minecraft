package net.minecraft.entity.ai.brain.task;

import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.mutable.MutableLong;

public class WalkTowardsLandTask {
   private static final int TASK_COOLDOWN = 60;

   public static Task create(int range, float speed) {
      MutableLong mutableLong = new MutableLong(0L);
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (attackTarget, walkTarget, lookTarget) -> {
            return (world, entity, time) -> {
               if (!world.getFluidState(entity.getBlockPos()).isIn(FluidTags.WATER)) {
                  return false;
               } else if (time < mutableLong.getValue()) {
                  mutableLong.setValue(time + 60L);
                  return true;
               } else {
                  BlockPos lv = entity.getBlockPos();
                  BlockPos.Mutable lv2 = new BlockPos.Mutable();
                  ShapeContext lv3 = ShapeContext.of(entity);
                  Iterator var12 = BlockPos.iterateOutwards(lv, range, range, range).iterator();

                  while(var12.hasNext()) {
                     BlockPos lv4 = (BlockPos)var12.next();
                     if (lv4.getX() != lv.getX() || lv4.getZ() != lv.getZ()) {
                        BlockState lv5 = world.getBlockState(lv4);
                        BlockState lv6 = world.getBlockState(lv2.set(lv4, (Direction)Direction.DOWN));
                        if (!lv5.isOf(Blocks.WATER) && world.getFluidState(lv4).isEmpty() && lv5.getCollisionShape(world, lv4, lv3).isEmpty() && lv6.isSideSolidFullSquare(world, lv2, Direction.UP)) {
                           BlockPos lv7 = lv4.toImmutable();
                           lookTarget.remember((Object)(new BlockPosLookTarget(lv7)));
                           walkTarget.remember((Object)(new WalkTarget(new BlockPosLookTarget(lv7), speed, 1)));
                           break;
                        }
                     }
                  }

                  mutableLong.setValue(time + 60L);
                  return true;
               }
            };
         });
      });
   }
}
