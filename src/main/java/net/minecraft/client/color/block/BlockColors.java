package net.minecraft.client.color.block;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BlockColors {
   private static final int NO_COLOR = -1;
   private final IdList providers = new IdList(32);
   private final Map properties = Maps.newHashMap();

   public static BlockColors create() {
      BlockColors lv = new BlockColors();
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return world != null && pos != null ? BiomeColors.getGrassColor(world, state.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos) : GrassColors.getDefaultColor();
      }, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      lv.registerColorProperty(TallPlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return world != null && pos != null ? BiomeColors.getGrassColor(world, pos) : GrassColors.getDefaultColor();
      }, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         if (tintIndex != 0) {
            return world != null && pos != null ? BiomeColors.getGrassColor(world, pos) : GrassColors.getDefaultColor();
         } else {
            return -1;
         }
      }, Blocks.PINK_PETALS);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return FoliageColors.getSpruceColor();
      }, Blocks.SPRUCE_LEAVES);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return FoliageColors.getBirchColor();
      }, Blocks.BIRCH_LEAVES);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return world != null && pos != null ? BiomeColors.getFoliageColor(world, pos) : FoliageColors.getDefaultColor();
      }, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE, Blocks.MANGROVE_LEAVES);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return world != null && pos != null ? BiomeColors.getWaterColor(world, pos) : -1;
      }, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.WATER_CAULDRON);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return RedstoneWireBlock.getWireColor((Integer)state.get(RedstoneWireBlock.POWER));
      }, Blocks.REDSTONE_WIRE);
      lv.registerColorProperty(RedstoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return world != null && pos != null ? BiomeColors.getGrassColor(world, pos) : -1;
      }, Blocks.SUGAR_CANE);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return 14731036;
      }, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         int j = (Integer)state.get(StemBlock.AGE);
         int k = j * 32;
         int l = 255 - j * 8;
         int m = j * 4;
         return k << 16 | l << 8 | m;
      }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      lv.registerColorProperty(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      lv.registerColorProvider((state, world, pos, tintIndex) -> {
         return world != null && pos != null ? 2129968 : 7455580;
      }, Blocks.LILY_PAD);
      return lv;
   }

   public int getParticleColor(BlockState state, World world, BlockPos pos) {
      BlockColorProvider lv = (BlockColorProvider)this.providers.get(Registries.BLOCK.getRawId(state.getBlock()));
      if (lv != null) {
         return lv.getColor(state, (BlockRenderView)null, (BlockPos)null, 0);
      } else {
         MapColor lv2 = state.getMapColor(world, pos);
         return lv2 != null ? lv2.color : -1;
      }
   }

   public int getColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex) {
      BlockColorProvider lv = (BlockColorProvider)this.providers.get(Registries.BLOCK.getRawId(state.getBlock()));
      return lv == null ? -1 : lv.getColor(state, world, pos, tintIndex);
   }

   public void registerColorProvider(BlockColorProvider provider, Block... blocks) {
      Block[] var3 = blocks;
      int var4 = blocks.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Block lv = var3[var5];
         this.providers.set(provider, Registries.BLOCK.getRawId(lv));
      }

   }

   private void registerColorProperties(Set properties, Block... blocks) {
      Block[] var3 = blocks;
      int var4 = blocks.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Block lv = var3[var5];
         this.properties.put(lv, properties);
      }

   }

   private void registerColorProperty(Property property, Block... blocks) {
      this.registerColorProperties(ImmutableSet.of(property), blocks);
   }

   public Set getProperties(Block block) {
      return (Set)this.properties.getOrDefault(block, ImmutableSet.of());
   }
}
