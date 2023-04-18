package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class MultifaceGrowthBlock extends Block {
   private static final float field_31194 = 1.0F;
   private static final VoxelShape UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
   private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
   private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
   private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
   private static final Map FACING_PROPERTIES;
   private static final Map SHAPES_FOR_DIRECTIONS;
   protected static final Direction[] DIRECTIONS;
   private final ImmutableMap SHAPES;
   private final boolean hasAllHorizontalDirections;
   private final boolean canMirrorX;
   private final boolean canMirrorZ;

   public MultifaceGrowthBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState(withAllDirections(this.stateManager));
      this.SHAPES = this.getShapesForStates(MultifaceGrowthBlock::getShapeForState);
      this.hasAllHorizontalDirections = Direction.Type.HORIZONTAL.stream().allMatch(this::canHaveDirection);
      this.canMirrorX = Direction.Type.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::canHaveDirection).count() % 2L == 0L;
      this.canMirrorZ = Direction.Type.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::canHaveDirection).count() % 2L == 0L;
   }

   public static Set collectDirections(BlockState state) {
      if (!(state.getBlock() instanceof MultifaceGrowthBlock)) {
         return Set.of();
      } else {
         Set set = EnumSet.noneOf(Direction.class);
         Direction[] var2 = Direction.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Direction lv = var2[var4];
            if (hasDirection(state, lv)) {
               set.add(lv);
            }
         }

         return set;
      }
   }

   public static Set flagToDirections(byte flag) {
      Set set = EnumSet.noneOf(Direction.class);
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction lv = var2[var4];
         if ((flag & (byte)(1 << lv.ordinal())) > 0) {
            set.add(lv);
         }
      }

      return set;
   }

   public static byte directionsToFlag(Collection directions) {
      byte b = 0;

      Direction lv;
      for(Iterator var2 = directions.iterator(); var2.hasNext(); b = (byte)(b | 1 << lv.ordinal())) {
         lv = (Direction)var2.next();
      }

      return b;
   }

   protected boolean canHaveDirection(Direction direction) {
      return true;
   }

   protected void appendProperties(StateManager.Builder builder) {
      Direction[] var2 = DIRECTIONS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction lv = var2[var4];
         if (this.canHaveDirection(lv)) {
            builder.add(getProperty(lv));
         }
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (!hasAnyDirection(state)) {
         return Blocks.AIR.getDefaultState();
      } else {
         return hasDirection(state, direction) && !canGrowOn(world, direction, neighborPos, neighborState) ? disableDirection(state, getProperty(direction)) : state;
      }
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)this.SHAPES.get(state);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      boolean bl = false;
      Direction[] var5 = DIRECTIONS;
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction lv = var5[var7];
         if (hasDirection(state, lv)) {
            BlockPos lv2 = pos.offset(lv);
            if (!canGrowOn(world, lv, lv2, world.getBlockState(lv2))) {
               return false;
            }

            bl = true;
         }
      }

      return bl;
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return isNotFullBlock(state);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      World lv = ctx.getWorld();
      BlockPos lv2 = ctx.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      return (BlockState)Arrays.stream(ctx.getPlacementDirections()).map((direction) -> {
         return this.withDirection(lv3, lv, lv2, direction);
      }).filter(Objects::nonNull).findFirst().orElse((Object)null);
   }

   public boolean canGrowWithDirection(BlockView world, BlockState state, BlockPos pos, Direction direction) {
      if (this.canHaveDirection(direction) && (!state.isOf(this) || !hasDirection(state, direction))) {
         BlockPos lv = pos.offset(direction);
         return canGrowOn(world, direction, lv, world.getBlockState(lv));
      } else {
         return false;
      }
   }

   @Nullable
   public BlockState withDirection(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      if (!this.canGrowWithDirection(world, state, pos, direction)) {
         return null;
      } else {
         BlockState lv;
         if (state.isOf(this)) {
            lv = state;
         } else if (this.isWaterlogged() && state.getFluidState().isEqualAndStill(Fluids.WATER)) {
            lv = (BlockState)this.getDefaultState().with(Properties.WATERLOGGED, true);
         } else {
            lv = this.getDefaultState();
         }

         return (BlockState)lv.with(getProperty(direction), true);
      }
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      if (!this.hasAllHorizontalDirections) {
         return state;
      } else {
         Objects.requireNonNull(rotation);
         return this.mirror(state, rotation::rotate);
      }
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      if (mirror == BlockMirror.FRONT_BACK && !this.canMirrorX) {
         return state;
      } else if (mirror == BlockMirror.LEFT_RIGHT && !this.canMirrorZ) {
         return state;
      } else {
         Objects.requireNonNull(mirror);
         return this.mirror(state, mirror::apply);
      }
   }

   private BlockState mirror(BlockState state, Function mirror) {
      BlockState lv = state;
      Direction[] var4 = DIRECTIONS;
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Direction lv2 = var4[var6];
         if (this.canHaveDirection(lv2)) {
            lv = (BlockState)lv.with(getProperty((Direction)mirror.apply(lv2)), (Boolean)state.get(getProperty(lv2)));
         }
      }

      return lv;
   }

   public static boolean hasDirection(BlockState state, Direction direction) {
      BooleanProperty lv = getProperty(direction);
      return state.contains(lv) && (Boolean)state.get(lv);
   }

   public static boolean canGrowOn(BlockView world, Direction direction, BlockPos pos, BlockState state) {
      return Block.isFaceFullSquare(state.getSidesShape(world, pos), direction.getOpposite()) || Block.isFaceFullSquare(state.getCollisionShape(world, pos), direction.getOpposite());
   }

   private boolean isWaterlogged() {
      return this.stateManager.getProperties().contains(Properties.WATERLOGGED);
   }

   private static BlockState disableDirection(BlockState state, BooleanProperty direction) {
      BlockState lv = (BlockState)state.with(direction, false);
      return hasAnyDirection(lv) ? lv : Blocks.AIR.getDefaultState();
   }

   public static BooleanProperty getProperty(Direction direction) {
      return (BooleanProperty)FACING_PROPERTIES.get(direction);
   }

   private static BlockState withAllDirections(StateManager stateManager) {
      BlockState lv = (BlockState)stateManager.getDefaultState();
      Iterator var2 = FACING_PROPERTIES.values().iterator();

      while(var2.hasNext()) {
         BooleanProperty lv2 = (BooleanProperty)var2.next();
         if (lv.contains(lv2)) {
            lv = (BlockState)lv.with(lv2, false);
         }
      }

      return lv;
   }

   private static VoxelShape getShapeForState(BlockState state) {
      VoxelShape lv = VoxelShapes.empty();
      Direction[] var2 = DIRECTIONS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction lv2 = var2[var4];
         if (hasDirection(state, lv2)) {
            lv = VoxelShapes.union(lv, (VoxelShape)SHAPES_FOR_DIRECTIONS.get(lv2));
         }
      }

      return lv.isEmpty() ? VoxelShapes.fullCube() : lv;
   }

   protected static boolean hasAnyDirection(BlockState state) {
      return Arrays.stream(DIRECTIONS).anyMatch((direction) -> {
         return hasDirection(state, direction);
      });
   }

   private static boolean isNotFullBlock(BlockState state) {
      return Arrays.stream(DIRECTIONS).anyMatch((direction) -> {
         return !hasDirection(state, direction);
      });
   }

   public abstract LichenGrower getGrower();

   static {
      FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES;
      SHAPES_FOR_DIRECTIONS = (Map)Util.make(Maps.newEnumMap(Direction.class), (shapes) -> {
         shapes.put(Direction.NORTH, SOUTH_SHAPE);
         shapes.put(Direction.EAST, WEST_SHAPE);
         shapes.put(Direction.SOUTH, NORTH_SHAPE);
         shapes.put(Direction.WEST, EAST_SHAPE);
         shapes.put(Direction.UP, UP_SHAPE);
         shapes.put(Direction.DOWN, DOWN_SHAPE);
      });
      DIRECTIONS = Direction.values();
   }
}
