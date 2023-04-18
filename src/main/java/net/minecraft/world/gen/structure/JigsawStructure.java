package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;

public final class JigsawStructure extends Structure {
   public static final int MAX_SIZE = 128;
   public static final Codec CODEC = RecordCodecBuilder.mapCodec((instance) -> {
      return instance.group(configCodecBuilder(instance), StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter((structure) -> {
         return structure.startPool;
      }), Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter((structure) -> {
         return structure.startJigsawName;
      }), Codec.intRange(0, 7).fieldOf("size").forGetter((structure) -> {
         return structure.size;
      }), HeightProvider.CODEC.fieldOf("start_height").forGetter((structure) -> {
         return structure.startHeight;
      }), Codec.BOOL.fieldOf("use_expansion_hack").forGetter((structure) -> {
         return structure.useExpansionHack;
      }), Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter((structure) -> {
         return structure.projectStartToHeightmap;
      }), Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter((structure) -> {
         return structure.maxDistanceFromCenter;
      })).apply(instance, JigsawStructure::new);
   }).flatXmap(createValidator(), createValidator()).codec();
   private final RegistryEntry startPool;
   private final Optional startJigsawName;
   private final int size;
   private final HeightProvider startHeight;
   private final boolean useExpansionHack;
   private final Optional projectStartToHeightmap;
   private final int maxDistanceFromCenter;

   private static Function createValidator() {
      return (feature) -> {
         byte var10000;
         switch (feature.getTerrainAdaptation()) {
            case NONE:
               var10000 = 0;
               break;
            case BURY:
            case BEARD_THIN:
            case BEARD_BOX:
               var10000 = 12;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         int i = var10000;
         return feature.maxDistanceFromCenter + i > 128 ? DataResult.error(() -> {
            return "Structure size including terrain adaptation must not exceed 128";
         }) : DataResult.success(feature);
      };
   }

   public JigsawStructure(Structure.Config config, RegistryEntry startPool, Optional startJigsawName, int size, HeightProvider startHeight, boolean useExpansionHack, Optional projectStartToHeightmap, int maxDistanceFromCenter) {
      super(config);
      this.startPool = startPool;
      this.startJigsawName = startJigsawName;
      this.size = size;
      this.startHeight = startHeight;
      this.useExpansionHack = useExpansionHack;
      this.projectStartToHeightmap = projectStartToHeightmap;
      this.maxDistanceFromCenter = maxDistanceFromCenter;
   }

   public JigsawStructure(Structure.Config config, RegistryEntry startPool, int size, HeightProvider startHeight, boolean useExpansionHack, Heightmap.Type projectStartToHeightmap) {
      this(config, startPool, Optional.empty(), size, startHeight, useExpansionHack, Optional.of(projectStartToHeightmap), 80);
   }

   public JigsawStructure(Structure.Config config, RegistryEntry startPool, int size, HeightProvider startHeight, boolean useExpansionHack) {
      this(config, startPool, Optional.empty(), size, startHeight, useExpansionHack, Optional.empty(), 80);
   }

   public Optional getStructurePosition(Structure.Context context) {
      ChunkPos lv = context.chunkPos();
      int i = this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world()));
      BlockPos lv2 = new BlockPos(lv.getStartX(), i, lv.getStartZ());
      return StructurePoolBasedGenerator.generate(context, this.startPool, this.startJigsawName, this.size, lv2, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter);
   }

   public StructureType getType() {
      return StructureType.JIGSAW;
   }
}
