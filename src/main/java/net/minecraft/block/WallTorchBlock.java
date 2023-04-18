package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
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

public class WallTorchBlock extends TorchBlock {
   public static final DirectionProperty FACING;
   protected static final float field_31285 = 2.5F;
   private static final Map BOUNDING_SHAPES;

   protected WallTorchBlock(AbstractBlock.Settings arg, ParticleEffect arg2) {
      super(arg, arg2);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
   }

   public String getTranslationKey() {
      return this.asItem().getTranslationKey();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return getBoundingShape(state);
   }

   public static VoxelShape getBoundingShape(BlockState state) {
      return (VoxelShape)BOUNDING_SHAPES.get(state.get(FACING));
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      Direction lv = (Direction)state.get(FACING);
      BlockPos lv2 = pos.offset(lv.getOpposite());
      BlockState lv3 = world.getBlockState(lv2);
      return lv3.isSideSolidFullSquare(world, lv2, lv);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = this.getDefaultState();
      WorldView lv2 = ctx.getWorld();
      BlockPos lv3 = ctx.getBlockPos();
      Direction[] lvs = ctx.getPlacementDirections();
      Direction[] var6 = lvs;
      int var7 = lvs.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction lv4 = var6[var8];
         if (lv4.getAxis().isHorizontal()) {
            Direction lv5 = lv4.getOpposite();
            lv = (BlockState)lv.with(FACING, lv5);
            if (lv.canPlaceAt(lv2, lv3)) {
               return lv;
            }
         }
      }

      return null;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      Direction lv = (Direction)state.get(FACING);
      double d = (double)pos.getX() + 0.5;
      double e = (double)pos.getY() + 0.7;
      double f = (double)pos.getZ() + 0.5;
      double g = 0.22;
      double h = 0.27;
      Direction lv2 = lv.getOpposite();
      world.addParticle(ParticleTypes.SMOKE, d + 0.27 * (double)lv2.getOffsetX(), e + 0.22, f + 0.27 * (double)lv2.getOffsetZ(), 0.0, 0.0, 0.0);
      world.addParticle(this.particle, d + 0.27 * (double)lv2.getOffsetX(), e + 0.22, f + 0.27 * (double)lv2.getOffsetZ(), 0.0, 0.0, 0.0);
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
      BOUNDING_SHAPES = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.createCuboidShape(5.5, 3.0, 11.0, 10.5, 13.0, 16.0), Direction.SOUTH, Block.createCuboidShape(5.5, 3.0, 0.0, 10.5, 13.0, 5.0), Direction.WEST, Block.createCuboidShape(11.0, 3.0, 5.5, 16.0, 13.0, 10.5), Direction.EAST, Block.createCuboidShape(0.0, 3.0, 5.5, 5.0, 13.0, 10.5)));
   }
}
