package net.minecraft.entity.ai.brain.task;

import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;

public class LayFrogSpawnTask {
   public static Task create(Block frogSpawn) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryValue(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(MemoryModuleType.IS_PREGNANT)).apply(context, (attackTarget, walkTarget, isPregnant) -> {
            return (world, entity, time) -> {
               if (!entity.isTouchingWater() && entity.isOnGround()) {
                  BlockPos lv = entity.getBlockPos().down();
                  Iterator var7 = Direction.Type.HORIZONTAL.iterator();

                  while(var7.hasNext()) {
                     Direction lv2 = (Direction)var7.next();
                     BlockPos lv3 = lv.offset(lv2);
                     if (world.getBlockState(lv3).getCollisionShape(world, lv3).getFace(Direction.UP).isEmpty() && world.getFluidState(lv3).isOf(Fluids.WATER)) {
                        BlockPos lv4 = lv3.up();
                        if (world.getBlockState(lv4).isAir()) {
                           BlockState lv5 = frogSpawn.getDefaultState();
                           world.setBlockState(lv4, lv5, Block.NOTIFY_ALL);
                           world.emitGameEvent(GameEvent.BLOCK_PLACE, lv4, GameEvent.Emitter.of(entity, lv5));
                           world.playSoundFromEntity((PlayerEntity)null, entity, SoundEvents.ENTITY_FROG_LAY_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                           isPregnant.forget();
                           return true;
                        }
                     }
                  }

                  return true;
               } else {
                  return false;
               }
            };
         });
      });
   }
}
