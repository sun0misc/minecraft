package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.GameRules;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class FarmerVillagerTask extends MultiTickTask {
   private static final int MAX_RUN_TIME = 200;
   public static final float WALK_SPEED = 0.5F;
   @Nullable
   private BlockPos currentTarget;
   private long nextResponseTime;
   private int ticksRan;
   private final List targetPositions = Lists.newArrayList();

   public FarmerVillagerTask() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleState.VALUE_PRESENT));
   }

   protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
      if (!arg.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
         return false;
      } else if (arg2.getVillagerData().getProfession() != VillagerProfession.FARMER) {
         return false;
      } else {
         BlockPos.Mutable lv = arg2.getBlockPos().mutableCopy();
         this.targetPositions.clear();

         for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
               for(int k = -1; k <= 1; ++k) {
                  lv.set(arg2.getX() + (double)i, arg2.getY() + (double)j, arg2.getZ() + (double)k);
                  if (this.isSuitableTarget(lv, arg)) {
                     this.targetPositions.add(new BlockPos(lv));
                  }
               }
            }
         }

         this.currentTarget = this.chooseRandomTarget(arg);
         return this.currentTarget != null;
      }
   }

   @Nullable
   private BlockPos chooseRandomTarget(ServerWorld world) {
      return this.targetPositions.isEmpty() ? null : (BlockPos)this.targetPositions.get(world.getRandom().nextInt(this.targetPositions.size()));
   }

   private boolean isSuitableTarget(BlockPos pos, ServerWorld world) {
      BlockState lv = world.getBlockState(pos);
      Block lv2 = lv.getBlock();
      Block lv3 = world.getBlockState(pos.down()).getBlock();
      return lv2 instanceof CropBlock && ((CropBlock)lv2).isMature(lv) || lv.isAir() && lv3 instanceof FarmlandBlock;
   }

   protected void run(ServerWorld arg, VillagerEntity arg2, long l) {
      if (l > this.nextResponseTime && this.currentTarget != null) {
         arg2.getBrain().remember(MemoryModuleType.LOOK_TARGET, (Object)(new BlockPosLookTarget(this.currentTarget)));
         arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(new BlockPosLookTarget(this.currentTarget), 0.5F, 1)));
      }

   }

   protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      arg2.getBrain().forget(MemoryModuleType.LOOK_TARGET);
      arg2.getBrain().forget(MemoryModuleType.WALK_TARGET);
      this.ticksRan = 0;
      this.nextResponseTime = l + 40L;
   }

   protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      if (this.currentTarget == null || this.currentTarget.isWithinDistance(arg2.getPos(), 1.0)) {
         if (this.currentTarget != null && l > this.nextResponseTime) {
            BlockState lv = arg.getBlockState(this.currentTarget);
            Block lv2 = lv.getBlock();
            Block lv3 = arg.getBlockState(this.currentTarget.down()).getBlock();
            if (lv2 instanceof CropBlock && ((CropBlock)lv2).isMature(lv)) {
               arg.breakBlock(this.currentTarget, true, arg2);
            }

            if (lv.isAir() && lv3 instanceof FarmlandBlock && arg2.hasSeedToPlant()) {
               SimpleInventory lv4 = arg2.getInventory();

               for(int i = 0; i < lv4.size(); ++i) {
                  ItemStack lv5 = lv4.getStack(i);
                  boolean bl = false;
                  if (!lv5.isEmpty()) {
                     BlockState lv6;
                     if (lv5.isOf(Items.WHEAT_SEEDS)) {
                        lv6 = Blocks.WHEAT.getDefaultState();
                        arg.setBlockState(this.currentTarget, lv6);
                        arg.emitGameEvent(GameEvent.BLOCK_PLACE, this.currentTarget, GameEvent.Emitter.of(arg2, lv6));
                        bl = true;
                     } else if (lv5.isOf(Items.POTATO)) {
                        lv6 = Blocks.POTATOES.getDefaultState();
                        arg.setBlockState(this.currentTarget, lv6);
                        arg.emitGameEvent(GameEvent.BLOCK_PLACE, this.currentTarget, GameEvent.Emitter.of(arg2, lv6));
                        bl = true;
                     } else if (lv5.isOf(Items.CARROT)) {
                        lv6 = Blocks.CARROTS.getDefaultState();
                        arg.setBlockState(this.currentTarget, lv6);
                        arg.emitGameEvent(GameEvent.BLOCK_PLACE, this.currentTarget, GameEvent.Emitter.of(arg2, lv6));
                        bl = true;
                     } else if (lv5.isOf(Items.BEETROOT_SEEDS)) {
                        lv6 = Blocks.BEETROOTS.getDefaultState();
                        arg.setBlockState(this.currentTarget, lv6);
                        arg.emitGameEvent(GameEvent.BLOCK_PLACE, this.currentTarget, GameEvent.Emitter.of(arg2, lv6));
                        bl = true;
                     }
                  }

                  if (bl) {
                     arg.playSound((PlayerEntity)null, (double)this.currentTarget.getX(), (double)this.currentTarget.getY(), (double)this.currentTarget.getZ(), SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                     lv5.decrement(1);
                     if (lv5.isEmpty()) {
                        lv4.setStack(i, ItemStack.EMPTY);
                     }
                     break;
                  }
               }
            }

            if (lv2 instanceof CropBlock && !((CropBlock)lv2).isMature(lv)) {
               this.targetPositions.remove(this.currentTarget);
               this.currentTarget = this.chooseRandomTarget(arg);
               if (this.currentTarget != null) {
                  this.nextResponseTime = l + 20L;
                  arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(new BlockPosLookTarget(this.currentTarget), 0.5F, 1)));
                  arg2.getBrain().remember(MemoryModuleType.LOOK_TARGET, (Object)(new BlockPosLookTarget(this.currentTarget)));
               }
            }
         }

         ++this.ticksRan;
      }
   }

   protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      return this.ticksRan < 200;
   }

   // $FF: synthetic method
   protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
      return this.shouldKeepRunning(world, (VillagerEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (VillagerEntity)entity, time);
   }
}
