package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.CaveSurface;
import net.minecraft.world.gen.feature.util.DripstoneHelper;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.jetbrains.annotations.Nullable;

public class LargeDripstoneFeature extends Feature {
   public LargeDripstoneFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      LargeDripstoneFeatureConfig lv3 = (LargeDripstoneFeatureConfig)context.getConfig();
      Random lv4 = context.getRandom();
      if (!DripstoneHelper.canGenerate(lv, lv2)) {
         return false;
      } else {
         Optional optional = CaveSurface.create(lv, lv2, lv3.floorToCeilingSearchRange, DripstoneHelper::canGenerate, DripstoneHelper::canReplaceOrLava);
         if (optional.isPresent() && optional.get() instanceof CaveSurface.Bounded) {
            CaveSurface.Bounded lv5 = (CaveSurface.Bounded)optional.get();
            if (lv5.getHeight() < 4) {
               return false;
            } else {
               int i = (int)((float)lv5.getHeight() * lv3.maxColumnRadiusToCaveHeightRatio);
               int j = MathHelper.clamp(i, lv3.columnRadius.getMin(), lv3.columnRadius.getMax());
               int k = MathHelper.nextBetween(lv4, lv3.columnRadius.getMin(), j);
               DripstoneGenerator lv6 = createGenerator(lv2.withY(lv5.getCeiling() - 1), false, lv4, k, lv3.stalactiteBluntness, lv3.heightScale);
               DripstoneGenerator lv7 = createGenerator(lv2.withY(lv5.getFloor() + 1), true, lv4, k, lv3.stalagmiteBluntness, lv3.heightScale);
               WindModifier lv8;
               if (lv6.generateWind(lv3) && lv7.generateWind(lv3)) {
                  lv8 = new WindModifier(lv2.getY(), lv4, lv3.windSpeed);
               } else {
                  lv8 = LargeDripstoneFeature.WindModifier.create();
               }

               boolean bl = lv6.canGenerate(lv, lv8);
               boolean bl2 = lv7.canGenerate(lv, lv8);
               if (bl) {
                  lv6.generate(lv, lv4, lv8);
               }

               if (bl2) {
                  lv7.generate(lv, lv4, lv8);
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   private static DripstoneGenerator createGenerator(BlockPos pos, boolean isStalagmite, Random arg2, int scale, FloatProvider bluntness, FloatProvider heightScale) {
      return new DripstoneGenerator(pos, isStalagmite, scale, (double)bluntness.get(arg2), (double)heightScale.get(arg2));
   }

   private void testGeneration(StructureWorldAccess world, BlockPos pos, CaveSurface.Bounded surface, WindModifier wind) {
      world.setBlockState(wind.modify(pos.withY(surface.getCeiling() - 1)), Blocks.DIAMOND_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
      world.setBlockState(wind.modify(pos.withY(surface.getFloor() + 1)), Blocks.GOLD_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);

      for(BlockPos.Mutable lv = pos.withY(surface.getFloor() + 2).mutableCopy(); lv.getY() < surface.getCeiling() - 1; lv.move(Direction.UP)) {
         BlockPos lv2 = wind.modify(lv);
         if (DripstoneHelper.canGenerate(world, lv2) || world.getBlockState(lv2).isOf(Blocks.DRIPSTONE_BLOCK)) {
            world.setBlockState(lv2, Blocks.CREEPER_HEAD.getDefaultState(), Block.NOTIFY_LISTENERS);
         }
      }

   }

   static final class DripstoneGenerator {
      private BlockPos pos;
      private final boolean isStalagmite;
      private int scale;
      private final double bluntness;
      private final double heightScale;

      DripstoneGenerator(BlockPos pos, boolean isStalagmite, int scale, double bluntness, double heightScale) {
         this.pos = pos;
         this.isStalagmite = isStalagmite;
         this.scale = scale;
         this.bluntness = bluntness;
         this.heightScale = heightScale;
      }

      private int getBaseScale() {
         return this.scale(0.0F);
      }

      private int getBottomY() {
         return this.isStalagmite ? this.pos.getY() : this.pos.getY() - this.getBaseScale();
      }

      private int getTopY() {
         return !this.isStalagmite ? this.pos.getY() : this.pos.getY() + this.getBaseScale();
      }

      boolean canGenerate(StructureWorldAccess world, WindModifier wind) {
         while(this.scale > 1) {
            BlockPos.Mutable lv = this.pos.mutableCopy();
            int i = Math.min(10, this.getBaseScale());

            for(int j = 0; j < i; ++j) {
               if (world.getBlockState(lv).isOf(Blocks.LAVA)) {
                  return false;
               }

               if (DripstoneHelper.canGenerateBase(world, wind.modify(lv), this.scale)) {
                  this.pos = lv;
                  return true;
               }

               lv.move(this.isStalagmite ? Direction.DOWN : Direction.UP);
            }

            this.scale /= 2;
         }

         return false;
      }

      private int scale(float height) {
         return (int)DripstoneHelper.scaleHeightFromRadius((double)height, (double)this.scale, this.heightScale, this.bluntness);
      }

      void generate(StructureWorldAccess world, Random arg2, WindModifier wind) {
         for(int i = -this.scale; i <= this.scale; ++i) {
            for(int j = -this.scale; j <= this.scale; ++j) {
               float f = MathHelper.sqrt((float)(i * i + j * j));
               if (!(f > (float)this.scale)) {
                  int k = this.scale(f);
                  if (k > 0) {
                     if ((double)arg2.nextFloat() < 0.2) {
                        k = (int)((float)k * MathHelper.nextBetween(arg2, 0.8F, 1.0F));
                     }

                     BlockPos.Mutable lv = this.pos.add(i, 0, j).mutableCopy();
                     boolean bl = false;
                     int l = this.isStalagmite ? world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, lv.getX(), lv.getZ()) : Integer.MAX_VALUE;

                     for(int m = 0; m < k && lv.getY() < l; ++m) {
                        BlockPos lv2 = wind.modify(lv);
                        if (DripstoneHelper.canGenerateOrLava(world, lv2)) {
                           bl = true;
                           Block lv3 = Blocks.DRIPSTONE_BLOCK;
                           world.setBlockState(lv2, lv3.getDefaultState(), Block.NOTIFY_LISTENERS);
                        } else if (bl && world.getBlockState(lv2).isIn(BlockTags.BASE_STONE_OVERWORLD)) {
                           break;
                        }

                        lv.move(this.isStalagmite ? Direction.UP : Direction.DOWN);
                     }
                  }
               }
            }
         }

      }

      boolean generateWind(LargeDripstoneFeatureConfig config) {
         return this.scale >= config.minRadiusForWind && this.bluntness >= (double)config.minBluntnessForWind;
      }
   }

   static final class WindModifier {
      private final int y;
      @Nullable
      private final Vec3d wind;

      WindModifier(int y, Random arg, FloatProvider wind) {
         this.y = y;
         float f = wind.get(arg);
         float g = MathHelper.nextBetween(arg, 0.0F, 3.1415927F);
         this.wind = new Vec3d((double)(MathHelper.cos(g) * f), 0.0, (double)(MathHelper.sin(g) * f));
      }

      private WindModifier() {
         this.y = 0;
         this.wind = null;
      }

      static WindModifier create() {
         return new WindModifier();
      }

      BlockPos modify(BlockPos pos) {
         if (this.wind == null) {
            return pos;
         } else {
            int i = this.y - pos.getY();
            Vec3d lv = this.wind.multiply((double)i);
            return pos.add(MathHelper.floor(lv.x), 0, MathHelper.floor(lv.z));
         }
      }
   }
}
