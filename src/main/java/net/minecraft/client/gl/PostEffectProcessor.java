package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidHierarchicalFileException;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class PostEffectProcessor implements AutoCloseable {
   private static final String MAIN_TARGET_NAME = "minecraft:main";
   private final Framebuffer mainTarget;
   private final ResourceManager resourceManager;
   private final String name;
   private final List passes = Lists.newArrayList();
   private final Map targetsByName = Maps.newHashMap();
   private final List defaultSizedTargets = Lists.newArrayList();
   private Matrix4f projectionMatrix;
   private int width;
   private int height;
   private float time;
   private float lastTickDelta;

   public PostEffectProcessor(TextureManager textureManager, ResourceManager resourceManager, Framebuffer framebuffer, Identifier id) throws IOException, JsonSyntaxException {
      this.resourceManager = resourceManager;
      this.mainTarget = framebuffer;
      this.time = 0.0F;
      this.lastTickDelta = 0.0F;
      this.width = framebuffer.viewportWidth;
      this.height = framebuffer.viewportHeight;
      this.name = id.toString();
      this.setupProjectionMatrix();
      this.parseEffect(textureManager, id);
   }

   private void parseEffect(TextureManager textureManager, Identifier id) throws IOException, JsonSyntaxException {
      Resource lv = this.resourceManager.getResourceOrThrow(id);

      try {
         Reader reader = lv.getReader();

         try {
            JsonObject jsonObject = JsonHelper.deserialize((Reader)reader);
            JsonArray jsonArray;
            int i;
            Iterator var8;
            JsonElement jsonElement;
            InvalidHierarchicalFileException lv2;
            if (JsonHelper.hasArray(jsonObject, "targets")) {
               jsonArray = jsonObject.getAsJsonArray("targets");
               i = 0;

               for(var8 = jsonArray.iterator(); var8.hasNext(); ++i) {
                  jsonElement = (JsonElement)var8.next();

                  try {
                     this.parseTarget(jsonElement);
                  } catch (Exception var14) {
                     lv2 = InvalidHierarchicalFileException.wrap(var14);
                     lv2.addInvalidKey("targets[" + i + "]");
                     throw lv2;
                  }
               }
            }

            if (JsonHelper.hasArray(jsonObject, "passes")) {
               jsonArray = jsonObject.getAsJsonArray("passes");
               i = 0;

               for(var8 = jsonArray.iterator(); var8.hasNext(); ++i) {
                  jsonElement = (JsonElement)var8.next();

                  try {
                     this.parsePass(textureManager, jsonElement);
                  } catch (Exception var13) {
                     lv2 = InvalidHierarchicalFileException.wrap(var13);
                     lv2.addInvalidKey("passes[" + i + "]");
                     throw lv2;
                  }
               }
            }
         } catch (Throwable var15) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var12) {
                  var15.addSuppressed(var12);
               }
            }

            throw var15;
         }

         if (reader != null) {
            reader.close();
         }

      } catch (Exception var16) {
         InvalidHierarchicalFileException lv3 = InvalidHierarchicalFileException.wrap(var16);
         String var10001 = id.getPath();
         lv3.addInvalidFile(var10001 + " (" + lv.getResourcePackName() + ")");
         throw lv3;
      }
   }

   private void parseTarget(JsonElement jsonTarget) throws InvalidHierarchicalFileException {
      if (JsonHelper.isString(jsonTarget)) {
         this.addTarget(jsonTarget.getAsString(), this.width, this.height);
      } else {
         JsonObject jsonObject = JsonHelper.asObject(jsonTarget, "target");
         String string = JsonHelper.getString(jsonObject, "name");
         int i = JsonHelper.getInt(jsonObject, "width", this.width);
         int j = JsonHelper.getInt(jsonObject, "height", this.height);
         if (this.targetsByName.containsKey(string)) {
            throw new InvalidHierarchicalFileException(string + " is already defined");
         }

         this.addTarget(string, i, j);
      }

   }

   private void parsePass(TextureManager textureManager, JsonElement jsonPass) throws IOException {
      JsonObject jsonObject = JsonHelper.asObject(jsonPass, "pass");
      String string = JsonHelper.getString(jsonObject, "name");
      String string2 = JsonHelper.getString(jsonObject, "intarget");
      String string3 = JsonHelper.getString(jsonObject, "outtarget");
      Framebuffer lv = this.getTarget(string2);
      Framebuffer lv2 = this.getTarget(string3);
      if (lv == null) {
         throw new InvalidHierarchicalFileException("Input target '" + string2 + "' does not exist");
      } else if (lv2 == null) {
         throw new InvalidHierarchicalFileException("Output target '" + string3 + "' does not exist");
      } else {
         PostEffectPass lv3 = this.addPass(string, lv, lv2);
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "auxtargets", (JsonArray)null);
         if (jsonArray != null) {
            int i = 0;

            for(Iterator var12 = jsonArray.iterator(); var12.hasNext(); ++i) {
               JsonElement jsonElement2 = (JsonElement)var12.next();

               try {
                  JsonObject jsonObject2 = JsonHelper.asObject(jsonElement2, "auxtarget");
                  String string4 = JsonHelper.getString(jsonObject2, "name");
                  String string5 = JsonHelper.getString(jsonObject2, "id");
                  boolean bl;
                  String string6;
                  if (string5.endsWith(":depth")) {
                     bl = true;
                     string6 = string5.substring(0, string5.lastIndexOf(58));
                  } else {
                     bl = false;
                     string6 = string5;
                  }

                  Framebuffer lv4 = this.getTarget(string6);
                  if (lv4 == null) {
                     if (bl) {
                        throw new InvalidHierarchicalFileException("Render target '" + string6 + "' can't be used as depth buffer");
                     }

                     Identifier lv5 = new Identifier("textures/effect/" + string6 + ".png");
                     this.resourceManager.getResource(lv5).orElseThrow(() -> {
                        return new InvalidHierarchicalFileException("Render target or texture '" + string6 + "' does not exist");
                     });
                     RenderSystem.setShaderTexture(0, lv5);
                     textureManager.bindTexture(lv5);
                     AbstractTexture lv6 = textureManager.getTexture(lv5);
                     int j = JsonHelper.getInt(jsonObject2, "width");
                     int k = JsonHelper.getInt(jsonObject2, "height");
                     boolean bl2 = JsonHelper.getBoolean(jsonObject2, "bilinear");
                     if (bl2) {
                        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
                        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
                     } else {
                        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
                        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
                     }

                     Objects.requireNonNull(lv6);
                     lv3.addAuxTarget(string4, lv6::getGlId, j, k);
                  } else if (bl) {
                     Objects.requireNonNull(lv4);
                     lv3.addAuxTarget(string4, lv4::getDepthAttachment, lv4.textureWidth, lv4.textureHeight);
                  } else {
                     Objects.requireNonNull(lv4);
                     lv3.addAuxTarget(string4, lv4::getColorAttachment, lv4.textureWidth, lv4.textureHeight);
                  }
               } catch (Exception var26) {
                  InvalidHierarchicalFileException lv7 = InvalidHierarchicalFileException.wrap(var26);
                  lv7.addInvalidKey("auxtargets[" + i + "]");
                  throw lv7;
               }
            }
         }

         JsonArray jsonArray2 = JsonHelper.getArray(jsonObject, "uniforms", (JsonArray)null);
         if (jsonArray2 != null) {
            int l = 0;

            for(Iterator var29 = jsonArray2.iterator(); var29.hasNext(); ++l) {
               JsonElement jsonElement3 = (JsonElement)var29.next();

               try {
                  this.parseUniform(jsonElement3);
               } catch (Exception var25) {
                  InvalidHierarchicalFileException lv8 = InvalidHierarchicalFileException.wrap(var25);
                  lv8.addInvalidKey("uniforms[" + l + "]");
                  throw lv8;
               }
            }
         }

      }
   }

   private void parseUniform(JsonElement jsonUniform) throws InvalidHierarchicalFileException {
      JsonObject jsonObject = JsonHelper.asObject(jsonUniform, "uniform");
      String string = JsonHelper.getString(jsonObject, "name");
      GlUniform lv = ((PostEffectPass)this.passes.get(this.passes.size() - 1)).getProgram().getUniformByName(string);
      if (lv == null) {
         throw new InvalidHierarchicalFileException("Uniform '" + string + "' does not exist");
      } else {
         float[] fs = new float[4];
         int i = 0;
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "values");

         for(Iterator var8 = jsonArray.iterator(); var8.hasNext(); ++i) {
            JsonElement jsonElement2 = (JsonElement)var8.next();

            try {
               fs[i] = JsonHelper.asFloat(jsonElement2, "value");
            } catch (Exception var12) {
               InvalidHierarchicalFileException lv2 = InvalidHierarchicalFileException.wrap(var12);
               lv2.addInvalidKey("values[" + i + "]");
               throw lv2;
            }
         }

         switch (i) {
            case 0:
            default:
               break;
            case 1:
               lv.set(fs[0]);
               break;
            case 2:
               lv.set(fs[0], fs[1]);
               break;
            case 3:
               lv.set(fs[0], fs[1], fs[2]);
               break;
            case 4:
               lv.setAndFlip(fs[0], fs[1], fs[2], fs[3]);
         }

      }
   }

   public Framebuffer getSecondaryTarget(String name) {
      return (Framebuffer)this.targetsByName.get(name);
   }

   public void addTarget(String name, int width, int height) {
      Framebuffer lv = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
      lv.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.targetsByName.put(name, lv);
      if (width == this.width && height == this.height) {
         this.defaultSizedTargets.add(lv);
      }

   }

   public void close() {
      Iterator var1 = this.targetsByName.values().iterator();

      while(var1.hasNext()) {
         Framebuffer lv = (Framebuffer)var1.next();
         lv.delete();
      }

      var1 = this.passes.iterator();

      while(var1.hasNext()) {
         PostEffectPass lv2 = (PostEffectPass)var1.next();
         lv2.close();
      }

      this.passes.clear();
   }

   public PostEffectPass addPass(String programName, Framebuffer source, Framebuffer dest) throws IOException {
      PostEffectPass lv = new PostEffectPass(this.resourceManager, programName, source, dest);
      this.passes.add(this.passes.size(), lv);
      return lv;
   }

   private void setupProjectionMatrix() {
      this.projectionMatrix = (new Matrix4f()).setOrtho(0.0F, (float)this.mainTarget.textureWidth, 0.0F, (float)this.mainTarget.textureHeight, 0.1F, 1000.0F);
   }

   public void setupDimensions(int targetsWidth, int targetsHeight) {
      this.width = this.mainTarget.textureWidth;
      this.height = this.mainTarget.textureHeight;
      this.setupProjectionMatrix();
      Iterator var3 = this.passes.iterator();

      while(var3.hasNext()) {
         PostEffectPass lv = (PostEffectPass)var3.next();
         lv.setProjectionMatrix(this.projectionMatrix);
      }

      var3 = this.defaultSizedTargets.iterator();

      while(var3.hasNext()) {
         Framebuffer lv2 = (Framebuffer)var3.next();
         lv2.resize(targetsWidth, targetsHeight, MinecraftClient.IS_SYSTEM_MAC);
      }

   }

   public void render(float tickDelta) {
      if (tickDelta < this.lastTickDelta) {
         this.time += 1.0F - this.lastTickDelta;
         this.time += tickDelta;
      } else {
         this.time += tickDelta - this.lastTickDelta;
      }

      for(this.lastTickDelta = tickDelta; this.time > 20.0F; this.time -= 20.0F) {
      }

      Iterator var2 = this.passes.iterator();

      while(var2.hasNext()) {
         PostEffectPass lv = (PostEffectPass)var2.next();
         lv.render(this.time / 20.0F);
      }

   }

   public final String getName() {
      return this.name;
   }

   @Nullable
   private Framebuffer getTarget(@Nullable String name) {
      if (name == null) {
         return null;
      } else {
         return name.equals("minecraft:main") ? this.mainTarget : (Framebuffer)this.targetsByName.get(name);
      }
   }
}
