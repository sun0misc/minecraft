/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.BitSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import org.jetbrains.annotations.Nullable;

public final class BelowZeroRetrogen {
    private static final BitSet EMPTY_MISSING_BEDROCK_BIT_SET = new BitSet(0);
    private static final Codec<BitSet> MISSING_BEDROCK_CODEC = Codec.LONG_STREAM.xmap(serializedBedrockBitSet -> BitSet.valueOf(serializedBedrockBitSet.toArray()), bedrockBitSet -> LongStream.of(bedrockBitSet.toLongArray()));
    private static final Codec<ChunkStatus> STATUS_CODEC = Registries.CHUNK_STATUS.getCodec().comapFlatMap(status -> status == ChunkStatus.EMPTY ? DataResult.error(() -> "target_status cannot be empty") : DataResult.success(status), Function.identity());
    public static final Codec<BelowZeroRetrogen> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)STATUS_CODEC.fieldOf("target_status")).forGetter(BelowZeroRetrogen::getTargetStatus), MISSING_BEDROCK_CODEC.lenientOptionalFieldOf("missing_bedrock").forGetter(belowZeroRetrogen -> belowZeroRetrogen.missingBedrock.isEmpty() ? Optional.empty() : Optional.of(belowZeroRetrogen.missingBedrock))).apply((Applicative<BelowZeroRetrogen, ?>)instance, BelowZeroRetrogen::new));
    private static final Set<RegistryKey<Biome>> CAVE_BIOMES = Set.of(BiomeKeys.LUSH_CAVES, BiomeKeys.DRIPSTONE_CAVES, BiomeKeys.DEEP_DARK);
    public static final HeightLimitView BELOW_ZERO_VIEW = new HeightLimitView(){

        @Override
        public int getHeight() {
            return 64;
        }

        @Override
        public int getBottomY() {
            return -64;
        }
    };
    private final ChunkStatus targetStatus;
    private final BitSet missingBedrock;

    private BelowZeroRetrogen(ChunkStatus targetStatus, Optional<BitSet> missingBedrock) {
        this.targetStatus = targetStatus;
        this.missingBedrock = missingBedrock.orElse(EMPTY_MISSING_BEDROCK_BIT_SET);
    }

    @Nullable
    public static BelowZeroRetrogen fromNbt(NbtCompound nbt) {
        ChunkStatus lv = ChunkStatus.byId(nbt.getString("target_status"));
        if (lv == ChunkStatus.EMPTY) {
            return null;
        }
        return new BelowZeroRetrogen(lv, Optional.of(BitSet.valueOf(nbt.getLongArray("missing_bedrock"))));
    }

    public static void replaceOldBedrock(ProtoChunk chunk) {
        int i = 4;
        BlockPos.iterate(0, 0, 0, 15, 4, 15).forEach(pos -> {
            if (chunk.getBlockState((BlockPos)pos).isOf(Blocks.BEDROCK)) {
                chunk.setBlockState((BlockPos)pos, Blocks.DEEPSLATE.getDefaultState(), false);
            }
        });
    }

    public void fillColumnsWithAirIfMissingBedrock(ProtoChunk chunk) {
        HeightLimitView lv = chunk.getHeightLimitView();
        int i = lv.getBottomY();
        int j = lv.getTopY() - 1;
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                if (!this.isColumnMissingBedrock(k, l)) continue;
                BlockPos.iterate(k, i, l, k, j, l).forEach(pos -> chunk.setBlockState((BlockPos)pos, Blocks.AIR.getDefaultState(), false));
            }
        }
    }

    public ChunkStatus getTargetStatus() {
        return this.targetStatus;
    }

    public boolean hasMissingBedrock() {
        return !this.missingBedrock.isEmpty();
    }

    public boolean isColumnMissingBedrock(int x, int z) {
        return this.missingBedrock.get((z & 0xF) * 16 + (x & 0xF));
    }

    public static BiomeSupplier getBiomeSupplier(BiomeSupplier biomeSupplier, Chunk chunk) {
        if (!chunk.hasBelowZeroRetrogen()) {
            return biomeSupplier;
        }
        Predicate<RegistryKey> predicate = CAVE_BIOMES::contains;
        return (x, y, z, noise) -> {
            RegistryEntry<Biome> lv = biomeSupplier.getBiome(x, y, z, noise);
            if (lv.matches(predicate)) {
                return lv;
            }
            return chunk.getBiomeForNoiseGen(x, 0, z);
        };
    }
}

