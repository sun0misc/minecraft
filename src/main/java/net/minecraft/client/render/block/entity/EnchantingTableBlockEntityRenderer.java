package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class EnchantingTableBlockEntityRenderer implements BlockEntityRenderer {
   public static final SpriteIdentifier BOOK_TEXTURE;
   private final BookModel book;

   public EnchantingTableBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.book = new BookModel(ctx.getLayerModelPart(EntityModelLayers.BOOK));
   }

   public void render(EnchantingTableBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      arg2.push();
      arg2.translate(0.5F, 0.75F, 0.5F);
      float g = (float)arg.ticks + f;
      arg2.translate(0.0F, 0.1F + MathHelper.sin(g * 0.1F) * 0.01F, 0.0F);

      float h;
      for(h = arg.bookRotation - arg.lastBookRotation; h >= 3.1415927F; h -= 6.2831855F) {
      }

      while(h < -3.1415927F) {
         h += 6.2831855F;
      }

      float k = arg.lastBookRotation + h * f;
      arg2.multiply(RotationAxis.POSITIVE_Y.rotation(-k));
      arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80.0F));
      float l = MathHelper.lerp(f, arg.pageAngle, arg.nextPageAngle);
      float m = MathHelper.fractionalPart(l + 0.25F) * 1.6F - 0.3F;
      float n = MathHelper.fractionalPart(l + 0.75F) * 1.6F - 0.3F;
      float o = MathHelper.lerp(f, arg.pageTurningSpeed, arg.nextPageTurningSpeed);
      this.book.setPageAngles(g, MathHelper.clamp(m, 0.0F, 1.0F), MathHelper.clamp(n, 0.0F, 1.0F), o);
      VertexConsumer lv = BOOK_TEXTURE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
      this.book.renderBook(arg2, lv, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
      arg2.pop();
   }

   static {
      BOOK_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/enchanting_table_book"));
   }
}
