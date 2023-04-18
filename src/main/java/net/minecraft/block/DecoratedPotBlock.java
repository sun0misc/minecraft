package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlock extends BlockWithEntity implements Waterloggable {
   private static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
   public static final Identifier SHARDS_NBT_KEY = new Identifier("shards");
   private static final DirectionProperty FACING;
   private static final BooleanProperty CRACKED;
   private static final BooleanProperty WATERLOGGED;

   protected DecoratedPotBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false)).with(CRACKED, false));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())).with(WATERLOGGED, lv.getFluid() == Fluids.WATER)).with(CRACKED, false);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, WATERLOGGED, CRACKED);
   }

   public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new DecoratedPotBlockEntity(pos, state);
   }

   public List getDroppedStacks(BlockState state, LootContext.Builder builder) {
      BlockEntity lv = (BlockEntity)builder.getNullable(LootContextParameters.BLOCK_ENTITY);
      if (lv instanceof DecoratedPotBlockEntity lv2) {
         builder.putDrop(SHARDS_NBT_KEY, (context, consumer) -> {
            Iterator var3 = lv2.getShards().iterator();

            while(var3.hasNext()) {
               Item lv = (Item)var3.next();
               consumer.accept(lv.getDefaultStack());
            }

         });
      }

      return super.getDroppedStacks(state, builder);
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      ItemStack lv = player.getMainHandStack();
      BlockState lv2 = state;
      if (lv.isIn(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(lv)) {
         lv2 = (BlockState)state.with(CRACKED, true);
         world.setBlockState(pos, lv2, Block.NO_REDRAW);
      }

      super.onBreak(world, pos, lv2, player);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public BlockSoundGroup getSoundGroup(BlockState state) {
      return (Boolean)state.get(CRACKED) ? BlockSoundGroup.DECORATED_POT_SHATTER : BlockSoundGroup.DECORATED_POT;
   }

   static {
      FACING = Properties.HORIZONTAL_FACING;
      CRACKED = Properties.CRACKED;
      WATERLOGGED = Properties.WATERLOGGED;
   }
}
