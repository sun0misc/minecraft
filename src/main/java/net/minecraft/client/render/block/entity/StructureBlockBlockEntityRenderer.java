package net.minecraft.client.render.block.entity;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;

@Environment(EnvType.CLIENT)
public class StructureBlockBlockEntityRenderer implements BlockEntityRenderer {
   public StructureBlockBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
   }

   public void render(StructureBlockBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      if (MinecraftClient.getInstance().player.isCreativeLevelTwoOp() || MinecraftClient.getInstance().player.isSpectator()) {
         BlockPos lv = arg.getOffset();
         Vec3i lv2 = arg.getSize();
         if (lv2.getX() >= 1 && lv2.getY() >= 1 && lv2.getZ() >= 1) {
            if (arg.getMode() == StructureBlockMode.SAVE || arg.getMode() == StructureBlockMode.LOAD) {
               double d = (double)lv.getX();
               double e = (double)lv.getZ();
               double g = (double)lv.getY();
               double h = g + (double)lv2.getY();
               double k;
               double l;
               switch (arg.getMirror()) {
                  case LEFT_RIGHT:
                     k = (double)lv2.getX();
                     l = (double)(-lv2.getZ());
                     break;
                  case FRONT_BACK:
                     k = (double)(-lv2.getX());
                     l = (double)lv2.getZ();
                     break;
                  default:
                     k = (double)lv2.getX();
                     l = (double)lv2.getZ();
               }

               double m;
               double n;
               double o;
               double p;
               switch (arg.getRotation()) {
                  case CLOCKWISE_90:
                     m = l < 0.0 ? d : d + 1.0;
                     n = k < 0.0 ? e + 1.0 : e;
                     o = m - l;
                     p = n + k;
                     break;
                  case CLOCKWISE_180:
                     m = k < 0.0 ? d : d + 1.0;
                     n = l < 0.0 ? e : e + 1.0;
                     o = m - k;
                     p = n - l;
                     break;
                  case COUNTERCLOCKWISE_90:
                     m = l < 0.0 ? d + 1.0 : d;
                     n = k < 0.0 ? e : e + 1.0;
                     o = m + l;
                     p = n - k;
                     break;
                  default:
                     m = k < 0.0 ? d + 1.0 : d;
                     n = l < 0.0 ? e + 1.0 : e;
                     o = m + k;
                     p = n + l;
               }

               float q = 1.0F;
               float r = 0.9F;
               float s = 0.5F;
               VertexConsumer lv3 = arg3.getBuffer(RenderLayer.getLines());
               if (arg.getMode() == StructureBlockMode.SAVE || arg.shouldShowBoundingBox()) {
                  WorldRenderer.drawBox(arg2, lv3, m, g, n, o, h, p, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
               }

               if (arg.getMode() == StructureBlockMode.SAVE && arg.shouldShowAir()) {
                  this.renderInvisibleBlocks(arg, lv3, lv, arg2);
               }

            }
         }
      }
   }

   private void renderInvisibleBlocks(StructureBlockBlockEntity entity, VertexConsumer vertices, BlockPos pos, MatrixStack matrices) {
      BlockView lv = entity.getWorld();
      BlockPos lv2 = entity.getPos();
      BlockPos lv3 = lv2.add(pos);
      Iterator var8 = BlockPos.iterate(lv3, lv3.add(entity.getSize()).add(-1, -1, -1)).iterator();

      while(true) {
         BlockPos lv4;
         boolean bl;
         boolean bl2;
         boolean bl3;
         boolean bl4;
         boolean bl5;
         do {
            if (!var8.hasNext()) {
               return;
            }

            lv4 = (BlockPos)var8.next();
            BlockState lv5 = lv.getBlockState(lv4);
            bl = lv5.isAir();
            bl2 = lv5.isOf(Blocks.STRUCTURE_VOID);
            bl3 = lv5.isOf(Blocks.BARRIER);
            bl4 = lv5.isOf(Blocks.LIGHT);
            bl5 = bl2 || bl3 || bl4;
         } while(!bl && !bl5);

         float f = bl ? 0.05F : 0.0F;
         double d = (double)((float)(lv4.getX() - lv2.getX()) + 0.45F - f);
         double e = (double)((float)(lv4.getY() - lv2.getY()) + 0.45F - f);
         double g = (double)((float)(lv4.getZ() - lv2.getZ()) + 0.45F - f);
         double h = (double)((float)(lv4.getX() - lv2.getX()) + 0.55F + f);
         double i = (double)((float)(lv4.getY() - lv2.getY()) + 0.55F + f);
         double j = (double)((float)(lv4.getZ() - lv2.getZ()) + 0.55F + f);
         if (bl) {
            WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
         } else if (bl2) {
            WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
         } else if (bl3) {
            WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
         } else if (bl4) {
            WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
         }
      }
   }

   public boolean rendersOutsideBoundingBox(StructureBlockBlockEntity arg) {
      return true;
   }

   public int getRenderDistance() {
      return 96;
   }
}
