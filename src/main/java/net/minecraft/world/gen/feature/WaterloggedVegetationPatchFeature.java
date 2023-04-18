package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class WaterloggedVegetationPatchFeature extends VegetationPatchFeature {
   public WaterloggedVegetationPatchFeature(Codec codec) {
      super(codec);
   }

   protected Set placeGroundAndGetPositions(StructureWorldAccess world, VegetationPatchFeatureConfig config, Random random, BlockPos pos, Predicate replaceable, int radiusX, int radiusZ) {
      Set set = super.placeGroundAndGetPositions(world, config, random, pos, replaceable, radiusX, radiusZ);
      Set set2 = new HashSet();
      BlockPos.Mutable lv = new BlockPos.Mutable();
      Iterator var11 = set.iterator();

      BlockPos lv2;
      while(var11.hasNext()) {
         lv2 = (BlockPos)var11.next();
         if (!isSolidBlockAroundPos(world, set, lv2, lv)) {
            set2.add(lv2);
         }
      }

      var11 = set2.iterator();

      while(var11.hasNext()) {
         lv2 = (BlockPos)var11.next();
         world.setBlockState(lv2, Blocks.WATER.getDefaultState(), Block.NOTIFY_LISTENERS);
      }

      return set2;
   }

   private static boolean isSolidBlockAroundPos(StructureWorldAccess world, Set positions, BlockPos pos, BlockPos.Mutable mutablePos) {
      return isSolidBlockSide(world, pos, mutablePos, Direction.NORTH) || isSolidBlockSide(world, pos, mutablePos, Direction.EAST) || isSolidBlockSide(world, pos, mutablePos, Direction.SOUTH) || isSolidBlockSide(world, pos, mutablePos, Direction.WEST) || isSolidBlockSide(world, pos, mutablePos, Direction.DOWN);
   }

   private static boolean isSolidBlockSide(StructureWorldAccess world, BlockPos pos, BlockPos.Mutable mutablePos, Direction direction) {
      mutablePos.set(pos, (Direction)direction);
      return !world.getBlockState(mutablePos).isSideSolidFullSquare(world, mutablePos, direction.getOpposite());
   }

   protected boolean generateVegetationFeature(StructureWorldAccess world, VegetationPatchFeatureConfig config, ChunkGenerator generator, Random random, BlockPos pos) {
      if (super.generateVegetationFeature(world, config, generator, random, pos.down())) {
         BlockState lv = world.getBlockState(pos);
         if (lv.contains(Properties.WATERLOGGED) && !(Boolean)lv.get(Properties.WATERLOGGED)) {
            world.setBlockState(pos, (BlockState)lv.with(Properties.WATERLOGGED, true), Block.NOTIFY_LISTENERS);
         }

         return true;
      } else {
         return false;
      }
   }
}
