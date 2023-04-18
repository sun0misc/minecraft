package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class WallRedstoneTorchBlock extends RedstoneTorchBlock {
   public static final DirectionProperty FACING;
   public static final BooleanProperty LIT;

   protected WallRedstoneTorchBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(LIT, true));
   }

   public String getTranslationKey() {
      return this.asItem().getTranslationKey();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return WallTorchBlock.getBoundingShape(state);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return Blocks.WALL_TORCH.canPlaceAt(state, world, pos);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return Blocks.WALL_TORCH.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = Blocks.WALL_TORCH.getPlacementState(ctx);
      return lv == null ? null : (BlockState)this.getDefaultState().with(FACING, (Direction)lv.get(FACING));
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         Direction lv = ((Direction)state.get(FACING)).getOpposite();
         double d = 0.27;
         double e = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2 + 0.27 * (double)lv.getOffsetX();
         double f = (double)pos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.2 + 0.22;
         double g = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2 + 0.27 * (double)lv.getOffsetZ();
         world.addParticle(this.particle, e, f, g, 0.0, 0.0, 0.0);
      }
   }

   protected boolean shouldUnpower(World world, BlockPos pos, BlockState state) {
      Direction lv = ((Direction)state.get(FACING)).getOpposite();
      return world.isEmittingRedstonePower(pos.offset(lv), lv);
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(LIT) && state.get(FACING) != direction ? 15 : 0;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return Blocks.WALL_TORCH.rotate(state, rotation);
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return Blocks.WALL_TORCH.mirror(state, mirror);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, LIT);
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      LIT = RedstoneTorchBlock.LIT;
   }
}
