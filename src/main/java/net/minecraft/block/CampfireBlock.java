package net.minecraft.block;

import java.util.Optional;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class CampfireBlock extends BlockWithEntity implements Waterloggable {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
   public static final BooleanProperty LIT;
   public static final BooleanProperty SIGNAL_FIRE;
   public static final BooleanProperty WATERLOGGED;
   public static final DirectionProperty FACING;
   private static final VoxelShape SMOKEY_SHAPE;
   private static final int field_31049 = 5;
   private final boolean emitsParticles;
   private final int fireDamage;

   public CampfireBlock(boolean emitsParticles, int fireDamage, AbstractBlock.Settings settings) {
      super(settings);
      this.emitsParticles = emitsParticles;
      this.fireDamage = fireDamage;
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LIT, true)).with(SIGNAL_FIRE, false)).with(WATERLOGGED, false)).with(FACING, Direction.NORTH));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof CampfireBlockEntity lv2) {
         ItemStack lv3 = player.getStackInHand(hand);
         Optional optional = lv2.getRecipeFor(lv3);
         if (optional.isPresent()) {
            if (!world.isClient && lv2.addItem(player, player.getAbilities().creativeMode ? lv3.copy() : lv3, ((CampfireCookingRecipe)optional.get()).getCookTime())) {
               player.incrementStat(Stats.INTERACT_WITH_CAMPFIRE);
               return ActionResult.SUCCESS;
            }

            return ActionResult.CONSUME;
         }
      }

      return ActionResult.PASS;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if ((Boolean)state.get(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
         entity.damage(world.getDamageSources().inFire(), (float)this.fireDamage);
      }

      super.onEntityCollision(state, world, pos, entity);
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof CampfireBlockEntity) {
            ItemScatterer.spawn(world, pos, ((CampfireBlockEntity)lv).getItemsBeingCooked());
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      WorldAccess lv = ctx.getWorld();
      BlockPos lv2 = ctx.getBlockPos();
      boolean bl = lv.getFluidState(lv2).getFluid() == Fluids.WATER;
      return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, bl)).with(SIGNAL_FIRE, this.isSignalFireBaseBlock(lv.getBlockState(lv2.down())))).with(LIT, !bl)).with(FACING, ctx.getHorizontalPlayerFacing());
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return direction == Direction.DOWN ? (BlockState)state.with(SIGNAL_FIRE, this.isSignalFireBaseBlock(neighborState)) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   private boolean isSignalFireBaseBlock(BlockState state) {
      return state.isOf(Blocks.HAY_BLOCK);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         if (random.nextInt(10) == 0) {
            world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
         }

         if (this.emitsParticles && random.nextInt(5) == 0) {
            for(int i = 0; i < random.nextInt(1) + 1; ++i) {
               world.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, (double)(random.nextFloat() / 2.0F), 5.0E-5, (double)(random.nextFloat() / 2.0F));
            }
         }

      }
   }

   public static void extinguish(@Nullable Entity entity, WorldAccess world, BlockPos pos, BlockState state) {
      if (world.isClient()) {
         for(int i = 0; i < 20; ++i) {
            spawnSmokeParticle((World)world, pos, (Boolean)state.get(SIGNAL_FIRE), true);
         }
      }

      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof CampfireBlockEntity) {
         ((CampfireBlockEntity)lv).spawnItemsBeingCooked();
      }

      world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
   }

   public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
      if (!(Boolean)state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
         boolean bl = (Boolean)state.get(LIT);
         if (bl) {
            if (!world.isClient()) {
               world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            extinguish((Entity)null, world, pos, state);
         }

         world.setBlockState(pos, (BlockState)((BlockState)state.with(WATERLOGGED, true)).with(LIT, false), Block.NOTIFY_ALL);
         world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
         return true;
      } else {
         return false;
      }
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      BlockPos lv = hit.getBlockPos();
      if (!world.isClient && projectile.isOnFire() && projectile.canModifyAt(world, lv) && !(Boolean)state.get(LIT) && !(Boolean)state.get(WATERLOGGED)) {
         world.setBlockState(lv, (BlockState)state.with(Properties.LIT, true), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
      }

   }

   public static void spawnSmokeParticle(World world, BlockPos pos, boolean isSignal, boolean lotsOfSmoke) {
      Random lv = world.getRandom();
      DefaultParticleType lv2 = isSignal ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
      world.addImportantParticle(lv2, true, (double)pos.getX() + 0.5 + lv.nextDouble() / 3.0 * (double)(lv.nextBoolean() ? 1 : -1), (double)pos.getY() + lv.nextDouble() + lv.nextDouble(), (double)pos.getZ() + 0.5 + lv.nextDouble() / 3.0 * (double)(lv.nextBoolean() ? 1 : -1), 0.0, 0.07, 0.0);
      if (lotsOfSmoke) {
         world.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.5 + lv.nextDouble() / 4.0 * (double)(lv.nextBoolean() ? 1 : -1), (double)pos.getY() + 0.4, (double)pos.getZ() + 0.5 + lv.nextDouble() / 4.0 * (double)(lv.nextBoolean() ? 1 : -1), 0.0, 0.005, 0.0);
      }

   }

   public static boolean isLitCampfireInRange(World world, BlockPos pos) {
      for(int i = 1; i <= 5; ++i) {
         BlockPos lv = pos.down(i);
         BlockState lv2 = world.getBlockState(lv);
         if (isLitCampfire(lv2)) {
            return true;
         }

         boolean bl = VoxelShapes.matchesAnywhere(SMOKEY_SHAPE, lv2.getCollisionShape(world, pos, ShapeContext.absent()), BooleanBiFunction.AND);
         if (bl) {
            BlockState lv3 = world.getBlockState(lv.down());
            return isLitCampfire(lv3);
         }
      }

      return false;
   }

   public static boolean isLitCampfire(BlockState state) {
      return state.contains(LIT) && state.isIn(BlockTags.CAMPFIRES) && (Boolean)state.get(LIT);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new CampfireBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      if (world.isClient) {
         return (Boolean)state.get(LIT) ? checkType(type, BlockEntityType.CAMPFIRE, CampfireBlockEntity::clientTick) : null;
      } else {
         return (Boolean)state.get(LIT) ? checkType(type, BlockEntityType.CAMPFIRE, CampfireBlockEntity::litServerTick) : checkType(type, BlockEntityType.CAMPFIRE, CampfireBlockEntity::unlitServerTick);
      }
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public static boolean canBeLit(BlockState state) {
      return state.isIn(BlockTags.CAMPFIRES, (statex) -> {
         return statex.contains(WATERLOGGED) && statex.contains(LIT);
      }) && !(Boolean)state.get(WATERLOGGED) && !(Boolean)state.get(LIT);
   }

   static {
      LIT = Properties.LIT;
      SIGNAL_FIRE = Properties.SIGNAL_FIRE;
      WATERLOGGED = Properties.WATERLOGGED;
      FACING = Properties.HORIZONTAL_FACING;
      SMOKEY_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
   }
}
