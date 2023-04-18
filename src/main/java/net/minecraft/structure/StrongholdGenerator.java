package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class StrongholdGenerator {
   private static final int field_31624 = 3;
   private static final int field_31625 = 3;
   private static final int field_31626 = 50;
   private static final int field_31627 = 10;
   private static final boolean field_31628 = true;
   public static final int field_36417 = 64;
   private static final PieceData[] ALL_PIECES = new PieceData[]{new PieceData(Corridor.class, 40, 0), new PieceData(PrisonHall.class, 5, 5), new PieceData(LeftTurn.class, 20, 0), new PieceData(RightTurn.class, 20, 0), new PieceData(SquareRoom.class, 10, 6), new PieceData(Stairs.class, 5, 5), new PieceData(SpiralStaircase.class, 5, 5), new PieceData(FiveWayCrossing.class, 5, 4), new PieceData(ChestCorridor.class, 5, 4), new PieceData(Library.class, 10, 2) {
      public boolean canGenerate(int chainLength) {
         return super.canGenerate(chainLength) && chainLength > 4;
      }
   }, new PieceData(PortalRoom.class, 20, 1) {
      public boolean canGenerate(int chainLength) {
         return super.canGenerate(chainLength) && chainLength > 5;
      }
   }};
   private static List possiblePieces;
   static Class activePieceType;
   private static int totalWeight;
   static final StoneBrickRandomizer STONE_BRICK_RANDOMIZER = new StoneBrickRandomizer();

   public static void init() {
      possiblePieces = Lists.newArrayList();
      PieceData[] var0 = ALL_PIECES;
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         PieceData lv = var0[var2];
         lv.generatedCount = 0;
         possiblePieces.add(lv);
      }

      activePieceType = null;
   }

   private static boolean checkRemainingPieces() {
      boolean bl = false;
      totalWeight = 0;

      PieceData lv;
      for(Iterator var1 = possiblePieces.iterator(); var1.hasNext(); totalWeight += lv.weight) {
         lv = (PieceData)var1.next();
         if (lv.limit > 0 && lv.generatedCount < lv.limit) {
            bl = true;
         }
      }

      return bl;
   }

   private static Piece createPiece(Class pieceType, StructurePiecesHolder holder, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength) {
      Piece lv = null;
      if (pieceType == Corridor.class) {
         lv = StrongholdGenerator.Corridor.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == PrisonHall.class) {
         lv = StrongholdGenerator.PrisonHall.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == LeftTurn.class) {
         lv = StrongholdGenerator.LeftTurn.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == RightTurn.class) {
         lv = StrongholdGenerator.RightTurn.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == SquareRoom.class) {
         lv = StrongholdGenerator.SquareRoom.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == Stairs.class) {
         lv = StrongholdGenerator.Stairs.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == SpiralStaircase.class) {
         lv = StrongholdGenerator.SpiralStaircase.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == FiveWayCrossing.class) {
         lv = StrongholdGenerator.FiveWayCrossing.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == ChestCorridor.class) {
         lv = StrongholdGenerator.ChestCorridor.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == Library.class) {
         lv = StrongholdGenerator.Library.create(holder, random, x, y, z, orientation, chainLength);
      } else if (pieceType == PortalRoom.class) {
         lv = StrongholdGenerator.PortalRoom.create(holder, x, y, z, orientation, chainLength);
      }

      return (Piece)lv;
   }

   private static Piece pickPiece(Start start, StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
      if (!checkRemainingPieces()) {
         return null;
      } else {
         if (activePieceType != null) {
            Piece lv = createPiece(activePieceType, holder, random, x, y, z, orientation, chainLength);
            activePieceType = null;
            if (lv != null) {
               return lv;
            }
         }

         int m = 0;

         while(m < 5) {
            ++m;
            int n = random.nextInt(totalWeight);
            Iterator var10 = possiblePieces.iterator();

            while(var10.hasNext()) {
               PieceData lv2 = (PieceData)var10.next();
               n -= lv2.weight;
               if (n < 0) {
                  if (!lv2.canGenerate(chainLength) || lv2 == start.lastPiece) {
                     break;
                  }

                  Piece lv3 = createPiece(lv2.pieceType, holder, random, x, y, z, orientation, chainLength);
                  if (lv3 != null) {
                     ++lv2.generatedCount;
                     start.lastPiece = lv2;
                     if (!lv2.canGenerate()) {
                        possiblePieces.remove(lv2);
                     }

                     return lv3;
                  }
               }
            }
         }

         BlockBox lv4 = StrongholdGenerator.SmallCorridor.create(holder, random, x, y, z, orientation);
         if (lv4 != null && lv4.getMinY() > 1) {
            return new SmallCorridor(chainLength, lv4, orientation);
         } else {
            return null;
         }
      }
   }

   static StructurePiece pieceGenerator(Start start, StructurePiecesHolder holder, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength) {
      if (chainLength > 50) {
         return null;
      } else if (Math.abs(x - start.getBoundingBox().getMinX()) <= 112 && Math.abs(z - start.getBoundingBox().getMinZ()) <= 112) {
         StructurePiece lv = pickPiece(start, holder, random, x, y, z, orientation, chainLength + 1);
         if (lv != null) {
            holder.addPiece(lv);
            start.pieces.add(lv);
         }

         return lv;
      } else {
         return null;
      }
   }

   private static class PieceData {
      public final Class pieceType;
      public final int weight;
      public int generatedCount;
      public final int limit;

      public PieceData(Class pieceType, int weight, int limit) {
         this.pieceType = pieceType;
         this.weight = weight;
         this.limit = limit;
      }

      public boolean canGenerate(int chainLength) {
         return this.limit == 0 || this.generatedCount < this.limit;
      }

      public boolean canGenerate() {
         return this.limit == 0 || this.generatedCount < this.limit;
      }
   }

   public static class Corridor extends Piece {
      private static final int SIZE_X = 5;
      private static final int SIZE_Y = 5;
      private static final int SIZE_Z = 7;
      private final boolean leftExitExists;
      private final boolean rightExitExists;

      public Corridor(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_CORRIDOR, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.leftExitExists = random.nextInt(2) == 0;
         this.rightExitExists = random.nextInt(2) == 0;
      }

      public Corridor(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_CORRIDOR, nbt);
         this.leftExitExists = nbt.getBoolean("Left");
         this.rightExitExists = nbt.getBoolean("Right");
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putBoolean("Left", this.leftExitExists);
         nbt.putBoolean("Right", this.rightExitExists);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         this.fillForwardOpening((Start)start, holder, random, 1, 1);
         if (this.leftExitExists) {
            this.fillNWOpening((Start)start, holder, random, 1, 2);
         }

         if (this.rightExitExists) {
            this.fillSEOpening((Start)start, holder, random, 1, 2);
         }

      }

      public static Corridor create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 7, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new Corridor(chainLength, random, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 4, 6, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 1, 1, 0);
         this.generateEntrance(world, random, chunkBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 6);
         BlockState lv = (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST);
         BlockState lv2 = (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST);
         this.addBlockWithRandomThreshold(world, chunkBox, random, 0.1F, 1, 2, 1, lv);
         this.addBlockWithRandomThreshold(world, chunkBox, random, 0.1F, 3, 2, 1, lv2);
         this.addBlockWithRandomThreshold(world, chunkBox, random, 0.1F, 1, 2, 5, lv);
         this.addBlockWithRandomThreshold(world, chunkBox, random, 0.1F, 3, 2, 5, lv2);
         if (this.leftExitExists) {
            this.fillWithOutline(world, chunkBox, 0, 1, 2, 0, 3, 4, AIR, AIR, false);
         }

         if (this.rightExitExists) {
            this.fillWithOutline(world, chunkBox, 4, 1, 2, 4, 3, 4, AIR, AIR, false);
         }

      }
   }

   public static class PrisonHall extends Piece {
      protected static final int SIZE_X = 9;
      protected static final int SIZE_Y = 5;
      protected static final int SIZE_Z = 11;

      public PrisonHall(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
      }

      public PrisonHall(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, nbt);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         this.fillForwardOpening((Start)start, holder, random, 1, 1);
      }

      public static PrisonHall create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -1, -1, 0, 9, 5, 11, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new PrisonHall(chainLength, random, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 8, 4, 10, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 1, 1, 0);
         this.fillWithOutline(world, chunkBox, 1, 1, 10, 3, 3, 10, AIR, AIR, false);
         this.fillWithOutline(world, chunkBox, 4, 1, 1, 4, 3, 1, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 4, 1, 3, 4, 3, 3, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 4, 1, 7, 4, 3, 7, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 4, 1, 9, 4, 3, 9, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);

         for(int i = 1; i <= 3; ++i) {
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, i, 4, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)).with(PaneBlock.EAST, true), 4, i, 5, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, i, 6, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 5, i, 5, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 6, i, 5, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 7, i, 5, chunkBox);
         }

         this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, 3, 2, chunkBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, 3, 8, chunkBox);
         BlockState lv = (BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.FACING, Direction.WEST);
         BlockState lv2 = (BlockState)((BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.FACING, Direction.WEST)).with(DoorBlock.HALF, DoubleBlockHalf.UPPER);
         this.addBlock(world, lv, 4, 1, 2, chunkBox);
         this.addBlock(world, lv2, 4, 2, 2, chunkBox);
         this.addBlock(world, lv, 4, 1, 8, chunkBox);
         this.addBlock(world, lv2, 4, 2, 8, chunkBox);
      }
   }

   public static class LeftTurn extends Turn {
      public LeftTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
      }

      public LeftTurn(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, nbt);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         Direction lv = this.getFacing();
         if (lv != Direction.NORTH && lv != Direction.EAST) {
            this.fillSEOpening((Start)start, holder, random, 1, 1);
         } else {
            this.fillNWOpening((Start)start, holder, random, 1, 1);
         }

      }

      public static LeftTurn create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 5, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new LeftTurn(chainLength, random, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 4, 4, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 1, 1, 0);
         Direction lv = this.getFacing();
         if (lv != Direction.NORTH && lv != Direction.EAST) {
            this.fillWithOutline(world, chunkBox, 4, 1, 1, 4, 3, 3, AIR, AIR, false);
         } else {
            this.fillWithOutline(world, chunkBox, 0, 1, 1, 0, 3, 3, AIR, AIR, false);
         }

      }
   }

   public static class RightTurn extends Turn {
      public RightTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
      }

      public RightTurn(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, nbt);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         Direction lv = this.getFacing();
         if (lv != Direction.NORTH && lv != Direction.EAST) {
            this.fillNWOpening((Start)start, holder, random, 1, 1);
         } else {
            this.fillSEOpening((Start)start, holder, random, 1, 1);
         }

      }

      public static RightTurn create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 5, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new RightTurn(chainLength, random, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 4, 4, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 1, 1, 0);
         Direction lv = this.getFacing();
         if (lv != Direction.NORTH && lv != Direction.EAST) {
            this.fillWithOutline(world, chunkBox, 0, 1, 1, 0, 3, 3, AIR, AIR, false);
         } else {
            this.fillWithOutline(world, chunkBox, 4, 1, 1, 4, 3, 3, AIR, AIR, false);
         }

      }
   }

   public static class SquareRoom extends Piece {
      protected static final int SIZE_X = 11;
      protected static final int SIZE_Y = 7;
      protected static final int SIZE_Z = 11;
      protected final int roomType;

      public SquareRoom(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_SQUARE_ROOM, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.roomType = random.nextInt(5);
      }

      public SquareRoom(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_SQUARE_ROOM, nbt);
         this.roomType = nbt.getInt("Type");
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putInt("Type", this.roomType);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         this.fillForwardOpening((Start)start, holder, random, 4, 1);
         this.fillNWOpening((Start)start, holder, random, 1, 4);
         this.fillSEOpening((Start)start, holder, random, 1, 4);
      }

      public static SquareRoom create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -4, -1, 0, 11, 7, 11, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new SquareRoom(chainLength, random, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 10, 6, 10, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 4, 1, 0);
         this.fillWithOutline(world, chunkBox, 4, 1, 10, 6, 3, 10, AIR, AIR, false);
         this.fillWithOutline(world, chunkBox, 0, 1, 4, 0, 3, 6, AIR, AIR, false);
         this.fillWithOutline(world, chunkBox, 10, 1, 4, 10, 3, 6, AIR, AIR, false);
         int i;
         switch (this.roomType) {
            case 0:
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 1, 5, chunkBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 2, 5, chunkBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 3, 5, chunkBox);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, chunkBox);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, chunkBox);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, chunkBox);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, chunkBox);
               this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 4, chunkBox);
               this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 5, chunkBox);
               this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 6, chunkBox);
               this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 4, chunkBox);
               this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 5, chunkBox);
               this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 6, chunkBox);
               this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 5, 1, 4, chunkBox);
               this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 5, 1, 6, chunkBox);
               break;
            case 1:
               for(i = 0; i < 5; ++i) {
                  this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 1, 3 + i, chunkBox);
                  this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 7, 1, 3 + i, chunkBox);
                  this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3 + i, 1, 3, chunkBox);
                  this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3 + i, 1, 7, chunkBox);
               }

               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 1, 5, chunkBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 2, 5, chunkBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 3, 5, chunkBox);
               this.addBlock(world, Blocks.WATER.getDefaultState(), 5, 4, 5, chunkBox);
               break;
            case 2:
               for(i = 1; i <= 9; ++i) {
                  this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 1, 3, i, chunkBox);
                  this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 9, 3, i, chunkBox);
               }

               for(i = 1; i <= 9; ++i) {
                  this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), i, 3, 1, chunkBox);
                  this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), i, 3, 9, chunkBox);
               }

               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 4, chunkBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 6, chunkBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 4, chunkBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 6, chunkBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, 1, 5, chunkBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, 1, 5, chunkBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, 3, 5, chunkBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, 3, 5, chunkBox);

               for(i = 1; i <= 3; ++i) {
                  this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, i, 4, chunkBox);
                  this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, i, 4, chunkBox);
                  this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, i, 6, chunkBox);
                  this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, i, 6, chunkBox);
               }

               this.addBlock(world, Blocks.TORCH.getDefaultState(), 5, 3, 5, chunkBox);

               for(i = 2; i <= 8; ++i) {
                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 2, 3, i, chunkBox);
                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 3, 3, i, chunkBox);
                  if (i <= 3 || i >= 7) {
                     this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 4, 3, i, chunkBox);
                     this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 5, 3, i, chunkBox);
                     this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 6, 3, i, chunkBox);
                  }

                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 7, 3, i, chunkBox);
                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 8, 3, i, chunkBox);
               }

               BlockState lv = (BlockState)Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.WEST);
               this.addBlock(world, lv, 9, 1, 3, chunkBox);
               this.addBlock(world, lv, 9, 2, 3, chunkBox);
               this.addBlock(world, lv, 9, 3, 3, chunkBox);
               this.addChest(world, chunkBox, random, 3, 4, 8, LootTables.STRONGHOLD_CROSSING_CHEST);
         }

      }
   }

   public static class Stairs extends Piece {
      private static final int SIZE_X = 5;
      private static final int SIZE_Y = 11;
      private static final int SIZE_Z = 8;

      public Stairs(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_STAIRS, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
      }

      public Stairs(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_STAIRS, nbt);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         this.fillForwardOpening((Start)start, holder, random, 1, 1);
      }

      public static Stairs create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -1, -7, 0, 5, 11, 8, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new Stairs(chainLength, random, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 10, 7, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 1, 7, 0);
         this.generateEntrance(world, random, chunkBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 7);
         BlockState lv = (BlockState)Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);

         for(int i = 0; i < 6; ++i) {
            this.addBlock(world, lv, 1, 6 - i, 1 + i, chunkBox);
            this.addBlock(world, lv, 2, 6 - i, 1 + i, chunkBox);
            this.addBlock(world, lv, 3, 6 - i, 1 + i, chunkBox);
            if (i < 5) {
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5 - i, 1 + i, chunkBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 5 - i, 1 + i, chunkBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 5 - i, 1 + i, chunkBox);
            }
         }

      }
   }

   public static class SpiralStaircase extends Piece {
      private static final int SIZE_X = 5;
      private static final int SIZE_Y = 11;
      private static final int SIZE_Z = 5;
      private final boolean isStructureStart;

      public SpiralStaircase(StructurePieceType structurePieceType, int chainLength, int x, int z, Direction orientation) {
         super(structurePieceType, chainLength, createBox(x, 64, z, orientation, 5, 11, 5));
         this.isStructureStart = true;
         this.setOrientation(orientation);
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.OPENING;
      }

      public SpiralStaircase(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_SPIRAL_STAIRCASE, chainLength, boundingBox);
         this.isStructureStart = false;
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
      }

      public SpiralStaircase(StructurePieceType arg, NbtCompound arg2) {
         super(arg, arg2);
         this.isStructureStart = arg2.getBoolean("Source");
      }

      public SpiralStaircase(NbtCompound nbt) {
         this(StructurePieceType.STRONGHOLD_SPIRAL_STAIRCASE, nbt);
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putBoolean("Source", this.isStructureStart);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         if (this.isStructureStart) {
            StrongholdGenerator.activePieceType = FiveWayCrossing.class;
         }

         this.fillForwardOpening((Start)start, holder, random, 1, 1);
      }

      public static SpiralStaircase create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -1, -7, 0, 5, 11, 5, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new SpiralStaircase(chainLength, random, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 10, 4, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 1, 7, 0);
         this.generateEntrance(world, random, chunkBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 4);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 6, 1, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5, 1, chunkBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 6, 1, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5, 2, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 4, 3, chunkBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 5, 3, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 4, 3, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 3, 3, chunkBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 3, 4, 3, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 3, 2, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 2, 1, chunkBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 3, 3, 1, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 2, 1, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 1, 1, chunkBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 2, 1, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 1, 2, chunkBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 1, 3, chunkBox);
      }
   }

   public static class FiveWayCrossing extends Piece {
      protected static final int SIZE_X = 10;
      protected static final int SIZE_Y = 9;
      protected static final int SIZE_Z = 11;
      private final boolean lowerLeftExists;
      private final boolean upperLeftExists;
      private final boolean lowerRightExists;
      private final boolean upperRightExists;

      public FiveWayCrossing(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_FIVE_WAY_CROSSING, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.lowerLeftExists = random.nextBoolean();
         this.upperLeftExists = random.nextBoolean();
         this.lowerRightExists = random.nextBoolean();
         this.upperRightExists = random.nextInt(3) > 0;
      }

      public FiveWayCrossing(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_FIVE_WAY_CROSSING, nbt);
         this.lowerLeftExists = nbt.getBoolean("leftLow");
         this.upperLeftExists = nbt.getBoolean("leftHigh");
         this.lowerRightExists = nbt.getBoolean("rightLow");
         this.upperRightExists = nbt.getBoolean("rightHigh");
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putBoolean("leftLow", this.lowerLeftExists);
         nbt.putBoolean("leftHigh", this.upperLeftExists);
         nbt.putBoolean("rightLow", this.lowerRightExists);
         nbt.putBoolean("rightHigh", this.upperRightExists);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         int i = 3;
         int j = 5;
         Direction lv = this.getFacing();
         if (lv == Direction.WEST || lv == Direction.NORTH) {
            i = 8 - i;
            j = 8 - j;
         }

         this.fillForwardOpening((Start)start, holder, random, 5, 1);
         if (this.lowerLeftExists) {
            this.fillNWOpening((Start)start, holder, random, i, 1);
         }

         if (this.upperLeftExists) {
            this.fillNWOpening((Start)start, holder, random, j, 7);
         }

         if (this.lowerRightExists) {
            this.fillSEOpening((Start)start, holder, random, i, 1);
         }

         if (this.upperRightExists) {
            this.fillSEOpening((Start)start, holder, random, j, 7);
         }

      }

      public static FiveWayCrossing create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -4, -3, 0, 10, 9, 11, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new FiveWayCrossing(chainLength, random, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 9, 8, 10, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 4, 3, 0);
         if (this.lowerLeftExists) {
            this.fillWithOutline(world, chunkBox, 0, 3, 1, 0, 5, 3, AIR, AIR, false);
         }

         if (this.lowerRightExists) {
            this.fillWithOutline(world, chunkBox, 9, 3, 1, 9, 5, 3, AIR, AIR, false);
         }

         if (this.upperLeftExists) {
            this.fillWithOutline(world, chunkBox, 0, 5, 7, 0, 7, 9, AIR, AIR, false);
         }

         if (this.upperRightExists) {
            this.fillWithOutline(world, chunkBox, 9, 5, 7, 9, 7, 9, AIR, AIR, false);
         }

         this.fillWithOutline(world, chunkBox, 5, 1, 10, 7, 3, 10, AIR, AIR, false);
         this.fillWithOutline(world, chunkBox, 1, 2, 1, 8, 2, 6, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 4, 1, 5, 4, 4, 9, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 8, 1, 5, 8, 4, 9, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 1, 4, 7, 3, 4, 9, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 1, 3, 5, 3, 3, 6, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, chunkBox, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, chunkBox, 5, 1, 7, 7, 1, 8, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, chunkBox, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, chunkBox, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, chunkBox, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, chunkBox, 5, 5, 7, 7, 5, 9, (BlockState)Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE), (BlockState)Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE), false);
         this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, chunkBox);
      }
   }

   public static class ChestCorridor extends Piece {
      private static final int SIZE_X = 5;
      private static final int SIZE_Y = 5;
      private static final int SIZE_Z = 7;
      private boolean chestGenerated;

      public ChestCorridor(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
      }

      public ChestCorridor(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, nbt);
         this.chestGenerated = nbt.getBoolean("Chest");
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putBoolean("Chest", this.chestGenerated);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         this.fillForwardOpening((Start)start, holder, random, 1, 1);
      }

      public static ChestCorridor create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainlength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 7, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new ChestCorridor(chainlength, random, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 4, 4, 6, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 1, 1, 0);
         this.generateEntrance(world, random, chunkBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 6);
         this.fillWithOutline(world, chunkBox, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.getDefaultState(), Blocks.STONE_BRICKS.getDefaultState(), false);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 1, 1, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 1, 5, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 2, 2, chunkBox);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 2, 4, chunkBox);

         for(int i = 2; i <= 4; ++i) {
            this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 2, 1, i, chunkBox);
         }

         if (!this.chestGenerated && chunkBox.contains(this.offsetPos(3, 2, 3))) {
            this.chestGenerated = true;
            this.addChest(world, chunkBox, random, 3, 2, 3, LootTables.STRONGHOLD_CORRIDOR_CHEST);
         }

      }
   }

   public static class Library extends Piece {
      protected static final int SIZE_X = 14;
      protected static final int field_31636 = 6;
      protected static final int SIZE_Y = 11;
      protected static final int SIZE_Z = 15;
      private final boolean tall;

      public Library(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.tall = boundingBox.getBlockCountY() > 6;
      }

      public Library(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, nbt);
         this.tall = nbt.getBoolean("Tall");
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putBoolean("Tall", this.tall);
      }

      public static Library create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -4, -1, 0, 14, 11, 15, orientation);
         if (!isInBounds(lv) || holder.getIntersecting(lv) != null) {
            lv = BlockBox.rotated(x, y, z, -4, -1, 0, 14, 6, 15, orientation);
            if (!isInBounds(lv) || holder.getIntersecting(lv) != null) {
               return null;
            }
         }

         return new Library(chainLength, random, lv, orientation);
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         int i = 11;
         if (!this.tall) {
            i = 6;
         }

         this.fillWithOutline(world, chunkBox, 0, 0, 0, 13, i - 1, 14, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, this.entryDoor, 4, 1, 0);
         this.fillWithOutlineUnderSeaLevel(world, chunkBox, random, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.getDefaultState(), Blocks.COBWEB.getDefaultState(), false, false);
         int j = true;
         int k = true;

         int l;
         for(l = 1; l <= 13; ++l) {
            if ((l - 1) % 4 == 0) {
               this.fillWithOutline(world, chunkBox, 1, 1, l, 1, 4, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
               this.fillWithOutline(world, chunkBox, 12, 1, l, 12, 4, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST), 2, 3, l, chunkBox);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST), 11, 3, l, chunkBox);
               if (this.tall) {
                  this.fillWithOutline(world, chunkBox, 1, 6, l, 1, 9, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                  this.fillWithOutline(world, chunkBox, 12, 6, l, 12, 9, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
               }
            } else {
               this.fillWithOutline(world, chunkBox, 1, 1, l, 1, 4, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               this.fillWithOutline(world, chunkBox, 12, 1, l, 12, 4, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               if (this.tall) {
                  this.fillWithOutline(world, chunkBox, 1, 6, l, 1, 9, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                  this.fillWithOutline(world, chunkBox, 12, 6, l, 12, 9, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               }
            }
         }

         for(l = 3; l < 12; l += 2) {
            this.fillWithOutline(world, chunkBox, 3, 1, l, 4, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 6, 1, l, 7, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 9, 1, l, 10, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
         }

         if (this.tall) {
            this.fillWithOutline(world, chunkBox, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.fillWithOutline(world, chunkBox, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 9, 5, 11, chunkBox);
            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 8, 5, 11, chunkBox);
            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 9, 5, 10, chunkBox);
            BlockState lv = (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState lv2 = (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            this.fillWithOutline(world, chunkBox, 3, 6, 3, 3, 6, 11, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 10, 6, 3, 10, 6, 9, lv2, lv2, false);
            this.fillWithOutline(world, chunkBox, 4, 6, 2, 9, 6, 2, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 4, 6, 12, 7, 6, 12, lv, lv, false);
            this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 3, 6, 2, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 3, 6, 12, chunkBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 10, 6, 2, chunkBox);

            for(int m = 0; m <= 2; ++m) {
               this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 8 + m, 6, 12 - m, chunkBox);
               if (m != 2) {
                  this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 8 + m, 6, 11 - m, chunkBox);
               }
            }

            BlockState lv3 = (BlockState)Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH);
            this.addBlock(world, lv3, 10, 1, 13, chunkBox);
            this.addBlock(world, lv3, 10, 2, 13, chunkBox);
            this.addBlock(world, lv3, 10, 3, 13, chunkBox);
            this.addBlock(world, lv3, 10, 4, 13, chunkBox);
            this.addBlock(world, lv3, 10, 5, 13, chunkBox);
            this.addBlock(world, lv3, 10, 6, 13, chunkBox);
            this.addBlock(world, lv3, 10, 7, 13, chunkBox);
            int n = true;
            int o = true;
            BlockState lv4 = (BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.EAST, true);
            this.addBlock(world, lv4, 6, 9, 7, chunkBox);
            BlockState lv5 = (BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.WEST, true);
            this.addBlock(world, lv5, 7, 9, 7, chunkBox);
            this.addBlock(world, lv4, 6, 8, 7, chunkBox);
            this.addBlock(world, lv5, 7, 8, 7, chunkBox);
            BlockState lv6 = (BlockState)((BlockState)lv2.with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            this.addBlock(world, lv6, 6, 7, 7, chunkBox);
            this.addBlock(world, lv6, 7, 7, 7, chunkBox);
            this.addBlock(world, lv4, 5, 7, 7, chunkBox);
            this.addBlock(world, lv5, 8, 7, 7, chunkBox);
            this.addBlock(world, (BlockState)lv4.with(FenceBlock.NORTH, true), 6, 7, 6, chunkBox);
            this.addBlock(world, (BlockState)lv4.with(FenceBlock.SOUTH, true), 6, 7, 8, chunkBox);
            this.addBlock(world, (BlockState)lv5.with(FenceBlock.NORTH, true), 7, 7, 6, chunkBox);
            this.addBlock(world, (BlockState)lv5.with(FenceBlock.SOUTH, true), 7, 7, 8, chunkBox);
            BlockState lv7 = Blocks.TORCH.getDefaultState();
            this.addBlock(world, lv7, 5, 8, 7, chunkBox);
            this.addBlock(world, lv7, 8, 8, 7, chunkBox);
            this.addBlock(world, lv7, 6, 8, 6, chunkBox);
            this.addBlock(world, lv7, 6, 8, 8, chunkBox);
            this.addBlock(world, lv7, 7, 8, 6, chunkBox);
            this.addBlock(world, lv7, 7, 8, 8, chunkBox);
         }

         this.addChest(world, chunkBox, random, 3, 3, 5, LootTables.STRONGHOLD_LIBRARY_CHEST);
         if (this.tall) {
            this.addBlock(world, AIR, 12, 9, 1, chunkBox);
            this.addChest(world, chunkBox, random, 12, 8, 1, LootTables.STRONGHOLD_LIBRARY_CHEST);
         }

      }
   }

   public static class PortalRoom extends Piece {
      protected static final int SIZE_X = 11;
      protected static final int SIZE_Y = 8;
      protected static final int SIZE_Z = 16;
      private boolean spawnerPlaced;

      public PortalRoom(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, chainLength, boundingBox);
         this.setOrientation(orientation);
      }

      public PortalRoom(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, nbt);
         this.spawnerPlaced = nbt.getBoolean("Mob");
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putBoolean("Mob", this.spawnerPlaced);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         if (start != null) {
            ((Start)start).portalRoom = this;
         }

      }

      public static PortalRoom create(StructurePiecesHolder holder, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox lv = BlockBox.rotated(x, y, z, -4, -1, 0, 11, 8, 16, orientation);
         return isInBounds(lv) && holder.getIntersecting(lv) == null ? new PortalRoom(chainLength, lv, orientation) : null;
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         this.fillWithOutline(world, chunkBox, 0, 0, 0, 10, 7, 15, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, chunkBox, StrongholdGenerator.Piece.EntranceType.GRATES, 4, 1, 0);
         int i = true;
         this.fillWithOutline(world, chunkBox, 1, 6, 1, 1, 6, 14, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 9, 6, 1, 9, 6, 14, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 2, 6, 1, 8, 6, 2, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 2, 6, 14, 8, 6, 14, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 1, 1, 1, 2, 1, 4, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 8, 1, 1, 9, 1, 4, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 1, 1, 1, 1, 1, 3, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
         this.fillWithOutline(world, chunkBox, 9, 1, 1, 9, 1, 3, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
         this.fillWithOutline(world, chunkBox, 3, 1, 8, 7, 1, 12, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 4, 1, 9, 6, 1, 11, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
         BlockState lv = (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true);
         BlockState lv2 = (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true);

         int j;
         for(j = 3; j < 14; j += 2) {
            this.fillWithOutline(world, chunkBox, 0, 3, j, 0, 4, j, lv, lv, false);
            this.fillWithOutline(world, chunkBox, 10, 3, j, 10, 4, j, lv, lv, false);
         }

         for(j = 2; j < 9; j += 2) {
            this.fillWithOutline(world, chunkBox, j, 3, 15, j, 4, 15, lv2, lv2, false);
         }

         BlockState lv3 = (BlockState)Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
         this.fillWithOutline(world, chunkBox, 4, 1, 5, 6, 1, 7, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 4, 2, 6, 6, 2, 7, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, chunkBox, 4, 3, 7, 6, 3, 7, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);

         for(int k = 4; k <= 6; ++k) {
            this.addBlock(world, lv3, k, 1, 4, chunkBox);
            this.addBlock(world, lv3, k, 2, 5, chunkBox);
            this.addBlock(world, lv3, k, 3, 6, chunkBox);
         }

         BlockState lv4 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
         BlockState lv5 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
         BlockState lv6 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
         BlockState lv7 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);
         boolean bl = true;
         boolean[] bls = new boolean[12];

         for(int l = 0; l < bls.length; ++l) {
            bls[l] = random.nextFloat() > 0.9F;
            bl &= bls[l];
         }

         this.addBlock(world, (BlockState)lv4.with(EndPortalFrameBlock.EYE, bls[0]), 4, 3, 8, chunkBox);
         this.addBlock(world, (BlockState)lv4.with(EndPortalFrameBlock.EYE, bls[1]), 5, 3, 8, chunkBox);
         this.addBlock(world, (BlockState)lv4.with(EndPortalFrameBlock.EYE, bls[2]), 6, 3, 8, chunkBox);
         this.addBlock(world, (BlockState)lv5.with(EndPortalFrameBlock.EYE, bls[3]), 4, 3, 12, chunkBox);
         this.addBlock(world, (BlockState)lv5.with(EndPortalFrameBlock.EYE, bls[4]), 5, 3, 12, chunkBox);
         this.addBlock(world, (BlockState)lv5.with(EndPortalFrameBlock.EYE, bls[5]), 6, 3, 12, chunkBox);
         this.addBlock(world, (BlockState)lv6.with(EndPortalFrameBlock.EYE, bls[6]), 3, 3, 9, chunkBox);
         this.addBlock(world, (BlockState)lv6.with(EndPortalFrameBlock.EYE, bls[7]), 3, 3, 10, chunkBox);
         this.addBlock(world, (BlockState)lv6.with(EndPortalFrameBlock.EYE, bls[8]), 3, 3, 11, chunkBox);
         this.addBlock(world, (BlockState)lv7.with(EndPortalFrameBlock.EYE, bls[9]), 7, 3, 9, chunkBox);
         this.addBlock(world, (BlockState)lv7.with(EndPortalFrameBlock.EYE, bls[10]), 7, 3, 10, chunkBox);
         this.addBlock(world, (BlockState)lv7.with(EndPortalFrameBlock.EYE, bls[11]), 7, 3, 11, chunkBox);
         if (bl) {
            BlockState lv8 = Blocks.END_PORTAL.getDefaultState();
            this.addBlock(world, lv8, 4, 3, 9, chunkBox);
            this.addBlock(world, lv8, 5, 3, 9, chunkBox);
            this.addBlock(world, lv8, 6, 3, 9, chunkBox);
            this.addBlock(world, lv8, 4, 3, 10, chunkBox);
            this.addBlock(world, lv8, 5, 3, 10, chunkBox);
            this.addBlock(world, lv8, 6, 3, 10, chunkBox);
            this.addBlock(world, lv8, 4, 3, 11, chunkBox);
            this.addBlock(world, lv8, 5, 3, 11, chunkBox);
            this.addBlock(world, lv8, 6, 3, 11, chunkBox);
         }

         if (!this.spawnerPlaced) {
            BlockPos lv9 = this.offsetPos(5, 3, 6);
            if (chunkBox.contains(lv9)) {
               this.spawnerPlaced = true;
               world.setBlockState(lv9, Blocks.SPAWNER.getDefaultState(), Block.NOTIFY_LISTENERS);
               BlockEntity lv10 = world.getBlockEntity(lv9);
               if (lv10 instanceof MobSpawnerBlockEntity) {
                  MobSpawnerBlockEntity lv11 = (MobSpawnerBlockEntity)lv10;
                  lv11.setEntityType(EntityType.SILVERFISH, random);
               }
            }
         }

      }
   }

   private abstract static class Piece extends StructurePiece {
      protected EntranceType entryDoor;

      protected Piece(StructurePieceType arg, int i, BlockBox arg2) {
         super(arg, i, arg2);
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.OPENING;
      }

      public Piece(StructurePieceType arg, NbtCompound arg2) {
         super(arg, arg2);
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.OPENING;
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.valueOf(arg2.getString("EntryDoor"));
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         nbt.putString("EntryDoor", this.entryDoor.name());
      }

      protected void generateEntrance(StructureWorldAccess world, Random random, BlockBox boundingBox, EntranceType type, int x, int y, int z) {
         switch (type) {
            case OPENING:
               this.fillWithOutline(world, boundingBox, x, y, z, x + 3 - 1, y + 3 - 1, z, AIR, AIR, false);
               break;
            case WOOD_DOOR:
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 1, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 2, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 1, y + 2, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 2, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 1, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y, z, boundingBox);
               this.addBlock(world, Blocks.OAK_DOOR.getDefaultState(), x + 1, y, z, boundingBox);
               this.addBlock(world, (BlockState)Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, boundingBox);
               break;
            case GRATES:
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), x + 1, y, z, boundingBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), x + 1, y + 1, z, boundingBox);
               this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true), x, y, z, boundingBox);
               this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true), x, y + 1, z, boundingBox);
               this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x, y + 2, z, boundingBox);
               this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x + 1, y + 2, z, boundingBox);
               this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x + 2, y + 2, z, boundingBox);
               this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true), x + 2, y + 1, z, boundingBox);
               this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true), x + 2, y, z, boundingBox);
               break;
            case IRON_DOOR:
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 1, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 2, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 1, y + 2, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 2, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 1, z, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y, z, boundingBox);
               this.addBlock(world, Blocks.IRON_DOOR.getDefaultState(), x + 1, y, z, boundingBox);
               this.addBlock(world, (BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, boundingBox);
               this.addBlock(world, (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.NORTH), x + 2, y + 1, z + 1, boundingBox);
               this.addBlock(world, (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.SOUTH), x + 2, y + 1, z - 1, boundingBox);
         }

      }

      protected EntranceType getRandomEntrance(Random random) {
         int i = random.nextInt(5);
         switch (i) {
            case 0:
            case 1:
            default:
               return StrongholdGenerator.Piece.EntranceType.OPENING;
            case 2:
               return StrongholdGenerator.Piece.EntranceType.WOOD_DOOR;
            case 3:
               return StrongholdGenerator.Piece.EntranceType.GRATES;
            case 4:
               return StrongholdGenerator.Piece.EntranceType.IRON_DOOR;
         }
      }

      @Nullable
      protected StructurePiece fillForwardOpening(Start start, StructurePiecesHolder holder, Random random, int leftRightOffset, int heightOffset) {
         Direction lv = this.getFacing();
         if (lv != null) {
            switch (lv) {
               case NORTH:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() - 1, lv, this.getChainLength());
               case SOUTH:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMaxZ() + 1, lv, this.getChainLength());
               case WEST:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, lv, this.getChainLength());
               case EAST:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, lv, this.getChainLength());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece fillNWOpening(Start start, StructurePiecesHolder holder, Random random, int heightOffset, int leftRightOffset) {
         Direction lv = this.getFacing();
         if (lv != null) {
            switch (lv) {
               case NORTH:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.WEST, this.getChainLength());
               case SOUTH:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.WEST, this.getChainLength());
               case WEST:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() - 1, Direction.NORTH, this.getChainLength());
               case EAST:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() - 1, Direction.NORTH, this.getChainLength());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece fillSEOpening(Start start, StructurePiecesHolder holder, Random random, int heightOffset, int leftRightOffset) {
         Direction lv = this.getFacing();
         if (lv != null) {
            switch (lv) {
               case NORTH:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.EAST, this.getChainLength());
               case SOUTH:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMinZ() + leftRightOffset, Direction.EAST, this.getChainLength());
               case WEST:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, this.getChainLength());
               case EAST:
                  return StrongholdGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + leftRightOffset, this.boundingBox.getMinY() + heightOffset, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, this.getChainLength());
            }
         }

         return null;
      }

      protected static boolean isInBounds(BlockBox boundingBox) {
         return boundingBox != null && boundingBox.getMinY() > 10;
      }

      protected static enum EntranceType {
         OPENING,
         WOOD_DOOR,
         GRATES,
         IRON_DOOR;

         // $FF: synthetic method
         private static EntranceType[] method_36762() {
            return new EntranceType[]{OPENING, WOOD_DOOR, GRATES, IRON_DOOR};
         }
      }
   }

   public static class Start extends SpiralStaircase {
      public PieceData lastPiece;
      @Nullable
      public PortalRoom portalRoom;
      public final List pieces = Lists.newArrayList();

      public Start(Random random, int i, int j) {
         super(StructurePieceType.STRONGHOLD_START, 0, i, j, getRandomHorizontalDirection(random));
      }

      public Start(NbtCompound arg) {
         super(StructurePieceType.STRONGHOLD_START, arg);
      }

      public BlockPos getCenter() {
         return this.portalRoom != null ? this.portalRoom.getCenter() : super.getCenter();
      }
   }

   public static class SmallCorridor extends Piece {
      private final int length;

      public SmallCorridor(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_SMALL_CORRIDOR, chainLength, boundingBox);
         this.setOrientation(orientation);
         this.length = orientation != Direction.NORTH && orientation != Direction.SOUTH ? boundingBox.getBlockCountX() : boundingBox.getBlockCountZ();
      }

      public SmallCorridor(NbtCompound nbt) {
         super(StructurePieceType.STRONGHOLD_SMALL_CORRIDOR, nbt);
         this.length = nbt.getInt("Steps");
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putInt("Steps", this.length);
      }

      public static BlockBox create(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation) {
         int l = true;
         BlockBox lv = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 4, orientation);
         StructurePiece lv2 = holder.getIntersecting(lv);
         if (lv2 == null) {
            return null;
         } else {
            if (lv2.getBoundingBox().getMinY() == lv.getMinY()) {
               for(int m = 2; m >= 1; --m) {
                  lv = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, m, orientation);
                  if (!lv2.getBoundingBox().intersects(lv)) {
                     return BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, m + 1, orientation);
                  }
               }
            }

            return null;
         }
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         for(int i = 0; i < this.length; ++i) {
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, 0, i, chunkBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 0, i, chunkBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 0, i, chunkBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 0, i, chunkBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, 0, i, chunkBox);

            for(int j = 1; j <= 3; ++j) {
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, j, i, chunkBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 1, j, i, chunkBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 2, j, i, chunkBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 3, j, i, chunkBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, j, i, chunkBox);
            }

            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, 4, i, chunkBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 4, i, chunkBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 4, i, chunkBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 4, i, chunkBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, 4, i, chunkBox);
         }

      }
   }

   private static class StoneBrickRandomizer extends StructurePiece.BlockRandomizer {
      StoneBrickRandomizer() {
      }

      public void setBlock(Random random, int x, int y, int z, boolean placeBlock) {
         if (placeBlock) {
            float f = random.nextFloat();
            if (f < 0.2F) {
               this.block = Blocks.CRACKED_STONE_BRICKS.getDefaultState();
            } else if (f < 0.5F) {
               this.block = Blocks.MOSSY_STONE_BRICKS.getDefaultState();
            } else if (f < 0.55F) {
               this.block = Blocks.INFESTED_STONE_BRICKS.getDefaultState();
            } else {
               this.block = Blocks.STONE_BRICKS.getDefaultState();
            }
         } else {
            this.block = Blocks.CAVE_AIR.getDefaultState();
         }

      }
   }

   public abstract static class Turn extends Piece {
      protected static final int SIZE_X = 5;
      protected static final int SIZE_Y = 5;
      protected static final int SIZE_Z = 5;

      protected Turn(StructurePieceType arg, int i, BlockBox arg2) {
         super(arg, i, arg2);
      }

      public Turn(StructurePieceType arg, NbtCompound arg2) {
         super(arg, arg2);
      }
   }
}
