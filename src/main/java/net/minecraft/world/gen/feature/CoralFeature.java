package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public abstract class CoralFeature extends Feature {
   public CoralFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      Random lv = context.getRandom();
      StructureWorldAccess lv2 = context.getWorld();
      BlockPos lv3 = context.getOrigin();
      Optional optional = Registries.BLOCK.getEntryList(BlockTags.CORAL_BLOCKS).flatMap((blocks) -> {
         return blocks.getRandom(lv);
      }).map(RegistryEntry::value);
      return optional.isEmpty() ? false : this.generateCoral(lv2, lv, lv3, ((Block)optional.get()).getDefaultState());
   }

   protected abstract boolean generateCoral(WorldAccess world, Random random, BlockPos pos, BlockState state);

   protected boolean generateCoralPiece(WorldAccess world, Random random, BlockPos pos, BlockState state) {
      BlockPos lv = pos.up();
      BlockState lv2 = world.getBlockState(pos);
      if ((lv2.isOf(Blocks.WATER) || lv2.isIn(BlockTags.CORALS)) && world.getBlockState(lv).isOf(Blocks.WATER)) {
         world.setBlockState(pos, state, Block.NOTIFY_ALL);
         if (random.nextFloat() < 0.25F) {
            Registries.BLOCK.getEntryList(BlockTags.CORALS).flatMap((blocks) -> {
               return blocks.getRandom(random);
            }).map(RegistryEntry::value).ifPresent((block) -> {
               world.setBlockState(lv, block.getDefaultState(), Block.NOTIFY_LISTENERS);
            });
         } else if (random.nextFloat() < 0.05F) {
            world.setBlockState(lv, (BlockState)Blocks.SEA_PICKLE.getDefaultState().with(SeaPickleBlock.PICKLES, random.nextInt(4) + 1), Block.NOTIFY_LISTENERS);
         }

         Iterator var7 = Direction.Type.HORIZONTAL.iterator();

         while(var7.hasNext()) {
            Direction lv3 = (Direction)var7.next();
            if (random.nextFloat() < 0.2F) {
               BlockPos lv4 = pos.offset(lv3);
               if (world.getBlockState(lv4).isOf(Blocks.WATER)) {
                  Registries.BLOCK.getEntryList(BlockTags.WALL_CORALS).flatMap((blocks) -> {
                     return blocks.getRandom(random);
                  }).map(RegistryEntry::value).ifPresent((block) -> {
                     BlockState lv = block.getDefaultState();
                     if (lv.contains(DeadCoralWallFanBlock.FACING)) {
                        lv = (BlockState)lv.with(DeadCoralWallFanBlock.FACING, lv3);
                     }

                     world.setBlockState(lv4, lv, Block.NOTIFY_LISTENERS);
                  });
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
