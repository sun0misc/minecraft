package net.minecraft.client.render.block.entity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class BedBlockEntityRenderer implements BlockEntityRenderer {
   private final ModelPart bedHead;
   private final ModelPart bedFoot;

   public BedBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.bedHead = ctx.getLayerModelPart(EntityModelLayers.BED_HEAD);
      this.bedFoot = ctx.getLayerModelPart(EntityModelLayers.BED_FOOT);
   }

   public static TexturedModelData getHeadTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(50, 6).cuboid(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F), ModelTransform.rotation(1.5707964F, 0.0F, 1.5707964F));
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(50, 18).cuboid(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F), ModelTransform.rotation(1.5707964F, 0.0F, 3.1415927F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public static TexturedModelData getFootTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("main", ModelPartBuilder.create().uv(0, 22).cuboid(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(50, 0).cuboid(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F), ModelTransform.rotation(1.5707964F, 0.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(50, 12).cuboid(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F), ModelTransform.rotation(1.5707964F, 0.0F, 4.712389F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void render(BedBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      SpriteIdentifier lv = TexturedRenderLayers.BED_TEXTURES[arg.getColor().getId()];
      World lv2 = arg.getWorld();
      if (lv2 != null) {
         BlockState lv3 = arg.getCachedState();
         DoubleBlockProperties.PropertySource lv4 = DoubleBlockProperties.toPropertySource(BlockEntityType.BED, BedBlock::getBedPart, BedBlock::getOppositePartDirection, ChestBlock.FACING, lv3, lv2, arg.getPos(), (world, pos) -> {
            return false;
         });
         int k = ((Int2IntFunction)lv4.apply(new LightmapCoordinatesRetriever())).get(i);
         this.renderPart(arg2, arg3, lv3.get(BedBlock.PART) == BedPart.HEAD ? this.bedHead : this.bedFoot, (Direction)lv3.get(BedBlock.FACING), lv, k, j, false);
      } else {
         this.renderPart(arg2, arg3, this.bedHead, Direction.SOUTH, lv, i, j, false);
         this.renderPart(arg2, arg3, this.bedFoot, Direction.SOUTH, lv, i, j, true);
      }

   }

   private void renderPart(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ModelPart part, Direction direction, SpriteIdentifier sprite, int light, int overlay, boolean isFoot) {
      matrices.push();
      matrices.translate(0.0F, 0.5625F, isFoot ? -1.0F : 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
      matrices.translate(0.5F, 0.5F, 0.5F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F + direction.asRotation()));
      matrices.translate(-0.5F, -0.5F, -0.5F);
      VertexConsumer lv = sprite.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid);
      part.render(matrices, lv, light, overlay);
      matrices.pop();
   }
}
