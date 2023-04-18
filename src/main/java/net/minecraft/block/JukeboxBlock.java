package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class JukeboxBlock extends BlockWithEntity {
   public static final BooleanProperty HAS_RECORD;

   protected JukeboxBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HAS_RECORD, false));
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      super.onPlaced(world, pos, state, placer, itemStack);
      NbtCompound lv = BlockItem.getBlockEntityNbt(itemStack);
      if (lv != null && lv.contains("RecordItem")) {
         world.setBlockState(pos, (BlockState)state.with(HAS_RECORD, true), Block.NOTIFY_LISTENERS);
      }

   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if ((Boolean)state.get(HAS_RECORD)) {
         BlockEntity var8 = world.getBlockEntity(pos);
         if (var8 instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity lv = (JukeboxBlockEntity)var8;
            lv.dropRecord();
            return ActionResult.success(world.isClient);
         }
      }

      return ActionResult.PASS;
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity var7 = world.getBlockEntity(pos);
         if (var7 instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity lv = (JukeboxBlockEntity)var7;
            lv.dropRecord();
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new JukeboxBlockEntity(pos, state);
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      BlockEntity var6 = world.getBlockEntity(pos);
      if (var6 instanceof JukeboxBlockEntity lv) {
         if (lv.isPlayingRecord()) {
            return 15;
         }
      }

      return 0;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      BlockEntity var6 = world.getBlockEntity(pos);
      if (var6 instanceof JukeboxBlockEntity lv) {
         Item var7 = lv.getStack().getItem();
         if (var7 instanceof MusicDiscItem lv2) {
            return lv2.getComparatorOutput();
         }
      }

      return 0;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(HAS_RECORD);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return (Boolean)state.get(HAS_RECORD) ? checkType(type, BlockEntityType.JUKEBOX, JukeboxBlockEntity::tick) : null;
   }

   static {
      HAS_RECORD = Properties.HAS_RECORD;
   }
}
