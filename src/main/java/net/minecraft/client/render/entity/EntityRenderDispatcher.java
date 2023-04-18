package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class EntityRenderDispatcher implements SynchronousResourceReloader {
   private static final RenderLayer SHADOW_LAYER = RenderLayer.getEntityShadow(new Identifier("textures/misc/shadow.png"));
   private static final float field_43377 = 32.0F;
   private static final float field_43378 = 0.5F;
   private Map renderers = ImmutableMap.of();
   private Map modelRenderers = ImmutableMap.of();
   public final TextureManager textureManager;
   private World world;
   public Camera camera;
   private Quaternionf rotation;
   public Entity targetedEntity;
   private final ItemRenderer itemRenderer;
   private final BlockRenderManager blockRenderManager;
   private final HeldItemRenderer heldItemRenderer;
   private final TextRenderer textRenderer;
   public final GameOptions gameOptions;
   private final EntityModelLoader modelLoader;
   private boolean renderShadows = true;
   private boolean renderHitboxes;

   public int getLight(Entity entity, float tickDelta) {
      return this.getRenderer(entity).getLight(entity, tickDelta);
   }

   public EntityRenderDispatcher(MinecraftClient client, TextureManager textureManager, ItemRenderer itemRenderer, BlockRenderManager blockRenderManager, TextRenderer textRenderer, GameOptions gameOptions, EntityModelLoader modelLoader) {
      this.textureManager = textureManager;
      this.itemRenderer = itemRenderer;
      this.heldItemRenderer = new HeldItemRenderer(client, this, itemRenderer);
      this.blockRenderManager = blockRenderManager;
      this.textRenderer = textRenderer;
      this.gameOptions = gameOptions;
      this.modelLoader = modelLoader;
   }

   public EntityRenderer getRenderer(Entity entity) {
      if (entity instanceof AbstractClientPlayerEntity) {
         String string = ((AbstractClientPlayerEntity)entity).getModel();
         EntityRenderer lv = (EntityRenderer)this.modelRenderers.get(string);
         return lv != null ? lv : (EntityRenderer)this.modelRenderers.get("default");
      } else {
         return (EntityRenderer)this.renderers.get(entity.getType());
      }
   }

   public void configure(World world, Camera camera, Entity target) {
      this.world = world;
      this.camera = camera;
      this.rotation = camera.getRotation();
      this.targetedEntity = target;
   }

   public void setRotation(Quaternionf rotation) {
      this.rotation = rotation;
   }

   public void setRenderShadows(boolean renderShadows) {
      this.renderShadows = renderShadows;
   }

   public void setRenderHitboxes(boolean renderHitboxes) {
      this.renderHitboxes = renderHitboxes;
   }

   public boolean shouldRenderHitboxes() {
      return this.renderHitboxes;
   }

   public boolean shouldRender(Entity entity, Frustum frustum, double x, double y, double z) {
      EntityRenderer lv = this.getRenderer(entity);
      return lv.shouldRender(entity, frustum, x, y, z);
   }

   public void render(Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      EntityRenderer lv = this.getRenderer(entity);

      try {
         Vec3d lv2 = lv.getPositionOffset(entity, tickDelta);
         double j = x + lv2.getX();
         double k = y + lv2.getY();
         double l = z + lv2.getZ();
         matrices.push();
         matrices.translate(j, k, l);
         lv.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
         if (entity.doesRenderOnFire()) {
            this.renderFire(matrices, vertexConsumers, entity);
         }

         matrices.translate(-lv2.getX(), -lv2.getY(), -lv2.getZ());
         if ((Boolean)this.gameOptions.getEntityShadows().getValue() && this.renderShadows && lv.shadowRadius > 0.0F && !entity.isInvisible()) {
            double m = this.getSquaredDistanceToCamera(entity.getX(), entity.getY(), entity.getZ());
            float n = (float)((1.0 - m / 256.0) * (double)lv.shadowOpacity);
            if (n > 0.0F) {
               renderShadow(matrices, vertexConsumers, entity, n, tickDelta, this.world, Math.min(lv.shadowRadius, 32.0F));
            }
         }

         if (this.renderHitboxes && !entity.isInvisible() && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
            renderHitbox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), entity, tickDelta);
         }

         matrices.pop();
      } catch (Throwable var24) {
         CrashReport lv3 = CrashReport.create(var24, "Rendering entity in world");
         CrashReportSection lv4 = lv3.addElement("Entity being rendered");
         entity.populateCrashReport(lv4);
         CrashReportSection lv5 = lv3.addElement("Renderer details");
         lv5.add("Assigned renderer", (Object)lv);
         lv5.add("Location", (Object)CrashReportSection.createPositionString(this.world, x, y, z));
         lv5.add("Rotation", (Object)yaw);
         lv5.add("Delta", (Object)tickDelta);
         throw new CrashException(lv3);
      }
   }

   private static void renderHitbox(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta) {
      Box lv = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
      WorldRenderer.drawBox(matrices, vertices, lv, 1.0F, 1.0F, 1.0F, 1.0F);
      if (entity instanceof EnderDragonEntity) {
         double d = -MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
         double e = -MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
         double g = -MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
         EnderDragonPart[] var11 = ((EnderDragonEntity)entity).getBodyParts();
         int var12 = var11.length;

         for(int var13 = 0; var13 < var12; ++var13) {
            EnderDragonPart lv2 = var11[var13];
            matrices.push();
            double h = d + MathHelper.lerp((double)tickDelta, lv2.lastRenderX, lv2.getX());
            double i = e + MathHelper.lerp((double)tickDelta, lv2.lastRenderY, lv2.getY());
            double j = g + MathHelper.lerp((double)tickDelta, lv2.lastRenderZ, lv2.getZ());
            matrices.translate(h, i, j);
            WorldRenderer.drawBox(matrices, vertices, lv2.getBoundingBox().offset(-lv2.getX(), -lv2.getY(), -lv2.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
            matrices.pop();
         }
      }

      if (entity instanceof LivingEntity) {
         float k = 0.01F;
         WorldRenderer.drawBox(matrices, vertices, lv.minX, (double)(entity.getStandingEyeHeight() - 0.01F), lv.minZ, lv.maxX, (double)(entity.getStandingEyeHeight() + 0.01F), lv.maxZ, 1.0F, 0.0F, 0.0F, 1.0F);
      }

      Vec3d lv3 = entity.getRotationVec(tickDelta);
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      Matrix3f matrix3f = matrices.peek().getNormalMatrix();
      vertices.vertex(matrix4f, 0.0F, entity.getStandingEyeHeight(), 0.0F).color(0, 0, 255, 255).normal(matrix3f, (float)lv3.x, (float)lv3.y, (float)lv3.z).next();
      vertices.vertex(matrix4f, (float)(lv3.x * 2.0), (float)((double)entity.getStandingEyeHeight() + lv3.y * 2.0), (float)(lv3.z * 2.0)).color(0, 0, 255, 255).normal(matrix3f, (float)lv3.x, (float)lv3.y, (float)lv3.z).next();
   }

   private void renderFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity) {
      Sprite lv = ModelLoader.FIRE_0.getSprite();
      Sprite lv2 = ModelLoader.FIRE_1.getSprite();
      matrices.push();
      float f = entity.getWidth() * 1.4F;
      matrices.scale(f, f, f);
      float g = 0.5F;
      float h = 0.0F;
      float i = entity.getHeight() / f;
      float j = 0.0F;
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-this.camera.getYaw()));
      matrices.translate(0.0F, 0.0F, -0.3F + (float)((int)i) * 0.02F);
      float k = 0.0F;
      int l = 0;
      VertexConsumer lv3 = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());

      for(MatrixStack.Entry lv4 = matrices.peek(); i > 0.0F; ++l) {
         Sprite lv5 = l % 2 == 0 ? lv : lv2;
         float m = lv5.getMinU();
         float n = lv5.getMinV();
         float o = lv5.getMaxU();
         float p = lv5.getMaxV();
         if (l / 2 % 2 == 0) {
            float q = o;
            o = m;
            m = q;
         }

         drawFireVertex(lv4, lv3, g - 0.0F, 0.0F - j, k, o, p);
         drawFireVertex(lv4, lv3, -g - 0.0F, 0.0F - j, k, m, p);
         drawFireVertex(lv4, lv3, -g - 0.0F, 1.4F - j, k, m, n);
         drawFireVertex(lv4, lv3, g - 0.0F, 1.4F - j, k, o, n);
         i -= 0.45F;
         j -= 0.45F;
         g *= 0.9F;
         k += 0.03F;
      }

      matrices.pop();
   }

   private static void drawFireVertex(MatrixStack.Entry entry, VertexConsumer vertices, float x, float y, float z, float u, float v) {
      vertices.vertex(entry.getPositionMatrix(), x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(0, 10).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).normal(entry.getNormalMatrix(), 0.0F, 1.0F, 0.0F).next();
   }

   private static void renderShadow(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float opacity, float tickDelta, WorldView world, float radius) {
      float i = radius;
      if (entity instanceof MobEntity lv) {
         if (lv.isBaby()) {
            i = radius * 0.5F;
         }
      }

      double d = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
      double e = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
      double j = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
      float k = Math.min(opacity / 0.5F, i);
      int l = MathHelper.floor(d - (double)i);
      int m = MathHelper.floor(d + (double)i);
      int n = MathHelper.floor(e - (double)k);
      int o = MathHelper.floor(e);
      int p = MathHelper.floor(j - (double)i);
      int q = MathHelper.floor(j + (double)i);
      MatrixStack.Entry lv2 = matrices.peek();
      VertexConsumer lv3 = vertexConsumers.getBuffer(SHADOW_LAYER);
      BlockPos.Mutable lv4 = new BlockPos.Mutable();

      for(int r = p; r <= q; ++r) {
         for(int s = l; s <= m; ++s) {
            lv4.set(s, 0, r);
            Chunk lv5 = world.getChunk(lv4);

            for(int t = n; t <= o; ++t) {
               lv4.setY(t);
               float u = opacity - (float)(e - (double)lv4.getY()) * 0.5F;
               renderShadowPart(lv2, lv3, lv5, world, lv4, d, e, j, i, u);
            }
         }
      }

   }

   private static void renderShadowPart(MatrixStack.Entry entry, VertexConsumer vertices, Chunk chunk, WorldView world, BlockPos pos, double x, double y, double z, float radius, float opacity) {
      BlockPos lv = pos.down();
      BlockState lv2 = chunk.getBlockState(lv);
      if (lv2.getRenderType() != BlockRenderType.INVISIBLE && world.getLightLevel(pos) > 3) {
         if (lv2.isFullCube(chunk, lv)) {
            VoxelShape lv3 = lv2.getOutlineShape(chunk, lv);
            if (!lv3.isEmpty()) {
               float i = LightmapTextureManager.getBrightness(world.getDimension(), world.getLightLevel(pos));
               float j = opacity * 0.5F * i;
               if (j >= 0.0F) {
                  if (j > 1.0F) {
                     j = 1.0F;
                  }

                  Box lv4 = lv3.getBoundingBox();
                  double k = (double)pos.getX() + lv4.minX;
                  double l = (double)pos.getX() + lv4.maxX;
                  double m = (double)pos.getY() + lv4.minY;
                  double n = (double)pos.getZ() + lv4.minZ;
                  double o = (double)pos.getZ() + lv4.maxZ;
                  float p = (float)(k - x);
                  float q = (float)(l - x);
                  float r = (float)(m - y);
                  float s = (float)(n - z);
                  float t = (float)(o - z);
                  float u = -p / 2.0F / radius + 0.5F;
                  float v = -q / 2.0F / radius + 0.5F;
                  float w = -s / 2.0F / radius + 0.5F;
                  float x = -t / 2.0F / radius + 0.5F;
                  drawShadowVertex(entry, vertices, j, p, r, s, u, w);
                  drawShadowVertex(entry, vertices, j, p, r, t, u, x);
                  drawShadowVertex(entry, vertices, j, q, r, t, v, x);
                  drawShadowVertex(entry, vertices, j, q, r, s, v, w);
               }

            }
         }
      }
   }

   private static void drawShadowVertex(MatrixStack.Entry entry, VertexConsumer vertices, float alpha, float x, float y, float z, float u, float v) {
      Vector3f vector3f = entry.getPositionMatrix().transformPosition(x, y, z, new Vector3f());
      vertices.vertex(vector3f.x(), vector3f.y(), vector3f.z(), 1.0F, 1.0F, 1.0F, alpha, u, v, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0.0F, 1.0F, 0.0F);
   }

   public void setWorld(@Nullable World world) {
      this.world = world;
      if (world == null) {
         this.camera = null;
      }

   }

   public double getSquaredDistanceToCamera(Entity entity) {
      return this.camera.getPos().squaredDistanceTo(entity.getPos());
   }

   public double getSquaredDistanceToCamera(double x, double y, double z) {
      return this.camera.getPos().squaredDistanceTo(x, y, z);
   }

   public Quaternionf getRotation() {
      return this.rotation;
   }

   public HeldItemRenderer getHeldItemRenderer() {
      return this.heldItemRenderer;
   }

   public void reload(ResourceManager manager) {
      EntityRendererFactory.Context lv = new EntityRendererFactory.Context(this, this.itemRenderer, this.blockRenderManager, this.heldItemRenderer, manager, this.modelLoader, this.textRenderer);
      this.renderers = EntityRenderers.reloadEntityRenderers(lv);
      this.modelRenderers = EntityRenderers.reloadPlayerRenderers(lv);
   }
}
