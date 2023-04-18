package net.minecraft.world.gen;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;

public class FlatLevelGeneratorPresets {
   public static final RegistryKey CLASSIC_FLAT = of("classic_flat");
   public static final RegistryKey TUNNELERS_DREAM = of("tunnelers_dream");
   public static final RegistryKey WATER_WORLD = of("water_world");
   public static final RegistryKey OVERWORLD = of("overworld");
   public static final RegistryKey SNOWY_KINGDOM = of("snowy_kingdom");
   public static final RegistryKey BOTTOMLESS_PIT = of("bottomless_pit");
   public static final RegistryKey DESERT = of("desert");
   public static final RegistryKey REDSTONE_READY = of("redstone_ready");
   public static final RegistryKey THE_VOID = of("the_void");

   public static void bootstrap(Registerable presetRegisterable) {
      (new Registrar(presetRegisterable)).bootstrap();
   }

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET, new Identifier(id));
   }

   private static class Registrar {
      private final Registerable presetRegisterable;

      Registrar(Registerable presetRegisterable) {
         this.presetRegisterable = presetRegisterable;
      }

      private void createAndRegister(RegistryKey registryKey, ItemConvertible icon, RegistryKey biome, Set structureSetKeys, boolean hasFeatures, boolean hasLakes, FlatChunkGeneratorLayer... layers) {
         RegistryEntryLookup lv = this.presetRegisterable.getRegistryLookup(RegistryKeys.STRUCTURE_SET);
         RegistryEntryLookup lv2 = this.presetRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
         RegistryEntryLookup lv3 = this.presetRegisterable.getRegistryLookup(RegistryKeys.BIOME);
         Stream var10000 = structureSetKeys.stream();
         Objects.requireNonNull(lv);
         RegistryEntryList.Direct lv4 = RegistryEntryList.of((List)var10000.map(lv::getOrThrow).collect(Collectors.toList()));
         FlatChunkGeneratorConfig lv5 = new FlatChunkGeneratorConfig(Optional.of(lv4), lv3.getOrThrow(biome), FlatChunkGeneratorConfig.getLavaLakes(lv2));
         if (hasFeatures) {
            lv5.enableFeatures();
         }

         if (hasLakes) {
            lv5.enableLakes();
         }

         for(int i = layers.length - 1; i >= 0; --i) {
            lv5.getLayers().add(layers[i]);
         }

         this.presetRegisterable.register(registryKey, new FlatLevelGeneratorPreset(icon.asItem().getRegistryEntry(), lv5));
      }

      public void bootstrap() {
         this.createAndRegister(FlatLevelGeneratorPresets.CLASSIC_FLAT, Blocks.GRASS_BLOCK, BiomeKeys.PLAINS, ImmutableSet.of(StructureSetKeys.VILLAGES), false, false, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(2, Blocks.DIRT), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
         this.createAndRegister(FlatLevelGeneratorPresets.TUNNELERS_DREAM, Blocks.STONE, BiomeKeys.WINDSWEPT_HILLS, ImmutableSet.of(StructureSetKeys.MINESHAFTS, StructureSetKeys.STRONGHOLDS), true, false, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(5, Blocks.DIRT), new FlatChunkGeneratorLayer(230, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
         this.createAndRegister(FlatLevelGeneratorPresets.WATER_WORLD, Items.WATER_BUCKET, BiomeKeys.DEEP_OCEAN, ImmutableSet.of(StructureSetKeys.OCEAN_RUINS, StructureSetKeys.SHIPWRECKS, StructureSetKeys.OCEAN_MONUMENTS), false, false, new FlatChunkGeneratorLayer(90, Blocks.WATER), new FlatChunkGeneratorLayer(5, Blocks.GRAVEL), new FlatChunkGeneratorLayer(5, Blocks.DIRT), new FlatChunkGeneratorLayer(5, Blocks.STONE), new FlatChunkGeneratorLayer(64, Blocks.DEEPSLATE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
         this.createAndRegister(FlatLevelGeneratorPresets.OVERWORLD, Blocks.GRASS, BiomeKeys.PLAINS, ImmutableSet.of(StructureSetKeys.VILLAGES, StructureSetKeys.MINESHAFTS, StructureSetKeys.PILLAGER_OUTPOSTS, StructureSetKeys.RUINED_PORTALS, StructureSetKeys.STRONGHOLDS), true, true, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(3, Blocks.DIRT), new FlatChunkGeneratorLayer(59, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
         this.createAndRegister(FlatLevelGeneratorPresets.SNOWY_KINGDOM, Blocks.SNOW, BiomeKeys.SNOWY_PLAINS, ImmutableSet.of(StructureSetKeys.VILLAGES, StructureSetKeys.IGLOOS), false, false, new FlatChunkGeneratorLayer(1, Blocks.SNOW), new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(3, Blocks.DIRT), new FlatChunkGeneratorLayer(59, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
         this.createAndRegister(FlatLevelGeneratorPresets.BOTTOMLESS_PIT, Items.FEATHER, BiomeKeys.PLAINS, ImmutableSet.of(StructureSetKeys.VILLAGES), false, false, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(3, Blocks.DIRT), new FlatChunkGeneratorLayer(2, Blocks.COBBLESTONE));
         this.createAndRegister(FlatLevelGeneratorPresets.DESERT, Blocks.SAND, BiomeKeys.DESERT, ImmutableSet.of(StructureSetKeys.VILLAGES, StructureSetKeys.DESERT_PYRAMIDS, StructureSetKeys.MINESHAFTS, StructureSetKeys.STRONGHOLDS), true, false, new FlatChunkGeneratorLayer(8, Blocks.SAND), new FlatChunkGeneratorLayer(52, Blocks.SANDSTONE), new FlatChunkGeneratorLayer(3, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
         this.createAndRegister(FlatLevelGeneratorPresets.REDSTONE_READY, Items.REDSTONE, BiomeKeys.DESERT, ImmutableSet.of(), false, false, new FlatChunkGeneratorLayer(116, Blocks.SANDSTONE), new FlatChunkGeneratorLayer(3, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
         this.createAndRegister(FlatLevelGeneratorPresets.THE_VOID, Blocks.BARRIER, BiomeKeys.THE_VOID, ImmutableSet.of(), true, false, new FlatChunkGeneratorLayer(1, Blocks.AIR));
      }
   }
}
