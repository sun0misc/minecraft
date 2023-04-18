package net.minecraft.entity.ai.brain.task;

import java.util.Iterator;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.mutable.MutableLong;

public class WalkTowardsWaterTask {
   public static Task create(int range, float speed) {
      MutableLong mutableLong = new MutableLong(0L);
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (attackTarget, walkTarget, lookTarget) -> {
            return (world, entity, time) -> {
               if (world.getFluidState(entity.getBlockPos()).isIn(FluidTags.WATER)) {
                  return false;
               } else if (time < mutableLong.getValue()) {
                  mutableLong.setValue(time + 40L);
                  return true;
               } else {
                  ShapeContext lv = ShapeContext.of(entity);
                  BlockPos lv2 = entity.getBlockPos();
                  BlockPos.Mutable lv3 = new BlockPos.Mutable();
                  Iterator var12 = BlockPos.iterateOutwards(lv2, range, range, range).iterator();

                  label45:
                  while(var12.hasNext()) {
                     BlockPos lv4 = (BlockPos)var12.next();
                     if ((lv4.getX() != lv2.getX() || lv4.getZ() != lv2.getZ()) && world.getBlockState(lv4).getCollisionShape(world, lv4, lv).isEmpty() && !world.getBlockState(lv3.set(lv4, (Direction)Direction.DOWN)).getCollisionShape(world, lv4, lv).isEmpty()) {
                        Iterator var14 = Direction.Type.HORIZONTAL.iterator();

                        while(var14.hasNext()) {
                           Direction lv5 = (Direction)var14.next();
                           lv3.set(lv4, (Direction)lv5);
                           if (world.getBlockState(lv3).isAir() && world.getBlockState(lv3.move(Direction.DOWN)).isOf(Blocks.WATER)) {
                              lookTarget.remember((Object)(new BlockPosLookTarget(lv4)));
                              walkTarget.remember((Object)(new WalkTarget(new BlockPosLookTarget(lv4), speed, 0)));
                              break label45;
                           }
                        }
                     }
                  }

                  mutableLong.setValue(time + 40L);
                  return true;
               }
            };
         });
      });
   }
}
