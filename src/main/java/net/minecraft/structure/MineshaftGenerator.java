package net.minecraft.structure;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.MineshaftStructure;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MineshaftGenerator {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_31551 = 3;
   private static final int field_31552 = 3;
   private static final int field_31553 = 5;
   private static final int field_31554 = 20;
   private static final int field_31555 = 50;
   private static final int field_31556 = 8;
   public static final int field_34729 = 50;

   private static MineshaftPart pickPiece(StructurePiecesHolder holder, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength, MineshaftStructure.Type type) {
      int m = random.nextInt(100);
      BlockBox lv;
      if (m >= 80) {
         lv = MineshaftGenerator.MineshaftCrossing.getBoundingBox(holder, random, x, y, z, orientation);
         if (lv != null) {
            return new MineshaftCrossing(chainLength, lv, orientation, type);
         }
      } else if (m >= 70) {
         lv = MineshaftGenerator.MineshaftStairs.getBoundingBox(holder, random, x, y, z, orientation);
         if (lv != null) {
            return new MineshaftStairs(chainLength, lv, orientation, type);
         }
      } else {
         lv = MineshaftGenerator.MineshaftCorridor.getBoundingBox(holder, random, x, y, z, orientation);
         if (lv != null) {
            return new MineshaftCorridor(chainLength, random, lv, orientation, type);
         }
      }

      return null;
   }

   static MineshaftPart pieceGenerator(StructurePiece start, StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation, int chainLength) {
      if (chainLength > 8) {
         return null;
      } else if (Math.abs(x - start.getBoundingBox().getMinX()) <= 80 && Math.abs(z - start.getBoundingBox().getMinZ()) <= 80) {
         MineshaftStructure.Type lv = ((MineshaftPart)start).mineshaftType;
         MineshaftPart lv2 = pickPiece(holder, random, x, y, z, orientation, chainLength + 1, lv);
         if (lv2 != null) {
            holder.addPiece(lv2);
            lv2.fillOpenings(start, holder, random);
         }

         return lv2;
      } else {
         return null;
      }
   }

   public static class MineshaftCrossing extends MineshaftPart {
      private final Direction direction;
      private final boolean twoFloors;

      public MineshaftCrossing(NbtCompound nbt) {
         super(StructurePieceType.MINESHAFT_CROSSING, nbt);
         this.twoFloors = nbt.getBoolean("tf");
         this.direction = Direction.fromHorizontal(nbt.getInt("D"));
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putBoolean("tf", this.twoFloors);
         nbt.putInt("D", this.direction.getHorizontal());
      }

      public MineshaftCrossing(int chainLength, BlockBox boundingBox, @Nullable Direction orientation, MineshaftStructure.Type type) {
         super(StructurePieceType.MINESHAFT_CROSSING, chainLength, type, boundingBox);
         this.direction = orientation;
         this.twoFloors = boundingBox.getBlockCountY() > 3;
      }

      @Nullable
      public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation) {
         byte l;
         if (random.nextInt(4) == 0) {
            l = 6;
         } else {
            l = 2;
         }

         BlockBox lv;
         switch (orientation) {
            case NORTH:
            default:
               lv = new BlockBox(-1, 0, -4, 3, l, 0);
               break;
            case SOUTH:
               lv = new BlockBox(-1, 0, 0, 3, l, 4);
               break;
            case WEST:
               lv = new BlockBox(-4, 0, -1, 0, l, 3);
               break;
            case EAST:
               lv = new BlockBox(0, 0, -1, 4, l, 3);
         }

         lv.move(x, y, z);
         return holder.getIntersecting(lv) != null ? null : lv;
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         int i = this.getChainLength();
         switch (this.direction) {
            case NORTH:
            default:
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.WEST, i);
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.EAST, i);
               break;
            case SOUTH:
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.WEST, i);
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.EAST, i);
               break;
            case WEST:
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.WEST, i);
               break;
            case EAST:
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, Direction.EAST, i);
         }

         if (this.twoFloors) {
            if (random.nextBoolean()) {
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY() + 3 + 1, this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
            }

            if (random.nextBoolean()) {
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + 3 + 1, this.boundingBox.getMinZ() + 1, Direction.WEST, i);
            }

            if (random.nextBoolean()) {
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + 3 + 1, this.boundingBox.getMinZ() + 1, Direction.EAST, i);
            }

            if (random.nextBoolean()) {
               MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY() + 3 + 1, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
            }
         }

      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         if (!this.cannotGenerate(world, chunkBox)) {
            BlockState lv = this.mineshaftType.getPlanks();
            if (this.twoFloors) {
               this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), this.boundingBox.getMaxX() - 1, this.boundingBox.getMinY() + 3 - 1, this.boundingBox.getMaxZ(), AIR, AIR, false);
               this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, this.boundingBox.getMaxX(), this.boundingBox.getMinY() + 3 - 1, this.boundingBox.getMaxZ() - 1, AIR, AIR, false);
               this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMaxY() - 2, this.boundingBox.getMinZ(), this.boundingBox.getMaxX() - 1, this.boundingBox.getMaxY(), this.boundingBox.getMaxZ(), AIR, AIR, false);
               this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMaxY() - 2, this.boundingBox.getMinZ() + 1, this.boundingBox.getMaxX(), this.boundingBox.getMaxY(), this.boundingBox.getMaxZ() - 1, AIR, AIR, false);
               this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY() + 3, this.boundingBox.getMinZ() + 1, this.boundingBox.getMaxX() - 1, this.boundingBox.getMinY() + 3, this.boundingBox.getMaxZ() - 1, AIR, AIR, false);
            } else {
               this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), this.boundingBox.getMaxX() - 1, this.boundingBox.getMaxY(), this.boundingBox.getMaxZ(), AIR, AIR, false);
               this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, this.boundingBox.getMaxX(), this.boundingBox.getMaxY(), this.boundingBox.getMaxZ() - 1, AIR, AIR, false);
            }

            this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, this.boundingBox.getMaxY());
            this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 1, this.boundingBox.getMaxY());
            this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1, this.boundingBox.getMaxY());
            this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 1, this.boundingBox.getMaxY());
            int i = this.boundingBox.getMinY() - 1;

            for(int j = this.boundingBox.getMinX(); j <= this.boundingBox.getMaxX(); ++j) {
               for(int k = this.boundingBox.getMinZ(); k <= this.boundingBox.getMaxZ(); ++k) {
                  this.tryPlaceFloor(world, chunkBox, lv, j, i, k);
               }
            }

         }
      }

      private void generateCrossingPillar(StructureWorldAccess world, BlockBox boundingBox, int x, int minY, int z, int maxY) {
         if (!this.getBlockAt(world, x, maxY + 1, z, boundingBox).isAir()) {
            this.fillWithOutline(world, boundingBox, x, minY, z, x, maxY, z, this.mineshaftType.getPlanks(), AIR, false);
         }

      }
   }

   public static class MineshaftStairs extends MineshaftPart {
      public MineshaftStairs(int chainLength, BlockBox boundingBox, Direction orientation, MineshaftStructure.Type type) {
         super(StructurePieceType.MINESHAFT_STAIRS, chainLength, type, boundingBox);
         this.setOrientation(orientation);
      }

      public MineshaftStairs(NbtCompound nbt) {
         super(StructurePieceType.MINESHAFT_STAIRS, nbt);
      }

      @Nullable
      public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation) {
         BlockBox lv;
         switch (orientation) {
            case NORTH:
            default:
               lv = new BlockBox(0, -5, -8, 2, 2, 0);
               break;
            case SOUTH:
               lv = new BlockBox(0, -5, 0, 2, 2, 8);
               break;
            case WEST:
               lv = new BlockBox(-8, -5, 0, 0, 2, 2);
               break;
            case EAST:
               lv = new BlockBox(0, -5, 0, 8, 2, 2);
         }

         lv.move(x, y, z);
         return holder.getIntersecting(lv) != null ? null : lv;
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         int i = this.getChainLength();
         Direction lv = this.getFacing();
         if (lv != null) {
            switch (lv) {
               case NORTH:
               default:
                  MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
                  break;
               case SOUTH:
                  MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
                  break;
               case WEST:
                  MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), Direction.WEST, i);
                  break;
               case EAST:
                  MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), this.boundingBox.getMinZ(), Direction.EAST, i);
            }
         }

      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         if (!this.cannotGenerate(world, chunkBox)) {
            this.fillWithOutline(world, chunkBox, 0, 5, 0, 2, 7, 1, AIR, AIR, false);
            this.fillWithOutline(world, chunkBox, 0, 0, 7, 2, 2, 8, AIR, AIR, false);

            for(int i = 0; i < 5; ++i) {
               this.fillWithOutline(world, chunkBox, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, AIR, AIR, false);
            }

         }
      }
   }

   public static class MineshaftCorridor extends MineshaftPart {
      private final boolean hasRails;
      private final boolean hasCobwebs;
      private boolean hasSpawner;
      private final int length;

      public MineshaftCorridor(NbtCompound nbt) {
         super(StructurePieceType.MINESHAFT_CORRIDOR, nbt);
         this.hasRails = nbt.getBoolean("hr");
         this.hasCobwebs = nbt.getBoolean("sc");
         this.hasSpawner = nbt.getBoolean("hps");
         this.length = nbt.getInt("Num");
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putBoolean("hr", this.hasRails);
         nbt.putBoolean("sc", this.hasCobwebs);
         nbt.putBoolean("hps", this.hasSpawner);
         nbt.putInt("Num", this.length);
      }

      public MineshaftCorridor(int chainLength, Random random, BlockBox boundingBox, Direction orientation, MineshaftStructure.Type type) {
         super(StructurePieceType.MINESHAFT_CORRIDOR, chainLength, type, boundingBox);
         this.setOrientation(orientation);
         this.hasRails = random.nextInt(3) == 0;
         this.hasCobwebs = !this.hasRails && random.nextInt(23) == 0;
         if (this.getFacing().getAxis() == Direction.Axis.Z) {
            this.length = boundingBox.getBlockCountZ() / 5;
         } else {
            this.length = boundingBox.getBlockCountX() / 5;
         }

      }

      @Nullable
      public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation) {
         for(int l = random.nextInt(3) + 2; l > 0; --l) {
            int m = l * 5;
            BlockBox lv;
            switch (orientation) {
               case NORTH:
               default:
                  lv = new BlockBox(0, 0, -(m - 1), 2, 2, 0);
                  break;
               case SOUTH:
                  lv = new BlockBox(0, 0, 0, 2, 2, m - 1);
                  break;
               case WEST:
                  lv = new BlockBox(-(m - 1), 0, 0, 0, 2, 2);
                  break;
               case EAST:
                  lv = new BlockBox(0, 0, 0, m - 1, 2, 2);
            }

            lv.move(x, y, z);
            if (holder.getIntersecting(lv) == null) {
               return lv;
            }
         }

         return null;
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         int i = this.getChainLength();
         int j = random.nextInt(4);
         Direction lv = this.getFacing();
         if (lv != null) {
            switch (lv) {
               case NORTH:
               default:
                  if (j <= 1) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMinZ() - 1, lv, i);
                  } else if (j == 2) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMinZ(), Direction.WEST, i);
                  } else {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMinZ(), Direction.EAST, i);
                  }
                  break;
               case SOUTH:
                  if (j <= 1) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMaxZ() + 1, lv, i);
                  } else if (j == 2) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMaxZ() - 3, Direction.WEST, i);
                  } else {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMaxZ() - 3, Direction.EAST, i);
                  }
                  break;
               case WEST:
                  if (j <= 1) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMinZ(), lv, i);
                  } else if (j == 2) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
                  } else {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX(), this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
                  }
                  break;
               case EAST:
                  if (j <= 1) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMinZ(), lv, i);
                  } else if (j == 2) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() - 3, this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
                  } else {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() - 3, this.boundingBox.getMinY() - 1 + random.nextInt(3), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
                  }
            }
         }

         if (i < 8) {
            int k;
            int l;
            if (lv != Direction.NORTH && lv != Direction.SOUTH) {
               for(k = this.boundingBox.getMinX() + 3; k + 3 <= this.boundingBox.getMaxX(); k += 5) {
                  l = random.nextInt(5);
                  if (l == 0) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, k, this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1, Direction.NORTH, i + 1);
                  } else if (l == 1) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, k, this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i + 1);
                  }
               }
            } else {
               for(k = this.boundingBox.getMinZ() + 3; k + 3 <= this.boundingBox.getMaxZ(); k += 5) {
                  l = random.nextInt(5);
                  if (l == 0) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(), k, Direction.WEST, i + 1);
                  } else if (l == 1) {
                     MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(), k, Direction.EAST, i + 1);
                  }
               }
            }
         }

      }

      protected boolean addChest(StructureWorldAccess world, BlockBox boundingBox, Random random, int x, int y, int z, Identifier lootTableId) {
         BlockPos lv = this.offsetPos(x, y, z);
         if (boundingBox.contains(lv) && world.getBlockState(lv).isAir() && !world.getBlockState(lv.down()).isAir()) {
            BlockState lv2 = (BlockState)Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, random.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
            this.addBlock(world, lv2, x, y, z, boundingBox);
            ChestMinecartEntity lv3 = new ChestMinecartEntity(world.toServerWorld(), (double)lv.getX() + 0.5, (double)lv.getY() + 0.5, (double)lv.getZ() + 0.5);
            lv3.setLootTable(lootTableId, random.nextLong());
            world.spawnEntity(lv3);
            return true;
         } else {
            return false;
         }
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         if (!this.cannotGenerate(world, chunkBox)) {
            int i = false;
            int j = true;
            int k = false;
            int l = true;
            int m = this.length * 5 - 1;
            BlockState lv = this.mineshaftType.getPlanks();
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 2, 1, m, AIR, AIR, false);
            this.fillWithOutlineUnderSeaLevel(world, chunkBox, random, 0.8F, 0, 2, 0, 2, 2, m, AIR, AIR, false, false);
            if (this.hasCobwebs) {
               this.fillWithOutlineUnderSeaLevel(world, chunkBox, random, 0.6F, 0, 0, 0, 2, 1, m, Blocks.COBWEB.getDefaultState(), AIR, false, true);
            }

            int n;
            int o;
            for(n = 0; n < this.length; ++n) {
               o = 2 + n * 5;
               this.generateSupports(world, chunkBox, 0, 0, o, 2, 2, random);
               this.addCobwebsUnderground(world, chunkBox, random, 0.1F, 0, 2, o - 1);
               this.addCobwebsUnderground(world, chunkBox, random, 0.1F, 2, 2, o - 1);
               this.addCobwebsUnderground(world, chunkBox, random, 0.1F, 0, 2, o + 1);
               this.addCobwebsUnderground(world, chunkBox, random, 0.1F, 2, 2, o + 1);
               this.addCobwebsUnderground(world, chunkBox, random, 0.05F, 0, 2, o - 2);
               this.addCobwebsUnderground(world, chunkBox, random, 0.05F, 2, 2, o - 2);
               this.addCobwebsUnderground(world, chunkBox, random, 0.05F, 0, 2, o + 2);
               this.addCobwebsUnderground(world, chunkBox, random, 0.05F, 2, 2, o + 2);
               if (random.nextInt(100) == 0) {
                  this.addChest(world, chunkBox, random, 2, 0, o - 1, LootTables.ABANDONED_MINESHAFT_CHEST);
               }

               if (random.nextInt(100) == 0) {
                  this.addChest(world, chunkBox, random, 0, 0, o + 1, LootTables.ABANDONED_MINESHAFT_CHEST);
               }

               if (this.hasCobwebs && !this.hasSpawner) {
                  int p = true;
                  int q = o - 1 + random.nextInt(3);
                  BlockPos lv2 = this.offsetPos(1, 0, q);
                  if (chunkBox.contains(lv2) && this.isUnderSeaLevel(world, 1, 0, q, chunkBox)) {
                     this.hasSpawner = true;
                     world.setBlockState(lv2, Blocks.SPAWNER.getDefaultState(), Block.NOTIFY_LISTENERS);
                     BlockEntity lv3 = world.getBlockEntity(lv2);
                     if (lv3 instanceof MobSpawnerBlockEntity) {
                        MobSpawnerBlockEntity lv4 = (MobSpawnerBlockEntity)lv3;
                        lv4.setEntityType(EntityType.CAVE_SPIDER, random);
                     }
                  }
               }
            }

            for(n = 0; n <= 2; ++n) {
               for(o = 0; o <= m; ++o) {
                  this.tryPlaceFloor(world, chunkBox, lv, n, -1, o);
               }
            }

            int n = true;
            this.fillSupportBeam(world, chunkBox, 0, -1, 2);
            if (this.length > 1) {
               o = m - 2;
               this.fillSupportBeam(world, chunkBox, 0, -1, o);
            }

            if (this.hasRails) {
               BlockState lv5 = (BlockState)Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.NORTH_SOUTH);

               for(int p = 0; p <= m; ++p) {
                  BlockState lv6 = this.getBlockAt(world, 1, -1, p, chunkBox);
                  if (!lv6.isAir() && lv6.isOpaqueFullCube(world, this.offsetPos(1, -1, p))) {
                     float f = this.isUnderSeaLevel(world, 1, 0, p, chunkBox) ? 0.7F : 0.9F;
                     this.addBlockWithRandomThreshold(world, chunkBox, random, f, 1, 0, p, lv5);
                  }
               }
            }

         }
      }

      private void fillSupportBeam(StructureWorldAccess world, BlockBox box, int x, int y, int z) {
         BlockState lv = this.mineshaftType.getLog();
         BlockState lv2 = this.mineshaftType.getPlanks();
         if (this.getBlockAt(world, x, y, z, box).isOf(lv2.getBlock())) {
            this.fillSupportBeam(world, lv, x, y, z, box);
         }

         if (this.getBlockAt(world, x + 2, y, z, box).isOf(lv2.getBlock())) {
            this.fillSupportBeam(world, lv, x + 2, y, z, box);
         }

      }

      protected void fillDownwards(StructureWorldAccess world, BlockState state, int x, int y, int z, BlockBox box) {
         BlockPos.Mutable lv = this.offsetPos(x, y, z);
         if (box.contains(lv)) {
            int l = lv.getY();

            while(this.canReplace(world.getBlockState(lv)) && lv.getY() > world.getBottomY() + 1) {
               lv.move(Direction.DOWN);
            }

            if (this.isUpsideSolidFullSquare(world, lv, world.getBlockState(lv))) {
               while(lv.getY() < l) {
                  lv.move(Direction.UP);
                  world.setBlockState(lv, state, Block.NOTIFY_LISTENERS);
               }

            }
         }
      }

      protected void fillSupportBeam(StructureWorldAccess world, BlockState state, int x, int y, int z, BlockBox box) {
         BlockPos.Mutable lv = this.offsetPos(x, y, z);
         if (box.contains(lv)) {
            int l = lv.getY();
            int m = 1;
            boolean bl = true;

            for(boolean bl2 = true; bl || bl2; ++m) {
               BlockState lv2;
               boolean bl3;
               if (bl) {
                  lv.setY(l - m);
                  lv2 = world.getBlockState(lv);
                  bl3 = this.canReplace(lv2) && !lv2.isOf(Blocks.LAVA);
                  if (!bl3 && this.isUpsideSolidFullSquare(world, lv, lv2)) {
                     fillColumn(world, state, lv, l - m + 1, l);
                     return;
                  }

                  bl = m <= 20 && bl3 && lv.getY() > world.getBottomY() + 1;
               }

               if (bl2) {
                  lv.setY(l + m);
                  lv2 = world.getBlockState(lv);
                  bl3 = this.canReplace(lv2);
                  if (!bl3 && this.sideCoversSmallSquare(world, lv, lv2)) {
                     world.setBlockState(lv.setY(l + 1), this.mineshaftType.getFence(), Block.NOTIFY_LISTENERS);
                     fillColumn(world, Blocks.CHAIN.getDefaultState(), lv, l + 2, l + m);
                     return;
                  }

                  bl2 = m <= 50 && bl3 && lv.getY() < world.getTopY() - 1;
               }
            }

         }
      }

      private static void fillColumn(StructureWorldAccess world, BlockState state, BlockPos.Mutable pos, int startY, int endY) {
         for(int k = startY; k < endY; ++k) {
            world.setBlockState(pos.setY(k), state, Block.NOTIFY_LISTENERS);
         }

      }

      private boolean isUpsideSolidFullSquare(WorldView world, BlockPos pos, BlockState state) {
         return state.isSideSolidFullSquare(world, pos, Direction.UP);
      }

      private boolean sideCoversSmallSquare(WorldView world, BlockPos pos, BlockState state) {
         return Block.sideCoversSmallSquare(world, pos, Direction.DOWN) && !(state.getBlock() instanceof FallingBlock);
      }

      private void generateSupports(StructureWorldAccess world, BlockBox boundingBox, int minX, int minY, int z, int maxY, int maxX, Random random) {
         if (this.isSolidCeiling(world, boundingBox, minX, maxX, maxY, z)) {
            BlockState lv = this.mineshaftType.getPlanks();
            BlockState lv2 = this.mineshaftType.getFence();
            this.fillWithOutline(world, boundingBox, minX, minY, z, minX, maxY - 1, z, (BlockState)lv2.with(FenceBlock.WEST, true), AIR, false);
            this.fillWithOutline(world, boundingBox, maxX, minY, z, maxX, maxY - 1, z, (BlockState)lv2.with(FenceBlock.EAST, true), AIR, false);
            if (random.nextInt(4) == 0) {
               this.fillWithOutline(world, boundingBox, minX, maxY, z, minX, maxY, z, lv, AIR, false);
               this.fillWithOutline(world, boundingBox, maxX, maxY, z, maxX, maxY, z, lv, AIR, false);
            } else {
               this.fillWithOutline(world, boundingBox, minX, maxY, z, maxX, maxY, z, lv, AIR, false);
               this.addBlockWithRandomThreshold(world, boundingBox, random, 0.05F, minX + 1, maxY, z - 1, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH));
               this.addBlockWithRandomThreshold(world, boundingBox, random, 0.05F, minX + 1, maxY, z + 1, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.NORTH));
            }

         }
      }

      private void addCobwebsUnderground(StructureWorldAccess world, BlockBox box, Random random, float threshold, int x, int y, int z) {
         if (this.isUnderSeaLevel(world, x, y, z, box) && random.nextFloat() < threshold && this.hasSolidNeighborBlocks(world, box, x, y, z, 2)) {
            this.addBlock(world, Blocks.COBWEB.getDefaultState(), x, y, z, box);
         }

      }

      private boolean hasSolidNeighborBlocks(StructureWorldAccess world, BlockBox box, int x, int y, int z, int count) {
         BlockPos.Mutable lv = this.offsetPos(x, y, z);
         int m = 0;
         Direction[] var9 = Direction.values();
         int var10 = var9.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            Direction lv2 = var9[var11];
            lv.move(lv2);
            if (box.contains(lv) && world.getBlockState(lv).isSideSolidFullSquare(world, lv, lv2.getOpposite())) {
               ++m;
               if (m >= count) {
                  return true;
               }
            }

            lv.move(lv2.getOpposite());
         }

         return false;
      }
   }

   abstract static class MineshaftPart extends StructurePiece {
      protected MineshaftStructure.Type mineshaftType;

      public MineshaftPart(StructurePieceType structurePieceType, int chainLength, MineshaftStructure.Type type, BlockBox box) {
         super(structurePieceType, chainLength, box);
         this.mineshaftType = type;
      }

      public MineshaftPart(StructurePieceType arg, NbtCompound arg2) {
         super(arg, arg2);
         this.mineshaftType = MineshaftStructure.Type.byId(arg2.getInt("MST"));
      }

      protected boolean canAddBlock(WorldView world, int x, int y, int z, BlockBox box) {
         BlockState lv = this.getBlockAt(world, x, y, z, box);
         return !lv.isOf(this.mineshaftType.getPlanks().getBlock()) && !lv.isOf(this.mineshaftType.getLog().getBlock()) && !lv.isOf(this.mineshaftType.getFence().getBlock()) && !lv.isOf(Blocks.CHAIN);
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         nbt.putInt("MST", this.mineshaftType.ordinal());
      }

      protected boolean isSolidCeiling(BlockView world, BlockBox boundingBox, int minX, int maxX, int y, int z) {
         for(int m = minX; m <= maxX; ++m) {
            if (this.getBlockAt(world, m, y + 1, z, boundingBox).isAir()) {
               return false;
            }
         }

         return true;
      }

      protected boolean cannotGenerate(WorldAccess world, BlockBox box) {
         int i = Math.max(this.boundingBox.getMinX() - 1, box.getMinX());
         int j = Math.max(this.boundingBox.getMinY() - 1, box.getMinY());
         int k = Math.max(this.boundingBox.getMinZ() - 1, box.getMinZ());
         int l = Math.min(this.boundingBox.getMaxX() + 1, box.getMaxX());
         int m = Math.min(this.boundingBox.getMaxY() + 1, box.getMaxY());
         int n = Math.min(this.boundingBox.getMaxZ() + 1, box.getMaxZ());
         BlockPos.Mutable lv = new BlockPos.Mutable((i + l) / 2, (j + m) / 2, (k + n) / 2);
         if (world.getBiome(lv).isIn(BiomeTags.MINESHAFT_BLOCKING)) {
            return true;
         } else {
            int o;
            int p;
            for(o = i; o <= l; ++o) {
               for(p = k; p <= n; ++p) {
                  if (world.getBlockState(lv.set(o, j, p)).isLiquid()) {
                     return true;
                  }

                  if (world.getBlockState(lv.set(o, m, p)).isLiquid()) {
                     return true;
                  }
               }
            }

            for(o = i; o <= l; ++o) {
               for(p = j; p <= m; ++p) {
                  if (world.getBlockState(lv.set(o, p, k)).isLiquid()) {
                     return true;
                  }

                  if (world.getBlockState(lv.set(o, p, n)).isLiquid()) {
                     return true;
                  }
               }
            }

            for(o = k; o <= n; ++o) {
               for(p = j; p <= m; ++p) {
                  if (world.getBlockState(lv.set(i, p, o)).isLiquid()) {
                     return true;
                  }

                  if (world.getBlockState(lv.set(l, p, o)).isLiquid()) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      protected void tryPlaceFloor(StructureWorldAccess world, BlockBox box, BlockState state, int x, int y, int z) {
         if (this.isUnderSeaLevel(world, x, y, z, box)) {
            BlockPos lv = this.offsetPos(x, y, z);
            BlockState lv2 = world.getBlockState(lv);
            if (!lv2.isSideSolidFullSquare(world, lv, Direction.UP)) {
               world.setBlockState(lv, state, Block.NOTIFY_LISTENERS);
            }

         }
      }
   }

   public static class MineshaftRoom extends MineshaftPart {
      private final List entrances = Lists.newLinkedList();

      public MineshaftRoom(int chainLength, Random random, int x, int z, MineshaftStructure.Type type) {
         super(StructurePieceType.MINESHAFT_ROOM, chainLength, type, new BlockBox(x, 50, z, x + 7 + random.nextInt(6), 54 + random.nextInt(6), z + 7 + random.nextInt(6)));
         this.mineshaftType = type;
      }

      public MineshaftRoom(NbtCompound nbt) {
         super(StructurePieceType.MINESHAFT_ROOM, nbt);
         DataResult var10000 = BlockBox.CODEC.listOf().parse(NbtOps.INSTANCE, nbt.getList("Entrances", NbtElement.INT_ARRAY_TYPE));
         Logger var10001 = MineshaftGenerator.LOGGER;
         Objects.requireNonNull(var10001);
         Optional var2 = var10000.resultOrPartial(var10001::error);
         List var3 = this.entrances;
         Objects.requireNonNull(var3);
         var2.ifPresent(var3::addAll);
      }

      public void fillOpenings(StructurePiece start, StructurePiecesHolder holder, Random random) {
         int i = this.getChainLength();
         int j = this.boundingBox.getBlockCountY() - 3 - 1;
         if (j <= 0) {
            j = 1;
         }

         int k;
         MineshaftPart lv3;
         BlockBox lv2;
         for(k = 0; k < this.boundingBox.getBlockCountX(); k += 4) {
            k += random.nextInt(this.boundingBox.getBlockCountX());
            if (k + 3 > this.boundingBox.getBlockCountX()) {
               break;
            }

            lv3 = MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + k, this.boundingBox.getMinY() + random.nextInt(j) + 1, this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
            if (lv3 != null) {
               lv2 = lv3.getBoundingBox();
               this.entrances.add(new BlockBox(lv2.getMinX(), lv2.getMinY(), this.boundingBox.getMinZ(), lv2.getMaxX(), lv2.getMaxY(), this.boundingBox.getMinZ() + 1));
            }
         }

         for(k = 0; k < this.boundingBox.getBlockCountX(); k += 4) {
            k += random.nextInt(this.boundingBox.getBlockCountX());
            if (k + 3 > this.boundingBox.getBlockCountX()) {
               break;
            }

            lv3 = MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() + k, this.boundingBox.getMinY() + random.nextInt(j) + 1, this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
            if (lv3 != null) {
               lv2 = lv3.getBoundingBox();
               this.entrances.add(new BlockBox(lv2.getMinX(), lv2.getMinY(), this.boundingBox.getMaxZ() - 1, lv2.getMaxX(), lv2.getMaxY(), this.boundingBox.getMaxZ()));
            }
         }

         for(k = 0; k < this.boundingBox.getBlockCountZ(); k += 4) {
            k += random.nextInt(this.boundingBox.getBlockCountZ());
            if (k + 3 > this.boundingBox.getBlockCountZ()) {
               break;
            }

            lv3 = MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + random.nextInt(j) + 1, this.boundingBox.getMinZ() + k, Direction.WEST, i);
            if (lv3 != null) {
               lv2 = lv3.getBoundingBox();
               this.entrances.add(new BlockBox(this.boundingBox.getMinX(), lv2.getMinY(), lv2.getMinZ(), this.boundingBox.getMinX() + 1, lv2.getMaxY(), lv2.getMaxZ()));
            }
         }

         for(k = 0; k < this.boundingBox.getBlockCountZ(); k += 4) {
            k += random.nextInt(this.boundingBox.getBlockCountZ());
            if (k + 3 > this.boundingBox.getBlockCountZ()) {
               break;
            }

            lv3 = MineshaftGenerator.pieceGenerator(start, holder, random, this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + random.nextInt(j) + 1, this.boundingBox.getMinZ() + k, Direction.EAST, i);
            if (lv3 != null) {
               lv2 = lv3.getBoundingBox();
               this.entrances.add(new BlockBox(this.boundingBox.getMaxX() - 1, lv2.getMinY(), lv2.getMinZ(), this.boundingBox.getMaxX(), lv2.getMaxY(), lv2.getMaxZ()));
            }
         }

      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         if (!this.cannotGenerate(world, chunkBox)) {
            this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMinY() + 1, this.boundingBox.getMinZ(), this.boundingBox.getMaxX(), Math.min(this.boundingBox.getMinY() + 3, this.boundingBox.getMaxY()), this.boundingBox.getMaxZ(), AIR, AIR, false);
            Iterator var8 = this.entrances.iterator();

            while(var8.hasNext()) {
               BlockBox lv = (BlockBox)var8.next();
               this.fillWithOutline(world, chunkBox, lv.getMinX(), lv.getMaxY() - 2, lv.getMinZ(), lv.getMaxX(), lv.getMaxY(), lv.getMaxZ(), AIR, AIR, false);
            }

            this.fillHalfEllipsoid(world, chunkBox, this.boundingBox.getMinX(), this.boundingBox.getMinY() + 4, this.boundingBox.getMinZ(), this.boundingBox.getMaxX(), this.boundingBox.getMaxY(), this.boundingBox.getMaxZ(), AIR, false);
         }
      }

      public void translate(int x, int y, int z) {
         super.translate(x, y, z);
         Iterator var4 = this.entrances.iterator();

         while(var4.hasNext()) {
            BlockBox lv = (BlockBox)var4.next();
            lv.move(x, y, z);
         }

      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         DataResult var10000 = BlockBox.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.entrances);
         Logger var10001 = MineshaftGenerator.LOGGER;
         Objects.requireNonNull(var10001);
         var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
            nbt.put("Entrances", arg2);
         });
      }
   }
}
