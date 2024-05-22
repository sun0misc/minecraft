/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.foliage;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.foliage.AcaciaFoliagePlacer;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.BushFoliagePlacer;
import net.minecraft.world.gen.foliage.CherryFoliagePlacer;
import net.minecraft.world.gen.foliage.DarkOakFoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.JungleFoliagePlacer;
import net.minecraft.world.gen.foliage.LargeOakFoliagePlacer;
import net.minecraft.world.gen.foliage.MegaPineFoliagePlacer;
import net.minecraft.world.gen.foliage.PineFoliagePlacer;
import net.minecraft.world.gen.foliage.RandomSpreadFoliagePlacer;
import net.minecraft.world.gen.foliage.SpruceFoliagePlacer;

public class FoliagePlacerType<P extends FoliagePlacer> {
    public static final FoliagePlacerType<BlobFoliagePlacer> BLOB_FOLIAGE_PLACER = FoliagePlacerType.register("blob_foliage_placer", BlobFoliagePlacer.CODEC);
    public static final FoliagePlacerType<SpruceFoliagePlacer> SPRUCE_FOLIAGE_PLACER = FoliagePlacerType.register("spruce_foliage_placer", SpruceFoliagePlacer.CODEC);
    public static final FoliagePlacerType<PineFoliagePlacer> PINE_FOLIAGE_PLACER = FoliagePlacerType.register("pine_foliage_placer", PineFoliagePlacer.CODEC);
    public static final FoliagePlacerType<AcaciaFoliagePlacer> ACACIA_FOLIAGE_PLACER = FoliagePlacerType.register("acacia_foliage_placer", AcaciaFoliagePlacer.CODEC);
    public static final FoliagePlacerType<BushFoliagePlacer> BUSH_FOLIAGE_PLACER = FoliagePlacerType.register("bush_foliage_placer", BushFoliagePlacer.CODEC);
    public static final FoliagePlacerType<LargeOakFoliagePlacer> FANCY_FOLIAGE_PLACER = FoliagePlacerType.register("fancy_foliage_placer", LargeOakFoliagePlacer.CODEC);
    public static final FoliagePlacerType<JungleFoliagePlacer> JUNGLE_FOLIAGE_PLACER = FoliagePlacerType.register("jungle_foliage_placer", JungleFoliagePlacer.CODEC);
    public static final FoliagePlacerType<MegaPineFoliagePlacer> MEGA_PINE_FOLIAGE_PLACER = FoliagePlacerType.register("mega_pine_foliage_placer", MegaPineFoliagePlacer.CODEC);
    public static final FoliagePlacerType<DarkOakFoliagePlacer> DARK_OAK_FOLIAGE_PLACER = FoliagePlacerType.register("dark_oak_foliage_placer", DarkOakFoliagePlacer.CODEC);
    public static final FoliagePlacerType<RandomSpreadFoliagePlacer> RANDOM_SPREAD_FOLIAGE_PLACER = FoliagePlacerType.register("random_spread_foliage_placer", RandomSpreadFoliagePlacer.CODEC);
    public static final FoliagePlacerType<CherryFoliagePlacer> CHERRY_FOLIAGE_PLACER = FoliagePlacerType.register("cherry_foliage_placer", CherryFoliagePlacer.CODEC);
    private final MapCodec<P> codec;

    private static <P extends FoliagePlacer> FoliagePlacerType<P> register(String id, MapCodec<P> codec) {
        return Registry.register(Registries.FOLIAGE_PLACER_TYPE, id, new FoliagePlacerType<P>(codec));
    }

    private FoliagePlacerType(MapCodec<P> codec) {
        this.codec = codec;
    }

    public MapCodec<P> getCodec() {
        return this.codec;
    }
}

