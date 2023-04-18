package net.minecraft.fluid;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.IceBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class FlowableFluid extends Fluid {
   public static final BooleanProperty FALLING;
   public static final IntProperty LEVEL;
   private static final int field_31726 = 200;
   private static final ThreadLocal field_15901;
   private final Map shapeCache = Maps.newIdentityHashMap();

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FALLING);
   }

   public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
      double d = 0.0;
      double e = 0.0;
      BlockPos.Mutable lv = new BlockPos.Mutable();
      Iterator var9 = Direction.Type.HORIZONTAL.iterator();

      while(var9.hasNext()) {
         Direction lv2 = (Direction)var9.next();
         lv.set(pos, (Direction)lv2);
         FluidState lv3 = world.getFluidState(lv);
         if (this.isEmptyOrThis(lv3)) {
            float f = lv3.getHeight();
            float g = 0.0F;
            if (f == 0.0F) {
               if (!world.getBlockState(lv).getMaterial().blocksMovement()) {
                  BlockPos lv4 = lv.down();
                  FluidState lv5 = world.getFluidState(lv4);
                  if (this.isEmptyOrThis(lv5)) {
                     f = lv5.getHeight();
                     if (f > 0.0F) {
                        g = state.getHeight() - (f - 0.8888889F);
                     }
                  }
               }
            } else if (f > 0.0F) {
               g = state.getHeight() - f;
            }

            if (g != 0.0F) {
               d += (double)((float)lv2.getOffsetX() * g);
               e += (double)((float)lv2.getOffsetZ() * g);
            }
         }
      }

      Vec3d lv6 = new Vec3d(d, 0.0, e);
      if ((Boolean)state.get(FALLING)) {
         Iterator var17 = Direction.Type.HORIZONTAL.iterator();

         Direction lv7;
         do {
            if (!var17.hasNext()) {
               return lv6.normalize();
            }

            lv7 = (Direction)var17.next();
            lv.set(pos, (Direction)lv7);
         } while(!this.isFlowBlocked(world, lv, lv7) && !this.isFlowBlocked(world, lv.up(), lv7));

         lv6 = lv6.normalize().add(0.0, -6.0, 0.0);
      }

      return lv6.normalize();
   }

   private boolean isEmptyOrThis(FluidState state) {
      return state.isEmpty() || state.getFluid().matchesType(this);
   }

   protected boolean isFlowBlocked(BlockView world, BlockPos pos, Direction direction) {
      BlockState lv = world.getBlockState(pos);
      FluidState lv2 = world.getFluidState(pos);
      if (lv2.getFluid().matchesType(this)) {
         return false;
      } else if (direction == Direction.UP) {
         return true;
      } else {
         return lv.getBlock() instanceof IceBlock ? false : lv.isSideSolidFullSquare(world, pos, direction);
      }
   }

   protected void tryFlow(World world, BlockPos fluidPos, FluidState state) {
      if (!state.isEmpty()) {
         BlockState lv = world.getBlockState(fluidPos);
         BlockPos lv2 = fluidPos.down();
         BlockState lv3 = world.getBlockState(lv2);
         FluidState lv4 = this.getUpdatedState(world, lv2, lv3);
         if (this.canFlow(world, fluidPos, lv, Direction.DOWN, lv2, lv3, world.getFluidState(lv2), lv4.getFluid())) {
            this.flow(world, lv2, lv3, Direction.DOWN, lv4);
            if (this.countNeighboringSources(world, fluidPos) >= 3) {
               this.flowToSides(world, fluidPos, state, lv);
            }
         } else if (state.isStill() || !this.canFlowDownTo(world, lv4.getFluid(), fluidPos, lv, lv2, lv3)) {
            this.flowToSides(world, fluidPos, state, lv);
         }

      }
   }

   private void flowToSides(World world, BlockPos pos, FluidState fluidState, BlockState blockState) {
      int i = fluidState.getLevel() - this.getLevelDecreasePerBlock(world);
      if ((Boolean)fluidState.get(FALLING)) {
         i = 7;
      }

      if (i > 0) {
         Map map = this.getSpread(world, pos, blockState);
         Iterator var7 = map.entrySet().iterator();

         while(var7.hasNext()) {
            Map.Entry entry = (Map.Entry)var7.next();
            Direction lv = (Direction)entry.getKey();
            FluidState lv2 = (FluidState)entry.getValue();
            BlockPos lv3 = pos.offset(lv);
            BlockState lv4 = world.getBlockState(lv3);
            if (this.canFlow(world, pos, blockState, lv, lv3, lv4, world.getFluidState(lv3), lv2.getFluid())) {
               this.flow(world, lv3, lv4, lv, lv2);
            }
         }

      }
   }

   protected FluidState getUpdatedState(World world, BlockPos pos, BlockState state) {
      int i = 0;
      int j = 0;
      Iterator var6 = Direction.Type.HORIZONTAL.iterator();

      while(var6.hasNext()) {
         Direction lv = (Direction)var6.next();
         BlockPos lv2 = pos.offset(lv);
         BlockState lv3 = world.getBlockState(lv2);
         FluidState lv4 = lv3.getFluidState();
         if (lv4.getFluid().matchesType(this) && this.receivesFlow(lv, world, pos, state, lv2, lv3)) {
            if (lv4.isStill()) {
               ++j;
            }

            i = Math.max(i, lv4.getLevel());
         }
      }

      if (this.isInfinite(world) && j >= 2) {
         BlockState lv5 = world.getBlockState(pos.down());
         FluidState lv6 = lv5.getFluidState();
         if (lv5.getMaterial().isSolid() || this.isMatchingAndStill(lv6)) {
            return this.getStill(false);
         }
      }

      BlockPos lv7 = pos.up();
      BlockState lv8 = world.getBlockState(lv7);
      FluidState lv9 = lv8.getFluidState();
      if (!lv9.isEmpty() && lv9.getFluid().matchesType(this) && this.receivesFlow(Direction.UP, world, pos, state, lv7, lv8)) {
         return this.getFlowing(8, true);
      } else {
         int k = i - this.getLevelDecreasePerBlock(world);
         if (k <= 0) {
            return Fluids.EMPTY.getDefaultState();
         } else {
            return this.getFlowing(k, false);
         }
      }
   }

   private boolean receivesFlow(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
      Object2ByteLinkedOpenHashMap object2ByteLinkedOpenHashMap;
      if (!state.getBlock().hasDynamicBounds() && !fromState.getBlock().hasDynamicBounds()) {
         object2ByteLinkedOpenHashMap = (Object2ByteLinkedOpenHashMap)field_15901.get();
      } else {
         object2ByteLinkedOpenHashMap = null;
      }

      Block.NeighborGroup lv;
      if (object2ByteLinkedOpenHashMap != null) {
         lv = new Block.NeighborGroup(state, fromState, face);
         byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(lv);
         if (b != 127) {
            return b != 0;
         }
      } else {
         lv = null;
      }

      VoxelShape lv2 = state.getCollisionShape(world, pos);
      VoxelShape lv3 = fromState.getCollisionShape(world, fromPos);
      boolean bl = !VoxelShapes.adjacentSidesCoverSquare(lv2, lv3, face);
      if (object2ByteLinkedOpenHashMap != null) {
         if (object2ByteLinkedOpenHashMap.size() == 200) {
            object2ByteLinkedOpenHashMap.removeLastByte();
         }

         object2ByteLinkedOpenHashMap.putAndMoveToFirst(lv, (byte)(bl ? 1 : 0));
      }

      return bl;
   }

   public abstract Fluid getFlowing();

   public FluidState getFlowing(int level, boolean falling) {
      return (FluidState)((FluidState)this.getFlowing().getDefaultState().with(LEVEL, level)).with(FALLING, falling);
   }

   public abstract Fluid getStill();

   public FluidState getStill(boolean falling) {
      return (FluidState)this.getStill().getDefaultState().with(FALLING, falling);
   }

   protected abstract boolean isInfinite(World world);

   protected void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
      if (state.getBlock() instanceof FluidFillable) {
         ((FluidFillable)state.getBlock()).tryFillWithFluid(world, pos, state, fluidState);
      } else {
         if (!state.isAir()) {
            this.beforeBreakingBlock(world, pos, state);
         }

         world.setBlockState(pos, fluidState.getBlockState(), Block.NOTIFY_ALL);
      }

   }

   protected abstract void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state);

   private static short method_15747(BlockPos arg, BlockPos arg2) {
      int i = arg2.getX() - arg.getX();
      int j = arg2.getZ() - arg.getZ();
      return (short)((i + 128 & 255) << 8 | j + 128 & 255);
   }

   protected int getFlowSpeedBetween(WorldView world, BlockPos arg2, int i, Direction arg3, BlockState arg4, BlockPos arg5, Short2ObjectMap short2ObjectMap, Short2BooleanMap short2BooleanMap) {
      int j = 1000;
      Iterator var10 = Direction.Type.HORIZONTAL.iterator();

      while(var10.hasNext()) {
         Direction lv = (Direction)var10.next();
         if (lv != arg3) {
            BlockPos lv2 = arg2.offset(lv);
            short s = method_15747(arg5, lv2);
            Pair pair = (Pair)short2ObjectMap.computeIfAbsent(s, (sx) -> {
               BlockState lv = world.getBlockState(lv2);
               return Pair.of(lv, lv.getFluidState());
            });
            BlockState lv3 = (BlockState)pair.getFirst();
            FluidState lv4 = (FluidState)pair.getSecond();
            if (this.canFlowThrough(world, this.getFlowing(), arg2, arg4, lv, lv2, lv3, lv4)) {
               boolean bl = short2BooleanMap.computeIfAbsent(s, (sx) -> {
                  BlockPos lv = lv2.down();
                  BlockState lv2x = world.getBlockState(lv);
                  return this.canFlowDownTo(world, this.getFlowing(), lv2, lv3, lv, lv2x);
               });
               if (bl) {
                  return i;
               }

               if (i < this.getFlowSpeed(world)) {
                  int k = this.getFlowSpeedBetween(world, lv2, i + 1, lv.getOpposite(), lv3, arg5, short2ObjectMap, short2BooleanMap);
                  if (k < j) {
                     j = k;
                  }
               }
            }
         }
      }

      return j;
   }

   private boolean canFlowDownTo(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
      if (!this.receivesFlow(Direction.DOWN, world, pos, state, fromPos, fromState)) {
         return false;
      } else {
         return fromState.getFluidState().getFluid().matchesType(this) ? true : this.canFill(world, fromPos, fromState, fluid);
      }
   }

   private boolean canFlowThrough(BlockView world, Fluid fluid, BlockPos pos, BlockState state, Direction face, BlockPos fromPos, BlockState fromState, FluidState fluidState) {
      return !this.isMatchingAndStill(fluidState) && this.receivesFlow(face, world, pos, state, fromPos, fromState) && this.canFill(world, fromPos, fromState, fluid);
   }

   private boolean isMatchingAndStill(FluidState state) {
      return state.getFluid().matchesType(this) && state.isStill();
   }

   protected abstract int getFlowSpeed(WorldView world);

   private int countNeighboringSources(WorldView world, BlockPos pos) {
      int i = 0;
      Iterator var4 = Direction.Type.HORIZONTAL.iterator();

      while(var4.hasNext()) {
         Direction lv = (Direction)var4.next();
         BlockPos lv2 = pos.offset(lv);
         FluidState lv3 = world.getFluidState(lv2);
         if (this.isMatchingAndStill(lv3)) {
            ++i;
         }
      }

      return i;
   }

   protected Map getSpread(World world, BlockPos pos, BlockState state) {
      int i = 1000;
      Map map = Maps.newEnumMap(Direction.class);
      Short2ObjectMap short2ObjectMap = new Short2ObjectOpenHashMap();
      Short2BooleanMap short2BooleanMap = new Short2BooleanOpenHashMap();
      Iterator var8 = Direction.Type.HORIZONTAL.iterator();

      while(var8.hasNext()) {
         Direction lv = (Direction)var8.next();
         BlockPos lv2 = pos.offset(lv);
         short s = method_15747(pos, lv2);
         Pair pair = (Pair)short2ObjectMap.computeIfAbsent(s, (sx) -> {
            BlockState lv = world.getBlockState(lv2);
            return Pair.of(lv, lv.getFluidState());
         });
         BlockState lv3 = (BlockState)pair.getFirst();
         FluidState lv4 = (FluidState)pair.getSecond();
         FluidState lv5 = this.getUpdatedState(world, lv2, lv3);
         if (this.canFlowThrough(world, lv5.getFluid(), pos, state, lv, lv2, lv3, lv4)) {
            BlockPos lv6 = lv2.down();
            boolean bl = short2BooleanMap.computeIfAbsent(s, (sx) -> {
               BlockState lv = world.getBlockState(lv6);
               return this.canFlowDownTo(world, this.getFlowing(), lv2, lv3, lv6, lv);
            });
            int j;
            if (bl) {
               j = 0;
            } else {
               j = this.getFlowSpeedBetween(world, lv2, 1, lv.getOpposite(), lv3, pos, short2ObjectMap, short2BooleanMap);
            }

            if (j < i) {
               map.clear();
            }

            if (j <= i) {
               map.put(lv, lv5);
               i = j;
            }
         }
      }

      return map;
   }

   private boolean canFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
      Block lv = state.getBlock();
      if (lv instanceof FluidFillable) {
         return ((FluidFillable)lv).canFillWithFluid(world, pos, state, fluid);
      } else if (!(lv instanceof DoorBlock) && !state.isIn(BlockTags.SIGNS) && !state.isOf(Blocks.LADDER) && !state.isOf(Blocks.SUGAR_CANE) && !state.isOf(Blocks.BUBBLE_COLUMN)) {
         if (!state.isOf(Blocks.NETHER_PORTAL) && !state.isOf(Blocks.END_PORTAL) && !state.isOf(Blocks.END_GATEWAY) && !state.isOf(Blocks.STRUCTURE_VOID)) {
            return !state.getMaterial().blocksMovement();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected boolean canFlow(BlockView world, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo, BlockState flowToBlockState, FluidState fluidState, Fluid fluid) {
      return fluidState.canBeReplacedWith(world, flowTo, fluid, flowDirection) && this.receivesFlow(flowDirection, world, fluidPos, fluidBlockState, flowTo, flowToBlockState) && this.canFill(world, flowTo, flowToBlockState, fluid);
   }

   protected abstract int getLevelDecreasePerBlock(WorldView world);

   protected int getNextTickDelay(World world, BlockPos pos, FluidState oldState, FluidState newState) {
      return this.getTickRate(world);
   }

   public void onScheduledTick(World world, BlockPos pos, FluidState state) {
      if (!state.isStill()) {
         FluidState lv = this.getUpdatedState(world, pos, world.getBlockState(pos));
         int i = this.getNextTickDelay(world, pos, state, lv);
         if (lv.isEmpty()) {
            state = lv;
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
         } else if (!lv.equals(state)) {
            state = lv;
            BlockState lv2 = lv.getBlockState();
            world.setBlockState(pos, lv2, Block.NOTIFY_LISTENERS);
            world.scheduleFluidTick(pos, lv.getFluid(), i);
            world.updateNeighborsAlways(pos, lv2.getBlock());
         }
      }

      this.tryFlow(world, pos, state);
   }

   protected static int getBlockStateLevel(FluidState state) {
      return state.isStill() ? 0 : 8 - Math.min(state.getLevel(), 8) + ((Boolean)state.get(FALLING) ? 8 : 0);
   }

   private static boolean isFluidAboveEqual(FluidState state, BlockView world, BlockPos pos) {
      return state.getFluid().matchesType(world.getFluidState(pos.up()).getFluid());
   }

   public float getHeight(FluidState state, BlockView world, BlockPos pos) {
      return isFluidAboveEqual(state, world, pos) ? 1.0F : state.getHeight();
   }

   public float getHeight(FluidState state) {
      return (float)state.getLevel() / 9.0F;
   }

   public abstract int getLevel(FluidState state);

   public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
      return state.getLevel() == 9 && isFluidAboveEqual(state, world, pos) ? VoxelShapes.fullCube() : (VoxelShape)this.shapeCache.computeIfAbsent(state, (state2) -> {
         return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, (double)state2.getHeight(world, pos), 1.0);
      });
   }

   static {
      FALLING = Properties.FALLING;
      LEVEL = Properties.LEVEL_1_8;
      field_15901 = ThreadLocal.withInitial(() -> {
         Object2ByteLinkedOpenHashMap object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap(200) {
            protected void rehash(int i) {
            }
         };
         object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
         return object2ByteLinkedOpenHashMap;
      });
   }
}
