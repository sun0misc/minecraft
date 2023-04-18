package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CalibratedSculkSensorBlock extends SculkSensorBlock {
   public static final DirectionProperty FACING;

   public CalibratedSculkSensorBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)this.getDefaultState().with(FACING, Direction.NORTH));
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new CalibratedSculkSensorBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return !world.isClient ? checkType(type, BlockEntityType.CALIBRATED_SCULK_SENSOR, (worldx, pos, statex, blockEntity) -> {
         blockEntity.getEventListener().tick(worldx);
      }) : null;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)super.getPlacementState(ctx).with(FACING, ctx.getHorizontalPlayerFacing());
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return direction != state.get(FACING) ? super.getWeakRedstonePower(state, world, pos, direction) : 0;
   }

   protected void appendProperties(StateManager.Builder builder) {
      super.appendProperties(builder);
      builder.add(FACING);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   public int getCooldownTime() {
      return 20;
   }

   static {
      FACING = Properties.HORIZONTAL_FACING;
   }
}
