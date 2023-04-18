package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.NetherPortal;

public class NetherPortalBlock extends Block {
   public static final EnumProperty AXIS;
   protected static final int field_31196 = 2;
   protected static final VoxelShape X_SHAPE;
   protected static final VoxelShape Z_SHAPE;

   public NetherPortalBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AXIS, Direction.Axis.X));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch ((Direction.Axis)state.get(AXIS)) {
         case Z:
            return Z_SHAPE;
         case X:
         default:
            return X_SHAPE;
      }
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.getDimension().natural() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && random.nextInt(2000) < world.getDifficulty().getId()) {
         while(world.getBlockState(pos).isOf(this)) {
            pos = pos.down();
         }

         if (world.getBlockState(pos).allowsSpawning(world, pos, EntityType.ZOMBIFIED_PIGLIN)) {
            Entity lv = EntityType.ZOMBIFIED_PIGLIN.spawn(world, pos.up(), SpawnReason.STRUCTURE);
            if (lv != null) {
               lv.resetPortalCooldown();
            }
         }
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      Direction.Axis lv = direction.getAxis();
      Direction.Axis lv2 = (Direction.Axis)state.get(AXIS);
      boolean bl = lv2 != lv && lv.isHorizontal();
      return !bl && !neighborState.isOf(this) && !(new NetherPortal(world, pos, lv2)).wasAlreadyValid() ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (entity.canUsePortals()) {
         entity.setInNetherPortal(pos);
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (random.nextInt(100) == 0) {
         world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
      }

      for(int i = 0; i < 4; ++i) {
         double d = (double)pos.getX() + random.nextDouble();
         double e = (double)pos.getY() + random.nextDouble();
         double f = (double)pos.getZ() + random.nextDouble();
         double g = ((double)random.nextFloat() - 0.5) * 0.5;
         double h = ((double)random.nextFloat() - 0.5) * 0.5;
         double j = ((double)random.nextFloat() - 0.5) * 0.5;
         int k = random.nextInt(2) * 2 - 1;
         if (!world.getBlockState(pos.west()).isOf(this) && !world.getBlockState(pos.east()).isOf(this)) {
            d = (double)pos.getX() + 0.5 + 0.25 * (double)k;
            g = (double)(random.nextFloat() * 2.0F * (float)k);
         } else {
            f = (double)pos.getZ() + 0.5 + 0.25 * (double)k;
            j = (double)(random.nextFloat() * 2.0F * (float)k);
         }

         world.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, j);
      }

   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            switch ((Direction.Axis)state.get(AXIS)) {
               case Z:
                  return (BlockState)state.with(AXIS, Direction.Axis.X);
               case X:
                  return (BlockState)state.with(AXIS, Direction.Axis.Z);
               default:
                  return state;
            }
         default:
            return state;
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(AXIS);
   }

   static {
      AXIS = Properties.HORIZONTAL_AXIS;
      X_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
      Z_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
   }
}
