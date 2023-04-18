package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class FallingBlockEntityRenderer extends EntityRenderer {
   private final BlockRenderManager blockRenderManager;

   public FallingBlockEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.shadowRadius = 0.5F;
      this.blockRenderManager = arg.getBlockRenderManager();
   }

   public void render(FallingBlockEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      BlockState lv = arg.getBlockState();
      if (lv.getRenderType() == BlockRenderType.MODEL) {
         World lv2 = arg.getWorld();
         if (lv != lv2.getBlockState(arg.getBlockPos()) && lv.getRenderType() != BlockRenderType.INVISIBLE) {
            arg2.push();
            BlockPos lv3 = BlockPos.ofFloored(arg.getX(), arg.getBoundingBox().maxY, arg.getZ());
            arg2.translate(-0.5, 0.0, -0.5);
            this.blockRenderManager.getModelRenderer().render(lv2, this.blockRenderManager.getModel(lv), lv, lv3, arg2, arg3.getBuffer(RenderLayers.getMovingBlockLayer(lv)), false, Random.create(), lv.getRenderingSeed(arg.getFallingBlockPos()), OverlayTexture.DEFAULT_UV);
            arg2.pop();
            super.render(arg, f, g, arg2, arg3, i);
         }
      }
   }

   public Identifier getTexture(FallingBlockEntity arg) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }
}
