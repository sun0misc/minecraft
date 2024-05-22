/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndSpikeFeature
extends Feature<EndSpikeFeatureConfig> {
    public static final int COUNT = 10;
    private static final int DISTANCE_FROM_ORIGIN = 42;
    private static final LoadingCache<Long, List<Spike>> CACHE = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build(new SpikeCache());

    public EndSpikeFeature(Codec<EndSpikeFeatureConfig> codec) {
        super(codec);
    }

    public static List<Spike> getSpikes(StructureWorldAccess world) {
        Random lv = Random.create(world.getSeed());
        long l = lv.nextLong() & 0xFFFFL;
        return CACHE.getUnchecked(l);
    }

    @Override
    public boolean generate(FeatureContext<EndSpikeFeatureConfig> context) {
        EndSpikeFeatureConfig lv = context.getConfig();
        StructureWorldAccess lv2 = context.getWorld();
        Random lv3 = context.getRandom();
        BlockPos lv4 = context.getOrigin();
        List<Spike> list = lv.getSpikes();
        if (list.isEmpty()) {
            list = EndSpikeFeature.getSpikes(lv2);
        }
        for (Spike lv5 : list) {
            if (!lv5.isInChunk(lv4)) continue;
            this.generateSpike(lv2, lv3, lv, lv5);
        }
        return true;
    }

    private void generateSpike(ServerWorldAccess world, Random random, EndSpikeFeatureConfig config, Spike spike) {
        EndCrystalEntity lv4;
        int i = spike.getRadius();
        for (BlockPos lv : BlockPos.iterate(new BlockPos(spike.getCenterX() - i, world.getBottomY(), spike.getCenterZ() - i), new BlockPos(spike.getCenterX() + i, spike.getHeight() + 10, spike.getCenterZ() + i))) {
            if (lv.getSquaredDistance(spike.getCenterX(), lv.getY(), spike.getCenterZ()) <= (double)(i * i + 1) && lv.getY() < spike.getHeight()) {
                this.setBlockState(world, lv, Blocks.OBSIDIAN.getDefaultState());
                continue;
            }
            if (lv.getY() <= 65) continue;
            this.setBlockState(world, lv, Blocks.AIR.getDefaultState());
        }
        if (spike.isGuarded()) {
            int j = -2;
            int k = 2;
            int l = 3;
            BlockPos.Mutable lv2 = new BlockPos.Mutable();
            for (int m = -2; m <= 2; ++m) {
                for (int n = -2; n <= 2; ++n) {
                    for (int o = 0; o <= 3; ++o) {
                        boolean bl3;
                        boolean bl = MathHelper.abs(m) == 2;
                        boolean bl2 = MathHelper.abs(n) == 2;
                        boolean bl4 = bl3 = o == 3;
                        if (!bl && !bl2 && !bl3) continue;
                        boolean bl42 = m == -2 || m == 2 || bl3;
                        boolean bl5 = n == -2 || n == 2 || bl3;
                        BlockState lv3 = (BlockState)((BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, bl42 && n != -2)).with(PaneBlock.SOUTH, bl42 && n != 2)).with(PaneBlock.WEST, bl5 && m != -2)).with(PaneBlock.EAST, bl5 && m != 2);
                        this.setBlockState(world, lv2.set(spike.getCenterX() + m, spike.getHeight() + o, spike.getCenterZ() + n), lv3);
                    }
                }
            }
        }
        if ((lv4 = EntityType.END_CRYSTAL.create(world.toServerWorld())) != null) {
            lv4.setBeamTarget(config.getPos());
            lv4.setInvulnerable(config.isCrystalInvulnerable());
            lv4.refreshPositionAndAngles((double)spike.getCenterX() + 0.5, spike.getHeight() + 1, (double)spike.getCenterZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
            world.spawnEntity(lv4);
            BlockPos lv = lv4.getBlockPos();
            this.setBlockState(world, lv.down(), Blocks.BEDROCK.getDefaultState());
            this.setBlockState(world, lv, FireBlock.getState(world, lv));
        }
    }

    public static class Spike {
        public static final Codec<Spike> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("centerX")).orElse(0).forGetter(spike -> spike.centerX), ((MapCodec)Codec.INT.fieldOf("centerZ")).orElse(0).forGetter(spike -> spike.centerZ), ((MapCodec)Codec.INT.fieldOf("radius")).orElse(0).forGetter(spike -> spike.radius), ((MapCodec)Codec.INT.fieldOf("height")).orElse(0).forGetter(spike -> spike.height), ((MapCodec)Codec.BOOL.fieldOf("guarded")).orElse(false).forGetter(spike -> spike.guarded)).apply((Applicative<Spike, ?>)instance, Spike::new));
        private final int centerX;
        private final int centerZ;
        private final int radius;
        private final int height;
        private final boolean guarded;
        private final Box boundingBox;

        public Spike(int centerX, int centerZ, int radius, int height, boolean guarded) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = radius;
            this.height = height;
            this.guarded = guarded;
            this.boundingBox = new Box(centerX - radius, DimensionType.MIN_HEIGHT, centerZ - radius, centerX + radius, DimensionType.MAX_COLUMN_HEIGHT, centerZ + radius);
        }

        public boolean isInChunk(BlockPos pos) {
            return ChunkSectionPos.getSectionCoord(pos.getX()) == ChunkSectionPos.getSectionCoord(this.centerX) && ChunkSectionPos.getSectionCoord(pos.getZ()) == ChunkSectionPos.getSectionCoord(this.centerZ);
        }

        public int getCenterX() {
            return this.centerX;
        }

        public int getCenterZ() {
            return this.centerZ;
        }

        public int getRadius() {
            return this.radius;
        }

        public int getHeight() {
            return this.height;
        }

        public boolean isGuarded() {
            return this.guarded;
        }

        public Box getBoundingBox() {
            return this.boundingBox;
        }
    }

    static class SpikeCache
    extends CacheLoader<Long, List<Spike>> {
        SpikeCache() {
        }

        @Override
        public List<Spike> load(Long long_) {
            IntArrayList intArrayList = Util.shuffle(IntStream.range(0, 10), Random.create(long_));
            ArrayList<Spike> list = Lists.newArrayList();
            for (int i = 0; i < 10; ++i) {
                int j = MathHelper.floor(42.0 * Math.cos(2.0 * (-Math.PI + 0.3141592653589793 * (double)i)));
                int k = MathHelper.floor(42.0 * Math.sin(2.0 * (-Math.PI + 0.3141592653589793 * (double)i)));
                int l = intArrayList.get(i);
                int m = 2 + l / 3;
                int n = 76 + l * 3;
                boolean bl = l == 1 || l == 2;
                list.add(new Spike(j, k, m, n, bl));
            }
            return list;
        }

        @Override
        public /* synthetic */ Object load(Object seed) throws Exception {
            return this.load((Long)seed);
        }
    }
}

