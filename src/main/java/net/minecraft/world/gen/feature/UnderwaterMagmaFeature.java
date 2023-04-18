package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.CaveSurface;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class UnderwaterMagmaFeature extends Feature {
   public UnderwaterMagmaFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      UnderwaterMagmaFeatureConfig lv3 = (UnderwaterMagmaFeatureConfig)context.getConfig();
      Random lv4 = context.getRandom();
      OptionalInt optionalInt = getFloorHeight(lv, lv2, lv3);
      if (!optionalInt.isPresent()) {
         return false;
      } else {
         BlockPos lv5 = lv2.withY(optionalInt.getAsInt());
         Vec3i lv6 = new Vec3i(lv3.placementRadiusAroundFloor, lv3.placementRadiusAroundFloor, lv3.placementRadiusAroundFloor);
         Box lv7 = new Box(lv5.subtract(lv6), lv5.add(lv6));
         return BlockPos.stream(lv7).filter((pos) -> {
            return lv4.nextFloat() < lv3.placementProbabilityPerValidPosition;
         }).filter((pos) -> {
            return this.isValidPosition(lv, pos);
         }).mapToInt((pos) -> {
            lv.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
            return 1;
         }).sum() > 0;
      }
   }

   private static OptionalInt getFloorHeight(StructureWorldAccess world, BlockPos pos, UnderwaterMagmaFeatureConfig config) {
      Predicate predicate = (state) -> {
         return state.isOf(Blocks.WATER);
      };
      Predicate predicate2 = (state) -> {
         return !state.isOf(Blocks.WATER);
      };
      Optional optional = CaveSurface.create(world, pos, config.floorSearchRange, predicate, predicate2);
      return (OptionalInt)optional.map(CaveSurface::getFloorHeight).orElseGet(OptionalInt::empty);
   }

   private boolean isValidPosition(StructureWorldAccess world, BlockPos pos) {
      if (!this.isWaterOrAir(world, pos) && !this.isWaterOrAir(world, pos.down())) {
         Iterator var3 = Direction.Type.HORIZONTAL.iterator();

         Direction lv;
         do {
            if (!var3.hasNext()) {
               return true;
            }

            lv = (Direction)var3.next();
         } while(!this.isWaterOrAir(world, pos.offset(lv)));

         return false;
      } else {
         return false;
      }
   }

   private boolean isWaterOrAir(WorldAccess world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      return lv.isOf(Blocks.WATER) || lv.isAir();
   }
}
