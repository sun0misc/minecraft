package net.minecraft.client.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class DimensionEffects {
   private static final Object2ObjectMap BY_IDENTIFIER = (Object2ObjectMap)Util.make(new Object2ObjectArrayMap(), (map) -> {
      Overworld lv = new Overworld();
      map.defaultReturnValue(lv);
      map.put(DimensionTypes.OVERWORLD_ID, lv);
      map.put(DimensionTypes.THE_NETHER_ID, new Nether());
      map.put(DimensionTypes.THE_END_ID, new End());
   });
   private final float[] rgba = new float[4];
   private final float cloudsHeight;
   private final boolean alternateSkyColor;
   private final SkyType skyType;
   private final boolean brightenLighting;
   private final boolean darkened;

   public DimensionEffects(float cloudsHeight, boolean alternateSkyColor, SkyType skyType, boolean brightenLighting, boolean darkened) {
      this.cloudsHeight = cloudsHeight;
      this.alternateSkyColor = alternateSkyColor;
      this.skyType = skyType;
      this.brightenLighting = brightenLighting;
      this.darkened = darkened;
   }

   public static DimensionEffects byDimensionType(DimensionType dimensionType) {
      return (DimensionEffects)BY_IDENTIFIER.get(dimensionType.effects());
   }

   @Nullable
   public float[] getFogColorOverride(float skyAngle, float tickDelta) {
      float h = 0.4F;
      float i = MathHelper.cos(skyAngle * 6.2831855F) - 0.0F;
      float j = -0.0F;
      if (i >= -0.4F && i <= 0.4F) {
         float k = (i - -0.0F) / 0.4F * 0.5F + 0.5F;
         float l = 1.0F - (1.0F - MathHelper.sin(k * 3.1415927F)) * 0.99F;
         l *= l;
         this.rgba[0] = k * 0.3F + 0.7F;
         this.rgba[1] = k * k * 0.7F + 0.2F;
         this.rgba[2] = k * k * 0.0F + 0.2F;
         this.rgba[3] = l;
         return this.rgba;
      } else {
         return null;
      }
   }

   public float getCloudsHeight() {
      return this.cloudsHeight;
   }

   public boolean isAlternateSkyColor() {
      return this.alternateSkyColor;
   }

   public abstract Vec3d adjustFogColor(Vec3d color, float sunHeight);

   public abstract boolean useThickFog(int camX, int camY);

   public SkyType getSkyType() {
      return this.skyType;
   }

   public boolean shouldBrightenLighting() {
      return this.brightenLighting;
   }

   public boolean isDarkened() {
      return this.darkened;
   }

   @Environment(EnvType.CLIENT)
   public static enum SkyType {
      NONE,
      NORMAL,
      END;

      // $FF: synthetic method
      private static SkyType[] method_36912() {
         return new SkyType[]{NONE, NORMAL, END};
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Overworld extends DimensionEffects {
      public static final int CLOUDS_HEIGHT = 192;

      public Overworld() {
         super(192.0F, true, DimensionEffects.SkyType.NORMAL, false, false);
      }

      public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
         return color.multiply((double)(sunHeight * 0.94F + 0.06F), (double)(sunHeight * 0.94F + 0.06F), (double)(sunHeight * 0.91F + 0.09F));
      }

      public boolean useThickFog(int camX, int camY) {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Nether extends DimensionEffects {
      public Nether() {
         super(Float.NaN, true, DimensionEffects.SkyType.NONE, false, true);
      }

      public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
         return color;
      }

      public boolean useThickFog(int camX, int camY) {
         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class End extends DimensionEffects {
      public End() {
         super(Float.NaN, false, DimensionEffects.SkyType.END, true, false);
      }

      public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
         return color.multiply(0.15000000596046448);
      }

      public boolean useThickFog(int camX, int camY) {
         return false;
      }

      @Nullable
      public float[] getFogColorOverride(float skyAngle, float tickDelta) {
         return null;
      }
   }
}
