package net.minecraft.structure;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class BuriedTreasureGenerator {
   public static class Piece extends StructurePiece {
      public Piece(BlockPos pos) {
         super(StructurePieceType.BURIED_TREASURE, 0, new BlockBox(pos));
      }

      public Piece(NbtCompound nbt) {
         super(StructurePieceType.BURIED_TREASURE, nbt);
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         int i = world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, this.boundingBox.getMinX(), this.boundingBox.getMinZ());
         BlockPos.Mutable lv = new BlockPos.Mutable(this.boundingBox.getMinX(), i, this.boundingBox.getMinZ());

         while(lv.getY() > world.getBottomY()) {
            BlockState lv2 = world.getBlockState(lv);
            BlockState lv3 = world.getBlockState(lv.down());
            if (lv3 == Blocks.SANDSTONE.getDefaultState() || lv3 == Blocks.STONE.getDefaultState() || lv3 == Blocks.ANDESITE.getDefaultState() || lv3 == Blocks.GRANITE.getDefaultState() || lv3 == Blocks.DIORITE.getDefaultState()) {
               BlockState lv4 = !lv2.isAir() && !this.isLiquid(lv2) ? lv2 : Blocks.SAND.getDefaultState();
               Direction[] var13 = Direction.values();
               int var14 = var13.length;

               for(int var15 = 0; var15 < var14; ++var15) {
                  Direction lv5 = var13[var15];
                  BlockPos lv6 = lv.offset(lv5);
                  BlockState lv7 = world.getBlockState(lv6);
                  if (lv7.isAir() || this.isLiquid(lv7)) {
                     BlockPos lv8 = lv6.down();
                     BlockState lv9 = world.getBlockState(lv8);
                     if ((lv9.isAir() || this.isLiquid(lv9)) && lv5 != Direction.UP) {
                        world.setBlockState(lv6, lv3, Block.NOTIFY_ALL);
                     } else {
                        world.setBlockState(lv6, lv4, Block.NOTIFY_ALL);
                     }
                  }
               }

               this.boundingBox = new BlockBox(lv);
               this.addChest(world, chunkBox, random, lv, LootTables.BURIED_TREASURE_CHEST, (BlockState)null);
               return;
            }

            lv.move(0, -1, 0);
         }

      }

      private boolean isLiquid(BlockState state) {
         return state == Blocks.WATER.getDefaultState() || state == Blocks.LAVA.getDefaultState();
      }
   }
}
