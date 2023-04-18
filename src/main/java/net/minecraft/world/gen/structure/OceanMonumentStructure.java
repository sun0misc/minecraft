package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.OceanMonumentGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.Heightmap;

public class OceanMonumentStructure extends Structure {
   public static final Codec CODEC = createCodec(OceanMonumentStructure::new);

   public OceanMonumentStructure(Structure.Config arg) {
      super(arg);
   }

   public Optional getStructurePosition(Structure.Context context) {
      int i = context.chunkPos().getOffsetX(9);
      int j = context.chunkPos().getOffsetZ(9);
      Set set = context.biomeSource().getBiomesInArea(i, context.chunkGenerator().getSeaLevel(), j, 29, context.noiseConfig().getMultiNoiseSampler());
      Iterator var5 = set.iterator();

      RegistryEntry lv;
      do {
         if (!var5.hasNext()) {
            return getStructurePosition(context, Heightmap.Type.OCEAN_FLOOR_WG, (collector) -> {
               addPieces(collector, context);
            });
         }

         lv = (RegistryEntry)var5.next();
      } while(lv.isIn(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING));

      return Optional.empty();
   }

   private static StructurePiece createBasePiece(ChunkPos pos, ChunkRandom random) {
      int i = pos.getStartX() - 29;
      int j = pos.getStartZ() - 29;
      Direction lv = Direction.Type.HORIZONTAL.random(random);
      return new OceanMonumentGenerator.Base(random, i, j, lv);
   }

   private static void addPieces(StructurePiecesCollector collector, Structure.Context context) {
      collector.addPiece(createBasePiece(context.chunkPos(), context.random()));
   }

   public static StructurePiecesList modifyPiecesOnRead(ChunkPos pos, long worldSeed, StructurePiecesList pieces) {
      if (pieces.isEmpty()) {
         return pieces;
      } else {
         ChunkRandom lv = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
         lv.setCarverSeed(worldSeed, pos.x, pos.z);
         StructurePiece lv2 = (StructurePiece)pieces.pieces().get(0);
         BlockBox lv3 = lv2.getBoundingBox();
         int i = lv3.getMinX();
         int j = lv3.getMinZ();
         Direction lv4 = Direction.Type.HORIZONTAL.random(lv);
         Direction lv5 = (Direction)Objects.requireNonNullElse(lv2.getFacing(), lv4);
         StructurePiece lv6 = new OceanMonumentGenerator.Base(lv, i, j, lv5);
         StructurePiecesCollector lv7 = new StructurePiecesCollector();
         lv7.addPiece(lv6);
         return lv7.toList();
      }
   }

   public StructureType getType() {
      return StructureType.OCEAN_MONUMENT;
   }
}
