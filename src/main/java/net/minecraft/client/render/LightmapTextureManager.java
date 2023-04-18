package net.minecraft.client.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class LightmapTextureManager implements AutoCloseable {
   public static final int MAX_LIGHT_COORDINATE = 15728880;
   public static final int MAX_SKY_LIGHT_COORDINATE = 15728640;
   public static final int MAX_BLOCK_LIGHT_COORDINATE = 240;
   private final NativeImageBackedTexture texture;
   private final NativeImage image;
   private final Identifier textureIdentifier;
   private boolean dirty;
   private float flickerIntensity;
   private final GameRenderer renderer;
   private final MinecraftClient client;

   public LightmapTextureManager(GameRenderer renderer, MinecraftClient client) {
      this.renderer = renderer;
      this.client = client;
      this.texture = new NativeImageBackedTexture(16, 16, false);
      this.textureIdentifier = this.client.getTextureManager().registerDynamicTexture("light_map", this.texture);
      this.image = this.texture.getImage();

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            this.image.setColor(j, i, -1);
         }
      }

      this.texture.upload();
   }

   public void close() {
      this.texture.close();
   }

   public void tick() {
      this.flickerIntensity += (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
      this.flickerIntensity *= 0.9F;
      this.dirty = true;
   }

   public void disable() {
      RenderSystem.setShaderTexture(2, 0);
   }

   public void enable() {
      RenderSystem.setShaderTexture(2, this.textureIdentifier);
      this.client.getTextureManager().bindTexture(this.textureIdentifier);
      RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
      RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
   }

   private float getDarknessFactor(float delta) {
      if (this.client.player.hasStatusEffect(StatusEffects.DARKNESS)) {
         StatusEffectInstance lv = this.client.player.getStatusEffect(StatusEffects.DARKNESS);
         if (lv != null && lv.getFactorCalculationData().isPresent()) {
            return ((StatusEffectInstance.FactorCalculationData)lv.getFactorCalculationData().get()).lerp(this.client.player, delta);
         }
      }

      return 0.0F;
   }

   private float getDarkness(LivingEntity entity, float factor, float delta) {
      float h = 0.45F * factor;
      return Math.max(0.0F, MathHelper.cos(((float)entity.age - delta) * 3.1415927F * 0.025F) * h);
   }

   public void update(float delta) {
      if (this.dirty) {
         this.dirty = false;
         this.client.getProfiler().push("lightTex");
         ClientWorld lv = this.client.world;
         if (lv != null) {
            float g = lv.getSkyBrightness(1.0F);
            float h;
            if (lv.getLightningTicksLeft() > 0) {
               h = 1.0F;
            } else {
               h = g * 0.95F + 0.05F;
            }

            float i = ((Double)this.client.options.getDarknessEffectScale().getValue()).floatValue();
            float j = this.getDarknessFactor(delta) * i;
            float k = this.getDarkness(this.client.player, j, delta) * i;
            float l = this.client.player.getUnderwaterVisibility();
            float m;
            if (this.client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
               m = GameRenderer.getNightVisionStrength(this.client.player, delta);
            } else if (l > 0.0F && this.client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
               m = l;
            } else {
               m = 0.0F;
            }

            Vector3f vector3f = (new Vector3f(g, g, 1.0F)).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
            float n = this.flickerIntensity + 1.5F;
            Vector3f vector3f2 = new Vector3f();

            for(int o = 0; o < 16; ++o) {
               for(int p = 0; p < 16; ++p) {
                  float q = getBrightness(lv.getDimension(), o) * h;
                  float r = getBrightness(lv.getDimension(), p) * n;
                  float t = r * ((r * 0.6F + 0.4F) * 0.6F + 0.4F);
                  float u = r * (r * r * 0.6F + 0.4F);
                  vector3f2.set(r, t, u);
                  boolean bl = lv.getDimensionEffects().shouldBrightenLighting();
                  float v;
                  Vector3f vector3f4;
                  if (bl) {
                     vector3f2.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
                     clamp(vector3f2);
                  } else {
                     Vector3f vector3f3 = (new Vector3f(vector3f)).mul(q);
                     vector3f2.add(vector3f3);
                     vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                     if (this.renderer.getSkyDarkness(delta) > 0.0F) {
                        v = this.renderer.getSkyDarkness(delta);
                        vector3f4 = (new Vector3f(vector3f2)).mul(0.7F, 0.6F, 0.6F);
                        vector3f2.lerp(vector3f4, v);
                     }
                  }

                  float w;
                  if (m > 0.0F) {
                     w = Math.max(vector3f2.x(), Math.max(vector3f2.y(), vector3f2.z()));
                     if (w < 1.0F) {
                        v = 1.0F / w;
                        vector3f4 = (new Vector3f(vector3f2)).mul(v);
                        vector3f2.lerp(vector3f4, m);
                     }
                  }

                  if (!bl) {
                     if (k > 0.0F) {
                        vector3f2.add(-k, -k, -k);
                     }

                     clamp(vector3f2);
                  }

                  w = ((Double)this.client.options.getGamma().getValue()).floatValue();
                  Vector3f vector3f5 = new Vector3f(this.easeOutQuart(vector3f2.x), this.easeOutQuart(vector3f2.y), this.easeOutQuart(vector3f2.z));
                  vector3f2.lerp(vector3f5, Math.max(0.0F, w - j));
                  vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                  clamp(vector3f2);
                  vector3f2.mul(255.0F);
                  int x = true;
                  int y = (int)vector3f2.x();
                  int z = (int)vector3f2.y();
                  int aa = (int)vector3f2.z();
                  this.image.setColor(p, o, -16777216 | aa << 16 | z << 8 | y);
               }
            }

            this.texture.upload();
            this.client.getProfiler().pop();
         }
      }
   }

   private static void clamp(Vector3f vec) {
      vec.set(MathHelper.clamp(vec.x, 0.0F, 1.0F), MathHelper.clamp(vec.y, 0.0F, 1.0F), MathHelper.clamp(vec.z, 0.0F, 1.0F));
   }

   private float easeOutQuart(float x) {
      float g = 1.0F - x;
      return 1.0F - g * g * g * g;
   }

   public static float getBrightness(DimensionType type, int lightLevel) {
      float f = (float)lightLevel / 15.0F;
      float g = f / (4.0F - 3.0F * f);
      return MathHelper.lerp(type.ambientLight(), g, 1.0F);
   }

   public static int pack(int block, int sky) {
      return block << 4 | sky << 20;
   }

   public static int getBlockLightCoordinates(int light) {
      return light >> 4 & (MAX_BLOCK_LIGHT_COORDINATE | '／');
   }

   public static int getSkyLightCoordinates(int light) {
      return light >> 20 & (MAX_BLOCK_LIGHT_COORDINATE | '／');
   }
}
