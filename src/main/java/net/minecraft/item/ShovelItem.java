package net.minecraft.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class ShovelItem extends MiningToolItem {
   protected static final Map PATH_STATES;

   public ShovelItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
      super(attackDamage, attackSpeed, material, BlockTags.SHOVEL_MINEABLE, settings);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      if (context.getSide() == Direction.DOWN) {
         return ActionResult.PASS;
      } else {
         PlayerEntity lv4 = context.getPlayer();
         BlockState lv5 = (BlockState)PATH_STATES.get(lv3.getBlock());
         BlockState lv6 = null;
         if (lv5 != null && lv.getBlockState(lv2.up()).isAir()) {
            lv.playSound(lv4, lv2, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            lv6 = lv5;
         } else if (lv3.getBlock() instanceof CampfireBlock && (Boolean)lv3.get(CampfireBlock.LIT)) {
            if (!lv.isClient()) {
               lv.syncWorldEvent((PlayerEntity)null, WorldEvents.FIRE_EXTINGUISHED, lv2, 0);
            }

            CampfireBlock.extinguish(context.getPlayer(), lv, lv2, lv3);
            lv6 = (BlockState)lv3.with(CampfireBlock.LIT, false);
         }

         if (lv6 != null) {
            if (!lv.isClient) {
               lv.setBlockState(lv2, lv6, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
               lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(lv4, lv6));
               if (lv4 != null) {
                  context.getStack().damage(1, (LivingEntity)lv4, (Consumer)((p) -> {
                     p.sendToolBreakStatus(context.getHand());
                  }));
               }
            }

            return ActionResult.success(lv.isClient);
         } else {
            return ActionResult.PASS;
         }
      }
   }

   static {
      PATH_STATES = Maps.newHashMap((new ImmutableMap.Builder()).put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.DIRT, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.PODZOL, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.COARSE_DIRT, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.MYCELIUM, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.ROOTED_DIRT, Blocks.DIRT_PATH.getDefaultState()).build());
   }
}
