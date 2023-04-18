package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class WallSkullBlock extends AbstractSkullBlock {
   public static final DirectionProperty FACING;
   private static final Map FACING_TO_SHAPE;

   protected WallSkullBlock(SkullBlock.SkullType arg, AbstractBlock.Settings arg2) {
      super(arg, arg2);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
   }

   public String getTranslationKey() {
      return this.asItem().getTranslationKey();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)FACING_TO_SHAPE.get(state.get(FACING));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = this.getDefaultState();
      BlockView lv2 = ctx.getWorld();
      BlockPos lv3 = ctx.getBlockPos();
      Direction[] lvs = ctx.getPlacementDirections();
      Direction[] var6 = lvs;
      int var7 = lvs.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction lv4 = var6[var8];
         if (lv4.getAxis().isHorizontal()) {
            Direction lv5 = lv4.getOpposite();
            lv = (BlockState)lv.with(FACING, lv5);
            if (!lv2.getBlockState(lv3.offset(lv4)).canReplace(ctx)) {
               return lv;
            }
         }
      }

      return null;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING);
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      FACING_TO_SHAPE = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.createCuboidShape(4.0, 4.0, 8.0, 12.0, 12.0, 16.0), Direction.SOUTH, Block.createCuboidShape(4.0, 4.0, 0.0, 12.0, 12.0, 8.0), Direction.EAST, Block.createCuboidShape(0.0, 4.0, 4.0, 8.0, 12.0, 12.0), Direction.WEST, Block.createCuboidShape(8.0, 4.0, 4.0, 16.0, 12.0, 12.0)));
   }
}
