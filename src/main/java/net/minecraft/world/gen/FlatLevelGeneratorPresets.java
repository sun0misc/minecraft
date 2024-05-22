/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.gen.feature.PlacedFeature;

public class FlatLevelGeneratorPresets {
    public static final RegistryKey<FlatLevelGeneratorPreset> CLASSIC_FLAT = FlatLevelGeneratorPresets.of("classic_flat");
    public static final RegistryKey<FlatLevelGeneratorPreset> TUNNELERS_DREAM = FlatLevelGeneratorPresets.of("tunnelers_dream");
    public static final RegistryKey<FlatLevelGeneratorPreset> WATER_WORLD = FlatLevelGeneratorPresets.of("water_world");
    public static final RegistryKey<FlatLevelGeneratorPreset> OVERWORLD = FlatLevelGeneratorPresets.of("overworld");
    public static final RegistryKey<FlatLevelGeneratorPreset> SNOWY_KINGDOM = FlatLevelGeneratorPresets.of("snowy_kingdom");
    public static final RegistryKey<FlatLevelGeneratorPreset> BOTTOMLESS_PIT = FlatLevelGeneratorPresets.of("bottomless_pit");
    public static final RegistryKey<FlatLevelGeneratorPreset> DESERT = FlatLevelGeneratorPresets.of("desert");
    public static final RegistryKey<FlatLevelGeneratorPreset> REDSTONE_READY = FlatLevelGeneratorPresets.of("redstone_ready");
    public static final RegistryKey<FlatLevelGeneratorPreset> THE_VOID = FlatLevelGeneratorPresets.of("the_void");

    public static void bootstrap(Registerable<FlatLevelGeneratorPreset> presetRegisterable) {
        new Registrar(presetRegisterable).bootstrap();
    }

    private static RegistryKey<FlatLevelGeneratorPreset> of(String id) {
        return RegistryKey.of(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET, Identifier.method_60656(id));
    }

    static class Registrar {
        private final Registerable<FlatLevelGeneratorPreset> presetRegisterable;

        Registrar(Registerable<FlatLevelGeneratorPreset> presetRegisterable) {
            this.presetRegisterable = presetRegisterable;
        }

        private void createAndRegister(RegistryKey<FlatLevelGeneratorPreset> registryKey, ItemConvertible icon, RegistryKey<Biome> biome, Set<RegistryKey<StructureSet>> structureSetKeys, boolean hasFeatures, boolean hasLakes, FlatChunkGeneratorLayer ... layers) {
            RegistryEntryLookup<StructureSet> lv = this.presetRegisterable.getRegistryLookup(RegistryKeys.STRUCTURE_SET);
            RegistryEntryLookup<PlacedFeature> lv2 = this.presetRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
            RegistryEntryLookup<Biome> lv3 = this.presetRegisterable.getRegistryLookup(RegistryKeys.BIOME);
            RegistryEntryList.Direct lv4 = RegistryEntryList.of(structureSetKeys.stream().map(lv::getOrThrow).collect(Collectors.toList()));
            FlatChunkGeneratorConfig lv5 = new FlatChunkGeneratorConfig(Optional.of(lv4), lv3.getOrThrow(biome), FlatChunkGeneratorConfig.getLavaLakes(lv2));
            if (hasFeatures) {
                lv5.enableFeatures();
            }
            if (hasLakes) {
                lv5.enableLakes();
            }
            for (int i = layers.length - 1; i >= 0; --i) {
                lv5.getLayers().add(layers[i]);
            }
            this.presetRegisterable.register(registryKey, new FlatLevelGeneratorPreset(icon.asItem().getRegistryEntry(), lv5));
        }

        public void bootstrap() {
            this.createAndRegister(CLASSIC_FLAT, Blocks.GRASS_BLOCK, BiomeKeys.PLAINS, ImmutableSet.of(StructureSetKeys.VILLAGES), false, false, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(2, Blocks.DIRT), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
            this.createAndRegister(TUNNELERS_DREAM, Blocks.STONE, BiomeKeys.WINDSWEPT_HILLS, ImmutableSet.of(StructureSetKeys.MINESHAFTS, StructureSetKeys.STRONGHOLDS), true, false, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(5, Blocks.DIRT), new FlatChunkGeneratorLayer(230, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
            this.createAndRegister(WATER_WORLD, Items.WATER_BUCKET, BiomeKeys.DEEP_OCEAN, ImmutableSet.of(StructureSetKeys.OCEAN_RUINS, StructureSetKeys.SHIPWRECKS, StructureSetKeys.OCEAN_MONUMENTS), false, false, new FlatChunkGeneratorLayer(90, Blocks.WATER), new FlatChunkGeneratorLayer(5, Blocks.GRAVEL), new FlatChunkGeneratorLayer(5, Blocks.DIRT), new FlatChunkGeneratorLayer(5, Blocks.STONE), new FlatChunkGeneratorLayer(64, Blocks.DEEPSLATE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
            this.createAndRegister(OVERWORLD, Blocks.SHORT_GRASS, BiomeKeys.PLAINS, ImmutableSet.of(StructureSetKeys.VILLAGES, StructureSetKeys.MINESHAFTS, StructureSetKeys.PILLAGER_OUTPOSTS, StructureSetKeys.RUINED_PORTALS, StructureSetKeys.STRONGHOLDS), true, true, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(3, Blocks.DIRT), new FlatChunkGeneratorLayer(59, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
            this.createAndRegister(SNOWY_KINGDOM, Blocks.SNOW, BiomeKeys.SNOWY_PLAINS, ImmutableSet.of(StructureSetKeys.VILLAGES, StructureSetKeys.IGLOOS), false, false, new FlatChunkGeneratorLayer(1, Blocks.SNOW), new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(3, Blocks.DIRT), new FlatChunkGeneratorLayer(59, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
            this.createAndRegister(BOTTOMLESS_PIT, Items.FEATHER, BiomeKeys.PLAINS, ImmutableSet.of(StructureSetKeys.VILLAGES), false, false, new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK), new FlatChunkGeneratorLayer(3, Blocks.DIRT), new FlatChunkGeneratorLayer(2, Blocks.COBBLESTONE));
            this.createAndRegister(DESERT, Blocks.SAND, BiomeKeys.DESERT, ImmutableSet.of(StructureSetKeys.VILLAGES, StructureSetKeys.DESERT_PYRAMIDS, StructureSetKeys.MINESHAFTS, StructureSetKeys.STRONGHOLDS), true, false, new FlatChunkGeneratorLayer(8, Blocks.SAND), new FlatChunkGeneratorLayer(52, Blocks.SANDSTONE), new FlatChunkGeneratorLayer(3, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
            this.createAndRegister(REDSTONE_READY, Items.REDSTONE, BiomeKeys.DESERT, ImmutableSet.of(), false, false, new FlatChunkGeneratorLayer(116, Blocks.SANDSTONE), new FlatChunkGeneratorLayer(3, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
            this.createAndRegister(THE_VOID, Blocks.BARRIER, BiomeKeys.THE_VOID, ImmutableSet.of(), true, false, new FlatChunkGeneratorLayer(1, Blocks.AIR));
        }
    }
}

