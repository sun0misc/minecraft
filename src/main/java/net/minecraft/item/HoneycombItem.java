package net.minecraft.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class HoneycombItem extends Item implements SignChangingItem {
   public static final Supplier UNWAXED_TO_WAXED_BLOCKS = Suppliers.memoize(() -> {
      return ImmutableBiMap.builder().put(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK).put(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER).put(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER).put(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER).put(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER).put(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER).put(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER).put(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER).put(Blocks.CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER_SLAB).put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB).put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB).put(Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB).put(Blocks.CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER_STAIRS).put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS).put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS).put(Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS).build();
   });
   public static final Supplier WAXED_TO_UNWAXED_BLOCKS = Suppliers.memoize(() -> {
      return ((BiMap)UNWAXED_TO_WAXED_BLOCKS.get()).inverse();
   });

   public HoneycombItem(Item.Settings arg) {
      super(arg);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      return (ActionResult)getWaxedState(lv3).map((state) -> {
         PlayerEntity lvx = context.getPlayer();
         ItemStack lv2x = context.getStack();
         if (lvx instanceof ServerPlayerEntity) {
            Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)lvx, lv2, lv2x);
         }

         lv2x.decrement(1);
         lv.setBlockState(lv2, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
         lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(lvx, state));
         lv.syncWorldEvent(lvx, WorldEvents.BLOCK_WAXED, lv2, 0);
         return ActionResult.success(lv.isClient);
      }).orElse(ActionResult.PASS);
   }

   public static Optional getWaxedState(BlockState state) {
      return Optional.ofNullable((Block)((BiMap)UNWAXED_TO_WAXED_BLOCKS.get()).get(state.getBlock())).map((block) -> {
         return block.getStateWithProperties(state);
      });
   }

   public boolean useOnSign(World world, SignBlockEntity signBlockEntity, boolean front, PlayerEntity player) {
      if (signBlockEntity.setWaxed(true)) {
         world.syncWorldEvent((PlayerEntity)null, WorldEvents.BLOCK_WAXED, signBlockEntity.getPos(), 0);
         return true;
      } else {
         return false;
      }
   }

   public boolean canUseOnSignText(SignText signText, PlayerEntity player) {
      return true;
   }
}
