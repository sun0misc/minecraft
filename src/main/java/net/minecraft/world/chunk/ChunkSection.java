package net.minecraft.world.chunk;

import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public class ChunkSection {
   public static final int field_31406 = 16;
   public static final int field_31407 = 16;
   public static final int field_31408 = 4096;
   public static final int field_34555 = 2;
   private final int yOffset;
   private short nonEmptyBlockCount;
   private short randomTickableBlockCount;
   private short nonEmptyFluidCount;
   private final PalettedContainer blockStateContainer;
   private ReadableContainer biomeContainer;

   public ChunkSection(int chunkPos, PalettedContainer blockStateContainer, ReadableContainer biomeContainer) {
      this.yOffset = blockCoordFromChunkCoord(chunkPos);
      this.blockStateContainer = blockStateContainer;
      this.biomeContainer = biomeContainer;
      this.calculateCounts();
   }

   public ChunkSection(int chunkPos, Registry biomeRegistry) {
      this.yOffset = blockCoordFromChunkCoord(chunkPos);
      this.blockStateContainer = new PalettedContainer(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
      this.biomeContainer = new PalettedContainer(biomeRegistry.getIndexedEntries(), biomeRegistry.entryOf(BiomeKeys.PLAINS), PalettedContainer.PaletteProvider.BIOME);
   }

   public static int blockCoordFromChunkCoord(int chunkPos) {
      return chunkPos << 4;
   }

   public BlockState getBlockState(int x, int y, int z) {
      return (BlockState)this.blockStateContainer.get(x, y, z);
   }

   public FluidState getFluidState(int x, int y, int z) {
      return ((BlockState)this.blockStateContainer.get(x, y, z)).getFluidState();
   }

   public void lock() {
      this.blockStateContainer.lock();
   }

   public void unlock() {
      this.blockStateContainer.unlock();
   }

   public BlockState setBlockState(int x, int y, int z, BlockState state) {
      return this.setBlockState(x, y, z, state, true);
   }

   public BlockState setBlockState(int x, int y, int z, BlockState state, boolean lock) {
      BlockState lv;
      if (lock) {
         lv = (BlockState)this.blockStateContainer.swap(x, y, z, state);
      } else {
         lv = (BlockState)this.blockStateContainer.swapUnsafe(x, y, z, state);
      }

      FluidState lv2 = lv.getFluidState();
      FluidState lv3 = state.getFluidState();
      if (!lv.isAir()) {
         --this.nonEmptyBlockCount;
         if (lv.hasRandomTicks()) {
            --this.randomTickableBlockCount;
         }
      }

      if (!lv2.isEmpty()) {
         --this.nonEmptyFluidCount;
      }

      if (!state.isAir()) {
         ++this.nonEmptyBlockCount;
         if (state.hasRandomTicks()) {
            ++this.randomTickableBlockCount;
         }
      }

      if (!lv3.isEmpty()) {
         ++this.nonEmptyFluidCount;
      }

      return lv;
   }

   public boolean isEmpty() {
      return this.nonEmptyBlockCount == 0;
   }

   public boolean hasRandomTicks() {
      return this.hasRandomBlockTicks() || this.hasRandomFluidTicks();
   }

   public boolean hasRandomBlockTicks() {
      return this.randomTickableBlockCount > 0;
   }

   public boolean hasRandomFluidTicks() {
      return this.nonEmptyFluidCount > 0;
   }

   public int getYOffset() {
      return this.yOffset;
   }

   public void calculateCounts() {
      class BlockStateCounter implements PalettedContainer.Counter {
         public int nonEmptyBlockCount;
         public int randomTickableBlockCount;
         public int nonEmptyFluidCount;

         public void accept(BlockState arg, int i) {
            FluidState lv = arg.getFluidState();
            if (!arg.isAir()) {
               this.nonEmptyBlockCount += i;
               if (arg.hasRandomTicks()) {
                  this.randomTickableBlockCount += i;
               }
            }

            if (!lv.isEmpty()) {
               this.nonEmptyBlockCount += i;
               if (lv.hasRandomTicks()) {
                  this.nonEmptyFluidCount += i;
               }
            }

         }

         // $FF: synthetic method
         public void accept(Object object, int i) {
            this.accept((BlockState)object, i);
         }
      }

      BlockStateCounter lv = new BlockStateCounter();
      this.blockStateContainer.count(lv);
      this.nonEmptyBlockCount = (short)lv.nonEmptyBlockCount;
      this.randomTickableBlockCount = (short)lv.randomTickableBlockCount;
      this.nonEmptyFluidCount = (short)lv.nonEmptyFluidCount;
   }

   public PalettedContainer getBlockStateContainer() {
      return this.blockStateContainer;
   }

   public ReadableContainer getBiomeContainer() {
      return this.biomeContainer;
   }

   public void readDataPacket(PacketByteBuf buf) {
      this.nonEmptyBlockCount = buf.readShort();
      this.blockStateContainer.readPacket(buf);
      PalettedContainer lv = this.biomeContainer.slice();
      lv.readPacket(buf);
      this.biomeContainer = lv;
   }

   public void readBiomePacket(PacketByteBuf buf) {
      PalettedContainer lv = this.biomeContainer.slice();
      lv.readPacket(buf);
      this.biomeContainer = lv;
   }

   public void toPacket(PacketByteBuf buf) {
      buf.writeShort(this.nonEmptyBlockCount);
      this.blockStateContainer.writePacket(buf);
      this.biomeContainer.writePacket(buf);
   }

   public int getPacketSize() {
      return 2 + this.blockStateContainer.getPacketSize() + this.biomeContainer.getPacketSize();
   }

   public boolean hasAny(Predicate predicate) {
      return this.blockStateContainer.hasAny(predicate);
   }

   public RegistryEntry getBiome(int x, int y, int z) {
      return (RegistryEntry)this.biomeContainer.get(x, y, z);
   }

   public void populateBiomes(BiomeSupplier biomeSupplier, MultiNoiseUtil.MultiNoiseSampler sampler, int x, int z) {
      PalettedContainer lv = this.biomeContainer.slice();
      int k = BiomeCoords.fromBlock(this.getYOffset());
      int l = true;

      for(int m = 0; m < 4; ++m) {
         for(int n = 0; n < 4; ++n) {
            for(int o = 0; o < 4; ++o) {
               lv.swapUnsafe(m, n, o, biomeSupplier.getBiome(x + m, k + n, z + o, sampler));
            }
         }
      }

      this.biomeContainer = lv;
   }
}
