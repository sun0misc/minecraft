package net.minecraft.structure.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class GravityStructureProcessor extends StructureProcessor {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Heightmap.Type.CODEC.fieldOf("heightmap").orElse(Heightmap.Type.WORLD_SURFACE_WG).forGetter((processor) -> {
         return processor.heightmap;
      }), Codec.INT.fieldOf("offset").orElse(0).forGetter((processor) -> {
         return processor.offset;
      })).apply(instance, GravityStructureProcessor::new);
   });
   private final Heightmap.Type heightmap;
   private final int offset;

   public GravityStructureProcessor(Heightmap.Type heightmap, int offset) {
      this.heightmap = heightmap;
      this.offset = offset;
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
      Heightmap.Type lv;
      if (world instanceof ServerWorld) {
         if (this.heightmap == Heightmap.Type.WORLD_SURFACE_WG) {
            lv = Heightmap.Type.WORLD_SURFACE;
         } else if (this.heightmap == Heightmap.Type.OCEAN_FLOOR_WG) {
            lv = Heightmap.Type.OCEAN_FLOOR;
         } else {
            lv = this.heightmap;
         }
      } else {
         lv = this.heightmap;
      }

      BlockPos lv2 = currentBlockInfo.pos();
      int i = world.getTopY(lv, lv2.getX(), lv2.getZ()) + this.offset;
      int j = originalBlockInfo.pos().getY();
      return new StructureTemplate.StructureBlockInfo(new BlockPos(lv2.getX(), i + j, lv2.getZ()), currentBlockInfo.state(), currentBlockInfo.nbt());
   }

   protected StructureProcessorType getType() {
      return StructureProcessorType.GRAVITY;
   }
}
