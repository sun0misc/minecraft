/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public enum CubeFace {
    DOWN(new Corner(DirectionIds.WEST, DirectionIds.DOWN, DirectionIds.SOUTH), new Corner(DirectionIds.WEST, DirectionIds.DOWN, DirectionIds.NORTH), new Corner(DirectionIds.EAST, DirectionIds.DOWN, DirectionIds.NORTH), new Corner(DirectionIds.EAST, DirectionIds.DOWN, DirectionIds.SOUTH)),
    UP(new Corner(DirectionIds.WEST, DirectionIds.UP, DirectionIds.NORTH), new Corner(DirectionIds.WEST, DirectionIds.UP, DirectionIds.SOUTH), new Corner(DirectionIds.EAST, DirectionIds.UP, DirectionIds.SOUTH), new Corner(DirectionIds.EAST, DirectionIds.UP, DirectionIds.NORTH)),
    NORTH(new Corner(DirectionIds.EAST, DirectionIds.UP, DirectionIds.NORTH), new Corner(DirectionIds.EAST, DirectionIds.DOWN, DirectionIds.NORTH), new Corner(DirectionIds.WEST, DirectionIds.DOWN, DirectionIds.NORTH), new Corner(DirectionIds.WEST, DirectionIds.UP, DirectionIds.NORTH)),
    SOUTH(new Corner(DirectionIds.WEST, DirectionIds.UP, DirectionIds.SOUTH), new Corner(DirectionIds.WEST, DirectionIds.DOWN, DirectionIds.SOUTH), new Corner(DirectionIds.EAST, DirectionIds.DOWN, DirectionIds.SOUTH), new Corner(DirectionIds.EAST, DirectionIds.UP, DirectionIds.SOUTH)),
    WEST(new Corner(DirectionIds.WEST, DirectionIds.UP, DirectionIds.NORTH), new Corner(DirectionIds.WEST, DirectionIds.DOWN, DirectionIds.NORTH), new Corner(DirectionIds.WEST, DirectionIds.DOWN, DirectionIds.SOUTH), new Corner(DirectionIds.WEST, DirectionIds.UP, DirectionIds.SOUTH)),
    EAST(new Corner(DirectionIds.EAST, DirectionIds.UP, DirectionIds.SOUTH), new Corner(DirectionIds.EAST, DirectionIds.DOWN, DirectionIds.SOUTH), new Corner(DirectionIds.EAST, DirectionIds.DOWN, DirectionIds.NORTH), new Corner(DirectionIds.EAST, DirectionIds.UP, DirectionIds.NORTH));

    private static final CubeFace[] DIRECTION_LOOKUP;
    private final Corner[] corners;

    public static CubeFace getFace(Direction direction) {
        return DIRECTION_LOOKUP[direction.getId()];
    }

    private CubeFace(Corner ... corners) {
        this.corners = corners;
    }

    public Corner getCorner(int corner) {
        return this.corners[corner];
    }

    static {
        DIRECTION_LOOKUP = Util.make(new CubeFace[6], lookup -> {
            lookup[DirectionIds.DOWN] = DOWN;
            lookup[DirectionIds.UP] = UP;
            lookup[DirectionIds.NORTH] = NORTH;
            lookup[DirectionIds.SOUTH] = SOUTH;
            lookup[DirectionIds.WEST] = WEST;
            lookup[DirectionIds.EAST] = EAST;
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static class Corner {
        public final int xSide;
        public final int ySide;
        public final int zSide;

        Corner(int xSide, int ySide, int zSide) {
            this.xSide = xSide;
            this.ySide = ySide;
            this.zSide = zSide;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class DirectionIds {
        public static final int SOUTH = Direction.SOUTH.getId();
        public static final int UP = Direction.UP.getId();
        public static final int EAST = Direction.EAST.getId();
        public static final int NORTH = Direction.NORTH.getId();
        public static final int DOWN = Direction.DOWN.getId();
        public static final int WEST = Direction.WEST.getId();
    }
}

