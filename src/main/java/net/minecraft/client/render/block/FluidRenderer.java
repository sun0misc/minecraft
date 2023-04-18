package net.minecraft.client.render.block;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;

@Environment(EnvType.CLIENT)
public class FluidRenderer {
   private static final float field_32781 = 0.8888889F;
   private final Sprite[] lavaSprites = new Sprite[2];
   private final Sprite[] waterSprites = new Sprite[2];
   private Sprite waterOverlaySprite;

   protected void onResourceReload() {
      this.lavaSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.LAVA.getDefaultState()).getParticleSprite();
      this.lavaSprites[1] = ModelLoader.LAVA_FLOW.getSprite();
      this.waterSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();
      this.waterSprites[1] = ModelLoader.WATER_FLOW.getSprite();
      this.waterOverlaySprite = ModelLoader.WATER_OVERLAY.getSprite();
   }

   private static boolean isSameFluid(FluidState a, FluidState b) {
      return b.getFluid().matchesType(a.getFluid());
   }

   private static boolean isSideCovered(BlockView world, Direction direction, float height, BlockPos pos, BlockState state) {
      if (state.isOpaque()) {
         VoxelShape lv = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, (double)height, 1.0);
         VoxelShape lv2 = state.getCullingShape(world, pos);
         return VoxelShapes.isSideCovered(lv, lv2, direction);
      } else {
         return false;
      }
   }

   private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, BlockState state) {
      return isSideCovered(world, direction, maxDeviation, pos.offset(direction), state);
   }

   private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
      return isSideCovered(world, direction.getOpposite(), 1.0F, pos, state);
   }

   public static boolean shouldRenderSide(BlockRenderView world, BlockPos pos, FluidState fluidState, BlockState blockState, Direction direction, FluidState neighborFluidState) {
      return !isOppositeSideCovered(world, pos, blockState, direction) && !isSameFluid(fluidState, neighborFluidState);
   }

   public void render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
      boolean bl = fluidState.isIn(FluidTags.LAVA);
      Sprite[] lvs = bl ? this.lavaSprites : this.waterSprites;
      int i = bl ? 16777215 : BiomeColors.getWaterColor(world, pos);
      float f = (float)(i >> 16 & 255) / 255.0F;
      float g = (float)(i >> 8 & 255) / 255.0F;
      float h = (float)(i & 255) / 255.0F;
      BlockState lv = world.getBlockState(pos.offset(Direction.DOWN));
      FluidState lv2 = lv.getFluidState();
      BlockState lv3 = world.getBlockState(pos.offset(Direction.UP));
      FluidState lv4 = lv3.getFluidState();
      BlockState lv5 = world.getBlockState(pos.offset(Direction.NORTH));
      FluidState lv6 = lv5.getFluidState();
      BlockState lv7 = world.getBlockState(pos.offset(Direction.SOUTH));
      FluidState lv8 = lv7.getFluidState();
      BlockState lv9 = world.getBlockState(pos.offset(Direction.WEST));
      FluidState lv10 = lv9.getFluidState();
      BlockState lv11 = world.getBlockState(pos.offset(Direction.EAST));
      FluidState lv12 = lv11.getFluidState();
      boolean bl2 = !isSameFluid(fluidState, lv4);
      boolean bl3 = shouldRenderSide(world, pos, fluidState, blockState, Direction.DOWN, lv2) && !isSideCovered(world, pos, Direction.DOWN, 0.8888889F, lv);
      boolean bl4 = shouldRenderSide(world, pos, fluidState, blockState, Direction.NORTH, lv6);
      boolean bl5 = shouldRenderSide(world, pos, fluidState, blockState, Direction.SOUTH, lv8);
      boolean bl6 = shouldRenderSide(world, pos, fluidState, blockState, Direction.WEST, lv10);
      boolean bl7 = shouldRenderSide(world, pos, fluidState, blockState, Direction.EAST, lv12);
      if (bl2 || bl3 || bl7 || bl6 || bl4 || bl5) {
         float j = world.getBrightness(Direction.DOWN, true);
         float k = world.getBrightness(Direction.UP, true);
         float l = world.getBrightness(Direction.NORTH, true);
         float m = world.getBrightness(Direction.WEST, true);
         Fluid lv13 = fluidState.getFluid();
         float n = this.getFluidHeight(world, lv13, pos, blockState, fluidState);
         float o;
         float p;
         float q;
         float r;
         if (n >= 1.0F) {
            o = 1.0F;
            p = 1.0F;
            q = 1.0F;
            r = 1.0F;
         } else {
            float s = this.getFluidHeight(world, lv13, pos.north(), lv5, lv6);
            float t = this.getFluidHeight(world, lv13, pos.south(), lv7, lv8);
            float u = this.getFluidHeight(world, lv13, pos.east(), lv11, lv12);
            float v = this.getFluidHeight(world, lv13, pos.west(), lv9, lv10);
            o = this.calculateFluidHeight(world, lv13, n, s, u, pos.offset(Direction.NORTH).offset(Direction.EAST));
            p = this.calculateFluidHeight(world, lv13, n, s, v, pos.offset(Direction.NORTH).offset(Direction.WEST));
            q = this.calculateFluidHeight(world, lv13, n, t, u, pos.offset(Direction.SOUTH).offset(Direction.EAST));
            r = this.calculateFluidHeight(world, lv13, n, t, v, pos.offset(Direction.SOUTH).offset(Direction.WEST));
         }

         double d = (double)(pos.getX() & 15);
         double e = (double)(pos.getY() & 15);
         double w = (double)(pos.getZ() & 15);
         float x = 0.001F;
         float y = bl3 ? 0.001F : 0.0F;
         float z;
         float ab;
         float ad;
         float af;
         float aa;
         float ac;
         float ae;
         float ag;
         if (bl2 && !isSideCovered(world, pos, Direction.UP, Math.min(Math.min(p, r), Math.min(q, o)), lv3)) {
            p -= 0.001F;
            r -= 0.001F;
            q -= 0.001F;
            o -= 0.001F;
            Vec3d lv14 = fluidState.getVelocity(world, pos);
            Sprite lv15;
            float ah;
            float ai;
            float ak;
            if (lv14.x == 0.0 && lv14.z == 0.0) {
               lv15 = lvs[0];
               z = lv15.getFrameU(0.0);
               aa = lv15.getFrameV(0.0);
               ab = z;
               ac = lv15.getFrameV(16.0);
               ad = lv15.getFrameU(16.0);
               ae = ac;
               af = ad;
               ag = aa;
            } else {
               lv15 = lvs[1];
               ah = (float)MathHelper.atan2(lv14.z, lv14.x) - 1.5707964F;
               ai = MathHelper.sin(ah) * 0.25F;
               float aj = MathHelper.cos(ah) * 0.25F;
               ak = 8.0F;
               z = lv15.getFrameU((double)(8.0F + (-aj - ai) * 16.0F));
               aa = lv15.getFrameV((double)(8.0F + (-aj + ai) * 16.0F));
               ab = lv15.getFrameU((double)(8.0F + (-aj + ai) * 16.0F));
               ac = lv15.getFrameV((double)(8.0F + (aj + ai) * 16.0F));
               ad = lv15.getFrameU((double)(8.0F + (aj + ai) * 16.0F));
               ae = lv15.getFrameV((double)(8.0F + (aj - ai) * 16.0F));
               af = lv15.getFrameU((double)(8.0F + (aj - ai) * 16.0F));
               ag = lv15.getFrameV((double)(8.0F + (-aj - ai) * 16.0F));
            }

            float al = (z + ab + ad + af) / 4.0F;
            ah = (aa + ac + ae + ag) / 4.0F;
            ai = lvs[0].getAnimationFrameDelta();
            z = MathHelper.lerp(ai, z, al);
            ab = MathHelper.lerp(ai, ab, al);
            ad = MathHelper.lerp(ai, ad, al);
            af = MathHelper.lerp(ai, af, al);
            aa = MathHelper.lerp(ai, aa, ah);
            ac = MathHelper.lerp(ai, ac, ah);
            ae = MathHelper.lerp(ai, ae, ah);
            ag = MathHelper.lerp(ai, ag, ah);
            int am = this.getLight(world, pos);
            ak = k * f;
            float an = k * g;
            float ao = k * h;
            this.vertex(vertexConsumer, d + 0.0, e + (double)p, w + 0.0, ak, an, ao, z, aa, am);
            this.vertex(vertexConsumer, d + 0.0, e + (double)r, w + 1.0, ak, an, ao, ab, ac, am);
            this.vertex(vertexConsumer, d + 1.0, e + (double)q, w + 1.0, ak, an, ao, ad, ae, am);
            this.vertex(vertexConsumer, d + 1.0, e + (double)o, w + 0.0, ak, an, ao, af, ag, am);
            if (fluidState.method_15756(world, pos.up())) {
               this.vertex(vertexConsumer, d + 0.0, e + (double)p, w + 0.0, ak, an, ao, z, aa, am);
               this.vertex(vertexConsumer, d + 1.0, e + (double)o, w + 0.0, ak, an, ao, af, ag, am);
               this.vertex(vertexConsumer, d + 1.0, e + (double)q, w + 1.0, ak, an, ao, ad, ae, am);
               this.vertex(vertexConsumer, d + 0.0, e + (double)r, w + 1.0, ak, an, ao, ab, ac, am);
            }
         }

         if (bl3) {
            z = lvs[0].getMinU();
            ab = lvs[0].getMaxU();
            ad = lvs[0].getMinV();
            af = lvs[0].getMaxV();
            int ap = this.getLight(world, pos.down());
            ac = j * f;
            ae = j * g;
            ag = j * h;
            this.vertex(vertexConsumer, d, e + (double)y, w + 1.0, ac, ae, ag, z, af, ap);
            this.vertex(vertexConsumer, d, e + (double)y, w, ac, ae, ag, z, ad, ap);
            this.vertex(vertexConsumer, d + 1.0, e + (double)y, w, ac, ae, ag, ab, ad, ap);
            this.vertex(vertexConsumer, d + 1.0, e + (double)y, w + 1.0, ac, ae, ag, ab, af, ap);
         }

         int aq = this.getLight(world, pos);
         Iterator var76 = Direction.Type.HORIZONTAL.iterator();

         while(true) {
            Direction lv16;
            double ar;
            double at;
            double as;
            double au;
            boolean bl8;
            do {
               do {
                  if (!var76.hasNext()) {
                     return;
                  }

                  lv16 = (Direction)var76.next();
                  switch (lv16) {
                     case NORTH:
                        af = p;
                        aa = o;
                        ar = d;
                        as = d + 1.0;
                        at = w + 0.0010000000474974513;
                        au = w + 0.0010000000474974513;
                        bl8 = bl4;
                        break;
                     case SOUTH:
                        af = q;
                        aa = r;
                        ar = d + 1.0;
                        as = d;
                        at = w + 1.0 - 0.0010000000474974513;
                        au = w + 1.0 - 0.0010000000474974513;
                        bl8 = bl5;
                        break;
                     case WEST:
                        af = r;
                        aa = p;
                        ar = d + 0.0010000000474974513;
                        as = d + 0.0010000000474974513;
                        at = w + 1.0;
                        au = w;
                        bl8 = bl6;
                        break;
                     default:
                        af = o;
                        aa = q;
                        ar = d + 1.0 - 0.0010000000474974513;
                        as = d + 1.0 - 0.0010000000474974513;
                        at = w;
                        au = w + 1.0;
                        bl8 = bl7;
                  }
               } while(!bl8);
            } while(isSideCovered(world, pos, lv16, Math.max(af, aa), world.getBlockState(pos.offset(lv16))));

            BlockPos lv17 = pos.offset(lv16);
            Sprite lv18 = lvs[1];
            if (!bl) {
               Block lv19 = world.getBlockState(lv17).getBlock();
               if (lv19 instanceof TransparentBlock || lv19 instanceof LeavesBlock) {
                  lv18 = this.waterOverlaySprite;
               }
            }

            float av = lv18.getFrameU(0.0);
            float aw = lv18.getFrameU(8.0);
            float ax = lv18.getFrameV((double)((1.0F - af) * 16.0F * 0.5F));
            float ay = lv18.getFrameV((double)((1.0F - aa) * 16.0F * 0.5F));
            float az = lv18.getFrameV(8.0);
            float ba = lv16.getAxis() == Direction.Axis.Z ? l : m;
            float bb = k * ba * f;
            float bc = k * ba * g;
            float bd = k * ba * h;
            this.vertex(vertexConsumer, ar, e + (double)af, at, bb, bc, bd, av, ax, aq);
            this.vertex(vertexConsumer, as, e + (double)aa, au, bb, bc, bd, aw, ay, aq);
            this.vertex(vertexConsumer, as, e + (double)y, au, bb, bc, bd, aw, az, aq);
            this.vertex(vertexConsumer, ar, e + (double)y, at, bb, bc, bd, av, az, aq);
            if (lv18 != this.waterOverlaySprite) {
               this.vertex(vertexConsumer, ar, e + (double)y, at, bb, bc, bd, av, az, aq);
               this.vertex(vertexConsumer, as, e + (double)y, au, bb, bc, bd, aw, az, aq);
               this.vertex(vertexConsumer, as, e + (double)aa, au, bb, bc, bd, aw, ay, aq);
               this.vertex(vertexConsumer, ar, e + (double)af, at, bb, bc, bd, av, ax, aq);
            }
         }
      }
   }

   private float calculateFluidHeight(BlockRenderView world, Fluid fluid, float originHeight, float northSouthHeight, float eastWestHeight, BlockPos pos) {
      if (!(eastWestHeight >= 1.0F) && !(northSouthHeight >= 1.0F)) {
         float[] fs = new float[2];
         if (eastWestHeight > 0.0F || northSouthHeight > 0.0F) {
            float i = this.getFluidHeight(world, fluid, pos);
            if (i >= 1.0F) {
               return 1.0F;
            }

            this.addHeight(fs, i);
         }

         this.addHeight(fs, originHeight);
         this.addHeight(fs, eastWestHeight);
         this.addHeight(fs, northSouthHeight);
         return fs[0] / fs[1];
      } else {
         return 1.0F;
      }
   }

   private void addHeight(float[] weightedAverageHeight, float height) {
      if (height >= 0.8F) {
         weightedAverageHeight[0] += height * 10.0F;
         weightedAverageHeight[1] += 10.0F;
      } else if (height >= 0.0F) {
         weightedAverageHeight[0] += height;
         int var10002 = weightedAverageHeight[1]++;
      }

   }

   private float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      return this.getFluidHeight(world, fluid, pos, lv, lv.getFluidState());
   }

   private float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
      if (fluid.matchesType(fluidState.getFluid())) {
         BlockState lv = world.getBlockState(pos.up());
         return fluid.matchesType(lv.getFluidState().getFluid()) ? 1.0F : fluidState.getHeight();
      } else {
         return !blockState.getMaterial().isSolid() ? 0.0F : -1.0F;
      }
   }

   private void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light) {
      vertexConsumer.vertex(x, y, z).color(red, green, blue, 1.0F).texture(u, v).light(light).normal(0.0F, 1.0F, 0.0F).next();
   }

   private int getLight(BlockRenderView world, BlockPos pos) {
      int i = WorldRenderer.getLightmapCoordinates(world, pos);
      int j = WorldRenderer.getLightmapCoordinates(world, pos.up());
      int k = i & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
      int l = j & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
      int m = i >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
      int n = j >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
      return (k > l ? k : l) | (m > n ? m : n) << 16;
   }
}
