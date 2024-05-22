/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import org.jetbrains.annotations.Nullable;

public final class SaplingGenerator {
    private static final Map<String, SaplingGenerator> GENERATORS = new Object2ObjectArrayMap<String, SaplingGenerator>();
    public static final Codec<SaplingGenerator> CODEC = Codec.stringResolver(generator -> generator.id, GENERATORS::get);
    public static final SaplingGenerator OAK = new SaplingGenerator("oak", 0.1f, Optional.empty(), Optional.empty(), Optional.of(TreeConfiguredFeatures.OAK), Optional.of(TreeConfiguredFeatures.FANCY_OAK), Optional.of(TreeConfiguredFeatures.OAK_BEES_005), Optional.of(TreeConfiguredFeatures.FANCY_OAK_BEES_005));
    public static final SaplingGenerator SPRUCE = new SaplingGenerator("spruce", 0.5f, Optional.of(TreeConfiguredFeatures.MEGA_SPRUCE), Optional.of(TreeConfiguredFeatures.MEGA_PINE), Optional.of(TreeConfiguredFeatures.SPRUCE), Optional.empty(), Optional.empty(), Optional.empty());
    public static final SaplingGenerator MANGROVE = new SaplingGenerator("mangrove", 0.85f, Optional.empty(), Optional.empty(), Optional.of(TreeConfiguredFeatures.MANGROVE), Optional.of(TreeConfiguredFeatures.TALL_MANGROVE), Optional.empty(), Optional.empty());
    public static final SaplingGenerator AZALEA = new SaplingGenerator("azalea", Optional.empty(), Optional.of(TreeConfiguredFeatures.AZALEA_TREE), Optional.empty());
    public static final SaplingGenerator BIRCH = new SaplingGenerator("birch", Optional.empty(), Optional.of(TreeConfiguredFeatures.BIRCH), Optional.of(TreeConfiguredFeatures.BIRCH_BEES_005));
    public static final SaplingGenerator JUNGLE = new SaplingGenerator("jungle", Optional.of(TreeConfiguredFeatures.MEGA_JUNGLE_TREE), Optional.of(TreeConfiguredFeatures.JUNGLE_TREE_NO_VINE), Optional.empty());
    public static final SaplingGenerator ACACIA = new SaplingGenerator("acacia", Optional.empty(), Optional.of(TreeConfiguredFeatures.ACACIA), Optional.empty());
    public static final SaplingGenerator CHERRY = new SaplingGenerator("cherry", Optional.empty(), Optional.of(TreeConfiguredFeatures.CHERRY), Optional.of(TreeConfiguredFeatures.CHERRY_BEES_005));
    public static final SaplingGenerator DARK_OAK = new SaplingGenerator("dark_oak", Optional.of(TreeConfiguredFeatures.DARK_OAK), Optional.empty(), Optional.empty());
    private final String id;
    private final float rareChance;
    private final Optional<RegistryKey<ConfiguredFeature<?, ?>>> megaVariant;
    private final Optional<RegistryKey<ConfiguredFeature<?, ?>>> rareMegaVariant;
    private final Optional<RegistryKey<ConfiguredFeature<?, ?>>> regularVariant;
    private final Optional<RegistryKey<ConfiguredFeature<?, ?>>> rareRegularVariant;
    private final Optional<RegistryKey<ConfiguredFeature<?, ?>>> beesVariant;
    private final Optional<RegistryKey<ConfiguredFeature<?, ?>>> rareBeesVariant;

    public SaplingGenerator(String id, Optional<RegistryKey<ConfiguredFeature<?, ?>>> megaVariant, Optional<RegistryKey<ConfiguredFeature<?, ?>>> regularVariant, Optional<RegistryKey<ConfiguredFeature<?, ?>>> beesVariant) {
        this(id, 0.0f, megaVariant, Optional.empty(), regularVariant, Optional.empty(), beesVariant, Optional.empty());
    }

    public SaplingGenerator(String id, float rareChance, Optional<RegistryKey<ConfiguredFeature<?, ?>>> megaVariant, Optional<RegistryKey<ConfiguredFeature<?, ?>>> rareMegaVariant, Optional<RegistryKey<ConfiguredFeature<?, ?>>> regularVariant, Optional<RegistryKey<ConfiguredFeature<?, ?>>> rareRegularVariant, Optional<RegistryKey<ConfiguredFeature<?, ?>>> beesVariant, Optional<RegistryKey<ConfiguredFeature<?, ?>>> rareBeesVariant) {
        this.id = id;
        this.rareChance = rareChance;
        this.megaVariant = megaVariant;
        this.rareMegaVariant = rareMegaVariant;
        this.regularVariant = regularVariant;
        this.rareRegularVariant = rareRegularVariant;
        this.beesVariant = beesVariant;
        this.rareBeesVariant = rareBeesVariant;
        GENERATORS.put(id, this);
    }

    @Nullable
    private RegistryKey<ConfiguredFeature<?, ?>> getSmallTreeFeature(Random random, boolean flowersNearby) {
        if (random.nextFloat() < this.rareChance) {
            if (flowersNearby && this.rareBeesVariant.isPresent()) {
                return this.rareBeesVariant.get();
            }
            if (this.rareRegularVariant.isPresent()) {
                return this.rareRegularVariant.get();
            }
        }
        if (flowersNearby && this.beesVariant.isPresent()) {
            return this.beesVariant.get();
        }
        return this.regularVariant.orElse(null);
    }

    @Nullable
    private RegistryKey<ConfiguredFeature<?, ?>> getMegaTreeFeature(Random random) {
        if (this.rareMegaVariant.isPresent() && random.nextFloat() < this.rareChance) {
            return this.rareMegaVariant.get();
        }
        return this.megaVariant.orElse(null);
    }

    public boolean generate(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random) {
        RegistryKey<ConfiguredFeature<?, ?>> lv5;
        RegistryEntry lv2;
        RegistryKey<ConfiguredFeature<?, ?>> lv = this.getMegaTreeFeature(random);
        if (lv != null && (lv2 = (RegistryEntry)world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).getEntry(lv).orElse(null)) != null) {
            for (int i = 0; i >= -1; --i) {
                for (int j = 0; j >= -1; --j) {
                    if (!SaplingGenerator.canGenerateLargeTree(state, world, pos, i, j)) continue;
                    ConfiguredFeature lv3 = (ConfiguredFeature)lv2.value();
                    BlockState lv4 = Blocks.AIR.getDefaultState();
                    world.setBlockState(pos.add(i, 0, j), lv4, Block.NO_REDRAW);
                    world.setBlockState(pos.add(i + 1, 0, j), lv4, Block.NO_REDRAW);
                    world.setBlockState(pos.add(i, 0, j + 1), lv4, Block.NO_REDRAW);
                    world.setBlockState(pos.add(i + 1, 0, j + 1), lv4, Block.NO_REDRAW);
                    if (lv3.generate(world, chunkGenerator, random, pos.add(i, 0, j))) {
                        return true;
                    }
                    world.setBlockState(pos.add(i, 0, j), state, Block.NO_REDRAW);
                    world.setBlockState(pos.add(i + 1, 0, j), state, Block.NO_REDRAW);
                    world.setBlockState(pos.add(i, 0, j + 1), state, Block.NO_REDRAW);
                    world.setBlockState(pos.add(i + 1, 0, j + 1), state, Block.NO_REDRAW);
                    return false;
                }
            }
        }
        if ((lv5 = this.getSmallTreeFeature(random, this.areFlowersNearby(world, pos))) == null) {
            return false;
        }
        RegistryEntry lv6 = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).getEntry(lv5).orElse(null);
        if (lv6 == null) {
            return false;
        }
        ConfiguredFeature lv7 = (ConfiguredFeature)lv6.value();
        BlockState lv8 = world.getFluidState(pos).getBlockState();
        world.setBlockState(pos, lv8, Block.NO_REDRAW);
        if (lv7.generate(world, chunkGenerator, random, pos)) {
            if (world.getBlockState(pos) == lv8) {
                world.updateListeners(pos, state, lv8, Block.NOTIFY_LISTENERS);
            }
            return true;
        }
        world.setBlockState(pos, state, Block.NO_REDRAW);
        return false;
    }

    private static boolean canGenerateLargeTree(BlockState state, BlockView world, BlockPos pos, int x, int z) {
        Block lv = state.getBlock();
        return world.getBlockState(pos.add(x, 0, z)).isOf(lv) && world.getBlockState(pos.add(x + 1, 0, z)).isOf(lv) && world.getBlockState(pos.add(x, 0, z + 1)).isOf(lv) && world.getBlockState(pos.add(x + 1, 0, z + 1)).isOf(lv);
    }

    private boolean areFlowersNearby(WorldAccess world, BlockPos pos) {
        for (BlockPos lv : BlockPos.Mutable.iterate(pos.down().north(2).west(2), pos.up().south(2).east(2))) {
            if (!world.getBlockState(lv).isIn(BlockTags.FLOWERS)) continue;
            return true;
        }
        return false;
    }
}

