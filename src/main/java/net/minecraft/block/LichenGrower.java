package net.minecraft.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class LichenGrower {
   public static final GrowType[] GROW_TYPES;
   private final GrowChecker growChecker;

   public LichenGrower(MultifaceGrowthBlock lichen) {
      this((GrowChecker)(new LichenGrowChecker(lichen)));
   }

   public LichenGrower(GrowChecker growChecker) {
      this.growChecker = growChecker;
   }

   public boolean canGrow(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return Direction.stream().anyMatch((direction2) -> {
         GrowChecker var10006 = this.growChecker;
         Objects.requireNonNull(var10006);
         return this.getGrowPos(state, world, pos, direction, direction2, var10006::canGrow).isPresent();
      });
   }

   public Optional grow(BlockState state, WorldAccess world, BlockPos pos, Random random) {
      return (Optional)Direction.shuffle(random).stream().filter((direction) -> {
         return this.growChecker.canGrow(state, direction);
      }).map((direction) -> {
         return this.grow(state, world, pos, direction, random, false);
      }).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
   }

   public long grow(BlockState state, WorldAccess world, BlockPos pos, boolean markForPostProcessing) {
      return (Long)Direction.stream().filter((direction) -> {
         return this.growChecker.canGrow(state, direction);
      }).map((direction) -> {
         return this.grow(state, world, pos, direction, markForPostProcessing);
      }).reduce(0L, Long::sum);
   }

   public Optional grow(BlockState state, WorldAccess world, BlockPos pos, Direction direction, Random random, boolean markForPostProcessing) {
      return (Optional)Direction.shuffle(random).stream().map((direction2) -> {
         return this.grow(state, world, pos, direction, direction2, markForPostProcessing);
      }).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
   }

   private long grow(BlockState state, WorldAccess world, BlockPos pos, Direction direction, boolean markForPostProcessing) {
      return Direction.stream().map((direction2) -> {
         return this.grow(state, world, pos, direction, direction2, markForPostProcessing);
      }).filter(Optional::isPresent).count();
   }

   @VisibleForTesting
   public Optional grow(BlockState state, WorldAccess world, BlockPos pos, Direction oldDirection, Direction newDirection, boolean markForPostProcessing) {
      GrowChecker var10006 = this.growChecker;
      Objects.requireNonNull(var10006);
      return this.getGrowPos(state, world, pos, oldDirection, newDirection, var10006::canGrow).flatMap((growPos) -> {
         return this.place(world, growPos, markForPostProcessing);
      });
   }

   public Optional getGrowPos(BlockState state, BlockView world, BlockPos pos, Direction oldDirection, Direction newDirection, GrowPosPredicate predicate) {
      if (newDirection.getAxis() == oldDirection.getAxis()) {
         return Optional.empty();
      } else if (!this.growChecker.canGrow(state) && (!this.growChecker.hasDirection(state, oldDirection) || this.growChecker.hasDirection(state, newDirection))) {
         return Optional.empty();
      } else {
         GrowType[] var7 = this.growChecker.getGrowTypes();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            GrowType lv = var7[var9];
            GrowPos lv2 = lv.getGrowPos(pos, newDirection, oldDirection);
            if (predicate.test(world, pos, lv2)) {
               return Optional.of(lv2);
            }
         }

         return Optional.empty();
      }
   }

   public Optional place(WorldAccess world, GrowPos pos, boolean markForPostProcessing) {
      BlockState lv = world.getBlockState(pos.pos());
      return this.growChecker.place(world, pos, lv, markForPostProcessing) ? Optional.of(pos) : Optional.empty();
   }

   static {
      GROW_TYPES = new GrowType[]{LichenGrower.GrowType.SAME_POSITION, LichenGrower.GrowType.SAME_PLANE, LichenGrower.GrowType.WRAP_AROUND};
   }

   public static class LichenGrowChecker implements GrowChecker {
      protected MultifaceGrowthBlock lichen;

      public LichenGrowChecker(MultifaceGrowthBlock lichen) {
         this.lichen = lichen;
      }

      @Nullable
      public BlockState getStateWithDirection(BlockState state, BlockView world, BlockPos pos, Direction direction) {
         return this.lichen.withDirection(state, world, pos, direction);
      }

      protected boolean canGrow(BlockView world, BlockPos pos, BlockPos growPos, Direction direction, BlockState state) {
         return state.isAir() || state.isOf(this.lichen) || state.isOf(Blocks.WATER) && state.getFluidState().isStill();
      }

      public boolean canGrow(BlockView world, BlockPos pos, GrowPos growPos) {
         BlockState lv = world.getBlockState(growPos.pos());
         return this.canGrow(world, pos, growPos.pos(), growPos.face(), lv) && this.lichen.canGrowWithDirection(world, lv, growPos.pos(), growPos.face());
      }
   }

   public interface GrowChecker {
      @Nullable
      BlockState getStateWithDirection(BlockState state, BlockView world, BlockPos pos, Direction direction);

      boolean canGrow(BlockView world, BlockPos pos, GrowPos growPos);

      default GrowType[] getGrowTypes() {
         return LichenGrower.GROW_TYPES;
      }

      default boolean hasDirection(BlockState state, Direction direction) {
         return MultifaceGrowthBlock.hasDirection(state, direction);
      }

      default boolean canGrow(BlockState state) {
         return false;
      }

      default boolean canGrow(BlockState state, Direction direction) {
         return this.canGrow(state) || this.hasDirection(state, direction);
      }

      default boolean place(WorldAccess world, GrowPos growPos, BlockState state, boolean markForPostProcessing) {
         BlockState lv = this.getStateWithDirection(state, world, growPos.pos(), growPos.face());
         if (lv != null) {
            if (markForPostProcessing) {
               world.getChunk(growPos.pos()).markBlockForPostProcessing(growPos.pos());
            }

            return world.setBlockState(growPos.pos(), lv, Block.NOTIFY_LISTENERS);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface GrowPosPredicate {
      boolean test(BlockView world, BlockPos pos, GrowPos growPos);
   }

   public static enum GrowType {
      SAME_POSITION {
         public GrowPos getGrowPos(BlockPos pos, Direction newDirection, Direction oldDirection) {
            return new GrowPos(pos, newDirection);
         }
      },
      SAME_PLANE {
         public GrowPos getGrowPos(BlockPos pos, Direction newDirection, Direction oldDirection) {
            return new GrowPos(pos.offset(newDirection), oldDirection);
         }
      },
      WRAP_AROUND {
         public GrowPos getGrowPos(BlockPos pos, Direction newDirection, Direction oldDirection) {
            return new GrowPos(pos.offset(newDirection).offset(oldDirection), newDirection.getOpposite());
         }
      };

      public abstract GrowPos getGrowPos(BlockPos pos, Direction newDirection, Direction oldDirection);

      // $FF: synthetic method
      private static GrowType[] method_41465() {
         return new GrowType[]{SAME_POSITION, SAME_PLANE, WRAP_AROUND};
      }
   }

   public static record GrowPos(BlockPos pos, Direction face) {
      public GrowPos(BlockPos arg, Direction arg2) {
         this.pos = arg;
         this.face = arg2;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public Direction face() {
         return this.face;
      }
   }
}
