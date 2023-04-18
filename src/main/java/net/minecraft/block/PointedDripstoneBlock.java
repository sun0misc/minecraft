package net.minecraft.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.block.enums.Thickness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PointedDripstoneBlock extends Block implements LandingBlock, Waterloggable {
   public static final DirectionProperty VERTICAL_DIRECTION;
   public static final EnumProperty THICKNESS;
   public static final BooleanProperty WATERLOGGED;
   private static final int field_31205 = 11;
   private static final int field_31207 = 2;
   private static final float field_31208 = 0.02F;
   private static final float field_31209 = 0.12F;
   private static final int field_31210 = 11;
   private static final float WATER_DRIP_CHANCE = 0.17578125F;
   private static final float LAVA_DRIP_CHANCE = 0.05859375F;
   private static final double field_31213 = 0.6;
   private static final float field_31214 = 1.0F;
   private static final int field_31215 = 40;
   private static final int field_31200 = 6;
   private static final float field_31201 = 2.0F;
   private static final int field_31202 = 2;
   private static final float field_33566 = 5.0F;
   private static final float field_33567 = 0.011377778F;
   private static final int MAX_STALACTITE_GROWTH = 7;
   private static final int STALACTITE_FLOOR_SEARCH_RANGE = 10;
   private static final float field_31203 = 0.6875F;
   private static final VoxelShape TIP_MERGE_SHAPE;
   private static final VoxelShape UP_TIP_SHAPE;
   private static final VoxelShape DOWN_TIP_SHAPE;
   private static final VoxelShape BASE_SHAPE;
   private static final VoxelShape FRUSTUM_SHAPE;
   private static final VoxelShape MIDDLE_SHAPE;
   private static final float field_31204 = 0.125F;
   private static final VoxelShape DRIP_COLLISION_SHAPE;

   public PointedDripstoneBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(VERTICAL_DIRECTION, Direction.UP)).with(THICKNESS, Thickness.TIP)).with(WATERLOGGED, false));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(VERTICAL_DIRECTION, THICKNESS, WATERLOGGED);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return canPlaceAtWithDirection(world, pos, (Direction)state.get(VERTICAL_DIRECTION));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      if (direction != Direction.UP && direction != Direction.DOWN) {
         return state;
      } else {
         Direction lv = (Direction)state.get(VERTICAL_DIRECTION);
         if (lv == Direction.DOWN && world.getBlockTickScheduler().isQueued(pos, this)) {
            return state;
         } else if (direction == lv.getOpposite() && !this.canPlaceAt(state, world, pos)) {
            if (lv == Direction.DOWN) {
               world.scheduleBlockTick(pos, this, 2);
            } else {
               world.scheduleBlockTick(pos, this, 1);
            }

            return state;
         } else {
            boolean bl = state.get(THICKNESS) == Thickness.TIP_MERGE;
            Thickness lv2 = getThickness(world, pos, lv, bl);
            return (BlockState)state.with(THICKNESS, lv2);
         }
      }
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      BlockPos lv = hit.getBlockPos();
      if (!world.isClient && projectile.canModifyAt(world, lv) && projectile instanceof TridentEntity && projectile.getVelocity().length() > 0.6) {
         world.breakBlock(lv, true);
      }

   }

   public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
      if (state.get(VERTICAL_DIRECTION) == Direction.UP && state.get(THICKNESS) == Thickness.TIP) {
         entity.handleFallDamage(fallDistance + 2.0F, 2.0F, world.getDamageSources().stalagmite());
      } else {
         super.onLandedUpon(world, state, pos, entity, fallDistance);
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (canDrip(state)) {
         float f = random.nextFloat();
         if (!(f > 0.12F)) {
            getFluid(world, pos, state).filter((fluid) -> {
               return f < 0.02F || isFluidLiquid(fluid.fluid);
            }).ifPresent((fluid) -> {
               createParticle(world, pos, state, fluid.fluid);
            });
         }
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (isPointingUp(state) && !this.canPlaceAt(state, world, pos)) {
         world.breakBlock(pos, true);
      } else {
         spawnFallingBlock(state, world, pos);
      }

   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      dripTick(state, world, pos, random.nextFloat());
      if (random.nextFloat() < 0.011377778F && isHeldByPointedDripstone(state, world, pos)) {
         tryGrow(state, world, pos, random);
      }

   }

   @VisibleForTesting
   public static void dripTick(BlockState state, ServerWorld world, BlockPos pos, float dripChance) {
      if (!(dripChance > 0.17578125F) || !(dripChance > 0.05859375F)) {
         if (isHeldByPointedDripstone(state, world, pos)) {
            Optional optional = getFluid(world, pos, state);
            if (!optional.isEmpty()) {
               Fluid lv = ((DrippingFluid)optional.get()).fluid;
               float g;
               if (lv == Fluids.WATER) {
                  g = 0.17578125F;
               } else {
                  if (lv != Fluids.LAVA) {
                     return;
                  }

                  g = 0.05859375F;
               }

               if (!(dripChance >= g)) {
                  BlockPos lv2 = getTipPos(state, world, pos, 11, false);
                  if (lv2 != null) {
                     if (((DrippingFluid)optional.get()).sourceState.isOf(Blocks.MUD) && lv == Fluids.WATER) {
                        BlockState lv3 = Blocks.CLAY.getDefaultState();
                        world.setBlockState(((DrippingFluid)optional.get()).pos, lv3);
                        Block.pushEntitiesUpBeforeBlockChange(((DrippingFluid)optional.get()).sourceState, lv3, world, ((DrippingFluid)optional.get()).pos);
                        world.emitGameEvent(GameEvent.BLOCK_CHANGE, ((DrippingFluid)optional.get()).pos, GameEvent.Emitter.of(lv3));
                        world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS, lv2, 0);
                     } else {
                        BlockPos lv4 = getCauldronPos(world, lv2, lv);
                        if (lv4 != null) {
                           world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS, lv2, 0);
                           int i = lv2.getY() - lv4.getY();
                           int j = 50 + i;
                           BlockState lv5 = world.getBlockState(lv4);
                           world.scheduleBlockTick(lv4, lv5.getBlock(), j);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      WorldAccess lv = ctx.getWorld();
      BlockPos lv2 = ctx.getBlockPos();
      Direction lv3 = ctx.getVerticalPlayerLookDirection().getOpposite();
      Direction lv4 = getDirectionToPlaceAt(lv, lv2, lv3);
      if (lv4 == null) {
         return null;
      } else {
         boolean bl = !ctx.shouldCancelInteraction();
         Thickness lv5 = getThickness(lv, lv2, lv4, bl);
         return lv5 == null ? null : (BlockState)((BlockState)((BlockState)this.getDefaultState().with(VERTICAL_DIRECTION, lv4)).with(THICKNESS, lv5)).with(WATERLOGGED, lv.getFluidState(lv2).getFluid() == Fluids.WATER);
      }
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
      return VoxelShapes.empty();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Thickness lv = (Thickness)state.get(THICKNESS);
      VoxelShape lv2;
      if (lv == Thickness.TIP_MERGE) {
         lv2 = TIP_MERGE_SHAPE;
      } else if (lv == Thickness.TIP) {
         if (state.get(VERTICAL_DIRECTION) == Direction.DOWN) {
            lv2 = DOWN_TIP_SHAPE;
         } else {
            lv2 = UP_TIP_SHAPE;
         }
      } else if (lv == Thickness.FRUSTUM) {
         lv2 = BASE_SHAPE;
      } else if (lv == Thickness.MIDDLE) {
         lv2 = FRUSTUM_SHAPE;
      } else {
         lv2 = MIDDLE_SHAPE;
      }

      Vec3d lv3 = state.getModelOffset(world, pos);
      return lv2.offset(lv3.x, 0.0, lv3.z);
   }

   public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
      return false;
   }

   public float getMaxHorizontalModelOffset() {
      return 0.125F;
   }

   public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
      if (!fallingBlockEntity.isSilent()) {
         world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_LANDS, pos, 0);
      }

   }

   public DamageSource getDamageSource(Entity attacker) {
      return attacker.getDamageSources().fallingStalactite(attacker);
   }

   public Predicate getEntityPredicate() {
      return EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(EntityPredicates.VALID_LIVING_ENTITY);
   }

   private static void spawnFallingBlock(BlockState state, ServerWorld world, BlockPos pos) {
      BlockPos.Mutable lv = pos.mutableCopy();

      for(BlockState lv2 = state; isPointingDown(lv2); lv2 = world.getBlockState(lv)) {
         FallingBlockEntity lv3 = FallingBlockEntity.spawnFromBlock(world, lv, lv2);
         if (isTip(lv2, true)) {
            int i = Math.max(1 + pos.getY() - lv.getY(), 6);
            float f = 1.0F * (float)i;
            lv3.setHurtEntities(f, 40);
            break;
         }

         lv.move(Direction.DOWN);
      }

   }

   @VisibleForTesting
   public static void tryGrow(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockState lv = world.getBlockState(pos.up(1));
      BlockState lv2 = world.getBlockState(pos.up(2));
      if (canGrow(lv, lv2)) {
         BlockPos lv3 = getTipPos(state, world, pos, 7, false);
         if (lv3 != null) {
            BlockState lv4 = world.getBlockState(lv3);
            if (canDrip(lv4) && canGrow(lv4, world, lv3)) {
               if (random.nextBoolean()) {
                  tryGrow(world, lv3, Direction.DOWN);
               } else {
                  tryGrowStalagmite(world, lv3);
               }

            }
         }
      }
   }

   private static void tryGrowStalagmite(ServerWorld world, BlockPos pos) {
      BlockPos.Mutable lv = pos.mutableCopy();

      for(int i = 0; i < 10; ++i) {
         lv.move(Direction.DOWN);
         BlockState lv2 = world.getBlockState(lv);
         if (!lv2.getFluidState().isEmpty()) {
            return;
         }

         if (isTip(lv2, Direction.UP) && canGrow(lv2, world, lv)) {
            tryGrow(world, lv, Direction.UP);
            return;
         }

         if (canPlaceAtWithDirection(world, lv, Direction.UP) && !world.isWater(lv.down())) {
            tryGrow(world, lv.down(), Direction.UP);
            return;
         }

         if (!canDripThrough(world, lv, lv2)) {
            return;
         }
      }

   }

   private static void tryGrow(ServerWorld world, BlockPos pos, Direction direction) {
      BlockPos lv = pos.offset(direction);
      BlockState lv2 = world.getBlockState(lv);
      if (isTip(lv2, direction.getOpposite())) {
         growMerged(lv2, world, lv);
      } else if (lv2.isAir() || lv2.isOf(Blocks.WATER)) {
         place(world, lv, direction, Thickness.TIP);
      }

   }

   private static void place(WorldAccess world, BlockPos pos, Direction direction, Thickness thickness) {
      BlockState lv = (BlockState)((BlockState)((BlockState)Blocks.POINTED_DRIPSTONE.getDefaultState().with(VERTICAL_DIRECTION, direction)).with(THICKNESS, thickness)).with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER);
      world.setBlockState(pos, lv, Block.NOTIFY_ALL);
   }

   private static void growMerged(BlockState state, WorldAccess world, BlockPos pos) {
      BlockPos lv2;
      BlockPos lv;
      if (state.get(VERTICAL_DIRECTION) == Direction.UP) {
         lv = pos;
         lv2 = pos.up();
      } else {
         lv2 = pos;
         lv = pos.down();
      }

      place(world, lv2, Direction.DOWN, Thickness.TIP_MERGE);
      place(world, lv, Direction.UP, Thickness.TIP_MERGE);
   }

   public static void createParticle(World world, BlockPos pos, BlockState state) {
      getFluid(world, pos, state).ifPresent((fluid) -> {
         createParticle(world, pos, state, fluid.fluid);
      });
   }

   private static void createParticle(World world, BlockPos pos, BlockState state, Fluid fluid) {
      Vec3d lv = state.getModelOffset(world, pos);
      double d = 0.0625;
      double e = (double)pos.getX() + 0.5 + lv.x;
      double f = (double)((float)(pos.getY() + 1) - 0.6875F) - 0.0625;
      double g = (double)pos.getZ() + 0.5 + lv.z;
      Fluid lv2 = getDripFluid(world, fluid);
      ParticleEffect lv3 = lv2.isIn(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
      world.addParticle(lv3, e, f, g, 0.0, 0.0, 0.0);
   }

   @Nullable
   private static BlockPos getTipPos(BlockState state, WorldAccess world, BlockPos pos, int range, boolean allowMerged) {
      if (isTip(state, allowMerged)) {
         return pos;
      } else {
         Direction lv = (Direction)state.get(VERTICAL_DIRECTION);
         BiPredicate biPredicate = (posx, statex) -> {
            return statex.isOf(Blocks.POINTED_DRIPSTONE) && statex.get(VERTICAL_DIRECTION) == lv;
         };
         return (BlockPos)searchInDirection(world, pos, lv.getDirection(), biPredicate, (statex) -> {
            return isTip(statex, allowMerged);
         }, range).orElse((Object)null);
      }
   }

   @Nullable
   private static Direction getDirectionToPlaceAt(WorldView world, BlockPos pos, Direction direction) {
      Direction lv;
      if (canPlaceAtWithDirection(world, pos, direction)) {
         lv = direction;
      } else {
         if (!canPlaceAtWithDirection(world, pos, direction.getOpposite())) {
            return null;
         }

         lv = direction.getOpposite();
      }

      return lv;
   }

   private static Thickness getThickness(WorldView world, BlockPos pos, Direction direction, boolean tryMerge) {
      Direction lv = direction.getOpposite();
      BlockState lv2 = world.getBlockState(pos.offset(direction));
      if (isPointedDripstoneFacingDirection(lv2, lv)) {
         return !tryMerge && lv2.get(THICKNESS) != Thickness.TIP_MERGE ? Thickness.TIP : Thickness.TIP_MERGE;
      } else if (!isPointedDripstoneFacingDirection(lv2, direction)) {
         return Thickness.TIP;
      } else {
         Thickness lv3 = (Thickness)lv2.get(THICKNESS);
         if (lv3 != Thickness.TIP && lv3 != Thickness.TIP_MERGE) {
            BlockState lv4 = world.getBlockState(pos.offset(lv));
            return !isPointedDripstoneFacingDirection(lv4, direction) ? Thickness.BASE : Thickness.MIDDLE;
         } else {
            return Thickness.FRUSTUM;
         }
      }
   }

   public static boolean canDrip(BlockState state) {
      return isPointingDown(state) && state.get(THICKNESS) == Thickness.TIP && !(Boolean)state.get(WATERLOGGED);
   }

   private static boolean canGrow(BlockState state, ServerWorld world, BlockPos pos) {
      Direction lv = (Direction)state.get(VERTICAL_DIRECTION);
      BlockPos lv2 = pos.offset(lv);
      BlockState lv3 = world.getBlockState(lv2);
      if (!lv3.getFluidState().isEmpty()) {
         return false;
      } else {
         return lv3.isAir() ? true : isTip(lv3, lv.getOpposite());
      }
   }

   private static Optional getSupportingPos(World world, BlockPos pos, BlockState state, int range) {
      Direction lv = (Direction)state.get(VERTICAL_DIRECTION);
      BiPredicate biPredicate = (posx, statex) -> {
         return statex.isOf(Blocks.POINTED_DRIPSTONE) && statex.get(VERTICAL_DIRECTION) == lv;
      };
      return searchInDirection(world, pos, lv.getOpposite().getDirection(), biPredicate, (statex) -> {
         return !statex.isOf(Blocks.POINTED_DRIPSTONE);
      }, range);
   }

   private static boolean canPlaceAtWithDirection(WorldView world, BlockPos pos, Direction direction) {
      BlockPos lv = pos.offset(direction.getOpposite());
      BlockState lv2 = world.getBlockState(lv);
      return lv2.isSideSolidFullSquare(world, lv, direction) || isPointedDripstoneFacingDirection(lv2, direction);
   }

   private static boolean isTip(BlockState state, boolean allowMerged) {
      if (!state.isOf(Blocks.POINTED_DRIPSTONE)) {
         return false;
      } else {
         Thickness lv = (Thickness)state.get(THICKNESS);
         return lv == Thickness.TIP || allowMerged && lv == Thickness.TIP_MERGE;
      }
   }

   private static boolean isTip(BlockState state, Direction direction) {
      return isTip(state, false) && state.get(VERTICAL_DIRECTION) == direction;
   }

   private static boolean isPointingDown(BlockState state) {
      return isPointedDripstoneFacingDirection(state, Direction.DOWN);
   }

   private static boolean isPointingUp(BlockState state) {
      return isPointedDripstoneFacingDirection(state, Direction.UP);
   }

   private static boolean isHeldByPointedDripstone(BlockState state, WorldView world, BlockPos pos) {
      return isPointingDown(state) && !world.getBlockState(pos.up()).isOf(Blocks.POINTED_DRIPSTONE);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   private static boolean isPointedDripstoneFacingDirection(BlockState state, Direction direction) {
      return state.isOf(Blocks.POINTED_DRIPSTONE) && state.get(VERTICAL_DIRECTION) == direction;
   }

   @Nullable
   private static BlockPos getCauldronPos(World world, BlockPos pos, Fluid fluid) {
      Predicate predicate = (state) -> {
         return state.getBlock() instanceof AbstractCauldronBlock && ((AbstractCauldronBlock)state.getBlock()).canBeFilledByDripstone(fluid);
      };
      BiPredicate biPredicate = (posx, state) -> {
         return canDripThrough(world, posx, state);
      };
      return (BlockPos)searchInDirection(world, pos, Direction.DOWN.getDirection(), biPredicate, predicate, 11).orElse((Object)null);
   }

   @Nullable
   public static BlockPos getDripPos(World world, BlockPos pos) {
      BiPredicate biPredicate = (posx, state) -> {
         return canDripThrough(world, posx, state);
      };
      return (BlockPos)searchInDirection(world, pos, Direction.UP.getDirection(), biPredicate, PointedDripstoneBlock::canDrip, 11).orElse((Object)null);
   }

   public static Fluid getDripFluid(ServerWorld world, BlockPos pos) {
      return (Fluid)getFluid(world, pos, world.getBlockState(pos)).map((fluid) -> {
         return fluid.fluid;
      }).filter(PointedDripstoneBlock::isFluidLiquid).orElse(Fluids.EMPTY);
   }

   private static Optional getFluid(World world, BlockPos pos, BlockState state) {
      return !isPointingDown(state) ? Optional.empty() : getSupportingPos(world, pos, state, 11).map((posx) -> {
         BlockPos lv = posx.up();
         BlockState lv2 = world.getBlockState(lv);
         Object lv3;
         if (lv2.isOf(Blocks.MUD) && !world.getDimension().ultrawarm()) {
            lv3 = Fluids.WATER;
         } else {
            lv3 = world.getFluidState(lv).getFluid();
         }

         return new DrippingFluid(lv, (Fluid)lv3, lv2);
      });
   }

   private static boolean isFluidLiquid(Fluid fluid) {
      return fluid == Fluids.LAVA || fluid == Fluids.WATER;
   }

   private static boolean canGrow(BlockState dripstoneBlockState, BlockState waterState) {
      return dripstoneBlockState.isOf(Blocks.DRIPSTONE_BLOCK) && waterState.isOf(Blocks.WATER) && waterState.getFluidState().isStill();
   }

   private static Fluid getDripFluid(World world, Fluid fluid) {
      if (fluid.matchesType(Fluids.EMPTY)) {
         return world.getDimension().ultrawarm() ? Fluids.LAVA : Fluids.WATER;
      } else {
         return fluid;
      }
   }

   private static Optional searchInDirection(WorldAccess world, BlockPos pos, Direction.AxisDirection direction, BiPredicate continuePredicate, Predicate stopPredicate, int range) {
      Direction lv = Direction.get(direction, Direction.Axis.Y);
      BlockPos.Mutable lv2 = pos.mutableCopy();

      for(int j = 1; j < range; ++j) {
         lv2.move(lv);
         BlockState lv3 = world.getBlockState(lv2);
         if (stopPredicate.test(lv3)) {
            return Optional.of(lv2.toImmutable());
         }

         if (world.isOutOfHeightLimit(lv2.getY()) || !continuePredicate.test(lv2, lv3)) {
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   private static boolean canDripThrough(BlockView world, BlockPos pos, BlockState state) {
      if (state.isAir()) {
         return true;
      } else if (state.isOpaqueFullCube(world, pos)) {
         return false;
      } else if (!state.getFluidState().isEmpty()) {
         return false;
      } else {
         VoxelShape lv = state.getCollisionShape(world, pos);
         return !VoxelShapes.matchesAnywhere(DRIP_COLLISION_SHAPE, lv, BooleanBiFunction.AND);
      }
   }

   static {
      VERTICAL_DIRECTION = Properties.VERTICAL_DIRECTION;
      THICKNESS = Properties.THICKNESS;
      WATERLOGGED = Properties.WATERLOGGED;
      TIP_MERGE_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
      UP_TIP_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
      DOWN_TIP_SHAPE = Block.createCuboidShape(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
      BASE_SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
      FRUSTUM_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
      MIDDLE_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
      DRIP_COLLISION_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
   }

   static record DrippingFluid(BlockPos pos, Fluid fluid, BlockState sourceState) {
      final BlockPos pos;
      final Fluid fluid;
      final BlockState sourceState;

      DrippingFluid(BlockPos arg, Fluid arg2, BlockState arg3) {
         this.pos = arg;
         this.fluid = arg2;
         this.sourceState = arg3;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public Fluid fluid() {
         return this.fluid;
      }

      public BlockState sourceState() {
         return this.sourceState;
      }
   }
}
