/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class OceanMonumentGenerator {
    private OceanMonumentGenerator() {
    }

    static class DoubleYZRoomFactory
    implements PieceFactory {
        DoubleYZRoomFactory() {
        }

        @Override
        public boolean canGenerate(PieceSetting setting) {
            if (setting.neighborPresences[Direction.NORTH.getId()] && !setting.neighbors[Direction.NORTH.getId()].used && setting.neighborPresences[Direction.UP.getId()] && !setting.neighbors[Direction.UP.getId()].used) {
                PieceSetting lv = setting.neighbors[Direction.NORTH.getId()];
                return lv.neighborPresences[Direction.UP.getId()] && !lv.neighbors[Direction.UP.getId()].used;
            }
            return false;
        }

        @Override
        public Piece generate(Direction direction, PieceSetting setting, Random random) {
            setting.used = true;
            setting.neighbors[Direction.NORTH.getId()].used = true;
            setting.neighbors[Direction.UP.getId()].used = true;
            setting.neighbors[Direction.NORTH.getId()].neighbors[Direction.UP.getId()].used = true;
            return new DoubleYZRoom(direction, setting);
        }
    }

    static class DoubleXYRoomFactory
    implements PieceFactory {
        DoubleXYRoomFactory() {
        }

        @Override
        public boolean canGenerate(PieceSetting setting) {
            if (setting.neighborPresences[Direction.EAST.getId()] && !setting.neighbors[Direction.EAST.getId()].used && setting.neighborPresences[Direction.UP.getId()] && !setting.neighbors[Direction.UP.getId()].used) {
                PieceSetting lv = setting.neighbors[Direction.EAST.getId()];
                return lv.neighborPresences[Direction.UP.getId()] && !lv.neighbors[Direction.UP.getId()].used;
            }
            return false;
        }

        @Override
        public Piece generate(Direction direction, PieceSetting setting, Random random) {
            setting.used = true;
            setting.neighbors[Direction.EAST.getId()].used = true;
            setting.neighbors[Direction.UP.getId()].used = true;
            setting.neighbors[Direction.EAST.getId()].neighbors[Direction.UP.getId()].used = true;
            return new DoubleXYRoom(direction, setting);
        }
    }

    static class DoubleZRoomFactory
    implements PieceFactory {
        DoubleZRoomFactory() {
        }

        @Override
        public boolean canGenerate(PieceSetting setting) {
            return setting.neighborPresences[Direction.NORTH.getId()] && !setting.neighbors[Direction.NORTH.getId()].used;
        }

        @Override
        public Piece generate(Direction direction, PieceSetting setting, Random random) {
            PieceSetting lv = setting;
            if (!setting.neighborPresences[Direction.NORTH.getId()] || setting.neighbors[Direction.NORTH.getId()].used) {
                lv = setting.neighbors[Direction.SOUTH.getId()];
            }
            lv.used = true;
            lv.neighbors[Direction.NORTH.getId()].used = true;
            return new DoubleZRoom(direction, lv);
        }
    }

    static class DoubleXRoomFactory
    implements PieceFactory {
        DoubleXRoomFactory() {
        }

        @Override
        public boolean canGenerate(PieceSetting setting) {
            return setting.neighborPresences[Direction.EAST.getId()] && !setting.neighbors[Direction.EAST.getId()].used;
        }

        @Override
        public Piece generate(Direction direction, PieceSetting setting, Random random) {
            setting.used = true;
            setting.neighbors[Direction.EAST.getId()].used = true;
            return new DoubleXRoom(direction, setting);
        }
    }

    static class DoubleYRoomFactory
    implements PieceFactory {
        DoubleYRoomFactory() {
        }

        @Override
        public boolean canGenerate(PieceSetting setting) {
            return setting.neighborPresences[Direction.UP.getId()] && !setting.neighbors[Direction.UP.getId()].used;
        }

        @Override
        public Piece generate(Direction direction, PieceSetting setting, Random random) {
            setting.used = true;
            setting.neighbors[Direction.UP.getId()].used = true;
            return new DoubleYRoom(direction, setting);
        }
    }

    static class SimpleRoomTopFactory
    implements PieceFactory {
        SimpleRoomTopFactory() {
        }

        @Override
        public boolean canGenerate(PieceSetting setting) {
            return !setting.neighborPresences[Direction.WEST.getId()] && !setting.neighborPresences[Direction.EAST.getId()] && !setting.neighborPresences[Direction.NORTH.getId()] && !setting.neighborPresences[Direction.SOUTH.getId()] && !setting.neighborPresences[Direction.UP.getId()];
        }

        @Override
        public Piece generate(Direction direction, PieceSetting setting, Random random) {
            setting.used = true;
            return new SimpleRoomTop(direction, setting);
        }
    }

    static class SimpleRoomFactory
    implements PieceFactory {
        SimpleRoomFactory() {
        }

        @Override
        public boolean canGenerate(PieceSetting setting) {
            return true;
        }

        @Override
        public Piece generate(Direction direction, PieceSetting setting, Random random) {
            setting.used = true;
            return new SimpleRoom(direction, setting, random);
        }
    }

    static interface PieceFactory {
        public boolean canGenerate(PieceSetting var1);

        public Piece generate(Direction var1, PieceSetting var2, Random var3);
    }

    static class PieceSetting {
        final int roomIndex;
        final PieceSetting[] neighbors = new PieceSetting[6];
        final boolean[] neighborPresences = new boolean[6];
        boolean used;
        boolean field_14484;
        private int field_14483;

        public PieceSetting(int index) {
            this.roomIndex = index;
        }

        public void setNeighbor(Direction orientation, PieceSetting setting) {
            this.neighbors[orientation.getId()] = setting;
            setting.neighbors[orientation.getOpposite().getId()] = this;
        }

        public void checkNeighborStates() {
            for (int i = 0; i < 6; ++i) {
                this.neighborPresences[i] = this.neighbors[i] != null;
            }
        }

        public boolean method_14783(int i) {
            if (this.field_14484) {
                return true;
            }
            this.field_14483 = i;
            for (int j = 0; j < 6; ++j) {
                if (this.neighbors[j] == null || !this.neighborPresences[j] || this.neighbors[j].field_14483 == i || !this.neighbors[j].method_14783(i)) continue;
                return true;
            }
            return false;
        }

        public boolean isAboveLevelThree() {
            return this.roomIndex >= 75;
        }

        public int countNeighbors() {
            int i = 0;
            for (int j = 0; j < 6; ++j) {
                if (!this.neighborPresences[j]) continue;
                ++i;
            }
            return i;
        }
    }

    public static class Penthouse
    extends Piece {
        public Penthouse(Direction orientation, BlockBox box) {
            super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, orientation, 1, box);
        }

        public Penthouse(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            int i;
            this.fillWithOutline(world, chunkBox, 2, -1, 2, 11, -1, 11, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 0, -1, 0, 1, -1, 11, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 12, -1, 0, 13, -1, 11, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 2, -1, 0, 11, -1, 1, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 2, -1, 12, 11, -1, 13, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 0, 0, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 13, 0, 0, 13, 0, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 0, 0, 12, 0, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 0, 13, 12, 0, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            for (i = 2; i <= 11; i += 3) {
                this.addBlock(world, SEA_LANTERN, 0, 0, i, chunkBox);
                this.addBlock(world, SEA_LANTERN, 13, 0, i, chunkBox);
                this.addBlock(world, SEA_LANTERN, i, 0, 0, chunkBox);
            }
            this.fillWithOutline(world, chunkBox, 2, 0, 3, 4, 0, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 9, 0, 3, 11, 0, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 4, 0, 9, 9, 0, 11, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.addBlock(world, PRISMARINE_BRICKS, 5, 0, 8, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 8, 0, 8, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 10, 0, 10, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 3, 0, 10, chunkBox);
            this.fillWithOutline(world, chunkBox, 3, 0, 3, 3, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 10, 0, 3, 10, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 6, 0, 10, 7, 0, 10, DARK_PRISMARINE, DARK_PRISMARINE, false);
            i = 3;
            for (int j = 0; j < 2; ++j) {
                for (int k = 2; k <= 8; k += 3) {
                    this.fillWithOutline(world, chunkBox, i, 0, k, i, 2, k, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                i = 10;
            }
            this.fillWithOutline(world, chunkBox, 5, 0, 10, 5, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 8, 0, 10, 8, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, -1, 7, 7, -1, 8, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.setAirAndWater(world, chunkBox, 6, -1, 3, 7, -1, 4);
            this.spawnElderGuardian(world, chunkBox, 6, 1, 6);
        }
    }

    public static class WingRoom
    extends Piece {
        private int field_14481;

        public WingRoom(Direction orientation, BlockBox box, int i) {
            super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, orientation, 1, box);
            this.field_14481 = i & 1;
        }

        public WingRoom(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            if (this.field_14481 == 0) {
                int i;
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, chunkBox, 10 - i, 3 - i, 20 - i, 12 + i, 3 - i, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                this.fillWithOutline(world, chunkBox, 7, 0, 6, 15, 0, 16, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 6, 0, 6, 6, 3, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 16, 0, 6, 16, 3, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 7, 1, 7, 7, 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 15, 1, 7, 15, 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 7, 1, 6, 9, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 13, 1, 6, 15, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 8, 1, 7, 9, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 13, 1, 7, 14, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 9, 0, 5, 13, 0, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 10, 0, 7, 12, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 8, 0, 10, 8, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 14, 0, 10, 14, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);
                for (i = 18; i >= 7; i -= 3) {
                    this.addBlock(world, SEA_LANTERN, 6, 3, i, chunkBox);
                    this.addBlock(world, SEA_LANTERN, 16, 3, i, chunkBox);
                }
                this.addBlock(world, SEA_LANTERN, 10, 0, 10, chunkBox);
                this.addBlock(world, SEA_LANTERN, 12, 0, 10, chunkBox);
                this.addBlock(world, SEA_LANTERN, 10, 0, 12, chunkBox);
                this.addBlock(world, SEA_LANTERN, 12, 0, 12, chunkBox);
                this.addBlock(world, SEA_LANTERN, 8, 3, 6, chunkBox);
                this.addBlock(world, SEA_LANTERN, 14, 3, 6, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 4, 2, 4, chunkBox);
                this.addBlock(world, SEA_LANTERN, 4, 1, 4, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 4, 0, 4, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 18, 2, 4, chunkBox);
                this.addBlock(world, SEA_LANTERN, 18, 1, 4, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 18, 0, 4, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 4, 2, 18, chunkBox);
                this.addBlock(world, SEA_LANTERN, 4, 1, 18, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 4, 0, 18, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 18, 2, 18, chunkBox);
                this.addBlock(world, SEA_LANTERN, 18, 1, 18, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 18, 0, 18, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 9, 7, 20, chunkBox);
                this.addBlock(world, PRISMARINE_BRICKS, 13, 7, 20, chunkBox);
                this.fillWithOutline(world, chunkBox, 6, 0, 21, 7, 4, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 15, 0, 21, 16, 4, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.spawnElderGuardian(world, chunkBox, 11, 2, 16);
            } else if (this.field_14481 == 1) {
                int l;
                this.fillWithOutline(world, chunkBox, 9, 3, 18, 13, 3, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 9, 0, 18, 9, 2, 18, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 13, 0, 18, 13, 2, 18, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                int i = 9;
                int j = 20;
                int k = 5;
                for (l = 0; l < 2; ++l) {
                    this.addBlock(world, PRISMARINE_BRICKS, i, 6, 20, chunkBox);
                    this.addBlock(world, SEA_LANTERN, i, 5, 20, chunkBox);
                    this.addBlock(world, PRISMARINE_BRICKS, i, 4, 20, chunkBox);
                    i = 13;
                }
                this.fillWithOutline(world, chunkBox, 7, 3, 7, 15, 3, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                i = 10;
                for (l = 0; l < 2; ++l) {
                    this.fillWithOutline(world, chunkBox, i, 0, 10, i, 6, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, i, 0, 12, i, 6, 12, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.addBlock(world, SEA_LANTERN, i, 0, 10, chunkBox);
                    this.addBlock(world, SEA_LANTERN, i, 0, 12, chunkBox);
                    this.addBlock(world, SEA_LANTERN, i, 4, 10, chunkBox);
                    this.addBlock(world, SEA_LANTERN, i, 4, 12, chunkBox);
                    i = 12;
                }
                i = 8;
                for (l = 0; l < 2; ++l) {
                    this.fillWithOutline(world, chunkBox, i, 0, 7, i, 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, i, 0, 14, i, 2, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    i = 14;
                }
                this.fillWithOutline(world, chunkBox, 8, 3, 8, 8, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 14, 3, 8, 14, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.spawnElderGuardian(world, chunkBox, 11, 5, 13);
            }
        }
    }

    public static class CoreRoom
    extends Piece {
        public CoreRoom(Direction orientation, PieceSetting setting) {
            super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, 1, orientation, setting, 2, 2, 2);
        }

        public CoreRoom(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.fillArea(world, chunkBox, 1, 8, 0, 14, 8, 14, PRISMARINE);
            int i = 7;
            BlockState lv = PRISMARINE_BRICKS;
            this.fillWithOutline(world, chunkBox, 0, 7, 0, 0, 7, 15, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 15, 7, 0, 15, 7, 15, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 1, 7, 0, 15, 7, 0, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 1, 7, 15, 14, 7, 15, lv, lv, false);
            for (i = 1; i <= 6; ++i) {
                lv = PRISMARINE_BRICKS;
                if (i == 2 || i == 6) {
                    lv = PRISMARINE;
                }
                for (int j = 0; j <= 15; j += 15) {
                    this.fillWithOutline(world, chunkBox, j, i, 0, j, i, 1, lv, lv, false);
                    this.fillWithOutline(world, chunkBox, j, i, 6, j, i, 9, lv, lv, false);
                    this.fillWithOutline(world, chunkBox, j, i, 14, j, i, 15, lv, lv, false);
                }
                this.fillWithOutline(world, chunkBox, 1, i, 0, 1, i, 0, lv, lv, false);
                this.fillWithOutline(world, chunkBox, 6, i, 0, 9, i, 0, lv, lv, false);
                this.fillWithOutline(world, chunkBox, 14, i, 0, 14, i, 0, lv, lv, false);
                this.fillWithOutline(world, chunkBox, 1, i, 15, 14, i, 15, lv, lv, false);
            }
            this.fillWithOutline(world, chunkBox, 6, 3, 6, 9, 6, 9, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.getDefaultState(), Blocks.GOLD_BLOCK.getDefaultState(), false);
            for (i = 3; i <= 6; i += 3) {
                for (int k = 6; k <= 9; k += 3) {
                    this.addBlock(world, SEA_LANTERN, k, i, 6, chunkBox);
                    this.addBlock(world, SEA_LANTERN, k, i, 9, chunkBox);
                }
            }
            this.fillWithOutline(world, chunkBox, 5, 1, 6, 5, 2, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 1, 9, 5, 2, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 10, 1, 6, 10, 2, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 10, 1, 9, 10, 2, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 1, 5, 6, 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 9, 1, 5, 9, 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 1, 10, 6, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 9, 1, 10, 9, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 2, 5, 5, 6, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 2, 10, 5, 6, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 10, 2, 5, 10, 6, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 10, 2, 10, 10, 6, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 7, 1, 5, 7, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 10, 7, 1, 10, 7, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 7, 9, 5, 7, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 10, 7, 9, 10, 7, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 7, 5, 6, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 7, 10, 6, 7, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 9, 7, 5, 14, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 9, 7, 10, 14, 7, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 2, 1, 2, 2, 1, 3, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 3, 1, 2, 3, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 13, 1, 2, 13, 1, 3, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 12, 1, 2, 12, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 2, 1, 12, 2, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 3, 1, 13, 3, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 13, 1, 12, 13, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 12, 1, 13, 12, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
        }
    }

    public static class DoubleYZRoom
    extends Piece {
        public DoubleYZRoom(Direction orientation, PieceSetting setting) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_Z_ROOM, 1, orientation, setting, 1, 2, 2);
        }

        public DoubleYZRoom(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_Z_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            BlockState lv5;
            int i;
            PieceSetting lv = this.setting.neighbors[Direction.NORTH.getId()];
            PieceSetting lv2 = this.setting;
            PieceSetting lv3 = lv.neighbors[Direction.UP.getId()];
            PieceSetting lv4 = lv2.neighbors[Direction.UP.getId()];
            if (this.setting.roomIndex / 25 > 0) {
                this.method_14774(world, chunkBox, 0, 8, lv.neighborPresences[Direction.DOWN.getId()]);
                this.method_14774(world, chunkBox, 0, 0, lv2.neighborPresences[Direction.DOWN.getId()]);
            }
            if (lv4.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 1, 8, 1, 6, 8, 7, PRISMARINE);
            }
            if (lv3.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 1, 8, 8, 6, 8, 14, PRISMARINE);
            }
            for (i = 1; i <= 7; ++i) {
                lv5 = PRISMARINE_BRICKS;
                if (i == 2 || i == 6) {
                    lv5 = PRISMARINE;
                }
                this.fillWithOutline(world, chunkBox, 0, i, 0, 0, i, 15, lv5, lv5, false);
                this.fillWithOutline(world, chunkBox, 7, i, 0, 7, i, 15, lv5, lv5, false);
                this.fillWithOutline(world, chunkBox, 1, i, 0, 6, i, 0, lv5, lv5, false);
                this.fillWithOutline(world, chunkBox, 1, i, 15, 6, i, 15, lv5, lv5, false);
            }
            for (i = 1; i <= 7; ++i) {
                lv5 = DARK_PRISMARINE;
                if (i == 2 || i == 6) {
                    lv5 = SEA_LANTERN;
                }
                this.fillWithOutline(world, chunkBox, 3, i, 7, 4, i, 8, lv5, lv5, false);
            }
            if (lv2.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 0, 4, 2, 0);
            }
            if (lv2.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 7, 1, 3, 7, 2, 4);
            }
            if (lv2.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 1, 3, 0, 2, 4);
            }
            if (lv.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 15, 4, 2, 15);
            }
            if (lv.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 1, 11, 0, 2, 12);
            }
            if (lv.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 7, 1, 11, 7, 2, 12);
            }
            if (lv4.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 5, 0, 4, 6, 0);
            }
            if (lv4.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 7, 5, 3, 7, 6, 4);
                this.fillWithOutline(world, chunkBox, 5, 4, 2, 6, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 6, 1, 2, 6, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 6, 1, 5, 6, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }
            if (lv4.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 5, 3, 0, 6, 4);
                this.fillWithOutline(world, chunkBox, 1, 4, 2, 2, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 1, 1, 2, 1, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 1, 1, 5, 1, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }
            if (lv3.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 5, 15, 4, 6, 15);
            }
            if (lv3.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 5, 11, 0, 6, 12);
                this.fillWithOutline(world, chunkBox, 1, 4, 10, 2, 4, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 1, 1, 10, 1, 3, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 1, 1, 13, 1, 3, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }
            if (lv3.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 7, 5, 11, 7, 6, 12);
                this.fillWithOutline(world, chunkBox, 5, 4, 10, 6, 4, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 6, 1, 10, 6, 3, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 6, 1, 13, 6, 3, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }
        }
    }

    public static class DoubleXYRoom
    extends Piece {
        public DoubleXYRoom(Direction orientation, PieceSetting setting) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_Y_ROOM, 1, orientation, setting, 2, 2, 1);
        }

        public DoubleXYRoom(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_Y_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            PieceSetting lv = this.setting.neighbors[Direction.EAST.getId()];
            PieceSetting lv2 = this.setting;
            PieceSetting lv3 = lv2.neighbors[Direction.UP.getId()];
            PieceSetting lv4 = lv.neighbors[Direction.UP.getId()];
            if (this.setting.roomIndex / 25 > 0) {
                this.method_14774(world, chunkBox, 8, 0, lv.neighborPresences[Direction.DOWN.getId()]);
                this.method_14774(world, chunkBox, 0, 0, lv2.neighborPresences[Direction.DOWN.getId()]);
            }
            if (lv3.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 1, 8, 1, 7, 8, 6, PRISMARINE);
            }
            if (lv4.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 8, 8, 1, 14, 8, 6, PRISMARINE);
            }
            for (int i = 1; i <= 7; ++i) {
                BlockState lv5 = PRISMARINE_BRICKS;
                if (i == 2 || i == 6) {
                    lv5 = PRISMARINE;
                }
                this.fillWithOutline(world, chunkBox, 0, i, 0, 0, i, 7, lv5, lv5, false);
                this.fillWithOutline(world, chunkBox, 15, i, 0, 15, i, 7, lv5, lv5, false);
                this.fillWithOutline(world, chunkBox, 1, i, 0, 15, i, 0, lv5, lv5, false);
                this.fillWithOutline(world, chunkBox, 1, i, 7, 14, i, 7, lv5, lv5, false);
            }
            this.fillWithOutline(world, chunkBox, 2, 1, 3, 2, 7, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 3, 1, 2, 4, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 3, 1, 5, 4, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 13, 1, 3, 13, 7, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 11, 1, 2, 12, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 11, 1, 5, 12, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 1, 3, 5, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 10, 1, 3, 10, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 7, 2, 10, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 5, 2, 5, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 10, 5, 2, 10, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 5, 5, 5, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 10, 5, 5, 10, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.addBlock(world, PRISMARINE_BRICKS, 6, 6, 2, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 9, 6, 2, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 6, 6, 5, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 9, 6, 5, chunkBox);
            this.fillWithOutline(world, chunkBox, 5, 4, 3, 6, 4, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 9, 4, 3, 10, 4, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.addBlock(world, SEA_LANTERN, 5, 4, 2, chunkBox);
            this.addBlock(world, SEA_LANTERN, 5, 4, 5, chunkBox);
            this.addBlock(world, SEA_LANTERN, 10, 4, 2, chunkBox);
            this.addBlock(world, SEA_LANTERN, 10, 4, 5, chunkBox);
            if (lv2.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 0, 4, 2, 0);
            }
            if (lv2.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 7, 4, 2, 7);
            }
            if (lv2.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 1, 3, 0, 2, 4);
            }
            if (lv.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 11, 1, 0, 12, 2, 0);
            }
            if (lv.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 11, 1, 7, 12, 2, 7);
            }
            if (lv.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 15, 1, 3, 15, 2, 4);
            }
            if (lv3.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 5, 0, 4, 6, 0);
            }
            if (lv3.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 5, 7, 4, 6, 7);
            }
            if (lv3.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 5, 3, 0, 6, 4);
            }
            if (lv4.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 11, 5, 0, 12, 6, 0);
            }
            if (lv4.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 11, 5, 7, 12, 6, 7);
            }
            if (lv4.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 15, 5, 3, 15, 6, 4);
            }
        }
    }

    public static class DoubleZRoom
    extends Piece {
        public DoubleZRoom(Direction orientation, PieceSetting setting) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, 1, orientation, setting, 1, 1, 2);
        }

        public DoubleZRoom(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            PieceSetting lv = this.setting.neighbors[Direction.NORTH.getId()];
            PieceSetting lv2 = this.setting;
            if (this.setting.roomIndex / 25 > 0) {
                this.method_14774(world, chunkBox, 0, 8, lv.neighborPresences[Direction.DOWN.getId()]);
                this.method_14774(world, chunkBox, 0, 0, lv2.neighborPresences[Direction.DOWN.getId()]);
            }
            if (lv2.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 1, 4, 1, 6, 4, 7, PRISMARINE);
            }
            if (lv.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 1, 4, 8, 6, 4, 14, PRISMARINE);
            }
            this.fillWithOutline(world, chunkBox, 0, 3, 0, 0, 3, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 7, 3, 0, 7, 3, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 3, 0, 7, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 3, 15, 6, 3, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 2, 15, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 7, 2, 0, 7, 2, 15, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 1, 2, 0, 7, 2, 0, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 1, 2, 15, 6, 2, 15, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 0, 1, 0, 0, 1, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 7, 1, 0, 7, 1, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 1, 0, 7, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 1, 15, 6, 1, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 1, 1, 1, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 1, 1, 6, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 3, 1, 1, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 3, 1, 6, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 1, 13, 1, 1, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 1, 13, 6, 1, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 3, 13, 1, 3, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 3, 13, 6, 3, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 2, 1, 6, 2, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 1, 6, 5, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 2, 1, 9, 2, 3, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 1, 9, 5, 3, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 3, 2, 6, 4, 2, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 3, 2, 9, 4, 2, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 2, 2, 7, 2, 2, 8, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 2, 7, 5, 2, 8, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.addBlock(world, SEA_LANTERN, 2, 2, 5, chunkBox);
            this.addBlock(world, SEA_LANTERN, 5, 2, 5, chunkBox);
            this.addBlock(world, SEA_LANTERN, 2, 2, 10, chunkBox);
            this.addBlock(world, SEA_LANTERN, 5, 2, 10, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 2, 3, 5, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 5, 3, 5, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 2, 3, 10, chunkBox);
            this.addBlock(world, PRISMARINE_BRICKS, 5, 3, 10, chunkBox);
            if (lv2.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 0, 4, 2, 0);
            }
            if (lv2.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 7, 1, 3, 7, 2, 4);
            }
            if (lv2.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 1, 3, 0, 2, 4);
            }
            if (lv.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 15, 4, 2, 15);
            }
            if (lv.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 1, 11, 0, 2, 12);
            }
            if (lv.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 7, 1, 11, 7, 2, 12);
            }
        }
    }

    public static class DoubleXRoom
    extends Piece {
        public DoubleXRoom(Direction orientation, PieceSetting setting) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, 1, orientation, setting, 2, 1, 1);
        }

        public DoubleXRoom(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            PieceSetting lv = this.setting.neighbors[Direction.EAST.getId()];
            PieceSetting lv2 = this.setting;
            if (this.setting.roomIndex / 25 > 0) {
                this.method_14774(world, chunkBox, 8, 0, lv.neighborPresences[Direction.DOWN.getId()]);
                this.method_14774(world, chunkBox, 0, 0, lv2.neighborPresences[Direction.DOWN.getId()]);
            }
            if (lv2.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 1, 4, 1, 7, 4, 6, PRISMARINE);
            }
            if (lv.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 8, 4, 1, 14, 4, 6, PRISMARINE);
            }
            this.fillWithOutline(world, chunkBox, 0, 3, 0, 0, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 15, 3, 0, 15, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 3, 0, 15, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 3, 7, 14, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 2, 7, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 15, 2, 0, 15, 2, 7, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 1, 2, 0, 15, 2, 0, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 1, 2, 7, 14, 2, 7, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 15, 1, 0, 15, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 1, 0, 15, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 1, 7, 14, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 1, 0, 10, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 2, 0, 9, 2, 3, PRISMARINE, PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 5, 3, 0, 10, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.addBlock(world, SEA_LANTERN, 6, 2, 3, chunkBox);
            this.addBlock(world, SEA_LANTERN, 9, 2, 3, chunkBox);
            if (lv2.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 0, 4, 2, 0);
            }
            if (lv2.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 7, 4, 2, 7);
            }
            if (lv2.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 1, 3, 0, 2, 4);
            }
            if (lv.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 11, 1, 0, 12, 2, 0);
            }
            if (lv.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 11, 1, 7, 12, 2, 7);
            }
            if (lv.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 15, 1, 3, 15, 2, 4);
            }
        }
    }

    public static class DoubleYRoom
    extends Piece {
        public DoubleYRoom(Direction orientation, PieceSetting setting) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, 1, orientation, setting, 1, 2, 1);
        }

        public DoubleYRoom(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            if (this.setting.roomIndex / 25 > 0) {
                this.method_14774(world, chunkBox, 0, 0, this.setting.neighborPresences[Direction.DOWN.getId()]);
            }
            PieceSetting lv = this.setting.neighbors[Direction.UP.getId()];
            if (lv.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 1, 8, 1, 6, 8, 6, PRISMARINE);
            }
            this.fillWithOutline(world, chunkBox, 0, 4, 0, 0, 4, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 7, 4, 0, 7, 4, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 4, 0, 6, 4, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 4, 7, 6, 4, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 2, 4, 1, 2, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 4, 2, 1, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 4, 1, 5, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 4, 2, 6, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 2, 4, 5, 2, 4, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 4, 5, 1, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 4, 5, 5, 4, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 4, 5, 6, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            PieceSetting lv2 = this.setting;
            for (int i = 1; i <= 5; i += 4) {
                int j = 0;
                if (lv2.neighborPresences[Direction.SOUTH.getId()]) {
                    this.fillWithOutline(world, chunkBox, 2, i, j, 2, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 5, i, j, 5, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 3, i + 2, j, 4, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                } else {
                    this.fillWithOutline(world, chunkBox, 0, i, j, 7, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 0, i + 1, j, 7, i + 1, j, PRISMARINE, PRISMARINE, false);
                }
                j = 7;
                if (lv2.neighborPresences[Direction.NORTH.getId()]) {
                    this.fillWithOutline(world, chunkBox, 2, i, j, 2, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 5, i, j, 5, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 3, i + 2, j, 4, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                } else {
                    this.fillWithOutline(world, chunkBox, 0, i, j, 7, i + 2, j, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 0, i + 1, j, 7, i + 1, j, PRISMARINE, PRISMARINE, false);
                }
                int k = 0;
                if (lv2.neighborPresences[Direction.WEST.getId()]) {
                    this.fillWithOutline(world, chunkBox, k, i, 2, k, i + 2, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, k, i, 5, k, i + 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, k, i + 2, 3, k, i + 2, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                } else {
                    this.fillWithOutline(world, chunkBox, k, i, 0, k, i + 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, k, i + 1, 0, k, i + 1, 7, PRISMARINE, PRISMARINE, false);
                }
                k = 7;
                if (lv2.neighborPresences[Direction.EAST.getId()]) {
                    this.fillWithOutline(world, chunkBox, k, i, 2, k, i + 2, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, k, i, 5, k, i + 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, k, i + 2, 3, k, i + 2, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                } else {
                    this.fillWithOutline(world, chunkBox, k, i, 0, k, i + 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, k, i + 1, 0, k, i + 1, 7, PRISMARINE, PRISMARINE, false);
                }
                lv2 = lv;
            }
        }
    }

    public static class SimpleRoomTop
    extends Piece {
        public SimpleRoomTop(Direction orientation, PieceSetting setting) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, 1, orientation, setting, 1, 1, 1);
        }

        public SimpleRoomTop(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            if (this.setting.roomIndex / 25 > 0) {
                this.method_14774(world, chunkBox, 0, 0, this.setting.neighborPresences[Direction.DOWN.getId()]);
            }
            if (this.setting.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 1, 4, 1, 6, 4, 6, PRISMARINE);
            }
            for (int i = 1; i <= 6; ++i) {
                for (int j = 1; j <= 6; ++j) {
                    if (random.nextInt(3) == 0) continue;
                    int k = 2 + (random.nextInt(4) == 0 ? 0 : 1);
                    BlockState lv = Blocks.WET_SPONGE.getDefaultState();
                    this.fillWithOutline(world, chunkBox, i, k, j, i, 3, j, lv, lv, false);
                }
            }
            this.fillWithOutline(world, chunkBox, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 7, 1, 0, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 1, 0, 6, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 1, 7, 6, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 0, 3, 0, 0, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 7, 3, 0, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 3, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 3, 7, 6, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithOutline(world, chunkBox, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            if (this.setting.neighborPresences[Direction.SOUTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 0, 4, 2, 0);
            }
        }
    }

    public static class SimpleRoom
    extends Piece {
        private int field_14480;

        public SimpleRoom(Direction orientation, PieceSetting setting, Random random) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, 1, orientation, setting, 1, 1, 1);
            this.field_14480 = random.nextInt(3);
        }

        public SimpleRoom(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            boolean bl;
            if (this.setting.roomIndex / 25 > 0) {
                this.method_14774(world, chunkBox, 0, 0, this.setting.neighborPresences[Direction.DOWN.getId()]);
            }
            if (this.setting.neighbors[Direction.UP.getId()] == null) {
                this.fillArea(world, chunkBox, 1, 4, 1, 6, 4, 6, PRISMARINE);
            }
            boolean bl2 = bl = this.field_14480 != 0 && random.nextBoolean() && !this.setting.neighborPresences[Direction.DOWN.getId()] && !this.setting.neighborPresences[Direction.UP.getId()] && this.setting.countNeighbors() > 1;
            if (this.field_14480 == 0) {
                this.fillWithOutline(world, chunkBox, 0, 1, 0, 2, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 0, 3, 0, 2, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 2, 2, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 1, 2, 0, 2, 2, 0, PRISMARINE, PRISMARINE, false);
                this.addBlock(world, SEA_LANTERN, 1, 2, 1, chunkBox);
                this.fillWithOutline(world, chunkBox, 5, 1, 0, 7, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 5, 3, 0, 7, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 7, 2, 0, 7, 2, 2, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 5, 2, 0, 6, 2, 0, PRISMARINE, PRISMARINE, false);
                this.addBlock(world, SEA_LANTERN, 6, 2, 1, chunkBox);
                this.fillWithOutline(world, chunkBox, 0, 1, 5, 2, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 0, 3, 5, 2, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 0, 2, 5, 0, 2, 7, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 1, 2, 7, 2, 2, 7, PRISMARINE, PRISMARINE, false);
                this.addBlock(world, SEA_LANTERN, 1, 2, 6, chunkBox);
                this.fillWithOutline(world, chunkBox, 5, 1, 5, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 5, 3, 5, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 7, 2, 5, 7, 2, 7, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 5, 2, 7, 6, 2, 7, PRISMARINE, PRISMARINE, false);
                this.addBlock(world, SEA_LANTERN, 6, 2, 6, chunkBox);
                if (this.setting.neighborPresences[Direction.SOUTH.getId()]) {
                    this.fillWithOutline(world, chunkBox, 3, 3, 0, 4, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                } else {
                    this.fillWithOutline(world, chunkBox, 3, 3, 0, 4, 3, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 3, 2, 0, 4, 2, 0, PRISMARINE, PRISMARINE, false);
                    this.fillWithOutline(world, chunkBox, 3, 1, 0, 4, 1, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                if (this.setting.neighborPresences[Direction.NORTH.getId()]) {
                    this.fillWithOutline(world, chunkBox, 3, 3, 7, 4, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                } else {
                    this.fillWithOutline(world, chunkBox, 3, 3, 6, 4, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 3, 2, 7, 4, 2, 7, PRISMARINE, PRISMARINE, false);
                    this.fillWithOutline(world, chunkBox, 3, 1, 6, 4, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                if (this.setting.neighborPresences[Direction.WEST.getId()]) {
                    this.fillWithOutline(world, chunkBox, 0, 3, 3, 0, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                } else {
                    this.fillWithOutline(world, chunkBox, 0, 3, 3, 1, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 0, 2, 3, 0, 2, 4, PRISMARINE, PRISMARINE, false);
                    this.fillWithOutline(world, chunkBox, 0, 1, 3, 1, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                if (this.setting.neighborPresences[Direction.EAST.getId()]) {
                    this.fillWithOutline(world, chunkBox, 7, 3, 3, 7, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                } else {
                    this.fillWithOutline(world, chunkBox, 6, 3, 3, 7, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 7, 2, 3, 7, 2, 4, PRISMARINE, PRISMARINE, false);
                    this.fillWithOutline(world, chunkBox, 6, 1, 3, 7, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
            } else if (this.field_14480 == 1) {
                this.fillWithOutline(world, chunkBox, 2, 1, 2, 2, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 2, 1, 5, 2, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 5, 1, 5, 5, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 5, 1, 2, 5, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.addBlock(world, SEA_LANTERN, 2, 2, 2, chunkBox);
                this.addBlock(world, SEA_LANTERN, 2, 2, 5, chunkBox);
                this.addBlock(world, SEA_LANTERN, 5, 2, 5, chunkBox);
                this.addBlock(world, SEA_LANTERN, 5, 2, 2, chunkBox);
                this.fillWithOutline(world, chunkBox, 0, 1, 0, 1, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 0, 1, 1, 0, 3, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 0, 1, 7, 1, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 0, 1, 6, 0, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 6, 1, 7, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 7, 1, 6, 7, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 6, 1, 0, 7, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 7, 1, 1, 7, 3, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.addBlock(world, PRISMARINE, 1, 2, 0, chunkBox);
                this.addBlock(world, PRISMARINE, 0, 2, 1, chunkBox);
                this.addBlock(world, PRISMARINE, 1, 2, 7, chunkBox);
                this.addBlock(world, PRISMARINE, 0, 2, 6, chunkBox);
                this.addBlock(world, PRISMARINE, 6, 2, 7, chunkBox);
                this.addBlock(world, PRISMARINE, 7, 2, 6, chunkBox);
                this.addBlock(world, PRISMARINE, 6, 2, 0, chunkBox);
                this.addBlock(world, PRISMARINE, 7, 2, 1, chunkBox);
                if (!this.setting.neighborPresences[Direction.SOUTH.getId()]) {
                    this.fillWithOutline(world, chunkBox, 1, 3, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 1, 2, 0, 6, 2, 0, PRISMARINE, PRISMARINE, false);
                    this.fillWithOutline(world, chunkBox, 1, 1, 0, 6, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                if (!this.setting.neighborPresences[Direction.NORTH.getId()]) {
                    this.fillWithOutline(world, chunkBox, 1, 3, 7, 6, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 1, 2, 7, 6, 2, 7, PRISMARINE, PRISMARINE, false);
                    this.fillWithOutline(world, chunkBox, 1, 1, 7, 6, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                if (!this.setting.neighborPresences[Direction.WEST.getId()]) {
                    this.fillWithOutline(world, chunkBox, 0, 3, 1, 0, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 0, 2, 1, 0, 2, 6, PRISMARINE, PRISMARINE, false);
                    this.fillWithOutline(world, chunkBox, 0, 1, 1, 0, 1, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                if (!this.setting.neighborPresences[Direction.EAST.getId()]) {
                    this.fillWithOutline(world, chunkBox, 7, 3, 1, 7, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, chunkBox, 7, 2, 1, 7, 2, 6, PRISMARINE, PRISMARINE, false);
                    this.fillWithOutline(world, chunkBox, 7, 1, 1, 7, 1, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
            } else if (this.field_14480 == 2) {
                this.fillWithOutline(world, chunkBox, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 7, 1, 0, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 1, 1, 0, 6, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 1, 1, 7, 6, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 0, 3, 0, 0, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 7, 3, 0, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 1, 3, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 1, 3, 7, 6, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
                if (this.setting.neighborPresences[Direction.SOUTH.getId()]) {
                    this.setAirAndWater(world, chunkBox, 3, 1, 0, 4, 2, 0);
                }
                if (this.setting.neighborPresences[Direction.NORTH.getId()]) {
                    this.setAirAndWater(world, chunkBox, 3, 1, 7, 4, 2, 7);
                }
                if (this.setting.neighborPresences[Direction.WEST.getId()]) {
                    this.setAirAndWater(world, chunkBox, 0, 1, 3, 0, 2, 4);
                }
                if (this.setting.neighborPresences[Direction.EAST.getId()]) {
                    this.setAirAndWater(world, chunkBox, 7, 1, 3, 7, 2, 4);
                }
            }
            if (bl) {
                this.fillWithOutline(world, chunkBox, 3, 1, 3, 4, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, chunkBox, 3, 2, 3, 4, 2, 4, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, chunkBox, 3, 3, 3, 4, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }
        }
    }

    public static class Entry
    extends Piece {
        public Entry(Direction orientation, PieceSetting setting) {
            super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, 1, orientation, setting, 1, 1, 1);
        }

        public Entry(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, nbt);
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            this.fillWithOutline(world, chunkBox, 0, 3, 0, 2, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 3, 0, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 0, 2, 0, 1, 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 6, 2, 0, 7, 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 7, 1, 0, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 0, 1, 7, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 1, 1, 0, 2, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithOutline(world, chunkBox, 5, 1, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            if (this.setting.neighborPresences[Direction.NORTH.getId()]) {
                this.setAirAndWater(world, chunkBox, 3, 1, 7, 4, 2, 7);
            }
            if (this.setting.neighborPresences[Direction.WEST.getId()]) {
                this.setAirAndWater(world, chunkBox, 0, 1, 3, 1, 2, 4);
            }
            if (this.setting.neighborPresences[Direction.EAST.getId()]) {
                this.setAirAndWater(world, chunkBox, 6, 1, 3, 7, 2, 4);
            }
        }
    }

    public static class Base
    extends Piece {
        private static final int field_31602 = 58;
        private static final int field_31603 = 22;
        private static final int field_31604 = 58;
        public static final int field_31606 = 29;
        private static final int field_31605 = 61;
        private PieceSetting entryPieceSetting;
        private PieceSetting coreRoomPieceSetting;
        private final List<Piece> children = Lists.newArrayList();

        public Base(Random random, int x, int z, Direction orientation) {
            super(StructurePieceType.OCEAN_MONUMENT_BASE, orientation, 0, Base.createBox(x, 39, z, orientation, 58, 23, 58));
            this.setOrientation(orientation);
            List<PieceSetting> list = this.method_14760(random);
            this.entryPieceSetting.used = true;
            this.children.add(new Entry(orientation, this.entryPieceSetting));
            this.children.add(new CoreRoom(orientation, this.coreRoomPieceSetting));
            ArrayList<PieceFactory> list2 = Lists.newArrayList();
            list2.add(new DoubleXYRoomFactory());
            list2.add(new DoubleYZRoomFactory());
            list2.add(new DoubleZRoomFactory());
            list2.add(new DoubleXRoomFactory());
            list2.add(new DoubleYRoomFactory());
            list2.add(new SimpleRoomTopFactory());
            list2.add(new SimpleRoomFactory());
            block0: for (PieceSetting pieceSetting : list) {
                if (pieceSetting.used || pieceSetting.isAboveLevelThree()) continue;
                for (PieceFactory lv2 : list2) {
                    if (!lv2.canGenerate(pieceSetting)) continue;
                    this.children.add(lv2.generate(orientation, pieceSetting, random));
                    continue block0;
                }
            }
            BlockPos.Mutable lv3 = this.offsetPos(9, 0, 22);
            for (Piece lv4 : this.children) {
                lv4.getBoundingBox().move(lv3);
            }
            BlockBox blockBox = BlockBox.create(this.offsetPos(1, 1, 1), this.offsetPos(23, 8, 21));
            BlockBox lv6 = BlockBox.create(this.offsetPos(34, 1, 1), this.offsetPos(56, 8, 21));
            BlockBox lv7 = BlockBox.create(this.offsetPos(22, 13, 22), this.offsetPos(35, 17, 35));
            int k = random.nextInt();
            this.children.add(new WingRoom(orientation, blockBox, k++));
            this.children.add(new WingRoom(orientation, lv6, k++));
            this.children.add(new Penthouse(orientation, lv7));
        }

        public Base(NbtCompound nbt) {
            super(StructurePieceType.OCEAN_MONUMENT_BASE, nbt);
        }

        private List<PieceSetting> method_14760(Random random) {
            int o;
            int n;
            int m;
            int l;
            int k;
            int j;
            int i;
            PieceSetting[] lvs = new PieceSetting[75];
            for (i = 0; i < 5; ++i) {
                for (j = 0; j < 4; ++j) {
                    k = 0;
                    l = Base.getIndex(i, 0, j);
                    lvs[l] = new PieceSetting(l);
                }
            }
            for (i = 0; i < 5; ++i) {
                for (j = 0; j < 4; ++j) {
                    k = 1;
                    l = Base.getIndex(i, 1, j);
                    lvs[l] = new PieceSetting(l);
                }
            }
            for (i = 1; i < 4; ++i) {
                for (j = 0; j < 2; ++j) {
                    k = 2;
                    l = Base.getIndex(i, 2, j);
                    lvs[l] = new PieceSetting(l);
                }
            }
            this.entryPieceSetting = lvs[TWO_ZERO_ZERO_INDEX];
            for (i = 0; i < 5; ++i) {
                for (j = 0; j < 5; ++j) {
                    for (k = 0; k < 3; ++k) {
                        l = Base.getIndex(i, k, j);
                        if (lvs[l] == null) continue;
                        for (Direction lv : Direction.values()) {
                            int p;
                            m = i + lv.getOffsetX();
                            n = k + lv.getOffsetY();
                            o = j + lv.getOffsetZ();
                            if (m < 0 || m >= 5 || o < 0 || o >= 5 || n < 0 || n >= 3 || lvs[p = Base.getIndex(m, n, o)] == null) continue;
                            if (o == j) {
                                lvs[l].setNeighbor(lv, lvs[p]);
                                continue;
                            }
                            lvs[l].setNeighbor(lv.getOpposite(), lvs[p]);
                        }
                    }
                }
            }
            PieceSetting lv2 = new PieceSetting(1003);
            PieceSetting lv3 = new PieceSetting(1001);
            PieceSetting lv4 = new PieceSetting(1002);
            lvs[TWO_TWO_ZERO_INDEX].setNeighbor(Direction.UP, lv2);
            lvs[ZERO_ONE_ZERO_INDEX].setNeighbor(Direction.SOUTH, lv3);
            lvs[FOUR_ONE_ZERO_INDEX].setNeighbor(Direction.SOUTH, lv4);
            lv2.used = true;
            lv3.used = true;
            lv4.used = true;
            this.entryPieceSetting.field_14484 = true;
            this.coreRoomPieceSetting = lvs[Base.getIndex(random.nextInt(4), 0, 2)];
            this.coreRoomPieceSetting.used = true;
            this.coreRoomPieceSetting.neighbors[Direction.EAST.getId()].used = true;
            this.coreRoomPieceSetting.neighbors[Direction.NORTH.getId()].used = true;
            this.coreRoomPieceSetting.neighbors[Direction.EAST.getId()].neighbors[Direction.NORTH.getId()].used = true;
            this.coreRoomPieceSetting.neighbors[Direction.UP.getId()].used = true;
            this.coreRoomPieceSetting.neighbors[Direction.EAST.getId()].neighbors[Direction.UP.getId()].used = true;
            this.coreRoomPieceSetting.neighbors[Direction.NORTH.getId()].neighbors[Direction.UP.getId()].used = true;
            this.coreRoomPieceSetting.neighbors[Direction.EAST.getId()].neighbors[Direction.NORTH.getId()].neighbors[Direction.UP.getId()].used = true;
            ObjectArrayList<PieceSetting> objectArrayList = new ObjectArrayList<PieceSetting>();
            for (PieceSetting lv5 : lvs) {
                if (lv5 == null) continue;
                lv5.checkNeighborStates();
                objectArrayList.add(lv5);
            }
            lv2.checkNeighborStates();
            Util.shuffle(objectArrayList, random);
            int q = 1;
            for (PieceSetting lv6 : objectArrayList) {
                int r = 0;
                for (m = 0; r < 2 && m < 5; ++m) {
                    n = random.nextInt(6);
                    if (!lv6.neighborPresences[n]) continue;
                    o = Direction.byId(n).getOpposite().getId();
                    lv6.neighborPresences[n] = false;
                    lv6.neighbors[n].neighborPresences[o] = false;
                    if (lv6.method_14783(q++) && lv6.neighbors[n].method_14783(q++)) {
                        ++r;
                        continue;
                    }
                    lv6.neighborPresences[n] = true;
                    lv6.neighbors[n].neighborPresences[o] = true;
                }
            }
            objectArrayList.add(lv2);
            objectArrayList.add(lv3);
            objectArrayList.add(lv4);
            return objectArrayList;
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            int j;
            int i = Math.max(world.getSeaLevel(), 64) - this.boundingBox.getMinY();
            this.setAirAndWater(world, chunkBox, 0, 0, 0, 58, i, 58);
            this.method_14761(false, 0, world, random, chunkBox);
            this.method_14761(true, 33, world, random, chunkBox);
            this.method_14763(world, random, chunkBox);
            this.method_14762(world, random, chunkBox);
            this.method_14765(world, random, chunkBox);
            this.method_14764(world, random, chunkBox);
            this.method_14766(world, random, chunkBox);
            this.method_14767(world, random, chunkBox);
            for (j = 0; j < 7; ++j) {
                int k = 0;
                while (k < 7) {
                    if (k == 0 && j == 3) {
                        k = 6;
                    }
                    int l = j * 9;
                    int m = k * 9;
                    for (int n = 0; n < 4; ++n) {
                        for (int o = 0; o < 4; ++o) {
                            this.addBlock(world, PRISMARINE_BRICKS, l + n, 0, m + o, chunkBox);
                            this.fillDownwards(world, PRISMARINE_BRICKS, l + n, -1, m + o, chunkBox);
                        }
                    }
                    if (j == 0 || j == 6) {
                        ++k;
                        continue;
                    }
                    k += 6;
                }
            }
            for (j = 0; j < 5; ++j) {
                this.setAirAndWater(world, chunkBox, -1 - j, 0 + j * 2, -1 - j, -1 - j, 23, 58 + j);
                this.setAirAndWater(world, chunkBox, 58 + j, 0 + j * 2, -1 - j, 58 + j, 23, 58 + j);
                this.setAirAndWater(world, chunkBox, 0 - j, 0 + j * 2, -1 - j, 57 + j, 23, -1 - j);
                this.setAirAndWater(world, chunkBox, 0 - j, 0 + j * 2, 58 + j, 57 + j, 23, 58 + j);
            }
            for (Piece lv : this.children) {
                if (!lv.getBoundingBox().intersects(chunkBox)) continue;
                lv.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
            }
        }

        private void method_14761(boolean bl, int i, StructureWorldAccess world, Random random, BlockBox box) {
            int j = 24;
            if (this.boxIntersects(box, i, 0, i + 23, 20)) {
                int m;
                int k;
                this.fillWithOutline(world, box, i + 0, 0, 0, i + 24, 0, 20, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, i + 0, 1, 0, i + 24, 10, 20);
                for (k = 0; k < 4; ++k) {
                    this.fillWithOutline(world, box, i + k, k + 1, k, i + k, k + 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, i + k + 7, k + 5, k + 7, i + k + 7, k + 5, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, i + 17 - k, k + 5, k + 7, i + 17 - k, k + 5, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, i + 24 - k, k + 1, k, i + 24 - k, k + 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, i + k + 1, k + 1, k, i + 23 - k, k + 1, k, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, i + k + 8, k + 5, k + 7, i + 16 - k, k + 5, k + 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                this.fillWithOutline(world, box, i + 4, 4, 4, i + 6, 4, 20, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, i + 7, 4, 4, i + 17, 4, 6, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, i + 18, 4, 4, i + 20, 4, 20, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, i + 11, 8, 11, i + 13, 8, 20, PRISMARINE, PRISMARINE, false);
                this.addBlock(world, ALSO_PRISMARINE_BRICKS, i + 12, 9, 12, box);
                this.addBlock(world, ALSO_PRISMARINE_BRICKS, i + 12, 9, 15, box);
                this.addBlock(world, ALSO_PRISMARINE_BRICKS, i + 12, 9, 18, box);
                k = i + (bl ? 19 : 5);
                int l = i + (bl ? 5 : 19);
                for (m = 20; m >= 5; m -= 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, k, 5, m, box);
                }
                for (m = 19; m >= 7; m -= 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, l, 5, m, box);
                }
                for (m = 0; m < 4; ++m) {
                    int n = bl ? i + 24 - (17 - m * 3) : i + 17 - m * 3;
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, n, 5, 5, box);
                }
                this.addBlock(world, ALSO_PRISMARINE_BRICKS, l, 5, 5, box);
                this.fillWithOutline(world, box, i + 11, 1, 12, i + 13, 7, 12, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, i + 12, 1, 11, i + 12, 7, 13, PRISMARINE, PRISMARINE, false);
            }
        }

        private void method_14763(StructureWorldAccess world, Random random, BlockBox box) {
            if (this.boxIntersects(box, 22, 5, 35, 17)) {
                this.setAirAndWater(world, box, 25, 0, 0, 32, 8, 20);
                for (int i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, 24, 2, 5 + i * 4, 24, 4, 5 + i * 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, 22, 4, 5 + i * 4, 23, 4, 5 + i * 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.addBlock(world, PRISMARINE_BRICKS, 25, 5, 5 + i * 4, box);
                    this.addBlock(world, PRISMARINE_BRICKS, 26, 6, 5 + i * 4, box);
                    this.addBlock(world, SEA_LANTERN, 26, 5, 5 + i * 4, box);
                    this.fillWithOutline(world, box, 33, 2, 5 + i * 4, 33, 4, 5 + i * 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, 34, 4, 5 + i * 4, 35, 4, 5 + i * 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.addBlock(world, PRISMARINE_BRICKS, 32, 5, 5 + i * 4, box);
                    this.addBlock(world, PRISMARINE_BRICKS, 31, 6, 5 + i * 4, box);
                    this.addBlock(world, SEA_LANTERN, 31, 5, 5 + i * 4, box);
                    this.fillWithOutline(world, box, 27, 6, 5 + i * 4, 30, 6, 5 + i * 4, PRISMARINE, PRISMARINE, false);
                }
            }
        }

        private void method_14762(StructureWorldAccess world, Random random, BlockBox box) {
            if (this.boxIntersects(box, 15, 20, 42, 21)) {
                int i;
                this.fillWithOutline(world, box, 15, 0, 21, 42, 0, 21, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 26, 1, 21, 31, 3, 21);
                this.fillWithOutline(world, box, 21, 12, 21, 36, 12, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 17, 11, 21, 40, 11, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 16, 10, 21, 41, 10, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 15, 7, 21, 42, 9, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 16, 6, 21, 41, 6, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 17, 5, 21, 40, 5, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 21, 4, 21, 36, 4, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 22, 3, 21, 26, 3, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 31, 3, 21, 35, 3, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 23, 2, 21, 25, 2, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 32, 2, 21, 34, 2, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 28, 4, 20, 29, 4, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.addBlock(world, PRISMARINE_BRICKS, 27, 3, 21, box);
                this.addBlock(world, PRISMARINE_BRICKS, 30, 3, 21, box);
                this.addBlock(world, PRISMARINE_BRICKS, 26, 2, 21, box);
                this.addBlock(world, PRISMARINE_BRICKS, 31, 2, 21, box);
                this.addBlock(world, PRISMARINE_BRICKS, 25, 1, 21, box);
                this.addBlock(world, PRISMARINE_BRICKS, 32, 1, 21, box);
                for (i = 0; i < 7; ++i) {
                    this.addBlock(world, DARK_PRISMARINE, 28 - i, 6 + i, 21, box);
                    this.addBlock(world, DARK_PRISMARINE, 29 + i, 6 + i, 21, box);
                }
                for (i = 0; i < 4; ++i) {
                    this.addBlock(world, DARK_PRISMARINE, 28 - i, 9 + i, 21, box);
                    this.addBlock(world, DARK_PRISMARINE, 29 + i, 9 + i, 21, box);
                }
                this.addBlock(world, DARK_PRISMARINE, 28, 12, 21, box);
                this.addBlock(world, DARK_PRISMARINE, 29, 12, 21, box);
                for (i = 0; i < 3; ++i) {
                    this.addBlock(world, DARK_PRISMARINE, 22 - i * 2, 8, 21, box);
                    this.addBlock(world, DARK_PRISMARINE, 22 - i * 2, 9, 21, box);
                    this.addBlock(world, DARK_PRISMARINE, 35 + i * 2, 8, 21, box);
                    this.addBlock(world, DARK_PRISMARINE, 35 + i * 2, 9, 21, box);
                }
                this.setAirAndWater(world, box, 15, 13, 21, 42, 15, 21);
                this.setAirAndWater(world, box, 15, 1, 21, 15, 6, 21);
                this.setAirAndWater(world, box, 16, 1, 21, 16, 5, 21);
                this.setAirAndWater(world, box, 17, 1, 21, 20, 4, 21);
                this.setAirAndWater(world, box, 21, 1, 21, 21, 3, 21);
                this.setAirAndWater(world, box, 22, 1, 21, 22, 2, 21);
                this.setAirAndWater(world, box, 23, 1, 21, 24, 1, 21);
                this.setAirAndWater(world, box, 42, 1, 21, 42, 6, 21);
                this.setAirAndWater(world, box, 41, 1, 21, 41, 5, 21);
                this.setAirAndWater(world, box, 37, 1, 21, 40, 4, 21);
                this.setAirAndWater(world, box, 36, 1, 21, 36, 3, 21);
                this.setAirAndWater(world, box, 33, 1, 21, 34, 1, 21);
                this.setAirAndWater(world, box, 35, 1, 21, 35, 2, 21);
            }
        }

        private void method_14765(StructureWorldAccess world, Random random, BlockBox box) {
            if (this.boxIntersects(box, 21, 21, 36, 36)) {
                this.fillWithOutline(world, box, 21, 0, 22, 36, 0, 36, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 21, 1, 22, 36, 23, 36);
                for (int i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, 21 + i, 13 + i, 21 + i, 36 - i, 13 + i, 21 + i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, 21 + i, 13 + i, 36 - i, 36 - i, 13 + i, 36 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, 21 + i, 13 + i, 22 + i, 21 + i, 13 + i, 35 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithOutline(world, box, 36 - i, 13 + i, 22 + i, 36 - i, 13 + i, 35 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                this.fillWithOutline(world, box, 25, 16, 25, 32, 16, 32, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 25, 17, 25, 25, 19, 25, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, box, 32, 17, 25, 32, 19, 25, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, box, 25, 17, 32, 25, 19, 32, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, box, 32, 17, 32, 32, 19, 32, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.addBlock(world, PRISMARINE_BRICKS, 26, 20, 26, box);
                this.addBlock(world, PRISMARINE_BRICKS, 27, 21, 27, box);
                this.addBlock(world, SEA_LANTERN, 27, 20, 27, box);
                this.addBlock(world, PRISMARINE_BRICKS, 26, 20, 31, box);
                this.addBlock(world, PRISMARINE_BRICKS, 27, 21, 30, box);
                this.addBlock(world, SEA_LANTERN, 27, 20, 30, box);
                this.addBlock(world, PRISMARINE_BRICKS, 31, 20, 31, box);
                this.addBlock(world, PRISMARINE_BRICKS, 30, 21, 30, box);
                this.addBlock(world, SEA_LANTERN, 30, 20, 30, box);
                this.addBlock(world, PRISMARINE_BRICKS, 31, 20, 26, box);
                this.addBlock(world, PRISMARINE_BRICKS, 30, 21, 27, box);
                this.addBlock(world, SEA_LANTERN, 30, 20, 27, box);
                this.fillWithOutline(world, box, 28, 21, 27, 29, 21, 27, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 27, 21, 28, 27, 21, 29, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 28, 21, 30, 29, 21, 30, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 30, 21, 28, 30, 21, 29, PRISMARINE, PRISMARINE, false);
            }
        }

        private void method_14764(StructureWorldAccess world, Random random, BlockBox box) {
            int i;
            if (this.boxIntersects(box, 0, 21, 6, 58)) {
                this.fillWithOutline(world, box, 0, 0, 21, 6, 0, 57, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 0, 1, 21, 6, 7, 57);
                this.fillWithOutline(world, box, 4, 4, 21, 6, 4, 53, PRISMARINE, PRISMARINE, false);
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, i, i + 1, 21, i, i + 1, 57 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                for (i = 23; i < 53; i += 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, 5, 5, i, box);
                }
                this.addBlock(world, ALSO_PRISMARINE_BRICKS, 5, 5, 52, box);
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, i, i + 1, 21, i, i + 1, 57 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                this.fillWithOutline(world, box, 4, 1, 52, 6, 3, 52, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 5, 1, 51, 5, 3, 53, PRISMARINE, PRISMARINE, false);
            }
            if (this.boxIntersects(box, 51, 21, 58, 58)) {
                this.fillWithOutline(world, box, 51, 0, 21, 57, 0, 57, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 51, 1, 21, 57, 7, 57);
                this.fillWithOutline(world, box, 51, 4, 21, 53, 4, 53, PRISMARINE, PRISMARINE, false);
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, 57 - i, i + 1, 21, 57 - i, i + 1, 57 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                for (i = 23; i < 53; i += 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, 52, 5, i, box);
                }
                this.addBlock(world, ALSO_PRISMARINE_BRICKS, 52, 5, 52, box);
                this.fillWithOutline(world, box, 51, 1, 52, 53, 3, 52, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 52, 1, 51, 52, 3, 53, PRISMARINE, PRISMARINE, false);
            }
            if (this.boxIntersects(box, 0, 51, 57, 57)) {
                this.fillWithOutline(world, box, 7, 0, 51, 50, 0, 57, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 7, 1, 51, 50, 10, 57);
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, i + 1, i + 1, 57 - i, 56 - i, i + 1, 57 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
            }
        }

        private void method_14766(StructureWorldAccess world, Random random, BlockBox box) {
            int i;
            if (this.boxIntersects(box, 7, 21, 13, 50)) {
                this.fillWithOutline(world, box, 7, 0, 21, 13, 0, 50, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 7, 1, 21, 13, 10, 50);
                this.fillWithOutline(world, box, 11, 8, 21, 13, 8, 53, PRISMARINE, PRISMARINE, false);
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, i + 7, i + 5, 21, i + 7, i + 5, 54, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                for (i = 21; i <= 45; i += 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, 12, 9, i, box);
                }
            }
            if (this.boxIntersects(box, 44, 21, 50, 54)) {
                this.fillWithOutline(world, box, 44, 0, 21, 50, 0, 50, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 44, 1, 21, 50, 10, 50);
                this.fillWithOutline(world, box, 44, 8, 21, 46, 8, 53, PRISMARINE, PRISMARINE, false);
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, 50 - i, i + 5, 21, 50 - i, i + 5, 54, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                for (i = 21; i <= 45; i += 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, 45, 9, i, box);
                }
            }
            if (this.boxIntersects(box, 8, 44, 49, 54)) {
                this.fillWithOutline(world, box, 14, 0, 44, 43, 0, 50, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 14, 1, 44, 43, 10, 50);
                for (i = 12; i <= 45; i += 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 9, 45, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 9, 52, box);
                    if (i != 12 && i != 18 && i != 24 && i != 33 && i != 39 && i != 45) continue;
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 9, 47, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 9, 50, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 10, 45, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 10, 46, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 10, 51, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 10, 52, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 11, 47, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 11, 50, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 12, 48, box);
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 12, 49, box);
                }
                for (i = 0; i < 3; ++i) {
                    this.fillWithOutline(world, box, 8 + i, 5 + i, 54, 49 - i, 5 + i, 54, PRISMARINE, PRISMARINE, false);
                }
                this.fillWithOutline(world, box, 11, 8, 54, 46, 8, 54, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, box, 14, 8, 44, 43, 8, 53, PRISMARINE, PRISMARINE, false);
            }
        }

        private void method_14767(StructureWorldAccess world, Random random, BlockBox box) {
            int i;
            if (this.boxIntersects(box, 14, 21, 20, 43)) {
                this.fillWithOutline(world, box, 14, 0, 21, 20, 0, 43, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 14, 1, 22, 20, 14, 43);
                this.fillWithOutline(world, box, 18, 12, 22, 20, 12, 39, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 18, 12, 21, 20, 12, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, i + 14, i + 9, 21, i + 14, i + 9, 43 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                for (i = 23; i <= 39; i += 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, 19, 13, i, box);
                }
            }
            if (this.boxIntersects(box, 37, 21, 43, 43)) {
                this.fillWithOutline(world, box, 37, 0, 21, 43, 0, 43, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 37, 1, 22, 43, 14, 43);
                this.fillWithOutline(world, box, 37, 12, 22, 39, 12, 39, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, 37, 12, 21, 39, 12, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, 43 - i, i + 9, 21, 43 - i, i + 9, 43 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                for (i = 23; i <= 39; i += 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, 38, 13, i, box);
                }
            }
            if (this.boxIntersects(box, 15, 37, 42, 43)) {
                this.fillWithOutline(world, box, 21, 0, 37, 36, 0, 43, PRISMARINE, PRISMARINE, false);
                this.setAirAndWater(world, box, 21, 1, 37, 36, 14, 43);
                this.fillWithOutline(world, box, 21, 12, 37, 36, 12, 39, PRISMARINE, PRISMARINE, false);
                for (i = 0; i < 4; ++i) {
                    this.fillWithOutline(world, box, 15 + i, i + 9, 43 - i, 42 - i, i + 9, 43 - i, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                for (i = 21; i <= 36; i += 3) {
                    this.addBlock(world, ALSO_PRISMARINE_BRICKS, i, 13, 38, box);
                }
            }
        }
    }

    protected static abstract class Piece
    extends StructurePiece {
        protected static final BlockState PRISMARINE = Blocks.PRISMARINE.getDefaultState();
        protected static final BlockState PRISMARINE_BRICKS = Blocks.PRISMARINE_BRICKS.getDefaultState();
        protected static final BlockState DARK_PRISMARINE = Blocks.DARK_PRISMARINE.getDefaultState();
        protected static final BlockState ALSO_PRISMARINE_BRICKS = PRISMARINE_BRICKS;
        protected static final BlockState SEA_LANTERN = Blocks.SEA_LANTERN.getDefaultState();
        protected static final boolean field_31607 = true;
        protected static final BlockState WATER = Blocks.WATER.getDefaultState();
        protected static final Set<Block> ICE_BLOCKS = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add(Blocks.ICE)).add(Blocks.PACKED_ICE)).add(Blocks.BLUE_ICE)).add(WATER.getBlock())).build();
        protected static final int field_31608 = 8;
        protected static final int field_31609 = 8;
        protected static final int field_31610 = 4;
        protected static final int field_31611 = 5;
        protected static final int field_31612 = 5;
        protected static final int field_31613 = 3;
        protected static final int field_31614 = 25;
        protected static final int field_31615 = 75;
        protected static final int TWO_ZERO_ZERO_INDEX = Piece.getIndex(2, 0, 0);
        protected static final int TWO_TWO_ZERO_INDEX = Piece.getIndex(2, 2, 0);
        protected static final int ZERO_ONE_ZERO_INDEX = Piece.getIndex(0, 1, 0);
        protected static final int FOUR_ONE_ZERO_INDEX = Piece.getIndex(4, 1, 0);
        protected static final int field_31616 = 1001;
        protected static final int field_31617 = 1002;
        protected static final int field_31618 = 1003;
        protected PieceSetting setting;

        protected static int getIndex(int x, int y, int z) {
            return y * 25 + z * 5 + x;
        }

        public Piece(StructurePieceType type, Direction orientation, int length, BlockBox box) {
            super(type, length, box);
            this.setOrientation(orientation);
        }

        protected Piece(StructurePieceType type, int length, Direction orientation, PieceSetting setting, int j, int k, int l) {
            super(type, length, Piece.createBox(orientation, setting, j, k, l));
            this.setOrientation(orientation);
            this.setting = setting;
        }

        private static BlockBox createBox(Direction orientation, PieceSetting setting, int i, int j, int k) {
            int l = setting.roomIndex;
            int m = l % 5;
            int n = l / 5 % 5;
            int o = l / 25;
            BlockBox lv = Piece.createBox(0, 0, 0, orientation, i * 8, j * 4, k * 8);
            switch (orientation) {
                case NORTH: {
                    lv.move(m * 8, o * 4, -(n + k) * 8 + 1);
                    break;
                }
                case SOUTH: {
                    lv.move(m * 8, o * 4, n * 8);
                    break;
                }
                case WEST: {
                    lv.move(-(n + k) * 8 + 1, o * 4, m * 8);
                    break;
                }
                default: {
                    lv.move(n * 8, o * 4, m * 8);
                }
            }
            return lv;
        }

        public Piece(StructurePieceType arg, NbtCompound arg2) {
            super(arg, arg2);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
        }

        protected void setAirAndWater(StructureWorldAccess world, BlockBox box, int x, int y, int z, int width, int height, int depth) {
            for (int o = y; o <= height; ++o) {
                for (int p = x; p <= width; ++p) {
                    for (int q = z; q <= depth; ++q) {
                        BlockState lv = this.getBlockAt(world, p, o, q, box);
                        if (ICE_BLOCKS.contains(lv.getBlock())) continue;
                        if (this.applyYTransform(o) >= world.getSeaLevel() && lv != WATER) {
                            this.addBlock(world, Blocks.AIR.getDefaultState(), p, o, q, box);
                            continue;
                        }
                        this.addBlock(world, WATER, p, o, q, box);
                    }
                }
            }
        }

        protected void method_14774(StructureWorldAccess world, BlockBox box, int x, int z, boolean bl) {
            if (bl) {
                this.fillWithOutline(world, box, x + 0, 0, z + 0, x + 2, 0, z + 8 - 1, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, x + 5, 0, z + 0, x + 8 - 1, 0, z + 8 - 1, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, x + 3, 0, z + 0, x + 4, 0, z + 2, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, x + 3, 0, z + 5, x + 4, 0, z + 8 - 1, PRISMARINE, PRISMARINE, false);
                this.fillWithOutline(world, box, x + 3, 0, z + 2, x + 4, 0, z + 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, box, x + 3, 0, z + 5, x + 4, 0, z + 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, box, x + 2, 0, z + 3, x + 2, 0, z + 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithOutline(world, box, x + 5, 0, z + 3, x + 5, 0, z + 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            } else {
                this.fillWithOutline(world, box, x + 0, 0, z + 0, x + 8 - 1, 0, z + 8 - 1, PRISMARINE, PRISMARINE, false);
            }
        }

        protected void fillArea(StructureWorldAccess world, BlockBox box, int x, int y, int z, int width, int height, int depth, BlockState state) {
            for (int o = y; o <= height; ++o) {
                for (int p = x; p <= width; ++p) {
                    for (int q = z; q <= depth; ++q) {
                        if (this.getBlockAt(world, p, o, q, box) != WATER) continue;
                        this.addBlock(world, state, p, o, q, box);
                    }
                }
            }
        }

        protected boolean boxIntersects(BlockBox box, int x1, int z1, int x2, int z2) {
            int m = this.applyXTransform(x1, z1);
            int n = this.applyZTransform(x1, z1);
            int o = this.applyXTransform(x2, z2);
            int p = this.applyZTransform(x2, z2);
            return box.intersectsXZ(Math.min(m, o), Math.min(n, p), Math.max(m, o), Math.max(n, p));
        }

        protected void spawnElderGuardian(StructureWorldAccess world, BlockBox box, int x, int y, int z) {
            ElderGuardianEntity lv2;
            BlockPos.Mutable lv = this.offsetPos(x, y, z);
            if (box.contains(lv) && (lv2 = EntityType.ELDER_GUARDIAN.create(world.toServerWorld())) != null) {
                lv2.heal(lv2.getMaxHealth());
                lv2.refreshPositionAndAngles((double)lv.getX() + 0.5, lv.getY(), (double)lv.getZ() + 0.5, 0.0f, 0.0f);
                lv2.initialize(world, world.getLocalDifficulty(lv2.getBlockPos()), SpawnReason.STRUCTURE, null);
                world.spawnEntityAndPassengers(lv2);
            }
        }
    }
}

