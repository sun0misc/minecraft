package net.minecraft.client.render.block.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PiglinHeadEntityModel;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SkullBlockEntityRenderer implements BlockEntityRenderer {
   private final Map MODELS;
   private static final Map TEXTURES = (Map)Util.make(Maps.newHashMap(), (map) -> {
      map.put(SkullBlock.Type.SKELETON, new Identifier("textures/entity/skeleton/skeleton.png"));
      map.put(SkullBlock.Type.WITHER_SKELETON, new Identifier("textures/entity/skeleton/wither_skeleton.png"));
      map.put(SkullBlock.Type.ZOMBIE, new Identifier("textures/entity/zombie/zombie.png"));
      map.put(SkullBlock.Type.CREEPER, new Identifier("textures/entity/creeper/creeper.png"));
      map.put(SkullBlock.Type.DRAGON, new Identifier("textures/entity/enderdragon/dragon.png"));
      map.put(SkullBlock.Type.PIGLIN, new Identifier("textures/entity/piglin/piglin.png"));
      map.put(SkullBlock.Type.PLAYER, DefaultSkinHelper.getTexture());
   });

   public static Map getModels(EntityModelLoader modelLoader) {
      ImmutableMap.Builder builder = ImmutableMap.builder();
      builder.put(SkullBlock.Type.SKELETON, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.SKELETON_SKULL)));
      builder.put(SkullBlock.Type.WITHER_SKELETON, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.WITHER_SKELETON_SKULL)));
      builder.put(SkullBlock.Type.PLAYER, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.PLAYER_HEAD)));
      builder.put(SkullBlock.Type.ZOMBIE, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.ZOMBIE_HEAD)));
      builder.put(SkullBlock.Type.CREEPER, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.CREEPER_HEAD)));
      builder.put(SkullBlock.Type.DRAGON, new DragonHeadEntityModel(modelLoader.getModelPart(EntityModelLayers.DRAGON_SKULL)));
      builder.put(SkullBlock.Type.PIGLIN, new PiglinHeadEntityModel(modelLoader.getModelPart(EntityModelLayers.PIGLIN_HEAD)));
      return builder.build();
   }

   public SkullBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.MODELS = getModels(ctx.getLayerRenderDispatcher());
   }

   public void render(SkullBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      float g = arg.getPoweredTicks(f);
      BlockState lv = arg.getCachedState();
      boolean bl = lv.getBlock() instanceof WallSkullBlock;
      Direction lv2 = bl ? (Direction)lv.get(WallSkullBlock.FACING) : null;
      int k = bl ? RotationPropertyHelper.fromDirection(lv2.getOpposite()) : (Integer)lv.get(SkullBlock.ROTATION);
      float h = RotationPropertyHelper.toDegrees(k);
      SkullBlock.SkullType lv3 = ((AbstractSkullBlock)lv.getBlock()).getSkullType();
      SkullBlockEntityModel lv4 = (SkullBlockEntityModel)this.MODELS.get(lv3);
      RenderLayer lv5 = getRenderLayer(lv3, arg.getOwner());
      renderSkull(lv2, h, g, arg2, arg3, i, lv4, lv5);
   }

   public static void renderSkull(@Nullable Direction direction, float yaw, float animationProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SkullBlockEntityModel model, RenderLayer renderLayer) {
      matrices.push();
      if (direction == null) {
         matrices.translate(0.5F, 0.0F, 0.5F);
      } else {
         float h = 0.25F;
         matrices.translate(0.5F - (float)direction.getOffsetX() * 0.25F, 0.25F, 0.5F - (float)direction.getOffsetZ() * 0.25F);
      }

      matrices.scale(-1.0F, -1.0F, 1.0F);
      VertexConsumer lv = vertexConsumers.getBuffer(renderLayer);
      model.setHeadRotation(animationProgress, yaw, 0.0F);
      model.render(matrices, lv, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
      matrices.pop();
   }

   public static RenderLayer getRenderLayer(SkullBlock.SkullType type, @Nullable GameProfile profile) {
      Identifier lv = (Identifier)TEXTURES.get(type);
      if (type == SkullBlock.Type.PLAYER && profile != null) {
         MinecraftClient lv2 = MinecraftClient.getInstance();
         Map map = lv2.getSkinProvider().getTextures(profile);
         return map.containsKey(Type.SKIN) ? RenderLayer.getEntityTranslucent(lv2.getSkinProvider().loadSkin((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN)) : RenderLayer.getEntityCutoutNoCull(DefaultSkinHelper.getTexture(Uuids.getUuidFromProfile(profile)));
      } else {
         return RenderLayer.getEntityCutoutNoCullZOffset(lv);
      }
   }
}
