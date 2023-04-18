package net.minecraft.client.render.block.entity;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.enums.ChestType;
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
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class ChestBlockEntityRenderer implements BlockEntityRenderer {
   private static final String BASE = "bottom";
   private static final String LID = "lid";
   private static final String LATCH = "lock";
   private final ModelPart singleChestLid;
   private final ModelPart singleChestBase;
   private final ModelPart singleChestLatch;
   private final ModelPart doubleChestLeftLid;
   private final ModelPart doubleChestLeftBase;
   private final ModelPart doubleChestLeftLatch;
   private final ModelPart doubleChestRightLid;
   private final ModelPart doubleChestRightBase;
   private final ModelPart doubleChestRightLatch;
   private boolean christmas;

   public ChestBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      Calendar calendar = Calendar.getInstance();
      if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
         this.christmas = true;
      }

      ModelPart lv = ctx.getLayerModelPart(EntityModelLayers.CHEST);
      this.singleChestBase = lv.getChild("bottom");
      this.singleChestLid = lv.getChild("lid");
      this.singleChestLatch = lv.getChild("lock");
      ModelPart lv2 = ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_LEFT);
      this.doubleChestLeftBase = lv2.getChild("bottom");
      this.doubleChestLeftLid = lv2.getChild("lid");
      this.doubleChestLeftLatch = lv2.getChild("lock");
      ModelPart lv3 = ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_RIGHT);
      this.doubleChestRightBase = lv3.getChild("bottom");
      this.doubleChestRightLid = lv3.getChild("lid");
      this.doubleChestRightLatch = lv3.getChild("lock");
   }

   public static TexturedModelData getSingleTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("bottom", ModelPartBuilder.create().uv(0, 19).cuboid(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), ModelTransform.NONE);
      lv2.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), ModelTransform.pivot(0.0F, 9.0F, 1.0F));
      lv2.addChild("lock", ModelPartBuilder.create().uv(0, 0).cuboid(7.0F, -2.0F, 14.0F, 2.0F, 4.0F, 1.0F), ModelTransform.pivot(0.0F, 9.0F, 1.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public static TexturedModelData getRightDoubleTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("bottom", ModelPartBuilder.create().uv(0, 19).cuboid(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), ModelTransform.NONE);
      lv2.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), ModelTransform.pivot(0.0F, 9.0F, 1.0F));
      lv2.addChild("lock", ModelPartBuilder.create().uv(0, 0).cuboid(15.0F, -2.0F, 14.0F, 1.0F, 4.0F, 1.0F), ModelTransform.pivot(0.0F, 9.0F, 1.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public static TexturedModelData getLeftDoubleTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("bottom", ModelPartBuilder.create().uv(0, 19).cuboid(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), ModelTransform.NONE);
      lv2.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), ModelTransform.pivot(0.0F, 9.0F, 1.0F));
      lv2.addChild("lock", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -2.0F, 14.0F, 1.0F, 4.0F, 1.0F), ModelTransform.pivot(0.0F, 9.0F, 1.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void render(BlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
      World lv = entity.getWorld();
      boolean bl = lv != null;
      BlockState lv2 = bl ? entity.getCachedState() : (BlockState)Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);
      ChestType lv3 = lv2.contains(ChestBlock.CHEST_TYPE) ? (ChestType)lv2.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
      Block lv4 = lv2.getBlock();
      if (lv4 instanceof AbstractChestBlock lv5) {
         boolean bl2 = lv3 != ChestType.SINGLE;
         matrices.push();
         float g = ((Direction)lv2.get(ChestBlock.FACING)).asRotation();
         matrices.translate(0.5F, 0.5F, 0.5F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-g));
         matrices.translate(-0.5F, -0.5F, -0.5F);
         DoubleBlockProperties.PropertySource lv6;
         if (bl) {
            lv6 = lv5.getBlockEntitySource(lv2, lv, entity.getPos(), true);
         } else {
            lv6 = DoubleBlockProperties.PropertyRetriever::getFallback;
         }

         float h = ((Float2FloatFunction)lv6.apply(ChestBlock.getAnimationProgressRetriever((LidOpenable)entity))).get(tickDelta);
         h = 1.0F - h;
         h = 1.0F - h * h * h;
         int k = ((Int2IntFunction)lv6.apply(new LightmapCoordinatesRetriever())).applyAsInt(light);
         SpriteIdentifier lv7 = TexturedRenderLayers.getChestTextureId(entity, lv3, this.christmas);
         VertexConsumer lv8 = lv7.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutout);
         if (bl2) {
            if (lv3 == ChestType.LEFT) {
               this.render(matrices, lv8, this.doubleChestLeftLid, this.doubleChestLeftLatch, this.doubleChestLeftBase, h, k, overlay);
            } else {
               this.render(matrices, lv8, this.doubleChestRightLid, this.doubleChestRightLatch, this.doubleChestRightBase, h, k, overlay);
            }
         } else {
            this.render(matrices, lv8, this.singleChestLid, this.singleChestLatch, this.singleChestBase, h, k, overlay);
         }

         matrices.pop();
      }
   }

   private void render(MatrixStack matrices, VertexConsumer vertices, ModelPart lid, ModelPart latch, ModelPart base, float openFactor, int light, int overlay) {
      lid.pitch = -(openFactor * 1.5707964F);
      latch.pitch = lid.pitch;
      lid.render(matrices, vertices, light, overlay);
      latch.render(matrices, vertices, light, overlay);
      base.render(matrices, vertices, light, overlay);
   }
}
