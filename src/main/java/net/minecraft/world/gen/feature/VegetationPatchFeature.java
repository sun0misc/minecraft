package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class VegetationPatchFeature extends Feature {
   public VegetationPatchFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      VegetationPatchFeatureConfig lv2 = (VegetationPatchFeatureConfig)context.getConfig();
      Random lv3 = context.getRandom();
      BlockPos lv4 = context.getOrigin();
      Predicate predicate = (state) -> {
         return state.isIn(lv2.replaceable);
      };
      int i = lv2.horizontalRadius.get(lv3) + 1;
      int j = lv2.horizontalRadius.get(lv3) + 1;
      Set set = this.placeGroundAndGetPositions(lv, lv2, lv3, lv4, predicate, i, j);
      this.generateVegetation(context, lv, lv2, lv3, set, i, j);
      return !set.isEmpty();
   }

   protected Set placeGroundAndGetPositions(StructureWorldAccess world, VegetationPatchFeatureConfig config, Random random, BlockPos pos, Predicate replaceable, int radiusX, int radiusZ) {
      BlockPos.Mutable lv = pos.mutableCopy();
      BlockPos.Mutable lv2 = lv.mutableCopy();
      Direction lv3 = config.surface.getDirection();
      Direction lv4 = lv3.getOpposite();
      Set set = new HashSet();

      for(int k = -radiusX; k <= radiusX; ++k) {
         boolean bl = k == -radiusX || k == radiusX;

         for(int l = -radiusZ; l <= radiusZ; ++l) {
            boolean bl2 = l == -radiusZ || l == radiusZ;
            boolean bl3 = bl || bl2;
            boolean bl4 = bl && bl2;
            boolean bl5 = bl3 && !bl4;
            if (!bl4 && (!bl5 || config.extraEdgeColumnChance != 0.0F && !(random.nextFloat() > config.extraEdgeColumnChance))) {
               lv.set((Vec3i)pos, k, 0, l);

               int m;
               for(m = 0; world.testBlockState(lv, AbstractBlock.AbstractBlockState::isAir) && m < config.verticalRange; ++m) {
                  lv.move(lv3);
               }

               for(m = 0; world.testBlockState(lv, (state) -> {
                  return !state.isAir();
               }) && m < config.verticalRange; ++m) {
                  lv.move(lv4);
               }

               lv2.set(lv, (Direction)config.surface.getDirection());
               BlockState lv5 = world.getBlockState(lv2);
               if (world.isAir(lv) && lv5.isSideSolidFullSquare(world, lv2, config.surface.getDirection().getOpposite())) {
                  int n = config.depth.get(random) + (config.extraBottomBlockChance > 0.0F && random.nextFloat() < config.extraBottomBlockChance ? 1 : 0);
                  BlockPos lv6 = lv2.toImmutable();
                  boolean bl6 = this.placeGround(world, config, replaceable, random, lv2, n);
                  if (bl6) {
                     set.add(lv6);
                  }
               }
            }
         }
      }

      return set;
   }

   protected void generateVegetation(FeatureContext context, StructureWorldAccess world, VegetationPatchFeatureConfig config, Random arg4, Set positions, int radiusX, int radiusZ) {
      Iterator var8 = positions.iterator();

      while(var8.hasNext()) {
         BlockPos lv = (BlockPos)var8.next();
         if (config.vegetationChance > 0.0F && arg4.nextFloat() < config.vegetationChance) {
            this.generateVegetationFeature(world, config, context.getGenerator(), arg4, lv);
         }
      }

   }

   protected boolean generateVegetationFeature(StructureWorldAccess world, VegetationPatchFeatureConfig config, ChunkGenerator generator, Random random, BlockPos pos) {
      return ((PlacedFeature)config.vegetationFeature.value()).generateUnregistered(world, generator, random, pos.offset(config.surface.getDirection().getOpposite()));
   }

   protected boolean placeGround(StructureWorldAccess world, VegetationPatchFeatureConfig config, Predicate replaceable, Random arg3, BlockPos.Mutable pos, int depth) {
      for(int j = 0; j < depth; ++j) {
         BlockState lv = config.groundState.get(arg3, pos);
         BlockState lv2 = world.getBlockState(pos);
         if (!lv.isOf(lv2.getBlock())) {
            if (!replaceable.test(lv2)) {
               return j != 0;
            }

            world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
            pos.move(config.surface.getDirection());
         }
      }

      return true;
   }
}
