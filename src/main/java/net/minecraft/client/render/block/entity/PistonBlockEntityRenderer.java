package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class PistonBlockEntityRenderer implements BlockEntityRenderer {
   private final BlockRenderManager manager;

   public PistonBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.manager = ctx.getRenderManager();
   }

   public void render(PistonBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      World lv = arg.getWorld();
      if (lv != null) {
         BlockPos lv2 = arg.getPos().offset(arg.getMovementDirection().getOpposite());
         BlockState lv3 = arg.getPushedBlock();
         if (!lv3.isAir()) {
            BlockModelRenderer.enableBrightnessCache();
            arg2.push();
            arg2.translate(arg.getRenderOffsetX(f), arg.getRenderOffsetY(f), arg.getRenderOffsetZ(f));
            if (lv3.isOf(Blocks.PISTON_HEAD) && arg.getProgress(f) <= 4.0F) {
               lv3 = (BlockState)lv3.with(PistonHeadBlock.SHORT, arg.getProgress(f) <= 0.5F);
               this.renderModel(lv2, lv3, arg2, arg3, lv, false, j);
            } else if (arg.isSource() && !arg.isExtending()) {
               PistonType lv4 = lv3.isOf(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
               BlockState lv5 = (BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.TYPE, lv4)).with(PistonHeadBlock.FACING, (Direction)lv3.get(PistonBlock.FACING));
               lv5 = (BlockState)lv5.with(PistonHeadBlock.SHORT, arg.getProgress(f) >= 0.5F);
               this.renderModel(lv2, lv5, arg2, arg3, lv, false, j);
               BlockPos lv6 = lv2.offset(arg.getMovementDirection());
               arg2.pop();
               arg2.push();
               lv3 = (BlockState)lv3.with(PistonBlock.EXTENDED, true);
               this.renderModel(lv6, lv3, arg2, arg3, lv, true, j);
            } else {
               this.renderModel(lv2, lv3, arg2, arg3, lv, false, j);
            }

            arg2.pop();
            BlockModelRenderer.disableBrightnessCache();
         }
      }
   }

   private void renderModel(BlockPos pos, BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, boolean cull, int overlay) {
      RenderLayer lv = RenderLayers.getMovingBlockLayer(state);
      VertexConsumer lv2 = vertexConsumers.getBuffer(lv);
      this.manager.getModelRenderer().render(world, this.manager.getModel(state), state, pos, matrices, lv2, cull, Random.create(), state.getRenderingSeed(pos), overlay);
   }

   public int getRenderDistance() {
      return 68;
   }
}
