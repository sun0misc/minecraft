package net.minecraft.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class AxeItem extends MiningToolItem {
   protected static final Map STRIPPED_BLOCKS;

   protected AxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
      super(attackDamage, attackSpeed, material, BlockTags.AXE_MINEABLE, settings);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      PlayerEntity lv3 = context.getPlayer();
      BlockState lv4 = lv.getBlockState(lv2);
      Optional optional = this.getStrippedState(lv4);
      Optional optional2 = Oxidizable.getDecreasedOxidationState(lv4);
      Optional optional3 = Optional.ofNullable((Block)((BiMap)HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get()).get(lv4.getBlock())).map((block) -> {
         return block.getStateWithProperties(lv4);
      });
      ItemStack lv5 = context.getStack();
      Optional optional4 = Optional.empty();
      if (optional.isPresent()) {
         lv.playSound(lv3, lv2, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
         optional4 = optional;
      } else if (optional2.isPresent()) {
         lv.playSound(lv3, lv2, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
         lv.syncWorldEvent(lv3, WorldEvents.BLOCK_SCRAPED, lv2, 0);
         optional4 = optional2;
      } else if (optional3.isPresent()) {
         lv.playSound(lv3, lv2, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
         lv.syncWorldEvent(lv3, WorldEvents.WAX_REMOVED, lv2, 0);
         optional4 = optional3;
      }

      if (optional4.isPresent()) {
         if (lv3 instanceof ServerPlayerEntity) {
            Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)lv3, lv2, lv5);
         }

         lv.setBlockState(lv2, (BlockState)optional4.get(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
         lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(lv3, (BlockState)optional4.get()));
         if (lv3 != null) {
            lv5.damage(1, (LivingEntity)lv3, (Consumer)((p) -> {
               p.sendToolBreakStatus(context.getHand());
            }));
         }

         return ActionResult.success(lv.isClient);
      } else {
         return ActionResult.PASS;
      }
   }

   private Optional getStrippedState(BlockState state) {
      return Optional.ofNullable((Block)STRIPPED_BLOCKS.get(state.getBlock())).map((block) -> {
         return (BlockState)block.getDefaultState().with(PillarBlock.AXIS, (Direction.Axis)state.get(PillarBlock.AXIS));
      });
   }

   static {
      STRIPPED_BLOCKS = (new ImmutableMap.Builder()).put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD).put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG).put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD).put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG).put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD).put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG).put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD).put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG).put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD).put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG).put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD).put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG).put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD).put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG).put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM).put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE).put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM).put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE).put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD).put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG).put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK).build();
   }
}
