package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class EntityRenderer {
   protected static final float field_32921 = 0.025F;
   protected final EntityRenderDispatcher dispatcher;
   private final TextRenderer textRenderer;
   protected float shadowRadius;
   protected float shadowOpacity = 1.0F;

   protected EntityRenderer(EntityRendererFactory.Context ctx) {
      this.dispatcher = ctx.getRenderDispatcher();
      this.textRenderer = ctx.getTextRenderer();
   }

   public final int getLight(Entity entity, float tickDelta) {
      BlockPos lv = BlockPos.ofFloored(entity.getClientCameraPosVec(tickDelta));
      return LightmapTextureManager.pack(this.getBlockLight(entity, lv), this.getSkyLight(entity, lv));
   }

   protected int getSkyLight(Entity entity, BlockPos pos) {
      return entity.world.getLightLevel(LightType.SKY, pos);
   }

   protected int getBlockLight(Entity entity, BlockPos pos) {
      return entity.isOnFire() ? 15 : entity.world.getLightLevel(LightType.BLOCK, pos);
   }

   public boolean shouldRender(Entity entity, Frustum frustum, double x, double y, double z) {
      if (!entity.shouldRender(x, y, z)) {
         return false;
      } else if (entity.ignoreCameraFrustum) {
         return true;
      } else {
         Box lv = entity.getVisibilityBoundingBox().expand(0.5);
         if (lv.isValid() || lv.getAverageSideLength() == 0.0) {
            lv = new Box(entity.getX() - 2.0, entity.getY() - 2.0, entity.getZ() - 2.0, entity.getX() + 2.0, entity.getY() + 2.0, entity.getZ() + 2.0);
         }

         return frustum.isVisible(lv);
      }
   }

   public Vec3d getPositionOffset(Entity entity, float tickDelta) {
      return Vec3d.ZERO;
   }

   public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (this.hasLabel(entity)) {
         this.renderLabelIfPresent(entity, entity.getDisplayName(), matrices, vertexConsumers, light);
      }
   }

   protected boolean hasLabel(Entity entity) {
      return entity.shouldRenderName() && entity.hasCustomName();
   }

   public abstract Identifier getTexture(Entity entity);

   public TextRenderer getTextRenderer() {
      return this.textRenderer;
   }

   protected void renderLabelIfPresent(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      double d = this.dispatcher.getSquaredDistanceToCamera(entity);
      if (!(d > 4096.0)) {
         boolean bl = !entity.isSneaky();
         float f = entity.getNameLabelHeight();
         int j = "deadmau5".equals(text.getString()) ? -10 : 0;
         matrices.push();
         matrices.translate(0.0F, f, 0.0F);
         matrices.multiply(this.dispatcher.getRotation());
         matrices.scale(-0.025F, -0.025F, 0.025F);
         Matrix4f matrix4f = matrices.peek().getPositionMatrix();
         float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
         int k = (int)(g * 255.0F) << 24;
         TextRenderer lv = this.getTextRenderer();
         float h = (float)(-lv.getWidth((StringVisitable)text) / 2);
         lv.draw(text, h, (float)j, 553648127, false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, k, light);
         if (bl) {
            lv.draw((Text)text, h, (float)j, -1, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
         }

         matrices.pop();
      }
   }
}
