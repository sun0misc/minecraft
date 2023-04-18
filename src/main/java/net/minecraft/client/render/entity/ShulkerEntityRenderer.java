package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.feature.ShulkerHeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ShulkerEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE;
   private static final Identifier[] COLORED_TEXTURES;

   public ShulkerEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new ShulkerEntityModel(arg.getPart(EntityModelLayers.SHULKER)), 0.0F);
      this.addFeature(new ShulkerHeadFeatureRenderer(this));
   }

   public Vec3d getPositionOffset(ShulkerEntity arg, float f) {
      return (Vec3d)arg.getRenderPositionOffset(f).orElse(super.getPositionOffset(arg, f));
   }

   public boolean shouldRender(ShulkerEntity arg, Frustum arg2, double d, double e, double f) {
      return super.shouldRender((MobEntity)arg, arg2, d, e, f) ? true : arg.getRenderPositionOffset(0.0F).filter((renderPositionOffset) -> {
         EntityType lv = arg.getType();
         float f = lv.getHeight() / 2.0F;
         float g = lv.getWidth() / 2.0F;
         Vec3d lv2 = Vec3d.ofBottomCenter(arg.getBlockPos());
         return arg2.isVisible((new Box(renderPositionOffset.x, renderPositionOffset.y + (double)f, renderPositionOffset.z, lv2.x, lv2.y + (double)f, lv2.z)).expand((double)g, (double)f, (double)g));
      }).isPresent();
   }

   public Identifier getTexture(ShulkerEntity arg) {
      return getTexture(arg.getColor());
   }

   public static Identifier getTexture(@Nullable DyeColor shulkerColor) {
      return shulkerColor == null ? TEXTURE : COLORED_TEXTURES[shulkerColor.getId()];
   }

   protected void setupTransforms(ShulkerEntity arg, MatrixStack arg2, float f, float g, float h) {
      super.setupTransforms(arg, arg2, f, g + 180.0F, h);
      arg2.translate(0.0, 0.5, 0.0);
      arg2.multiply(arg.getAttachedFace().getOpposite().getRotationQuaternion());
      arg2.translate(0.0, -0.5, 0.0);
   }

   static {
      TEXTURE = new Identifier("textures/" + TexturedRenderLayers.SHULKER_TEXTURE_ID.getTextureId().getPath() + ".png");
      COLORED_TEXTURES = (Identifier[])TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.stream().map((spriteId) -> {
         return new Identifier("textures/" + spriteId.getTextureId().getPath() + ".png");
      }).toArray((i) -> {
         return new Identifier[i];
      });
   }
}
