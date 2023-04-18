package net.minecraft.world.gen.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.RuinedPortalStructurePiece;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

public class RuinedPortalStructure extends Structure {
   private static final String[] COMMON_PORTAL_STRUCTURE_IDS = new String[]{"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
   private static final String[] RARE_PORTAL_STRUCTURE_IDS = new String[]{"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};
   private static final float field_31512 = 0.05F;
   private static final int field_31511 = 15;
   private final List setups;
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(configCodecBuilder(instance), Codecs.nonEmptyList(RuinedPortalStructure.Setup.CODEC.listOf()).fieldOf("setups").forGetter((structure) -> {
         return structure.setups;
      })).apply(instance, RuinedPortalStructure::new);
   });

   public RuinedPortalStructure(Structure.Config config, List setups) {
      super(config);
      this.setups = setups;
   }

   public RuinedPortalStructure(Structure.Config config, Setup setup) {
      this(config, List.of(setup));
   }

   public Optional getStructurePosition(Structure.Context context) {
      RuinedPortalStructurePiece.Properties lv = new RuinedPortalStructurePiece.Properties();
      ChunkRandom lv2 = context.random();
      Setup lv3 = null;
      if (this.setups.size() > 1) {
         float f = 0.0F;

         Setup lv4;
         for(Iterator var6 = this.setups.iterator(); var6.hasNext(); f += lv4.weight()) {
            lv4 = (Setup)var6.next();
         }

         float g = lv2.nextFloat();
         Iterator var22 = this.setups.iterator();

         while(var22.hasNext()) {
            Setup lv5 = (Setup)var22.next();
            g -= lv5.weight() / f;
            if (g < 0.0F) {
               lv3 = lv5;
               break;
            }
         }
      } else {
         lv3 = (Setup)this.setups.get(0);
      }

      if (lv3 == null) {
         throw new IllegalStateException();
      } else {
         lv.airPocket = shouldPlaceAirPocket(lv2, lv3.airPocketProbability());
         lv.mossiness = lv3.mossiness();
         lv.overgrown = lv3.overgrown();
         lv.vines = lv3.vines();
         lv.replaceWithBlackstone = lv3.replaceWithBlackstone();
         Identifier lv7;
         if (lv2.nextFloat() < 0.05F) {
            lv7 = new Identifier(RARE_PORTAL_STRUCTURE_IDS[lv2.nextInt(RARE_PORTAL_STRUCTURE_IDS.length)]);
         } else {
            lv7 = new Identifier(COMMON_PORTAL_STRUCTURE_IDS[lv2.nextInt(COMMON_PORTAL_STRUCTURE_IDS.length)]);
         }

         StructureTemplate lv8 = context.structureTemplateManager().getTemplateOrBlank(lv7);
         BlockRotation lv9 = (BlockRotation)Util.getRandom((Object[])BlockRotation.values(), lv2);
         BlockMirror lv10 = lv2.nextFloat() < 0.5F ? BlockMirror.NONE : BlockMirror.FRONT_BACK;
         BlockPos lv11 = new BlockPos(lv8.getSize().getX() / 2, 0, lv8.getSize().getZ() / 2);
         ChunkGenerator lv12 = context.chunkGenerator();
         HeightLimitView lv13 = context.world();
         NoiseConfig lv14 = context.noiseConfig();
         BlockPos lv15 = context.chunkPos().getStartPos();
         BlockBox lv16 = lv8.calculateBoundingBox(lv15, lv9, lv11, lv10);
         BlockPos lv17 = lv16.getCenter();
         int i = lv12.getHeight(lv17.getX(), lv17.getZ(), RuinedPortalStructurePiece.getHeightmapType(lv3.placement()), lv13, lv14) - 1;
         int j = getFloorHeight(lv2, lv12, lv3.placement(), lv.airPocket, i, lv16.getBlockCountY(), lv16, lv13, lv14);
         BlockPos lv18 = new BlockPos(lv15.getX(), j, lv15.getZ());
         return Optional.of(new Structure.StructurePosition(lv18, (collector) -> {
            if (lv3.canBeCold()) {
               lv.cold = isColdAt(lv18, context.chunkGenerator().getBiomeSource().getBiome(BiomeCoords.fromBlock(lv18.getX()), BiomeCoords.fromBlock(lv18.getY()), BiomeCoords.fromBlock(lv18.getZ()), lv14.getMultiNoiseSampler()));
            }

            collector.addPiece(new RuinedPortalStructurePiece(context.structureTemplateManager(), lv18, lv3.placement(), lv, lv7, lv8, lv9, lv10, lv11));
         }));
      }
   }

   private static boolean shouldPlaceAirPocket(ChunkRandom random, float probability) {
      if (probability == 0.0F) {
         return false;
      } else if (probability == 1.0F) {
         return true;
      } else {
         return random.nextFloat() < probability;
      }
   }

   private static boolean isColdAt(BlockPos pos, RegistryEntry biome) {
      return ((Biome)biome.value()).isCold(pos);
   }

   private static int getFloorHeight(Random random, ChunkGenerator chunkGenerator, RuinedPortalStructurePiece.VerticalPlacement verticalPlacement, boolean airPocket, int height, int blockCountY, BlockBox box, HeightLimitView world, NoiseConfig noiseConfig) {
      int k = world.getBottomY() + 15;
      int l;
      if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.IN_NETHER) {
         if (airPocket) {
            l = MathHelper.nextBetween(random, 32, 100);
         } else if (random.nextFloat() < 0.5F) {
            l = MathHelper.nextBetween(random, 27, 29);
         } else {
            l = MathHelper.nextBetween(random, 29, 100);
         }
      } else {
         int m;
         if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.IN_MOUNTAIN) {
            m = height - blockCountY;
            l = choosePlacementHeight(random, 70, m);
         } else if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.UNDERGROUND) {
            m = height - blockCountY;
            l = choosePlacementHeight(random, k, m);
         } else if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.PARTLY_BURIED) {
            l = height - blockCountY + MathHelper.nextBetween(random, 2, 8);
         } else {
            l = height;
         }
      }

      List list = ImmutableList.of(new BlockPos(box.getMinX(), 0, box.getMinZ()), new BlockPos(box.getMaxX(), 0, box.getMinZ()), new BlockPos(box.getMinX(), 0, box.getMaxZ()), new BlockPos(box.getMaxX(), 0, box.getMaxZ()));
      List list2 = (List)list.stream().map((pos) -> {
         return chunkGenerator.getColumnSample(pos.getX(), pos.getZ(), world, noiseConfig);
      }).collect(Collectors.toList());
      Heightmap.Type lv = verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Type.OCEAN_FLOOR_WG : Heightmap.Type.WORLD_SURFACE_WG;

      int n;
      for(n = l; n > k; --n) {
         int o = 0;
         Iterator var16 = list2.iterator();

         while(var16.hasNext()) {
            VerticalBlockSample lv2 = (VerticalBlockSample)var16.next();
            BlockState lv3 = lv2.getState(n);
            if (lv.getBlockPredicate().test(lv3)) {
               ++o;
               if (o == 3) {
                  return n;
               }
            }
         }
      }

      return n;
   }

   private static int choosePlacementHeight(Random random, int min, int max) {
      return min < max ? MathHelper.nextBetween(random, min, max) : max;
   }

   public StructureType getType() {
      return StructureType.RUINED_PORTAL;
   }

   public static record Setup(RuinedPortalStructurePiece.VerticalPlacement placement, float airPocketProbability, float mossiness, boolean overgrown, boolean vines, boolean canBeCold, boolean replaceWithBlackstone, float weight) {
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(RuinedPortalStructurePiece.VerticalPlacement.CODEC.fieldOf("placement").forGetter(Setup::placement), Codec.floatRange(0.0F, 1.0F).fieldOf("air_pocket_probability").forGetter(Setup::airPocketProbability), Codec.floatRange(0.0F, 1.0F).fieldOf("mossiness").forGetter(Setup::mossiness), Codec.BOOL.fieldOf("overgrown").forGetter(Setup::overgrown), Codec.BOOL.fieldOf("vines").forGetter(Setup::vines), Codec.BOOL.fieldOf("can_be_cold").forGetter(Setup::canBeCold), Codec.BOOL.fieldOf("replace_with_blackstone").forGetter(Setup::replaceWithBlackstone), Codecs.POSITIVE_FLOAT.fieldOf("weight").forGetter(Setup::weight)).apply(instance, Setup::new);
      });

      public Setup(RuinedPortalStructurePiece.VerticalPlacement arg, float f, float g, boolean bl, boolean bl2, boolean bl3, boolean bl4, float h) {
         this.placement = arg;
         this.airPocketProbability = f;
         this.mossiness = g;
         this.overgrown = bl;
         this.vines = bl2;
         this.canBeCold = bl3;
         this.replaceWithBlackstone = bl4;
         this.weight = h;
      }

      public RuinedPortalStructurePiece.VerticalPlacement placement() {
         return this.placement;
      }

      public float airPocketProbability() {
         return this.airPocketProbability;
      }

      public float mossiness() {
         return this.mossiness;
      }

      public boolean overgrown() {
         return this.overgrown;
      }

      public boolean vines() {
         return this.vines;
      }

      public boolean canBeCold() {
         return this.canBeCold;
      }

      public boolean replaceWithBlackstone() {
         return this.replaceWithBlackstone;
      }

      public float weight() {
         return this.weight;
      }
   }
}
