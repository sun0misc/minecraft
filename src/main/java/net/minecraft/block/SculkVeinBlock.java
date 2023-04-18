package net.minecraft.block;

import java.util.Collection;
import java.util.Iterator;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class SculkVeinBlock extends MultifaceGrowthBlock implements SculkSpreadable, Waterloggable {
   private static final BooleanProperty WATERLOGGED;
   private final LichenGrower allGrowTypeGrower;
   private final LichenGrower samePositionOnlyGrower;

   public SculkVeinBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.allGrowTypeGrower = new LichenGrower(new SculkVeinGrowChecker(LichenGrower.GROW_TYPES));
      this.samePositionOnlyGrower = new LichenGrower(new SculkVeinGrowChecker(new LichenGrower.GrowType[]{LichenGrower.GrowType.SAME_POSITION}));
      this.setDefaultState((BlockState)this.getDefaultState().with(WATERLOGGED, false));
   }

   public LichenGrower getGrower() {
      return this.allGrowTypeGrower;
   }

   public LichenGrower getSamePositionOnlyGrower() {
      return this.samePositionOnlyGrower;
   }

   public static boolean place(WorldAccess world, BlockPos pos, BlockState state, Collection directions) {
      boolean bl = false;
      BlockState lv = Blocks.SCULK_VEIN.getDefaultState();
      Iterator var6 = directions.iterator();

      while(var6.hasNext()) {
         Direction lv2 = (Direction)var6.next();
         BlockPos lv3 = pos.offset(lv2);
         if (canGrowOn(world, lv2, lv3, world.getBlockState(lv3))) {
            lv = (BlockState)lv.with(getProperty(lv2), true);
            bl = true;
         }
      }

      if (!bl) {
         return false;
      } else {
         if (!state.getFluidState().isEmpty()) {
            lv = (BlockState)lv.with(WATERLOGGED, true);
         }

         world.setBlockState(pos, lv, Block.NOTIFY_ALL);
         return true;
      }
   }

   public void spreadAtSamePosition(WorldAccess world, BlockState state, BlockPos pos, Random random) {
      if (state.isOf(this)) {
         Direction[] var5 = DIRECTIONS;
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Direction lv = var5[var7];
            BooleanProperty lv2 = getProperty(lv);
            if ((Boolean)state.get(lv2) && world.getBlockState(pos.offset(lv)).isOf(Blocks.SCULK)) {
               state = (BlockState)state.with(lv2, false);
            }
         }

         if (!hasAnyDirection(state)) {
            FluidState lv3 = world.getFluidState(pos);
            state = (lv3.isEmpty() ? Blocks.AIR : Blocks.WATER).getDefaultState();
         }

         world.setBlockState(pos, state, Block.NOTIFY_ALL);
         SculkSpreadable.super.spreadAtSamePosition(world, state, pos, random);
      }
   }

   public int spread(SculkSpreadManager.Cursor cursor, WorldAccess world, BlockPos catalystPos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock) {
      if (shouldConvertToBlock && this.convertToBlock(spreadManager, world, cursor.getPos(), random)) {
         return cursor.getCharge() - 1;
      } else {
         return random.nextInt(spreadManager.getSpreadChance()) == 0 ? MathHelper.floor((float)cursor.getCharge() * 0.5F) : cursor.getCharge();
      }
   }

   private boolean convertToBlock(SculkSpreadManager spreadManager, WorldAccess world, BlockPos pos, Random random) {
      BlockState lv = world.getBlockState(pos);
      TagKey lv2 = spreadManager.getReplaceableTag();
      Iterator var7 = Direction.shuffle(random).iterator();

      while(var7.hasNext()) {
         Direction lv3 = (Direction)var7.next();
         if (hasDirection(lv, lv3)) {
            BlockPos lv4 = pos.offset(lv3);
            BlockState lv5 = world.getBlockState(lv4);
            if (lv5.isIn(lv2)) {
               BlockState lv6 = Blocks.SCULK.getDefaultState();
               world.setBlockState(lv4, lv6, Block.NOTIFY_ALL);
               Block.pushEntitiesUpBeforeBlockChange(lv5, lv6, world, lv4);
               world.playSound((PlayerEntity)null, lv4, SoundEvents.BLOCK_SCULK_SPREAD, SoundCategory.BLOCKS, 1.0F, 1.0F);
               this.allGrowTypeGrower.grow(lv6, world, lv4, spreadManager.isWorldGen());
               Direction lv7 = lv3.getOpposite();
               Direction[] var13 = DIRECTIONS;
               int var14 = var13.length;

               for(int var15 = 0; var15 < var14; ++var15) {
                  Direction lv8 = var13[var15];
                  if (lv8 != lv7) {
                     BlockPos lv9 = lv4.offset(lv8);
                     BlockState lv10 = world.getBlockState(lv9);
                     if (lv10.isOf(this)) {
                        this.spreadAtSamePosition(world, lv10, lv9, random);
                     }
                  }
               }

               return true;
            }
         }
      }

      return false;
   }

   public static boolean veinCoversSculkReplaceable(WorldAccess world, BlockState state, BlockPos pos) {
      if (!state.isOf(Blocks.SCULK_VEIN)) {
         return false;
      } else {
         Direction[] var3 = DIRECTIONS;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Direction lv = var3[var5];
            if (hasDirection(state, lv) && world.getBlockState(pos.offset(lv)).isIn(BlockTags.SCULK_REPLACEABLE)) {
               return true;
            }
         }

         return false;
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   protected void appendProperties(StateManager.Builder builder) {
      super.appendProperties(builder);
      builder.add(WATERLOGGED);
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return !context.getStack().isOf(Items.SCULK_VEIN) || super.canReplace(state, context);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
   }

   class SculkVeinGrowChecker extends LichenGrower.LichenGrowChecker {
      private final LichenGrower.GrowType[] growTypes;

      public SculkVeinGrowChecker(LichenGrower.GrowType... growTypes) {
         super(SculkVeinBlock.this);
         this.growTypes = growTypes;
      }

      public boolean canGrow(BlockView world, BlockPos pos, BlockPos growPos, Direction direction, BlockState state) {
         BlockState lv = world.getBlockState(growPos.offset(direction));
         if (!lv.isOf(Blocks.SCULK) && !lv.isOf(Blocks.SCULK_CATALYST) && !lv.isOf(Blocks.MOVING_PISTON)) {
            if (pos.getManhattanDistance(growPos) == 2) {
               BlockPos lv2 = pos.offset(direction.getOpposite());
               if (world.getBlockState(lv2).isSideSolidFullSquare(world, lv2, direction)) {
                  return false;
               }
            }

            FluidState lv3 = state.getFluidState();
            if (!lv3.isEmpty() && !lv3.isOf(Fluids.WATER)) {
               return false;
            } else {
               Material lv4 = state.getMaterial();
               if (state.isIn(BlockTags.FIRE)) {
                  return false;
               } else {
                  return state.isReplaceable() || super.canGrow(world, pos, growPos, direction, state);
               }
            }
         } else {
            return false;
         }
      }

      public LichenGrower.GrowType[] getGrowTypes() {
         return this.growTypes;
      }

      public boolean canGrow(BlockState state) {
         return !state.isOf(Blocks.SCULK_VEIN);
      }
   }
}
