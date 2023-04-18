package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.SnowGolemEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class SnowmanPumpkinFeatureRenderer extends FeatureRenderer {
   private final BlockRenderManager blockRenderManager;
   private final ItemRenderer itemRenderer;

   public SnowmanPumpkinFeatureRenderer(FeatureRendererContext context, BlockRenderManager blockRenderManager, ItemRenderer itemRenderer) {
      super(context);
      this.blockRenderManager = blockRenderManager;
      this.itemRenderer = itemRenderer;
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, SnowGolemEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (arg3.hasPumpkin()) {
         boolean bl = MinecraftClient.getInstance().hasOutline(arg3) && arg3.isInvisible();
         if (!arg3.isInvisible() || bl) {
            arg.push();
            ((SnowGolemEntityModel)this.getContextModel()).getHead().rotate(arg);
            float m = 0.625F;
            arg.translate(0.0F, -0.34375F, 0.0F);
            arg.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            arg.scale(0.625F, -0.625F, -0.625F);
            ItemStack lv = new ItemStack(Blocks.CARVED_PUMPKIN);
            if (bl) {
               BlockState lv2 = Blocks.CARVED_PUMPKIN.getDefaultState();
               BakedModel lv3 = this.blockRenderManager.getModel(lv2);
               int n = LivingEntityRenderer.getOverlay(arg3, 0.0F);
               arg.translate(-0.5F, -0.5F, -0.5F);
               this.blockRenderManager.getModelRenderer().render(arg.peek(), arg2.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)), lv2, lv3, 0.0F, 0.0F, 0.0F, i, n);
            } else {
               this.itemRenderer.renderItem(arg3, lv, ModelTransformationMode.HEAD, false, arg, arg2, arg3.world, i, LivingEntityRenderer.getOverlay(arg3, 0.0F), arg3.getId());
            }

            arg.pop();
         }
      }
   }
}
