package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CandleCakeBlock extends AbstractCandleBlock {
   public static final BooleanProperty LIT;
   protected static final float field_31052 = 1.0F;
   protected static final VoxelShape CAKE_SHAPE;
   protected static final VoxelShape CANDLE_SHAPE;
   protected static final VoxelShape SHAPE;
   private static final Map CANDLES_TO_CANDLE_CAKES;
   private static final Iterable PARTICLE_OFFSETS;

   protected CandleCakeBlock(Block candle, AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LIT, false));
      CANDLES_TO_CANDLE_CAKES.put(candle, this);
   }

   protected Iterable getParticleOffsets(BlockState state) {
      return PARTICLE_OFFSETS;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack lv = player.getStackInHand(hand);
      if (!lv.isOf(Items.FLINT_AND_STEEL) && !lv.isOf(Items.FIRE_CHARGE)) {
         if (isHittingCandle(hit) && player.getStackInHand(hand).isEmpty() && (Boolean)state.get(LIT)) {
            extinguish(player, state, world, pos);
            return ActionResult.success(world.isClient);
         } else {
            ActionResult lv2 = CakeBlock.tryEat(world, pos, Blocks.CAKE.getDefaultState(), player);
            if (lv2.isAccepted()) {
               dropStacks(state, world, pos);
            }

            return lv2;
         }
      } else {
         return ActionResult.PASS;
      }
   }

   private static boolean isHittingCandle(BlockHitResult hitResult) {
      return hitResult.getPos().y - (double)hitResult.getBlockPos().getY() > 0.5;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(LIT);
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(Blocks.CAKE);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos.down()).getMaterial().isSolid();
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return CakeBlock.DEFAULT_COMPARATOR_OUTPUT;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public static BlockState getCandleCakeFromCandle(Block candle) {
      return ((CandleCakeBlock)CANDLES_TO_CANDLE_CAKES.get(candle)).getDefaultState();
   }

   public static boolean canBeLit(BlockState state) {
      return state.isIn(BlockTags.CANDLE_CAKES, (statex) -> {
         return statex.contains(LIT) && !(Boolean)state.get(LIT);
      });
   }

   static {
      LIT = AbstractCandleBlock.LIT;
      CAKE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 8.0, 15.0);
      CANDLE_SHAPE = Block.createCuboidShape(7.0, 8.0, 7.0, 9.0, 14.0, 9.0);
      SHAPE = VoxelShapes.union(CAKE_SHAPE, CANDLE_SHAPE);
      CANDLES_TO_CANDLE_CAKES = Maps.newHashMap();
      PARTICLE_OFFSETS = ImmutableList.of(new Vec3d(0.5, 1.0, 0.5));
   }
}
