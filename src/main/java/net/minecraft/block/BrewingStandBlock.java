package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BrewingStandBlock extends BlockWithEntity {
   public static final BooleanProperty[] BOTTLE_PROPERTIES;
   protected static final VoxelShape SHAPE;

   public BrewingStandBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(BOTTLE_PROPERTIES[0], false)).with(BOTTLE_PROPERTIES[1], false)).with(BOTTLE_PROPERTIES[2], false));
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new BrewingStandBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return world.isClient ? null : checkType(type, BlockEntityType.BREWING_STAND, BrewingStandBlockEntity::tick);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof BrewingStandBlockEntity) {
            player.openHandledScreen((BrewingStandBlockEntity)lv);
            player.incrementStat(Stats.INTERACT_WITH_BREWINGSTAND);
         }

         return ActionResult.CONSUME;
      }
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof BrewingStandBlockEntity) {
            ((BrewingStandBlockEntity)lv).setCustomName(itemStack.getName());
         }
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      double d = (double)pos.getX() + 0.4 + (double)random.nextFloat() * 0.2;
      double e = (double)pos.getY() + 0.7 + (double)random.nextFloat() * 0.3;
      double f = (double)pos.getZ() + 0.4 + (double)random.nextFloat() * 0.2;
      world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof BrewingStandBlockEntity) {
            ItemScatterer.spawn(world, (BlockPos)pos, (Inventory)((BrewingStandBlockEntity)lv));
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(BOTTLE_PROPERTIES[0], BOTTLE_PROPERTIES[1], BOTTLE_PROPERTIES[2]);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      BOTTLE_PROPERTIES = new BooleanProperty[]{Properties.HAS_BOTTLE_0, Properties.HAS_BOTTLE_1, Properties.HAS_BOTTLE_2};
      SHAPE = VoxelShapes.union(Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 2.0, 15.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 14.0, 9.0));
   }
}
