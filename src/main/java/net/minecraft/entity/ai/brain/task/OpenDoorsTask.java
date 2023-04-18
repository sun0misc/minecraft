package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.MemoryQueryResult;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class OpenDoorsTask {
   private static final int RUN_TIME = 20;
   private static final double PATHING_DISTANCE = 3.0;
   private static final double REACH_DISTANCE = 2.0;

   public static Task create() {
      MutableObject mutableObject = new MutableObject((Object)null);
      MutableInt mutableInt = new MutableInt(0);
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryValue(MemoryModuleType.PATH), context.queryMemoryOptional(MemoryModuleType.DOORS_TO_CLOSE), context.queryMemoryOptional(MemoryModuleType.MOBS)).apply(context, (path, doorsToClose, mobs) -> {
            return (world, entity, time) -> {
               Path lv = (Path)context.getValue(path);
               Optional optional = context.getOptionalValue(doorsToClose);
               if (!lv.isStart() && !lv.isFinished()) {
                  if (Objects.equals(mutableObject.getValue(), lv.getCurrentNode())) {
                     mutableInt.setValue(20);
                  } else if (mutableInt.decrementAndGet() > 0) {
                     return false;
                  }

                  mutableObject.setValue(lv.getCurrentNode());
                  PathNode lv2 = lv.getLastNode();
                  PathNode lv3 = lv.getCurrentNode();
                  BlockPos lv4 = lv2.getBlockPos();
                  BlockState lv5 = world.getBlockState(lv4);
                  if (lv5.isIn(BlockTags.WOODEN_DOORS, (state) -> {
                     return state.getBlock() instanceof DoorBlock;
                  })) {
                     DoorBlock lv6 = (DoorBlock)lv5.getBlock();
                     if (!lv6.isOpen(lv5)) {
                        lv6.setOpen(entity, world, lv5, lv4, true);
                     }

                     optional = storePos(doorsToClose, optional, world, lv4);
                  }

                  BlockPos lv7 = lv3.getBlockPos();
                  BlockState lv8 = world.getBlockState(lv7);
                  if (lv8.isIn(BlockTags.WOODEN_DOORS, (state) -> {
                     return state.getBlock() instanceof DoorBlock;
                  })) {
                     DoorBlock lv9 = (DoorBlock)lv8.getBlock();
                     if (!lv9.isOpen(lv8)) {
                        lv9.setOpen(entity, world, lv8, lv7, true);
                        optional = storePos(doorsToClose, optional, world, lv7);
                     }
                  }

                  optional.ifPresent((doors) -> {
                     pathToDoor(world, entity, lv2, lv3, doors, context.getOptionalValue(mobs));
                  });
                  return true;
               } else {
                  return false;
               }
            };
         });
      });
   }

   public static void pathToDoor(ServerWorld world, LivingEntity entity, @Nullable PathNode lastNode, @Nullable PathNode currentNode, Set doors, Optional otherMobs) {
      Iterator iterator = doors.iterator();

      while(true) {
         GlobalPos lv;
         BlockPos lv2;
         do {
            do {
               if (!iterator.hasNext()) {
                  return;
               }

               lv = (GlobalPos)iterator.next();
               lv2 = lv.getPos();
            } while(lastNode != null && lastNode.getBlockPos().equals(lv2));
         } while(currentNode != null && currentNode.getBlockPos().equals(lv2));

         if (cannotReachDoor(world, entity, lv)) {
            iterator.remove();
         } else {
            BlockState lv3 = world.getBlockState(lv2);
            if (!lv3.isIn(BlockTags.WOODEN_DOORS, (state) -> {
               return state.getBlock() instanceof DoorBlock;
            })) {
               iterator.remove();
            } else {
               DoorBlock lv4 = (DoorBlock)lv3.getBlock();
               if (!lv4.isOpen(lv3)) {
                  iterator.remove();
               } else if (hasOtherMobReachedDoor(entity, lv2, otherMobs)) {
                  iterator.remove();
               } else {
                  lv4.setOpen(entity, world, lv3, lv2, false);
                  iterator.remove();
               }
            }
         }
      }
   }

   private static boolean hasOtherMobReachedDoor(LivingEntity entity, BlockPos pos, Optional otherMobs) {
      return otherMobs.isEmpty() ? false : ((List)otherMobs.get()).stream().filter((mob) -> {
         return mob.getType() == entity.getType();
      }).filter((mob) -> {
         return pos.isWithinDistance(mob.getPos(), 2.0);
      }).anyMatch((mob) -> {
         return hasReached(mob.getBrain(), pos);
      });
   }

   private static boolean hasReached(Brain brain, BlockPos pos) {
      if (!brain.hasMemoryModule(MemoryModuleType.PATH)) {
         return false;
      } else {
         Path lv = (Path)brain.getOptionalRegisteredMemory(MemoryModuleType.PATH).get();
         if (lv.isFinished()) {
            return false;
         } else {
            PathNode lv2 = lv.getLastNode();
            if (lv2 == null) {
               return false;
            } else {
               PathNode lv3 = lv.getCurrentNode();
               return pos.equals(lv2.getBlockPos()) || pos.equals(lv3.getBlockPos());
            }
         }
      }
   }

   private static boolean cannotReachDoor(ServerWorld world, LivingEntity entity, GlobalPos doorPos) {
      return doorPos.getDimension() != world.getRegistryKey() || !doorPos.getPos().isWithinDistance(entity.getPos(), 3.0);
   }

   private static Optional storePos(MemoryQueryResult queryResult, Optional doors, ServerWorld world, BlockPos pos) {
      GlobalPos lv = GlobalPos.create(world.getRegistryKey(), pos);
      return Optional.of((Set)doors.map((doorSet) -> {
         doorSet.add(lv);
         return doorSet;
      }).orElseGet(() -> {
         Set set = Sets.newHashSet(new GlobalPos[]{lv});
         queryResult.remember((Object)set);
         return set;
      }));
   }
}
