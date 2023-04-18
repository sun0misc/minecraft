package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class LecternBlockEntityRenderer implements BlockEntityRenderer {
   private final BookModel book;

   public LecternBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.book = new BookModel(ctx.getLayerModelPart(EntityModelLayers.BOOK));
   }

   public void render(LecternBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      BlockState lv = arg.getCachedState();
      if ((Boolean)lv.get(LecternBlock.HAS_BOOK)) {
         arg2.push();
         arg2.translate(0.5F, 1.0625F, 0.5F);
         float g = ((Direction)lv.get(LecternBlock.FACING)).rotateYClockwise().asRotation();
         arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-g));
         arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(67.5F));
         arg2.translate(0.0F, -0.125F, 0.0F);
         this.book.setPageAngles(0.0F, 0.1F, 0.9F, 1.2F);
         VertexConsumer lv2 = EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
         this.book.renderBook(arg2, lv2, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
         arg2.pop();
      }
   }
}
