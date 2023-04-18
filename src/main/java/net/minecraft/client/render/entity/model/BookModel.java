package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BookModel extends Model {
   private static final String LEFT_PAGES = "left_pages";
   private static final String RIGHT_PAGES = "right_pages";
   private static final String FLIP_PAGE1 = "flip_page1";
   private static final String FLIP_PAGE2 = "flip_page2";
   private final ModelPart root;
   private final ModelPart leftCover;
   private final ModelPart rightCover;
   private final ModelPart leftPages;
   private final ModelPart rightPages;
   private final ModelPart leftFlippingPage;
   private final ModelPart rightFlippingPage;

   public BookModel(ModelPart root) {
      super(RenderLayer::getEntitySolid);
      this.root = root;
      this.leftCover = root.getChild(EntityModelPartNames.LEFT_LID);
      this.rightCover = root.getChild(EntityModelPartNames.RIGHT_LID);
      this.leftPages = root.getChild("left_pages");
      this.rightPages = root.getChild("right_pages");
      this.leftFlippingPage = root.getChild("flip_page1");
      this.rightFlippingPage = root.getChild("flip_page2");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.LEFT_LID, ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), ModelTransform.pivot(0.0F, 0.0F, -1.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_LID, ModelPartBuilder.create().uv(16, 0).cuboid(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), ModelTransform.pivot(0.0F, 0.0F, 1.0F));
      lv2.addChild("seam", ModelPartBuilder.create().uv(12, 0).cuboid(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F), ModelTransform.rotation(0.0F, 1.5707964F, 0.0F));
      lv2.addChild("left_pages", ModelPartBuilder.create().uv(0, 10).cuboid(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), ModelTransform.NONE);
      lv2.addChild("right_pages", ModelPartBuilder.create().uv(12, 10).cuboid(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), ModelTransform.NONE);
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(24, 10).cuboid(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
      lv2.addChild("flip_page1", lv3, ModelTransform.NONE);
      lv2.addChild("flip_page2", lv3, ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 32);
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.renderBook(matrices, vertices, light, overlay, red, green, blue, alpha);
   }

   public void renderBook(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
   }

   public void setPageAngles(float pageTurnAmount, float leftFlipAmount, float rightFlipAmount, float pageTurnSpeed) {
      float j = (MathHelper.sin(pageTurnAmount * 0.02F) * 0.1F + 1.25F) * pageTurnSpeed;
      this.leftCover.yaw = 3.1415927F + j;
      this.rightCover.yaw = -j;
      this.leftPages.yaw = j;
      this.rightPages.yaw = -j;
      this.leftFlippingPage.yaw = j - j * 2.0F * leftFlipAmount;
      this.rightFlippingPage.yaw = j - j * 2.0F * rightFlipAmount;
      this.leftPages.pivotX = MathHelper.sin(j);
      this.rightPages.pivotX = MathHelper.sin(j);
      this.leftFlippingPage.pivotX = MathHelper.sin(j);
      this.rightFlippingPage.pivotX = MathHelper.sin(j);
   }
}
