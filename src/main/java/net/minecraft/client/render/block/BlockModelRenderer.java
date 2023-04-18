package net.minecraft.client.render.block;

import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BlockModelRenderer {
   private static final int field_32782 = 0;
   private static final int field_32783 = 1;
   static final Direction[] DIRECTIONS = Direction.values();
   private final BlockColors colors;
   private static final int BRIGHTNESS_CACHE_MAX_SIZE = 100;
   static final ThreadLocal BRIGHTNESS_CACHE = ThreadLocal.withInitial(BrightnessCache::new);

   public BlockModelRenderer(BlockColors colors) {
      this.colors = colors;
   }

   public void render(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
      boolean bl2 = MinecraftClient.isAmbientOcclusionEnabled() && state.getLuminance() == 0 && model.useAmbientOcclusion();
      Vec3d lv = state.getModelOffset(world, pos);
      matrices.translate(lv.x, lv.y, lv.z);

      try {
         if (bl2) {
            this.renderSmooth(world, model, state, pos, matrices, vertexConsumer, cull, random, seed, overlay);
         } else {
            this.renderFlat(world, model, state, pos, matrices, vertexConsumer, cull, random, seed, overlay);
         }

      } catch (Throwable var17) {
         CrashReport lv2 = CrashReport.create(var17, "Tesselating block model");
         CrashReportSection lv3 = lv2.addElement("Block model being tesselated");
         CrashReportSection.addBlockInfo(lv3, world, pos, state);
         lv3.add("Using AO", (Object)bl2);
         throw new CrashException(lv2);
      }
   }

   public void renderSmooth(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
      float[] fs = new float[DIRECTIONS.length * 2];
      BitSet bitSet = new BitSet(3);
      AmbientOcclusionCalculator lv = new AmbientOcclusionCalculator();
      BlockPos.Mutable lv2 = pos.mutableCopy();
      Direction[] var16 = DIRECTIONS;
      int var17 = var16.length;

      for(int var18 = 0; var18 < var17; ++var18) {
         Direction lv3 = var16[var18];
         random.setSeed(seed);
         List list = model.getQuads(state, lv3, random);
         if (!list.isEmpty()) {
            lv2.set(pos, (Direction)lv3);
            if (!cull || Block.shouldDrawSide(state, world, pos, lv3, lv2)) {
               this.renderQuadsSmooth(world, state, pos, matrices, vertexConsumer, list, fs, bitSet, lv, overlay);
            }
         }
      }

      random.setSeed(seed);
      List list2 = model.getQuads(state, (Direction)null, random);
      if (!list2.isEmpty()) {
         this.renderQuadsSmooth(world, state, pos, matrices, vertexConsumer, list2, fs, bitSet, lv, overlay);
      }

   }

   public void renderFlat(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
      BitSet bitSet = new BitSet(3);
      BlockPos.Mutable lv = pos.mutableCopy();
      Direction[] var14 = DIRECTIONS;
      int var15 = var14.length;

      for(int var16 = 0; var16 < var15; ++var16) {
         Direction lv2 = var14[var16];
         random.setSeed(seed);
         List list = model.getQuads(state, lv2, random);
         if (!list.isEmpty()) {
            lv.set(pos, (Direction)lv2);
            if (!cull || Block.shouldDrawSide(state, world, pos, lv2, lv)) {
               int j = WorldRenderer.getLightmapCoordinates(world, state, lv);
               this.renderQuadsFlat(world, state, pos, j, overlay, false, matrices, vertexConsumer, list, bitSet);
            }
         }
      }

      random.setSeed(seed);
      List list2 = model.getQuads(state, (Direction)null, random);
      if (!list2.isEmpty()) {
         this.renderQuadsFlat(world, state, pos, -1, overlay, true, matrices, vertexConsumer, list2, bitSet);
      }

   }

   private void renderQuadsSmooth(BlockRenderView world, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, List quads, float[] box, BitSet flags, AmbientOcclusionCalculator ambientOcclusionCalculator, int overlay) {
      Iterator var11 = quads.iterator();

      while(var11.hasNext()) {
         BakedQuad lv = (BakedQuad)var11.next();
         this.getQuadDimensions(world, state, pos, lv.getVertexData(), lv.getFace(), box, flags);
         ambientOcclusionCalculator.apply(world, state, pos, lv.getFace(), box, flags, lv.hasShade());
         this.renderQuad(world, state, pos, vertexConsumer, matrices.peek(), lv, ambientOcclusionCalculator.brightness[0], ambientOcclusionCalculator.brightness[1], ambientOcclusionCalculator.brightness[2], ambientOcclusionCalculator.brightness[3], ambientOcclusionCalculator.light[0], ambientOcclusionCalculator.light[1], ambientOcclusionCalculator.light[2], ambientOcclusionCalculator.light[3], overlay);
      }

   }

   private void renderQuad(BlockRenderView world, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad, float brightness0, float brightness1, float brightness2, float brightness3, int light0, int light1, int light2, int light3, int overlay) {
      float p;
      float q;
      float r;
      if (quad.hasColor()) {
         int o = this.colors.getColor(state, world, pos, quad.getColorIndex());
         p = (float)(o >> 16 & 255) / 255.0F;
         q = (float)(o >> 8 & 255) / 255.0F;
         r = (float)(o & 255) / 255.0F;
      } else {
         p = 1.0F;
         q = 1.0F;
         r = 1.0F;
      }

      vertexConsumer.quad(matrixEntry, quad, new float[]{brightness0, brightness1, brightness2, brightness3}, p, q, r, new int[]{light0, light1, light2, light3}, overlay, true);
   }

   private void getQuadDimensions(BlockRenderView world, BlockState state, BlockPos pos, int[] vertexData, Direction face, @Nullable float[] box, BitSet flags) {
      float f = 32.0F;
      float g = 32.0F;
      float h = 32.0F;
      float i = -32.0F;
      float j = -32.0F;
      float k = -32.0F;

      int l;
      float m;
      for(l = 0; l < 4; ++l) {
         m = Float.intBitsToFloat(vertexData[l * 8]);
         float n = Float.intBitsToFloat(vertexData[l * 8 + 1]);
         float o = Float.intBitsToFloat(vertexData[l * 8 + 2]);
         f = Math.min(f, m);
         g = Math.min(g, n);
         h = Math.min(h, o);
         i = Math.max(i, m);
         j = Math.max(j, n);
         k = Math.max(k, o);
      }

      if (box != null) {
         box[Direction.WEST.getId()] = f;
         box[Direction.EAST.getId()] = i;
         box[Direction.DOWN.getId()] = g;
         box[Direction.UP.getId()] = j;
         box[Direction.NORTH.getId()] = h;
         box[Direction.SOUTH.getId()] = k;
         l = DIRECTIONS.length;
         box[Direction.WEST.getId() + l] = 1.0F - f;
         box[Direction.EAST.getId() + l] = 1.0F - i;
         box[Direction.DOWN.getId() + l] = 1.0F - g;
         box[Direction.UP.getId() + l] = 1.0F - j;
         box[Direction.NORTH.getId() + l] = 1.0F - h;
         box[Direction.SOUTH.getId() + l] = 1.0F - k;
      }

      float p = 1.0E-4F;
      m = 0.9999F;
      switch (face) {
         case DOWN:
            flags.set(1, f >= 1.0E-4F || h >= 1.0E-4F || i <= 0.9999F || k <= 0.9999F);
            flags.set(0, g == j && (g < 1.0E-4F || state.isFullCube(world, pos)));
            break;
         case UP:
            flags.set(1, f >= 1.0E-4F || h >= 1.0E-4F || i <= 0.9999F || k <= 0.9999F);
            flags.set(0, g == j && (j > 0.9999F || state.isFullCube(world, pos)));
            break;
         case NORTH:
            flags.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
            flags.set(0, h == k && (h < 1.0E-4F || state.isFullCube(world, pos)));
            break;
         case SOUTH:
            flags.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
            flags.set(0, h == k && (k > 0.9999F || state.isFullCube(world, pos)));
            break;
         case WEST:
            flags.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
            flags.set(0, f == i && (f < 1.0E-4F || state.isFullCube(world, pos)));
            break;
         case EAST:
            flags.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
            flags.set(0, f == i && (i > 0.9999F || state.isFullCube(world, pos)));
      }

   }

   private void renderQuadsFlat(BlockRenderView world, BlockState state, BlockPos pos, int light, int overlay, boolean useWorldLight, MatrixStack matrices, VertexConsumer vertexConsumer, List quads, BitSet flags) {
      Iterator var11 = quads.iterator();

      while(var11.hasNext()) {
         BakedQuad lv = (BakedQuad)var11.next();
         if (useWorldLight) {
            this.getQuadDimensions(world, state, pos, lv.getVertexData(), lv.getFace(), (float[])null, flags);
            BlockPos lv2 = flags.get(0) ? pos.offset(lv.getFace()) : pos;
            light = WorldRenderer.getLightmapCoordinates(world, state, lv2);
         }

         float f = world.getBrightness(lv.getFace(), lv.hasShade());
         this.renderQuad(world, state, pos, vertexConsumer, matrices.peek(), lv, f, f, f, f, light, light, light, light, overlay);
      }

   }

   public void render(MatrixStack.Entry entry, VertexConsumer vertexConsumer, @Nullable BlockState state, BakedModel bakedModel, float red, float green, float blue, int light, int overlay) {
      Random lv = Random.create();
      long l = 42L;
      Direction[] var13 = DIRECTIONS;
      int var14 = var13.length;

      for(int var15 = 0; var15 < var14; ++var15) {
         Direction lv2 = var13[var15];
         lv.setSeed(42L);
         renderQuads(entry, vertexConsumer, red, green, blue, bakedModel.getQuads(state, lv2, lv), light, overlay);
      }

      lv.setSeed(42L);
      renderQuads(entry, vertexConsumer, red, green, blue, bakedModel.getQuads(state, (Direction)null, lv), light, overlay);
   }

   private static void renderQuads(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, List quads, int light, int overlay) {
      BakedQuad lv;
      float k;
      float l;
      float m;
      for(Iterator var8 = quads.iterator(); var8.hasNext(); vertexConsumer.quad(entry, lv, k, l, m, light, overlay)) {
         lv = (BakedQuad)var8.next();
         if (lv.hasColor()) {
            k = MathHelper.clamp(red, 0.0F, 1.0F);
            l = MathHelper.clamp(green, 0.0F, 1.0F);
            m = MathHelper.clamp(blue, 0.0F, 1.0F);
         } else {
            k = 1.0F;
            l = 1.0F;
            m = 1.0F;
         }
      }

   }

   public static void enableBrightnessCache() {
      ((BrightnessCache)BRIGHTNESS_CACHE.get()).enable();
   }

   public static void disableBrightnessCache() {
      ((BrightnessCache)BRIGHTNESS_CACHE.get()).disable();
   }

   @Environment(EnvType.CLIENT)
   static class AmbientOcclusionCalculator {
      final float[] brightness = new float[4];
      final int[] light = new int[4];

      public AmbientOcclusionCalculator() {
      }

      public void apply(BlockRenderView world, BlockState state, BlockPos pos, Direction direction, float[] box, BitSet flags, boolean shaded) {
         BlockPos lv = flags.get(0) ? pos.offset(direction) : pos;
         NeighborData lv2 = BlockModelRenderer.NeighborData.getData(direction);
         BlockPos.Mutable lv3 = new BlockPos.Mutable();
         BrightnessCache lv4 = (BrightnessCache)BlockModelRenderer.BRIGHTNESS_CACHE.get();
         lv3.set(lv, (Direction)lv2.faces[0]);
         BlockState lv5 = world.getBlockState(lv3);
         int i = lv4.getInt(lv5, world, lv3);
         float f = lv4.getFloat(lv5, world, lv3);
         lv3.set(lv, (Direction)lv2.faces[1]);
         BlockState lv6 = world.getBlockState(lv3);
         int j = lv4.getInt(lv6, world, lv3);
         float g = lv4.getFloat(lv6, world, lv3);
         lv3.set(lv, (Direction)lv2.faces[2]);
         BlockState lv7 = world.getBlockState(lv3);
         int k = lv4.getInt(lv7, world, lv3);
         float h = lv4.getFloat(lv7, world, lv3);
         lv3.set(lv, (Direction)lv2.faces[3]);
         BlockState lv8 = world.getBlockState(lv3);
         int l = lv4.getInt(lv8, world, lv3);
         float m = lv4.getFloat(lv8, world, lv3);
         BlockState lv9 = world.getBlockState(lv3.set(lv, (Direction)lv2.faces[0]).move(direction));
         boolean bl2 = !lv9.shouldBlockVision(world, lv3) || lv9.getOpacity(world, lv3) == 0;
         BlockState lv10 = world.getBlockState(lv3.set(lv, (Direction)lv2.faces[1]).move(direction));
         boolean bl3 = !lv10.shouldBlockVision(world, lv3) || lv10.getOpacity(world, lv3) == 0;
         BlockState lv11 = world.getBlockState(lv3.set(lv, (Direction)lv2.faces[2]).move(direction));
         boolean bl4 = !lv11.shouldBlockVision(world, lv3) || lv11.getOpacity(world, lv3) == 0;
         BlockState lv12 = world.getBlockState(lv3.set(lv, (Direction)lv2.faces[3]).move(direction));
         boolean bl5 = !lv12.shouldBlockVision(world, lv3) || lv12.getOpacity(world, lv3) == 0;
         float n;
         int o;
         BlockState lv13;
         if (!bl4 && !bl2) {
            n = f;
            o = i;
         } else {
            lv3.set(lv, (Direction)lv2.faces[0]).move(lv2.faces[2]);
            lv13 = world.getBlockState(lv3);
            n = lv4.getFloat(lv13, world, lv3);
            o = lv4.getInt(lv13, world, lv3);
         }

         float p;
         int q;
         if (!bl5 && !bl2) {
            p = f;
            q = i;
         } else {
            lv3.set(lv, (Direction)lv2.faces[0]).move(lv2.faces[3]);
            lv13 = world.getBlockState(lv3);
            p = lv4.getFloat(lv13, world, lv3);
            q = lv4.getInt(lv13, world, lv3);
         }

         float r;
         int s;
         if (!bl4 && !bl3) {
            r = f;
            s = i;
         } else {
            lv3.set(lv, (Direction)lv2.faces[1]).move(lv2.faces[2]);
            lv13 = world.getBlockState(lv3);
            r = lv4.getFloat(lv13, world, lv3);
            s = lv4.getInt(lv13, world, lv3);
         }

         float t;
         int u;
         if (!bl5 && !bl3) {
            t = f;
            u = i;
         } else {
            lv3.set(lv, (Direction)lv2.faces[1]).move(lv2.faces[3]);
            lv13 = world.getBlockState(lv3);
            t = lv4.getFloat(lv13, world, lv3);
            u = lv4.getInt(lv13, world, lv3);
         }

         int v = lv4.getInt(state, world, pos);
         lv3.set(pos, (Direction)direction);
         BlockState lv14 = world.getBlockState(lv3);
         if (flags.get(0) || !lv14.isOpaqueFullCube(world, lv3)) {
            v = lv4.getInt(lv14, world, lv3);
         }

         float w = flags.get(0) ? lv4.getFloat(world.getBlockState(lv), world, lv) : lv4.getFloat(world.getBlockState(pos), world, pos);
         Translation lv15 = BlockModelRenderer.Translation.getTranslations(direction);
         float x;
         float y;
         float z;
         float aa;
         if (flags.get(1) && lv2.nonCubicWeight) {
            x = (m + f + p + w) * 0.25F;
            y = (h + f + n + w) * 0.25F;
            z = (h + g + r + w) * 0.25F;
            aa = (m + g + t + w) * 0.25F;
            float ab = box[lv2.field_4192[0].shape] * box[lv2.field_4192[1].shape];
            float ac = box[lv2.field_4192[2].shape] * box[lv2.field_4192[3].shape];
            float ad = box[lv2.field_4192[4].shape] * box[lv2.field_4192[5].shape];
            float ae = box[lv2.field_4192[6].shape] * box[lv2.field_4192[7].shape];
            float af = box[lv2.field_4185[0].shape] * box[lv2.field_4185[1].shape];
            float ag = box[lv2.field_4185[2].shape] * box[lv2.field_4185[3].shape];
            float ah = box[lv2.field_4185[4].shape] * box[lv2.field_4185[5].shape];
            float ai = box[lv2.field_4185[6].shape] * box[lv2.field_4185[7].shape];
            float aj = box[lv2.field_4180[0].shape] * box[lv2.field_4180[1].shape];
            float ak = box[lv2.field_4180[2].shape] * box[lv2.field_4180[3].shape];
            float al = box[lv2.field_4180[4].shape] * box[lv2.field_4180[5].shape];
            float am = box[lv2.field_4180[6].shape] * box[lv2.field_4180[7].shape];
            float an = box[lv2.field_4188[0].shape] * box[lv2.field_4188[1].shape];
            float ao = box[lv2.field_4188[2].shape] * box[lv2.field_4188[3].shape];
            float ap = box[lv2.field_4188[4].shape] * box[lv2.field_4188[5].shape];
            float aq = box[lv2.field_4188[6].shape] * box[lv2.field_4188[7].shape];
            this.brightness[lv15.firstCorner] = x * ab + y * ac + z * ad + aa * ae;
            this.brightness[lv15.secondCorner] = x * af + y * ag + z * ah + aa * ai;
            this.brightness[lv15.thirdCorner] = x * aj + y * ak + z * al + aa * am;
            this.brightness[lv15.fourthCorner] = x * an + y * ao + z * ap + aa * aq;
            int ar = this.getAmbientOcclusionBrightness(l, i, q, v);
            int as = this.getAmbientOcclusionBrightness(k, i, o, v);
            int at = this.getAmbientOcclusionBrightness(k, j, s, v);
            int au = this.getAmbientOcclusionBrightness(l, j, u, v);
            this.light[lv15.firstCorner] = this.getBrightness(ar, as, at, au, ab, ac, ad, ae);
            this.light[lv15.secondCorner] = this.getBrightness(ar, as, at, au, af, ag, ah, ai);
            this.light[lv15.thirdCorner] = this.getBrightness(ar, as, at, au, aj, ak, al, am);
            this.light[lv15.fourthCorner] = this.getBrightness(ar, as, at, au, an, ao, ap, aq);
         } else {
            x = (m + f + p + w) * 0.25F;
            y = (h + f + n + w) * 0.25F;
            z = (h + g + r + w) * 0.25F;
            aa = (m + g + t + w) * 0.25F;
            this.light[lv15.firstCorner] = this.getAmbientOcclusionBrightness(l, i, q, v);
            this.light[lv15.secondCorner] = this.getAmbientOcclusionBrightness(k, i, o, v);
            this.light[lv15.thirdCorner] = this.getAmbientOcclusionBrightness(k, j, s, v);
            this.light[lv15.fourthCorner] = this.getAmbientOcclusionBrightness(l, j, u, v);
            this.brightness[lv15.firstCorner] = x;
            this.brightness[lv15.secondCorner] = y;
            this.brightness[lv15.thirdCorner] = z;
            this.brightness[lv15.fourthCorner] = aa;
         }

         x = world.getBrightness(direction, shaded);

         for(int av = 0; av < this.brightness.length; ++av) {
            float[] var10000 = this.brightness;
            var10000[av] *= x;
         }

      }

      private int getAmbientOcclusionBrightness(int i, int j, int k, int l) {
         if (i == 0) {
            i = l;
         }

         if (j == 0) {
            j = l;
         }

         if (k == 0) {
            k = l;
         }

         return i + j + k + l >> 2 & 16711935;
      }

      private int getBrightness(int i, int j, int k, int l, float f, float g, float h, float m) {
         int n = (int)((float)(i >> 16 & 255) * f + (float)(j >> 16 & 255) * g + (float)(k >> 16 & 255) * h + (float)(l >> 16 & 255) * m) & 255;
         int o = (int)((float)(i & 255) * f + (float)(j & 255) * g + (float)(k & 255) * h + (float)(l & 255) * m) & 255;
         return n << 16 | o;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class BrightnessCache {
      private boolean enabled;
      private final Long2IntLinkedOpenHashMap intCache = (Long2IntLinkedOpenHashMap)Util.make(() -> {
         Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25F) {
            protected void rehash(int newN) {
            }
         };
         long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
         return long2IntLinkedOpenHashMap;
      });
      private final Long2FloatLinkedOpenHashMap floatCache = (Long2FloatLinkedOpenHashMap)Util.make(() -> {
         Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25F) {
            protected void rehash(int newN) {
            }
         };
         long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
         return long2FloatLinkedOpenHashMap;
      });

      public void enable() {
         this.enabled = true;
      }

      public void disable() {
         this.enabled = false;
         this.intCache.clear();
         this.floatCache.clear();
      }

      public int getInt(BlockState state, BlockRenderView world, BlockPos pos) {
         long l = pos.asLong();
         int i;
         if (this.enabled) {
            i = this.intCache.get(l);
            if (i != Integer.MAX_VALUE) {
               return i;
            }
         }

         i = WorldRenderer.getLightmapCoordinates(world, state, pos);
         if (this.enabled) {
            if (this.intCache.size() == 100) {
               this.intCache.removeFirstInt();
            }

            this.intCache.put(l, i);
         }

         return i;
      }

      public float getFloat(BlockState state, BlockRenderView blockView, BlockPos pos) {
         long l = pos.asLong();
         float f;
         if (this.enabled) {
            f = this.floatCache.get(l);
            if (!Float.isNaN(f)) {
               return f;
            }
         }

         f = state.getAmbientOcclusionLightLevel(blockView, pos);
         if (this.enabled) {
            if (this.floatCache.size() == 100) {
               this.floatCache.removeFirstFloat();
            }

            this.floatCache.put(l, f);
         }

         return f;
      }
   }

   @Environment(EnvType.CLIENT)
   protected static enum NeighborData {
      DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5F, true, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.SOUTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.NORTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.NORTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.SOUTH}),
      UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0F, true, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.SOUTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.NORTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.NORTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.SOUTH}),
      NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8F, true, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_WEST}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_EAST}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_EAST}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_WEST}),
      SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8F, true, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.WEST}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_WEST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.WEST, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.WEST}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.EAST}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_EAST, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.EAST, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.EAST}),
      WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.SOUTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.NORTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.NORTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.SOUTH}),
      EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.SOUTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.FLIP_DOWN, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.DOWN, BlockModelRenderer.NeighborOrientation.NORTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.NORTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_NORTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.NORTH}, new NeighborOrientation[]{BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.SOUTH, BlockModelRenderer.NeighborOrientation.FLIP_UP, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.FLIP_SOUTH, BlockModelRenderer.NeighborOrientation.UP, BlockModelRenderer.NeighborOrientation.SOUTH});

      final Direction[] faces;
      final boolean nonCubicWeight;
      final NeighborOrientation[] field_4192;
      final NeighborOrientation[] field_4185;
      final NeighborOrientation[] field_4180;
      final NeighborOrientation[] field_4188;
      private static final NeighborData[] VALUES = (NeighborData[])Util.make(new NeighborData[6], (values) -> {
         values[Direction.DOWN.getId()] = DOWN;
         values[Direction.UP.getId()] = UP;
         values[Direction.NORTH.getId()] = NORTH;
         values[Direction.SOUTH.getId()] = SOUTH;
         values[Direction.WEST.getId()] = WEST;
         values[Direction.EAST.getId()] = EAST;
      });

      private NeighborData(Direction[] faces, float f, boolean nonCubicWeight, NeighborOrientation[] args2, NeighborOrientation[] args3, NeighborOrientation[] args4, NeighborOrientation[] args5) {
         this.faces = faces;
         this.nonCubicWeight = nonCubicWeight;
         this.field_4192 = args2;
         this.field_4185 = args3;
         this.field_4180 = args4;
         this.field_4188 = args5;
      }

      public static NeighborData getData(Direction direction) {
         return VALUES[direction.getId()];
      }

      // $FF: synthetic method
      private static NeighborData[] method_36917() {
         return new NeighborData[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
      }
   }

   @Environment(EnvType.CLIENT)
   protected static enum NeighborOrientation {
      DOWN(Direction.DOWN, false),
      UP(Direction.UP, false),
      NORTH(Direction.NORTH, false),
      SOUTH(Direction.SOUTH, false),
      WEST(Direction.WEST, false),
      EAST(Direction.EAST, false),
      FLIP_DOWN(Direction.DOWN, true),
      FLIP_UP(Direction.UP, true),
      FLIP_NORTH(Direction.NORTH, true),
      FLIP_SOUTH(Direction.SOUTH, true),
      FLIP_WEST(Direction.WEST, true),
      FLIP_EAST(Direction.EAST, true);

      final int shape;

      private NeighborOrientation(Direction direction, boolean flip) {
         this.shape = direction.getId() + (flip ? BlockModelRenderer.DIRECTIONS.length : 0);
      }

      // $FF: synthetic method
      private static NeighborOrientation[] method_36919() {
         return new NeighborOrientation[]{DOWN, UP, NORTH, SOUTH, WEST, EAST, FLIP_DOWN, FLIP_UP, FLIP_NORTH, FLIP_SOUTH, FLIP_WEST, FLIP_EAST};
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum Translation {
      DOWN(0, 1, 2, 3),
      UP(2, 3, 0, 1),
      NORTH(3, 0, 1, 2),
      SOUTH(0, 1, 2, 3),
      WEST(3, 0, 1, 2),
      EAST(1, 2, 3, 0);

      final int firstCorner;
      final int secondCorner;
      final int thirdCorner;
      final int fourthCorner;
      private static final Translation[] VALUES = (Translation[])Util.make(new Translation[6], (values) -> {
         values[Direction.DOWN.getId()] = DOWN;
         values[Direction.UP.getId()] = UP;
         values[Direction.NORTH.getId()] = NORTH;
         values[Direction.SOUTH.getId()] = SOUTH;
         values[Direction.WEST.getId()] = WEST;
         values[Direction.EAST.getId()] = EAST;
      });

      private Translation(int firstCorner, int secondCorner, int thirdCorner, int fourthCorner) {
         this.firstCorner = firstCorner;
         this.secondCorner = secondCorner;
         this.thirdCorner = thirdCorner;
         this.fourthCorner = fourthCorner;
      }

      public static Translation getTranslations(Direction direction) {
         return VALUES[direction.getId()];
      }

      // $FF: synthetic method
      private static Translation[] method_36918() {
         return new Translation[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
      }
   }
}
