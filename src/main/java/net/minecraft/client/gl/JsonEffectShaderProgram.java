package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidHierarchicalFileException;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class JsonEffectShaderProgram implements EffectShaderProgram, AutoCloseable {
   private static final String PROGRAM_DIRECTORY = "shaders/program/";
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Uniform DEFAULT_UNIFORM = new Uniform();
   private static final boolean field_32683 = true;
   private static JsonEffectShaderProgram activeProgram;
   private static int activeProgramGlRef = -1;
   private final Map samplerBinds = Maps.newHashMap();
   private final List samplerNames = Lists.newArrayList();
   private final List samplerLocations = Lists.newArrayList();
   private final List uniformData = Lists.newArrayList();
   private final List uniformLocations = Lists.newArrayList();
   private final Map uniformByName = Maps.newHashMap();
   private final int glRef;
   private final String name;
   private boolean uniformStateDirty;
   private final GlBlendState blendState;
   private final List attributeLocations;
   private final List attributeNames;
   private final EffectShaderStage vertexShader;
   private final EffectShaderStage fragmentShader;

   public JsonEffectShaderProgram(ResourceManager resource, String name) throws IOException {
      Identifier lv = new Identifier("shaders/program/" + name + ".json");
      this.name = name;
      Resource lv2 = resource.getResourceOrThrow(lv);

      try {
         Reader reader = lv2.getReader();

         try {
            JsonObject jsonObject = JsonHelper.deserialize((Reader)reader);
            String string2 = JsonHelper.getString(jsonObject, "vertex");
            String string3 = JsonHelper.getString(jsonObject, "fragment");
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "samplers", (JsonArray)null);
            if (jsonArray != null) {
               int i = 0;

               for(Iterator var11 = jsonArray.iterator(); var11.hasNext(); ++i) {
                  JsonElement jsonElement = (JsonElement)var11.next();

                  try {
                     this.addSampler(jsonElement);
                  } catch (Exception var20) {
                     InvalidHierarchicalFileException lv3 = InvalidHierarchicalFileException.wrap(var20);
                     lv3.addInvalidKey("samplers[" + i + "]");
                     throw lv3;
                  }
               }
            }

            JsonArray jsonArray2 = JsonHelper.getArray(jsonObject, "attributes", (JsonArray)null);
            Iterator var27;
            if (jsonArray2 != null) {
               int j = 0;
               this.attributeLocations = Lists.newArrayListWithCapacity(jsonArray2.size());
               this.attributeNames = Lists.newArrayListWithCapacity(jsonArray2.size());

               for(var27 = jsonArray2.iterator(); var27.hasNext(); ++j) {
                  JsonElement jsonElement2 = (JsonElement)var27.next();

                  try {
                     this.attributeNames.add(JsonHelper.asString(jsonElement2, "attribute"));
                  } catch (Exception var19) {
                     InvalidHierarchicalFileException lv4 = InvalidHierarchicalFileException.wrap(var19);
                     lv4.addInvalidKey("attributes[" + j + "]");
                     throw lv4;
                  }
               }
            } else {
               this.attributeLocations = null;
               this.attributeNames = null;
            }

            JsonArray jsonArray3 = JsonHelper.getArray(jsonObject, "uniforms", (JsonArray)null);
            if (jsonArray3 != null) {
               int k = 0;

               for(Iterator var29 = jsonArray3.iterator(); var29.hasNext(); ++k) {
                  JsonElement jsonElement3 = (JsonElement)var29.next();

                  try {
                     this.addUniform(jsonElement3);
                  } catch (Exception var18) {
                     InvalidHierarchicalFileException lv5 = InvalidHierarchicalFileException.wrap(var18);
                     lv5.addInvalidKey("uniforms[" + k + "]");
                     throw lv5;
                  }
               }
            }

            this.blendState = deserializeBlendState(JsonHelper.getObject(jsonObject, "blend", (JsonObject)null));
            this.vertexShader = loadEffect(resource, ShaderStage.Type.VERTEX, string2);
            this.fragmentShader = loadEffect(resource, ShaderStage.Type.FRAGMENT, string3);
            this.glRef = GlProgramManager.createProgram();
            GlProgramManager.linkProgram(this);
            this.finalizeUniformsAndSamplers();
            if (this.attributeNames != null) {
               var27 = this.attributeNames.iterator();

               while(var27.hasNext()) {
                  String string4 = (String)var27.next();
                  int l = GlUniform.getAttribLocation(this.glRef, string4);
                  this.attributeLocations.add(l);
               }
            }
         } catch (Throwable var21) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var17) {
                  var21.addSuppressed(var17);
               }
            }

            throw var21;
         }

         if (reader != null) {
            reader.close();
         }
      } catch (Exception var22) {
         InvalidHierarchicalFileException lv6 = InvalidHierarchicalFileException.wrap(var22);
         String var10001 = lv.getPath();
         lv6.addInvalidFile(var10001 + " (" + lv2.getResourcePackName() + ")");
         throw lv6;
      }

      this.markUniformsDirty();
   }

   public static EffectShaderStage loadEffect(ResourceManager resourceManager, ShaderStage.Type type, String name) throws IOException {
      ShaderStage lv = (ShaderStage)type.getLoadedShaders().get(name);
      if (lv != null && !(lv instanceof EffectShaderStage)) {
         throw new InvalidClassException("Program is not of type EffectProgram");
      } else {
         EffectShaderStage lv4;
         if (lv == null) {
            Identifier lv2 = new Identifier("shaders/program/" + name + type.getFileExtension());
            Resource lv3 = resourceManager.getResourceOrThrow(lv2);
            InputStream inputStream = lv3.getInputStream();

            try {
               lv4 = EffectShaderStage.createFromResource(type, name, inputStream, lv3.getResourcePackName());
            } catch (Throwable var11) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (inputStream != null) {
               inputStream.close();
            }
         } else {
            lv4 = (EffectShaderStage)lv;
         }

         return lv4;
      }
   }

   public static GlBlendState deserializeBlendState(@Nullable JsonObject json) {
      if (json == null) {
         return new GlBlendState();
      } else {
         int i = GlConst.GL_FUNC_ADD;
         int j = 1;
         int k = 0;
         int l = 1;
         int m = 0;
         boolean bl = true;
         boolean bl2 = false;
         if (JsonHelper.hasString(json, "func")) {
            i = GlBlendState.getModeFromString(json.get("func").getAsString());
            if (i != GlConst.GL_FUNC_ADD) {
               bl = false;
            }
         }

         if (JsonHelper.hasString(json, "srcrgb")) {
            j = GlBlendState.getFactorFromString(json.get("srcrgb").getAsString());
            if (j != 1) {
               bl = false;
            }
         }

         if (JsonHelper.hasString(json, "dstrgb")) {
            k = GlBlendState.getFactorFromString(json.get("dstrgb").getAsString());
            if (k != 0) {
               bl = false;
            }
         }

         if (JsonHelper.hasString(json, "srcalpha")) {
            l = GlBlendState.getFactorFromString(json.get("srcalpha").getAsString());
            if (l != 1) {
               bl = false;
            }

            bl2 = true;
         }

         if (JsonHelper.hasString(json, "dstalpha")) {
            m = GlBlendState.getFactorFromString(json.get("dstalpha").getAsString());
            if (m != 0) {
               bl = false;
            }

            bl2 = true;
         }

         if (bl) {
            return new GlBlendState();
         } else {
            return bl2 ? new GlBlendState(j, k, l, m, i) : new GlBlendState(j, k, i);
         }
      }
   }

   public void close() {
      Iterator var1 = this.uniformData.iterator();

      while(var1.hasNext()) {
         GlUniform lv = (GlUniform)var1.next();
         lv.close();
      }

      GlProgramManager.deleteProgram(this);
   }

   public void disable() {
      RenderSystem.assertOnRenderThread();
      GlProgramManager.useProgram(0);
      activeProgramGlRef = -1;
      activeProgram = null;

      for(int i = 0; i < this.samplerLocations.size(); ++i) {
         if (this.samplerBinds.get(this.samplerNames.get(i)) != null) {
            GlStateManager._activeTexture(GlConst.GL_TEXTURE0 + i);
            GlStateManager._bindTexture(0);
         }
      }

   }

   public void enable() {
      RenderSystem.assertOnGameThread();
      this.uniformStateDirty = false;
      activeProgram = this;
      this.blendState.enable();
      if (this.glRef != activeProgramGlRef) {
         GlProgramManager.useProgram(this.glRef);
         activeProgramGlRef = this.glRef;
      }

      for(int i = 0; i < this.samplerLocations.size(); ++i) {
         String string = (String)this.samplerNames.get(i);
         IntSupplier intSupplier = (IntSupplier)this.samplerBinds.get(string);
         if (intSupplier != null) {
            RenderSystem.activeTexture(GlConst.GL_TEXTURE0 + i);
            int j = intSupplier.getAsInt();
            if (j != -1) {
               RenderSystem.bindTexture(j);
               GlUniform.uniform1((Integer)this.samplerLocations.get(i), i);
            }
         }
      }

      Iterator var5 = this.uniformData.iterator();

      while(var5.hasNext()) {
         GlUniform lv = (GlUniform)var5.next();
         lv.upload();
      }

   }

   public void markUniformsDirty() {
      this.uniformStateDirty = true;
   }

   @Nullable
   public GlUniform getUniformByName(String name) {
      RenderSystem.assertOnRenderThread();
      return (GlUniform)this.uniformByName.get(name);
   }

   public Uniform getUniformByNameOrDummy(String name) {
      RenderSystem.assertOnGameThread();
      GlUniform lv = this.getUniformByName(name);
      return (Uniform)(lv == null ? DEFAULT_UNIFORM : lv);
   }

   private void finalizeUniformsAndSamplers() {
      RenderSystem.assertOnRenderThread();
      IntList intList = new IntArrayList();

      int i;
      for(i = 0; i < this.samplerNames.size(); ++i) {
         String string = (String)this.samplerNames.get(i);
         int j = GlUniform.getUniformLocation(this.glRef, string);
         if (j == -1) {
            LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.name, string);
            this.samplerBinds.remove(string);
            intList.add(i);
         } else {
            this.samplerLocations.add(j);
         }
      }

      for(i = intList.size() - 1; i >= 0; --i) {
         this.samplerNames.remove(intList.getInt(i));
      }

      Iterator var6 = this.uniformData.iterator();

      while(var6.hasNext()) {
         GlUniform lv = (GlUniform)var6.next();
         String string2 = lv.getName();
         int k = GlUniform.getUniformLocation(this.glRef, string2);
         if (k == -1) {
            LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, string2);
         } else {
            this.uniformLocations.add(k);
            lv.setLocation(k);
            this.uniformByName.put(string2, lv);
         }
      }

   }

   private void addSampler(JsonElement json) {
      JsonObject jsonObject = JsonHelper.asObject(json, "sampler");
      String string = JsonHelper.getString(jsonObject, "name");
      if (!JsonHelper.hasString(jsonObject, "file")) {
         this.samplerBinds.put(string, (Object)null);
         this.samplerNames.add(string);
      } else {
         this.samplerNames.add(string);
      }
   }

   public void bindSampler(String samplerName, IntSupplier intSupplier) {
      if (this.samplerBinds.containsKey(samplerName)) {
         this.samplerBinds.remove(samplerName);
      }

      this.samplerBinds.put(samplerName, intSupplier);
      this.markUniformsDirty();
   }

   private void addUniform(JsonElement json) throws InvalidHierarchicalFileException {
      JsonObject jsonObject = JsonHelper.asObject(json, "uniform");
      String string = JsonHelper.getString(jsonObject, "name");
      int i = GlUniform.getTypeIndex(JsonHelper.getString(jsonObject, "type"));
      int j = JsonHelper.getInt(jsonObject, "count");
      float[] fs = new float[Math.max(j, 16)];
      JsonArray jsonArray = JsonHelper.getArray(jsonObject, "values");
      if (jsonArray.size() != j && jsonArray.size() > 1) {
         throw new InvalidHierarchicalFileException("Invalid amount of values specified (expected " + j + ", found " + jsonArray.size() + ")");
      } else {
         int k = 0;

         for(Iterator var9 = jsonArray.iterator(); var9.hasNext(); ++k) {
            JsonElement jsonElement2 = (JsonElement)var9.next();

            try {
               fs[k] = JsonHelper.asFloat(jsonElement2, "value");
            } catch (Exception var13) {
               InvalidHierarchicalFileException lv = InvalidHierarchicalFileException.wrap(var13);
               lv.addInvalidKey("values[" + k + "]");
               throw lv;
            }
         }

         if (j > 1 && jsonArray.size() == 1) {
            while(k < j) {
               fs[k] = fs[0];
               ++k;
            }
         }

         int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
         GlUniform lv2 = new GlUniform(string, i + l, j, this);
         if (i <= 3) {
            lv2.setForDataType((int)fs[0], (int)fs[1], (int)fs[2], (int)fs[3]);
         } else if (i <= 7) {
            lv2.setForDataType(fs[0], fs[1], fs[2], fs[3]);
         } else {
            lv2.set(fs);
         }

         this.uniformData.add(lv2);
      }
   }

   public ShaderStage getVertexShader() {
      return this.vertexShader;
   }

   public ShaderStage getFragmentShader() {
      return this.fragmentShader;
   }

   public void attachReferencedShaders() {
      this.fragmentShader.attachTo(this);
      this.vertexShader.attachTo(this);
   }

   public String getName() {
      return this.name;
   }

   public int getGlRef() {
      return this.glRef;
   }
}
