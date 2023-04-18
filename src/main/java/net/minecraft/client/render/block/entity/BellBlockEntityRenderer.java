package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BellBlockEntityRenderer implements BlockEntityRenderer {
   public static final SpriteIdentifier BELL_BODY_TEXTURE;
   private static final String BELL_BODY = "bell_body";
   private final ModelPart bellBody;

   public BellBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      ModelPart lv = ctx.getLayerModelPart(EntityModelLayers.BELL);
      this.bellBody = lv.getChild("bell_body");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild("bell_body", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F), ModelTransform.pivot(8.0F, 12.0F, 8.0F));
      lv3.addChild("bell_base", ModelPartBuilder.create().uv(0, 13).cuboid(4.0F, 4.0F, 4.0F, 8.0F, 2.0F, 8.0F), ModelTransform.pivot(-8.0F, -12.0F, -8.0F));
      return TexturedModelData.of(lv, 32, 32);
   }

   public void render(BellBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      float g = (float)arg.ringTicks + f;
      float h = 0.0F;
      float k = 0.0F;
      if (arg.ringing) {
         float l = MathHelper.sin(g / 3.1415927F) / (4.0F + g / 3.0F);
         if (arg.lastSideHit == Direction.NORTH) {
            h = -l;
         } else if (arg.lastSideHit == Direction.SOUTH) {
            h = l;
         } else if (arg.lastSideHit == Direction.EAST) {
            k = -l;
         } else if (arg.lastSideHit == Direction.WEST) {
            k = l;
         }
      }

      this.bellBody.pitch = h;
      this.bellBody.roll = k;
      VertexConsumer lv = BELL_BODY_TEXTURE.getVertexConsumer(arg3, RenderLayer::getEntitySolid);
      this.bellBody.render(arg2, lv, i, j);
   }

   static {
      BELL_BODY_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/bell/bell_body"));
   }
}
