package net.minecraft.item;

import java.util.function.Consumer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ShearsItem extends Item {
   public ShearsItem(Item.Settings arg) {
      super(arg);
   }

   public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
      if (!world.isClient && !state.isIn(BlockTags.FIRE)) {
         stack.damage(1, (LivingEntity)miner, (Consumer)((e) -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
         }));
      }

      return !state.isIn(BlockTags.LEAVES) && !state.isOf(Blocks.COBWEB) && !state.isOf(Blocks.GRASS) && !state.isOf(Blocks.FERN) && !state.isOf(Blocks.DEAD_BUSH) && !state.isOf(Blocks.HANGING_ROOTS) && !state.isOf(Blocks.VINE) && !state.isOf(Blocks.TRIPWIRE) && !state.isIn(BlockTags.WOOL) ? super.postMine(stack, world, state, pos, miner) : true;
   }

   public boolean isSuitableFor(BlockState state) {
      return state.isOf(Blocks.COBWEB) || state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(Blocks.TRIPWIRE);
   }

   public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
      if (!state.isOf(Blocks.COBWEB) && !state.isIn(BlockTags.LEAVES)) {
         if (state.isIn(BlockTags.WOOL)) {
            return 5.0F;
         } else {
            return !state.isOf(Blocks.VINE) && !state.isOf(Blocks.GLOW_LICHEN) ? super.getMiningSpeedMultiplier(stack, state) : 2.0F;
         }
      } else {
         return 15.0F;
      }
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      Block lv4 = lv3.getBlock();
      if (lv4 instanceof AbstractPlantStemBlock lv5) {
         if (!lv5.hasMaxAge(lv3)) {
            PlayerEntity lv6 = context.getPlayer();
            ItemStack lv7 = context.getStack();
            if (lv6 instanceof ServerPlayerEntity) {
               Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)lv6, lv2, lv7);
            }

            lv.playSound(lv6, lv2, SoundEvents.BLOCK_GROWING_PLANT_CROP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            BlockState lv8 = lv5.withMaxAge(lv3);
            lv.setBlockState(lv2, lv8);
            lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(context.getPlayer(), lv8));
            if (lv6 != null) {
               lv7.damage(1, (LivingEntity)lv6, (Consumer)((player) -> {
                  player.sendToolBreakStatus(context.getHand());
               }));
            }

            return ActionResult.success(lv.isClient);
         }
      }

      return super.useOnBlock(context);
   }
}
