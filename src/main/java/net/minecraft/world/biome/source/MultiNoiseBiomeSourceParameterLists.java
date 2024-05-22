/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.biome.source;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;

public class MultiNoiseBiomeSourceParameterLists {
    public static final RegistryKey<MultiNoiseBiomeSourceParameterList> NETHER = MultiNoiseBiomeSourceParameterLists.of("nether");
    public static final RegistryKey<MultiNoiseBiomeSourceParameterList> OVERWORLD = MultiNoiseBiomeSourceParameterLists.of("overworld");

    public static void bootstrap(Registerable<MultiNoiseBiomeSourceParameterList> registry) {
        RegistryEntryLookup<Biome> lv = registry.getRegistryLookup(RegistryKeys.BIOME);
        registry.register(NETHER, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.NETHER, lv));
        registry.register(OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, lv));
    }

    private static RegistryKey<MultiNoiseBiomeSourceParameterList> of(String id) {
        return RegistryKey.of(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, Identifier.method_60656(id));
    }
}

