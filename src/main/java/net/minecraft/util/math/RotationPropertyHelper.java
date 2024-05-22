/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import java.util.Optional;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationCalculator;

public class RotationPropertyHelper {
    private static final RotationCalculator CALCULATOR = new RotationCalculator(4);
    private static final int MAX = CALCULATOR.getMax();
    private static final int NORTH = 0;
    private static final int EAST = 4;
    private static final int SOUTH = 8;
    private static final int WEST = 12;

    public static int getMax() {
        return MAX;
    }

    public static int fromDirection(Direction direction) {
        return CALCULATOR.toRotation(direction);
    }

    public static int fromYaw(float yaw) {
        return CALCULATOR.toClampedRotation(yaw);
    }

    public static Optional<Direction> toDirection(int rotation) {
        Direction lv = switch (rotation) {
            case 0 -> Direction.NORTH;
            case 4 -> Direction.EAST;
            case 8 -> Direction.SOUTH;
            case 12 -> Direction.WEST;
            default -> null;
        };
        return Optional.ofNullable(lv);
    }

    public static float toDegrees(int rotation) {
        return CALCULATOR.toWrappedDegrees(rotation);
    }
}

