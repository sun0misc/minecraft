package net.minecraft.client.gl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
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
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidHierarchicalFileException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PathUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ShaderProgram implements ShaderProgramSetupView, AutoCloseable {
   public static final String SHADERS_DIRECTORY = "shaders";
   private static final String CORE_DIRECTORY = "shaders/core/";
   private static final String INCLUDE_DIRECTORY = "shaders/include/";
   static final Logger LOGGER = LogUtils.getLogger();
   private static final Uniform DEFAULT_UNIFORM = new Uniform();
   private static final boolean field_32780 = true;
   private static ShaderProgram activeProgram;
   private static int activeProgramGlRef = -1;
   private final Map samplers = Maps.newHashMap();
   private final List samplerNames = Lists.newArrayList();
   private final List loadedSamplerIds = Lists.newArrayList();
   private final List uniforms = Lists.newArrayList();
   private final List loadedUniformIds = Lists.newArrayList();
   private final Map loadedUniforms = Maps.newHashMap();
   private final int glRef;
   private final String name;
   private boolean dirty;
   private final GlBlendState blendState;
   private final List loadedAttributeIds;
   private final List attributeNames;
   private final ShaderStage vertexShader;
   private final ShaderStage fragmentShader;
   private final VertexFormat format;
   @Nullable
   public final GlUniform modelViewMat;
   @Nullable
   public final GlUniform projectionMat;
   @Nullable
   public final GlUniform viewRotationMat;
   @Nullable
   public final GlUniform textureMat;
   @Nullable
   public final GlUniform screenSize;
   @Nullable
   public final GlUniform colorModulator;
   @Nullable
   public final GlUniform light0Direction;
   @Nullable
   public final GlUniform light1Direction;
   @Nullable
   public final GlUniform glintAlpha;
   @Nullable
   public final GlUniform fogStart;
   @Nullable
   public final GlUniform fogEnd;
   @Nullable
   public final GlUniform fogColor;
   @Nullable
   public final GlUniform fogShape;
   @Nullable
   public final GlUniform lineWidth;
   @Nullable
   public final GlUniform gameTime;
   @Nullable
   public final GlUniform chunkOffset;

   public ShaderProgram(ResourceFactory factory, String name, VertexFormat format) throws IOException {
      this.name = name;
      this.format = format;
      Identifier lv = new Identifier("shaders/core/" + name + ".json");

      try {
         Reader reader = factory.openAsReader(lv);

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
                     this.readSampler(jsonElement);
                  } catch (Exception var20) {
                     InvalidHierarchicalFileException lv2 = InvalidHierarchicalFileException.wrap(var20);
                     lv2.addInvalidKey("samplers[" + i + "]");
                     throw lv2;
                  }
               }
            }

            JsonArray jsonArray2 = JsonHelper.getArray(jsonObject, "attributes", (JsonArray)null);
            if (jsonArray2 != null) {
               int j = 0;
               this.loadedAttributeIds = Lists.newArrayListWithCapacity(jsonArray2.size());
               this.attributeNames = Lists.newArrayListWithCapacity(jsonArray2.size());

               for(Iterator var27 = jsonArray2.iterator(); var27.hasNext(); ++j) {
                  JsonElement jsonElement2 = (JsonElement)var27.next();

                  try {
                     this.attributeNames.add(JsonHelper.asString(jsonElement2, "attribute"));
                  } catch (Exception var19) {
                     InvalidHierarchicalFileException lv3 = InvalidHierarchicalFileException.wrap(var19);
                     lv3.addInvalidKey("attributes[" + j + "]");
                     throw lv3;
                  }
               }
            } else {
               this.loadedAttributeIds = null;
               this.attributeNames = null;
            }

            JsonArray jsonArray3 = JsonHelper.getArray(jsonObject, "uniforms", (JsonArray)null);
            int k;
            if (jsonArray3 != null) {
               k = 0;

               for(Iterator var29 = jsonArray3.iterator(); var29.hasNext(); ++k) {
                  JsonElement jsonElement3 = (JsonElement)var29.next();

                  try {
                     this.addUniform(jsonElement3);
                  } catch (Exception var18) {
                     InvalidHierarchicalFileException lv4 = InvalidHierarchicalFileException.wrap(var18);
                     lv4.addInvalidKey("uniforms[" + k + "]");
                     throw lv4;
                  }
               }
            }

            this.blendState = readBlendState(JsonHelper.getObject(jsonObject, "blend", (JsonObject)null));
            this.vertexShader = loadShader(factory, ShaderStage.Type.VERTEX, string2);
            this.fragmentShader = loadShader(factory, ShaderStage.Type.FRAGMENT, string3);
            this.glRef = GlProgramManager.createProgram();
            if (this.attributeNames != null) {
               k = 0;

               for(UnmodifiableIterator var30 = format.getAttributeNames().iterator(); var30.hasNext(); ++k) {
                  String string4 = (String)var30.next();
                  GlUniform.bindAttribLocation(this.glRef, k, string4);
                  this.loadedAttributeIds.add(k);
               }
            }

            GlProgramManager.linkProgram(this);
            this.loadReferences();
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
         InvalidHierarchicalFileException lv5 = InvalidHierarchicalFileException.wrap(var22);
         lv5.addInvalidFile(lv.getPath());
         throw lv5;
      }

      this.markUniformsDirty();
      this.modelViewMat = this.getUniform("ModelViewMat");
      this.projectionMat = this.getUniform("ProjMat");
      this.viewRotationMat = this.getUniform("IViewRotMat");
      this.textureMat = this.getUniform("TextureMat");
      this.screenSize = this.getUniform("ScreenSize");
      this.colorModulator = this.getUniform("ColorModulator");
      this.light0Direction = this.getUniform("Light0_Direction");
      this.light1Direction = this.getUniform("Light1_Direction");
      this.glintAlpha = this.getUniform("GlintAlpha");
      this.fogStart = this.getUniform("FogStart");
      this.fogEnd = this.getUniform("FogEnd");
      this.fogColor = this.getUniform("FogColor");
      this.fogShape = this.getUniform("FogShape");
      this.lineWidth = this.getUniform("LineWidth");
      this.gameTime = this.getUniform("GameTime");
      this.chunkOffset = this.getUniform("ChunkOffset");
   }

   private static ShaderStage loadShader(final ResourceFactory factory, ShaderStage.Type type, String name) throws IOException {
      ShaderStage lv = (ShaderStage)type.getLoadedShaders().get(name);
      ShaderStage lv3;
      if (lv == null) {
         String string2 = "shaders/core/" + name + type.getFileExtension();
         Resource lv2 = factory.getResourceOrThrow(new Identifier(string2));
         InputStream inputStream = lv2.getInputStream();

         try {
            final String string3 = PathUtil.getPosixFullPath(string2);
            lv3 = ShaderStage.createFromResource(type, name, inputStream, lv2.getResourcePackName(), new GlImportProcessor() {
               private final Set visitedImports = Sets.newHashSet();

               public String loadImport(boolean inline, String name) {
                  String var10000 = inline ? string3 : "shaders/include/";
                  name = PathUtil.normalizeToPosix(var10000 + name);
                  if (!this.visitedImports.add(name)) {
                     return null;
                  } else {
                     Identifier lv = new Identifier(name);

                     try {
                        Reader reader = factory.openAsReader(lv);

                        String var5;
                        try {
                           var5 = IOUtils.toString(reader);
                        } catch (Throwable var8) {
                           if (reader != null) {
                              try {
                                 reader.close();
                              } catch (Throwable var7) {
                                 var8.addSuppressed(var7);
                              }
                           }

                           throw var8;
                        }

                        if (reader != null) {
                           reader.close();
                        }

                        return var5;
                     } catch (IOException var9) {
                        ShaderProgram.LOGGER.error("Could not open GLSL import {}: {}", name, var9.getMessage());
                        return "#error " + var9.getMessage();
                     }
                  }
               }
            });
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
         lv3 = lv;
      }

      return lv3;
   }

   public static GlBlendState readBlendState(JsonObject json) {
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
      Iterator var1 = this.uniforms.iterator();

      while(var1.hasNext()) {
         GlUniform lv = (GlUniform)var1.next();
         lv.close();
      }

      GlProgramManager.deleteProgram(this);
   }

   public void unbind() {
      RenderSystem.assertOnRenderThread();
      GlProgramManager.useProgram(0);
      activeProgramGlRef = -1;
      activeProgram = null;
      int i = GlStateManager._getActiveTexture();

      for(int j = 0; j < this.loadedSamplerIds.size(); ++j) {
         if (this.samplers.get(this.samplerNames.get(j)) != null) {
            GlStateManager._activeTexture(GlConst.GL_TEXTURE0 + j);
            GlStateManager._bindTexture(0);
         }
      }

      GlStateManager._activeTexture(i);
   }

   public void bind() {
      RenderSystem.assertOnRenderThread();
      this.dirty = false;
      activeProgram = this;
      this.blendState.enable();
      if (this.glRef != activeProgramGlRef) {
         GlProgramManager.useProgram(this.glRef);
         activeProgramGlRef = this.glRef;
      }

      int i = GlStateManager._getActiveTexture();

      for(int j = 0; j < this.loadedSamplerIds.size(); ++j) {
         String string = (String)this.samplerNames.get(j);
         if (this.samplers.get(string) != null) {
            int k = GlUniform.getUniformLocation(this.glRef, string);
            GlUniform.uniform1(k, j);
            RenderSystem.activeTexture(GlConst.GL_TEXTURE0 + j);
            Object object = this.samplers.get(string);
            int l = -1;
            if (object instanceof Framebuffer) {
               l = ((Framebuffer)object).getColorAttachment();
            } else if (object instanceof AbstractTexture) {
               l = ((AbstractTexture)object).getGlId();
            } else if (object instanceof Integer) {
               l = (Integer)object;
            }

            if (l != -1) {
               RenderSystem.bindTexture(l);
            }
         }
      }

      GlStateManager._activeTexture(i);
      Iterator var7 = this.uniforms.iterator();

      while(var7.hasNext()) {
         GlUniform lv = (GlUniform)var7.next();
         lv.upload();
      }

   }

   public void markUniformsDirty() {
      this.dirty = true;
   }

   @Nullable
   public GlUniform getUniform(String name) {
      RenderSystem.assertOnRenderThread();
      return (GlUniform)this.loadedUniforms.get(name);
   }

   public Uniform getUniformOrDefault(String name) {
      RenderSystem.assertOnGameThread();
      GlUniform lv = this.getUniform(name);
      return (Uniform)(lv == null ? DEFAULT_UNIFORM : lv);
   }

   private void loadReferences() {
      RenderSystem.assertOnRenderThread();
      IntList intList = new IntArrayList();

      int i;
      for(i = 0; i < this.samplerNames.size(); ++i) {
         String string = (String)this.samplerNames.get(i);
         int j = GlUniform.getUniformLocation(this.glRef, string);
         if (j == -1) {
            LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.name, string);
            this.samplers.remove(string);
            intList.add(i);
         } else {
            this.loadedSamplerIds.add(j);
         }
      }

      for(i = intList.size() - 1; i >= 0; --i) {
         int k = intList.getInt(i);
         this.samplerNames.remove(k);
      }

      Iterator var6 = this.uniforms.iterator();

      while(var6.hasNext()) {
         GlUniform lv = (GlUniform)var6.next();
         String string2 = lv.getName();
         int l = GlUniform.getUniformLocation(this.glRef, string2);
         if (l == -1) {
            LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, string2);
         } else {
            this.loadedUniformIds.add(l);
            lv.setLocation(l);
            this.loadedUniforms.put(string2, lv);
         }
      }

   }

   private void readSampler(JsonElement json) {
      JsonObject jsonObject = JsonHelper.asObject(json, "sampler");
      String string = JsonHelper.getString(jsonObject, "name");
      if (!JsonHelper.hasString(jsonObject, "file")) {
         this.samplers.put(string, (Object)null);
         this.samplerNames.add(string);
      } else {
         this.samplerNames.add(string);
      }
   }

   public void addSampler(String name, Object sampler) {
      this.samplers.put(name, sampler);
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
            lv2.set(Arrays.copyOfRange(fs, 0, j));
         }

         this.uniforms.add(lv2);
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

   public VertexFormat getFormat() {
      return this.format;
   }

   public String getName() {
      return this.name;
   }

   public int getGlRef() {
      return this.glRef;
   }
}
