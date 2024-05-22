/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature.util;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.TestableWorld;

public abstract class CaveSurface {
    public static Bounded method_35326(int i, int j) {
        return new Bounded(i - 1, j + 1);
    }

    public static Bounded createBounded(int floor, int ceiling) {
        return new Bounded(floor, ceiling);
    }

    public static CaveSurface createHalfWithCeiling(int ceiling) {
        return new Half(ceiling, false);
    }

    public static CaveSurface method_35327(int i) {
        return new Half(i + 1, false);
    }

    public static CaveSurface createHalfWithFloor(int floor) {
        return new Half(floor, true);
    }

    public static CaveSurface method_35329(int i) {
        return new Half(i - 1, true);
    }

    public static CaveSurface createEmpty() {
        return Empty.INSTANCE;
    }

    public static CaveSurface create(OptionalInt ceilingHeight, OptionalInt floorHeight) {
        if (ceilingHeight.isPresent() && floorHeight.isPresent()) {
            return CaveSurface.createBounded(ceilingHeight.getAsInt(), floorHeight.getAsInt());
        }
        if (ceilingHeight.isPresent()) {
            return CaveSurface.createHalfWithFloor(ceilingHeight.getAsInt());
        }
        if (floorHeight.isPresent()) {
            return CaveSurface.createHalfWithCeiling(floorHeight.getAsInt());
        }
        return CaveSurface.createEmpty();
    }

    public abstract OptionalInt getCeilingHeight();

    public abstract OptionalInt getFloorHeight();

    public abstract OptionalInt getOptionalHeight();

    public CaveSurface withFloor(OptionalInt floor) {
        return CaveSurface.create(floor, this.getCeilingHeight());
    }

    public CaveSurface withCeiling(OptionalInt ceiling) {
        return CaveSurface.create(this.getFloorHeight(), ceiling);
    }

    public static Optional<CaveSurface> create(TestableWorld world, BlockPos pos, int height, Predicate<BlockState> canGenerate, Predicate<BlockState> canReplace) {
        BlockPos.Mutable lv = pos.mutableCopy();
        if (!world.testBlockState(pos, canGenerate)) {
            return Optional.empty();
        }
        int j = pos.getY();
        OptionalInt optionalInt = CaveSurface.getCaveSurface(world, height, canGenerate, canReplace, lv, j, Direction.UP);
        OptionalInt optionalInt2 = CaveSurface.getCaveSurface(world, height, canGenerate, canReplace, lv, j, Direction.DOWN);
        return Optional.of(CaveSurface.create(optionalInt2, optionalInt));
    }

    private static OptionalInt getCaveSurface(TestableWorld world, int height, Predicate<BlockState> canGenerate, Predicate<BlockState> canReplace, BlockPos.Mutable mutablePos, int y, Direction direction) {
        mutablePos.setY(y);
        for (int k = 1; k < height && world.testBlockState(mutablePos, canGenerate); ++k) {
            mutablePos.move(direction);
        }
        return world.testBlockState(mutablePos, canReplace) ? OptionalInt.of(mutablePos.getY()) : OptionalInt.empty();
    }

    public static final class Bounded
    extends CaveSurface {
        private final int floor;
        private final int ceiling;

        protected Bounded(int floor, int ceiling) {
            this.floor = floor;
            this.ceiling = ceiling;
            if (this.getHeight() < 0) {
                throw new IllegalArgumentException("Column of negative height: " + String.valueOf(this));
            }
        }

        @Override
        public OptionalInt getCeilingHeight() {
            return OptionalInt.of(this.ceiling);
        }

        @Override
        public OptionalInt getFloorHeight() {
            return OptionalInt.of(this.floor);
        }

        @Override
        public OptionalInt getOptionalHeight() {
            return OptionalInt.of(this.getHeight());
        }

        public int getCeiling() {
            return this.ceiling;
        }

        public int getFloor() {
            return this.floor;
        }

        public int getHeight() {
            return this.ceiling - this.floor - 1;
        }

        public String toString() {
            return "C(" + this.ceiling + "-" + this.floor + ")";
        }
    }

    public static final class Half
    extends CaveSurface {
        private final int height;
        private final boolean floor;

        public Half(int height, boolean floor) {
            this.height = height;
            this.floor = floor;
        }

        @Override
        public OptionalInt getCeilingHeight() {
            return this.floor ? OptionalInt.empty() : OptionalInt.of(this.height);
        }

        @Override
        public OptionalInt getFloorHeight() {
            return this.floor ? OptionalInt.of(this.height) : OptionalInt.empty();
        }

        @Override
        public OptionalInt getOptionalHeight() {
            return OptionalInt.empty();
        }

        public String toString() {
            return this.floor ? "C(" + this.height + "-)" : "C(-" + this.height + ")";
        }
    }

    public static final class Empty
    extends CaveSurface {
        static final Empty INSTANCE = new Empty();

        private Empty() {
        }

        @Override
        public OptionalInt getCeilingHeight() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getFloorHeight() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getOptionalHeight() {
            return OptionalInt.empty();
        }

        public String toString() {
            return "C(-)";
        }
    }
}

