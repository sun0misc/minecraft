package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class BackgroundRenderer {
   private static final int field_32685 = 96;
   private static final List FOG_MODIFIERS = Lists.newArrayList(new StatusEffectFogModifier[]{new BlindnessFogModifier(), new DarknessFogModifier()});
   public static final float field_32684 = 5000.0F;
   private static float red;
   private static float green;
   private static float blue;
   private static int waterFogColor = -1;
   private static int nextWaterFogColor = -1;
   private static long lastWaterFogColorUpdateTime = -1L;

   public static void render(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness) {
      CameraSubmersionType lv = camera.getSubmersionType();
      Entity lv2 = camera.getFocusedEntity();
      float h;
      float r;
      float s;
      float u;
      float v;
      float w;
      if (lv == CameraSubmersionType.WATER) {
         long l = Util.getMeasuringTimeMs();
         int j = ((Biome)world.getBiome(BlockPos.ofFloored(camera.getPos())).value()).getWaterFogColor();
         if (lastWaterFogColorUpdateTime < 0L) {
            waterFogColor = j;
            nextWaterFogColor = j;
            lastWaterFogColorUpdateTime = l;
         }

         int k = waterFogColor >> 16 & 255;
         int m = waterFogColor >> 8 & 255;
         int n = waterFogColor & 255;
         int o = nextWaterFogColor >> 16 & 255;
         int p = nextWaterFogColor >> 8 & 255;
         int q = nextWaterFogColor & 255;
         h = MathHelper.clamp((float)(l - lastWaterFogColorUpdateTime) / 5000.0F, 0.0F, 1.0F);
         r = MathHelper.lerp(h, (float)o, (float)k);
         s = MathHelper.lerp(h, (float)p, (float)m);
         float t = MathHelper.lerp(h, (float)q, (float)n);
         red = r / 255.0F;
         green = s / 255.0F;
         blue = t / 255.0F;
         if (waterFogColor != j) {
            waterFogColor = j;
            nextWaterFogColor = MathHelper.floor(r) << 16 | MathHelper.floor(s) << 8 | MathHelper.floor(t);
            lastWaterFogColorUpdateTime = l;
         }
      } else if (lv == CameraSubmersionType.LAVA) {
         red = 0.6F;
         green = 0.1F;
         blue = 0.0F;
         lastWaterFogColorUpdateTime = -1L;
      } else if (lv == CameraSubmersionType.POWDER_SNOW) {
         red = 0.623F;
         green = 0.734F;
         blue = 0.785F;
         lastWaterFogColorUpdateTime = -1L;
         RenderSystem.clearColor(red, green, blue, 0.0F);
      } else {
         u = 0.25F + 0.75F * (float)viewDistance / 32.0F;
         u = 1.0F - (float)Math.pow((double)u, 0.25);
         Vec3d lv3 = world.getSkyColor(camera.getPos(), tickDelta);
         v = (float)lv3.x;
         w = (float)lv3.y;
         float x = (float)lv3.z;
         float y = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * 6.2831855F) * 2.0F + 0.5F, 0.0F, 1.0F);
         BiomeAccess lv4 = world.getBiomeAccess();
         Vec3d lv5 = camera.getPos().subtract(2.0, 2.0, 2.0).multiply(0.25);
         Vec3d lv6 = CubicSampler.sampleColor(lv5, (xx, yx, z) -> {
            return world.getDimensionEffects().adjustFogColor(Vec3d.unpackRgb(((Biome)lv4.getBiomeForNoiseGen(xx, yx, z).value()).getFogColor()), y);
         });
         red = (float)lv6.getX();
         green = (float)lv6.getY();
         blue = (float)lv6.getZ();
         if (viewDistance >= 4) {
            h = MathHelper.sin(world.getSkyAngleRadians(tickDelta)) > 0.0F ? -1.0F : 1.0F;
            Vector3f vector3f = new Vector3f(h, 0.0F, 0.0F);
            s = camera.getHorizontalPlane().dot(vector3f);
            if (s < 0.0F) {
               s = 0.0F;
            }

            if (s > 0.0F) {
               float[] fs = world.getDimensionEffects().getFogColorOverride(world.getSkyAngle(tickDelta), tickDelta);
               if (fs != null) {
                  s *= fs[3];
                  red = red * (1.0F - s) + fs[0] * s;
                  green = green * (1.0F - s) + fs[1] * s;
                  blue = blue * (1.0F - s) + fs[2] * s;
               }
            }
         }

         red += (v - red) * u;
         green += (w - green) * u;
         blue += (x - blue) * u;
         h = world.getRainGradient(tickDelta);
         if (h > 0.0F) {
            r = 1.0F - h * 0.5F;
            s = 1.0F - h * 0.4F;
            red *= r;
            green *= r;
            blue *= s;
         }

         r = world.getThunderGradient(tickDelta);
         if (r > 0.0F) {
            s = 1.0F - r * 0.5F;
            red *= s;
            green *= s;
            blue *= s;
         }

         lastWaterFogColorUpdateTime = -1L;
      }

      u = ((float)camera.getPos().y - (float)world.getBottomY()) * world.getLevelProperties().getHorizonShadingRatio();
      StatusEffectFogModifier lv7 = getFogModifier(lv2, tickDelta);
      if (lv7 != null) {
         LivingEntity lv8 = (LivingEntity)lv2;
         u = lv7.applyColorModifier(lv8, lv8.getStatusEffect(lv7.getStatusEffect()), u, tickDelta);
      }

      if (u < 1.0F && lv != CameraSubmersionType.LAVA && lv != CameraSubmersionType.POWDER_SNOW) {
         if (u < 0.0F) {
            u = 0.0F;
         }

         u *= u;
         red *= u;
         green *= u;
         blue *= u;
      }

      if (skyDarkness > 0.0F) {
         red = red * (1.0F - skyDarkness) + red * 0.7F * skyDarkness;
         green = green * (1.0F - skyDarkness) + green * 0.6F * skyDarkness;
         blue = blue * (1.0F - skyDarkness) + blue * 0.6F * skyDarkness;
      }

      if (lv == CameraSubmersionType.WATER) {
         if (lv2 instanceof ClientPlayerEntity) {
            v = ((ClientPlayerEntity)lv2).getUnderwaterVisibility();
         } else {
            v = 1.0F;
         }
      } else {
         label86: {
            if (lv2 instanceof LivingEntity) {
               LivingEntity lv9 = (LivingEntity)lv2;
               if (lv9.hasStatusEffect(StatusEffects.NIGHT_VISION) && !lv9.hasStatusEffect(StatusEffects.DARKNESS)) {
                  v = GameRenderer.getNightVisionStrength(lv9, tickDelta);
                  break label86;
               }
            }

            v = 0.0F;
         }
      }

      if (red != 0.0F && green != 0.0F && blue != 0.0F) {
         w = Math.min(1.0F / red, Math.min(1.0F / green, 1.0F / blue));
         red = red * (1.0F - v) + red * w * v;
         green = green * (1.0F - v) + green * w * v;
         blue = blue * (1.0F - v) + blue * w * v;
      }

      RenderSystem.clearColor(red, green, blue, 0.0F);
   }

   public static void clearFog() {
      RenderSystem.setShaderFogStart(Float.MAX_VALUE);
   }

   @Nullable
   private static StatusEffectFogModifier getFogModifier(Entity entity, float tickDelta) {
      if (entity instanceof LivingEntity lv) {
         return (StatusEffectFogModifier)FOG_MODIFIERS.stream().filter((modifier) -> {
            return modifier.shouldApply(lv, tickDelta);
         }).findFirst().orElse((Object)null);
      } else {
         return null;
      }
   }

   public static void applyFog(Camera camera, FogType fogType, float viewDistance, boolean thickFog, float tickDelta) {
      CameraSubmersionType lv = camera.getSubmersionType();
      Entity lv2 = camera.getFocusedEntity();
      FogData lv3 = new FogData(fogType);
      StatusEffectFogModifier lv4 = getFogModifier(lv2, tickDelta);
      if (lv == CameraSubmersionType.LAVA) {
         if (lv2.isSpectator()) {
            lv3.fogStart = -8.0F;
            lv3.fogEnd = viewDistance * 0.5F;
         } else if (lv2 instanceof LivingEntity && ((LivingEntity)lv2).hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            lv3.fogStart = 0.0F;
            lv3.fogEnd = 3.0F;
         } else {
            lv3.fogStart = 0.25F;
            lv3.fogEnd = 1.0F;
         }
      } else if (lv == CameraSubmersionType.POWDER_SNOW) {
         if (lv2.isSpectator()) {
            lv3.fogStart = -8.0F;
            lv3.fogEnd = viewDistance * 0.5F;
         } else {
            lv3.fogStart = 0.0F;
            lv3.fogEnd = 2.0F;
         }
      } else if (lv4 != null) {
         LivingEntity lv5 = (LivingEntity)lv2;
         StatusEffectInstance lv6 = lv5.getStatusEffect(lv4.getStatusEffect());
         if (lv6 != null) {
            lv4.applyStartEndModifier(lv3, lv5, lv6, viewDistance, tickDelta);
         }
      } else if (lv == CameraSubmersionType.WATER) {
         lv3.fogStart = -8.0F;
         lv3.fogEnd = 96.0F;
         if (lv2 instanceof ClientPlayerEntity) {
            ClientPlayerEntity lv7 = (ClientPlayerEntity)lv2;
            lv3.fogEnd *= Math.max(0.25F, lv7.getUnderwaterVisibility());
            RegistryEntry lv8 = lv7.world.getBiome(lv7.getBlockPos());
            if (lv8.isIn(BiomeTags.HAS_CLOSER_WATER_FOG)) {
               lv3.fogEnd *= 0.85F;
            }
         }

         if (lv3.fogEnd > viewDistance) {
            lv3.fogEnd = viewDistance;
            lv3.fogShape = FogShape.CYLINDER;
         }
      } else if (thickFog) {
         lv3.fogStart = viewDistance * 0.05F;
         lv3.fogEnd = Math.min(viewDistance, 192.0F) * 0.5F;
      } else if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
         lv3.fogStart = 0.0F;
         lv3.fogEnd = viewDistance;
         lv3.fogShape = FogShape.CYLINDER;
      } else {
         float h = MathHelper.clamp(viewDistance / 10.0F, 4.0F, 64.0F);
         lv3.fogStart = viewDistance - h;
         lv3.fogEnd = viewDistance;
         lv3.fogShape = FogShape.CYLINDER;
      }

      RenderSystem.setShaderFogStart(lv3.fogStart);
      RenderSystem.setShaderFogEnd(lv3.fogEnd);
      RenderSystem.setShaderFogShape(lv3.fogShape);
   }

   public static void setFogBlack() {
      RenderSystem.setShaderFogColor(red, green, blue);
   }

   @Environment(EnvType.CLIENT)
   private interface StatusEffectFogModifier {
      StatusEffect getStatusEffect();

      void applyStartEndModifier(FogData fogData, LivingEntity entity, StatusEffectInstance effect, float viewDistance, float tickDelta);

      default boolean shouldApply(LivingEntity entity, float tickDelta) {
         return entity.hasStatusEffect(this.getStatusEffect());
      }

      default float applyColorModifier(LivingEntity entity, StatusEffectInstance effect, float f, float tickDelta) {
         StatusEffectInstance lv = entity.getStatusEffect(this.getStatusEffect());
         if (lv != null) {
            if (lv.isDurationBelow(19)) {
               f = 1.0F - (float)lv.getDuration() / 20.0F;
            } else {
               f = 0.0F;
            }
         }

         return f;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class FogData {
      public final FogType fogType;
      public float fogStart;
      public float fogEnd;
      public FogShape fogShape;

      public FogData(FogType fogType) {
         this.fogShape = FogShape.SPHERE;
         this.fogType = fogType;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum FogType {
      FOG_SKY,
      FOG_TERRAIN;

      // $FF: synthetic method
      private static FogType[] method_36914() {
         return new FogType[]{FOG_SKY, FOG_TERRAIN};
      }
   }

   @Environment(EnvType.CLIENT)
   static class BlindnessFogModifier implements StatusEffectFogModifier {
      public StatusEffect getStatusEffect() {
         return StatusEffects.BLINDNESS;
      }

      public void applyStartEndModifier(FogData fogData, LivingEntity entity, StatusEffectInstance effect, float viewDistance, float tickDelta) {
         float h = effect.isInfinite() ? 5.0F : MathHelper.lerp(Math.min(1.0F, (float)effect.getDuration() / 20.0F), viewDistance, 5.0F);
         if (fogData.fogType == BackgroundRenderer.FogType.FOG_SKY) {
            fogData.fogStart = 0.0F;
            fogData.fogEnd = h * 0.8F;
         } else {
            fogData.fogStart = h * 0.25F;
            fogData.fogEnd = h;
         }

      }
   }

   @Environment(EnvType.CLIENT)
   static class DarknessFogModifier implements StatusEffectFogModifier {
      public StatusEffect getStatusEffect() {
         return StatusEffects.DARKNESS;
      }

      public void applyStartEndModifier(FogData fogData, LivingEntity entity, StatusEffectInstance effect, float viewDistance, float tickDelta) {
         if (!effect.getFactorCalculationData().isEmpty()) {
            float h = MathHelper.lerp(((StatusEffectInstance.FactorCalculationData)effect.getFactorCalculationData().get()).lerp(entity, tickDelta), viewDistance, 15.0F);
            fogData.fogStart = fogData.fogType == BackgroundRenderer.FogType.FOG_SKY ? 0.0F : h * 0.75F;
            fogData.fogEnd = h;
         }
      }

      public float applyColorModifier(LivingEntity entity, StatusEffectInstance effect, float f, float tickDelta) {
         return effect.getFactorCalculationData().isEmpty() ? 0.0F : 1.0F - ((StatusEffectInstance.FactorCalculationData)effect.getFactorCalculationData().get()).lerp(entity, tickDelta);
      }
   }
}
