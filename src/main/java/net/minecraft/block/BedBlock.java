package net.minecraft.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

public class BedBlock extends HorizontalFacingBlock implements BlockEntityProvider {
   public static final EnumProperty PART;
   public static final BooleanProperty OCCUPIED;
   protected static final int field_31009 = 9;
   protected static final VoxelShape TOP_SHAPE;
   private static final int field_31010 = 3;
   protected static final VoxelShape LEG_1_SHAPE;
   protected static final VoxelShape LEG_2_SHAPE;
   protected static final VoxelShape LEG_3_SHAPE;
   protected static final VoxelShape LEG_4_SHAPE;
   protected static final VoxelShape NORTH_SHAPE;
   protected static final VoxelShape SOUTH_SHAPE;
   protected static final VoxelShape WEST_SHAPE;
   protected static final VoxelShape EAST_SHAPE;
   private final DyeColor color;

   public BedBlock(DyeColor color, AbstractBlock.Settings settings) {
      super(settings);
      this.color = color;
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(PART, BedPart.FOOT)).with(OCCUPIED, false));
   }

   @Nullable
   public static Direction getDirection(BlockView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      return lv.getBlock() instanceof BedBlock ? (Direction)lv.get(FACING) : null;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.CONSUME;
      } else {
         if (state.get(PART) != BedPart.HEAD) {
            pos = pos.offset((Direction)state.get(FACING));
            state = world.getBlockState(pos);
            if (!state.isOf(this)) {
               return ActionResult.CONSUME;
            }
         }

         if (!isBedWorking(world)) {
            world.removeBlock(pos, false);
            BlockPos lv = pos.offset(((Direction)state.get(FACING)).getOpposite());
            if (world.getBlockState(lv).isOf(this)) {
               world.removeBlock(lv, false);
            }

            Vec3d lv2 = pos.toCenterPos();
            world.createExplosion((Entity)null, world.getDamageSources().badRespawnPoint(lv2), (ExplosionBehavior)null, lv2, 5.0F, true, World.ExplosionSourceType.BLOCK);
            return ActionResult.SUCCESS;
         } else if ((Boolean)state.get(OCCUPIED)) {
            if (!this.wakeVillager(world, pos)) {
               player.sendMessage(Text.translatable("block.minecraft.bed.occupied"), true);
            }

            return ActionResult.SUCCESS;
         } else {
            player.trySleep(pos).ifLeft((reason) -> {
               if (reason.getMessage() != null) {
                  player.sendMessage(reason.getMessage(), true);
               }

            });
            return ActionResult.SUCCESS;
         }
      }
   }

   public static boolean isBedWorking(World world) {
      return world.getDimension().bedWorks();
   }

   private boolean wakeVillager(World world, BlockPos pos) {
      List list = world.getEntitiesByClass(VillagerEntity.class, new Box(pos), LivingEntity::isSleeping);
      if (list.isEmpty()) {
         return false;
      } else {
         ((VillagerEntity)list.get(0)).wakeUp();
         return true;
      }
   }

   public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
      super.onLandedUpon(world, state, pos, entity, fallDistance * 0.5F);
   }

   public void onEntityLand(BlockView world, Entity entity) {
      if (entity.bypassesLandingEffects()) {
         super.onEntityLand(world, entity);
      } else {
         this.bounceEntity(entity);
      }

   }

   private void bounceEntity(Entity entity) {
      Vec3d lv = entity.getVelocity();
      if (lv.y < 0.0) {
         double d = entity instanceof LivingEntity ? 1.0 : 0.8;
         entity.setVelocity(lv.x, -lv.y * 0.6600000262260437 * d, lv.z);
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction == getDirectionTowardsOtherPart((BedPart)state.get(PART), (Direction)state.get(FACING))) {
         return neighborState.isOf(this) && neighborState.get(PART) != state.get(PART) ? (BlockState)state.with(OCCUPIED, (Boolean)neighborState.get(OCCUPIED)) : Blocks.AIR.getDefaultState();
      } else {
         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }

   private static Direction getDirectionTowardsOtherPart(BedPart part, Direction direction) {
      return part == BedPart.FOOT ? direction : direction.getOpposite();
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient && player.isCreative()) {
         BedPart lv = (BedPart)state.get(PART);
         if (lv == BedPart.FOOT) {
            BlockPos lv2 = pos.offset(getDirectionTowardsOtherPart(lv, (Direction)state.get(FACING)));
            BlockState lv3 = world.getBlockState(lv2);
            if (lv3.isOf(this) && lv3.get(PART) == BedPart.HEAD) {
               world.setBlockState(lv2, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
               world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, lv2, Block.getRawIdFromState(lv3));
            }
         }
      }

      super.onBreak(world, pos, state, player);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction lv = ctx.getHorizontalPlayerFacing();
      BlockPos lv2 = ctx.getBlockPos();
      BlockPos lv3 = lv2.offset(lv);
      World lv4 = ctx.getWorld();
      return lv4.getBlockState(lv3).canReplace(ctx) && lv4.getWorldBorder().contains(lv3) ? (BlockState)this.getDefaultState().with(FACING, lv) : null;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Direction lv = getOppositePartDirection(state).getOpposite();
      switch (lv) {
         case NORTH:
            return NORTH_SHAPE;
         case SOUTH:
            return SOUTH_SHAPE;
         case WEST:
            return WEST_SHAPE;
         default:
            return EAST_SHAPE;
      }
   }

   public static Direction getOppositePartDirection(BlockState state) {
      Direction lv = (Direction)state.get(FACING);
      return state.get(PART) == BedPart.HEAD ? lv.getOpposite() : lv;
   }

   public static DoubleBlockProperties.Type getBedPart(BlockState state) {
      BedPart lv = (BedPart)state.get(PART);
      return lv == BedPart.HEAD ? DoubleBlockProperties.Type.FIRST : DoubleBlockProperties.Type.SECOND;
   }

   private static boolean isBedBelow(BlockView world, BlockPos pos) {
      return world.getBlockState(pos.down()).getBlock() instanceof BedBlock;
   }

   public static Optional findWakeUpPosition(EntityType type, CollisionView world, BlockPos pos, Direction bedDirection, float spawnAngle) {
      Direction lv = bedDirection.rotateYClockwise();
      Direction lv2 = lv.pointsTo(spawnAngle) ? lv.getOpposite() : lv;
      if (isBedBelow(world, pos)) {
         return findWakeUpPosition(type, world, pos, bedDirection, lv2);
      } else {
         int[][] is = getAroundAndOnBedOffsets(bedDirection, lv2);
         Optional optional = findWakeUpPosition(type, world, pos, is, true);
         return optional.isPresent() ? optional : findWakeUpPosition(type, world, pos, is, false);
      }
   }

   private static Optional findWakeUpPosition(EntityType type, CollisionView world, BlockPos pos, Direction bedDirection, Direction respawnDirection) {
      int[][] is = getAroundBedOffsets(bedDirection, respawnDirection);
      Optional optional = findWakeUpPosition(type, world, pos, is, true);
      if (optional.isPresent()) {
         return optional;
      } else {
         BlockPos lv = pos.down();
         Optional optional2 = findWakeUpPosition(type, world, lv, is, true);
         if (optional2.isPresent()) {
            return optional2;
         } else {
            int[][] js = getOnBedOffsets(bedDirection);
            Optional optional3 = findWakeUpPosition(type, world, pos, js, true);
            if (optional3.isPresent()) {
               return optional3;
            } else {
               Optional optional4 = findWakeUpPosition(type, world, pos, is, false);
               if (optional4.isPresent()) {
                  return optional4;
               } else {
                  Optional optional5 = findWakeUpPosition(type, world, lv, is, false);
                  return optional5.isPresent() ? optional5 : findWakeUpPosition(type, world, pos, js, false);
               }
            }
         }
      }
   }

   private static Optional findWakeUpPosition(EntityType type, CollisionView world, BlockPos pos, int[][] possibleOffsets, boolean ignoreInvalidPos) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      int[][] var6 = possibleOffsets;
      int var7 = possibleOffsets.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         int[] js = var6[var8];
         lv.set(pos.getX() + js[0], pos.getY(), pos.getZ() + js[1]);
         Vec3d lv2 = Dismounting.findRespawnPos(type, world, lv, ignoreInvalidPos);
         if (lv2 != null) {
            return Optional.of(lv2);
         }
      }

      return Optional.empty();
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, PART, OCCUPIED);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new BedBlockEntity(pos, state, this.color);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      super.onPlaced(world, pos, state, placer, itemStack);
      if (!world.isClient) {
         BlockPos lv = pos.offset((Direction)state.get(FACING));
         world.setBlockState(lv, (BlockState)state.with(PART, BedPart.HEAD), Block.NOTIFY_ALL);
         world.updateNeighbors(pos, Blocks.AIR);
         state.updateNeighbors(world, pos, Block.NOTIFY_ALL);
      }

   }

   public DyeColor getColor() {
      return this.color;
   }

   public long getRenderingSeed(BlockState state, BlockPos pos) {
      BlockPos lv = pos.offset((Direction)state.get(FACING), state.get(PART) == BedPart.HEAD ? 0 : 1);
      return MathHelper.hashCode(lv.getX(), pos.getY(), lv.getZ());
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   private static int[][] getAroundAndOnBedOffsets(Direction bedDirection, Direction respawnDirection) {
      return (int[][])ArrayUtils.addAll(getAroundBedOffsets(bedDirection, respawnDirection), getOnBedOffsets(bedDirection));
   }

   private static int[][] getAroundBedOffsets(Direction bedDirection, Direction respawnDirection) {
      return new int[][]{{respawnDirection.getOffsetX(), respawnDirection.getOffsetZ()}, {respawnDirection.getOffsetX() - bedDirection.getOffsetX(), respawnDirection.getOffsetZ() - bedDirection.getOffsetZ()}, {respawnDirection.getOffsetX() - bedDirection.getOffsetX() * 2, respawnDirection.getOffsetZ() - bedDirection.getOffsetZ() * 2}, {-bedDirection.getOffsetX() * 2, -bedDirection.getOffsetZ() * 2}, {-respawnDirection.getOffsetX() - bedDirection.getOffsetX() * 2, -respawnDirection.getOffsetZ() - bedDirection.getOffsetZ() * 2}, {-respawnDirection.getOffsetX() - bedDirection.getOffsetX(), -respawnDirection.getOffsetZ() - bedDirection.getOffsetZ()}, {-respawnDirection.getOffsetX(), -respawnDirection.getOffsetZ()}, {-respawnDirection.getOffsetX() + bedDirection.getOffsetX(), -respawnDirection.getOffsetZ() + bedDirection.getOffsetZ()}, {bedDirection.getOffsetX(), bedDirection.getOffsetZ()}, {respawnDirection.getOffsetX() + bedDirection.getOffsetX(), respawnDirection.getOffsetZ() + bedDirection.getOffsetZ()}};
   }

   private static int[][] getOnBedOffsets(Direction bedDirection) {
      return new int[][]{{0, 0}, {-bedDirection.getOffsetX(), -bedDirection.getOffsetZ()}};
   }

   static {
      PART = Properties.BED_PART;
      OCCUPIED = Properties.OCCUPIED;
      TOP_SHAPE = Block.createCuboidShape(0.0, 3.0, 0.0, 16.0, 9.0, 16.0);
      LEG_1_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 3.0, 3.0);
      LEG_2_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 3.0, 3.0, 16.0);
      LEG_3_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 3.0, 3.0);
      LEG_4_SHAPE = Block.createCuboidShape(13.0, 0.0, 13.0, 16.0, 3.0, 16.0);
      NORTH_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_1_SHAPE, LEG_3_SHAPE);
      SOUTH_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_2_SHAPE, LEG_4_SHAPE);
      WEST_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_1_SHAPE, LEG_2_SHAPE);
      EAST_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_3_SHAPE, LEG_4_SHAPE);
   }
}
