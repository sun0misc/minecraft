package net.minecraft.client.render.block.entity;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DecoratedPotPatterns;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class DecoratedPotBlockEntityRenderer implements BlockEntityRenderer {
   private static final String NECK = "neck";
   private static final String FRONT = "front";
   private static final String BACK = "back";
   private static final String LEFT = "left";
   private static final String RIGHT = "right";
   private static final String TOP = "top";
   private static final String BOTTOM = "bottom";
   private final ModelPart neck;
   private final ModelPart front;
   private final ModelPart back;
   private final ModelPart left;
   private final ModelPart right;
   private final ModelPart top;
   private final ModelPart bottom;
   private final SpriteIdentifier baseTexture;

   public DecoratedPotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
      this.baseTexture = (SpriteIdentifier)Objects.requireNonNull(TexturedRenderLayers.getDecoratedPotPatternTextureId(DecoratedPotPatterns.DECORATED_POT_BASE_KEY));
      ModelPart lv = context.getLayerModelPart(EntityModelLayers.DECORATED_POT_BASE);
      this.neck = lv.getChild(EntityModelPartNames.NECK);
      this.top = lv.getChild("top");
      this.bottom = lv.getChild("bottom");
      ModelPart lv2 = context.getLayerModelPart(EntityModelLayers.DECORATED_POT_SIDES);
      this.front = lv2.getChild("front");
      this.back = lv2.getChild("back");
      this.left = lv2.getChild("left");
      this.right = lv2.getChild("right");
   }

   public static TexturedModelData getTopBottomNeckTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      Dilation lv3 = new Dilation(0.2F);
      Dilation lv4 = new Dilation(-0.1F);
      lv2.addChild(EntityModelPartNames.NECK, ModelPartBuilder.create().uv(0, 0).cuboid(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, lv4).uv(0, 5).cuboid(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, lv3), ModelTransform.of(0.0F, 37.0F, 16.0F, 3.1415927F, 0.0F, 0.0F));
      ModelPartBuilder lv5 = ModelPartBuilder.create().uv(-14, 13).cuboid(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F);
      lv2.addChild("top", lv5, ModelTransform.of(1.0F, 16.0F, 1.0F, 0.0F, 0.0F, 0.0F));
      lv2.addChild("bottom", lv5, ModelTransform.of(1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F));
      return TexturedModelData.of(lv, 32, 32);
   }

   public static TexturedModelData getSidesTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(1, 0).cuboid(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, (Set)EnumSet.of(Direction.NORTH));
      lv2.addChild("back", lv3, ModelTransform.of(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, 3.1415927F));
      lv2.addChild("left", lv3, ModelTransform.of(1.0F, 16.0F, 1.0F, 0.0F, -1.5707964F, 3.1415927F));
      lv2.addChild("right", lv3, ModelTransform.of(15.0F, 16.0F, 15.0F, 0.0F, 1.5707964F, 3.1415927F));
      lv2.addChild("front", lv3, ModelTransform.of(1.0F, 16.0F, 15.0F, 3.1415927F, 0.0F, 0.0F));
      return TexturedModelData.of(lv, 16, 16);
   }

   @Nullable
   private static SpriteIdentifier getTextureIdFromShard(Item item) {
      SpriteIdentifier lv = TexturedRenderLayers.getDecoratedPotPatternTextureId(DecoratedPotPatterns.fromShard(item));
      if (lv == null) {
         lv = TexturedRenderLayers.getDecoratedPotPatternTextureId(DecoratedPotPatterns.fromShard(Items.BRICK));
      }

      return lv;
   }

   public void render(DecoratedPotBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      arg2.push();
      Direction lv = arg.getHorizontalFacing();
      arg2.translate(0.5, 0.0, 0.5);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - lv.asRotation()));
      arg2.translate(-0.5, 0.0, -0.5);
      VertexConsumer lv2 = this.baseTexture.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
      this.neck.render(arg2, lv2, i, j);
      this.top.render(arg2, lv2, i, j);
      this.bottom.render(arg2, lv2, i, j);
      List list = arg.getShards();
      this.renderDecoratedSide(this.front, arg2, arg3, i, j, getTextureIdFromShard((Item)list.get(3)));
      this.renderDecoratedSide(this.back, arg2, arg3, i, j, getTextureIdFromShard((Item)list.get(0)));
      this.renderDecoratedSide(this.left, arg2, arg3, i, j, getTextureIdFromShard((Item)list.get(1)));
      this.renderDecoratedSide(this.right, arg2, arg3, i, j, getTextureIdFromShard((Item)list.get(2)));
      arg2.pop();
   }

   private void renderDecoratedSide(ModelPart part, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, @Nullable SpriteIdentifier textureId) {
      if (textureId == null) {
         textureId = getTextureIdFromShard(Items.BRICK);
      }

      if (textureId != null) {
         part.render(matrices, textureId.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid), light, overlay);
      }

   }
}
