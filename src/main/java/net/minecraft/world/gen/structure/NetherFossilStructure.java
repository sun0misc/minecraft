package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.NetherFossilGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.heightprovider.HeightProvider;

public class NetherFossilStructure extends Structure {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(configCodecBuilder(instance), HeightProvider.CODEC.fieldOf("height").forGetter((structure) -> {
         return structure.height;
      })).apply(instance, NetherFossilStructure::new);
   });
   public final HeightProvider height;

   public NetherFossilStructure(Structure.Config config, HeightProvider height) {
      super(config);
      this.height = height;
   }

   public Optional getStructurePosition(Structure.Context context) {
      ChunkRandom lv = context.random();
      int i = context.chunkPos().getStartX() + lv.nextInt(16);
      int j = context.chunkPos().getStartZ() + lv.nextInt(16);
      int k = context.chunkGenerator().getSeaLevel();
      HeightContext lv2 = new HeightContext(context.chunkGenerator(), context.world());
      int l = this.height.get(lv, lv2);
      VerticalBlockSample lv3 = context.chunkGenerator().getColumnSample(i, j, context.world(), context.noiseConfig());
      BlockPos.Mutable lv4 = new BlockPos.Mutable(i, l, j);

      while(l > k) {
         BlockState lv5 = lv3.getState(l);
         --l;
         BlockState lv6 = lv3.getState(l);
         if (lv5.isAir() && (lv6.isOf(Blocks.SOUL_SAND) || lv6.isSideSolidFullSquare(EmptyBlockView.INSTANCE, lv4.setY(l), Direction.UP))) {
            break;
         }
      }

      if (l <= k) {
         return Optional.empty();
      } else {
         BlockPos lv7 = new BlockPos(i, l, j);
         return Optional.of(new Structure.StructurePosition(lv7, (arg4) -> {
            NetherFossilGenerator.addPieces(context.structureTemplateManager(), arg4, lv, lv7);
         }));
      }
   }

   public StructureType getType() {
      return StructureType.NETHER_FOSSIL;
   }
}
