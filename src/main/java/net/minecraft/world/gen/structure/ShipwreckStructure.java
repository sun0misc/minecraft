package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.structure.ShipwreckGenerator;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class ShipwreckStructure extends Structure {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(configCodecBuilder(instance), Codec.BOOL.fieldOf("is_beached").forGetter((arg) -> {
         return arg.beached;
      })).apply(instance, ShipwreckStructure::new);
   });
   public final boolean beached;

   public ShipwreckStructure(Structure.Config config, boolean beached) {
      super(config);
      this.beached = beached;
   }

   public Optional getStructurePosition(Structure.Context context) {
      Heightmap.Type lv = this.beached ? Heightmap.Type.WORLD_SURFACE_WG : Heightmap.Type.OCEAN_FLOOR_WG;
      return getStructurePosition(context, lv, (collector) -> {
         this.addPieces(collector, context);
      });
   }

   private void addPieces(StructurePiecesCollector collector, Structure.Context context) {
      BlockRotation lv = BlockRotation.random(context.random());
      BlockPos lv2 = new BlockPos(context.chunkPos().getStartX(), 90, context.chunkPos().getStartZ());
      ShipwreckGenerator.addParts(context.structureTemplateManager(), lv2, lv, collector, context.random(), this.beached);
   }

   public StructureType getType() {
      return StructureType.SHIPWRECK;
   }
}
