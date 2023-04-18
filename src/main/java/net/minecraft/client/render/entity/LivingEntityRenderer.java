package net.minecraft.client.render.entity;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public abstract class LivingEntityRenderer extends EntityRenderer implements FeatureRendererContext {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float field_32939 = 0.1F;
   protected EntityModel model;
   protected final List features = Lists.newArrayList();

   public LivingEntityRenderer(EntityRendererFactory.Context ctx, EntityModel model, float shadowRadius) {
      super(ctx);
      this.model = model;
      this.shadowRadius = shadowRadius;
   }

   protected final boolean addFeature(FeatureRenderer feature) {
      return this.features.add(feature);
   }

   public EntityModel getModel() {
      return this.model;
   }

   public void render(LivingEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      this.model.handSwingProgress = this.getHandSwingProgress(arg, g);
      this.model.riding = arg.hasVehicle();
      this.model.child = arg.isBaby();
      float h = MathHelper.lerpAngleDegrees(g, arg.prevBodyYaw, arg.bodyYaw);
      float j = MathHelper.lerpAngleDegrees(g, arg.prevHeadYaw, arg.headYaw);
      float k = j - h;
      float l;
      if (arg.hasVehicle() && arg.getVehicle() instanceof LivingEntity) {
         LivingEntity lv = (LivingEntity)arg.getVehicle();
         h = MathHelper.lerpAngleDegrees(g, lv.prevBodyYaw, lv.bodyYaw);
         k = j - h;
         l = MathHelper.wrapDegrees(k);
         if (l < -85.0F) {
            l = -85.0F;
         }

         if (l >= 85.0F) {
            l = 85.0F;
         }

         h = j - l;
         if (l * l > 2500.0F) {
            h += l * 0.2F;
         }

         k = j - h;
      }

      float m = MathHelper.lerp(g, arg.prevPitch, arg.getPitch());
      if (shouldFlipUpsideDown(arg)) {
         m *= -1.0F;
         k *= -1.0F;
      }

      float n;
      if (arg.isInPose(EntityPose.SLEEPING)) {
         Direction lv2 = arg.getSleepingDirection();
         if (lv2 != null) {
            n = arg.getEyeHeight(EntityPose.STANDING) - 0.1F;
            arg2.translate((float)(-lv2.getOffsetX()) * n, 0.0F, (float)(-lv2.getOffsetZ()) * n);
         }
      }

      l = this.getAnimationProgress(arg, g);
      this.setupTransforms(arg, arg2, l, h, g);
      arg2.scale(-1.0F, -1.0F, 1.0F);
      this.scale(arg, arg2, g);
      arg2.translate(0.0F, -1.501F, 0.0F);
      n = 0.0F;
      float o = 0.0F;
      if (!arg.hasVehicle() && arg.isAlive()) {
         n = arg.limbAnimator.getSpeed(g);
         o = arg.limbAnimator.getPos(g);
         if (arg.isBaby()) {
            o *= 3.0F;
         }

         if (n > 1.0F) {
            n = 1.0F;
         }
      }

      this.model.animateModel(arg, o, n, g);
      this.model.setAngles(arg, o, n, l, k, m);
      MinecraftClient lv3 = MinecraftClient.getInstance();
      boolean bl = this.isVisible(arg);
      boolean bl2 = !bl && !arg.isInvisibleTo(lv3.player);
      boolean bl3 = lv3.hasOutline(arg);
      RenderLayer lv4 = this.getRenderLayer(arg, bl, bl2, bl3);
      if (lv4 != null) {
         VertexConsumer lv5 = arg3.getBuffer(lv4);
         int p = getOverlay(arg, this.getAnimationCounter(arg, g));
         this.model.render(arg2, lv5, i, p, 1.0F, 1.0F, 1.0F, bl2 ? 0.15F : 1.0F);
      }

      if (!arg.isSpectator()) {
         Iterator var23 = this.features.iterator();

         while(var23.hasNext()) {
            FeatureRenderer lv6 = (FeatureRenderer)var23.next();
            lv6.render(arg2, arg3, i, arg, o, n, g, l, k, m);
         }
      }

      arg2.pop();
      super.render(arg, f, g, arg2, arg3, i);
   }

   @Nullable
   protected RenderLayer getRenderLayer(LivingEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
      Identifier lv = this.getTexture(entity);
      if (translucent) {
         return RenderLayer.getItemEntityTranslucentCull(lv);
      } else if (showBody) {
         return this.model.getLayer(lv);
      } else {
         return showOutline ? RenderLayer.getOutline(lv) : null;
      }
   }

   public static int getOverlay(LivingEntity entity, float whiteOverlayProgress) {
      return OverlayTexture.packUv(OverlayTexture.getU(whiteOverlayProgress), OverlayTexture.getV(entity.hurtTime > 0 || entity.deathTime > 0));
   }

   protected boolean isVisible(LivingEntity entity) {
      return !entity.isInvisible();
   }

   private static float getYaw(Direction direction) {
      switch (direction) {
         case SOUTH:
            return 90.0F;
         case WEST:
            return 0.0F;
         case NORTH:
            return 270.0F;
         case EAST:
            return 180.0F;
         default:
            return 0.0F;
      }
   }

   protected boolean isShaking(LivingEntity entity) {
      return entity.isFrozen();
   }

   protected void setupTransforms(LivingEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
      if (this.isShaking(entity)) {
         bodyYaw += (float)(Math.cos((double)entity.age * 3.25) * Math.PI * 0.4000000059604645);
      }

      if (!entity.isInPose(EntityPose.SLEEPING)) {
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - bodyYaw));
      }

      if (entity.deathTime > 0) {
         float i = ((float)entity.deathTime + tickDelta - 1.0F) / 20.0F * 1.6F;
         i = MathHelper.sqrt(i);
         if (i > 1.0F) {
            i = 1.0F;
         }

         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * this.getLyingAngle(entity)));
      } else if (entity.isUsingRiptide()) {
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F - entity.getPitch()));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float)entity.age + tickDelta) * -75.0F));
      } else if (entity.isInPose(EntityPose.SLEEPING)) {
         Direction lv = entity.getSleepingDirection();
         float j = lv != null ? getYaw(lv) : bodyYaw;
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.getLyingAngle(entity)));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0F));
      } else if (shouldFlipUpsideDown(entity)) {
         matrices.translate(0.0F, entity.getHeight() + 0.1F, 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
      }

   }

   protected float getHandSwingProgress(LivingEntity entity, float tickDelta) {
      return entity.getHandSwingProgress(tickDelta);
   }

   protected float getAnimationProgress(LivingEntity entity, float tickDelta) {
      return (float)entity.age + tickDelta;
   }

   protected float getLyingAngle(LivingEntity entity) {
      return 90.0F;
   }

   protected float getAnimationCounter(LivingEntity entity, float tickDelta) {
      return 0.0F;
   }

   protected void scale(LivingEntity entity, MatrixStack matrices, float amount) {
   }

   protected boolean hasLabel(LivingEntity arg) {
      double d = this.dispatcher.getSquaredDistanceToCamera(arg);
      float f = arg.isSneaky() ? 32.0F : 64.0F;
      if (d >= (double)(f * f)) {
         return false;
      } else {
         MinecraftClient lv = MinecraftClient.getInstance();
         ClientPlayerEntity lv2 = lv.player;
         boolean bl = !arg.isInvisibleTo(lv2);
         if (arg != lv2) {
            AbstractTeam lv3 = arg.getScoreboardTeam();
            AbstractTeam lv4 = lv2.getScoreboardTeam();
            if (lv3 != null) {
               AbstractTeam.VisibilityRule lv5 = lv3.getNameTagVisibilityRule();
               switch (lv5) {
                  case ALWAYS:
                     return bl;
                  case NEVER:
                     return false;
                  case HIDE_FOR_OTHER_TEAMS:
                     return lv4 == null ? bl : lv3.isEqual(lv4) && (lv3.shouldShowFriendlyInvisibles() || bl);
                  case HIDE_FOR_OWN_TEAM:
                     return lv4 == null ? bl : !lv3.isEqual(lv4) && bl;
                  default:
                     return true;
               }
            }
         }

         return MinecraftClient.isHudEnabled() && arg != lv.getCameraEntity() && bl && !arg.hasPassengers();
      }
   }

   public static boolean shouldFlipUpsideDown(LivingEntity entity) {
      if (entity instanceof PlayerEntity || entity.hasCustomName()) {
         String string = Formatting.strip(entity.getName().getString());
         if ("Dinnerbone".equals(string) || "Grumm".equals(string)) {
            return !(entity instanceof PlayerEntity) || ((PlayerEntity)entity).isPartVisible(PlayerModelPart.CAPE);
         }
      }

      return false;
   }
}
