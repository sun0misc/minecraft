package net.minecraft.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class HoeItem extends MiningToolItem {
   protected static final Map TILLING_ACTIONS;

   protected HoeItem(ToolMaterial material, int attackDamage, float attackSpeed, Item.Settings settings) {
      super((float)attackDamage, attackSpeed, material, BlockTags.HOE_MINEABLE, settings);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      Pair pair = (Pair)TILLING_ACTIONS.get(lv.getBlockState(lv2).getBlock());
      if (pair == null) {
         return ActionResult.PASS;
      } else {
         Predicate predicate = (Predicate)pair.getFirst();
         Consumer consumer = (Consumer)pair.getSecond();
         if (predicate.test(context)) {
            PlayerEntity lv3 = context.getPlayer();
            lv.playSound(lv3, lv2, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!lv.isClient) {
               consumer.accept(context);
               if (lv3 != null) {
                  context.getStack().damage(1, (LivingEntity)lv3, (Consumer)((p) -> {
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

   public static Consumer createTillAction(BlockState result) {
      return (context) -> {
         context.getWorld().setBlockState(context.getBlockPos(), result, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
         context.getWorld().emitGameEvent(GameEvent.BLOCK_CHANGE, context.getBlockPos(), GameEvent.Emitter.of(context.getPlayer(), result));
      };
   }

   public static Consumer createTillAndDropAction(BlockState result, ItemConvertible droppedItem) {
      return (context) -> {
         context.getWorld().setBlockState(context.getBlockPos(), result, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
         context.getWorld().emitGameEvent(GameEvent.BLOCK_CHANGE, context.getBlockPos(), GameEvent.Emitter.of(context.getPlayer(), result));
         Block.dropStack(context.getWorld(), context.getBlockPos(), context.getSide(), new ItemStack(droppedItem));
      };
   }

   public static boolean canTillFarmland(ItemUsageContext context) {
      return context.getSide() != Direction.DOWN && context.getWorld().getBlockState(context.getBlockPos().up()).isAir();
   }

   static {
      TILLING_ACTIONS = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Pair.of(HoeItem::canTillFarmland, createTillAction(Blocks.FARMLAND.getDefaultState())), Blocks.DIRT_PATH, Pair.of(HoeItem::canTillFarmland, createTillAction(Blocks.FARMLAND.getDefaultState())), Blocks.DIRT, Pair.of(HoeItem::canTillFarmland, createTillAction(Blocks.FARMLAND.getDefaultState())), Blocks.COARSE_DIRT, Pair.of(HoeItem::canTillFarmland, createTillAction(Blocks.DIRT.getDefaultState())), Blocks.ROOTED_DIRT, Pair.of((arg) -> {
         return true;
      }, createTillAndDropAction(Blocks.DIRT.getDefaultState(), Items.HANGING_ROOTS))));
   }
}
