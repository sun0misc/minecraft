package net.minecraft.block.dispenser;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ShearsDispenserBehavior extends FallibleItemDispenserBehavior {
   protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
      World lv = pointer.getWorld();
      if (!lv.isClient()) {
         BlockPos lv2 = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
         this.setSuccess(tryShearBlock((ServerWorld)lv, lv2) || tryShearEntity((ServerWorld)lv, lv2));
         if (this.isSuccess() && stack.damage(1, (Random)lv.getRandom(), (ServerPlayerEntity)null)) {
            stack.setCount(0);
         }
      }

      return stack;
   }

   private static boolean tryShearBlock(ServerWorld world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      if (lv.isIn(BlockTags.BEEHIVES, (state) -> {
         return state.contains(BeehiveBlock.HONEY_LEVEL) && state.getBlock() instanceof BeehiveBlock;
      })) {
         int i = (Integer)lv.get(BeehiveBlock.HONEY_LEVEL);
         if (i >= 5) {
            world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
            BeehiveBlock.dropHoneycomb(world, pos);
            ((BeehiveBlock)lv.getBlock()).takeHoney(world, lv, pos, (PlayerEntity)null, BeehiveBlockEntity.BeeState.BEE_RELEASED);
            world.emitGameEvent((Entity)null, GameEvent.SHEAR, pos);
            return true;
         }
      }

      return false;
   }

   private static boolean tryShearEntity(ServerWorld world, BlockPos pos) {
      List list = world.getEntitiesByClass(LivingEntity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR);
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         LivingEntity lv = (LivingEntity)var3.next();
         if (lv instanceof Shearable lv2) {
            if (lv2.isShearable()) {
               lv2.sheared(SoundCategory.BLOCKS);
               world.emitGameEvent((Entity)null, GameEvent.SHEAR, pos);
               return true;
            }
         }
      }

      return false;
   }
}
