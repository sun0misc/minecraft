package net.minecraft.client.render.block.entity;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.entity.BannerBlockEntity;
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
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;

@Environment(EnvType.CLIENT)
public class BannerBlockEntityRenderer implements BlockEntityRenderer {
   private static final int WIDTH = 20;
   private static final int HEIGHT = 40;
   private static final int ROTATIONS = 16;
   public static final String BANNER = "flag";
   private static final String PILLAR = "pole";
   private static final String CROSSBAR = "bar";
   private final ModelPart banner;
   private final ModelPart pillar;
   private final ModelPart crossbar;

   public BannerBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      ModelPart lv = ctx.getLayerModelPart(EntityModelLayers.BANNER);
      this.banner = lv.getChild("flag");
      this.pillar = lv.getChild("pole");
      this.crossbar = lv.getChild("bar");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("flag", ModelPartBuilder.create().uv(0, 0).cuboid(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F), ModelTransform.NONE);
      lv2.addChild("pole", ModelPartBuilder.create().uv(44, 0).cuboid(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F), ModelTransform.NONE);
      lv2.addChild("bar", ModelPartBuilder.create().uv(0, 42).cuboid(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 64);
   }

   public void render(BannerBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      List list = arg.getPatterns();
      float g = 0.6666667F;
      boolean bl = arg.getWorld() == null;
      arg2.push();
      long l;
      if (bl) {
         l = 0L;
         arg2.translate(0.5F, 0.5F, 0.5F);
         this.pillar.visible = true;
      } else {
         l = arg.getWorld().getTime();
         BlockState lv = arg.getCachedState();
         float h;
         if (lv.getBlock() instanceof BannerBlock) {
            arg2.translate(0.5F, 0.5F, 0.5F);
            h = -RotationPropertyHelper.toDegrees((Integer)lv.get(BannerBlock.ROTATION));
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
            this.pillar.visible = true;
         } else {
            arg2.translate(0.5F, -0.16666667F, 0.5F);
            h = -((Direction)lv.get(WallBannerBlock.FACING)).asRotation();
            arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
            arg2.translate(0.0F, -0.3125F, -0.4375F);
            this.pillar.visible = false;
         }
      }

      arg2.push();
      arg2.scale(0.6666667F, -0.6666667F, -0.6666667F);
      VertexConsumer lv2 = ModelLoader.BANNER_BASE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
      this.pillar.render(arg2, lv2, i, j);
      this.crossbar.render(arg2, lv2, i, j);
      BlockPos lv3 = arg.getPos();
      float k = ((float)Math.floorMod((long)(lv3.getX() * 7 + lv3.getY() * 9 + lv3.getZ() * 13) + l, 100L) + f) / 100.0F;
      this.banner.pitch = (-0.0125F + 0.01F * MathHelper.cos(6.2831855F * k)) * 3.1415927F;
      this.banner.pivotY = -32.0F;
      renderCanvas(arg2, arg3, i, j, this.banner, ModelLoader.BANNER_BASE, true, list);
      arg2.pop();
      arg2.pop();
   }

   public static void renderCanvas(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner, List patterns) {
      renderCanvas(matrices, vertexConsumers, light, overlay, canvas, baseSprite, isBanner, patterns, false);
   }

   public static void renderCanvas(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner, List patterns, boolean glint) {
      canvas.render(matrices, baseSprite.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid, glint), light, overlay);

      for(int k = 0; k < 17 && k < patterns.size(); ++k) {
         Pair pair = (Pair)patterns.get(k);
         float[] fs = ((DyeColor)pair.getSecond()).getColorComponents();
         ((RegistryEntry)pair.getFirst()).getKey().map((key) -> {
            return isBanner ? TexturedRenderLayers.getBannerPatternTextureId(key) : TexturedRenderLayers.getShieldPatternTextureId(key);
         }).ifPresent((sprite) -> {
            canvas.render(matrices, sprite.getVertexConsumer(vertexConsumers, RenderLayer::getEntityNoOutline), light, overlay, fs[0], fs[1], fs[2], 1.0F);
         });
      }

   }
}
