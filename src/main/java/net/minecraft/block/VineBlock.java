package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class VineBlock extends Block {
   public static final BooleanProperty UP;
   public static final BooleanProperty NORTH;
   public static final BooleanProperty EAST;
   public static final BooleanProperty SOUTH;
   public static final BooleanProperty WEST;
   public static final Map FACING_PROPERTIES;
   protected static final float field_31275 = 1.0F;
   private static final VoxelShape UP_SHAPE;
   private static final VoxelShape EAST_SHAPE;
   private static final VoxelShape WEST_SHAPE;
   private static final VoxelShape SOUTH_SHAPE;
   private static final VoxelShape NORTH_SHAPE;
   private final Map shapesByState;

   public VineBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(UP, false)).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false));
      this.shapesByState = ImmutableMap.copyOf((Map)this.stateManager.getStates().stream().collect(Collectors.toMap(Function.identity(), VineBlock::getShapeForState)));
   }

   private static VoxelShape getShapeForState(BlockState state) {
      VoxelShape lv = VoxelShapes.empty();
      if ((Boolean)state.get(UP)) {
         lv = UP_SHAPE;
      }

      if ((Boolean)state.get(NORTH)) {
         lv = VoxelShapes.union(lv, SOUTH_SHAPE);
      }

      if ((Boolean)state.get(SOUTH)) {
         lv = VoxelShapes.union(lv, NORTH_SHAPE);
      }

      if ((Boolean)state.get(EAST)) {
         lv = VoxelShapes.union(lv, WEST_SHAPE);
      }

      if ((Boolean)state.get(WEST)) {
         lv = VoxelShapes.union(lv, EAST_SHAPE);
      }

      return lv.isEmpty() ? VoxelShapes.fullCube() : lv;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)this.shapesByState.get(state);
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return true;
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return this.hasAdjacentBlocks(this.getPlacementShape(state, world, pos));
   }

   private boolean hasAdjacentBlocks(BlockState state) {
      return this.getAdjacentBlockCount(state) > 0;
   }

   private int getAdjacentBlockCount(BlockState state) {
      int i = 0;
      Iterator var3 = FACING_PROPERTIES.values().iterator();

      while(var3.hasNext()) {
         BooleanProperty lv = (BooleanProperty)var3.next();
         if ((Boolean)state.get(lv)) {
            ++i;
         }
      }

      return i;
   }

   private boolean shouldHaveSide(BlockView world, BlockPos pos, Direction side) {
      if (side == Direction.DOWN) {
         return false;
      } else {
         BlockPos lv = pos.offset(side);
         if (shouldConnectTo(world, lv, side)) {
            return true;
         } else if (side.getAxis() == Direction.Axis.Y) {
            return false;
         } else {
            BooleanProperty lv2 = (BooleanProperty)FACING_PROPERTIES.get(side);
            BlockState lv3 = world.getBlockState(pos.up());
            return lv3.isOf(this) && (Boolean)lv3.get(lv2);
         }
      }
   }

   public static boolean shouldConnectTo(BlockView world, BlockPos pos, Direction direction) {
      return MultifaceGrowthBlock.canGrowOn(world, direction, pos, world.getBlockState(pos));
   }

   private BlockState getPlacementShape(BlockState state, BlockView world, BlockPos pos) {
      BlockPos lv = pos.up();
      if ((Boolean)state.get(UP)) {
         state = (BlockState)state.with(UP, shouldConnectTo(world, lv, Direction.DOWN));
      }

      BlockState lv2 = null;
      Iterator var6 = Direction.Type.HORIZONTAL.iterator();

      while(true) {
         Direction lv3;
         BooleanProperty lv4;
         do {
            if (!var6.hasNext()) {
               return state;
            }

            lv3 = (Direction)var6.next();
            lv4 = getFacingProperty(lv3);
         } while(!(Boolean)state.get(lv4));

         boolean bl = this.shouldHaveSide(world, pos, lv3);
         if (!bl) {
            if (lv2 == null) {
               lv2 = world.getBlockState(lv);
            }

            bl = lv2.isOf(this) && (Boolean)lv2.get(lv4);
         }

         state = (BlockState)state.with(lv4, bl);
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction == Direction.DOWN) {
         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      } else {
         BlockState lv = this.getPlacementShape(state, world, pos);
         return !this.hasAdjacentBlocks(lv) ? Blocks.AIR.getDefaultState() : lv;
      }
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.getGameRules().getBoolean(GameRules.DO_VINES_SPREAD)) {
         if (random.nextInt(4) == 0) {
            Direction lv = Direction.random(random);
            BlockPos lv2 = pos.up();
            BlockPos lv3;
            BlockState lv4;
            Direction lv5;
            if (lv.getAxis().isHorizontal() && !(Boolean)state.get(getFacingProperty(lv))) {
               if (this.canGrowAt(world, pos)) {
                  lv3 = pos.offset(lv);
                  lv4 = world.getBlockState(lv3);
                  if (lv4.isAir()) {
                     lv5 = lv.rotateYClockwise();
                     Direction lv6 = lv.rotateYCounterclockwise();
                     boolean bl = (Boolean)state.get(getFacingProperty(lv5));
                     boolean bl2 = (Boolean)state.get(getFacingProperty(lv6));
                     BlockPos lv7 = lv3.offset(lv5);
                     BlockPos lv8 = lv3.offset(lv6);
                     if (bl && shouldConnectTo(world, lv7, lv5)) {
                        world.setBlockState(lv3, (BlockState)this.getDefaultState().with(getFacingProperty(lv5), true), Block.NOTIFY_LISTENERS);
                     } else if (bl2 && shouldConnectTo(world, lv8, lv6)) {
                        world.setBlockState(lv3, (BlockState)this.getDefaultState().with(getFacingProperty(lv6), true), Block.NOTIFY_LISTENERS);
                     } else {
                        Direction lv9 = lv.getOpposite();
                        if (bl && world.isAir(lv7) && shouldConnectTo(world, pos.offset(lv5), lv9)) {
                           world.setBlockState(lv7, (BlockState)this.getDefaultState().with(getFacingProperty(lv9), true), Block.NOTIFY_LISTENERS);
                        } else if (bl2 && world.isAir(lv8) && shouldConnectTo(world, pos.offset(lv6), lv9)) {
                           world.setBlockState(lv8, (BlockState)this.getDefaultState().with(getFacingProperty(lv9), true), Block.NOTIFY_LISTENERS);
                        } else if ((double)random.nextFloat() < 0.05 && shouldConnectTo(world, lv3.up(), Direction.UP)) {
                           world.setBlockState(lv3, (BlockState)this.getDefaultState().with(UP, true), Block.NOTIFY_LISTENERS);
                        }
                     }
                  } else if (shouldConnectTo(world, lv3, lv)) {
                     world.setBlockState(pos, (BlockState)state.with(getFacingProperty(lv), true), Block.NOTIFY_LISTENERS);
                  }

               }
            } else {
               if (lv == Direction.UP && pos.getY() < world.getTopY() - 1) {
                  if (this.shouldHaveSide(world, pos, lv)) {
                     world.setBlockState(pos, (BlockState)state.with(UP, true), Block.NOTIFY_LISTENERS);
                     return;
                  }

                  if (world.isAir(lv2)) {
                     if (!this.canGrowAt(world, pos)) {
                        return;
                     }

                     BlockState lv10 = state;
                     Iterator var17 = Direction.Type.HORIZONTAL.iterator();

                     while(true) {
                        do {
                           if (!var17.hasNext()) {
                              if (this.hasHorizontalSide(lv10)) {
                                 world.setBlockState(lv2, lv10, Block.NOTIFY_LISTENERS);
                              }

                              return;
                           }

                           lv5 = (Direction)var17.next();
                        } while(!random.nextBoolean() && shouldConnectTo(world, lv2.offset(lv5), lv5));

                        lv10 = (BlockState)lv10.with(getFacingProperty(lv5), false);
                     }
                  }
               }

               if (pos.getY() > world.getBottomY()) {
                  lv3 = pos.down();
                  lv4 = world.getBlockState(lv3);
                  if (lv4.isAir() || lv4.isOf(this)) {
                     BlockState lv11 = lv4.isAir() ? this.getDefaultState() : lv4;
                     BlockState lv12 = this.getGrownState(state, lv11, random);
                     if (lv11 != lv12 && this.hasHorizontalSide(lv12)) {
                        world.setBlockState(lv3, lv12, Block.NOTIFY_LISTENERS);
                     }
                  }
               }

            }
         }
      }
   }

   private BlockState getGrownState(BlockState above, BlockState state, Random random) {
      Iterator var4 = Direction.Type.HORIZONTAL.iterator();

      while(var4.hasNext()) {
         Direction lv = (Direction)var4.next();
         if (random.nextBoolean()) {
            BooleanProperty lv2 = getFacingProperty(lv);
            if ((Boolean)above.get(lv2)) {
               state = (BlockState)state.with(lv2, true);
            }
         }
      }

      return state;
   }

   private boolean hasHorizontalSide(BlockState state) {
      return (Boolean)state.get(NORTH) || (Boolean)state.get(EAST) || (Boolean)state.get(SOUTH) || (Boolean)state.get(WEST);
   }

   private boolean canGrowAt(BlockView world, BlockPos pos) {
      int i = true;
      Iterable iterable = BlockPos.iterate(pos.getX() - 4, pos.getY() - 1, pos.getZ() - 4, pos.getX() + 4, pos.getY() + 1, pos.getZ() + 4);
      int j = 5;
      Iterator var6 = iterable.iterator();

      while(var6.hasNext()) {
         BlockPos lv = (BlockPos)var6.next();
         if (world.getBlockState(lv).isOf(this)) {
            --j;
            if (j <= 0) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      BlockState lv = context.getWorld().getBlockState(context.getBlockPos());
      if (lv.isOf(this)) {
         return this.getAdjacentBlockCount(lv) < FACING_PROPERTIES.size();
      } else {
         return super.canReplace(state, context);
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos());
      boolean bl = lv.isOf(this);
      BlockState lv2 = bl ? lv : this.getDefaultState();
      Direction[] var5 = ctx.getPlacementDirections();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction lv3 = var5[var7];
         if (lv3 != Direction.DOWN) {
            BooleanProperty lv4 = getFacingProperty(lv3);
            boolean bl2 = bl && (Boolean)lv.get(lv4);
            if (!bl2 && this.shouldHaveSide(ctx.getWorld(), ctx.getBlockPos(), lv3)) {
               return (BlockState)lv2.with(lv4, true);
            }
         }
      }

      return bl ? lv2 : null;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(UP, NORTH, EAST, SOUTH, WEST);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, (Boolean)state.get(SOUTH))).with(EAST, (Boolean)state.get(WEST))).with(SOUTH, (Boolean)state.get(NORTH))).with(WEST, (Boolean)state.get(EAST));
         case COUNTERCLOCKWISE_90:
            return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, (Boolean)state.get(EAST))).with(EAST, (Boolean)state.get(SOUTH))).with(SOUTH, (Boolean)state.get(WEST))).with(WEST, (Boolean)state.get(NORTH));
         case CLOCKWISE_90:
            return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, (Boolean)state.get(WEST))).with(EAST, (Boolean)state.get(NORTH))).with(SOUTH, (Boolean)state.get(EAST))).with(WEST, (Boolean)state.get(SOUTH));
         default:
            return state;
      }
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      switch (mirror) {
         case LEFT_RIGHT:
            return (BlockState)((BlockState)state.with(NORTH, (Boolean)state.get(SOUTH))).with(SOUTH, (Boolean)state.get(NORTH));
         case FRONT_BACK:
            return (BlockState)((BlockState)state.with(EAST, (Boolean)state.get(WEST))).with(WEST, (Boolean)state.get(EAST));
         default:
            return super.mirror(state, mirror);
      }
   }

   public static BooleanProperty getFacingProperty(Direction direction) {
      return (BooleanProperty)FACING_PROPERTIES.get(direction);
   }

   static {
      UP = ConnectingBlock.UP;
      NORTH = ConnectingBlock.NORTH;
      EAST = ConnectingBlock.EAST;
      SOUTH = ConnectingBlock.SOUTH;
      WEST = ConnectingBlock.WEST;
      FACING_PROPERTIES = (Map)ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter((entry) -> {
         return entry.getKey() != Direction.DOWN;
      }).collect(Util.toMap());
      UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
      EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
      WEST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
      SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
      NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
   }
}
